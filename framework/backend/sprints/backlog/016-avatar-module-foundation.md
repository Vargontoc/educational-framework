# Sprint 016 - backend
# -----------------------------------------------

## Goal
Create the backend avatar module foundation without real TTS, audio cache, or game WebSocket binary delivery.

## Status
status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Module Structure
- [ ] Create the `avatar` module package structure following the current backend modular style.
- [ ] Define the avatar application service entry point for child-facing avatar events.
- [ ] Define input data for avatar event processing: `childSessionId`, `eventType`, `locale`, and optional context.
- [ ] Define an avatar result model with `eventType`, `text`, `audioAvailable`, and optional future audio metadata.

### Session And Configuration
- [ ] Validate that the referenced child session exists and is active.
- [ ] Resolve the child profile associated with the active session.
- [ ] Consume effective `ttsEnabled` and `agentEnabled` values from the family/child profile implementation.
- [ ] Suppress avatar interaction when `agentEnabled` is false.
- [ ] Return text-only metadata when `ttsEnabled` is false.

### Content Catalog Selection
- [ ] Consume active `content.AvatarEventCatalog` messages.
- [ ] Select catalog text by `eventType`, tone, and locale.
- [ ] Use `NEUTRAL` as the safe default tone when no child/family tone is available.
- [ ] Provide deterministic fallback text when no exact catalog match exists.
- [ ] Do not call GameAgent in this sprint.

### Tests
- [ ] Unit test active session validation.
- [ ] Unit test inactive or missing session behavior.
- [ ] Unit test `agentEnabled=false` suppression.
- [ ] Unit test `ttsEnabled=false` text-only result.
- [ ] Unit test catalog lookup by event type, tone, and locale.
- [ ] Unit test safe fallback when catalog text is missing.

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

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
