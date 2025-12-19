-- V2: Ajustes para UFBA - Sistemas de Informação (Salvador)
-- Atualiza estrutura do curso para o código oficial e cargas horárias

UPDATE estrutura_curso
SET codigo = 'G20251X',
    curso = 'SISTEMAS DE INFORMAÇÃO - SALVADOR - BACHARELADO',
    entrada = '2025.2',
    municipio = 'Salvador',
    ch_obrigatoria = 2206,
    ch_optativa    = 480,
    ch_complementar= 415
WHERE codigo = 'SI-2025';

-- Caso a estrutura original não exista, insere nova
INSERT INTO estrutura_curso (ch_complementar, ch_obrigatoria, ch_optativa, codigo, curso, entrada, municipio)
SELECT 415, 2206, 480, 'G20251X', 'SISTEMAS DE INFORMAÇÃO - SALVADOR - BACHARELADO', '2025.2', 'Salvador'
WHERE NOT EXISTS (SELECT 1 FROM estrutura_curso WHERE codigo = 'G20251X');

-- Garante que o curso "Sistemas de Informação" aponte para a estrutura atualizada
UPDATE cursos
SET estrutura_id = (SELECT id FROM estrutura_curso WHERE codigo = 'G20251X')
WHERE nome = 'Sistemas de Informação';

-- Componentes Optativos principais (amostra representativa, tipo='Optativa')
-- Observação: nivel e ementa não informados -> NULL
INSERT INTO componentes_curriculares (codigo, nome, nivel, tipo, ementa, prerequisito, corequisito, posrequisito, estrutura_id)
SELECT 'MATA50', 'LINGUAGENS FORMAIS E AUTÔMATOS', NULL, 'Optativa', NULL, NULL, NULL, NULL,
       (SELECT id FROM estrutura_curso WHERE codigo='G20251X')
WHERE NOT EXISTS (SELECT 1 FROM componentes_curriculares WHERE codigo='MATA50');

INSERT INTO componentes_curriculares (codigo, nome, nivel, tipo, ementa, prerequisito, corequisito, posrequisito, estrutura_id)
SELECT 'MATA51', 'TEORIA DA COMPUTAÇÃO', NULL, 'Optativa', NULL, NULL, NULL, NULL,
       (SELECT id FROM estrutura_curso WHERE codigo='G20251X')
WHERE NOT EXISTS (SELECT 1 FROM componentes_curriculares WHERE codigo='MATA51');

INSERT INTO componentes_curriculares (codigo, nome, nivel, tipo, ementa, prerequisito, corequisito, posrequisito, estrutura_id)
SELECT 'MATA53', 'TEORIA DOS GRAFOS', NULL, 'Optativa', NULL, NULL, NULL, NULL,
       (SELECT id FROM estrutura_curso WHERE codigo='G20251X')
WHERE NOT EXISTS (SELECT 1 FROM componentes_curriculares WHERE codigo='MATA53');

INSERT INTO componentes_curriculares (codigo, nome, nivel, tipo, ementa, prerequisito, corequisito, posrequisito, estrutura_id)
SELECT 'MATA54', 'ESTRUTURAS DE DADOS E ALGORITMOS II', NULL, 'Optativa', NULL, NULL, NULL, NULL,
       (SELECT id FROM estrutura_curso WHERE codigo='G20251X')
WHERE NOT EXISTS (SELECT 1 FROM componentes_curriculares WHERE codigo='MATA54');

INSERT INTO componentes_curriculares (codigo, nome, nivel, tipo, ementa, prerequisito, corequisito, posrequisito, estrutura_id)
SELECT 'MATA85', 'REDES DE COMPUTADORES II', NULL, 'Optativa', NULL, NULL, NULL, NULL,
       (SELECT id FROM estrutura_curso WHERE codigo='G20251X')
WHERE NOT EXISTS (SELECT 1 FROM componentes_curriculares WHERE codigo='MATA85');

INSERT INTO componentes_curriculares (codigo, nome, nivel, tipo, ementa, prerequisito, corequisito, posrequisito, estrutura_id)
SELECT 'MATA87', 'SEGURANÇA DA INFORMAÇÃO', NULL, 'Optativa', NULL, NULL, NULL, NULL,
       (SELECT id FROM estrutura_curso WHERE codigo='G20251X')
WHERE NOT EXISTS (SELECT 1 FROM componentes_curriculares WHERE codigo='MATA87');

INSERT INTO componentes_curriculares (codigo, nome, nivel, tipo, ementa, prerequisito, corequisito, posrequisito, estrutura_id)
SELECT 'MATA88', 'FUNDAMENTOS DE SISTEMAS DISTRIBUÍDOS', NULL, 'Optativa', NULL, NULL, NULL, NULL,
       (SELECT id FROM estrutura_curso WHERE codigo='G20251X')
WHERE NOT EXISTS (SELECT 1 FROM componentes_curriculares WHERE codigo='MATA88');

INSERT INTO componentes_curriculares (codigo, nome, nivel, tipo, ementa, prerequisito, corequisito, posrequisito, estrutura_id)
SELECT 'MATF57', 'INTRODUÇÃO AO RACIOCÍNIO COMPUTACIONAL', NULL, 'Optativa', NULL, NULL, NULL, NULL,
       (SELECT id FROM estrutura_curso WHERE codigo='G20251X')
WHERE NOT EXISTS (SELECT 1 FROM componentes_curriculares WHERE codigo='MATF57');

INSERT INTO componentes_curriculares (codigo, nome, nivel, tipo, ementa, prerequisito, corequisito, posrequisito, estrutura_id)
SELECT 'MATA01', 'GEOMETRIA ANALÍTICA', NULL, 'Optativa', NULL, NULL, NULL, NULL,
       (SELECT id FROM estrutura_curso WHERE codigo='G20251X')
WHERE NOT EXISTS (SELECT 1 FROM componentes_curriculares WHERE codigo='MATA01');

INSERT INTO componentes_curriculares (codigo, nome, nivel, tipo, ementa, prerequisito, corequisito, posrequisito, estrutura_id)
SELECT 'MATA03', 'CÁLCULO B', NULL, 'Optativa', NULL, NULL, NULL, NULL,
       (SELECT id FROM estrutura_curso WHERE codigo='G20251X')
WHERE NOT EXISTS (SELECT 1 FROM componentes_curriculares WHERE codigo='MATA03');

INSERT INTO componentes_curriculares (codigo, nome, nivel, tipo, ementa, prerequisito, corequisito, posrequisito, estrutura_id)
SELECT 'MATA52', 'ANÁLISE E PROJETO DE ALGORITMOS', NULL, 'Optativa', NULL, NULL, NULL, NULL,
       (SELECT id FROM estrutura_curso WHERE codigo='G20251X')
WHERE NOT EXISTS (SELECT 1 FROM componentes_curriculares WHERE codigo='MATA52');

-- Horários para turmas UFBA
INSERT INTO horarios (codigo, turno)
SELECT 'MATA03-T1', 'Matutino'
WHERE NOT EXISTS (SELECT 1 FROM horarios WHERE codigo='MATA03-T1');

INSERT INTO horarios (codigo, turno)
SELECT 'MATF57-T1', 'Vespertino'
WHERE NOT EXISTS (SELECT 1 FROM horarios WHERE codigo='MATF57-T1');

INSERT INTO horarios (codigo, turno)
SELECT 'MATA50-T1', 'Noturno'
WHERE NOT EXISTS (SELECT 1 FROM horarios WHERE codigo='MATA50-T1');

-- Vagas e turmas UFBA (Sistemas de Informação)

-- Cálculo B
DO $$
DECLARE
    v_vagas_id bigint;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM turmas WHERE numero='T1' AND componente_id=(SELECT id FROM componentes_curriculares WHERE codigo='MATA03')) THEN
        INSERT INTO vagas (total_vagas) VALUES (50) RETURNING id INTO v_vagas_id;
        INSERT INTO turmas (local, numero, professor, tipo, componente_id, guia_id, horario_id, vagas_id)
        VALUES ('IME - UFBA', 'T1', 'Prof. Maria Gaetana Agnesi', 1,
                (SELECT id FROM componentes_curriculares WHERE codigo='MATA03'),
                (SELECT id FROM guias_matricula WHERE curso_id = (SELECT id FROM cursos WHERE nome='Sistemas de Informação')),
                (SELECT id FROM horarios WHERE codigo='MATA03-T1'),
                v_vagas_id);
    END IF;
END $$;

-- Introdução ao Raciocínio Computacional
DO $$
DECLARE
    v_vagas_id bigint;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM turmas WHERE numero='T1' AND componente_id=(SELECT id FROM componentes_curriculares WHERE codigo='MATF57')) THEN
        INSERT INTO vagas (total_vagas) VALUES (60) RETURNING id INTO v_vagas_id;
        INSERT INTO turmas (local, numero, professor, tipo, componente_id, guia_id, horario_id, vagas_id)
        VALUES ('ICE - UFBA', 'T1', 'Prof. Ada Lovelace', 1,
                (SELECT id FROM componentes_curriculares WHERE codigo='MATF57'),
                (SELECT id FROM guias_matricula WHERE curso_id = (SELECT id FROM cursos WHERE nome='Sistemas de Informação')),
                (SELECT id FROM horarios WHERE codigo='MATF57-T1'),
                v_vagas_id);
    END IF;
END $$;

-- Linguagens Formais e Autômatos
DO $$
DECLARE
    v_vagas_id bigint;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM turmas WHERE numero='T1' AND componente_id=(SELECT id FROM componentes_curriculares WHERE codigo='MATA50')) THEN
        INSERT INTO vagas (total_vagas) VALUES (45) RETURNING id INTO v_vagas_id;
        INSERT INTO turmas (local, numero, professor, tipo, componente_id, guia_id, horario_id, vagas_id)
        VALUES ('ICE - UFBA', 'T1', 'Prof. Noam Chomsky', 1,
                (SELECT id FROM componentes_curriculares WHERE codigo='MATA50'),
                (SELECT id FROM guias_matricula WHERE curso_id = (SELECT id FROM cursos WHERE nome='Sistemas de Informação')),
                (SELECT id FROM horarios WHERE codigo='MATA50-T1'),
                v_vagas_id);
    END IF;
END $$;
