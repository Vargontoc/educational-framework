# Sprint 006 - backend
# -----------------------------------------------

## Goal
Lay the foundations for FEAT-002 (Session Module): add the WebSocket dependency, document decisions in ADR-009, create the two DB migrations, update security to expose the auth endpoint, and publish the WebSocket contract baseline - no domain logic in this sprint.

## Status
status: completed
started_at: 2026-05-05 00:00:00
closed_at: 2026-05-05 00:00:00
blocked_by:
waiting_for:

## Tasks

### Dependency
- [x] Add `spring-boot-starter-websocket` to `pom.xml`

### ADR
- [x] `docs/architecture/decisions/ADR-009-Session-Module.md` verified and aligned

### Liquibase Migrations
- [x] Create `migrations/005__create_family_session.xml`
- [x] Create `migrations/006__create_child_session.xml`
- [x] Register both changeSets in `db.changelog-master.xml`

### Security
- [x] Update `shared/config/SecurityConfig.java` — `/api/v1/auth/**` permitted

### WebSocket contract baseline
- [x] Create `docs/contracts/api/websocket.json` with stable event names

### Application config
- [x] Add `app.session` defaults to `application.yml`

## Review

completed_tasks:
- Added `spring-boot-starter-websocket` dependency in backend `pom.xml`.
- Verified ADR-009 decisions are aligned with sprint goals and event taxonomy.
- Created `005__create_family_session.xml` and `006__create_child_session.xml` Liquibase migrations.
- Registered both migration files in `db.changelog-master.xml`.
- Updated `SecurityConfig` to permit `/api/v1/auth/**` before authenticated fallback.
- Created `docs/contracts/api/websocket.json` baseline with stable server-to-client events.
- Added `app.session` default configuration in `application.yml`.
incomplete_tasks:
- None.
contract_changes:
- Added new contract file: `docs/contracts/api/websocket.json` (baseline, channels placeholder, stable events).
learnings:
- Session storage design is now anchored in DB with explicit cascade behavior and heartbeat metadata.
- Event naming baseline is ready early, reducing string-literal drift in later infrastructure/domain sprints.
- DEFECT FOUND post-close: `family_session` and `child_session` tables are missing the `updated_at` column required by `BaseEntity`. Fixed in Sprint 007 migration `007__add_updated_at_to_session_tables.xml`.
- DEFECT FOUND post-close: `001__init_schema.xml` was accidentally dropped from `db.changelog-master.xml`. Restored manually before Sprint 007 start.
next_sprint_suggestions:
- Sprint 007: Session domain layer + fix `updated_at` migration.
