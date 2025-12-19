package com.jangada.RADAR.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jangada.RADAR.exceptions.ResourceNotFoundException;
import com.jangada.RADAR.mappers.HistoricoEstudanteMapper;
import com.jangada.RADAR.models.dtos.HistoricoEstudanteDTO;
import com.jangada.RADAR.models.entities.ComponenteCurricular;
import com.jangada.RADAR.models.entities.HistoricoEstudante;
import com.jangada.RADAR.models.entities.Usuario;
import com.jangada.RADAR.repositories.ComponenteCurricularRepository;
import com.jangada.RADAR.repositories.HistoricoEstudanteRepository;
import com.jangada.RADAR.repositories.UsuarioRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/historico")
@Tag(name = "Histórico Estudante", description = "API para gerenciamento de histórico acadêmico")
public class HistoricoEstudanteController {
    
    private final HistoricoEstudanteRepository historicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ComponenteCurricularRepository componenteRepository;
    
    public HistoricoEstudanteController(
            HistoricoEstudanteRepository historicoRepository,
            UsuarioRepository usuarioRepository,
            ComponenteCurricularRepository componenteRepository) {
        this.historicoRepository = historicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.componenteRepository = componenteRepository;
    }
    
    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Buscar histórico de um usuário")
    public ResponseEntity<List<HistoricoEstudanteDTO>> findByUsuario(@PathVariable Long usuarioId) {
        List<HistoricoEstudanteDTO> dtos = historicoRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(HistoricoEstudanteMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/usuario/{usuarioId}/status/{status}")
    @Operation(summary = "Buscar histórico de um usuário por status")
    public ResponseEntity<List<HistoricoEstudanteDTO>> findByUsuarioAndStatus(
            @PathVariable Long usuarioId,
            @PathVariable String status) {
        List<HistoricoEstudanteDTO> dtos = historicoRepository.findByUsuarioIdAndStatus(usuarioId, status)
                .stream()
                .map(HistoricoEstudanteMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    @PostMapping
    @Operation(summary = "Adicionar registro ao histórico")
    public ResponseEntity<HistoricoEstudanteDTO> create(@Valid @RequestBody HistoricoEstudanteDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + dto.getUsuarioId() + " não encontrado"));
        
        ComponenteCurricular componente = componenteRepository.findById(dto.getComponenteId())
                .orElseThrow(() -> new ResourceNotFoundException("Componente com ID " + dto.getComponenteId() + " não encontrado"));
        
        // Verificar se já existe registro para esse semestre
        if (historicoRepository.existsByUsuarioIdAndComponenteIdAndSemestre(
                dto.getUsuarioId(), dto.getComponenteId(), dto.getSemestre())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        HistoricoEstudante historico = HistoricoEstudante.builder()
                .usuario(usuario)
                .componente(componente)
                .semestre(dto.getSemestre())
                .nota(dto.getNota())
                .status(dto.getStatus())
                .dataConclusao(dto.getDataConclusao())
                .build();
        
        HistoricoEstudante saved = historicoRepository.save(historico);
        return ResponseEntity.status(HttpStatus.CREATED).body(HistoricoEstudanteMapper.toDto(saved));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Remover registro do histórico")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!historicoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Histórico com ID " + id + " não encontrado");
        }
        historicoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
