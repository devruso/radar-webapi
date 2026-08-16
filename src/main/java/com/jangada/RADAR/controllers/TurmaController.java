package com.jangada.RADAR.controllers;

import com.jangada.RADAR.mappers.TurmaMapper;
import com.jangada.RADAR.models.dtos.TurmaDTO;
import com.jangada.RADAR.models.entities.Turma;
import com.jangada.RADAR.repositories.TurmaRepository;
import com.jangada.RADAR.repositories.CursoRepository;
import com.jangada.RADAR.exceptions.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/turmas")
public class TurmaController {

    private final TurmaRepository turmaRepository;
    private final CursoRepository cursoRepository;

    public TurmaController(
            TurmaRepository turmaRepository,
            CursoRepository cursoRepository) {
        this.turmaRepository = turmaRepository;
        this.cursoRepository = cursoRepository;
    }

    @GetMapping
    public ResponseEntity<List<TurmaDTO>> listAll(
            @RequestParam(defaultValue = "false") boolean incluirInativas) {
        List<Turma> classes = incluirInativas
                ? turmaRepository.findAllWithDetails()
                : turmaRepository.findAllActiveWithDetails();
        List<TurmaDTO> dtos = classes.stream()
                .map(TurmaMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/componente/{componenteId}")
    public List<TurmaDTO> listByComponent(@PathVariable Long componenteId) {
        return turmaRepository.findByComponenteId(componenteId).stream()
                .map(TurmaMapper::toDto)
                .toList();
    }

    @GetMapping("/curso/{cursoId}")
    public List<TurmaDTO> listByCourse(@PathVariable Long cursoId) {
        String courseName = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ResourceNotFoundException("Curso não encontrado"))
                .getNome();
        return turmaRepository.findAllActiveByCourseName(courseName).stream()
                .map(TurmaMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TurmaDTO> getOne(@PathVariable Long id) {
        return turmaRepository.findByIdWithDetails(id)
                .map(t -> ResponseEntity.ok(TurmaMapper.toDto(t)))
                .orElseThrow(() -> new ResourceNotFoundException("Turma com ID " + id + " não encontrada"));
    }

}
