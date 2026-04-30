# Sprint 001 - backend
# -----------------------------------------------

## Goal
Bootstrap the minimum viable Spring Boot 3 project: Maven structure, hexagonal package skeleton, Liquibase setup, Spring AI wiring, and a running `/actuator/health` endpoint connected to PostgreSQL.

## Status
status: completed
started_at: 2026-04-30 00:00:00
closed_at: 2026-04-30 00:00:00
blocked_by:
waiting_for:

## Tasks
- [x] Initialize Maven project: create `pom.xml` with Spring Boot 3.3.5 + Spring AI 1.0.0 BOM and all required dependencies
- [x] Create Maven wrapper (`mvnw` + `mvnw.cmd` + `.mvn/wrapper/maven-wrapper.properties`) — Maven 3.9.8
- [x] Create main application class `EducationalFrameworkApplication.java` under `es.vargontoc.educational.framework`
- [x] Create `src/main/resources/application.yml` with externalized config for datasource, server port, actuator, Liquibase, and Spring AI
- [x] Set up Liquibase: `db/changelog/db.changelog-master.xml` + `migrations/001__init_schema.xml` (empty schema baseline)
- [x] Create root package skeleton: `shared/config/SecurityConfig.java` + `shared/config/OpenApiConfig.java`
- [x] Configure Spring Security: permit `/actuator/health` + Swagger UI; lock all other endpoints by default (STATELESS)
- [x] Verify root `.gitignore` covers `framework/backend/target/` and `.mvn/` (entries present)
- [x] Create `src/main/resources/application-dev.yml` — SQL logging, actuator verbose, verbose Spring AI + Hibernate logging
- [x] Create `src/main/resources/application-prod.yml` — SQL off, Hikari pool tuned, actuator show-details: never, Spring AI at WARN
- [x] Set active profile via env var: `SPRING_PROFILES_ACTIVE=dev` added to `backend.env.example`
- [ ] Copy `envs/backend.env.example` → `envs/backend.env` — manual step, gitignored
- [ ] Verify: `mvn spring-boot:run` starts with profile `dev` and `GET /actuator/health` returns UP — pending real DB connection
- [ ] Verify Docker build: `docker build -t edu-backend .` — pending infrastructure env

## Review

completed_tasks:
    - pom.xml with Spring Boot 3.3.5, Spring AI 1.0.0 BOM (spring-ai-starter-model-ollama), Liquibase, SpringDoc, Testcontainers.
    - Dockerfile updated to multi-stage using maven:3.9-eclipse-temurin-21-alpine (no wrapper needed).
    - Main application class, SecurityConfig (STATELESS, health + Swagger permitted), OpenApiConfig.
    - application.yml with fully externalized config; application-dev.yml and application-prod.yml with profile-specific overrides.
    - Liquibase master changelog + empty baseline migration 001__init_schema.xml.
    - Test scaffolding: TestcontainersConfiguration (@ServiceConnection) + EducationalFrameworkApplicationTests (@ActiveProfiles("test"), H2 in-memory for contextLoads).
    - Root .gitignore updated with backend/target/ and backend/.mvn/ entries.
    - backend.env.example with SPRING_PROFILES_ACTIVE, datasource, Ollama, JWT, and logging vars.

incomplete_tasks:
    - Manual env copy and startup verification — no blocker, requires developer action.
    - Docker build verification — depends on infrastructure team wiring the service.

contract_changes:
    none — no REST endpoints added in this sprint; openapi.json not yet generated.

learnings:
    - Spring AI 1.0.0 GA renamed the Ollama starter from spring-ai-ollama-spring-boot-starter to spring-ai-starter-model-ollama. Use the BOM to manage versions; do not pin explicitly to avoid conflicts.
    - Dockerfile must use maven:3.9-eclipse-temurin-21-alpine as base to avoid needing the Maven wrapper; environment requires Maven installed.
    - YAML keys containing dots (package names under logging.level) must be wrapped in "[key]" to avoid YAML nesting misinterpretation.
    - ${SPRING_DATASOURCE_URL} with no default causes YAML parse failure in test context — always provide a fallback default for required env vars.
    - @ServiceConnection with Testcontainers overrides datasource at ConnectionDetails level, not property level; YAML still needs a parseable placeholder.

next_sprint_suggestions:
    - Sprint 002: Implement the shared module per ADR-008 — BaseEntity, exception hierarchy, IValidator/AbstractValidator, ApiResponse.
    - Sprint 003: First domain (TTS or agent child) using shared building blocks.
