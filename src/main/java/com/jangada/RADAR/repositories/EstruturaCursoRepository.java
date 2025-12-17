package com.jangada.RADAR.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jangada.RADAR.models.entities.EstruturaCurso;

@Repository
public interface EstruturaCursoRepository extends JpaRepository<EstruturaCurso, Long> {
}
