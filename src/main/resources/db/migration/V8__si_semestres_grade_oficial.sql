-- V8: Grade oficial de Sistemas de Informação (semestres 1-10)
-- Remove seeds anteriores do curso SI e insere componentes com nivel=semestre

-- Remove turmas vinculadas aos componentes da estrutura SI
DELETE FROM turmas t
USING componentes_curriculares c
WHERE t.componente_id = c.id
  AND c.estrutura_id = (SELECT id FROM estrutura_curso WHERE codigo = 'G20251X');

-- Remove componentes antigos da estrutura SI
DELETE FROM componentes_curriculares
WHERE estrutura_id = (SELECT id FROM estrutura_curso WHERE codigo = 'G20251X');

-- Cadastro de componentes por semestre (nivel = semestre)
-- Semestre 1
INSERT INTO componentes_curriculares (codigo, nome, nivel, tipo, estrutura_id)
VALUES
 ('CALC1', 'Cálculo 1', 1, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X')),
 ('CES', 'Computador, Ética e Sociedade', 1, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X')),
 ('ILP', 'Introdução à Lógica de Programação', 1, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X')),
 ('CDA1', 'Circuitos Digitais e Arquitetura de Computadores', 1, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X')),
 ('SIC', 'Seminários de Introdução ao Curso', 1, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X'));

-- Semestre 2
INSERT INTO componentes_curriculares (codigo, nome, nivel, tipo, estrutura_id)
VALUES
 ('ECOI', 'Economia da Inovação', 2, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X')),
 ('CDA2', 'Circuitos Digitais e Arquitetura de Computadores II', 2, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X')),
 ('ED', 'Estruturas de Dados', 2, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X')),
 ('FSI', 'Fundamentos de Sistemas de Informação', 2, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X')),
 ('ILM', 'Introdução à Lógica Matemática', 2, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X'));

-- Semestre 3
INSERT INTO componentes_curriculares (codigo, nome, nivel, tipo, estrutura_id)
VALUES
 ('ALGLIN', 'Álgebra Linear', 3, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X')),
 ('IADM', 'Introdução a Administração', 3, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X')),
 ('LFTC', 'Linguagens Formais e Teoria da Computação', 3, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X')),
 ('POO', 'Programação Orientada a Objetos', 3, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X')),
 ('SO', 'Sistemas Operacionais', 3, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X'));

-- Semestre 4
INSERT INTO componentes_curriculares (codigo, nome, nivel, tipo, estrutura_id)
VALUES
 ('ES1', 'Engenharia de Software I', 4, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X')),
 ('MEST', 'Métodos Estatísticos', 4, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X')),
 ('OLPT', 'Oficina de Leitura e Produção de Textos', 4, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X')),
 ('REDES1', 'Redes de Computadores I', 4, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X')),
 ('SWEB', 'Sistemas Web', 4, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X'));

-- Semestre 5
INSERT INTO componentes_curriculares (codigo, nome, nivel, tipo, estrutura_id)
VALUES
 ('BD', 'Banco de Dados', 5, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X')),
 ('ES2', 'Engenharia de Software II', 5, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X')),
 ('LPWEB', 'Laboratório de Programação Web', 5, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X')),
 ('MQAA', 'Métodos Quantitativos Aplicados à Administração', 5, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X')),
 ('PLP', 'Paradigmas de Linguagem de Programação', 5, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X'));

-- Semestre 6
INSERT INTO componentes_curriculares (codigo, nome, nivel, tipo, estrutura_id)
VALUES
 ('ADMOV', 'Aplicações para Dispositivos Móveis', 6, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X')),
 ('EMPINF', 'Empreendedores em Informática', 6, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X')),
 ('LBD', 'Laboratório de Banco de Dados', 6, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X')),
 ('LAC', 'Linguagens para Aplicação Comercial', 6, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X')),
 ('SAD', 'Sistemas de Apoio a Decisão', 6, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X'));

-- Semestre 7
INSERT INTO componentes_curriculares (codigo, nome, nivel, tipo, estrutura_id)
VALUES
 ('IA', 'Inteligência Artificial', 7, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X')),
 ('IHC', 'Interação Humano Computador', 7, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X')),
 ('QS', 'Qualidade de Software', 7, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X')),
 ('SASI', 'Segurança e Auditoria de Sistemas de Informação', 7, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X')),
 ('SMULT', 'Sistemas Multimídia', 7, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X'));

-- Semestre 8 (optativas - deixado vazio intencionalmente)

-- Semestre 9
INSERT INTO componentes_curriculares (codigo, nome, nivel, tipo, estrutura_id)
VALUES ('TCC1', 'TCC Bacharelado Sistemas de Informação I', 9, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X'));

-- Semestre 10
INSERT INTO componentes_curriculares (codigo, nome, nivel, tipo, estrutura_id)
VALUES ('TCC2', 'TCC Bacharelado Sistemas de Informação II', 10, 'Obrigatória', (SELECT id FROM estrutura_curso WHERE codigo='G20251X'));

-- Horários (um por componente)
INSERT INTO horarios (codigo, turno)
SELECT CONCAT(c.codigo, '-T1'), 'Matutino'
FROM componentes_curriculares c
WHERE c.estrutura_id = (SELECT id FROM estrutura_curso WHERE codigo='G20251X')
  AND NOT EXISTS (SELECT 1 FROM horarios h WHERE h.codigo = CONCAT(c.codigo, '-T1'));

-- Vagas e turmas (uma turma T1 por componente)
DO $$
DECLARE
    v_guia_id BIGINT;
    rec RECORD;
BEGIN
    SELECT id INTO v_guia_id FROM guias_matricula WHERE curso_id = (SELECT id FROM cursos WHERE nome='Sistemas de Informação') LIMIT 1;

    FOR rec IN (
        SELECT id, codigo FROM componentes_curriculares WHERE estrutura_id = (SELECT id FROM estrutura_curso WHERE codigo='G20251X')
    ) LOOP
        INSERT INTO vagas (total_vagas) VALUES (60);
        INSERT INTO turmas (local, numero, professor, tipo, componente_id, guia_id, horario_id, vagas_id)
        SELECT 'ICS - UFBA', 'T1', 'Prof. A Confirmar', 1,
               rec.id,
               v_guia_id,
               (SELECT id FROM horarios WHERE codigo = CONCAT(rec.codigo, '-T1')),
               (SELECT MAX(id) FROM vagas)
        WHERE NOT EXISTS (SELECT 1 FROM turmas WHERE componente_id = rec.id);
    END LOOP;
END $$;
