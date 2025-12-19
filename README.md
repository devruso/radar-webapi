# 🎓 RADAR - Sistema de Recomendação de Cursos

![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-6DB33F?style=flat-square&logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16+-336791?style=flat-square&logo=postgresql)
![Maven](https://img.shields.io/badge/Maven-3.9+-C71A36?style=flat-square&logo=apache-maven)
![License](https://img.shields.io/badge/License-CC%20BY--NC--SA%204.0-blue?style=flat-square)

RADAR é um **sistema REST API de recomendação de cursos** desenvolvido para a UFBA (Universidade Federal da Bahia). Utiliza um algoritmo inteligente que considera dificuldade, disponibilidade de professor, pré-requisitos e avaliações para sugerir as melhores disciplinas para cada estudante.

> 🚀 Desenvolvido em **equipe**, mantido com dedicação integral para maximizar qualidade e performance.

---

## ✨ Características Principais

### 🧠 Engine de Recomendação Inteligente
- **Algoritmo 4-passos**: Filtra → Ordena → Encaixa → Retorna recomendações
- **Balanceamento de Dificuldade**: Fácil, Intermediário, Difícil distribuídos equilibradamente
- **Avaliação de Professores**: Sistema 1-5 que influencia recomendações futuras
- **Validação de Pré-requisitos**: Garante que estudante tem preparação necessária
- **Otimização de Horários**: Encaixa 3-8 disciplinas sem conflito de horário

### 📊 Gestão Completa de Cursos
- Estrutura curricular por programa acadêmico
- Guias de matrícula personalizadas
- Horários e vagas em tempo real
- Histórico de disciplinas completadas

### 🎯 Sistema de Avaliação
- Avaliação de professores por disciplina (1-5 estrelas)
- Cálculo automático de score médio
- Exclusão de professores com baixa avaliação
- Rastreamento completo de feedback estudantil

### 🔗 Gerenciamento de Pré-requisitos
- Suporte a PREREQUISITO, COREQUISITO, POSREQUISITO
- Validação automática de dependências
- Detecção de bloqueios futuros
- Cadeia de desbloqueio de disciplinas

---

## 🚀 Quick Start

### Pré-requisitos
- **Java 17+**
- **Docker & Docker Compose** (ou PostgreSQL 16 instalado)
- **Maven 3.9+**

### Instalação & Execução

#### Option 1: Com Docker (Recomendado)
```powershell
# 1. Clone o repositório
git clone https://github.com/devruso/radar-webapi.git
cd radar-webapi

# 2. Inicie PostgreSQL + PgAdmin
docker-compose up -d

# 3. Build da aplicação
.\mvnw.cmd clean package -DskipTests

# 4. Execute a aplicação
.\mvnw.cmd spring-boot:run
```

#### Option 2: Script Automatizado (Windows)
```powershell
.\setup-and-run.ps1
```

#### Option 3: PostgreSQL Local
1. Crie database: `radar`
2. Crie usuário: `radar` / senha: `radar123`
3. Atualize `application.properties` se necessário
4. Siga os passos acima

### 🌐 URLs Após Inicialização

| Recurso | URL |
|---------|-----|
| **API REST** | http://localhost:9090 |
| **Swagger UI** | http://localhost:9090/swagger-ui/index.html |
| **OpenAPI Docs** | http://localhost:9090/v3/api-docs |
| **PgAdmin** | http://localhost:5050 |

### 🔐 Credenciais Padrão

**PostgreSQL:**
- Host: `localhost:5432`
- User: `radar`
- Password: `radar123`
- Database: `radar`

**PgAdmin:**
- Email: `admin@radar.com`
- Password: `admin123`

---

## 📚 API Endpoints

### Recomendações 🎯
```http
POST /api/recomendacoes/gerar/{usuarioId}?metodo=burrinho
GET /api/recomendacoes/professor/{nome}/avaliacoes
GET /api/recomendacoes/professor/{nome}/score?componenteId=1
POST /api/recomendacoes/avaliar-professor
```

### Cursos 📖
```http
GET /api/cursos
GET /api/cursos/{id}
```

### Turmas (Classes) 🕐
```http
GET /api/turmas
GET /api/turmas/{id}
GET /api/turmas/curso/{cursoId}
```

### Usuários 👤
```http
GET /api/usuarios
GET /api/usuarios/{id}
```

### Avaliações de Professores ⭐
```http
GET /api/avaliacoes-professor
GET /api/avaliacoes-professor/{id}
GET /api/avaliacoes-professor/usuario/{usuarioId}
POST /api/avaliacoes-professor
DELETE /api/avaliacoes-professor/{id}
```

### Pré-requisitos 🔗
```http
GET /api/prerequisitos
GET /api/prerequisitos/componente/{componenteId}
GET /api/prerequisitos/componente/{componenteId}/tipo/{tipo}
POST /api/prerequisitos
DELETE /api/prerequisitos/{id}
```

📖 **Documentação Completa**: Acesse Swagger UI após inicializar a aplicação

---

## 🏗️ Arquitetura

### Estrutura de Pastas
```
src/main/java/com/jangada/RADAR/
├── controllers/          # REST endpoints
├── services/            # Lógica de negócio
├── repositories/        # Acesso a dados (Spring Data JPA)
├── models/
│   ├── entities/        # Entidades JPA
│   └── dtos/            # Data Transfer Objects
├── mappers/             # Conversão Entity ↔ DTO
├── config/              # Configuração Spring
├── exceptions/          # Tratamento de erros
└── utils/               # Utilitários (algoritmo de recomendação)
```

### Stack Tecnológico
- **Framework**: Spring Boot 4.0
- **Linguagem**: Java 17
- **Database**: PostgreSQL 16 (+ Flyway migrations)
- **API Docs**: Springdoc OpenAPI 2.7.0 (Swagger UI)
- **Build**: Maven 3.9+
- **ORM**: Hibernate 7.1.8 (Spring Data JPA)
- **Boilerplate**: Lombok
- **Segurança**: Spring Security + BCrypt
- **Containerização**: Docker + Docker Compose

---

## 🧮 Algoritmo de Recomendação

O engine segue um pipeline de **4 passos**:

### 1️⃣ **FILTRO** - Remove inviáveis
- ❌ Disciplinas já completadas
- ❌ Com professores na lista de exclusão do estudante
- ❌ Sem vagas disponíveis
- ❌ Com pré-requisitos não atendidos

### 2️⃣ **ORDENAÇÃO** - Classifica por estratégia
```
Prioridade:
1. Dificuldade (FÁCIL < INTERMEDIÁRIO < DIFÍCIL)
2. Score do Professor (5★ antes de 1★)
```

### 3️⃣ **ENCAIXE** - Seleciona 3-8 disciplinas
- Mínimo: 3 disciplinas
- Máximo: 8 disciplinas
- Sem conflito de horário

### 4️⃣ **CONVERSÃO** - Formata resposta
- Inclui motivo de recomendação
- Score do professor
- Nível de dificuldade
- Posição na recomendação

**Resultado**: Lista de 3-8 disciplinas otimizadas e justificadas

---

## 📖 Documentação Adicional

- [COMANDOS.md](COMANDOS.md) - Comandos Maven, Docker e utilitários
- [QUICK_START.md](QUICK_START.md) - Exemplos práticos de uso
- [RECOMENDACOES.md](RECOMENDACOES.md) - Detalhes do engine de recomendação
- [IMPLEMENTACAO_RECOMENDACOES.md](IMPLEMENTACAO_RECOMENDACOES.md) - Guia técnico de implementação
- [README-DATABASE.md](README-DATABASE.md) - Setup e schema do banco de dados
- [INTEGRACAO_FRONTEND.md](INTEGRACAO_FRONTEND.md) - Guia para integração com Next.js

---

## 🔌 Integração com Frontend (Next.js)

A API foi desenvolvida com foco em integração frontend-agnostica:

✅ **CORS Habilitado** - Acesso de qualquer origin durante desenvolvimento
✅ **Respostas JSON Padronizadas** - Fácil consumo em JavaScript/TypeScript
✅ **Swagger UI** - Playground interativo para testar endpoints
✅ **Sem Autenticação Complexa** - HTTP Basic para começar

📋 **Veja [INTEGRACAO_FRONTEND.md](INTEGRACAO_FRONTEND.md)** para:
- Exemplo de hook `useRecomendacoes()`
- Tipos TypeScript pré-gerados
- Tratamento de erros
- Cache com React Query
- Integração com Tailwind + shadcn

---

## 🧪 Testes

### Executar Testes
```bash
.\mvnw.cmd test
```

### Com Coverage
```bash
.\mvnw.cmd test jacoco:report
```

---

## 🛠️ Desenvolvimento

### Setup Local
```powershell
# 1. Clone
git clone https://github.com/devruso/radar-webapi.git
cd radar-webapi

# 2. Inicie Docker
docker-compose up -d

# 3. Build
.\mvnw.cmd clean package -DskipTests

# 4. Run
.\mvnw.cmd spring-boot:run
```

### Estrutura de Commits
Seguimos [Conventional Commits](https://www.conventionalcommits.org/):
```
feat(scope): descrição curta
fix(scope): descrição curta
docs(scope): descrição curta
style(scope): descrição curta
test(scope): descrição curta
chore(scope): descrição curta
```

### Padrões de Código
- ✅ Mappers estáticos para Entity ↔ DTO
- ✅ Constructor injection (não `@Autowired`)
- ✅ `@Transactional` em serviços
- ✅ Swagger annotations em controllers
- ✅ Tratamento centralizado de exceções

---

## 📊 Estrutura do Banco de Dados

### Entidades Principais
- `Usuario` - Estudantes
- `Curso` - Programas acadêmicos
- `ComponenteCurricular` - Disciplinas
- `Turma` - Ofertas de disciplinas (horários + professor)
- `EstruturaCurso` - Currículo do programa
- `GuiaMatricula` - Guia de matrícula
- `AvaliacaoProfessor` - Ratings de professores
- `PreRequisito` - Dependências entre disciplinas

### Migrations (Flyway)
- `V1` - Dados base
- `V2` - Estrutura SI UFBA
- `V3` - Avaliações e pré-requisitos
- `V4` - Dados seed de pré-requisitos

---

## 📝 Licença

Este projeto está protegido sob a licença **Creative Commons Attribution-NonCommercial-ShareAlike 4.0** (CC BY-NC-SA 4.0).

Você é livre para:
- ✅ Compartilhar — copiar e redistribuir o material
- ✅ Adaptar — modificar, remixar e desenvolver o material

**Sob as condições:**
- 🏷️ Atribuição — Dar crédito apropriado aos autores
- 🚫 Não Comercial — Não usar para fins comerciais
- 🔄 Compartilhar Igual — Distribuir sob a mesma licença

Veja [LICENSE](LICENSE) para detalhes completos.

---

## 👥 Equipe

Desenvolvido com dedicação para a comunidade acadêmica da **UFBA**.

- 🎓 Desenvolvido para: **Universidade Federal da Bahia**
- 📚 Aplicação: **Recomendação de Cursos para Estudantes**
- 🚀 Status: **Em desenvolvimento contínuo**

---

## 💡 Ideias Futuras

- [ ] Cache distribuído para ratings de professores
- [ ] Detecção avançada de conflito de horários
- [ ] Machine Learning para personalização
- [ ] Integração com sistema acadêmico UFBA oficial
- [ ] App mobile (React Native)
- [ ] Analytics dashboard para coordenadores
- [ ] Notificações de alterações de horário
- [ ] Exportação de planos de matrícula em PDF

---

## 🤝 Contribuindo

Este projeto foi desenvolvido em equipe para a UFBA. Contribuições são bem-vindas!

1. Faça um Fork
2. Crie uma branch (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'feat: add AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

---

## 📞 Suporte

- 📧 Email: [contato da equipe]
- 🐛 Issues: [GitHub Issues](https://github.com/devruso/radar-webapi/issues)
- 💬 Discussões: [GitHub Discussions](https://github.com/devruso/radar-webapi/discussions)

---

## 📜 Changelog

### v0.0.1 (18/12/2025)
- ✨ Engine de recomendação com algoritmo 4-passos
- ⭐ Sistema de avaliação de professores (1-5)
- 🔗 Gerenciamento de pré-requisitos
- 📚 15+ endpoints REST documentados
- 🐳 Docker setup completo
- 📖 Documentação abrangente

---

<div align="center">

**Desenvolvido com ❤️ para a comunidade acadêmica da UFBA**

[⬆ Voltar ao topo](#-radar---sistema-de-recomendação-de-cursos)

</div>
