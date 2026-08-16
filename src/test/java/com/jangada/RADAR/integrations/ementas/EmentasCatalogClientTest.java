package com.jangada.RADAR.integrations.ementas;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

class EmentasCatalogClientTest {

    private HttpServer goodServer;
    private HttpServer failingServer;

    @AfterEach
    void stopServers() {
        if (goodServer != null) goodServer.stop(0);
        if (failingServer != null) failingServer.stop(0);
    }

    @Test
    void paginatesAnAuthenticatedSourceAndToleratesAnotherSourceFailure() throws IOException {
        AtomicBoolean bearerReceived = new AtomicBoolean(false);
        goodServer = server();
        goodServer.createContext("/api/auth/login", exchange -> json(exchange, 200, "{\"accessToken\":\"test-token\"}"));
        goodServer.createContext("/api/components", exchange -> {
            bearerReceived.set("Bearer test-token".equals(exchange.getRequestHeaders().getFirst("Authorization")));
            boolean secondPage = exchange.getRequestURI().getQuery().contains("page=1");
            String code = secondPage ? "MAT002" : "MAT001";
            int page = secondPage ? 1 : 0;
            json(exchange, 200, """
                {"results":[{"id":"%s-id","code":"%s","name":"Component %s", "curriculumContexts":[]}],
                 "total":2,"meta":{"page":%d,"limit":1,"total":2,"totalPages":2}}
                """.formatted(code, code, code, page));
        });
        goodServer.start();

        failingServer = server();
        failingServer.createContext("/api/auth/login", exchange -> json(exchange, 503, "{\"message\":\"offline\"}"));
        failingServer.start();

        EmentasCatalogClient client = new EmentasCatalogClient(
            baseUrl(goodServer) + "/api," + baseUrl(failingServer) + "/api",
            "radar@example.test",
            "secret",
            1
        );

        EmentasApiModels.CatalogSnapshot snapshot = client.fetchGraduationCatalog();

        assertThat(snapshot.components()).extracting(item -> item.component().code())
            .containsExactly("MAT001", "MAT002");
        assertThat(snapshot.sources()).hasSize(2);
        assertThat(snapshot.sources().get(0).successful()).isTrue();
        assertThat(snapshot.sources().get(1).successful()).isFalse();
        assertThat(bearerReceived).isTrue();
    }

    private static HttpServer server() throws IOException {
        return HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    }

    private static String baseUrl(HttpServer server) {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    private static void json(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
