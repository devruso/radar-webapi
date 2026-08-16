package com.jangada.RADAR.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jangada.RADAR.models.entities.Horario;

@Repository
public interface HorarioRepository extends JpaRepository<Horario, Long> {

    @Query("SELECT DISTINCT h FROM Horario h LEFT JOIN FETCH h.horarios")
    List<Horario> findAllWithSlots();

    @Query("SELECT DISTINCT h FROM Horario h LEFT JOIN FETCH h.horarios WHERE h.id = :id")
    Optional<Horario> findByIdWithSlots(@Param("id") Long id);
}
