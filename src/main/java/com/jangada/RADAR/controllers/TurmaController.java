package com.jangada.RADAR.controllers;

import com.jangada.RADAR.mappers.TurmaMapper;
import com.jangada.RADAR.models.dtos.TurmaDTO;
import com.jangada.RADAR.models.entities.Turma;
import com.jangada.RADAR.repositories.TurmaRepository;
import com.jangada.RADAR.repositories.ComponenteCurricularRepository;
import com.jangada.RADAR.exceptions.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/turmas")
public class TurmaController {

    private final TurmaRepository turmaRepository;
    private final ComponenteCurricularRepository componenteRepository;

    public TurmaController(TurmaRepository turmaRepository, ComponenteCurricularRepository componenteRepository) {
        this.turmaRepository = turmaRepository;
        this.componenteRepository = componenteRepository;
    }

    @GetMapping
    public ResponseEntity<List<TurmaDTO>> listAll() {
        // Usa query otimizada com FETCH JOIN para evitar N+1 queries
        List<TurmaDTO> dtos = turmaRepository.findAllWithDetails().stream()
                .map(TurmaMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TurmaDTO> getOne(@PathVariable Long id) {
        return turmaRepository.findById(id)
                .map(t -> ResponseEntity.ok(TurmaMapper.toDto(t)))
                .orElseThrow(() -> new ResourceNotFoundException("Turma com ID " + id + " não encontrada"));
    }

    @PostMapping
    public ResponseEntity<TurmaDTO> create(@Valid @RequestBody TurmaDTO dto) {
        Turma t = new Turma();
        t.setLocal(dto.getLocal());
        t.setProfessor(dto.getProfessor());
        t.setNumero(dto.getNumero());
        t.setTipo(dto.getTipo());
        
        // Set component
        if (dto.getComponenteId() != null) {
            t.setComponenteCurricular(componenteRepository.findById(dto.getComponenteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Componente curricular não encontrado")));
        }
        
        Turma saved = turmaRepository.save(t);
        return ResponseEntity.ok(TurmaMapper.toDto(saved));
    }

}
