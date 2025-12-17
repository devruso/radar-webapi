package com.jangada.RADAR.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jangada.RADAR.models.entities.ComponenteCurricular;

@Repository
public interface ComponenteCurricularRepository extends JpaRepository<ComponenteCurricular, Long> {
    ComponenteCurricular findByCodigo(String codigo);
}
