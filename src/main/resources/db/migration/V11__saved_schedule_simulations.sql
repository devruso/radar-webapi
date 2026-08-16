CREATE TABLE simulacoes_grade (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    nome VARCHAR(120) NOT NULL,
    metodo VARCHAR(20) NOT NULL,
    criada_em TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_simulacoes_grade_usuario_criada
    ON simulacoes_grade (usuario_id, criada_em DESC);

CREATE TABLE simulacao_grade_turmas (
    simulacao_id BIGINT NOT NULL REFERENCES simulacoes_grade(id) ON DELETE CASCADE,
    turma_id BIGINT NOT NULL REFERENCES turmas(id),
    posicao INTEGER NOT NULL,
    PRIMARY KEY (simulacao_id, posicao),
    UNIQUE (simulacao_id, turma_id)
);
