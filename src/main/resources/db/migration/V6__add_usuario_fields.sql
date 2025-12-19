-- Adiciona novos campos ao usuário para suportar fluxo de cadastro/teste
-- e preferências do aluno (mês de ingresso, senha, flag de teste)

-- Adiciona campos novos (IF NOT EXISTS)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='usuarios' AND column_name='senha') THEN
        ALTER TABLE usuarios ADD COLUMN senha VARCHAR(255);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='usuarios' AND column_name='mes_ingresso') THEN
        ALTER TABLE usuarios ADD COLUMN mes_ingresso INTEGER;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='usuarios' AND column_name='is_teste') THEN
        ALTER TABLE usuarios ADD COLUMN is_teste BOOLEAN NOT NULL DEFAULT false;
    END IF;
END $$;

-- Atualiza tipo do ano_ingresso de VARCHAR para INTEGER (apenas se necessário)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name='usuarios' AND column_name='ano_ingresso' 
        AND data_type='character varying'
    ) THEN
        ALTER TABLE usuarios ALTER COLUMN ano_ingresso TYPE INTEGER USING ano_ingresso::INTEGER;
    END IF;
END $$;

-- Comentários para documentação
COMMENT ON COLUMN usuarios.senha IS 'Hash BCrypt da senha do usuário (null se for teste)';
COMMENT ON COLUMN usuarios.mes_ingresso IS 'Mês de ingresso (1-12)';
COMMENT ON COLUMN usuarios.ano_ingresso IS 'Ano de ingresso (ex.: 2025)';
COMMENT ON COLUMN usuarios.is_teste IS 'Flag para diferenciar usuário teste de usuário autenticado';
