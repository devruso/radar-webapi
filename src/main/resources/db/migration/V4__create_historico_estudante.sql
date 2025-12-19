-- V4__create_historico_estudante.sql
-- Cria tabela de histórico acadêmico do estudante

CREATE TABLE IF NOT EXISTS historico_estudante (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    componente_id BIGINT NOT NULL,
    semestre VARCHAR(10) NOT NULL,
    nota DOUBLE PRECISION,
    status VARCHAR(20) NOT NULL,
    data_conclusao DATE,
    CONSTRAINT fk_historico_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_historico_componente FOREIGN KEY (componente_id) REFERENCES componentes_curriculares(id) ON DELETE CASCADE,
    CONSTRAINT uk_historico_usuario_componente_semestre UNIQUE (usuario_id, componente_id, semestre)
);

-- Índices para melhorar performance
CREATE INDEX IF NOT EXISTS idx_historico_usuario ON historico_estudante(usuario_id);
CREATE INDEX IF NOT EXISTS idx_historico_componente ON historico_estudante(componente_id);
CREATE INDEX IF NOT EXISTS idx_historico_status ON historico_estudante(status);

-- Comentários
COMMENT ON TABLE historico_estudante IS 'Histórico acadêmico do estudante com notas e status';
COMMENT ON COLUMN historico_estudante.status IS 'Status: APROVADO, REPROVADO, TRANCADO';
COMMENT ON COLUMN historico_estudante.semestre IS 'Formato: YYYY.S (ex: 2024.1, 2024.2)';
