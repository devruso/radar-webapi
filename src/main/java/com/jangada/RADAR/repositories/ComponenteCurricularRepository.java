package com.jangada.RADAR.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jangada.RADAR.models.entities.ComponenteCurricular;

@Repository
public interface ComponenteCurricularRepository extends JpaRepository<ComponenteCurricular, Long> {
    Optional<ComponenteCurricular> findByCodigo(String codigo);
    
    @Query("SELECT c FROM ComponenteCurricular c WHERE c.nivel = :nivel")
    List<ComponenteCurricular> findByNivel(@Param("nivel") Short nivel);
    
    @Query("SELECT c FROM ComponenteCurricular c WHERE c.tipo = :tipo")
    List<ComponenteCurricular> findByTipo(@Param("tipo") String tipo);
}

