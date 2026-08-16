package com.jangada.RADAR.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.jangada.RADAR.models.dtos.ImportacaoOfertasResultadoDTO;
import com.jangada.RADAR.models.dtos.ImportarOfertasDTO;
import com.jangada.RADAR.services.OfertaTurmaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ofertas")
public class OfertaTurmaController {

    private final OfertaTurmaService service;

    public OfertaTurmaController(OfertaTurmaService service) {
        this.service = service;
    }

    @PostMapping("/importar")
    public ImportacaoOfertasResultadoDTO importPeriod(
        @RequestHeader(name = "X-Radar-Import-Key", required = false) String importKey,
        @Valid @RequestBody ImportarOfertasDTO request
    ) {
        if (!service.isAuthorized(importKey)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chave de importação inválida.");
        }
        return service.importPeriod(request);
    }
}
