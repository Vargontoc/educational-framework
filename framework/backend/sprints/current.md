# Sprint 006 - backend
# -----------------------------------------------

## Goal
Lay the foundations for FEAT-002 (Session Module): add the WebSocket dependency, document decisions in ADR-009, create the two DB migrations, update security to expose the auth endpoint, and publish the WebSocket contract baseline — no domain logic in this sprint.

## Status
status: active
started_at: 2026-05-05 00:00:00
closed_at:
blocked_by:
waiting_for:

## Tasks

### Dependency
- [ ] Add `spring-boot-starter-websocket` to `pom.xml` inside the `<dependencies>` block (no version — managed by Spring Boot BOM)

### ADR
- [ ] `docs/architecture/decisions/ADR-009-Session-Module.md` is already written — verify content reflects the decisions below and update if needed

### Liquibase Migrations
- [ ] Create `migrations/005__create_family_session.xml` — table `family_session`: `id` (bigserial PK), `token_hash` (varchar 64 NOT NULL UNIQUE — SHA-256 hex), `token_type` (varchar 10 NOT NULL DEFAULT 'opaque'), `family_id` (bigint NOT NULL FK → family(id) ON DELETE CASCADE), `created_at` (timestamptz NOT NULL), `expires_at` (timestamptz nullable), `revoked` (boolean NOT NULL DEFAULT false), `created_by_ip` (varchar 45), `device_id` (varchar 255), `status` (varchar 20 NOT NULL DEFAULT 'active'); indexes on `family_id`, `status`, `token_hash` (unique already covers this), `created_at`
- [ ] Create `migrations/006__create_child_session.xml` — table `child_session`: `id` (bigserial PK), `child_profile_id` (bigint NOT NULL FK → child_profile(id) ON DELETE CASCADE), `family_id` (bigint NOT NULL FK → family(id) ON DELETE CASCADE), `started_at` (timestamptz NOT NULL), `ended_at` (timestamptz nullable), `duration_seconds` (integer nullable — computed at close), `status` (varchar 20 NOT NULL DEFAULT 'active'), `last_activity_at` (timestamptz NOT NULL), `heartbeat_interval_seconds` (integer NOT NULL DEFAULT 30), `connection_meta` (text nullable — JSON string: ip, deviceId, userAgent), `persisted_game_state_ref` (varchar 255 nullable); indexes on `child_profile_id`, `family_id`, `status`, `last_activity_at`
- [ ] Register both changeSets in `db.changelog-master.xml`

### Security
- [ ] Update `shared/config/SecurityConfig.java` — add `.requestMatchers("/api/v1/auth/**").permitAll()` alongside the existing `/api/v1/family/**` permit; all other endpoints remain `anyRequest().authenticated()` (the token filter is added in Sprint 008)

### WebSocket contract baseline
- [ ] Create `docs/contracts/api/websocket.json` — include `info`, an empty `channels: {}` placeholder, and a `components.events` section with the seven stable event names from ADR-009 (`GAME_STATE_UPDATE`, `SESSION_EXPIRED`, `SESSION_INVALIDATED`, `CHILD_EXPELLED`, `PARENT_BLOCK`, `HEARTBEAT_ACK`) and their direction/description

### Application config
- [ ] Add session defaults to `application.yml` under `app.session`:
    ```yaml
    app:
      session:
        default-heartbeat-interval-seconds: 30
        heartbeat-grace-multiplier: 2
        retention-days: 30
    ```

## Risks
- `spring-boot-starter-websocket` transitively pulls `spring-messaging` and `spring-websocket`; verify no version conflicts with the existing Spring Boot 3.3.5 BOM after adding the dependency.
- `family_session.token_hash` is VARCHAR(64) — SHA-256 hex output is exactly 64 characters; do not use a shorter type.
- `ON DELETE CASCADE` on `family_id` in `family_session` means deleting the family also deletes all sessions — intentional for a single-family private app.
- `child_session.connection_meta` stored as TEXT (JSON string) avoids a JSONB dependency on Liquibase XML and is sufficient for a private app; parse it at the service layer when needed.

## Dependencies
- Sprint 005 completed: family module REST API is live; `default_seq` sequence exists.
- ADR-009 accepted: token strategy (opaque), WebSocket channel split, and heartbeat defaults are decided.
- No domain or infrastructure code changes in this sprint.

## Agent Instruction
- `pom.xml` change: add the dependency block immediately after the `spring-boot-starter-security` block for grouping clarity.
- Migrations must follow the same XML format as 002–004: `dbchangelog-4.27.xsd`, changeSet id equals filename without extension.
- `websocket.json` format: use AsyncAPI 2.x or a simple custom JSON structure consistent with `openapi_tts.json` in the same folder — check the existing TTS contract for the format in use.
- `SecurityConfig` change: the new `requestMatchers` line must come before `anyRequest().authenticated()` — order matters in Spring Security.
- Do not create any Java source files in this sprint — foundations only.
- After completing all tasks, mark status as `completed` and fill the Review section.

## Notes
This sprint unblocks Sprint 007 (domain) and Sprint 008 (infrastructure). No service or controller code is written here.

Suggested sequence:
- Sprint 007: Session domain layer (FamilySession + ChildSession models, ports, services, token utility, unit tests)
- Sprint 008: Session REST infrastructure (JPA entities, adapters, DTOs, REST controllers, token security filter, integration tests)

## Review

completed_tasks:
incomplete_tasks:
contract_changes:
learnings:
next_sprint_suggestions:
