package com.jangada.RADAR.models.enums;

public enum PrioridadeMatricula {
    I(1, "Obrigatória do semestre acadêmico atual"),
    II(2, "Estudante com status FORMANDO"),
    III(3, "Obrigatória atrasada ou componente optativo"),
    IV(4, "Obrigatória de semestre futuro"),
    V(5, "Componente livre ou equivalente");

    private final int ordem;
    private final String descricao;

    PrioridadeMatricula(int ordem, String descricao) {
        this.ordem = ordem;
        this.descricao = descricao;
    }

    public int getOrdem() {
        return ordem;
    }

    public String getDescricao() {
        return descricao;
    }
}
