package com.jangada.RADAR.services.impl;

import com.jangada.RADAR.models.dtos.TurmaDTO;
import com.jangada.RADAR.models.entities.Usuario;
import com.jangada.RADAR.repositories.UsuarioRepository;
import com.jangada.RADAR.repositories.TurmaRepository;
import com.jangada.RADAR.services.RecomendacaoService;
import com.jangada.RADAR.mappers.TurmaMapper;
import com.jangada.RADAR.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RecomendacaoServiceImpl implements RecomendacaoService {

    private final UsuarioRepository usuarioRepository;
    private final TurmaRepository turmaRepository;

    public RecomendacaoServiceImpl(UsuarioRepository usuarioRepository, TurmaRepository turmaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.turmaRepository = turmaRepository;
    }

    @Override
    public List<TurmaDTO> recomendar(Long usuarioId, String metodo) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // TODO: implementar filtrar -> ordenar -> encaixar
        // Por enquanto, retorna todas as turmas disponíveis
        List<TurmaDTO> allTurmas = turmaRepository.findAll()
                .stream()
                .map(TurmaMapper::toDto)
                .collect(Collectors.toList());

        return allTurmas;
    }
}
