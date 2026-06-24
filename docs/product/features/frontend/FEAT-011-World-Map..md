# FEAT-011 - Frontend: World Map Discovery Walk

## Status

state: proposal
user_history: World Map as a backend-driven discovery walk for the child experience
depends_on: FEAT-007-Game-View-Shell, FEAT-010-Greetings-And-Farwell-Event, docs/product/features/backend/FEAT-008-World-Module.md, docs/contracts/api/openapi.json, docs/contracts/api/websocket.json
owned_by: frontend
scope: frontend implementation of the child-facing World Map for ages 3-4 inside GameView, driven by contracted backend world events over `/ws/game`. Includes loading while world state is requested, rendering valid backend world destinations, discovery element visual signaling, `world_heartbeat`, `world_discovery_interacted`, avatar/NPC placeholder support, animation state preparation, and a reusable generic child-safe error screen with backend-provided `GAME_AVATAR_EVENT` audio. No backend implementation, minigame rendering, LearningPath progression logic, activity selection logic, tracking interpretation, REST world integration, local fallback map, or contract changes are included.
test: GameView does not render a provisional map while loading; valid `WORLD_DESTINATION_READY` or `WORLD_STATE_SYNC` with `ACTIVE` destination renders the World Map; invalid/missing world state shows the generic child-safe error screen and plays backend-provided error audio at most once per error entry; discovery elements from backend show a whitish pulsing visual cue and send `world_discovery_interacted` only when their contracted runtime identifiers are present.

## Description

The World Map is the child's main screen and acts as the connection point between activities, exploration, and family accompaniment.

It must not feel like a level selector or a traditional progress map.

The child should feel that they are taking a walk with the avatar, discovering world elements and visiting friendly characters. Progress exists internally in the system, but it is presented as a narrative journey. The child never perceives it as progress.

FEAT-008 makes the backend `world` module the source of truth for runtime world decisions: destination, host, narrative situation, biome, discovery elements, activity proposal lifecycle, and engagement interpretation. This frontend feature renders those decisions when they arrive through the contracted Game WebSocket.

## Design Principles

### The Map Is A Walk, Not A Level List

The previous LearningPath tile-based design is discarded for this feature.

The World Map does not render nodes, `locked` / `completed` / `available` states, engine icons, or a visible progress path. Any visual representation that suggests a level selector is out of scope for this feature.

The child never sees:

- Numbered levels.
- Progress percentages.
- Level tiles.
- Unlock stars.
- Visible locked elements.

### Curiosity Before Instruction

The avatar does not give direct orders. Its role is to spark curiosity and support conversation between adult and child.

Avoid:

> Tap the cloud.

Prefer:

> That cloud looks interesting.

> I think that frog wants to show us something.

> What could be hidden there?

### The Adult Is Part Of The Experience

The application assumes family accompaniment as the preferred context. The avatar invites discovery, and the adult can naturally reinforce exploration.

The design does not depend exclusively on the adult. When the adult is not available, the avatar acts as a sufficient guide through combined visual and audio signals. The design encourages family presence but does not require it.

### The Child's Behavior Is Not Automatically Interpreted

Ignoring an element, leaving an activity, or not interacting during part of the walk is not interpreted as lack of ability, lack of interest, or learning difficulty.

These behaviors can come from many factors outside the content, such as distraction, tiredness, or family context at that moment. The system does not draw pedagogical conclusions from isolated signals.

Usage information is presented to the parent as descriptive observation, never as diagnosis. Interpretation belongs to the adult, not to the system.

## Contract Alignment

### REST API

`docs/contracts/api/openapi.json` does not define a child-facing World Map REST endpoint.

Frontend must not use `/api/v1/dev/content/*` endpoints to render the child World Map. Those endpoints are development/content administration APIs, not the runtime child experience.

### Game WebSocket

World Map runtime integration uses `/ws/game` from `docs/contracts/api/websocket.json`.

The frontend sends:

- `auth` with `childSessionId`, after opening `/ws/game`.
- `world_heartbeat`, only while world state is active and no minigame is active.
- `world_discovery_interacted`, only for backend-provided discovery elements with valid `proposalRuntimeId` and `discoveryElementId`.

The frontend receives and handles:

- `AUTH_ACK`.
- `GAME_AVATAR_EVENT`.
- `WORLD_DESTINATION_READY`.
- `WORLD_STATE_SYNC`.
- `WORLD_ACTIVITY_STARTED`.
- Existing terminal session events: `SESSION_EXPIRED`, `SESSION_INVALIDATED`, `CHILD_EXPELLED`, and `PARENT_BLOCK`.

The frontend must use the contractual `event` field for incoming WebSocket messages.

## World Loading Flow

GameView does not render a provisional World Map while waiting for backend world state.

Flow:

1. Child enters GameView through the FEAT-007 child session flow.
2. GameView shows a child-facing loading screen with the existing avatar placeholder image.
3. GameView opens `/ws/game`.
4. GameView sends the contracted `auth` message with `childSessionId`.
5. After `AUTH_ACK`, GameView waits for a valid backend world payload.
6. GameView renders the World Map only after receiving `WORLD_DESTINATION_READY` or `WORLD_STATE_SYNC` with `status: ACTIVE` and a valid `destination`.
7. If world loading fails, times out, closes, or receives invalid/no world state, GameView shows the generic child-safe error screen instead of rendering a fallback map.

The frontend does not fabricate biome, destination, host, narrative situation, discovery elements, activity availability, proposal runtime ids, or world progression while loading.

## World Rendering

The World Map is rendered only from backend-provided world payloads.

### Valid Payloads

The frontend can render from:

- `WORLD_DESTINATION_READY` payload using `WorldDestinationPayload`.
- `WORLD_STATE_SYNC` payload using `WorldStateSyncPayload` when `status` is `ACTIVE` and `destination` is present.

### Destination Rendering

The destination is the child's narrative anchor. It is rendered from:

- `destinationId`.
- `host`.
- `narrativeSituation`.
- `biome`.
- `discoveryElements`.

If `discoveryElements` is an empty array but the destination payload is otherwise valid, the frontend renders the destination without discovery elements.

If the destination itself is missing or invalid, the frontend does not render the map and enters the generic child-safe error screen.

### Host Rendering

The host is rendered from `WorldHostPayload`:

- `id` and `code` are runtime identifiers.
- `displayName` can be used as supportive copy when needed.
- `visualAssetKey` selects the host asset when available.

If `visualAssetKey` is missing or unknown, the frontend uses the existing avatar/NPC placeholder. Placeholders are allowed for missing assets, not for missing backend data.

### Narrative Situation Rendering

The narrative situation is rendered from `WorldNarrativeSituationPayload`:

- `code` identifies the situation.
- `displayText` can be shown as sparse supportive copy for the adult-child moment.
- `tone` can influence presentation styling only when supported.

The child must not be required to read text to understand the experience.

### Biome Rendering

The frontend uses `biome` as a visual skin key for the background layer.

For v1, the expected supported biome is the meadow/grass baseline. If a biome is unknown, the frontend may use a neutral supported background while preserving the backend destination, host, and discovery element data. This is an asset fallback, not a world-data fallback.

## World Elements

The world is composed of visual layers with distinct behavior and signaling.

### Layer 1: Living World

Purely decorative elements. They communicate life and movement in the scenery.

Behavior: they move by themselves in periodic and predictable ways. They do not react to the child's touch. They do not emit any interaction signal.

Examples: clouds, flowers, butterflies, birds, leaves.

Signaling: none. Their movement is environmental, not interactive.

Decorative elements may be local visual scenery, but they must never be reported to backend as discovery interaction and must never imply backend content that was not received.

### Layer 2: Simple Interactive Elements

Simple interactive elements can provide immediate local sensory feedback, but they do not start activities or minigames.

Behavior: no active prior signal. When the child touches them, they can provide an immediate, brief, satisfying reaction. The discovery belongs to the child, not to the system.

Examples:

- The frog jumps.
- The dog barks.
- The tree drops leaves.
- The cloud creates rain.

Signaling: very soft idle movement or none. No glow and no call-to-action animation. The avatar can comment on them occasionally when backend/avatar events support it, but not systematically.

Consequence: none on progression. They are immediate sensory rewards and must not be persisted by frontend.

### Layer 3: Discovery Elements

Discovery elements are backend-provided world elements that can represent entry points to activities.

They are rendered from `WorldDiscoveryElementPayload`:

- `proposalRuntimeId`.
- `discoveryElementId`.
- `code`.
- `displayName`.
- `elementType`.
- `visualAssetKey`.
- `interactionCueType`.
- `hasActivity`.

The frontend renders discovery elements immediately when they arrive in `WORLD_DESTINATION_READY` or active `WORLD_STATE_SYNC` payloads.

First version visual signal: a soft whitish shadow with a small, slow pulse. This signal is the primary channel and must be enough for the child to notice the element without audio.

Later versions can specialize the cue by `elementType`, `visualAssetKey`, or `interactionCueType`, but this feature only requires the generic whitish pulse.

Interaction: if the child touches a backend-provided discovery element with `hasActivity: true`, `proposalRuntimeId`, and `discoveryElementId`, the frontend sends `world_discovery_interacted` to backend.

If the element is missing required identifiers, the frontend can show it visually but must not send `world_discovery_interacted`.

## Visual Philosophy

### Avoid Traditional Video Game Appearance

Do not use:

- Giant objective arrows.
- Mission markers.
- HUD-like signs.
- Numbered tiles.
- Any element that communicates "you have to do something here".

### Organic Signaling

Interesting elements stand out through natural behavior: soft swaying, breathing glow, particular movement. They must not look like interface buttons.

### Visual Hierarchy Of The Three Layers

The difference between layers is perceptible but not explicit:

| Layer | Visual signal | Backend interaction |
|---|---|---|
| Living world | Periodic environmental movement | Never |
| Simple interactive | Soft idle movement or none | Never in v1 |
| Discovery element | Whitish shadow with small pulse | `world_discovery_interacted` when contracted identifiers are present |

The visual signal is always the main channel. Audio is reinforcement, never a dependency.

## World Interaction

### World Heartbeat

After the world is active, the frontend sends `world_heartbeat` periodically to keep the world session alive while the child explores the map.

The frontend starts `world_heartbeat` only after:

- WebSocket auth succeeded with `AUTH_ACK`.
- A valid active world payload was received.

The frontend stops `world_heartbeat` when:

- GameView unmounts.
- A terminal session event arrives.
- World loading or rendering enters the generic child-safe error screen.
- A minigame transition starts.
- The WebSocket closes.

### Discovery Interaction

When the child touches a discovery element from backend, the frontend sends:

```json
{
  "type": "world_discovery_interacted",
  "proposalRuntimeId": "runtime-id",
  "discoveryElementId": 123
}
```

The frontend sends this message only for discovery elements received from backend. It never sends this message for decorative or locally rendered simple interactive elements.

### Activity Started

When the frontend receives `WORLD_ACTIVITY_STARTED`, it must not attempt to render a minigame in this feature.

Until minigame rendering exists, the frontend should move to a safe transition/loading state using the avatar placeholder and avoid child-facing technical errors.

## Generic Child-Safe Error Screen

The generic child-safe error screen is a reusable frontend pattern for states where GameView cannot safely show the requested child experience.

In FEAT-011, it is used when the World Map cannot be loaded or rendered.

Future reuse includes portrait orientation handling when the device is rotated vertically.

### Triggers In This Feature

- World loading timeout.
- WebSocket closes before a valid world payload is received.
- `WORLD_STATE_SYNC` with `status: NO_WORLD_STATE`.
- `WORLD_STATE_SYNC` with `status: INACTIVE_CLOSED` when no terminal navigation is triggered.
- Missing or invalid destination payload.
- Missing required discovery identifiers when interaction would otherwise be sent.
- Any world payload shape that cannot be rendered safely.

### Visual Behavior

The screen shows:

- Existing avatar placeholder image.
- Calm child-safe layout.
- No technical error text.
- No provisional map.
- No child-facing retry pressure.
- Minimal visible copy through i18n only if product decides visible copy is needed.

### Error Audio

The error audio comes from backend as `GAME_AVATAR_EVENT`.

The frontend must not generate local audio, call TTS directly, or call Coqui directly.

The frontend plays the backend-provided error audio at most once per entry into the generic error state.

The same error audio must not replay on every heartbeat, `world_heartbeat`, reconnect attempt, duplicated error event, or repeated render of the same error state.

If audio is unavailable, late, blocked by autoplay, invalid, or playback fails, the visual error state remains sufficient.

## Frontend Animation Preparation

Although v1 can work with static placeholders, the frontend can introduce a `useAnimationStore` to centralize the visual state of the avatar/NPC.

The purpose of the store is to prepare the presentation architecture for future animations without introducing domain logic or a premature Lottie dependency.

### `useAnimationStore` Scope

The store can manage simple visual states:

- `idle`
- `waiting`
- `speaking`
- `curious`
- `celebrating`
- `transitioning`
- `error`

The store can expose presentation actions such as:

- Change the current visual context.
- Activate the speaking state while an avatar event is playing.
- Return to idle after an interaction ends.
- Enter an error state once when world loading fails.
- Interrupt a visual animation on navigation, system event, or new interaction.

### Restrictions

The `useAnimationStore` does not decide:

- Which destination to visit.
- Which host appears.
- Which activity a discovery element contains.
- Which phrase the avatar says.
- Whether the child is interested, tired, or blocked.
- How the LearningPath advances.

The store is a visual rendering tool, not a source of truth for the experience.

## Progression And LearningPath

### The LearningPath Is Invisible To The Child

Progression exists in the backend, but the child never perceives it as such. There are no tiles, no visible unlocks, and no progress indicators.

### Discovery Element Content

The type of minigame contained by a discovery element is not associated with the visual appearance of the element. The child cannot predict which activity is waiting. The element is always a surprise. This avoids teaching the child to skip specific activity types by recognizing the element that contains them.

### Ignored Elements

Ignoring a discovery element is not interpreted by frontend as a failure or a definitive rejection of the associated content.

Backend `world` and `tracking` own the proposal lifecycle and any `ActivityProposalLog` state. The frontend only sends the contracted interaction message when the child interacts with a backend-provided discovery element.

The frontend never shows child-facing labels such as `ignored`, `abandoned`, `low engagement`, or diagnostic equivalents.

### Parent Dashboard Data

Dashboard data is outside this frontend feature.

Backend/tracking can later expose descriptive information such as started, ignored, completed, and abandoned counts by engine type. The frontend child experience does not display this information.

## Combinatorial Situation System

Characters can repeat. Situations do not.

Backend `world` builds each visit dynamically by combining:

- Host character.
- Emotional state or narrative tone.
- Narrative situation.
- Object or discovery element.
- Biome.

The frontend receives the resolved combination as world payload and renders it. The frontend does not generate combinations locally.

## Host Interaction

Arrival at a host is not only a transition between destinations. Each host can have a small narrative microinteraction that reinforces their identity as a world character.

In this frontend feature, host interaction is limited to presentation:

- Render the host placeholder or asset.
- Render sparse supportive narrative copy when backend provides it.
- Use simple visual states from `useAnimationStore` when available.
- Keep the interaction calm and non-blocking.

Backend remains responsible for deciding when a destination is ready and when the next destination should be selected.

## Minigame Transition And Exit

No minigame is rendered in this feature.

When `WORLD_ACTIVITY_STARTED` arrives, the frontend must move into a safe transition/loading state and wait for a future minigame feature to own the actual engine rendering.

When future minigames exist, game completion or abandonment will return the child to the backend world flow. Backend `world` decides whether the LearningPath advances after the complete narrative arrival sequence, not immediately when `GAME_COMPLETED` is received.

## Behavior Without Adult Present

The avatar acts as a sufficient guide when the adult is not available. The primary visual signal on discovery elements allows the child to identify interesting elements even if the avatar is muted.

If the child does not react to an element, the scroll or world flow continues according to backend `world` decisions. There are no repeated calls for attention and no urgency animations.

## Frontend Responsibilities

The frontend scope of this feature is to present the backend-driven World Map experience as a narrative walk.

The frontend is responsible for:

- Entering GameView only through the existing FEAT-007 child session flow.
- Opening `/ws/game` and sending the contracted `auth` message.
- Keeping the loading screen visible until valid backend world state is received.
- Rendering the World Map only from `WORLD_DESTINATION_READY` or active `WORLD_STATE_SYNC` payloads.
- Validating world payloads before rendering.
- Rendering destination, host, narrative situation, biome, and discovery elements from backend payloads.
- Using visual placeholders only for missing/unknown assets, not for missing world data.
- Rendering backend discovery elements with a first-version whitish shadow and small pulse.
- Starting `world_heartbeat` only when world state is active.
- Stopping `world_heartbeat` on error, terminal events, unmount, WebSocket close, or minigame transition.
- Sending `world_discovery_interacted` only for backend discovery elements with valid `proposalRuntimeId`, `discoveryElementId`, and `hasActivity: true`.
- Showing the generic child-safe error screen when world cannot be loaded or rendered safely.
- Playing backend-provided `GAME_AVATAR_EVENT` error audio at most once per entry into the generic error state.
- Preparing `useAnimationStore` for avatar/NPC visual states.
- Reacting to contracted events without inferring the child's intention, ability, or interest.

The frontend must not:

- Render a provisional local World Map while waiting for backend state.
- Render a meadow fallback if backend world state is unavailable.
- Fabricate `destinationId`, `host`, `narrativeSituation`, `biome`, `discoveryElements`, `proposalRuntimeId`, `discoveryElementId`, `hasActivity`, or activity availability.
- Use `/api/v1/dev/content/*` endpoints to render the child World Map.
- Interpret ignored elements, abandoned activities, or lack of interaction as pedagogical signals.
- Decide LearningPath progression.
- Decide engine or activity priority.
- Persist engagement data on its own initiative.
- Show diagnostics, progress percentages, levels, unlocks, or locked states to the child.
- Replay error audio on every heartbeat, reconnect, duplicated event, or repeated render of the same error state.

## Backend Responsibilities

Backend responsibilities are owned by `docs/product/features/backend/FEAT-008-World-Module.md` and related modules.

Backend is responsible for:

- Deciding the current destination and next destination.
- Selecting host, narrative situation, object, biome, and associated activity.
- Emitting `WORLD_DESTINATION_READY`.
- Emitting `WORLD_STATE_SYNC`.
- Emitting `WORLD_ACTIVITY_STARTED` when a discovery interaction starts an activity.
- Defining and owning `WorldDestinationPayload`, `WorldHostPayload`, `WorldNarrativeSituationPayload`, and `WorldDiscoveryElementPayload`.
- Managing the LearningPath that remains invisible to the child.
- Deciding which engine or minigame each discovery element contains.
- Managing proposal lifecycle through tracking, including started and ignored outcomes.
- Managing consistent patterns of abandoned activities or engagement when applicable.
- Defining inactivity thresholds for world exploration.
- Recording started, ignored, abandoned, and completed activity data through the proper tracking ports.
- Providing descriptive data for the parent dashboard.
- Emitting backend-provided `GAME_AVATAR_EVENT` audio for the generic child-safe error state when applicable.
- Avoiding diagnoses or automatic pedagogical conclusions from isolated signals.

Backend must not send child-facing labels such as `ignored`, `abandoned`, `low engagement`, or diagnostic equivalents to the child World Map UI.

## Out Of Scope

- Backend implementation.
- Contract changes.
- REST world integration.
- Local fallback World Map rendering.
- Minigame engines.
- Starting games directly from frontend without backend world flow.
- Rendering real minigames after `WORLD_ACTIVITY_STARTED`.
- Dashboard tracking UI.
- Local engagement persistence.
- Lottie integration if no Lottie avatar assets are available yet.
- Final per-element visual cue specialization.

## Acceptance Criteria

- GameView shows the loading screen with the existing avatar placeholder while waiting for world state.
- GameView does not render a World Map during loading.
- GameView does not render a local fallback map if backend world state is unavailable.
- Valid `WORLD_DESTINATION_READY` renders the World Map.
- Valid `WORLD_STATE_SYNC` with `status: ACTIVE` and `destination` renders the World Map.
- `WORLD_STATE_SYNC` with `NO_WORLD_STATE` shows the generic child-safe error screen.
- `WORLD_STATE_SYNC` with `INACTIVE_CLOSED` shows the generic child-safe error screen unless a terminal navigation flow is triggered.
- Invalid or missing destination payload shows the generic child-safe error screen.
- The generic child-safe error screen shows the avatar placeholder and no technical child-facing error.
- Backend-provided `GAME_AVATAR_EVENT` error audio is played at most once per entry into the generic error state.
- Heartbeats, world heartbeats, reconnect attempts, duplicated errors, or repeated renders do not replay the same error audio.
- `world_heartbeat` starts only after a valid active world payload is received.
- `world_heartbeat` stops on unmount, terminal events, WebSocket close, generic error state, or minigame transition.
- Backend discovery elements are rendered immediately with a whitish shadow and small pulse.
- `world_discovery_interacted` is sent only for backend-provided discovery elements with valid `proposalRuntimeId`, `discoveryElementId`, and `hasActivity: true`.
- `WORLD_ACTIVITY_STARTED` moves the UI to a safe transition/loading state and does not attempt to render a minigame.
- Existing terminal session events still close the socket, clear child session state, and navigate Home according to FEAT-007 behavior.
- No level tiles, nodes, numbered levels, progress percentages, unlock stars, engine icons, child-facing diagnostics, or locked states are shown.

## Emotional Goal

The child must not feel that they are advancing through levels.

They must feel that they are exploring the world with their companion and visiting friends.
