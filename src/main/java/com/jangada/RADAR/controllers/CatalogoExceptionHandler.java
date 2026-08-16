package com.jangada.RADAR.controllers;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.jangada.RADAR.integrations.ementas.CatalogSyncInProgressException;
import com.jangada.RADAR.integrations.ementas.EmentasCatalogException;

@RestControllerAdvice
public class CatalogoExceptionHandler {

    @ExceptionHandler(EmentasCatalogException.class)
    public ResponseEntity<Map<String, Object>> unavailable(EmentasCatalogException exception) {
        return response(HttpStatus.BAD_GATEWAY, exception.getMessage());
    }

    @ExceptionHandler(CatalogSyncInProgressException.class)
    public ResponseEntity<Map<String, Object>> conflict(CatalogSyncInProgressException exception) {
        return response(HttpStatus.CONFLICT, exception.getMessage());
    }

    private static ResponseEntity<Map<String, Object>> response(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
            "timestamp", Instant.now().toString(),
            "status", status.value(),
            "error", status.getReasonPhrase(),
            "message", message
        ));
    }
}
