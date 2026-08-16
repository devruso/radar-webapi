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

    @Query("SELECT DISTINCT c FROM ComponenteCurricular c LEFT JOIN FETCH c.turmas")
    List<ComponenteCurricular> findAllWithTurmas();

    @Query("SELECT DISTINCT c FROM ComponenteCurricular c LEFT JOIN FETCH c.turmas " +
           "WHERE c.id IN (SELECT context.componenteCurricular.id " +
           "FROM ComponenteCurricularContexto context " +
           "WHERE LOWER(context.courseName) LIKE LOWER(CONCAT('%', :courseName, '%')) " +
           "AND (:onlyActive = false OR context.isActive = true))")
    List<ComponenteCurricular> findAllByCourseContext(
        @Param("courseName") String courseName,
        @Param("onlyActive") boolean onlyActive
    );

    @Query("SELECT DISTINCT c FROM ComponenteCurricular c LEFT JOIN FETCH c.turmas WHERE c.id = :id")
    Optional<ComponenteCurricular> findByIdWithTurmas(@Param("id") Long id);

    @Query("SELECT DISTINCT c FROM ComponenteCurricular c LEFT JOIN FETCH c.turmas WHERE c.codigo = :codigo")
    Optional<ComponenteCurricular> findByCodigoWithTurmas(@Param("codigo") String codigo);

    List<ComponenteCurricular> findAllByCodigoIn(List<String> codigos);

    long countByEmentasSyncedAtIsNotNull();

    Optional<ComponenteCurricular> findFirstByEmentasSyncedAtIsNotNullOrderByEmentasSyncedAtDesc();
    
    @Query("SELECT c FROM ComponenteCurricular c WHERE c.nivel = :nivel")
    List<ComponenteCurricular> findByNivel(@Param("nivel") Short nivel);
    
    @Query("SELECT c FROM ComponenteCurricular c WHERE c.tipo = :tipo")
    List<ComponenteCurricular> findByTipo(@Param("tipo") String tipo);
}

