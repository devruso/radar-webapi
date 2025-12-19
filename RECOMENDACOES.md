# Guia de Recomendações - RADAR API

## Visão Geral

O sistema de recomendações do RADAR sugere disciplinas para alunos seguindo uma estratégia "dumb" (simples) mas eficiente que considera:

1. **Disciplinas já concluídas** - Não recomenda o que aluno já fez
2. **Professores excluídos** - Respeita preferências do aluno
3. **Vagas disponíveis** - Apenas turmas com assentos livres
4. **Pré-requisitos** - Valida se aluno tem conhecimento prévio necessário
5. **Dificuldade** - Balanceia fácil → intermediário → difícil
6. **Rating de professores** - Prioriza docentes bem avaliados (1-5 estrelas)

## Fluxo de Uso

### 1. Gerar Recomendações

```bash
POST /api/recomendacoes/gerar/{usuarioId}?metodo=burrinho

Resposta (200 OK):
[
  {
    "turma": {
      "id": 1,
      "professor": "Prof. Silva",
      "local": "Sala 101",
      "numero": "01",
      "componenteCurricular": {
        "id": 5,
        "codigo": "PROG1",
        "nome": "Programação I",
        "nivel": 1
      }
    },
    "dificuldade": "FACIL",
    "scoreProfessor": 4.5,
    "motivo": "Disciplina Programação I (dificuldade FACIL) com prof. Prof. Silva (score: 4.5)",
    "posicao": 1
  },
  {
    "turma": {...},
    "dificuldade": "FACIL",
    "scoreProfessor": 3.8,
    "motivo": "...",
    "posicao": 2
  }
]
```

**Retorna**: 3-8 disciplinas ordenadas por estratégia

---

### 2. Avaliar Professor (Após Conclusão de Semestre)

```bash
POST /api/recomendacoes/avaliar-professor
  ?usuarioId=1
  &professorNome=Prof. Silva
  &componenteId=5
  &nota=5
  &comentario=Excelente professor, muito didático

Resposta (200 OK):
{
  "id": 42,
  "usuarioId": 1,
  "professorNome": "Prof. Silva",
  "componenteId": 5,
  "nota": 5,
  "comentario": "Excelente professor, muito didático",
  "dataAvaliacao": "2025-12-18T21:45:30"
}
```

**Escala de notas**:
- 1 = Ruim (desorganizado, não domina matéria)
- 2 = Insuficiente (dificuldades na didática)
- 3 = Neutro (sem avaliações prévias, score padrão)
- 4 = Bom (ensina bem, dinâmico)
- 5 = Excelente (excepcional, muito recomendado)

---

### 3. Consultar Score de Professor

```bash
GET /api/recomendacoes/professor/Prof. Silva/score?componenteId=5

Resposta (200 OK):
{
  "professorNome": "Prof. Silva",
  "componenteId": 5,
  "score": 4.5,
  "qualidade": "ALTA"
}
```

**Qualidade**:
- `BAIXA` (score < 2.5)
- `MÉDIA` (score 2.5-3.5)
- `ALTA` (score > 3.5)

---

### 4. Gerenciar Pré-requisitos

#### Criar Pré-requisito

```bash
POST /api/prerequisitos
  ?componenteId=6
  &componentePreRequisitoId=5
  &tipo=PREREQUISITO

Resposta (200 OK):
{
  "id": 1,
  "componenteId": 6,
  "componentePreRequisitoId": 5,
  "tipo": "PREREQUISITO"
}
```

**Tipos de pré-requisito**:
- `PREREQUISITO`: Deve ser concluído ANTES (bloqueia recomendação)
- `COREQUISITO`: Pode ser cursado SIMULTANEAMENTE
- `POSREQUISITO`: Pode ser cursado DEPOIS (bloqueador futuro)

#### Listar Pré-requisitos de Uma Disciplina

```bash
GET /api/prerequisitos/componente/6

Resposta (200 OK):
[
  {
    "id": 1,
    "componenteId": 6,
    "componentePreRequisitoId": 5,
    "tipo": "PREREQUISITO"
  }
]
```

---

## Estrutura de Dados

### Usuario Fields Relevantes para Recomendação

```java
@Entity
public class Usuario {
    private Long id;
    private String nome;
    private String matricula;
    
    // Disciplinas que aluno já completou
    @ElementCollection
    private Set<String> disciplinasFeitas; // Códigos como "PROG1", "BD1"
    
    // Professores que quer evitar
    @ElementCollection
    private Set<String> professoresExcluidos; // Nomes de professores
    
    // Limite de disciplinas por semestre
    private Integer limiteMatricula; // Padrão: 8
}
```

### Atualizar Disciplinas Feitas (Admin/Aluno)

```bash
// Simular conclusão de disciplina (requer implementação de endpoint PATCH)
PATCH /api/usuarios/1
{
  "disciplinasFeitas": ["PROG1", "BD1", "CALCULO1"]
}
```

### Adicionar Professor à Lista de Exclusão

```bash
// Simular exclusão de professor (requer implementação de endpoint PATCH)
PATCH /api/usuarios/1
{
  "professoresExcluidos": ["Prof. Ruim", "Prof. Chato"]
}
```

---

## Algoritmo Detalhado

### 1. FILTRAR (RecomendacaoUtil.filtrarTurmas)

Remove turmas que:
- ✗ Aluno já concluiu (em `disciplinasFeitas`)
- ✗ Professor está na exclusão (em `professoresExcluidos`)
- ✗ Não têm vagas (`turma.vagas.totalVagas <= 0`)
- ✗ Não cumprem pré-requisitos

**Resultado**: Lista reduzida de turmas válidas

### 2. ORDENAR (RecomendacaoUtil.ordenarPorEstrategia)

Para cada turma válida:
1. Classifica dificuldade (FACIL < INTERMEDIO < DIFICIL)
2. Calcula score do professor (1-5, padrão 3)
3. Ordena por: dificuldade ASC, score DESC

**Resultado**: Turmas ordenadas por estratégia

### 3. ENCAIXAR (RecomendacaoUtil.encaixarTurmas)

- **Mínimo**: 3 disciplinas
- **Máximo**: 8 disciplinas
- **Regra**: Retorna entre 3-8 (ou menos se não houver suficientes)

**Resultado**: Lista final de recomendações

### 4. CONVERTER

Transforma em `RecomendacaoTurmaDTO` com:
- Turma completa (DTO)
- Dificuldade (string)
- Score do professor (Double)
- Motivo (texto explicativo)
- Posição na lista (1-8)

---

## Performance

### Índices de Banco

```sql
CREATE INDEX idx_avaliacoes_usuario ON avaliacoes_professor(usuario_id);
CREATE INDEX idx_avaliacoes_professor ON avaliacoes_professor(professor_nome);
CREATE INDEX idx_avaliacoes_componente ON avaliacoes_professor(componente_id);
CREATE INDEX idx_prerequisitos_componente ON prerequisitos(componente_id);
CREATE INDEX idx_prerequisitos_pre_componente ON prerequisitos(componente_prerequisito_id);
```

### Otimizações Implementadas

✅ **Sem N+1**: Carrega todas as avaliações + pré-requisitos em queries únicas
✅ **Transacional**: `@Transactional` na service garante consistência
✅ **Lazy Loading**: DTOs carregam apenas dados necessários
❌ **Cache**: Futuro - implementar Redis para ratings

### Melhorias Futuras

1. **Cache de Ratings**: Atualizar cache de professores 1x por semana
2. **Conflito de Horário**: Validar sobreposição de horários
3. **Corequisitos**: Implementar lógica de agrupar disciplinas simultâneas
4. **Machine Learning**: Algoritmo avançado baseado em histórico do aluno

---

## Exemplos de Uso - cURL

### Gerar Recomendações

```bash
curl -X POST http://localhost:9090/api/recomendacoes/gerar/1?metodo=burrinho
```

### Avaliar Professor

```bash
curl -X POST "http://localhost:9090/api/recomendacoes/avaliar-professor?usuarioId=1&professorNome=Prof.%20Silva&componenteId=5&nota=4&comentario=Bom"
```

### Obter Score de Professor

```bash
curl http://localhost:9090/api/recomendacoes/professor/Prof.%20Silva/score?componenteId=5
```

### Criar Pré-requisito

```bash
curl -X POST "http://localhost:9090/api/prerequisitos?componenteId=6&componentePreRequisitoId=5&tipo=PREREQUISITO"
```

---

## Erros Comuns

| Erro | Causa | Solução |
|------|-------|--------|
| 404 User not found | Usuario ID não existe | Verificar ID do usuário |
| 404 Component not found | Componente ID inválido | Verificar ID do componente |
| 400 Nota deve estar entre 1 e 5 | Nota fora do intervalo | Usar nota entre 1-5 |
| 409 Duplicate prerequisite | Pré-requisito já existe | Verificar antes de inserir |

---

## Próximos Passos

1. ✅ Implementar sistema de avaliação de professores
2. ✅ Estruturar pré-requisitos no banco
3. ✅ Criar lógica de filtro/ordenação
4. ⏳ Adicionar validação de conflito de horário
5. ⏳ Implementar algoritmo alternativo "busca" com ML
6. ⏳ Criar dashboard web para visualizar recomendações
