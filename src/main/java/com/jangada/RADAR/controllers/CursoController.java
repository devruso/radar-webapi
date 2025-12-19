package com.jangada.RADAR.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jangada.RADAR.exceptions.ResourceNotFoundException;
import com.jangada.RADAR.mappers.CursoMapper;
import com.jangada.RADAR.models.dtos.CursoDTO;
import com.jangada.RADAR.repositories.CursoRepository;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    private final CursoRepository cursoRepository;

    public CursoController(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    @GetMapping
    public ResponseEntity<List<CursoDTO>> listAll() {
        // Usa query otimizada com FETCH JOIN para evitar N+1 queries
        List<CursoDTO> dtos = cursoRepository.findAllWithDetails()
                .stream()
                .map(CursoMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CursoDTO> getOne(@PathVariable Long id) {
        // Usa query otimizada com FETCH JOIN para evitar N+1 queries
        return cursoRepository.findByIdWithDetails(id)
                .map(c -> ResponseEntity.ok(CursoMapper.toDto(c)))
                .orElseThrow(() -> new ResourceNotFoundException("Curso com ID " + id + " não encontrado"));
    }
}
