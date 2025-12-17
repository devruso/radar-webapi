-- Popula dados de exemplo para o ambiente de desenvolvimento
-- Este script assume que as tabelas já existem (criada pelo Hibernate)

-- 1) Curso, EstruturaCurso, GuiaMatricula
INSERT INTO estrutura_curso (curso, municipio, entrada, codigo, ch_optativa, ch_obrigatoria, ch_complementar)
VALUES ('Ciência da Computação', 'Salvador', '2020', 'CC-2020', 20, 200, 40);

INSERT INTO cursos (nome, coordenador, nivel, turno, estrutura_id)
VALUES ('Ciência da Computação', 'Prof. Exemplo', 'Graduação', 'M', (SELECT id FROM estrutura_curso WHERE codigo='CC-2020'));

INSERT INTO guias_matricula (ano_periodo, curso_id)
VALUES ('2025-2', (SELECT id FROM cursos WHERE nome='Ciência da Computação'));

-- 2) Componentes curriculares
INSERT INTO componentes_curriculares (codigo, nome, nivel, ementa, tipo, prerequisito, corequisito, posrequisito)
VALUES ('CC101','Algoritmos I',1,'Introdução a algoritmos','Obrigatoria', NULL, NULL, NULL),
       ('CC102','Algoritmos II',2,'Algoritmos e estruturas de dados','Obrigatoria','CC101', NULL, NULL);

-- 3) Horarios
INSERT INTO horarios (codigo, turno) VALUES ('H1','M'), ('H2','T');

-- Horario map (horario_map): horario_id, dia, horarios
INSERT INTO horario_map (horario_id, dia, horarios) VALUES
((SELECT id FROM horarios WHERE codigo='H1'), 'SEG', '08:00-10:00'),
((SELECT id FROM horarios WHERE codigo='H1'), 'QUA', '08:00-10:00'),
((SELECT id FROM horarios WHERE codigo='H2'), 'TER', '14:00-16:00');

-- 4) Vagas
INSERT INTO vagas (total_vagas) VALUES (30), (25);

-- reservas (vagas_reserva): vagas_id, tipo, quantidade
INSERT INTO vagas_reserva (vagas_id, tipo, quantidade) VALUES
((SELECT id FROM vagas WHERE total_vagas=30 LIMIT 1), 'regular', 20),
((SELECT id FROM vagas WHERE total_vagas=30 LIMIT 1), 'monitor', 2);

-- 5) Turmas
INSERT INTO turmas (local, professor, numero, tipo, componente_id, horario_id, vagas_id, guia_id)
VALUES ('Sala A','Prof. A','101','1', (SELECT id FROM componentes_curriculares WHERE codigo='CC101'), (SELECT id FROM horarios WHERE codigo='H1'), (SELECT id FROM vagas WHERE total_vagas=30 LIMIT 1), (SELECT id FROM guias_matricula WHERE ano_periodo='2025-2')),
       ('Sala B','Prof. B','102','1', (SELECT id FROM componentes_curriculares WHERE codigo='CC102'), (SELECT id FROM horarios WHERE codigo='H2'), (SELECT id FROM vagas WHERE total_vagas=25 LIMIT 1), (SELECT id FROM guias_matricula WHERE ano_periodo='2025-2'));

-- 6) Usuario de exemplo
INSERT INTO usuarios (nome, matricula, email, limite_matricula, tempo_estudo, tempo_transporte, curso_id)
VALUES ('Aluno Teste','20250001','aluno@exemplo.com', 4, 10, 30, (SELECT id FROM cursos WHERE nome='Ciência da Computação'));

-- Element collections do usuario (ids obtidos por subselect)
INSERT INTO usuario_turnos_livres (usuario_id, livre)
VALUES ((SELECT id FROM usuarios WHERE matricula='20250001'), true),
       ((SELECT id FROM usuarios WHERE matricula='20250001'), false),
       ((SELECT id FROM usuarios WHERE matricula='20250001'), true);

INSERT INTO usuario_professores_excluidos (usuario_id, professor)
VALUES ((SELECT id FROM usuarios WHERE matricula='20250001'), 'Prof. B');

INSERT INTO usuario_disciplinas_feitas (usuario_id, componente_codigo)
VALUES ((SELECT id FROM usuarios WHERE matricula='20250001'), 'CC101');

-- Selecionar turma já matriculada (join table)
INSERT INTO usuario_turmas_selecionadas (usuario_id, turma_id)
VALUES ((SELECT id FROM usuarios WHERE matricula='20250001'), (SELECT id FROM turmas WHERE numero='101'));

-- Observação: ajuste o conteúdo conforme necessário para seu ambiente.
