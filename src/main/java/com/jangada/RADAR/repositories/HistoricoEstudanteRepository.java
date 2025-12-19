package com.jangada.RADAR.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jangada.RADAR.models.entities.HistoricoEstudante;

@Repository
public interface HistoricoEstudanteRepository extends JpaRepository<HistoricoEstudante, Long> {
    
    List<HistoricoEstudante> findByUsuarioId(Long usuarioId);
    
    List<HistoricoEstudante> findByUsuarioIdAndStatus(Long usuarioId, String status);
    
    boolean existsByUsuarioIdAndComponenteIdAndSemestre(Long usuarioId, Long componenteId, String semestre);

}
