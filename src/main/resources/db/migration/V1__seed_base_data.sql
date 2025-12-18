-- Seed base academic data: courses, structures, components, schedules, classes (turmas), and vacancies

-- Estruturas de curso
INSERT INTO estrutura_curso (ch_complementar, ch_obrigatoria, ch_optativa, codigo, curso, entrada, municipio)
VALUES (200, 2000, 400, 'SI-2025', 'Bacharelado em Sistemas de Informação', '2025.1', 'Salvador');

INSERT INTO estrutura_curso (ch_complementar, ch_obrigatoria, ch_optativa, codigo, curso, entrada, municipio)
VALUES (180, 2200, 420, 'CC-2025', 'Bacharelado em Ciência da Computação', '2025.1', 'Salvador');

-- Cursos (guia_id será atualizado após criação da guia)
INSERT INTO cursos (coordenador, nivel, nome, turno, estrutura_id, guia_id)
VALUES ('Prof. Coordenador SI', 'Graduação', 'Sistemas de Informação', 'Integral',
        (SELECT id FROM estrutura_curso WHERE codigo='SI-2025'), NULL);

INSERT INTO cursos (coordenador, nivel, nome, turno, estrutura_id, guia_id)
VALUES ('Prof. Coordenador CC', 'Graduação', 'Ciência da Computação', 'Integral',
        (SELECT id FROM estrutura_curso WHERE codigo='CC-2025'), NULL);

-- Guias de matrícula
INSERT INTO guias_matricula (ano_periodo, curso_id)
VALUES ('2025.1', (SELECT id FROM cursos WHERE nome='Sistemas de Informação'));

INSERT INTO guias_matricula (ano_periodo, curso_id)
VALUES ('2025.1', (SELECT id FROM cursos WHERE nome='Ciência da Computação'));

-- Atualiza cursos para vincular guia
UPDATE cursos SET guia_id = (SELECT id FROM guias_matricula WHERE curso_id = cursos.id) WHERE guia_id IS NULL;

-- Componentes curriculares (disciplinas)
INSERT INTO componentes_curriculares (codigo, nome, nivel, tipo, ementa, prerequisito, corequisito, posrequisito, estrutura_id)
VALUES ('MDIS', 'Matemática Discreta', 1, 'Obrigatória', 'Conjuntos, relações, grafos, lógica, combinatória', NULL, NULL, NULL,
        (SELECT id FROM estrutura_curso WHERE codigo='SI-2025'));

INSERT INTO componentes_curriculares (codigo, nome, nivel, tipo, ementa, prerequisito, corequisito, posrequisito, estrutura_id)
VALUES ('CALCA', 'Cálculo A', 1, 'Obrigatória', 'Funções, limites, derivadas, aplicações', NULL, NULL, NULL,
        (SELECT id FROM estrutura_curso WHERE codigo='SI-2025'));

INSERT INTO componentes_curriculares (codigo, nome, nivel, tipo, ementa, prerequisito, corequisito, posrequisito, estrutura_id)
VALUES ('ILP', 'Introdução à Lógica de Programação', 1, 'Obrigatória', 'Algoritmos, estruturas de controle, funções', NULL, NULL, NULL,
        (SELECT id FROM estrutura_curso WHERE codigo='SI-2025'));

INSERT INTO componentes_curriculares (codigo, nome, nivel, tipo, ementa, prerequisito, corequisito, posrequisito, estrutura_id)
VALUES ('ED', 'Estruturas de Dados', 2, 'Obrigatória', 'Listas, pilhas, filas, árvores, grafos', 'ILP', NULL, NULL,
        (SELECT id FROM estrutura_curso WHERE codigo='SI-2025'));

-- Mesmas disciplinas para CC (códigos diferentes para evitar conflito de UNIQUE)
INSERT INTO componentes_curriculares (codigo, nome, nivel, tipo, ementa, prerequisito, corequisito, posrequisito, estrutura_id)
VALUES ('MDIS_CC', 'Matemática Discreta', 1, 'Obrigatória', 'Conjuntos, relações, grafos, lógica, combinatória', NULL, NULL, NULL,
        (SELECT id FROM estrutura_curso WHERE codigo='CC-2025'));

INSERT INTO componentes_curriculares (codigo, nome, nivel, tipo, ementa, prerequisito, corequisito, posrequisito, estrutura_id)
VALUES ('CALCA_CC', 'Cálculo A', 1, 'Obrigatória', 'Funções, limites, derivadas, aplicações', NULL, NULL, NULL,
        (SELECT id FROM estrutura_curso WHERE codigo='CC-2025'));

INSERT INTO componentes_curriculares (codigo, nome, nivel, tipo, ementa, prerequisito, corequisito, posrequisito, estrutura_id)
VALUES ('ILP_CC', 'Introdução à Lógica de Programação', 1, 'Obrigatória', 'Algoritmos, estruturas de controle, funções', NULL, NULL, NULL,
        (SELECT id FROM estrutura_curso WHERE codigo='CC-2025'));

INSERT INTO componentes_curriculares (codigo, nome, nivel, tipo, ementa, prerequisito, corequisito, posrequisito, estrutura_id)
VALUES ('ED_CC', 'Estruturas de Dados', 2, 'Obrigatória', 'Listas, pilhas, filas, árvores, grafos', 'ILP_CC', NULL, NULL,
        (SELECT id FROM estrutura_curso WHERE codigo='CC-2025'));

-- Horários (um por turma)
INSERT INTO horarios (codigo, turno) VALUES ('MDIS-T1', 'Matutino');
INSERT INTO horarios (codigo, turno) VALUES ('CALCA-T1', 'Matutino');
INSERT INTO horarios (codigo, turno) VALUES ('ILP-T1', 'Vespertino');
INSERT INTO horarios (codigo, turno) VALUES ('ED-T1', 'Noturno');

INSERT INTO horarios (codigo, turno) VALUES ('MDIS_CC-T1', 'Matutino');
INSERT INTO horarios (codigo, turno) VALUES ('CALCA_CC-T1', 'Matutino');
INSERT INTO horarios (codigo, turno) VALUES ('ILP_CC-T1', 'Vespertino');
INSERT INTO horarios (codigo, turno) VALUES ('ED_CC-T1', 'Noturno');

-- Vagas (uma por turma). Referenciadas por MAX(id) logo após cada inserção
INSERT INTO vagas (total_vagas) VALUES (50);
INSERT INTO turmas (local, numero, professor, tipo, componente_id, guia_id, horario_id, vagas_id)
VALUES ('ICS - UFBA', 'T1', 'Prof. Ada Lovelace', 1,
        (SELECT id FROM componentes_curriculares WHERE codigo='MDIS'),
        (SELECT id FROM guias_matricula WHERE curso_id = (SELECT id FROM cursos WHERE nome='Sistemas de Informação')),
        (SELECT id FROM horarios WHERE codigo='MDIS-T1'),
        (SELECT MAX(id) FROM vagas));

INSERT INTO vagas (total_vagas) VALUES (50);
INSERT INTO turmas (local, numero, professor, tipo, componente_id, guia_id, horario_id, vagas_id)
VALUES ('ICS - UFBA', 'T1', 'Prof. Isaac Newton', 1,
        (SELECT id FROM componentes_curriculares WHERE codigo='CALCA'),
        (SELECT id FROM guias_matricula WHERE curso_id = (SELECT id FROM cursos WHERE nome='Sistemas de Informação')),
        (SELECT id FROM horarios WHERE codigo='CALCA-T1'),
        (SELECT MAX(id) FROM vagas));

INSERT INTO vagas (total_vagas) VALUES (60);
INSERT INTO turmas (local, numero, professor, tipo, componente_id, guia_id, horario_id, vagas_id)
VALUES ('ICE - UFBA', 'T1', 'Prof. Alan Turing', 1,
        (SELECT id FROM componentes_curriculares WHERE codigo='ILP'),
        (SELECT id FROM guias_matricula WHERE curso_id = (SELECT id FROM cursos WHERE nome='Sistemas de Informação')),
        (SELECT id FROM horarios WHERE codigo='ILP-T1'),
        (SELECT MAX(id) FROM vagas));

INSERT INTO vagas (total_vagas) VALUES (45);
INSERT INTO turmas (local, numero, professor, tipo, componente_id, guia_id, horario_id, vagas_id)
VALUES ('ICE - UFBA', 'T1', 'Prof. Edsger Dijkstra', 1,
        (SELECT id FROM componentes_curriculares WHERE codigo='ED'),
        (SELECT id FROM guias_matricula WHERE curso_id = (SELECT id FROM cursos WHERE nome='Sistemas de Informação')),
        (SELECT id FROM horarios WHERE codigo='ED-T1'),
        (SELECT MAX(id) FROM vagas));

-- CC
INSERT INTO vagas (total_vagas) VALUES (50);
INSERT INTO turmas (local, numero, professor, tipo, componente_id, guia_id, horario_id, vagas_id)
VALUES ('IME - UFBA', 'T1', 'Prof. Emmy Noether', 1,
        (SELECT id FROM componentes_curriculares WHERE codigo='MDIS_CC'),
        (SELECT id FROM guias_matricula WHERE curso_id = (SELECT id FROM cursos WHERE nome='Ciência da Computação')),
        (SELECT id FROM horarios WHERE codigo='MDIS_CC-T1'),
        (SELECT MAX(id) FROM vagas));

INSERT INTO vagas (total_vagas) VALUES (50);
INSERT INTO turmas (local, numero, professor, tipo, componente_id, guia_id, horario_id, vagas_id)
VALUES ('IME - UFBA', 'T1', 'Prof. Leonhard Euler', 1,
        (SELECT id FROM componentes_curriculares WHERE codigo='CALCA_CC'),
        (SELECT id FROM guias_matricula WHERE curso_id = (SELECT id FROM cursos WHERE nome='Ciência da Computação')),
        (SELECT id FROM horarios WHERE codigo='CALCA_CC-T1'),
        (SELECT MAX(id) FROM vagas));

INSERT INTO vagas (total_vagas) VALUES (60);
INSERT INTO turmas (local, numero, professor, tipo, componente_id, guia_id, horario_id, vagas_id)
VALUES ('ICE - UFBA', 'T1', 'Prof. Donald Knuth', 1,
        (SELECT id FROM componentes_curriculares WHERE codigo='ILP_CC'),
        (SELECT id FROM guias_matricula WHERE curso_id = (SELECT id FROM cursos WHERE nome='Ciência da Computação')),
        (SELECT id FROM horarios WHERE codigo='ILP_CC-T1'),
        (SELECT MAX(id) FROM vagas));

INSERT INTO vagas (total_vagas) VALUES (45);
INSERT INTO turmas (local, numero, professor, tipo, componente_id, guia_id, horario_id, vagas_id)
VALUES ('ICE - UFBA', 'T1', 'Prof. Niklaus Wirth', 1,
        (SELECT id FROM componentes_curriculares WHERE codigo='ED_CC'),
        (SELECT id FROM guias_matricula WHERE curso_id = (SELECT id FROM cursos WHERE nome='Ciência da Computação')),
        (SELECT id FROM horarios WHERE codigo='ED_CC-T1'),
        (SELECT MAX(id) FROM vagas));
