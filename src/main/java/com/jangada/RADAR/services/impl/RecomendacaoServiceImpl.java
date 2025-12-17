package com.jangada.RADAR.services.impl;

import com.jangada.RADAR.models.dtos.TurmaDTO;
import com.jangada.RADAR.repositories.UsuarioRepository;
import com.jangada.RADAR.services.RecomendacaoService;
import com.jangada.RADAR.mappers.TurmaMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecomendacaoServiceImpl implements RecomendacaoService {

    private final UsuarioRepository usuarioRepository;

    public RecomendacaoServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<TurmaDTO> recomendar(Long usuarioId, String metodo) {
        // Implementação inicial: placeholder que retorna lista vazia
        // TODO: implementar filtrar -> ordenar -> encaixar
        return new ArrayList<>();
    }
}
