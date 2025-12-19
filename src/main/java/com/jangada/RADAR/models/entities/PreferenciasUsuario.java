package com.jangada.RADAR.models.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "preferencias_usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferenciasUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "turnos_disponiveis", length = 100)
    private String turnosDisponiveis; // CSV: "MATUTINO,VESPERTINO,NOTURNO"

    @Column(name = "professores_banidos", columnDefinition = "TEXT")
    private String professoresBanidos; // CSV de nomes separados por vírgula

    @Column(name = "data_atualizacao")
    @Builder.Default
    private LocalDateTime dataAtualizacao = LocalDateTime.now();

}
