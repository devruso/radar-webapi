package com.jangada.RADAR.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.jangada.RADAR.integrations.ementas.CatalogSyncService;
import com.jangada.RADAR.integrations.ementas.CatalogSyncService.CatalogStatus;
import com.jangada.RADAR.integrations.ementas.CatalogSyncService.CatalogSyncReport;

@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {

    private final CatalogSyncService catalogSyncService;

    public CatalogoController(CatalogSyncService catalogSyncService) {
        this.catalogSyncService = catalogSyncService;
    }

    @GetMapping("/status")
    public CatalogStatus status() {
        return catalogSyncService.status();
    }

    @PostMapping("/sincronizar")
    @ResponseStatus(HttpStatus.OK)
    public CatalogSyncReport synchronize(
        @RequestHeader(name = "X-Radar-Sync-Key", required = false) String syncKey
    ) {
        if (!catalogSyncService.isAuthorized(syncKey)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chave de sincronização inválida.");
        }
        return catalogSyncService.synchronize();
    }
}
