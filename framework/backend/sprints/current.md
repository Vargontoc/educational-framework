# Sprint 001 - backend
# -----------------------------------------------

## Goal
Bootstrap the minimum viable Spring Boot 3 project: Maven structure, hexagonal package skeleton, Liquibase setup, Spring AI wiring, and a running `/actuator/health` endpoint connected to PostgreSQL.

## Status
status: active
started_at: 2026-04-30 00:00:00
closed_at:
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
- [ ] Copy `envs/backend.env.example` → `envs/backend.env` (gitignored) and fill real values for local dev
- [ ] Verify: `./mvnw spring-boot:run` starts with profile `dev`, connects to postgres, and `GET /actuator/health` returns `{"status":"UP"}`
- [ ] Verify Docker build: `docker build -t edu-backend .` succeeds from `framework/backend/`

## Risks
- Spring AI Ollama starter version must be compatible with the Spring Boot BOM version chosen — pin `spring-ai.version` explicitly to avoid conflicts.
- Liquibase master changelog must be registered before running the application; failure to do so blocks DB startup.
- `backend.env` must not be committed — verify `.gitignore` covers `framework/backend/envs/*.env`.

## Dependencies
- PostgreSQL must be running (infrastructure layer, `docker-compose up postgres`).
- Ollama is not required for this sprint — Spring AI config just needs to point to the right URL; lazy initialization avoids startup failures if Ollama is offline.
- No contract changes expected — `openapi.json` will be generated in a future sprint when the first domain endpoint is added.

## Agent Instruction
- Follow hexagonal architecture strictly per `framework/backend/skills/coding/SKILL.md`.
- Root package: `es.vargontoc.educational.framework`.
- Use constructor injection everywhere — no `@Autowired` field injection.
- All config values that appear in `application.yml` must be sourced from environment variables — no hardcoded credentials.
- Profile activation is always via env var `SPRING_PROFILES_ACTIVE` — never hardcode `spring.profiles.active` in any yml file.
- `application.yml` holds shared config only; profile files override only what differs per environment.
- Set `spring.ai.ollama.init.pull-model-strategy=never` to avoid Spring AI trying to pull models on startup.
- Set `spring.jpa.hibernate.ddl-auto=validate` — Liquibase owns the schema, Hibernate only validates.
- Do NOT add any domain or business logic in this sprint — only the skeleton and infrastructure wiring.
- After completing all tasks, mark status as `completed` and fill the Review section.

## Notes
Sprint triggered by ADR-007 (`docs/architecture/decisions/ADR-007-backend-layer.md`).

### Minimum dependency list
```xml
<!-- Core -->
spring-boot-starter-web
spring-boot-starter-data-jpa
spring-boot-starter-security
spring-boot-starter-actuator

<!-- Database -->
org.postgresql:postgresql (runtime)
org.liquibase:liquibase-core

<!-- AI -->
org.springframework.ai:spring-ai-ollama-spring-boot-starter

<!-- API Docs -->
org.springdoc:springdoc-openapi-starter-webmvc-ui

<!-- Test -->
spring-boot-starter-test
org.testcontainers:postgresql
```

### application.yml skeleton
```yaml
server:
  port: ${SERVER_PORT:8080}

spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.xml
  ai:
    ollama:
      base-url: ${SPRING_AI_OLLAMA_BASE_URL:http://ollama-educational:11434}
      chat:
        model: ${SPRING_AI_OLLAMA_CHAT_MODEL:llama3.2}
      init:
        pull-model-strategy: never

management:
  endpoints:
    web:
      exposure:
        include: health
  endpoint:
    health:
      show-details: always

logging:
  level:
    es.vargontoc: ${LOGGING_LEVEL_ES_VARGONTOC:INFO}
```

### Profile strategy
Base config (`application.yml`) holds shared values and env-var placeholders. Profile files only override what genuinely differs.

```yaml
# application-dev.yml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  liquibase:
    drop-first: false

management:
  endpoint:
    health:
      show-details: always

logging:
  level:
    es.vargontoc: DEBUG
    org.springframework.ai: DEBUG
```

```yaml
# application-prod.yml
spring:
  jpa:
    show-sql: false
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 30000

management:
  endpoint:
    health:
      show-details: never

logging:
  level:
    es.vargontoc: ${LOGGING_LEVEL_ES_VARGONTOC:INFO}
    org.springframework.ai: WARN
```

Profile activation in `backend.env.example`:
```
SPRING_PROFILES_ACTIVE=dev
```
Infrastructure sets `SPRING_PROFILES_ACTIVE=prod` in the prod env file — no code change required between environments.

### Package skeleton
```
es.vargontoc.educational.framework/
    EducationalFrameworkApplication.java
    shared/
        config/
            SecurityConfig.java       (permit /actuator/health)
            OpenApiConfig.java        (SpringDoc base config)
```

## Review
completed_tasks:
incomplete_tasks:
contract_changes:
learnings:
next_sprint_suggestions:
