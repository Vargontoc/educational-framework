# Sprint 020 - frontend
# -----------------------------------------------

## Goal

Render FEAT-011 backend-provided World Map destination data: host, biome, narrative situation, and discovery elements with first-version whitish pulse signaling. Backend implementation is out of scope.

## Status

status: completed
started_at: 2026-06-26
closed_at: 2026-06-26
blocked_by:
waiting_for:

## Tasks

### Feature And Contract Review
- [x] Review `docs/product/features/frontend/FEAT-011-World-Map..md`, especially `World Rendering` and `World Elements`.
- [x] Review Sprint 018 map shell output.
- [x] Review `docs/contracts/api/websocket.json` schemas: `WorldDestinationPayload`, `WorldHostPayload`, `WorldNarrativeSituationPayload`, and `WorldDiscoveryElementPayload`.
- [x] Do not change backend code or backend contracts in this sprint.

### Destination And Biome Rendering
- [x] Render the current backend destination using `destinationId` as runtime identity.
- [x] Render the background from `biome` when supported.
- [x] Use the neutral supported background only as an asset fallback for unknown biome, never as a fabricated world state.
- [x] Keep the scene child-facing and full viewport.
- [x] Do not render levels, nodes, locked states, completed states, or visible progress.

### Host And Narrative Rendering
- [x] Render the backend `host` using `visualAssetKey` when available.
- [x] Use the existing avatar/NPC placeholder when `visualAssetKey` is missing or unknown.
- [x] Render `displayName` only as sparse supportive copy if needed. (Skipped per clarification - no text for 3-4 years old)
- [x] Render `narrativeSituation.displayText` only as sparse supportive copy if provided. (Skipped per clarification - no text for 3-4 years old)
- [x] Do not require the child to read text to understand the experience.

### Discovery Element Rendering
- [x] Render each backend `discoveryElements` item immediately when the destination is shown.
- [x] Use `visualAssetKey` when available and placeholder visuals when the asset is missing.
- [x] Apply the first-version discovery cue: soft whitish shadow plus small slow pulse.
- [x] Keep discovery elements visually organic, not button-like.
- [x] Keep discovery element touch targets at least `64px`.
- [x] If a discovery element has missing required identifiers, render it visually but mark it as not interactable for backend messages.

### Decorative Layer
- [x] Add or keep decorative living-world elements only as local scenery.
- [x] Ensure decorative local elements do not look like backend discovery elements.
- [x] Ensure decorative local elements never send `world_discovery_interacted`.

### Verification
- [x] Verify backend destination payload renders host, biome, narrative, and discovery elements.
- [x] Verify discovery elements show whitish pulse cue without audio.
- [x] Verify missing visual assets use placeholders without inventing world data.
- [x] Verify empty `discoveryElements` renders a valid destination without interactive elements.
- [x] Run `npm run build` from `framework/frontend/app`.

## Risks

- **Asset fallback confusion**: fallback visuals could become fallback world data.
  Mitigation: placeholders only replace missing visual assets; required world data is still required.
- **Button-like discovery elements**: visual cue may look like UI controls.
  Mitigation: use organic pulse and shadow, no button chrome.
- **Local element leakage**: decorative elements could accidentally emit backend interaction messages.
  Mitigation: only backend discovery elements can send `world_discovery_interacted`.

## Dependencies

- Sprint 018 - backend world loading and render shell.
- `docs/product/features/frontend/FEAT-011-World-Map..md` - source feature.
- `docs/contracts/api/websocket.json` - world payload schemas.

## Agent Instruction

- Work only in the frontend layer.
- Do not implement backend changes.
- Do not change `docs/contracts`.
- Do not fabricate destination, host, biome, narrative situation, discovery elements, or proposal ids.
- Do not call REST content/dev endpoints for child World Map rendering.
- Keep all code, comments, and documentation in English.

## Notes

- Per-element specialized cues are future work; this sprint only requires the generic whitish pulse.

## Review

completed_tasks:
- Review FEAT-011-World-Map..md
- Review Sprint 018 map shell output
- Review websocket.json schemas
- Add getAssetUrl() helper for bundled assets
- Add getBiomeClass() helper for biome-based theming
- Add biome CSS variants: meadow (default), forest, beach, mountain
- Update .game-view to use biome class instead of hardcoded gradient
- Enhance discovery element to show asset image when visualAssetKey available
- Add .game-view__discovery--with-asset and .game-view__discovery-img CSS
- Add decorative butterflies with float animation (Layer 1 living world)
- Add decorative flowers in grass area (Layer 1 living world)
- Verify npm run build passes

incomplete_tasks:

contract_changes: none

learnings:
- Biome class switching allows dynamic background without hardcoded gradient
- Asset key uses bundled asset path: /assets/${assetKey}
- Discovery element with asset shows image with whitish pulse overlay
- Discovery element without asset shows generic whitish pulse circle
- Decorative elements (butterflies, flowers) are purely CSS, no interaction
- No narrative text per clarification - game is visual/auditory only for 3-4 years old

next_sprint_suggestions:
- FEAT-011 continues: add host interaction micro-animations
- FEAT-011 continues: add per-element cue specialization by elementType
- FEAT-011 continues: add Layer 2 simple interactive elements

## Implementation Details

### Files Modified

#### src/views/GameView.vue
- Added getAssetUrl(assetKey) helper: returns `/assets/${assetKey}`
- Added getBiomeClass(biome) helper: returns biome-specific class
- Added :class="getBiomeClass(worldState?.biome)" to main game-view
- Updated discovery element to show img when visualAssetKey available
- Added decorative butterflies and flowers to template

### New CSS Classes

#### Biome backgrounds
- .game-view--biome-meadow (default): sky to grass gradient
- .game-view--biome-forest: green tones
- .game-view--biome-beach: sky to sand gradient
- .game-view--biome-mountain: gray tones

#### Discovery element
- .game-view__discovery--with-asset: transparent background when asset shown
- .game-view__discovery-img: asset image with pulse animation overlay

#### Decorative Layer 1 elements
- .game-view__butterfly: floating butterfly elements
- .game-view__butterfly--one, --two: positioned with different animations
- .game-view__flower: flower elements in grass
- .game-view__flower--one, --two, --three: positioned in grass area

### Build Verification
- npm run build: passed
- GameView CSS: 5.78 kB (up from 3.82 kB)
- GameView JS: 8.59 kB (up from 7.48 kB)
