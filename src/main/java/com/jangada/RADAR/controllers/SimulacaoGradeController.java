package com.jangada.RADAR.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.jangada.RADAR.models.dtos.SalvarSimulacaoGradeDTO;
import com.jangada.RADAR.models.dtos.SimulacaoGradeDTO;
import com.jangada.RADAR.services.SimulacaoGradeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/simulacoes")
public class SimulacaoGradeController {

    private final SimulacaoGradeService service;

    public SimulacaoGradeController(SimulacaoGradeService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@userAccess.canAccess(#request.usuarioId(), authentication)")
    public SimulacaoGradeDTO save(@Valid @RequestBody SalvarSimulacaoGradeDTO request) {
        return service.save(request);
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("@userAccess.canAccess(#usuarioId, authentication)")
    public List<SimulacaoGradeDTO> list(@PathVariable Long usuarioId) {
        return service.listByUser(usuarioId);
    }

    @GetMapping("/{id}")
    @PostAuthorize("@userAccess.canAccess(returnObject.usuarioId(), authentication)")
    public SimulacaoGradeDTO find(@PathVariable Long id) {
        return service.find(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@userAccess.canAccess(#usuarioId, authentication)")
    public void delete(@PathVariable Long id, @RequestParam Long usuarioId) {
        service.delete(id, usuarioId);
    }
}
