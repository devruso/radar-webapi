-- V3: Adicionar tabelas para avaliação de professores e pré-requisitos estruturados

-- Tabela de avaliações de professores
CREATE TABLE IF NOT EXISTS avaliacoes_professor (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    professor_nome VARCHAR(255) NOT NULL,
    componente_id BIGINT NOT NULL,
    nota INTEGER NOT NULL CHECK (nota >= 1 AND nota <= 5),
    comentario TEXT,
    data_avaliacao TIMESTAMP NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (componente_id) REFERENCES componentes_curriculares(id) ON DELETE CASCADE,
    UNIQUE(usuario_id, professor_nome, componente_id)
);

-- Tabela de pré-requisitos (estrutura normalizada)
CREATE TABLE IF NOT EXISTS prerequisitos (
    id BIGSERIAL PRIMARY KEY,
    componente_id BIGINT NOT NULL,
    componente_prerequisito_id BIGINT NOT NULL,
    tipo VARCHAR(50) NOT NULL DEFAULT 'PREREQUISITO',
    FOREIGN KEY (componente_id) REFERENCES componentes_curriculares(id) ON DELETE CASCADE,
    FOREIGN KEY (componente_prerequisito_id) REFERENCES componentes_curriculares(id) ON DELETE CASCADE,
    UNIQUE(componente_id, componente_prerequisito_id)
);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_avaliacoes_usuario ON avaliacoes_professor(usuario_id);
CREATE INDEX IF NOT EXISTS idx_avaliacoes_professor ON avaliacoes_professor(professor_nome);
CREATE INDEX IF NOT EXISTS idx_avaliacoes_componente ON avaliacoes_professor(componente_id);
CREATE INDEX IF NOT EXISTS idx_prerequisitos_componente ON prerequisitos(componente_id);
CREATE INDEX IF NOT EXISTS idx_prerequisitos_pre_componente ON prerequisitos(componente_prerequisito_id);
