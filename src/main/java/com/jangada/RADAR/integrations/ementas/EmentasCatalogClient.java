package com.jangada.RADAR.integrations.ementas;

import static com.jangada.RADAR.integrations.ementas.EmentasApiModels.CatalogSnapshot;
import static com.jangada.RADAR.integrations.ementas.EmentasApiModels.LoginResponse;
import static com.jangada.RADAR.integrations.ementas.EmentasApiModels.Page;
import static com.jangada.RADAR.integrations.ementas.EmentasApiModels.SourceStatus;
import static com.jangada.RADAR.integrations.ementas.EmentasApiModels.SourcedComponent;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class EmentasCatalogClient {

    private static final Logger log = LoggerFactory.getLogger(EmentasCatalogClient.class);
    private static final int MAX_PAGES = 10_000;

    private final List<String> baseUrls;
    private final String email;
    private final String password;
    private final int pageSize;

    public EmentasCatalogClient(
        @Value("${radar.ementas.base-urls}") String baseUrls,
        @Value("${radar.ementas.email:}") String email,
        @Value("${radar.ementas.password:}") String password,
        @Value("${radar.ementas.page-size:100}") int pageSize
    ) {
        this.baseUrls = normalizeBaseUrls(baseUrls);
        this.email = email == null ? "" : email.trim();
        this.password = password == null ? "" : password;
        this.pageSize = Math.max(1, Math.min(pageSize, 500));
    }

    public CatalogSnapshot fetchGraduationCatalog() {
        if (email.isBlank() || password.isBlank()) {
            throw new EmentasCatalogException(
                "Configure EMENTAS_API_EMAIL e EMENTAS_API_PASSWORD para sincronizar o catálogo."
            );
        }
        if (baseUrls.isEmpty()) {
            throw new EmentasCatalogException("Configure ao menos uma URL em EMENTAS_API_BASE_URLS.");
        }

        List<SourcedComponent> components = new ArrayList<>();
        List<SourceStatus> statuses = new ArrayList<>();

        for (String sourceUrl : baseUrls) {
            try {
                List<EmentasApiModels.Component> sourceComponents = fetchSource(sourceUrl);
                sourceComponents.stream()
                    .map(component -> new SourcedComponent(sourceUrl, component))
                    .forEach(components::add);
                statuses.add(new SourceStatus(sourceUrl, sourceComponents.size(), null));
            } catch (RestClientException | EmentasCatalogException exception) {
                log.warn("Ementas source {} unavailable: {}", sourceUrl, exception.getMessage());
                statuses.add(new SourceStatus(sourceUrl, 0, safeError(exception)));
            }
        }

        if (statuses.stream().noneMatch(SourceStatus::successful)) {
            throw new EmentasCatalogException(
                "Nenhuma fonte Ementas pôde ser consultada: "
                    + statuses.stream().map(SourceStatus::error).toList()
            );
        }

        return new CatalogSnapshot(List.copyOf(components), List.copyOf(statuses));
    }

    private List<EmentasApiModels.Component> fetchSource(String sourceUrl) {
        RestClient client = createClient(sourceUrl);
        LoginResponse login = client.post()
            .uri("/auth/login")
            .body(Map.of("email", email, "password", password))
            .retrieve()
            .body(LoginResponse.class);

        String token = login == null ? null : login.bearerToken();
        if (token == null || token.isBlank()) {
            throw new EmentasCatalogException("A autenticação não retornou um access token.");
        }

        List<EmentasApiModels.Component> components = new ArrayList<>();
        for (int pageNumber = 0; pageNumber < MAX_PAGES; pageNumber++) {
            final int requestedPage = pageNumber;
            Page page = client.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/components")
                    .queryParam("page", requestedPage)
                    .queryParam("limit", pageSize)
                    .queryParam("academicLevel", "graduacao")
                    .queryParam("sortBy", "code")
                    .queryParam("sortOrder", "ASC")
                    .build())
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(Page.class);

            if (page == null || page.results() == null || page.results().isEmpty()) {
                break;
            }
            components.addAll(page.results());

            int totalPages = page.meta() == null ? 0 : page.meta().totalPages();
            if ((totalPages > 0 && pageNumber + 1 >= totalPages)
                    || components.size() >= page.total()) {
                break;
            }
        }
        return components;
    }

    private RestClient createClient(String sourceUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(30));
        return RestClient.builder()
            .baseUrl(sourceUrl)
            .requestFactory(requestFactory)
            .build();
    }

    private static List<String> normalizeBaseUrls(String rawBaseUrls) {
        if (rawBaseUrls == null) {
            return List.of();
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        Arrays.stream(rawBaseUrls.split(","))
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .map(value -> value.replaceAll("/+$", ""))
            .forEach(normalized::add);
        return List.copyOf(normalized);
    }

    private static String safeError(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message.length() > 300 ? message.substring(0, 300) : message;
    }
}
