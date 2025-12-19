package com.jangada.RADAR.controllers;

import com.jangada.RADAR.models.dtos.AvaliacaoProfessorDTO;
import com.jangada.RADAR.models.dtos.RecomendacaoTurmaDTO;
import com.jangada.RADAR.services.RecomendacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recomendacoes")
@Tag(name = "Recomendações", description = "Endpoints para geração de recomendações de disciplinas e avaliação de professores")
public class RecomendacaoController {

    private final RecomendacaoService recomendacaoService;

    public RecomendacaoController(RecomendacaoService recomendacaoService) {
        this.recomendacaoService = recomendacaoService;
    }

    @PostMapping("/gerar/{usuarioId}")
    @Operation(
            summary = "Gerar recomendações de disciplinas",
            description = "Gera uma lista de disciplinas recomendadas para um aluno considerando: " +
                    "disciplinas já feitas, professores excluídos, vagas disponíveis, pré-requisitos, " +
                    "dificuldade e rating de professores."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recomendações geradas com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<List<RecomendacaoTurmaDTO>> gerarRecomendacoes(
            @PathVariable
            @Parameter(description = "ID do usuário (aluno)")
            Long usuarioId,
            @RequestParam(defaultValue = "burrinho")
            @Parameter(description = "Método de recomendação: 'burrinho' (simples) ou 'busca' (futuro)")
            String metodo) {
        List<RecomendacaoTurmaDTO> result = recomendacaoService.recomendar(usuarioId, metodo);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/avaliar-professor")
    @Operation(
            summary = "Avaliar professor",
            description = "Registra a avaliação de um professor após a conclusão de uma disciplina. " +
                    "Escala: 1 (ruim) a 5 (excelente). Esta avaliação influencia futuras recomendações."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Avaliação registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Nota inválida (deve estar entre 1 e 5)"),
            @ApiResponse(responseCode = "404", description = "Usuário ou componente não encontrado")
    })
    public ResponseEntity<AvaliacaoProfessorDTO> avaliarProfessor(
            @RequestParam
            @Parameter(description = "ID do usuário (aluno que está avaliando)")
            Long usuarioId,
            @RequestParam
            @Parameter(description = "Nome do professor a ser avaliado")
            String professorNome,
            @RequestParam
            @Parameter(description = "ID do componente curricular (disciplina)")
            Long componenteId,
            @RequestParam
            @Parameter(description = "Nota de 1 a 5", example = "4")
            Integer nota,
            @RequestParam(required = false)
            @Parameter(description = "Comentário opcional sobre a experiência")
            String comentario) {
        AvaliacaoProfessorDTO result = recomendacaoService.avaliarProfessor(
                usuarioId,
                professorNome,
                componenteId,
                nota,
                comentario
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/professor/{professorNome}/avaliacoes")
    @Operation(
            summary = "Obter avaliações de um professor",
            description = "Retorna todas as avaliações registradas para um professor específico"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Avaliações recuperadas com sucesso")
    })
    public ResponseEntity<List<AvaliacaoProfessorDTO>> obterAvaliacoesProfessor(
            @PathVariable
            @Parameter(description = "Nome do professor")
            String professorNome) {
        List<AvaliacaoProfessorDTO> result = recomendacaoService.obterAvaliacoesProfessor(professorNome);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/professor/{professorNome}/score")
    @Operation(
            summary = "Obter score médio de um professor",
            description = "Retorna o score médio (1-5) de um professor para uma disciplina específica"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Score calculado com sucesso")
    })
    public ResponseEntity<Map<String, Object>> obterScoreProfessor(
            @PathVariable
            @Parameter(description = "Nome do professor")
            String professorNome,
            @RequestParam
            @Parameter(description = "ID do componente curricular (disciplina)")
            Long componenteId) {
        Double score = recomendacaoService.obterScoreProfessor(professorNome, componenteId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("professorNome", professorNome);
        response.put("componenteId", componenteId);
        response.put("score", score);
        response.put("qualidade", score < 2.5 ? "BAIXA" : score < 3.5 ? "MÉDIA" : "ALTA");
        
        return ResponseEntity.ok(response);
    }
}

