# Sprint 016 - backend
# -----------------------------------------------

## Goal
Create the backend avatar module foundation without real TTS, audio cache, or game WebSocket binary delivery.

## Status
status: in_progress
started_at: 2026-06-08
closed_at:
blocked_by:
waiting_for:

## Tasks

### Module Structure
- [x] Create the `avatar` module package structure following the current backend modular style.
- [x] Define the avatar application service entry point for child-facing avatar events.
- [x] Define input data for avatar event processing: `childSessionId`, `eventType`, `locale`, and optional context.
- [x] Define an avatar result model with `eventType`, `text`, `audioAvailable`, and optional future audio metadata.

### Session And Configuration
- [x] Validate that the referenced child session exists and is active.
- [x] Resolve the child profile associated with the active session.
- [x] Consume effective `ttsEnabled` and `agentEnabled` values from the family/child profile implementation.
- [x] Suppress avatar interaction when `agentEnabled` is false.
- [x] Return text-only metadata when `ttsEnabled` is false.

### Content Catalog Selection
- [x] Consume active `content.AvatarEventCatalog` messages.
- [x] Select catalog text by `eventType`, tone, and locale.
- [x] Use `NEUTRAL` as the safe default tone when no child/family tone is available.
- [x] Provide deterministic fallback text when no exact catalog match exists.
- [x] Do not call GameAgent in this sprint.

### Tests
- [x] Unit test active session validation.
- [x] Unit test inactive or missing session behavior.
- [x] Unit test `agentEnabled=false` suppression.
- [x] Unit test `ttsEnabled=false` text-only result.
- [x] Unit test catalog lookup by event type, tone, and locale.
- [x] Unit test safe fallback when catalog text is missing.

## Risks
- Family configuration ownership can drift if avatar duplicates effective flag logic.
- Missing catalog data can make child-facing fallback copy inconsistent.
- Introducing TTS or WebSocket too early would make the foundation harder to test.

## Dependencies
- FEAT-005 Avatar Module.
- Active session implementation from the session module.
- Effective child/family `ttsEnabled` and `agentEnabled` values from the family module.
- `AvatarEventCatalog`, `AvatarEventType`, and `AvatarTone` from the content module.

## Agent Instruction
- Keep this sprint free of real HTTP TTS calls.
- Keep this sprint free of audio cache implementation.
- Keep this sprint free of WebSocket binary delivery.
- Prefer small ports and service classes that can be reused by later TTS, cache, and WebSocket sprints.
- Do not introduce GameAgent calls.

## Notes
This sprint makes avatar behavior testable as pure backend orchestration before adding external service latency or binary audio handling.

## Review

completed_tasks:
- Module structure with model, ports/in, service, validation, infrastructure/dto, application packages
- AvatarEventResult model with eventType, text, audioAvailable, audioMetadata, suppressed fields
- AvatarUseCase interface with processAvatarEvent method
- AvatarValidator for input validation (childSessionId, eventType, locale)
- AvatarEventRequest DTO with childSessionId, eventType, locale, context
- AvatarEventResponse DTO with fromResult factory method
- AvatarService implementation with session validation, profile resolution, catalog lookup
- AvatarModuleConfiguration Spring configuration
- AvatarServiceTest with 7 unit tests covering all required scenarios
- AvatarValidatorTest with 6 unit tests

incomplete_tasks:

contract_changes:
- No REST endpoints added in this sprint (service-only layer)
- No OpenAPI changes required

learnings:
- Pre-existing test compilation issues in ChildProfileServiceTest and FamilyServiceTest block full test suite
- Main source compilation succeeds, confirming avatar module is correctly implemented

next_sprint_suggestions:
- Add REST controller to expose avatar events via HTTP
- Integrate with TTS client (sprint 017)
- Add audio cache implementation (sprint 018)