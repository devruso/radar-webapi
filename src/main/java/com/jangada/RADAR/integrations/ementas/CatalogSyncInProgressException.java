package com.jangada.RADAR.integrations.ementas;

public class CatalogSyncInProgressException extends RuntimeException {

    public CatalogSyncInProgressException() {
        super("Já existe uma sincronização do catálogo em andamento.");
    }
}
