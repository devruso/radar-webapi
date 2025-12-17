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
@Table(name = "vagas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vagas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Short totalVagas;

    @ElementCollection
    @CollectionTable(name = "vagas_reserva", joinColumns = @JoinColumn(name = "vagas_id"))
    @MapKeyColumn(name = "tipo")
    @Column(name = "quantidade")
    @Builder.Default
    private Map<String, Integer> reservaVagas = new HashMap<>();

}
