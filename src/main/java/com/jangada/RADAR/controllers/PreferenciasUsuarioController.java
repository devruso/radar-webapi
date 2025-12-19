package com.jangada.RADAR.controllers;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jangada.RADAR.exceptions.ResourceNotFoundException;
import com.jangada.RADAR.mappers.PreferenciasUsuarioMapper;
import com.jangada.RADAR.models.dtos.PreferenciasUsuarioDTO;
import com.jangada.RADAR.models.entities.PreferenciasUsuario;
import com.jangada.RADAR.models.entities.Usuario;
import com.jangada.RADAR.repositories.PreferenciasUsuarioRepository;
import com.jangada.RADAR.repositories.UsuarioRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/preferencias")
@Tag(name = "Preferências Usuário", description = "API para gerenciamento de preferências de usuários")
public class PreferenciasUsuarioController {
    
    private final PreferenciasUsuarioRepository preferenciasRepository;
    private final UsuarioRepository usuarioRepository;
    
    public PreferenciasUsuarioController(
            PreferenciasUsuarioRepository preferenciasRepository,
            UsuarioRepository usuarioRepository) {
        this.preferenciasRepository = preferenciasRepository;
        this.usuarioRepository = usuarioRepository;
    }
    
    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Buscar preferências de um usuário")
    public ResponseEntity<PreferenciasUsuarioDTO> findByUsuario(@PathVariable Long usuarioId) {
        return preferenciasRepository.findByUsuarioId(usuarioId)
                .map(PreferenciasUsuarioMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @Operation(summary = "Criar preferências de usuário")
    public ResponseEntity<PreferenciasUsuarioDTO> create(@RequestBody PreferenciasUsuarioDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + dto.getUsuarioId() + " não encontrado"));
        
        // Verificar se já existe preferências para esse usuário
        if (preferenciasRepository.existsByUsuarioId(dto.getUsuarioId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        PreferenciasUsuario preferencias = PreferenciasUsuarioMapper.toEntity(dto);
        preferencias.setUsuario(usuario);
        preferencias.setDataAtualizacao(LocalDateTime.now());
        
        PreferenciasUsuario saved = preferenciasRepository.save(preferencias);
        return ResponseEntity.status(HttpStatus.CREATED).body(PreferenciasUsuarioMapper.toDto(saved));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar preferências de usuário")
    public ResponseEntity<PreferenciasUsuarioDTO> update(
            @PathVariable Long id,
            @RequestBody PreferenciasUsuarioDTO dto) {
        
        PreferenciasUsuario existing = preferenciasRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Preferências com ID " + id + " não encontradas"));
        
        PreferenciasUsuario updated = PreferenciasUsuarioMapper.toEntity(dto);
        updated.setId(existing.getId());
        updated.setUsuario(existing.getUsuario());
        updated.setDataAtualizacao(LocalDateTime.now());
        
        PreferenciasUsuario saved = preferenciasRepository.save(updated);
        return ResponseEntity.ok(PreferenciasUsuarioMapper.toDto(saved));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Remover preferências de usuário")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!preferenciasRepository.existsById(id)) {
            throw new ResourceNotFoundException("Preferências com ID " + id + " não encontradas");
        }
        preferenciasRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
