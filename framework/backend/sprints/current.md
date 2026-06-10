# Sprint 020 - backend
# -----------------------------------------------

## Goal
Deliver the first real avatar audio over the native game WebSocket using existing connection/authentication and controlled session lifecycle events.

## Status
status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Lifecycle Events
- [ ] Emit a `GAME_AVATAR_EVENT` after successful `/ws/game` authentication.
- [ ] Use welcome text: `Hola, vamos a jugar!`.
- [ ] Emit a controlled farewell `GAME_AVATAR_EVENT` before closing the socket when the backend initiates a close, expulsion, timeout, or equivalent controlled session end.
- [ ] Use farewell text: `Vaya, parece que es hora de despedirnos. Hasta la proxima.`.
- [ ] Do not guarantee farewell delivery when the client abruptly disconnects before the backend can send.

### WebSocket Payload
- [ ] Add `GAME_AVATAR_EVENT` to the native game WebSocket server-to-client event model.
- [ ] Send metadata as JSON with `eventType`, `audioAvailable`, `audioId` when available, and fallback `text`.
- [ ] Use lifecycle event types such as `SESSION_CONNECTED` and `SESSION_DISCONNECTED` for this sprint.
- [ ] Send MP3 binary audio correlated by `audioId` when audio is available.
- [ ] Keep the binary format small and deterministic for frontend parsing.
- [ ] Continue using text fallback when audio is unavailable, disabled, late, or dropped.

### Avatar Integration
- [ ] Generate welcome and farewell audio through the avatar service.
- [ ] Use `voice_profile: "npc"`.
- [ ] Use safe default tone `NEUTRAL`, mapped to TTS `calm`.
- [ ] Respect `agentEnabled`; suppress lifecycle avatar messages when disabled.
- [ ] Respect `ttsEnabled`; emit text-only metadata when disabled.
- [ ] Use cache if Sprint 018 is already implemented.

### Contract Documentation
- [ ] Update `docs/contracts/api/websocket.json` with the lifecycle subset of `GAME_AVATAR_EVENT`.
- [ ] Document that future minigame events will reuse the same event family and binary correlation strategy.
- [ ] Keep parental STOMP out of avatar audio delivery.

### Tests
- [ ] Unit test successful auth triggers welcome avatar generation.
- [ ] Unit test welcome metadata uses `GAME_AVATAR_EVENT`.
- [ ] Unit test welcome audio uses `voice_profile: "npc"`.
- [ ] Unit test `ttsEnabled=false` sends welcome text-only metadata.
- [ ] Unit test `agentEnabled=false` suppresses welcome avatar message.
- [ ] Unit test controlled backend close attempts farewell before closing.
- [ ] Unit test abrupt client disconnect does not fail when farewell cannot be sent.
- [ ] Integration test native game WebSocket auth receives avatar welcome metadata where practical.
- [ ] Integration test binary audio correlation by `audioId` where practical.

## Risks
- WebSocket binary delivery can become hard to parse if metadata and bytes are not clearly correlated.
- Farewell cannot be guaranteed on abrupt client disconnects.
- Introducing lifecycle avatar delivery can expose frontend contract drift earlier than minigame implementation.
- Audio generation on auth can add perceived connection latency if not timeout-bounded.

## Dependencies
- Sprint 016 avatar module foundation.
- Sprint 017 TTS client and voice profiles.
- Sprint 018 avatar audio cache, if cache-backed delivery is desired.
- Existing native `/ws/game` authentication flow.
- `docs/contracts/api/websocket.json` update in this sprint.

## Agent Instruction
- Use `GAME_AVATAR_EVENT` now as the base event for lifecycle avatar audio.
- Limit this sprint to connection/authentication and controlled disconnection/session-end events.
- Do not implement minigame event triggers such as activity completion or help requests.
- Keep fallback behavior text-first and non-blocking.
- Make abrupt disconnect behavior best-effort, not a hard guarantee.

## Notes
This sprint creates a real end-to-end test path for avatar audio before backend minigame engines exist.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions: