# Quick Start - Exemplos Práticos da API de Recomendações

## 🚀 5 Minutos para Começar

### Setup Inicial

```bash
# 1. Iniciar banco de dados
docker-compose up -d

# 2. Compilar projeto
.\mvnw.cmd clean compile

# 3. Executar aplicação
.\mvnw.cmd spring-boot:run

# 4. Abrir Swagger UI
# http://localhost:9090/swagger-ui/index.html
```

---

## 💡 Cenários de Uso Prático

### Cenário 1: Novo Aluno - Primeira Recomendação

**Contexto**:
- Aluno João (ID: 1) no 1º período
- Nenhuma disciplina concluída ainda
- Quer evitar Prof. Carlos

**Passos**:

1. **Adicionar Prof. à lista de exclusão** (via banco ou PATCH futuro)
```sql
UPDATE usuarios SET professores_excluidos = ARRAY['Prof. Carlos'] WHERE id = 1;
```

2. **Gerar recomendações**
```bash
curl -X POST http://localhost:9090/api/recomendacoes/gerar/1?metodo=burrinho
```

**Resultado esperado**:
```json
[
  {
    "turma": {
      "id": 1,
      "professor": "Prof. Silva",
      "numeroTurma": "01",
      "local": "Sala 101",
      "componenteCurricular": {
        "codigo": "PROG1",
        "nome": "Programação I",
        "nivel": 1
      }
    },
    "dificuldade": "FACIL",
    "scoreProfessor": 3.0,
    "motivo": "Disciplina Programação I (dificuldade FACIL) com prof. Prof. Silva (score: 3.0)",
    "posicao": 1
  },
  {
    "turma": { ... },
    "dificuldade": "FACIL",
    "scoreProfessor": 3.0,
    "motivo": "Disciplina Matemática Discreta (dificuldade FACIL) com prof. Prof. João (score: 3.0)",
    "posicao": 2
  }
  // ... até 8 disciplinas
]
```

---

### Cenário 2: Aluno com Histórico - Usando Avaliações

**Contexto**:
- Aluno Maria (ID: 2)
- Já concluiu: PROG1, CALCULO1
- Avaliou Prof. Silva (5⭐) em PROG1

**Passos**:

1. **Verificar avaliações que fez**
```bash
curl http://localhost:9090/api/avaliacoes-professor/usuario/2
```

**Resultado**:
```json
[
  {
    "id": 1,
    "usuarioId": 2,
    "professorNome": "Prof. Silva",
    "componenteId": 5,
    "nota": 5,
    "comentario": "Excelente professor, muito didático!",
    "dataAvaliacao": "2025-12-15T19:30:00"
  }
]
```

2. **Gerar recomendações (vai priorizar Prof. Silva novamente)**
```bash
curl -X POST http://localhost:9090/api/recomendacoes/gerar/2?metodo=burrinho
```

**Resultado**:
- Se Prof. Silva tem turma de PROG2: vai aparecer na posição 1
- Score será 5.0⭐ em vez de 3.0 (padrão)
- Motivo dirá "score: 5.0" em destaque

---

### Cenário 3: Pré-requisitos em Ação

**Contexto**:
- Aluno Pedro (ID: 3)
- Concluiu: PROG1
- Tenta fazer: PROG2 (requer PROG1), PROG3 (requer PROG2)

**Passos**:

1. **Verificar pré-requisitos de PROG3**
```bash
curl http://localhost:9090/api/prerequisitos/componente/7/tipo/PREREQUISITO
```

**Resultado**:
```json
[
  {
    "id": 1,
    "componenteId": 7,  // PROG3
    "componentePreRequisitoId": 6,  // PROG2
    "tipo": "PREREQUISITO"
  }
]
```

2. **Gerar recomendações**
```bash
curl -X POST http://localhost:9090/api/recomendacoes/gerar/3?metodo=burrinho
```

**Resultado esperado**:
- ✅ PROG2 aparece (tem PROG1 que é pré-req)
- ❌ PROG3 NÃO aparece (falta PROG2)

---

### Cenário 4: Avaliar Professor Após Semestre

**Contexto**:
- Aluno concluiu PROG2 com Prof. João
- Quer avaliar a experiência

**Passo**:

```bash
curl -X POST "http://localhost:9090/api/recomendacoes/avaliar-professor?usuarioId=3&professorNome=Prof.%20João&componenteId=6&nota=4&comentario=Bom%20professor,%20mas%20exigente"
```

**Resultado**:
```json
{
  "id": 42,
  "usuarioId": 3,
  "professorNome": "Prof. João",
  "componenteId": 6,
  "nota": 4,
  "comentario": "Bom professor, mas exigente",
  "dataAvaliacao": "2025-12-18T21:45:30"
}
```

**Impacto futuro**:
- Score de Prof. João em PROG2 sobe de 3.0 para 4.0 (se ele teve 1 avaliação)
- Próximas recomendações vão priorizar Prof. João

---

### Cenário 5: Gerenciar Pré-requisitos (Admin)

**Contexto**:
- Admin quer criar novo vínculo: BD2 requer BD1

**Passo**:

```bash
curl -X POST "http://localhost:9090/api/prerequisitos?componenteId=10&componentePreRequisitoId=9&tipo=PREREQUISITO"
```

**Resultado**:
```json
{
  "id": 2,
  "componenteId": 10,
  "componentePreRequisitoId": 9,
  "tipo": "PREREQUISITO"
}
```

**Verificar**:
```bash
curl http://localhost:9090/api/prerequisitos/componente/10
```

---

## 🔄 Fluxo Completo: Do Zero até Recomendação

### Dia 1: Aluno novo se registra

```
1. Criar usuário: POST /api/usuarios
   {
     "nome": "João Silva",
     "matricula": "202501001",
     "email": "joao@ufba.br",
     "disciplinasFeitas": [],
     "professoresExcluidos": []
   }
   → Retorna usuarioId = 10

2. (Opcional) Adicionar professor à exclusão
   UPDATE usuarios SET professores_excluidos = ARRAY['Prof. Chato'] WHERE id = 10;
```

### Dia 2: Pedir recomendação

```
3. Gerar recomendações
   POST /api/recomendacoes/gerar/10?metodo=burrinho
   → Retorna 3-8 disciplinas ordenadas

4. Aluno escolhe disciplinas e se matricula
   (Fora do escopo da API, gerenciado por outro sistema)
```

### Fim do semestre: Avaliar professores

```
5. Avaliar Prof. Silva em PROG1
   POST /api/recomendacoes/avaliar-professor
   ?usuarioId=10
   &professorNome=Prof. Silva
   &componenteId=5
   &nota=5
   &comentario=Excelente!
   → Avaliação salva

6. Avaliar Prof. João em CALCULO1
   POST /api/recomendacoes/avaliar-professor
   ?usuarioId=10
   &professorNome=Prof. João
   &componenteId=6
   &nota=2
   &comentario=Desorganizado
   → Avaliação salva

7. (Sistema admin) Marcar PROG1 e CALCULO1 como concluídas
   UPDATE usuarios SET disciplinas_feitas = ARRAY['PROG1', 'CALCULO1'] WHERE id = 10;
```

### Próximo semestre: Recomendação melhorada

```
8. Gerar recomendações novamente
   POST /api/recomendacoes/gerar/10?metodo=burrinho
   
   Resultado:
   - PROG1 e CALCULO1 removidas (já concluídas)
   - PROG2 priorizada se Prof. Silva ensinar (score 5.0⭐)
   - CALCULO2 deprioritizada se Prof. João ensinar (score 2.0⭐ = BAIXA)
   - Novas disciplinas fáceis aparecem na frente
```

---

## 🐛 Troubleshooting

### Problema: "Recomendações retorna lista vazia"

**Causas possíveis**:
1. ❌ Aluno já fez todas as disciplinas
   ```sql
   SELECT disciplinas_feitas FROM usuarios WHERE id = 1;
   ```

2. ❌ Todas as turmas têm professor excluído
   ```sql
   SELECT professores_excluidos FROM usuarios WHERE id = 1;
   ```

3. ❌ Sem turmas com vagas
   ```sql
   SELECT * FROM vagas WHERE total_vagas > 0;
   ```

4. ❌ Pré-requisitos não atendidos
   ```sql
   SELECT * FROM prerequisitos p
   WHERE p.componente_id NOT IN (
     SELECT id FROM componentes_curriculares
     WHERE codigo IN (SELECT disciplinas_feitas FROM usuarios WHERE id = 1)
   );
   ```

**Solução**: Verificar dados no banco com PgAdmin (http://localhost:5050)

---

### Problema: "Nota deve estar entre 1 e 5"

**Causa**: Valor fora do intervalo

**Solução**: 
```bash
# ❌ ERRADO
curl -X POST "...&nota=10"

# ✅ CERTO
curl -X POST "...&nota=5"
```

---

### Problema: "Usuário não encontrado"

**Causa**: ID do usuário não existe

**Solução**:
```bash
# Listar todos os usuários
curl http://localhost:9090/api/usuarios

# Usar ID existente
curl -X POST http://localhost:9090/api/recomendacoes/gerar/1
```

---

### Problema: "Componente não encontrado"

**Causa**: ID do componente inválido

**Solução**:
```bash
# Listar todos os componentes
curl http://localhost:9090/api/componentes-curriculares

# Usar ID correto
curl -X POST "...&componenteId=5"
```

---

## 📊 Queries Úteis - Análise de Dados

### Ver todas as avaliações

```sql
SELECT 
  ap.id,
  u.nome AS aluno,
  ap.professor_nome,
  cc.nome AS disciplina,
  ap.nota,
  ap.comentario,
  ap.data_avaliacao
FROM avaliacoes_professor ap
JOIN usuarios u ON ap.usuario_id = u.id
JOIN componentes_curriculares cc ON ap.componente_id = cc.id
ORDER BY ap.data_avaliacao DESC;
```

### Score médio dos professores

```sql
SELECT 
  professor_nome,
  COUNT(*) AS total_avaliacoes,
  AVG(nota) AS score_medio,
  CASE 
    WHEN AVG(nota) < 2.5 THEN 'BAIXA'
    WHEN AVG(nota) < 3.5 THEN 'MÉDIA'
    ELSE 'ALTA'
  END AS qualidade
FROM avaliacoes_professor
GROUP BY professor_nome
ORDER BY score_medio DESC;
```

### Pré-requisitos por disciplina

```sql
SELECT 
  c1.nome AS disciplina,
  c2.nome AS prerequisito,
  p.tipo
FROM prerequisitos p
JOIN componentes_curriculares c1 ON p.componente_id = c1.id
JOIN componentes_curriculares c2 ON p.componente_prerequisito_id = c2.id
ORDER BY c1.nome, p.tipo;
```

### Alunos que não completaram recomendações

```sql
SELECT 
  u.nome,
  COUNT(DISTINCT t.id) AS turmas_recomendadas,
  COUNT(DISTINCT uts.turma_id) AS turmas_selecionadas
FROM usuarios u
LEFT JOIN turmas t ON 1=1
LEFT JOIN usuario_turmas_selecionadas uts ON u.id = uts.usuario_id
GROUP BY u.id
HAVING COUNT(DISTINCT uts.turma_id) = 0;
```

---

## 🎓 Entendendo a Dificuldade

### Classificação

```
Nível 1-2   → FACIL      (ex: Introdução à Programação)
Nível 3-4   → INTERMEDIO (ex: Banco de Dados)
Nível 5+    → DIFICIL    (ex: Compiladores, IA Avançada)
```

### Regra de Recomendação

```
Primeiro: Muitas disciplinas FACIL (build confidence)
Depois:   Mix de INTERMEDIO com uma FACIL
Evitar:   Muitas DIFICIL no mesmo semestre
```

---

## 📈 Métricas de Sucesso

### KPIs para Monitorar

1. **Taxa de cobertura**: % de recomendações seguidas
2. **Score médio**: Avaliação média dos professores
3. **Taxa de pré-requisito**: % de alunos que cumprem
4. **Tempo médio de resposta**: Deve ser < 200ms

---

**Última atualização**: 2025-12-18
