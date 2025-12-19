package com.jangada.RADAR.controllers;

import com.jangada.RADAR.models.dtos.PreRequisitoDTO;
import com.jangada.RADAR.models.entities.PreRequisito;
import com.jangada.RADAR.models.entities.ComponenteCurricular;
import com.jangada.RADAR.repositories.PreRequisitoRepository;
import com.jangada.RADAR.repositories.ComponenteCurricularRepository;
import com.jangada.RADAR.mappers.PreRequisitoMapper;
import com.jangada.RADAR.exceptions.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/prerequisitos")
@Tag(name = "Pré-requisitos", description = "Gerenciamento de dependências entre disciplinas")
public class PreRequisitoController {

    private final PreRequisitoRepository preRequisitoRepository;
    private final ComponenteCurricularRepository componenteRepository;

    public PreRequisitoController(
            PreRequisitoRepository preRequisitoRepository,
            ComponenteCurricularRepository componenteRepository) {
        this.preRequisitoRepository = preRequisitoRepository;
        this.componenteRepository = componenteRepository;
    }

    @GetMapping
    @Operation(summary = "Listar todos os pré-requisitos", description = "Retorna todas as relações de pré-requisitos")
    public ResponseEntity<List<PreRequisitoDTO>> listarTodos() {
        List<PreRequisitoDTO> preRequisitos = preRequisitoRepository.findAll()
                .stream()
                .map(PreRequisitoMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(preRequisitos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter pré-requisito por ID")
    public ResponseEntity<PreRequisitoDTO> obterPorId(
            @PathVariable
            @Parameter(description = "ID do pré-requisito")
            Long id) {
        PreRequisito preRequisito = preRequisitoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pré-requisito não encontrado"));
        return ResponseEntity.ok(PreRequisitoMapper.toDto(preRequisito));
    }

    @GetMapping("/componente/{componenteId}")
    @Operation(
            summary = "Obter pré-requisitos de uma disciplina",
            description = "Retorna todos os pré-requisitos necessários para cursar uma disciplina"
    )
    public ResponseEntity<List<PreRequisitoDTO>> obterPorComponente(
            @PathVariable
            @Parameter(description = "ID do componente curricular (disciplina)")
            Long componenteId) {
        componenteRepository.findById(componenteId)
                .orElseThrow(() -> new ResourceNotFoundException("Componente não encontrado"));

        List<PreRequisitoDTO> preRequisitos = preRequisitoRepository.findByComponenteId(componenteId)
                .stream()
                .map(PreRequisitoMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(preRequisitos);
    }

    @GetMapping("/componente/{componenteId}/tipo/{tipo}")
    @Operation(
            summary = "Obter pré-requisitos por tipo",
            description = "Retorna pré-requisitos de um tipo específico (PREREQUISITO, COREQUISITO, POSREQUISITO)"
    )
    public ResponseEntity<List<PreRequisitoDTO>> obterPorComponenteETipo(
            @PathVariable
            @Parameter(description = "ID do componente curricular")
            Long componenteId,
            @PathVariable
            @Parameter(description = "Tipo de pré-requisito: PREREQUISITO, COREQUISITO ou POSREQUISITO", example = "PREREQUISITO")
            String tipo) {
        componenteRepository.findById(componenteId)
                .orElseThrow(() -> new ResourceNotFoundException("Componente não encontrado"));

        List<PreRequisitoDTO> preRequisitos = preRequisitoRepository.findByComponenteIdAndTipo(componenteId, tipo)
                .stream()
                .map(PreRequisitoMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(preRequisitos);
    }

    @PostMapping
    @Operation(
            summary = "Criar novo pré-requisito",
            description = "Define uma relação de pré-requisito entre duas disciplinas"
    )
    public ResponseEntity<PreRequisitoDTO> criar(
            @RequestParam
            @Parameter(description = "ID do componente que terá pré-requisito")
            Long componenteId,
            @RequestParam
            @Parameter(description = "ID do componente que é pré-requisito")
            Long componentePreRequisitoId,
            @RequestParam(defaultValue = "PREREQUISITO")
            @Parameter(description = "Tipo de relação: PREREQUISITO, COREQUISITO ou POSREQUISITO")
            String tipo) {

        ComponenteCurricular componente = componenteRepository.findById(componenteId)
                .orElseThrow(() -> new ResourceNotFoundException("Componente não encontrado"));

        ComponenteCurricular componentePreReq = componenteRepository.findById(componentePreRequisitoId)
                .orElseThrow(() -> new ResourceNotFoundException("Componente pré-requisito não encontrado"));

        PreRequisito preRequisito = PreRequisito.builder()
                .componente(componente)
                .componentePreRequisito(componentePreReq)
                .tipo(tipo)
                .build();

        PreRequisito saved = preRequisitoRepository.save(preRequisito);
        return ResponseEntity.ok(PreRequisitoMapper.toDto(saved));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar um pré-requisito")
    public ResponseEntity<Void> deletar(
            @PathVariable
            @Parameter(description = "ID do pré-requisito")
            Long id) {
        PreRequisito preRequisito = preRequisitoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pré-requisito não encontrado"));
        preRequisitoRepository.delete(preRequisito);
        return ResponseEntity.noContent().build();
    }
}
