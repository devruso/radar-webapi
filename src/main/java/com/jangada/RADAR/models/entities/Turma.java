package com.jangada.RADAR.models.entities;

import java.time.Instant;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "turmas")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"componenteCurricular", "horario", "vagas", "guiaMatricula"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Turma {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String local;
    private String professor;
    private String numero;
    private Byte tipo;
    @Column(length = 20)
    private String periodoLetivo;

    @Column(length = 100)
    private String source;

    @Column(length = 160)
    private String externalKey;
    private boolean ativa;
    private Instant updatedAt;

    @Version
    private Long version;

    @ManyToOne
    @JoinColumn(name = "componente_id")
    private ComponenteCurricular componenteCurricular;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "horario_id")
    private Horario horario;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "vagas_id")
    private Vagas vagas;

    @ManyToOne
    @JoinColumn(name = "guia_id")
    private GuiaMatricula guiaMatricula;

}
