package com.jangada.RADAR.services;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jangada.RADAR.exceptions.ResourceNotFoundException;
import com.jangada.RADAR.mappers.TurmaMapper;
import com.jangada.RADAR.models.dtos.SalvarSimulacaoGradeDTO;
import com.jangada.RADAR.models.dtos.SimulacaoGradeDTO;
import com.jangada.RADAR.models.entities.SimulacaoGrade;
import com.jangada.RADAR.models.entities.Turma;
import com.jangada.RADAR.models.entities.Usuario;
import com.jangada.RADAR.repositories.SimulacaoGradeRepository;
import com.jangada.RADAR.repositories.TurmaRepository;
import com.jangada.RADAR.repositories.UsuarioRepository;
import com.jangada.RADAR.utils.GradeOptimizer;
import com.jangada.RADAR.utils.RecomendacaoUtil.RecomendacaoCriteria;

@Service
public class SimulacaoGradeService {

    private final SimulacaoGradeRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final TurmaRepository turmaRepository;

    public SimulacaoGradeService(
        SimulacaoGradeRepository repository,
        UsuarioRepository usuarioRepository,
        TurmaRepository turmaRepository
    ) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.turmaRepository = turmaRepository;
    }

    @Transactional
    public SimulacaoGradeDTO save(SalvarSimulacaoGradeDTO request) {
        Usuario user = usuarioRepository.findById(request.usuarioId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        List<Turma> fetchedClasses = turmaRepository.findAllByIdWithDetails(request.turmaIds());
        if (fetchedClasses.size() != request.turmaIds().stream().distinct().count()) {
            throw new ResourceNotFoundException("Uma ou mais turmas não foram encontradas");
        }
        Map<Long, Turma> classesById = fetchedClasses.stream()
            .collect(Collectors.toMap(Turma::getId, Function.identity()));
        List<Turma> classes = request.turmaIds().stream().map(classesById::get).toList();
        List<RecomendacaoCriteria> candidates = classes.stream()
            .map(item -> new RecomendacaoCriteria(item, "INTERMEDIO", 3.0, "validação"))
            .toList();
        List<RecomendacaoCriteria> verified = GradeOptimizer.optimize(
            candidates, classes.size(), request.metodo()
        );
        if (verified.size() != classes.size()) {
            throw new IllegalArgumentException(
                "A simulação contém horários ausentes, conflitos ou mais de uma turma da mesma disciplina."
            );
        }
        SimulacaoGrade saved = repository.save(SimulacaoGrade.builder()
            .usuario(user)
            .nome(request.nome().trim())
            .metodo(normalizeMethod(request.metodo()))
            .criadaEm(Instant.now())
            .turmas(classes)
            .build());
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<SimulacaoGradeDTO> listByUser(Long userId) {
        if (!usuarioRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuário não encontrado");
        }
        return repository.findAllByUsuarioIdWithDetails(userId).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public SimulacaoGradeDTO find(Long id) {
        return repository.findByIdWithDetails(id).map(this::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("Simulação não encontrada"));
    }

    @Transactional
    public void delete(Long id, Long userId) {
        SimulacaoGrade simulation = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Simulação não encontrada"));
        if (!simulation.getUsuario().getId().equals(userId)) {
            throw new ResourceNotFoundException("Simulação não encontrada");
        }
        repository.delete(simulation);
    }

    private SimulacaoGradeDTO toDto(SimulacaoGrade simulation) {
        return new SimulacaoGradeDTO(
            simulation.getId(),
            simulation.getUsuario().getId(),
            simulation.getNome(),
            simulation.getMetodo(),
            simulation.getCriadaEm(),
            simulation.getTurmas().stream().map(TurmaMapper::toDto).toList()
        );
    }

    private static String normalizeMethod(String method) {
        return "burrinho".equalsIgnoreCase(method) ? "guloso" : method.trim().toLowerCase();
    }
}
