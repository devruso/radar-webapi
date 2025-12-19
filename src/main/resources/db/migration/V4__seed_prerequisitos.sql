-- V4: Seed data para pré-requisitos e exemplo de recomendações

-- Adicionar pré-requisitos para as disciplinas (exemplo com dados SI)
-- Exemplo: Banco de Dados II requer Banco de Dados I
INSERT INTO prerequisitos (componente_id, componente_prerequisito_id, tipo)
SELECT 
    (SELECT id FROM componentes_curriculares WHERE codigo='BD2' LIMIT 1),
    (SELECT id FROM componentes_curriculares WHERE codigo='BD1' LIMIT 1),
    'PREREQUISITO'
WHERE EXISTS (SELECT 1 FROM componentes_curriculares WHERE codigo='BD1')
  AND EXISTS (SELECT 1 FROM componentes_curriculares WHERE codigo='BD2');

-- Exemplo: Programação II requer Programação I
INSERT INTO prerequisitos (componente_id, componente_prerequisito_id, tipo)
SELECT 
    (SELECT id FROM componentes_curriculares WHERE codigo='PROG2' LIMIT 1),
    (SELECT id FROM componentes_curriculares WHERE codigo='PROG1' LIMIT 1),
    'PREREQUISITO'
WHERE EXISTS (SELECT 1 FROM componentes_curriculares WHERE codigo='PROG1')
  AND EXISTS (SELECT 1 FROM componentes_curriculares WHERE codigo='PROG2')
  AND NOT EXISTS (
    SELECT 1 FROM prerequisitos p1
    JOIN componentes_curriculares c1 ON p1.componente_id = c1.id
    JOIN componentes_curriculares c2 ON p1.componente_prerequisito_id = c2.id
    WHERE c1.codigo='PROG2' AND c2.codigo='PROG1'
  );

-- Dados de exemplo para professor (avaliação neutra inicial)
-- Estes dados serão adicionados quando alunos fizerem avaliações
