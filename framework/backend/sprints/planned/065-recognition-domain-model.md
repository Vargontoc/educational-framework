# Sprint 065 - backend
# -----------------------------------------------

## Goal
Create the pure recognition domain model required by FEAT-009 without orchestration, persistence, WebSocket, or tracking integration.

## Status
status: planned
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Domain Model
- [ ] Create the recognition package following the backend hexagonal structure.
- [ ] Add `RecognitionCategory` enum or equivalent domain value for `LETTER`, `NUMBER`, `SHAPE`, `COLOR`, and `ANIMAL`.
- [ ] Add `RecognitionState` payload model with FEAT-009 fields.
- [ ] Add `RecognitionAttemptContext` model with FEAT-009 fields.
- [ ] Add defaults or configuration hooks for 5 rounds and 2-3 options.
- [ ] Ensure recognition models do not contain `topicId`, `promptType`, `biomeCode`, `totalTimeouts`, or motor timeout fields.

### Tests
- [ ] Unit test `RecognitionState` can represent an initial round.
- [ ] Unit test retry counters and hint state can be represented.
- [ ] Unit test `RecognitionAttemptContext` stores target, selected option, options, first-try flag, hint flags, attempt number, and response time.
- [ ] Unit test recognition models do not require framework or persistence dependencies.

## Manual Tests
- Not required. This is a domain-only sprint.

## Risks
- Over-modeling persistence details here would make later engine work harder for junior developers.
- Reintroducing prompt, biome, topic, or timeout fields would contradict FEAT-009.

## Dependencies
- FEAT-007 game domain model.
- Sprint 063 if category names depend on content conventions.

## Agent Instruction
- Keep all classes framework-free where possible.
- Do not add JPA entities, migrations, REST endpoints, or WebSocket payloads.
- Do not call content, tracking, world, avatar, or frontend-specific code.
- Keep code, comments, and names in English.

## Notes
This sprint establishes the vocabulary used by the concrete `RecognitionEngine`.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
