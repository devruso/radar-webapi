# RADAR Web API - Copilot Instructions

## Project Overview

**RADAR** is a Spring Boot 4.0 course recommendation system for UFBA (Universidade Federal da Bahia). It provides REST APIs for managing courses, class schedules, enrollment guides, and student recommendations.

- **Tech Stack**: Java 17, Spring Boot 4.0, PostgreSQL, Maven, Flyway migrations
- **Port**: 9090
- **Database**: PostgreSQL (via Docker)
- **API Docs**: Swagger UI at http://localhost:9090/swagger-ui/index.html

## Architecture & Data Flow

### Layered Structure

**Controllers** → **Mappers** → **DTOs** ↔ **Services** ↔ **Repositories** ↔ **Entities** (Database)

```
com.jangada.RADAR/
├── controllers/        # REST endpoints (@RestController, @RequestMapping)
├── services/          # Business logic (currently minimal)
├── repositories/      # Data access (Spring Data JPA interfaces)
├── models/
│   ├── entities/      # JPA entities with @Entity, Lombok @Data
│   └── dtos/          # Data transfer objects (matching entity names + DTO suffix)
├── mappers/           # Static mapper methods (Entity ↔ DTO conversion)
├── config/            # Spring configuration
├── exceptions/        # GlobalExceptionHandler for centralized error handling
└── utils/             # Utility functions
```

### Core Entities & Relationships

The system manages academic structures:

- **Curso** (Course) - Contains links to EstruturaCurso and GuiaMatricula
- **Turma** (Class) - Scheduled offerings of courses
- **ComponenteCurricular** (Curriculum Component) - Course subjects
- **EstruturaCurso** (Course Structure) - Defines course curriculum
- **GuiaMatricula** (Enrollment Guide) - Guidance for student enrollments
- **Horario** (Schedule) - Class time slots
- **Vagas** (Vacancies) - Available seats per class
- **Usuario** (User) - Students and staff
- **AvaliacaoProfessor** (Professor Rating) - Student ratings 1-5 per professor/component
- **PreRequisito** (Prerequisite Link) - Dependency between components (PREREQUISITO/COREQUISITO/POSREQUISITO)

**Key Pattern**: All entities use Lombok `@Data`, `@Builder`, and constructors. All have corresponding DTO classes using static mappers.

## Critical Workflows

### Local Development Setup

```powershell
# Start PostgreSQL + PgAdmin
docker-compose up -d

# Run application on port 9090
.\mvnw.cmd spring-boot:run

# Or use setup script
.\setup-and-run.ps1
```

**Database Credentials** (from docker-compose.yml):
- Host: localhost | Port: 5432
- User: `radar` | Password: `radar123`
- Database: `radar`

### Database Migrations

Flyway manages schema via SQL files in `src/main/resources/db/migration/`:

- `V1__seed_base_data.sql` - Initial seed data
- `V2__ufba_si_structure.sql` - UFBA SI program structure

Migration runs automatically on startup (enabled in application.properties).

### Build & Test

```bash
# Build with Maven
mvn clean package

# Run tests (currently test folder exists but content minimal)
mvn test
```

## Recommendation Engine Logic (Critical)

### "Dumb" Algorithm Overview

The recommendation system filters, sorts, and fits courses into a student's schedule using 4 steps:

```
1. FILTER    → Remove: already completed, excluded professors, no vacancies, unmet prerequisites
2. ORDER     → By difficulty (EASY < INTERMEDIATE < HARD), then by professor rating (5★ first)
3. FIT       → Select 3-8 courses that fit within capacity limits
4. CONVERT   → Transform to DTOs with reasoning explanations
```

**File**: [RecomendacaoUtil.java](src/main/java/com/jangada/RADAR/utils/RecomendacaoUtil.java)

### Difficulty Levels

Classified by `componente.nivel`:
- **FACIL**: nivel ≤ 2
- **INTERMEDIO**: nivel 3-4
- **DIFICIL**: nivel ≥ 5

### Professor Rating System

- **Scale**: 1-5 (1=poor, 5=excellent)
- **Neutral Score**: 3.0 (if no evaluations exist for a professor)
- **Stored in**: `avaliacoes_professor` table (unique per student/professor/discipline combo)
- **Updated on**: Student completion of semester
- **Integration**: Scores filter out low-rated professors and sort by highest rated

### Prerequisite Chain

Requirements are stored in `prerequisitos` table linking `ComponenteCurricular` entities:
- **PREREQUISITO**: Must complete before taking course
- **COREQUISITO**: Can take simultaneously
- **POSREQUISITO**: Can take after (future blocker detection)

**Validation**: [RecomendacaoUtil.verificarPreRequisitos()](src/main/java/com/jangada/RADAR/utils/RecomendacaoUtil.java#L88)

### Performance Notes

- **No N+1 queries**: Batch fetch prerequisites and ratings
- **Indexes**: On `avaliacoes_professor(usuario_id, professor_nome)` and `prerequisitos(componente_id)`
- **Min/Max disciplines**: Hard-coded 3 minimum, 8 maximum per cycle
- **Future optimization**: Cache professor ratings (refresh weekly), denormalize frequently accessed prereq chains

## Key Conventions

### Mapping Pattern (Critical)

**Always use static mapper methods** for Entity ↔ DTO conversion:

```java
// ✅ DO THIS
CursoDTO dto = CursoMapper.toDto(cursoEntity);

// ❌ NOT THIS (manual mapping)
```

See [CursoMapper.java](src/main/java/com/jangada/RADAR/mappers/CursoMapper.java) - handles null checks and nested relationship IDs (e.g., estruturaId, guiaId).

### Error Handling

All exceptions handled by [GlobalExceptionHandler.java](src/main/java/com/jangada/RADAR/exceptions/GlobalExceptionHandler.java):

- `MethodArgumentNotValidException` → BAD_REQUEST with field errors
- `ResourceNotFoundException` → 404 with custom message
- Controllers throw `new ResourceNotFoundException("Entity type with ID " + id + " not found")`

### Security Configuration

[SecurityConfig.java](src/main/java/com/jangada/RADAR/config/SecurityConfig.java):

- CSRF disabled (stateless API)
- `/api/**`, `/swagger-ui/**`, `/v3/api-docs/**` → permitAll()
- All other requests require authentication
- BCryptPasswordEncoder for password hashing

### DTO vs Entity Naming

- Entities: `Curso.java` in `models/entities/`
- DTOs: `CursoDTO.java` in `models/dtos/`
- **Always add DTO suffix** to distinguish from entities

### REST Conventions

Controllers use:
- `@RestController` with `@RequestMapping("/api/{resource}")`
- Method injection (constructor-based, not @Autowired)
- `ResponseEntity` with status codes
- Stream + collect for list transformations
- **Swagger annotations**: `@Operation`, `@ApiResponse`, `@Parameter` for full API docs

Example pattern from [CursoController.java](src/main/java/com/jangada/RADAR/controllers/CursoController.java):

```java
@GetMapping
public ResponseEntity<List<CursoDTO>> listAll() {
    List<CursoDTO> dtos = cursoRepository.findAll()
        .stream()
        .map(CursoMapper::toDto)
        .collect(Collectors.toList());
    return ResponseEntity.ok(dtos);
}
```

### Recommendation Endpoints (Swagger-documented)

All endpoints in [RecomendacaoController.java](src/main/java/com/jangada/RADAR/controllers/RecomendacaoController.java):

- `POST /api/recomendacoes/gerar/{usuarioId}` - Generate recommended courses
  - Query: `?metodo=burrinho` (simple) or `busca` (future)
  - Returns: List of `RecomendacaoTurmaDTO` (turma + difficulty + professor_score + reason)

- `POST /api/recomendacoes/avaliar-professor` - Rate professor after completion
  - Params: `usuarioId`, `professorNome`, `componenteId`, `nota` (1-5), `comentario` (optional)
  - Returns: `AvaliacaoProfessorDTO`

- `GET /api/recomendacoes/professor/{professorNome}/avaliacoes` - Get all ratings for professor
- `GET /api/recomendacoes/professor/{professorNome}/score` - Get average rating for professor/component

### Professor Rating Endpoints

[AvaliacaoProfessorController.java](src/main/java/com/jangada/RADAR/controllers/AvaliacaoProfessorController.java):

- `GET /api/avaliacoes-professor` - List all evaluations
- `GET /api/avaliacoes-professor/{id}` - Get by ID
- `GET /api/avaliacoes-professor/usuario/{usuarioId}` - Get by student
- `GET /api/avaliacoes-professor/professor/{professorNome}` - Get by professor name
- `DELETE /api/avaliacoes-professor/{id}` - Delete evaluation

### Prerequisite Endpoints

[PreRequisitoController.java](src/main/java/com/jangada/RADAR/controllers/PreRequisitoController.java):

- `GET /api/prerequisitos` - List all prerequisites
- `GET /api/prerequisitos/componente/{componenteId}` - Get prerequisites for a component
- `GET /api/prerequisitos/componente/{componenteId}/tipo/{tipo}` - Filter by type
- `POST /api/prerequisitos` - Create prerequisite relationship
- `DELETE /api/prerequisitos/{id}` - Delete prerequisite

## Integration Points

### External Dependencies

- **Flyway**: Manages DB schema versioning
- **Springdoc OpenAPI**: Auto-generates Swagger from annotations (port 9090/swagger-ui/index.html)
- **Lombok**: Reduces boilerplate (build requires annotation processing)
- **Spring Security**: Authentication/authorization (may need expansion)
- **PostgreSQL**: Primary database (H2 available for testing)

### Key Properties

See [application.properties](src/main/resources/application.properties):

- Hibernate DDL: `ddl-auto=update` (auto-creates/updates schema)
- Flyway validation enabled
- SQL logging: DEBUG for app, INFO for framework
- Swagger paths: `/v3/api-docs`, `/swagger-ui.html`

## Common Tasks

### Adding a New Entity + API

1. Create entity in `models/entities/EntityName.java` (use Lombok @Data, @Builder)
2. Create `models/dtos/EntityNameDTO.java`
3. Create mapper: `mappers/EntityNameMapper.java` with static toDto() method
4. Create repository: `repositories/EntityNameRepository extends JpaRepository<EntityName, Long>`
5. Create controller: `controllers/EntityNameController` with standard REST methods
6. Add service layer if complex business logic needed

### Updating Existing Schema

1. Create new migration file: `V{n}__description.sql` in `src/main/resources/db/migration/`
2. Restart application - Flyway runs on startup
3. Verify via PgAdmin or database client

### Testing

Extend [RadarApplicationTests.java](src/test/java/com/jangada/RADAR/RadarApplicationTests.java) with:

- `@SpringBootTest` for integration tests
- `@Transactional` for rollback after test
- Use test-scoped H2 database or test container setup

## Notes for AI Agents

- **Strict naming**: Follow Entity/DTO/Mapper/Controller naming conventions exactly
- **Mappers are critical**: Never manually instantiate DTOs from entities; use mappers
- **Services underutilized**: Current app delegates most logic to repositories; expand services for complex workflows
- **Security context**: Currently permissive; hardening needed before production (e.g., JWT tokens, role-based access)
- **Pagination/Filtering**: Not implemented yet; consider adding for large result sets
- **Input validation**: Basic @Valid annotations present; expand with custom validators if needed
