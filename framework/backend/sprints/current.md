# Sprint 019 - backend
# -----------------------------------------------

## Goal
Add a reusable storyteller synthesis use case that uses the same TTS endpoint as avatar/NPC speech with `voice_profile: "storyteller"`.

## Status
status: in_progress
started_at: 2026-06-10
closed_at:
blocked_by:
waiting_for:

## Tasks

### Storyteller Use Case
- [ ] Add an avatar-owned use case for narration/storyteller audio generation.
- [ ] Accept direct narration text, locale, and optional tone.
- [ ] Send `voice_profile: "storyteller"` to `tts-educational`.
- [ ] Reuse the same TTS client introduced for avatar/NPC speech.
- [ ] Reuse the avatar audio cache with `voiceProfile` in the cache key.
- [ ] Return text fallback metadata when audio is disabled, unavailable, or TTS fails.

### Boundaries
- [ ] Do not implement the full reading module in this sprint.
- [ ] Do not require `AvatarEventCatalog` for direct narration text.
- [ ] Do not introduce GameAgent or AdultAgent calls.
- [ ] Keep narration TTS owned by avatar as the generated speech gateway.

### Validation
- [ ] Reject blank narration text.
- [ ] Apply any TTS text length limits documented in the contract, or document the pending limit if not available.
- [ ] Use safe locale defaults aligned with content/family defaults.
- [ ] Use safe tone defaults aligned with avatar behavior.

### Tests
- [ ] Unit test storyteller request uses `voice_profile: "storyteller"`.
- [ ] Unit test narration does not require `AvatarEventCatalog`.
- [ ] Unit test storyteller cache key differs from `npc` for the same text.
- [ ] Unit test blank text validation.
- [ ] Unit test TTS failure returns text fallback.
- [ ] Unit test `ttsEnabled=false` returns text-only result.

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

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions: