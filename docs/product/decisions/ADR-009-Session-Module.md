# ADR-009 — Session Module: Token Strategy and Real-Time Architecture
# ─────────────────────────────────────────────────────────────────

## Status

status:        accepted
date:          2026-05-05
superseded_by: —

## Context

FEAT-002 introduces session management for two actor types: adult family members (parental panel) and child game clients. Each requires different auth, transport, and lifecycle characteristics.

Key constraints:
- Single-family, private application: no multi-tenant concerns.
- Adult panel access is PIN-controlled (4-digit BCrypt hash from Family entity).
- Child game clients connect to a game channel with low-latency requirements.
- Sessions must be revocable immediately (e.g. when the family PIN changes).
- No OAuth2 or external identity provider is in scope.

## Decisions

### D1 — Token format: opaque tokens (server-managed)

Chosen over JWT because:
- Immediate revocation: a database row update (revoked=true) invalidates the session without waiting for token expiry.
- PIN is 4 digits — short-lived JWTs still have an expiry window where a revoked token could be reused.
- Server-side lookup adds one DB query per request; acceptable for a single-node private app.

Implementation:
- `SecureRandom` generates 32 bytes → Base64url-encoded raw token (43 characters), returned to the client **once** at creation.
- Stored in DB as `token_hash = HEX(SHA-256(rawToken))` — never store the raw token.
- Lookup: hash incoming Bearer token → query `family_session.token_hash`.

### D2 — FamilySession lifecycle

- Created on successful PIN authentication.
- No automatic expiry by default (adults keep sessions until logout).
- Explicitly revoked when: adult calls logout OR family PIN changes (all sessions for that family are marked revoked=true in one transaction).
- `status` field: `active | expired | closed | revoked`.

### D3 — ChildSession: single-active-per-child enforcement

- At most one `active` ChildSession per `child_profile_id` at any time.
- On `createChildSession`: if an active session exists, close it gracefully (status=closed, endedAt=now, durationSeconds computed) before creating the new one. Entire operation in one `@Transactional` call.
- `status` field: `active | expired | expelled | closed`.

### D4 — Inactivity expiration (ChildSession)

- Clients send periodic heartbeats (REST or WebSocket message).
- Server records `last_activity_at` on each heartbeat.
- A `@Scheduled` job (every 5 minutes) marks sessions with `last_activity_at + heartbeat_grace_window < now` as `expired`.
- `heartbeat_interval_seconds` is configured per-session (defaults to 30s); grace window = 2 × interval.
- Configurable via `app.session.default-heartbeat-interval-seconds` and `app.session.heartbeat-grace-multiplier`.

### D5 — WebSocket channel separation

- **Parental / adult channel**: STOMP over WebSocket. Enables pub/sub topics for real-time parental control events. SockJS is acceptable as fallback for browser compatibility.
- **Game channel**: native WebSocket (`TextWebSocketHandler`), no SockJS, to minimize overhead for mobile game clients. Spring's `@EnableWebSocket` supports both simultaneously.
- Authentication on connect: all WebSocket connections must present a valid Bearer token in the handshake header; rejected if absent or invalid.

### D6 — Scheduled jobs

- **Expiration job**: `@Scheduled(cron = "0 */5 * * * *")` — runs every 5 minutes, marks inactive ChildSessions as expired.
- **Archival job**: `@Scheduled(cron = "0 0 2 * * *")` — runs nightly at 02:00, archives/deletes sessions older than `app.session.retention-days` (default: 30). Idempotent; logs structured events.

### D7 — Stable WebSocket event names

Defined in `docs/contracts/api/websocket.json`. Client and server must never use string literals directly — always reference these constants:

| Event                  | Direction        | Description                                |
|------------------------|------------------|--------------------------------------------|
| `GAME_STATE_UPDATE`    | server → client  | State restore on reconnect                 |
| `SESSION_EXPIRED`      | server → client  | Inactivity timeout reached                 |
| `SESSION_INVALIDATED`  | server → client  | Session revoked externally (PIN change)    |
| `CHILD_EXPELLED`       | server → client  | Admin expelled child                       |
| `PARENT_BLOCK`         | server → client  | Parent blocked child (expel + lock)        |
| `HEARTBEAT_ACK`        | server → client  | Heartbeat acknowledged                     |

## Consequences

positive:
  - Immediate revocation without token rotation complexity.
  - Clean separation between adult STOMP channel and game native WS.
  - Heartbeat + scheduled jobs provide observable inactivity management.
  - Opaque tokens are simpler to reason about for a private single-node deployment.

negative:
  - Every authenticated REST request incurs one DB read (token lookup).
  - Opaque tokens cannot carry claims — any role/permission check requires an extra DB query.
  - Scheduled jobs must be monitored; a stuck job causes stale expired sessions.

neutral:
  - If the app ever scales horizontally, the scheduled job must be made cluster-aware (e.g. ShedLock or Quartz). Not required now.

## Alternatives considered

alternative: JWT with short TTL (e.g. 15 min) + refresh tokens
reason_rejected: Adds complexity (refresh flow, token rotation) with no benefit in a private single-user deployment. Revocation still requires a deny-list.

alternative: Spring Session with Redis
reason_rejected: Introduces an additional infrastructure dependency (Redis) that is not in the current stack. PostgreSQL-backed sessions suffice.

## References

- FEAT-002: Session Module feature proposal
- ADR-007: Backend Stack (Spring Boot + Spring AI)
- ADR-001: Infrastructure Setup (Docker Compose services)
