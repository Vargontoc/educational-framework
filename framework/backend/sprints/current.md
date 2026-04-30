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
- [ ] Initialize Maven project: create `pom.xml` with Spring Boot 3.3.x BOM and dependencies (web, data-jpa, postgresql, liquibase, actuator, security, springdoc-openapi, spring-ai-ollama-spring-boot-starter)
- [ ] Create Maven wrapper (`mvnw` + `.mvn/wrapper/`) so the Dockerfile can build without a host Maven installation
- [ ] Create main application class `EducationalFrameworkApplication.java` under `es.vargontoc.educational.framework`
- [ ] Create `src/main/resources/application.yml` with externalized config for datasource, server port, actuator exposure, Liquibase, and Spring AI
- [ ] Set up Liquibase: `src/main/resources/db/changelog/db.changelog-master.xml` + first migration `migrations/001__init_schema.xml` (empty schema baseline)
- [ ] Create root package skeleton matching hexagonal layout (empty placeholder packages for first domain)
- [ ] Configure Spring Security: permit `/actuator/health` without authentication; lock all other endpoints by default
- [ ] Verify root `.gitignore` covers `framework/backend/target/` and `framework/backend/.mvn/wrapper/maven-wrapper.jar` (already added in sprint prep — confirm entries are present after scaffolding)
- [ ] Create `src/main/resources/application-dev.yml` — dev profile overrides: SQL logging enabled, actuator `show-details: always`, Liquibase `drop-first: false`, verbose Spring AI logging
- [ ] Create `src/main/resources/application-prod.yml` — prod profile overrides: SQL logging disabled, actuator `show-details: never`, Hikari pool tuned, Spring AI logging at WARN
- [ ] Set active profile via env var: `SPRING_PROFILES_ACTIVE=dev` in `backend.env.example`; infrastructure sets `SPRING_PROFILES_ACTIVE=prod` in the prod environment
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
