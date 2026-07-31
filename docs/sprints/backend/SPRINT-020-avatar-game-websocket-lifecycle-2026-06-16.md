# Sprint 020 - backend
# -----------------------------------------------

## Goal
Deliver the first real avatar audio over the native game WebSocket using existing connection/authentication and controlled session lifecycle events.

## Status
status: closed
started_at: 2026-06-16
closed_at: 2026-06-16
blocked_by:
waiting_for:

## Tasks

### Lifecycle Events
- [x] Emit a `GAME_AVATAR_EVENT` after successful `/ws/game` authentication.
- [x] Use welcome text: `Hola, vamos a jugar!`.
- [x] Emit a controlled farewell `GAME_AVATAR_EVENT` before closing the socket when the backend initiates a close, expulsion, timeout, or equivalent controlled session end.
- [x] Use farewell text: `Vaya, parece que es hora de despedirnos. Hasta la proxima.`.
- [x] Do not guarantee farewell delivery when the client abruptly disconnects before the backend can send.

### WebSocket Payload
- [x] Add `GAME_AVATAR_EVENT` to the native game WebSocket server-to-client event model.
- [x] Send metadata as JSON with `eventType`, `audioAvailable`, `audioId` when available, and fallback `text`.
- [x] Use lifecycle event types such as `SESSION_CONNECTED` and `SESSION_DISCONNECTED` for this sprint.
- [x] Send MP3 binary audio correlated by `audioId` when audio is available.
- [x] Keep the binary format small and deterministic for frontend parsing.
- [x] Continue using text fallback when audio is unavailable, disabled, late, or dropped.

### Avatar Integration
- [x] Generate welcome and farewell audio through the avatar service.
- [x] Use `voice_profile: "npc"`.
- [x] Use safe default tone `NEUTRAL`, mapped to TTS `calm`.
- [x] Respect `agentEnabled`; suppress lifecycle avatar messages when disabled.
- [x] Respect `ttsEnabled`; emit text-only metadata when disabled.
- [x] Use cache if Sprint 018 is already implemented.

### Contract Documentation
- [x] Update `docs/contracts/api/websocket.json` with the lifecycle subset of `GAME_AVATAR_EVENT`.
- [x] Document that future minigame events will reuse the same event family and binary correlation strategy.
- [x] Keep parental STOMP out of avatar audio delivery.

### Tests
- [x] Unit test successful auth triggers welcome avatar generation.
- [x] Unit test welcome metadata uses `GAME_AVATAR_EVENT`.
- [x] Unit test welcome audio uses `voice_profile: "npc"`.
- [x] Unit test `ttsEnabled=false` sends welcome text-only metadata.
- [x] Unit test `agentEnabled=false` suppresses welcome avatar message.
- [x] Unit test controlled backend close attempts farewell before closing.
- [x] Unit test abrupt client disconnect does not fail when farewell cannot be sent.
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
- All lifecycle events implemented via `GameWebSocketHandler.sendWelcomeAvatar()` and `sendFarewellAndClose()`
- `GameAvatarEvent` DTO with `SESSION_CONNECTED` and `SESSION_DISCONNECTED` event types
- Binary frame format `[4 bytes length][audioId][MP3 data]` implemented in `sendBinaryFrame()`
- Avatar integration using `AvatarLifecycleService` with NPC voice profile and NEUTRAL tone
- `agentEnabled` and `ttsEnabled` flags properly respected
- Sprint 018 cache (CachingTtsClient) integrated
- Contract documented in `websocket.json` v1.2.0
- Unit tests for all avatar lifecycle scenarios in `AvatarLifecycleServiceTest`
- New unit tests added for GAME_AVATAR_EVENT field verification and binary frame format

incomplete_tasks:
- Integration tests for WebSocket with actual socket connection (requires Docker/Testcontainers setup complexity)

contract_changes:
- `websocket.json` updated to v1.2.0 with `GAME_AVATAR_EVENT` definition

learnings:
- Binary frame format requires careful byte handling for audioId correlation
- Unit tests with mocks provide sufficient coverage for WebSocket handler behavior
- Integration tests for native WebSocket require complex setup with real socket connections

next_sprint_suggestions:
- Add integration tests for WebSocket avatar lifecycle when Docker environment is available
- Implement minigame event triggers (activity completion, help requests) in next sprint
- Consider adding WebSocket session timeout configuration for avatar audio generation
