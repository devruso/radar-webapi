-- Authoritative baseline for a brand-new RADAR PostgreSQL database.
-- Historical migrations V1..V8 contain seed/evolution data and assume these
-- tables already exist because the project originally relied on ddl-auto=update.

CREATE TABLE IF NOT EXISTS estrutura_curso (
    id BIGSERIAL PRIMARY KEY,
    curso VARCHAR(255),
    municipio VARCHAR(255),
    entrada VARCHAR(255),
    codigo VARCHAR(255),
    ch_optativa INTEGER,
    ch_obrigatoria INTEGER,
    ch_complementar INTEGER
);

CREATE TABLE IF NOT EXISTS cursos (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255),
    coordenador VARCHAR(255),
    nivel VARCHAR(255),
    turno VARCHAR(255),
    estrutura_id BIGINT UNIQUE REFERENCES estrutura_curso(id),
    guia_id BIGINT UNIQUE
);

CREATE TABLE IF NOT EXISTS guias_matricula (
    id BIGSERIAL PRIMARY KEY,
    ano_periodo VARCHAR(255),
    curso_id BIGINT REFERENCES cursos(id)
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_cursos_guia'
    ) THEN
        ALTER TABLE cursos
            ADD CONSTRAINT fk_cursos_guia
            FOREIGN KEY (guia_id) REFERENCES guias_matricula(id);
    END IF;
END
$$;

CREATE TABLE IF NOT EXISTS componentes_curriculares (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(255) UNIQUE,
    nome VARCHAR(255),
    nivel SMALLINT,
    ementa TEXT,
    tipo VARCHAR(255),
    prerequisito VARCHAR(255),
    corequisito VARCHAR(255),
    posrequisito VARCHAR(255),
    estrutura_id BIGINT REFERENCES estrutura_curso(id)
);

CREATE TABLE IF NOT EXISTS horarios (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(255),
    turno VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS horario_map (
    horario_id BIGINT NOT NULL REFERENCES horarios(id) ON DELETE CASCADE,
    dia VARCHAR(255) NOT NULL,
    horarios VARCHAR(255),
    PRIMARY KEY (horario_id, dia)
);

CREATE TABLE IF NOT EXISTS vagas (
    id BIGSERIAL PRIMARY KEY,
    total_vagas SMALLINT
);

CREATE TABLE IF NOT EXISTS vagas_reserva (
    vagas_id BIGINT NOT NULL REFERENCES vagas(id) ON DELETE CASCADE,
    tipo VARCHAR(255) NOT NULL,
    quantidade INTEGER,
    PRIMARY KEY (vagas_id, tipo)
);

CREATE TABLE IF NOT EXISTS turmas (
    id BIGSERIAL PRIMARY KEY,
    local VARCHAR(255),
    professor VARCHAR(255),
    numero VARCHAR(255),
    tipo SMALLINT,
    componente_id BIGINT REFERENCES componentes_curriculares(id),
    horario_id BIGINT UNIQUE REFERENCES horarios(id),
    vagas_id BIGINT UNIQUE REFERENCES vagas(id),
    guia_id BIGINT REFERENCES guias_matricula(id)
);

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255),
    matricula VARCHAR(255),
    email VARCHAR(255),
    senha VARCHAR(255),
    limite_matricula INTEGER,
    tempo_estudo INTEGER,
    tempo_transporte INTEGER,
    ano_ingresso INTEGER,
    mes_ingresso INTEGER,
    periodo_atual INTEGER,
    is_teste BOOLEAN NOT NULL DEFAULT FALSE,
    curso_id BIGINT REFERENCES cursos(id)
);

CREATE TABLE IF NOT EXISTS usuario_turnos_livres (
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    livre BOOLEAN
);

CREATE TABLE IF NOT EXISTS usuario_professores_excluidos (
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    professor VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS usuario_disciplinas_feitas (
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    componente_codigo VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS usuario_turmas_selecionadas (
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    turma_id BIGINT NOT NULL REFERENCES turmas(id) ON DELETE CASCADE,
    PRIMARY KEY (turma_id, usuario_id)
);

CREATE TABLE IF NOT EXISTS avaliacoes_professor (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    professor_nome VARCHAR(255) NOT NULL,
    componente_id BIGINT NOT NULL REFERENCES componentes_curriculares(id) ON DELETE CASCADE,
    nota INTEGER NOT NULL CHECK (nota BETWEEN 1 AND 5),
    comentario VARCHAR(255),
    data_avaliacao TIMESTAMP,
    UNIQUE (usuario_id, professor_nome, componente_id)
);

CREATE TABLE IF NOT EXISTS prerequisitos (
    id BIGSERIAL PRIMARY KEY,
    componente_id BIGINT NOT NULL REFERENCES componentes_curriculares(id) ON DELETE CASCADE,
    componente_prerequisito_id BIGINT NOT NULL REFERENCES componentes_curriculares(id) ON DELETE CASCADE,
    tipo VARCHAR(255),
    UNIQUE (componente_id, componente_prerequisito_id)
);

CREATE TABLE IF NOT EXISTS historico_estudante (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    componente_id BIGINT NOT NULL REFERENCES componentes_curriculares(id) ON DELETE CASCADE,
    semestre VARCHAR(10) NOT NULL,
    nota DOUBLE PRECISION,
    status VARCHAR(20) NOT NULL,
    data_conclusao DATE,
    UNIQUE (usuario_id, componente_id, semestre)
);

CREATE TABLE IF NOT EXISTS preferencias_usuario (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE REFERENCES usuarios(id) ON DELETE CASCADE,
    turnos_disponiveis VARCHAR(100),
    professores_banidos TEXT,
    data_atualizacao TIMESTAMP
);
