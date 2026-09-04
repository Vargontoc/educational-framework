# Sprint 072 - backend
# -----------------------------------------------

## Goal
Make `world` provide recognition launch context for animal activities without coupling `RecognitionEngine` to `world`.

## Status
status: completed
started_at: 2026-09-04
closed_at: 2026-09-04
blocked_by:
waiting_for:
verified_at: 2026-09-04

## Tasks

### World Context
- [x] Detect when a world-started activity is a recognition animal activity using existing activity metadata.
- [x] Derive `habitatTag` from the active destination or world host data.
- [x] Include `worldHostId` when available.
- [x] Include `discoveryElementId` and `narrativeContextId` only when the current world model already exposes them.
- [x] Do not include or duplicate `biomeCode` in launch context.

### Game Start Integration
- [x] Pass launch context to the game start use case or orchestrator.
- [x] Preserve existing behavior for non-animal and non-recognition activities.
- [x] Keep all world-specific decisions inside the world module.
- [x] Ensure game and recognition do not call world directly.

### Tests
- [x] Unit test animal recognition launch includes habitat tag.
- [x] Unit test non-animal launch does not force habitat filtering.
- [x] Unit test missing habitat falls back safely without blocking game start.
- [x] Unit test there is no direct dependency from `RecognitionEngine` to world classes.

## Manual Tests
- Start a world destination with an animal discovery element in dev mode.
- Confirm the game starts normally.
- Confirm logs or debug output show launch context with `habitatTag` when available.

## Risks
- Existing world activity proposal code may not expose enough metadata; avoid large refactors.
- Wrong habitat derivation can reduce content variety but must not block the child flow.

## Dependencies
- Sprint 069 completed.
- Existing world game start integration from backend sprint 056 or equivalent.
- FEAT-008 world module behavior.

## Agent Instruction
- Keep world responsible for narrative context only.
- Do not move world state lookup into game or recognition.
- Do not add habitat sub-topics.
- Keep code, comments, and names in English.

## Notes
This sprint implements the FEAT-009 rule: world decides context, game receives opaque data, recognition receives only resolved candidates.

## Review

completed_tasks:
- Detect animal recognition activity via TopicUseCase.getTopic() checking recognitionType == ANIMAL
- Derive habitatTag from worldState.currentDestination.biome
- Include worldHostId from worldState.currentDestination.hostId as String
- Include discoveryElementId from matching WorldDiscoveryProposal as String
- Include narrativeContextId from worldState.currentDestination.narrativeSituationCode
- No biomeCode duplication — habitatTag is the biome value directly
- Pass LaunchContext to gameOrchestrator.startGame(childProfileId, activityId, launchContext)
- Non-animal and non-recognition activities pass null LaunchContext
- Missing biome/host gracefully produces partial LaunchContext without blocking game start
- All world-specific logic stays in WorldGameStartService (world module)
- RecognitionEngine has zero imports/dependencies to world package (verified by reflection test)
- 4 new unit tests added, all 9 tests in WorldGameStartServiceTest pass
- Full suite: 883 tests, 0 failures, 0 errors

incomplete_tasks:

contract_changes:
- No contract changes. LaunchContext model (Sprint 069) reused as-is. WorldGameStartUseCase interface unchanged.

learnings:
- WorldDiscoveryProposal already carries activityId and topicId, making it straightforward to match proposals to activities and resolve their recognition type via TopicUseCase.
- Searching both currentDestination.discoveryProposals and visibleDiscoveryElements ensures proposals from either source are matched.

next_sprint_suggestions:
- GameOrchestratorService can now consume LaunchContext.habitatTag to filter recognition candidates by habitat when building engine params.
- Consider caching topic lookups in WorldGameStartService if topic resolution becomes a hotspot.

verification:
- All 9 unit tests pass (WorldGameStartServiceTest: 9 tests, 0 failures).
- Main code compiles successfully (`mvn compile` → BUILD SUCCESS).
- Animal recognition activity detection via TopicUseCase.getTopic() checking recognitionType == ANIMAL (line 115).
- habitatTag derived from worldState.currentDestination.biome (line 125).
- worldHostId included from worldState.currentDestination.hostId as String (line 127).
- discoveryElementId included from matching WorldDiscoveryProposal as String (line 134).
- narrativeContextId included from worldState.currentDestination.narrativeSituationCode (line 129).
- No biomeCode duplication — habitatTag is the biome value directly.
- LaunchContext passed to gameOrchestrator.startGame(childProfileId, activityId, launchContext) (line 78).
- Non-animal and non-recognition activities pass null LaunchContext (line 116).
- Missing biome/host gracefully produces partial LaunchContext without blocking game start (lines 120-135).
- All world-specific logic stays in WorldGameStartService (world module).
- RecognitionEngine has zero imports/dependencies to world package (verified by reflection test, lines 344-382).
- findMatchingProposal searches both currentDestination.discoveryProposals and visibleDiscoveryElements (lines 140-156).
