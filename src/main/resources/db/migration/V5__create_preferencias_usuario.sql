-- V5__create_preferencias_usuario.sql
-- Cria tabela de preferências do usuário (turnos e professores banidos)

CREATE TABLE IF NOT EXISTS preferencias_usuario (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE,
    turnos_disponiveis TEXT,
    professores_banidos TEXT,
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_preferencias_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- Índice para melhorar performance
CREATE INDEX IF NOT EXISTS idx_preferencias_usuario ON preferencias_usuario(usuario_id);

-- Comentários
COMMENT ON TABLE preferencias_usuario IS 'Preferências do usuário para recomendações';
COMMENT ON COLUMN preferencias_usuario.turnos_disponiveis IS 'CSV dos turnos disponíveis: MATUTINO,VESPERTINO,NOTURNO';
COMMENT ON COLUMN preferencias_usuario.professores_banidos IS 'CSV dos nomes de professores banidos';
