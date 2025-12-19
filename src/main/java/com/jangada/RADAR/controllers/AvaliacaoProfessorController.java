package com.jangada.RADAR.controllers;

import com.jangada.RADAR.models.dtos.AvaliacaoProfessorDTO;
import com.jangada.RADAR.models.entities.AvaliacaoProfessor;
import com.jangada.RADAR.repositories.AvaliacaoProfessorRepository;
import com.jangada.RADAR.repositories.ComponenteCurricularRepository;
import com.jangada.RADAR.repositories.UsuarioRepository;
import com.jangada.RADAR.mappers.AvaliacaoProfessorMapper;
import com.jangada.RADAR.exceptions.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/avaliacoes-professor")
@Tag(name = "Avaliações de Professores", description = "Gerenciamento de avaliações de professores pelos alunos")
public class AvaliacaoProfessorController {

    private final AvaliacaoProfessorRepository avaliacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ComponenteCurricularRepository componenteRepository;

    public AvaliacaoProfessorController(
            AvaliacaoProfessorRepository avaliacaoRepository,
            UsuarioRepository usuarioRepository,
            ComponenteCurricularRepository componenteRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.componenteRepository = componenteRepository;
    }

    @GetMapping
    @Operation(summary = "Listar todas as avaliações", description = "Retorna todas as avaliações de professores registradas")
    public ResponseEntity<List<AvaliacaoProfessorDTO>> listarTodas() {
        List<AvaliacaoProfessorDTO> avaliacoes = avaliacaoRepository.findAll()
                .stream()
                .map(AvaliacaoProfessorMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(avaliacoes);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter avaliação por ID")
    public ResponseEntity<AvaliacaoProfessorDTO> obterPorId(
            @PathVariable
            @Parameter(description = "ID da avaliação")
            Long id) {
        AvaliacaoProfessor avaliacao = avaliacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Avaliação não encontrada"));
        return ResponseEntity.ok(AvaliacaoProfessorMapper.toDto(avaliacao));
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(
            summary = "Obter avaliações de um usuário",
            description = "Retorna todas as avaliações de professores feitas por um aluno específico"
    )
    public ResponseEntity<List<AvaliacaoProfessorDTO>> obterPorUsuario(
            @PathVariable
            @Parameter(description = "ID do usuário (aluno)")
            Long usuarioId) {
        usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        List<AvaliacaoProfessorDTO> avaliacoes = avaliacaoRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(AvaliacaoProfessorMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(avaliacoes);
    }

    @GetMapping("/professor/{professorNome}")
    @Operation(
            summary = "Obter todas as avaliações de um professor",
            description = "Retorna todas as avaliações registradas para um professor específico"
    )
    public ResponseEntity<List<AvaliacaoProfessorDTO>> obterPorProfessor(
            @PathVariable
            @Parameter(description = "Nome do professor")
            String professorNome) {
        List<AvaliacaoProfessorDTO> avaliacoes = avaliacaoRepository.findByProfessorNome(professorNome)
                .stream()
                .map(AvaliacaoProfessorMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(avaliacoes);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar uma avaliação")
    public ResponseEntity<Void> deletar(
            @PathVariable
            @Parameter(description = "ID da avaliação")
            Long id) {
        AvaliacaoProfessor avaliacao = avaliacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Avaliação não encontrada"));
        avaliacaoRepository.delete(avaliacao);
        return ResponseEntity.noContent().build();
    }
}
