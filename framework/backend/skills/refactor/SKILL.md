# Name

Refactor Skill

## Before Refactoring

1. Confirm existing tests pass before making any changes
2. Identify the scope - do not refactor beyond what the sprint task requires
3. Check that the refactor does not affect any contract in docs/contracts/

## Architecture Violations to Fix

- Business logic found in controllers -> move to domain/service
- JPA annotations found in domain/model -> extract to infrastructure/persistence
- Direct repository calls from controllers -> add port/in use case
- Hasrdcoded configuration values -> move to application.yml or env vars

## Rules

- One refactor commit per logical change - do not batch unrelated changes
- All tests must pass after every commit
- If a refactor requires changing a contract, update docs/contracts/openapi.json and notify frontend via frontend/sprints/current.md (set waiting_for field)

## After Refactoring

- Run full test suite
- Update Notes section in backend/sprints/current.md with what changed and why