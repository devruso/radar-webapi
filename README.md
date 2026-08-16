# RADAR API

API do recomendador de grades da UFBA. O backend concentra a regra de negócio e a
persistência do RADAR; Ementas e SIGAA são origens externas somente leitura.

## Arquitetura atual

```text
Ementas local + publicado ──> catálogo/contextos ──┐
                                                   v
SIGAA público ──> ofertas-sync ──> API RADAR ──> PostgreSQL 16
                                      ^
                                      └── frontend Next.js
```

- Java 17, Spring Boot 4.1, Spring Data JPA e Flyway;
- PostgreSQL como único banco do RADAR;
- JWT HMAC e autorização por proprietário dos dados;
- pool Hikari limitado e Open Session in View desativado;
- catálogo Ementas mesclado por código, sem exclusão por fonte parcial;
- ofertas periódicas idempotentes, com desativação em vez de apagamento;
- recomendação determinística por busca ou estratégia gulosa;
- simulações persistidas com controle otimista de concorrência.

## Executar com Docker

```bash
cp .env.example .env
docker compose up -d --build postgres api
docker compose ps
```

Endpoints úteis:

- API: `http://localhost:9090/api`
- health: `http://localhost:9090/actuator/health`
- OpenAPI: `http://localhost:9090/v3/api-docs`
- Swagger: `http://localhost:9090/swagger-ui.html`

O pgAdmin é opcional:

```bash
docker compose --profile tools up -d pgadmin
```

## Configuração indispensável

Veja `.env.example`. Em deploy, defina obrigatoriamente:

- `POSTGRES_PASSWORD`;
- `RADAR_JWT_SECRET` com ao menos 32 bytes aleatórios;
- `RADAR_CATALOG_SYNC_KEY` e `RADAR_OFERTAS_IMPORT_KEY`;
- `RADAR_ALLOWED_ORIGINS` com os domínios reais do frontend;
- credenciais de consulta do Ementas, quando a sincronização for usada.

`EMENTAS_API_BASE_URLS` aceita várias URLs separadas por vírgula. O cliente consulta essas
fontes via HTTP e persiste apenas uma cópia normalizada no RADAR; nunca executa escrita no
Ementas.

## Fluxo autenticado

Cadastro, login e modo de teste retornam:

```json
{
  "accessToken": "...",
  "tokenType": "Bearer",
  "expiresIn": 43200,
  "usuario": { "id": 1 }
}
```

Envie o token em `Authorization: Bearer ...`. Rotas de catálogo, cursos, componentes,
horários e turmas são públicas. Perfil, preferências, recomendações e simulações exigem o
token do próprio usuário. Importações continuam protegidas por chaves operacionais próprias.

## Ofertas e simulações

O endpoint `POST /api/ofertas/importar` recebe um snapshot com `source`, `periodoLetivo` e
turmas identificadas por chave externa. Repetir o lote atualiza os registros; turmas ausentes
do snapshot ficam inativas. Horários inválidos ou ausentes não entram na recomendação.

Recomendações:

```http
POST /api/recomendacoes/gerar/{usuarioId}?metodo=busca
Authorization: Bearer <token>
```

`busca` explora combinações sem conflito; `guloso` é a alternativa mais barata. Ambos usam
consultas em lote e retornam no máximo uma turma por componente.

### Fonte de verdade da prioridade

A legenda de prioridades de matrícula do SIGAA/UFBA é a regra canônica do RADAR:

1. obrigatória cujo semestre curricular coincide com o semestre acadêmico;
2. estudante com status `FORMANDO` (salvo o caso I, que tem precedência);
3. obrigatória atrasada ou componente optativo;
4. obrigatória de semestre futuro;
5. componente livre ou com equivalência curricular explícita.

O semestre acadêmico é `perfilInicial + periodosRegularesCursados`. O segundo campo já
deve excluir períodos de trancamento, suspensão e mobilidade. Uma obrigatória sem semestre
curricular válido não é classificada por aproximação e fica fora da recomendação.

O CR desempata estudantes concorrendo dentro da mesma prioridade no processo de matrícula
do SIGAA. Como uma geração de grade ordena disciplinas para apenas um estudante, seu CR é
constante e não pode desempatar essas disciplinas. Depois da prioridade institucional, o
RADAR usa score de professor e identificadores estáveis como critérios próprios. A busca de
grade nunca troca uma quantidade de itens de prioridade superior por itens de prioridade
inferior.

Optativas vêm dos contextos curriculares não obrigatórios do Ementas. Ofertas descobertas
sem contexto não são tratadas como livres; a prioridade V exige equivalência importada e
registrada no RADAR.

## Testes

```bash
./mvnw test
```

A suíte cobre fallback do Ementas, otimizador, importação idempotente, recomendação,
persistência de simulações, JWT, isolamento entre usuários, CORS e erros de autenticação.
Migrações devem ser validadas também em PostgreSQL real antes de cada deploy.
