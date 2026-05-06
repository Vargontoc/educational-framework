# FEAT-002 - Backend: Session Module

## Status

state: scheduled
user_history: session module configuration
depends_on: FEAT-001-Family-Module.md
owned_by: backend
test: Verify WebSocket connection, reconnection and session lifecycle
adr: docs/architecture/decisions/ADR-009-Session-Module.md
sprints:
    - backend/sprints/history/006-session-module-foundations-2026-05-05.md (Sprint 006 — completed)
    - backend/sprints/history/007-session-module-domain-2026-05-06.md (Sprint 007 — completed: domain layer + updated_at migration fix)
    - backend/sprints/current.md (Sprint 008 — active: REST layer + jobs + security filter)

# Description

This feature defines the Session Module responsible for managing real-time connections and session lifecycle for families (adults) and children (game clients). The module handles authentication/access control and real-time parental control functionality.

It is the real-time bridge between the Family context and the rest of the application, providing a clear separation of concerns:
- Authentication and access tokens for family panels
- Child session lifecycle and real-time control for game clients

# Decisions

Persistence with retention/archival
All sessions are persisted. A configurable retention policy will be implemented (e.g. a scheduled job that archives or deletes sessions older than N days). This is the first use of the historical-deletion pattern which will be reused for events and tracking. Consider exposing this as a generic facility in shared.

FamilySession lifetime
Family sessions (adult panel) do not expire automatically by default: tokens remain valid until the adult explicitly signs out. A forced invalidation mechanism must exist: when a Family PIN changes or an admin revokes access, all active FamilySessions must be revoked.

Single active ChildSession per child profile
When creating a ChildSession for a child profile, the system must check for an existing active session. If one exists, it should be closed gracefully before creating the new session to avoid inconsistent tracking or duplicate WebSocket channels.

Inactivity expiration
Support a configurable inactivity timeout scoped per Family and a global default. Clients send periodic heartbeats; if no heartbeat is received within the configured window the ChildSession expires. The server must notify the client via a specific WebSocket event so the UI can react gracefully.

Fallback transports
SockJS is acceptable as a fallback for browser compatibility but should be limited to parental and adult agent channels. For game channels prefer native WebSocket (no SockJS) to minimize latency and overhead on modern mobile browsers.

Bidirectional events on game channel
The backend can emit events without prior action from the client (e.g., parental block, agent dropped). Define a clear event model for both directions and reserve specific system event types that the frontend will always handle.

Streaming for adult agent
For streaming conversational responses consider Server-Sent Events (SSE) or WebSocket with chunked messages. SSE is simpler for uni-directional streaming (server → client) when the adult only needs to send a message and receive a streaming response.

Reconnection and state restore
Automatic reconnection with exponential backoff is mandatory for child clients. Because ChildSession state and game state are persisted, the server must restore state on reconnect and emit a GAME_STATE_UPDATE to resynchronize the client.

# WebSocket policy

Channel selection
- Parental/control panel: STOMP over WebSocket (convenient pub/sub and lower need for ultra-low latency).
- Game channel: native WebSocket (no SockJS) or optimized STOMP (if used, remove SockJS fallback).

Authentication on connect
All WebSocket/SOCKS/STOMP connects must authenticate during the initial handshake (token or session id). Connections without valid auth must be rejected.

Topic and channel naming
Define a stable topic naming convention, e.g.:
- /topic/family/{familyId}/children
- /user/family/{familyId}/alerts
- /ws/game/{childSessionId}

Rate limits and quotas
Document per-channel rate limits (messages/sec) and connection quotas per Family to prevent abuse.

# Schemas (recommended fields)

FamilySession
- id: long
- tokenHash: string (store hash, never store raw token)
- tokenType: enum (opaque | jwt)
- familyId: long (reference)
- createdAt: timestamp
- expiresAt: timestamp (nullable)
- revoked: boolean
- createdByIP: string
- deviceId: string
- status: enum(active | expired | closed)
- indexes: familyId, status, createdAt

ChildSession
- id: long
- childProfileId: long
- familyId: long
- startedAt: timestamp
- endedAt: timestamp (nullable)
- durationSeconds: integer (computed at close)
- status: enum(active | expired | expelled | closed)
- lastActivityAt: timestamp
- heartbeatIntervalSeconds: integer (configured per-session or default)
- connectionMeta: { ip, deviceId, userAgent }
- persistedGameStateRef: reference (optional)
- indexes: childProfileId, familyId, status, lastActivityAt

Security note: store tokens or sensitive fields encrypted at rest and use tokenHash for lookup/revocation.

# Operations / Use cases

- Authenticate family with PIN → create FamilySession → return token (opaque or JWT)
- Revoke all FamilySessions when PIN changes or admin action occurs
- Open ChildSession → if existing active session: close it gracefully → create new ChildSession
- Close child session cleanly (compute duration, persist endedAt)
- Expel child → set status=expelled → notify via WebSocket
- Block child → combine expel + delegate block event to family channel
- Query active child sessions for parental panel (quick index-based queries)
- Receive heartbeat from child client → update lastActivityAt
- Scheduled job: expire sessions by inactivity
- Scheduled job: archival / cleanup according to retention policy

# Tokens and invalidation

- Decide token format explicitly: JWT (self-contained) vs opaque (server-managed). Recommendation: opaque tokens with server-side revocation for immediate invalidation on PIN change.
- If JWTs are used, include a revocation strategy (e.g., short TTL + revoke list in datastore).
- Never return raw tokens in logs; store only tokenHash.
- On PIN change: mark existing FamilySessions.revoked = true and publish a SESSION_INVALIDATED event to affected clients.

# Heartbeat and reconnection parameters (defaults)

- defaultHeartbeatInterval: 30s
- heartbeatGraceWindow: 2 × heartbeatInterval
- reconnectionBackoff: exponential starting at 1s, max 60s
- server event names (stable): GAME_STATE_UPDATE, SESSION_EXPIRED, SESSION_INVALIDATED, CHILD_EXPELLED, PARENT_BLOCK

# Jobs and scheduling

- Expiration job (example cron): run every 5 minutes to find sessions with lastActivityAt + timeout < now and mark as expired
- Cleanup/archival job: run nightly; archive or delete sessions older than retentionDays (configurable)
- Jobs should be idempotent and observable (emit metrics and logs)

# Observability and metrics

Emit metrics and events for:
- active_sessions_count
- session_creations_total
- session_expirations_total
- session_invalidations_total
- reconnections_total
- forced_session_closures_total
- heartbeat_misses_total

Log structured events for critical actions: session_create, session_close, session_expire, session_revoke, child_expel.

# Tests and Acceptance Criteria

Automated tests and acceptance tests should cover:
- WebSocket connection establishment and authenticated handshake
- Heartbeat lifecycle: active → expired after missed heartbeats
- Single-session enforcement for ChildSession (session takeover)
- PIN change revocation: all family sessions invalidated and clients notified
- Reconnection restores state and emits GAME_STATE_UPDATE
- Jobs correctly expire and archive old sessions respecting retention policy

# State machine (suggested)

ChildSession lifecycle: created → active → (expired | expelled | closed)
Transitions must be deterministic and recorded in persistent storage to allow reconnection/state restore.

# Privacy and compliance

- Treat session records as PII when they include device identifiers or IPs. Document retention policy and ensure encrypted storage.
- Provide safe-delete vs archive modes to comply with GDPR requests.

# Deliverables and next steps (for the backend team)

- Add the fields specified in the Schemas section to the session persistence model
- Choose token strategy (opaque recommended) and document the revocation flow
- Implement heartbeat and reconnection strategy with default values above
- Implement scheduled jobs for expiration and archival with configuration knobs
- Define and publish WebSocket topic naming and stable event definitions in docs/contracts/websocket.json
- Add tests and observability for the items listed in Tests and Observability
