# Sprint 032 - backend
# -----------------------------------------------

## Goal
Add `GameSessionSummary` in tracking so the future game shell can persist completed or abandoned game session summaries.

## Status
status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Tracking Schema
- [ ] Add `GameSessionSummary` domain model in tracking.
- [ ] Add persistence entity and repository for `GameSessionSummary`.
- [ ] Add Liquibase migration for the `game_session_summary` table.
- [ ] Store `childProfileId`, `childSessionId`, `activityId`, start/end difficulty, score, totals, timestamps, and final status.
- [ ] Add indexes for `childProfileId`, `childSessionId`, `activityId`, and `endedAt`.

### Tracking Port
- [ ] Add internal port/use case `registerGameSessionSummary(...)`.
- [ ] Validate `finalStatus` only accepts `COMPLETED` or `ABANDONED`.
- [ ] Validate `endedAt` is not before `startedAt`.
- [ ] Return the created summary id or a lightweight response object.

### Tests
- [ ] Unit test successful summary registration.
- [ ] Unit test invalid final status is rejected.
- [ ] Unit test invalid timestamp range is rejected.
- [ ] Persistence/integration test saves and reads a summary if Testcontainers is available.

## Manual Tests
- Start backend locally.
- Use a temporary dev runner, test fixture, or direct DB check to register one summary.
- Confirm the row exists in `game_session_summary` with the expected values.
- Confirm no public REST endpoint was added for this internal operation.

## Risks
- Duplicating session data outside tracking would reintroduce cross-module read coupling.
- Missing indexes could make future dashboard recent-session queries slower.
- Exposing this as public REST would violate the tracking boundary.

## Dependencies
- Sprint 021 tracking schema foundation completed.
- Sprint 022 tracking attempt registration completed.
- FEAT-007 requires this before the game orchestrator can finalize sessions.

## Agent Instruction
- Keep this sprint tracking-only; do not implement game orchestration.
- Do not add WebSocket behavior.
- Do not expose public REST endpoints unless a later dashboard sprint explicitly requires it.
- Keep the migration additive; never modify existing migration files.

## Notes
This sprint resolves the FEAT-007 dependency that tracking owns persisted per-game session summaries.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
