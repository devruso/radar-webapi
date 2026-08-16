package com.jangada.RADAR.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jangada.RADAR.models.entities.ComponenteCurricularContexto;

@Repository
public interface ComponenteCurricularContextoRepository
        extends JpaRepository<ComponenteCurricularContexto, Long> {

    List<ComponenteCurricularContexto> findAllByComponenteCurricularId(Long componenteId);

    List<ComponenteCurricularContexto> findAllByComponenteCurricularIdIn(List<Long> componenteIds);

    @Query("SELECT context FROM ComponenteCurricularContexto context " +
           "JOIN FETCH context.componenteCurricular component " +
           "WHERE context.isActive = true " +
           "AND LOWER(context.courseName) = LOWER(:courseName)")
    List<ComponenteCurricularContexto> findAllActiveByCourseName(
        @Param("courseName") String courseName
    );
}
