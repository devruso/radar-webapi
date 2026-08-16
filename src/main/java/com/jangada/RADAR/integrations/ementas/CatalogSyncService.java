package com.jangada.RADAR.integrations.ementas;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jangada.RADAR.integrations.ementas.CatalogPersistenceService.PersistenceResult;
import com.jangada.RADAR.integrations.ementas.EmentasApiModels.CatalogSnapshot;
import com.jangada.RADAR.integrations.ementas.EmentasApiModels.SourceStatus;
import com.jangada.RADAR.models.entities.ComponenteCurricular;
import com.jangada.RADAR.repositories.ComponenteCurricularRepository;

@Service
public class CatalogSyncService {

    private final EmentasCatalogClient client;
    private final CatalogPersistenceService persistenceService;
    private final ComponenteCurricularRepository componenteRepository;
    private final AtomicBoolean synchronizing = new AtomicBoolean(false);
    private final String syncKey;

    public CatalogSyncService(
        EmentasCatalogClient client,
        CatalogPersistenceService persistenceService,
        ComponenteCurricularRepository componenteRepository,
        @Value("${radar.catalog.sync-key:}") String syncKey
    ) {
        this.client = client;
        this.persistenceService = persistenceService;
        this.componenteRepository = componenteRepository;
        this.syncKey = syncKey == null ? "" : syncKey;
    }

    public CatalogSyncReport synchronize() {
        if (!synchronizing.compareAndSet(false, true)) {
            throw new CatalogSyncInProgressException();
        }
        try {
            Instant startedAt = Instant.now();
            CatalogSnapshot snapshot = client.fetchGraduationCatalog();
            Instant syncedAt = Instant.now();
            PersistenceResult persisted = persistenceService.persist(snapshot, syncedAt);
            return new CatalogSyncReport(
                startedAt,
                syncedAt,
                persisted,
                snapshot.sources()
            );
        } finally {
            synchronizing.set(false);
        }
    }

    public CatalogStatus status() {
        Instant lastSync = componenteRepository
            .findFirstByEmentasSyncedAtIsNotNullOrderByEmentasSyncedAtDesc()
            .map(ComponenteCurricular::getEmentasSyncedAt)
            .orElse(null);
        return new CatalogStatus(
            synchronizing.get(),
            componenteRepository.count(),
            componenteRepository.countByEmentasSyncedAtIsNotNull(),
            lastSync
        );
    }

    public boolean isAuthorized(String suppliedKey) {
        if (syncKey.isBlank()) {
            return false;
        }
        if (suppliedKey == null) {
            return false;
        }
        return MessageDigest.isEqual(
            syncKey.getBytes(StandardCharsets.UTF_8),
            suppliedKey.getBytes(StandardCharsets.UTF_8)
        );
    }

    public record CatalogSyncReport(
        Instant startedAt,
        Instant completedAt,
        PersistenceResult result,
        List<SourceStatus> sources
    ) {
    }

    public record CatalogStatus(
        boolean synchronizing,
        long totalComponents,
        long ementasComponents,
        Instant lastSuccessfulSync
    ) {
    }
}
