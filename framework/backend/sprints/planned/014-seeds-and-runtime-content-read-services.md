# Sprint 014 - backend
# -----------------------------------------------

## Goal
Add idempotent MVP seed loading and runtime read services for game engines and WebSocket flows without exposing development administrative APIs in production.

## Status
status: pending
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Seed Loading
- [ ] Implement idempotent seed loading for MVP 3-4 content.
- [ ] Seed categories first.
- [ ] Seed topics referencing categories.
- [ ] Seed curiosities referencing topics.
- [ ] Seed activities referencing topics.
- [ ] Seed difficulty levels referencing activities.
- [ ] Seed avatar event fallback messages.
- [ ] Seed learning paths and learning path steps.
- [ ] Seed tracing patterns.
- [ ] Seed stories and story pages.

### Runtime Read Services
- [ ] Implement service methods for active categories and topics.
- [ ] Implement service methods for active activities by age/category/topic when applicable.
- [ ] Implement service methods for active difficulty levels by activity.
- [ ] Implement service methods for active activity resources by activity/topic.
- [ ] Implement service methods for active curiosities by topic, age, and locale.
- [ ] Implement service methods for active avatar fallback messages by event type, tone, and locale.
- [ ] Implement service methods for active tracing patterns by topic.
- [ ] Implement service methods for active learning paths where runtime consumers need them.

### Productive Read APIs
- [ ] Add productive read-only APIs only where required by runtime/frontend flows.
- [ ] Ensure productive read APIs return only active content.
- [ ] Ensure productive read APIs do not expose administrative update fields unnecessarily.

### Tests
- [ ] Add tests proving seed loading is idempotent.
- [ ] Add tests proving repeated seed runs do not duplicate records.
- [ ] Add tests for runtime read filters.
- [ ] Add tests proving inactive and draft content is excluded from productive runtime reads.

## Risks
- Seeds may become too large or too coupled to development test data.
- Runtime read APIs could become accidental replacements for internal services.
- Public read endpoints should not expose draft or inactive editorial content.

## Dependencies
- Sprints 010, 011, 012, and 013 completed.
- Final MVP 3-4 content list agreed or represented as provisional seed data.

## Agent Instruction
- Seeds must be idempotent and safe to re-run.
- Do not expose `/api/v1/dev/content/**` behavior in production.
- Runtime consumers should use backend services first; create productive APIs only when a frontend/runtime flow requires them.
- Do not implement tracking, game engines, WebSocket handlers, TTS, or agent calls in this sprint.

## Notes
This sprint creates usable MVP data and stable read services for future game/websocket integration.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
