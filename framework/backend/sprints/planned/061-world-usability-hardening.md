# Sprint 061 - backend
# -----------------------------------------------

## Goal
Harden FEAT-008 with boundary checks, contract verification, and manual usability validation for the World Map flow.

## Status
status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Boundary Review
- [ ] Verify `world` does not own static catalog persistence.
- [ ] Verify `world` does not persist child learning progress directly.
- [ ] Verify `world` does not invoke avatar/TTS directly.
- [ ] Verify `world` does not expose dashboard reads.
- [ ] Verify tracking does not emit WebSocket events.
- [ ] Verify game does not depend on `world`.

### Contract Review
- [ ] Verify `docs/contracts/api/websocket.json` contains all world events needed by frontend.
- [ ] Verify `docs/contracts/api/openapi.json` contains dashboard engagement endpoint if implemented.
- [ ] Verify child-facing payloads do not include `ignored`, `abandoned`, `low engagement`, hidden progress, or diagnosis fields.

### Test Review
- [ ] Run all world unit tests.
- [ ] Run relevant content tests.
- [ ] Run relevant tracking tests.
- [ ] Run relevant game event tests.
- [ ] Run full backend test suite if practical.

### Manual Usability Validation
- [ ] Verify a discovery element does not look like a button/mission/level in the payload consumed by frontend.
- [ ] Verify ignoring a discovery element causes no pressure or repeated instruction.
- [ ] Verify an ignored proposal is tracked but not shown to the child.
- [ ] Verify rejected game start continues the walk with safe fallback.
- [ ] Verify abandoned activity returns to the walk calmly.
- [ ] Verify 2-3 second narrative pauses do not trigger inactivity.
- [ ] Verify `WORLD_HEARTBEAT` keeps the child session alive without active game.

## Manual Tests
- Use the checklist in Manual Usability Validation.
- Record commands, WebSocket messages, or fixtures used in the sprint review.

## Risks
- Manual validation may require frontend/dev fixtures.
- Docker/Testcontainers may not be available for integration tests.
- Contract fields can accidentally leak hidden engagement state to frontend.

## Dependencies
- Sprint 058 completed.
- Sprint 059 completed.
- Sprint 060 completed.

## Agent Instruction
- This is a hardening sprint, not a feature expansion sprint.
- Do not add new world behavior unless it fixes a test, boundary, or contract gap.
- Document skipped tests and manual validation gaps.

## Notes
This sprint closes FEAT-008 backend before full frontend integration and real usability testing.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
