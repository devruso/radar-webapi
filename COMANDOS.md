# Comandos Essenciais - RADAR

## 🐳 Docker (PostgreSQL + PgAdmin)

```powershell
# Iniciar containers em background
docker-compose up -d

# Parar containers
docker-compose down

# Ver logs
docker-compose logs -f
```

**Acessos:**
- **PostgreSQL**: localhost:5432 (usuário: `radar` / senha: `radar123`)
- **PgAdmin**: http://localhost:5050 (email: `admin@radar.com` / senha: `admin123`)

---

## 🔨 Build & Run

```powershell
# Build completo (compilar + gerar JAR)
.\mvnw.cmd clean package -DskipTests

# Apenas rodar aplicação (sem rebuild)
.\mvnw.cmd spring-boot:run

# Build com testes
.\mvnw.cmd clean package
```

---

## 🚀 Iniciar Tudo (Sequência Completa)

```powershell
# 1. Iniciar Docker
docker-compose up -d

# 2. Build da aplicação
.\mvnw.cmd clean package -DskipTests

# 3. Rodar aplicação
.\mvnw.cmd spring-boot:run
```

**Ou use o script automatizado:**
```powershell
.\setup-and-run.ps1
```

---

## 🌐 URLs Importantes

- **API**: http://localhost:9090
- **Swagger UI**: http://localhost:9090/swagger-ui/index.html
- **API Docs (JSON)**: http://localhost:9090/v3/api-docs
- **PgAdmin**: http://localhost:5050

---

## 🛠️ Comandos Úteis

```powershell
# Limpar build anterior
.\mvnw.cmd clean

# Ver versão do Maven
.\mvnw.cmd --version

# Compilar sem executar
.\mvnw.cmd compile

# Executar apenas testes
.\mvnw.cmd test

# Verificar porta em uso (Windows)
netstat -ano | findstr :9090

# Matar processo na porta (substitua PID)
taskkill /F /PID <numero>
```

---

## 📦 Gerar JAR executável

```powershell
# Gerar JAR em target/
.\mvnw.cmd clean package -DskipTests

# Executar JAR diretamente
java -jar target/RADAR-0.0.1-SNAPSHOT.jar
```

---

## 🔄 Migrations (Flyway)

As migrations rodam automaticamente no startup em:
- `src/main/resources/db/migration/V1__seed_base_data.sql`
- `src/main/resources/db/migration/V2__ufba_si_structure.sql`
- `src/main/resources/db/migration/V3__add_avaliacoes_e_prerequisitos.sql`
- `src/main/resources/db/migration/V4__seed_prerequisitos.sql`

Para forçar re-execução, limpe a tabela `flyway_schema_history` no banco.
