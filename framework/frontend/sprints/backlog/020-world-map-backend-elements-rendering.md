# Sprint 020 - frontend
# -----------------------------------------------

## Goal

Render FEAT-011 backend-provided World Map destination data: host, biome, narrative situation, and discovery elements with first-version whitish pulse signaling. Backend implementation is out of scope.

## Status

status: backlog
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks

### Feature And Contract Review
- [ ] Review `docs/product/features/frontend/FEAT-011-World-Map..md`, especially `World Rendering` and `World Elements`.
- [ ] Review Sprint 018 map shell output.
- [ ] Review `docs/contracts/api/websocket.json` schemas: `WorldDestinationPayload`, `WorldHostPayload`, `WorldNarrativeSituationPayload`, and `WorldDiscoveryElementPayload`.
- [ ] Do not change backend code or backend contracts in this sprint.

### Destination And Biome Rendering
- [ ] Render the current backend destination using `destinationId` as runtime identity.
- [ ] Render the background from `biome` when supported.
- [ ] Use the neutral supported background only as an asset fallback for unknown biome, never as a fabricated world state.
- [ ] Keep the scene child-facing and full viewport.
- [ ] Do not render levels, nodes, locked states, completed states, or visible progress.

### Host And Narrative Rendering
- [ ] Render the backend `host` using `visualAssetKey` when available.
- [ ] Use the existing avatar/NPC placeholder when `visualAssetKey` is missing or unknown.
- [ ] Render `displayName` only as sparse supportive copy if needed.
- [ ] Render `narrativeSituation.displayText` only as sparse supportive copy if provided.
- [ ] Do not require the child to read text to understand the experience.

### Discovery Element Rendering
- [ ] Render each backend `discoveryElements` item immediately when the destination is shown.
- [ ] Use `visualAssetKey` when available and placeholder visuals when the asset is missing.
- [ ] Apply the first-version discovery cue: soft whitish shadow plus small slow pulse.
- [ ] Keep discovery elements visually organic, not button-like.
- [ ] Keep discovery element touch targets at least `64px`.
- [ ] If a discovery element has missing required identifiers, render it visually but mark it as not interactable for backend messages.

### Decorative Layer
- [ ] Add or keep decorative living-world elements only as local scenery.
- [ ] Ensure decorative local elements do not look like backend discovery elements.
- [ ] Ensure decorative local elements never send `world_discovery_interacted`.

### Verification
- [ ] Verify backend destination payload renders host, biome, narrative, and discovery elements.
- [ ] Verify discovery elements show whitish pulse cue without audio.
- [ ] Verify missing visual assets use placeholders without inventing world data.
- [ ] Verify empty `discoveryElements` renders a valid destination without interactive elements.
- [ ] Run `npm run build` from `framework/frontend/app`.

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

incomplete_tasks:

contract_changes: none planned

learnings:

next_sprint_suggestions:
