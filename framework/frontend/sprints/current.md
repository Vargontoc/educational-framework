# Sprint 016 - frontend
# -----------------------------------------------

## Goal

Implement FEAT-010: receive and play contracted greeting and farewell `GAME_AVATAR_EVENT` lifecycle audio inside `GameView`.

## Status

status: ready
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

- Update `GameView` WebSocket handling to support `GAME_AVATAR_EVENT` using the contractual `event` field from `docs/contracts/api/websocket.json`.
- Handle `SESSION_CONNECTED` greeting events during the splash/preparing state before entering the map world.
- Handle `SESSION_DISCONNECTED` farewell events inside `GameView` before continuing terminal session behavior when possible.
- Set the game WebSocket `binaryType` to `arraybuffer`.
- Parse contracted `BinaryAudioFrame` payloads: `[4 bytes audioId length][audioId UTF-8][MP3 bytes]`.
- Correlate binary audio frames with metadata through `audioId` before playback.
- Play valid MP3 audio using a `Blob` with `type: audio/mpeg`.
- Continue the normal flow if audio is unavailable, invalid, uncorrelated, late, blocked by autoplay, or playback fails.
- Enforce a `3_000 ms` maximum wait for correlated binary audio when `audioAvailable=true`.
- Preserve existing heartbeat, WebSocket cleanup, and terminal event handling for `SESSION_EXPIRED`, `SESSION_INVALIDATED`, `CHILD_EXPELLED`, and `PARENT_BLOCK`.
- Run the frontend build after implementation.

## Risks

- Browser autoplay policy may reject playback; implementation must continue normal navigation without blocking.
- Binary frame parsing must match `docs/contracts/api/websocket.json` exactly to avoid dropped audio.
- Farewell delivery is best-effort and may not arrive before a terminal session event.
- Terminal navigation must not be delayed indefinitely by missing or failed audio.
- Object URLs created for audio playback must be revoked to avoid memory leaks.

## Dependencies

- `docs/product/features/frontend/FEAT-010-Greetings-And-Farwell-Event.md`
- `docs/contracts/api/websocket.json`
- `docs/contracts/api/openapi.json`
- FEAT-007 Game View Shell
- FEAT-008 Child Section

## Agent Instruction

- Work only in the frontend layer unless an explicit contract issue is found and reported.
- Do not change backend code or WebSocket contracts for this sprint.
- Follow `docs/contracts/api/websocket.json` as the source of truth.
- Implement this feature only inside `GameView`; do not introduce a global WebSocket listener.
- Use the contractual metadata field `event`, not `type`, for incoming `GAME_AVATAR_EVENT` messages.
- Keep child session state in memory only.
- Do not persist audio, child session data, or auth tokens in localStorage or sessionStorage.
- Keep all code and comments in English.
- Avoid child-facing technical error messages.

## Notes

- Greeting event: `GAME_AVATAR_EVENT` with `eventType: SESSION_CONNECTED`.
- Farewell event: `GAME_AVATAR_EVENT` with `eventType: SESSION_DISCONNECTED`.
- Binary audio timeout: `3_000 ms`.
- If `audioAvailable=false`, continue immediately without waiting for a binary frame.

## Review

completed_tasks:

incomplete_tasks:

contract_changes: none planned

learnings:

next_sprint_suggestions:
