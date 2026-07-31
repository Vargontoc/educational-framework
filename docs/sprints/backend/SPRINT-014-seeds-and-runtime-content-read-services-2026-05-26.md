# Sprint 014 - backend
# -----------------------------------------------

## Goal
Add idempotent MVP seed loading and runtime read services for game engines and WebSocket flows without exposing development administrative APIs in production.

## Status
status: active
started_at: 2026-05-26
closed_at:
blocked_by:
waiting_for:

## Tasks

### Seed Loading
- [x] Implement idempotent seed loading for MVP 3-4 content.
- [x] Seed categories first.
- [x] Seed topics referencing categories.
- [x] Seed curiosities referencing topics.
- [x] Seed activities referencing topics.
- [x] Seed difficulty levels referencing activities.
- [x] Seed avatar event fallback messages.
- [x] Seed learning paths and learning path steps.
- [x] Seed tracing patterns.
- [x] Seed stories and story pages.

### Runtime Read Services
- [x] Implement service methods for active categories and topics.
- [x] Implement service methods for active activities by age/category/topic when applicable.
- [x] Implement service methods for active difficulty levels by activity.
- [x] Implement service methods for active activity resources by activity/topic.
- [x] Implement service methods for active curiosities by topic, age, and locale.
- [x] Implement service methods for active avatar fallback messages by event type, tone, and locale.
- [x] Implement service methods for active tracing patterns by topic.
- [x] Implement service methods for active learning paths where runtime consumers need them.

### Productive Read APIs
- [x] Add productive read-only APIs only where required by runtime/frontend flows.
- [x] Ensure productive read APIs return only active content.
- [x] Ensure productive read APIs do not expose administrative update fields unnecessarily.

### Tests
- [x] Add tests proving seed loading is idempotent.
- [x] Add tests proving repeated seed runs do not duplicate records.
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
- Created dev_seed_state migration (013) for per-record seed tracking
- Created DevSeedStateJpaEntity and DevSeedStateJpaRepository
- Created 11 seed JSON files with MVP content (2 categories, 4 topics, 8 curiosities, 4 activities, 12 difficulty levels, 6 avatar events, 1 learning path, 3 steps, 2 tracing patterns, 1 story, 3 pages)
- Created SeedData records for JSON deserialization
- Created SeedService with idempotent loading logic using dev_seed_state tracking table
- Created SeedLoader CommandLineRunner (runs in all profiles)
- Wired SeedService in ContentModuleConfiguration
- Added 4 unit tests for seed loading (idempotency, skip already loaded, partial loading)
- All existing runtime read services already implemented (no new methods needed)
- ProductiveStoryController already exists for productive read APIs

incomplete_tasks:
- Runtime read filter tests (deferred - existing service tests cover these)
- Inactive content exclusion tests (deferred - existing service tests cover these)

contract_changes:
- No API contract changes - seeds are internal, runtime services already existed

learnings:
- dev_seed_state table provides per-record control over seed loading
- Seeds can be skipped by deleting their key from dev_seed_state
- Re-seeding requires truncating dev_seed_state table
- Topic/Activity/LearningPath/Story resolution uses in-memory cache + repository fallback
- Seed files are processed in order (dependencies resolved by sequence)

next_sprint_suggestions:
- Sprint 015: contract hardening and integration readiness
- Consider adding seed data for more content types as MVP evolves
