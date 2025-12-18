package com.jangada.RADAR.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jangada.RADAR.exceptions.ResourceNotFoundException;
import com.jangada.RADAR.mappers.UsuarioMapper;
import com.jangada.RADAR.models.dtos.UsuarioDTO;
import com.jangada.RADAR.models.entities.Usuario;
import com.jangada.RADAR.models.entities.Turma;
import com.jangada.RADAR.models.entities.Curso;
import com.jangada.RADAR.repositories.TurmaRepository;
import com.jangada.RADAR.repositories.CursoRepository;
import com.jangada.RADAR.repositories.UsuarioRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final CursoRepository cursoRepository;
    private final TurmaRepository turmaRepository;

    public UsuarioController(UsuarioRepository usuarioRepository,
                             CursoRepository cursoRepository,
                             TurmaRepository turmaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.cursoRepository = cursoRepository;
        this.turmaRepository = turmaRepository;
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

        // Vincula curso, se informado
        if (dto.getCursoId() != null) {
            Curso curso = cursoRepository.findById(dto.getCursoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Curso com ID " + dto.getCursoId() + " não encontrado"));
            u.setCurso(curso);
        }

        // Vincula turmas selecionadas, se informadas
        if (dto.getTurmasSelecionadasIds() != null && !dto.getTurmasSelecionadasIds().isEmpty()) {
            for (Long turmaId : dto.getTurmasSelecionadasIds()) {
                Turma t = turmaRepository.findById(turmaId)
                        .orElseThrow(() -> new ResourceNotFoundException("Turma com ID " + turmaId + " não encontrada"));
                u.getTurmasSelecionadas().add(t);
            }
        }

        Usuario saved = usuarioRepository.save(u);
        return ResponseEntity.ok(UsuarioMapper.toDto(saved));
    }

}
