package com.jangada.RADAR.models.entities;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(
    name = "componente_contextos_curriculares",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_componente_contexto_source",
        columnNames = {"componente_id", "source_url", "source_key"}
    )
)
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "componenteCurricular")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComponenteCurricularContexto {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "componente_id", nullable = false)
    private ComponenteCurricular componenteCurricular;

    private String ementasExternalId;

    @Column(length = 500, nullable = false)
    private String sourceUrl;

    @Column(length = 500, nullable = false)
    private String sourceKey;

    private String curriculumCode;

    @Column(columnDefinition = "TEXT")
    private String curriculumName;

    @Column(columnDefinition = "TEXT")
    private String courseName;

    private String implementationSemester;
    private Integer recommendedPeriod;
    private boolean isRequired;
    private boolean isActive;

    @Column(columnDefinition = "TEXT")
    private String prerequeriments;

    private String academicLevel;
    private Instant syncedAt;
}
