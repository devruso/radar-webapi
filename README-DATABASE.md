# RADAR - Sistema de Recomendação de Disciplinas

## Configuração do Banco de Dados PostgreSQL

### Iniciando o PostgreSQL e PgAdmin com Docker

```powershell
docker-compose up -d
```

### Credenciais do Banco

**PostgreSQL:**
- Host: localhost
- Porta: 5432
- Database: radar
- Usuário: radar
- Senha: radar123

**PgAdmin:**
- URL: http://localhost:5050
- Email: admin@radar.com
- Senha: admin123

### Configurando Conexão no PgAdmin

1. Acesse http://localhost:5050
2. Login com `admin@radar.com` / `admin123`
3. Clique em "Add New Server"
4. Aba "General": Name = `RADAR Local`
5. Aba "Connection":
   - Host: `postgres` (ou `host.docker.internal` se não funcionar)
   - Port: `5432`
   - Database: `radar`
   - Username: `radar`
   - Password: `radar123`
6. Salvar

### Rodando a Aplicação

```powershell
.\mvnw.cmd spring-boot:run
```

A aplicação iniciará na porta 9090:
- **Swagger UI:** http://localhost:9090/swagger-ui/index.html
- **API Docs:** http://localhost:9090/v3/api-docs/v1

### Migrations

As migrations Flyway estão em `src/main/resources/db/migration/`:
- V1: Seeds base (SI e CC, componentes, turmas)
- V2: Estrutura UFBA SI com optativas

Elas rodarão automaticamente no startup da aplicação.

## Parando os Containers

```powershell
docker-compose down
```

Para remover volumes (apagar dados):
```powershell
docker-compose down -v
```
