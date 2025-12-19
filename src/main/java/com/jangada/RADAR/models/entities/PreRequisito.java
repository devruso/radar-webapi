package com.jangada.RADAR.models.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "prerequisitos",
    uniqueConstraints = @UniqueConstraint(columnNames = {"componente_id", "componente_prerequisito_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreRequisito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "componente_id", nullable = false)
    private ComponenteCurricular componente;

    @ManyToOne
    @JoinColumn(name = "componente_prerequisito_id", nullable = false)
    private ComponenteCurricular componentePreRequisito;

    // Tipo de relação: PREREQUISITO, COREQUISITO, POSREQUISITO
    private String tipo;
}
