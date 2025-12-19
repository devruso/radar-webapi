package com.jangada.RADAR.repositories;

import com.jangada.RADAR.models.entities.PreRequisito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreRequisitoRepository extends JpaRepository<PreRequisito, Long> {

    List<PreRequisito> findByComponenteId(Long componenteId);

    List<PreRequisito> findByComponentePreRequisitoId(Long componentePreRequisitoId);

    List<PreRequisito> findByComponenteIdAndTipo(Long componenteId, String tipo);
}
