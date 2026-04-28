## Layer Context

layer: backend
stack: Java 21 + Spring Boot 3 + PostgreSQL 16 + Liquibase + JWT + SpringAI
pattern: Hexagonal architecture (ports and adapters) per functional domain
does_not_own: UI components, Docker Compose, environment orchestration

## Package Structure

root: es.vargontoc.educational.framework
{domain}:
    {domain}/model/       -> pure entities, no framework dependencies
    {domain}/ports/in/    -> use case interfaces (driven by application)
    {domain}/ports/out/   -> repository and external service interfaces
    {domain}/service/         -> pure business logig, implements ports/in
    {domain}/application/        -> orchestrates use cases, calls ports
    {domain}infrastructure/
    {domain}/infrastructure/persistence/    -> JPA entities, repositories (implements ports/out)
    {domain}/infrastructure/web/            -> REST Controllers (adapters in)
    {domain}/infrastructure/dto/                -> request and response objects 

## Migrations

tool: Liquibase
location: src/main/resource/db/changelog/
rules:
    - Never modify an existing migration file
    - Always create a new migration for schemas changes
    - Migration naming: V{version}_{description}.sql (e.g. 001_init_schema.sql)

## Contract

output: docs/contracts/api/openapi.json
rule: After any change to a REST endpoint, regenerate and update openapi.json
rule: Frontend depends on this file - breaking changes must be communicated

output: docs/contracts/api/websocket.json
rule: After any change to a WebSocket endpoint, regenerate and update websocket.json
rule: Frontend depends on this file - breaking changes must be communicated

## Testing strategy

unit: JUnit 5 + Mockito - test {domain}/service in isolation
integration: @SpringBootTest + Testcontainers - test controllers with real DB
location: mirrors src/main/java structure under src/test/java
rule: Every port/in use case must have at least one unit test
rule: Every port/in use case must have at least positive and negative integration test

## Skills Available

coding: backeckend/skills/coding/SKILL.md
testing: backend/skills/testing/SKILL.md
refactor: backend/skills/refactor/SKILL.md
design: backend/skills/design/SKILL.md

## Sprint Context

current_sprint: backend/sprints/current.md

## Workflow

1. Read root AGENT.md for global context
2. Read this file for backend context
3. Check backend/sprints/current.md - if blocked, stop and report
4. Identify task type (coding, testing, refactor, design)
5. Load the matching skill from backend/skills/
6. Follow the Agent Instructions in current.md for this sprint
7. Never hardcode values present in backend.env.example
8. After endpoint changes, update docs/contracts/openapi.json
9. Commit following: backend/type/short-description

## Agent Compatibility

This file is written in plain structured natural language.
It is compatible with: Claude Code, Gemini, ChatGPT, and local models