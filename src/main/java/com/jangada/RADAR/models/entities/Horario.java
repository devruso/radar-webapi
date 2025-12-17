package com.jangada.RADAR.models.entities;

import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "horarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Horario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;

    private String turno;

    // Map dia -> string com horários (por exemplo: "08:00-10:00,14:00-16:00")
    @ElementCollection
    @CollectionTable(name = "horario_map", joinColumns = @JoinColumn(name = "horario_id"))
    @MapKeyColumn(name = "dia")
    @Column(name = "horarios")
    @Builder.Default
    private Map<String, String> horarios = new HashMap<>();

    public static final String[] dias = new String[]{"SEG", "TER", "QUA", "QUI", "SEX", "SAB"};

}
