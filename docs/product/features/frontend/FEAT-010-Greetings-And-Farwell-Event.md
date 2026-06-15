# FEAT-010 - Frontend: Greetings And Farewell Game Avatar Events

## Status

state: proposal
user_history: Receive greeting and farewell avatar events in GameView
depends_on: FEAT-007-Game-View-Shell, FEAT-008-Child-Section, docs/contracts/api/openapi.json, docs/contracts/api/websocket.json
owned_by: frontend
scope: GameView-only reception and playback of contracted GAME_AVATAR_EVENT lifecycle events. No global WebSocket listener, backend changes, or contract changes are included.
test: GameView receives SESSION_CONNECTED and SESSION_DISCONNECTED avatar events, plays correlated MP3 binary audio when available, falls back after timeout or playback failure, and preserves normal session terminal navigation.

## Description

This feature implements reception of contracted `GAME_AVATAR_EVENT` lifecycle messages inside `GameView`.

The frontend must follow `docs/contracts/api/websocket.json` as the source of truth. `GAME_AVATAR_EVENT` is sent as a JSON metadata frame and may be followed by a binary audio frame correlated by `audioId`.

Supported lifecycle subtypes:

- `SESSION_CONNECTED`: greeting event. It is received after successful game WebSocket authentication. The audio is played during the splash/preparing state before entering the map world.
- `SESSION_DISCONNECTED`: farewell event. It is received before a backend-initiated session close when delivery is possible. The audio is played inside `GameView`; after playback, timeout, or failure, the normal terminal behavior continues.

## Contract Rules

The frontend must use the `event` field from `docs/contracts/api/websocket.json`, not `type`.

Expected metadata shape:

```json
{
  "event": "GAME_AVATAR_EVENT",
  "sessionId": 123,
  "eventType": "SESSION_CONNECTED",
  "audioAvailable": true,
  "audioId": "uuid-correlation-id",
  "text": "Hola, vamos a jugar!"
}
```

When `audioAvailable=true`, the following binary frame must be parsed according to the contract:

```text
[4 bytes: audioId length as big-endian int32][N bytes: audioId UTF-8][remaining bytes: MP3 audio data]
```

The decoded `audioId` must match the metadata `audioId` before playback.

## Product Rules

- This feature applies only while the user is inside `GameView`.
- No global WebSocket connection is introduced.
- Greeting audio must not block indefinitely.
- Farewell audio must not block terminal navigation indefinitely.
- If audio is unavailable, late, invalid, uncorrelated, or blocked by the browser, continue the normal flow.
- If `audioAvailable=false`, use the normal flow immediately.
- If `audioAvailable=true`, wait up to `3_000 ms` for the correlated binary audio frame.
- If playback fails or autoplay is blocked, continue as if no audio was available.
- Terminal session events remain: `SESSION_EXPIRED`, `SESSION_INVALIDATED`, `CHILD_EXPELLED`, and `PARENT_BLOCK`.

## UX Flow

### Greeting

- GameView opens `/ws/game`.
- Frontend sends the contracted auth message.
- Backend sends `AUTH_ACK`.
- Frontend starts heartbeat.
- Backend sends `GAME_AVATAR_EVENT` with `eventType: SESSION_CONNECTED`.
- If audio is available, frontend waits for the correlated binary MP3 frame for up to 3 seconds.
- Frontend plays the MP3 audio when available and valid.
- After playback, timeout, or failure, GameView enters the map world.

### Farewell

- Backend sends `GAME_AVATAR_EVENT` with `eventType: SESSION_DISCONNECTED` when possible.
- If audio is available, frontend waits for the correlated binary MP3 frame for up to 3 seconds.
- Frontend plays the MP3 audio when available and valid.
- After playback, timeout, or failure, the normal terminal behavior continues.
- If a terminal event arrives without a prior farewell event, GameView follows the existing terminal behavior immediately.

## Implementation Notes

- Set `ws.binaryType = 'arraybuffer'`.
- JSON WebSocket messages are parsed as metadata/session events.
- Binary WebSocket messages are parsed as contracted `BinaryAudioFrame`.
- MP3 playback should use a `Blob` with `type: audio/mpeg`.
- Object URLs must be revoked after playback or failure.
- Heartbeat cleanup and WebSocket cleanup must remain unchanged.
