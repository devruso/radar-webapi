package com.jangada.RADAR.services;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jangada.RADAR.models.entities.Curso;
import com.jangada.RADAR.repositories.CursoRepository;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest(properties = "debug=false")
@AutoConfigureMockMvc
class AuthenticationFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CursoRepository cursoRepository;

    private Long cursoId;

    @BeforeEach
    void createCourse() {
        cursoId = cursoRepository.save(Curso.builder()
                .nome("Sistemas de Informação")
                .nivel("Graduação")
                .turno("Noturno")
                .build()).getId();
    }

    @Test
    void protectsUserDataAndIssuesScopedTokens() throws Exception {
        String firstAuth = mockMvc.perform(post("/api/usuarios/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Primeiro Aluno",
                                  "email": "primeiro@ufba.br",
                                  "senha": "senha-segura-123",
                                  "confirmarSenha": "senha-segura-123",
                                  "cursoId": %d,
                                  "mesIngresso": 3,
                                  "anoIngresso": 2024,
                                  "perfilInicial": 1,
                                  "periodosRegularesCursados": 4,
                                  "coeficienteRendimento": 8.25,
                                  "statusFormando": false
                                }
                                """.formatted(cursoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.usuario.email").value("primeiro@ufba.br"))
                .andReturn().getResponse().getContentAsString();
        String firstToken = capture(firstAuth, "\\\"accessToken\\\":\\\"([^\\\"]+)\\\"");
        long firstUserId = Long.parseLong(capture(firstAuth, "\\\"usuario\\\":\\{\\\"id\\\":(\\d+)"));

        String secondAuth = mockMvc.perform(post("/api/usuarios/teste")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cursoId": %d,
                                  "mesIngresso": 8,
                                  "anoIngresso": 2025,
                                  "perfilInicial": 1,
                                  "periodosRegularesCursados": 2,
                                  "coeficienteRendimento": 7.5,
                                  "statusFormando": false
                                }
                                """.formatted(cursoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.mesIngresso").value(8))
                .andReturn().getResponse().getContentAsString();
        long secondUserId = Long.parseLong(capture(secondAuth, "\\\"usuario\\\":\\{\\\"id\\\":(\\d+)"));

        mockMvc.perform(get("/api/usuarios/{id}", firstUserId))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/usuarios/{id}", firstUserId)
                        .header("Authorization", "Bearer " + firstToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(firstUserId));

        mockMvc.perform(get("/api/usuarios/{id}", secondUserId)
                        .header("Authorization", "Bearer " + firstToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/recomendacoes/gerar/{id}", secondUserId)
                        .header("Authorization", "Bearer " + firstToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + firstToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/turmas")
                        .header("Authorization", "Bearer " + firstToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/usuarios/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Email Duplicado",
                                  "email": "PRIMEIRO@UFBA.BR",
                                  "senha": "senha-segura-123",
                                  "confirmarSenha": "senha-segura-123",
                                  "cursoId": %d,
                                  "mesIngresso": 3,
                                  "anoIngresso": 2024,
                                  "perfilInicial": 1,
                                  "periodosRegularesCursados": 4,
                                  "coeficienteRendimento": 8.25,
                                  "statusFormando": false
                                }
                                """.formatted(cursoId)))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"primeiro@ufba.br\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void acceptsConfiguredCorsPreflight() throws Exception {
        mockMvc.perform(options("/api/usuarios/login")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    @Test
    void refusesAdministrativeImportsWhenKeysAreNotConfigured() throws Exception {
        mockMvc.perform(post("/api/catalogo/sincronizar"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/ofertas/importar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "source": "test",
                                  "periodoLetivo": "2026.1",
                                  "substituirPeriodo": true,
                                  "ofertas": [{
                                    "externalKey": "test-1",
                                    "componenteCodigo": "MAT001",
                                    "componenteNome": "Matemática I",
                                    "numero": "01",
                                    "professor": "Professor",
                                    "local": "Sala 1",
                                    "turno": "MATUTINO",
                                    "horarios": {"SEG": "08:00-10:00"}
                                  }]
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    private static String capture(String json, String expression) {
        Matcher matcher = Pattern.compile(expression).matcher(json);
        if (!matcher.find()) {
            throw new AssertionError("Resposta JSON não corresponde ao contrato esperado");
        }
        return matcher.group(1);
    }
}
