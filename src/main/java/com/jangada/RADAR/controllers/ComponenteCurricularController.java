package com.jangada.RADAR.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jangada.RADAR.exceptions.ResourceNotFoundException;
import com.jangada.RADAR.mappers.ComponenteCurricularMapper;
import com.jangada.RADAR.models.dtos.ComponenteCurricularDTO;
import com.jangada.RADAR.repositories.ComponenteCurricularRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/componentes")
@Tag(name = "Componentes Curriculares", description = "API para gerenciamento de componentes curriculares")
public class ComponenteCurricularController {
    
    private final ComponenteCurricularRepository componenteRepository;
    
    public ComponenteCurricularController(ComponenteCurricularRepository componenteRepository) {
        this.componenteRepository = componenteRepository;
    }
    
    @GetMapping
    @Operation(summary = "Listar todos os componentes curriculares")
    public ResponseEntity<List<ComponenteCurricularDTO>> listAll(
        @RequestParam(required = false) String curso,
        @RequestParam(defaultValue = "true") boolean somenteContextoAtivo
    ) {
        var components = curso == null || curso.isBlank()
            ? componenteRepository.findAllWithTurmas()
            : componenteRepository.findAllByCourseContext(curso.trim(), somenteContextoAtivo);
        List<ComponenteCurricularDTO> dtos = components
                .stream()
                .map(ComponenteCurricularMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Buscar componente por ID")
    public ResponseEntity<ComponenteCurricularDTO> findById(@PathVariable Long id) {
        return componenteRepository.findByIdWithTurmas(id)
                .map(ComponenteCurricularMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Componente com ID " + id + " não encontrado"));
    }
    
    @GetMapping("/codigo/{codigo}")
    @Operation(summary = "Buscar componente por código")
    public ResponseEntity<ComponenteCurricularDTO> findByCodigo(@PathVariable String codigo) {
        return componenteRepository.findByCodigoWithTurmas(codigo.trim().toUpperCase())
                .map(ComponenteCurricularMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Componente com código " + codigo + " não encontrado"));
    }

}
