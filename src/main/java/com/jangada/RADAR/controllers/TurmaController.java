package com.jangada.RADAR.controllers;

import com.jangada.RADAR.mappers.TurmaMapper;
import com.jangada.RADAR.models.dtos.TurmaDTO;
import com.jangada.RADAR.models.entities.Turma;
import com.jangada.RADAR.repositories.TurmaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/turmas")
public class TurmaController {

    private final TurmaRepository turmaRepository;

    public TurmaController(TurmaRepository turmaRepository) {
        this.turmaRepository = turmaRepository;
    }

    @GetMapping
    public ResponseEntity<List<TurmaDTO>> listAll() {
        List<TurmaDTO> dtos = turmaRepository.findAll().stream().map(TurmaMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TurmaDTO> getOne(@PathVariable Long id) {
        return turmaRepository.findById(id).map(t -> ResponseEntity.ok(TurmaMapper.toDto(t))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TurmaDTO> create(@RequestBody TurmaDTO dto) {
        Turma t = new Turma();
        t.setLocal(dto.getLocal());
        t.setProfessor(dto.getProfessor());
        t.setNumero(dto.getNumero());
        t.setTipo(dto.getTipo());
        // associations must be set in service layer or by client using existing ids
        Turma saved = turmaRepository.save(t);
        return ResponseEntity.ok(TurmaMapper.toDto(saved));
    }

}
