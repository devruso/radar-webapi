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
        List<CursoDTO> dtos = cursoRepository.findAll().stream().map(CursoMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CursoDTO> getOne(@PathVariable Long id) {
        return cursoRepository.findById(id)
                .map(c -> ResponseEntity.ok(CursoMapper.toDto(c)))
                .orElseThrow(() -> new ResourceNotFoundException("Curso com ID " + id + " não encontrado"));
    }
}
