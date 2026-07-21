# Sprint 019 - backend
# -----------------------------------------------

## Goal
Add a reusable storyteller synthesis use case that uses the same TTS endpoint as avatar/NPC speech with `voice_profile: "storyteller"`.

## Status
status: completed
started_at: 2026-06-10
closed_at: 2026-06-10
blocked_by:
waiting_for:

## Tasks

### Storyteller Use Case
- [x] Add an avatar-owned use case for narration/storyteller audio generation.
- [x] Accept direct narration text, locale, and optional tone.
- [x] Send `voice_profile: "storyteller"` to `tts-educational`.
- [x] Reuse the same TTS client introduced for avatar/NPC speech.
- [x] Reuse the avatar audio cache with `voiceProfile` in the cache key.
- [x] Return text fallback metadata when audio is disabled, unavailable, or TTS fails.

### Boundaries
- [x] Do not implement the full reading module in this sprint.
- [x] Do not require `AvatarEventCatalog` for direct narration text.
- [x] Do not introduce GameAgent or AdultAgent calls.
- [x] Keep narration TTS owned by avatar as the generated speech gateway.

### Validation
- [x] Reject blank narration text.
- [x] Apply any TTS text length limits documented in the contract, or document the pending limit if not available. (TTS service owns validation — backend is bridge)
- [x] Use safe locale defaults aligned with content/family defaults.
- [x] Use safe tone defaults aligned with avatar behavior.

### Tests
- [x] Unit test storyteller request uses `voice_profile: "storyteller"`.
- [x] Unit test narration does not require `AvatarEventCatalog`.
- [x] Unit test storyteller cache key differs from `npc` for the same text.
- [x] Unit test blank text validation.
- [x] Unit test TTS failure returns text fallback.
- [x] Unit test `ttsEnabled=false` returns text-only result.

## Risks
- Storyteller behavior can become coupled to reading implementation too early.
- Long narration text can exceed TTS latency or request-size budgets.
- Cache keys can collide with NPC speech if `voiceProfile` is omitted.

## Dependencies
- Sprint 017 TTS client and voice profiles.
- Sprint 018 avatar audio cache.
- Future reading module integration.

## Agent Instruction
- Keep this as a reusable backend audio use case, not a reading module implementation.
- Use the same endpoint and client as NPC/avatar speech.
- Keep `storyteller` semantics explicit in tests.
- Do not introduce WebSocket delivery in this sprint.

## Notes
This sprint captures the design decision that avatar owns generated child-facing speech beyond game NPC feedback.

## Review

completed_tasks:
- Created NarrateStorytellerUseCase (inbound port interface)
- Created NarrateStorytellerRequest DTO (text, locale, tone with defaults)
- Created StorytellerResult domain class (text, audioAvailable, audioMetadata, audioData)
- Created StorytellerResponse DTO (fromResult excludes audioData)
- Created NarrateStorytellerValidator (rejects blank text only)
- Created NarrateStorytellerService (voiceProfile="storyteller", text fallback on TTS failure)
- Created NarrateStorytellerServiceTest (6 tests)
- Modified AvatarModuleConfiguration to wire NarrateStorytellerUseCase bean
- All 41 avatar tests pass

incomplete_tasks:

contract_changes:
- No contract changes — backend acts as bridge to TTS service

learnings:
- TTS text length validation is owned by the TTS service, not the backend
- Backend passes text through and handles TtsException for text fallback
- Locale default "es" and tone default NEUTRAL align with existing avatar behavior

next_sprint_suggestions:
- Sprint 020: Avatar session lifecycle audio delivery via WebSocket