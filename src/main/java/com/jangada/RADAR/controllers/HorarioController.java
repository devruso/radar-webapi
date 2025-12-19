package com.jangada.RADAR.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jangada.RADAR.exceptions.ResourceNotFoundException;
import com.jangada.RADAR.mappers.HorarioMapper;
import com.jangada.RADAR.models.dtos.HorarioDTO;
import com.jangada.RADAR.repositories.HorarioRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/horarios")
@Tag(name = "Horários", description = "API para gerenciamento de horários")
public class HorarioController {
    
    private final HorarioRepository horarioRepository;
    
    public HorarioController(HorarioRepository horarioRepository) {
        this.horarioRepository = horarioRepository;
    }
    
    @GetMapping
    @Operation(summary = "Listar todos os horários")
    public ResponseEntity<List<HorarioDTO>> listAll() {
        List<HorarioDTO> dtos = horarioRepository.findAll()
                .stream()
                .map(HorarioMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Buscar horário por ID")
    public ResponseEntity<HorarioDTO> findById(@PathVariable Long id) {
        return horarioRepository.findById(id)
                .map(HorarioMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Horário com ID " + id + " não encontrado"));
    }

}
