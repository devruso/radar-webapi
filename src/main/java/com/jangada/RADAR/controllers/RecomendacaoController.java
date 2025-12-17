package com.jangada.RADAR.controllers;

import com.jangada.RADAR.models.dtos.TurmaDTO;
import com.jangada.RADAR.services.RecomendacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
public class RecomendacaoController {

    private final RecomendacaoService recomendacaoService;

    public RecomendacaoController(RecomendacaoService recomendacaoService) {
        this.recomendacaoService = recomendacaoService;
    }

    @PostMapping("/{usuarioId}")
    public ResponseEntity<List<TurmaDTO>> recommend(
            @PathVariable Long usuarioId,
            @RequestParam(defaultValue = "burrinho") String metodo
    ) {
        List<TurmaDTO> result = recomendacaoService.recomendar(usuarioId, metodo);
        return ResponseEntity.ok(result);
    }

}
