package com.jangada.RADAR.controllers;

import com.jangada.RADAR.models.dtos.UsuarioDTO;
import com.jangada.RADAR.mappers.UsuarioMapper;
import com.jangada.RADAR.models.entities.Usuario;
import com.jangada.RADAR.repositories.UsuarioRepository;
import com.jangada.RADAR.exceptions.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listAll() {
        List<UsuarioDTO> dtos = usuarioRepository.findAll().stream().map(UsuarioMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> getOne(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(u -> ResponseEntity.ok(UsuarioMapper.toDto(u)))
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + id + " não encontrado"));
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> create(@Valid @RequestBody UsuarioDTO dto) {
        Usuario u = UsuarioMapper.toEntity(dto);
        Usuario saved = usuarioRepository.save(u);
        return ResponseEntity.ok(UsuarioMapper.toDto(saved));
    }

}
