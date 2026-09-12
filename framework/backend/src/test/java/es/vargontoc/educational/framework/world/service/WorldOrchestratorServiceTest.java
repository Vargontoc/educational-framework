package es.vargontoc.educational.framework.world.service;

import es.vargontoc.educational.framework.content.model.Biome;
import es.vargontoc.educational.framework.content.model.CompatibleActivityProjection;
import es.vargontoc.educational.framework.content.model.ElementType;
import es.vargontoc.educational.framework.content.model.InteractionCueType;
import es.vargontoc.educational.framework.content.model.WorldDiscoveryElementProjection;
import es.vargontoc.educational.framework.content.model.WorldHostProjection;
import es.vargontoc.educational.framework.content.model.WorldNarrativeSituationProjection;
import es.vargontoc.educational.framework.content.ports.in.WorldCatalogUseCase;
import es.vargontoc.educational.framework.tracking.model.DifficultyLevel;
import es.vargontoc.educational.framework.tracking.model.TopicSelectionResult;
import es.vargontoc.educational.framework.tracking.ports.in.SelectTopicsForDifficultyUseCase;
import es.vargontoc.educational.framework.world.model.WorldDestination;
import es.vargontoc.educational.framework.world.model.WorldDestinationSelectionResult;
import es.vargontoc.educational.framework.world.model.WorldDiscoveryProposal;
import es.vargontoc.educational.framework.world.model.WorldEngagementThresholdConfig;
import es.vargontoc.educational.framework.world.model.WorldState;

import es.vargontoc.educational.framework.world.ports.in.EngagementThresholdConfigUseCase;
import es.vargontoc.educational.framework.world.ports.in.WorldOrchestrator;
import es.vargontoc.educational.framework.world.ports.out.WorldStateRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorldOrchestratorServiceTest {

    @Mock
    private SelectTopicsForDifficultyUseCase selectTopicsForDifficultyUseCase;

    @Mock
    private WorldCatalogUseCase worldCatalogUseCase;

    @Mock
    private EngagementThresholdConfigUseCase engagementThresholdConfigUseCase;

    @Mock
    private WorldEngagementEvaluator worldEngagementEvaluator;

    @Mock
    private WorldStateRegistry worldStateRegistry;

    private WorldExplorationConfig worldExplorationConfig;

    private WorldOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        worldExplorationConfig = new WorldExplorationConfig();
        orchestrator = new WorldOrchestratorService(
            selectTopicsForDifficultyUseCase,
            worldCatalogUseCase,
            engagementThresholdConfigUseCase,
            worldEngagementEvaluator,
            worldStateRegistry,
            worldExplorationConfig
        );
    }

    @Test
    void selectDestination_topicSelection_isCalled() {
        when(selectTopicsForDifficultyUseCase.selectTopicsForDifficulty(eq(1L), eq(DifficultyLevel.EASY), eq(1)))
            .thenReturn(new TopicSelectionResult(List.of(10L)));
        when(worldCatalogUseCase.listCompatibleActivitiesByTopic(eq(10L), anyInt()))
            .thenReturn(List.of(createActivity(1L, "PUZZLE")));
        when(worldCatalogUseCase.listActiveHostsForAge(anyInt()))
            .thenReturn(List.of(createHost(1L)));
        when(worldCatalogUseCase.listActiveSituationsForAge(anyInt()))
            .thenReturn(List.of(createSituation(1L)));
        when(worldCatalogUseCase.listActiveElementsByBiomeAndAge(eq(Biome.MEADOW), anyInt()))
            .thenReturn(List.of(createElement(1L, 1)));
        when(engagementThresholdConfigUseCase.engagementThresholdConfig(any()))
            .thenReturn(new WorldEngagementThresholdConfig());
        when(worldEngagementEvaluator.evaluatePatterns(any(), any()))
            .thenReturn(Collections.emptyList());
        when(worldEngagementEvaluator.evaluateAdjustments(any()))
            .thenReturn(Collections.emptyList());

        orchestrator.selectDestination(100L, 1L, null, 5);

        verify(selectTopicsForDifficultyUseCase).selectTopicsForDifficulty(1L, DifficultyLevel.EASY, 1);
    }

    @Test
    void selectDestination_compatibleActivity_isSelected() {
        when(selectTopicsForDifficultyUseCase.selectTopicsForDifficulty(any(), any(), any()))
            .thenReturn(new TopicSelectionResult(List.of(10L)));
        when(worldCatalogUseCase.listCompatibleActivitiesByTopic(eq(10L), anyInt()))
            .thenReturn(List.of(createActivity(1L, "PUZZLE")));
        when(worldCatalogUseCase.listActiveHostsForAge(anyInt()))
            .thenReturn(List.of(createHost(1L)));
        when(worldCatalogUseCase.listActiveSituationsForAge(anyInt()))
            .thenReturn(List.of(createSituation(1L)));
        when(worldCatalogUseCase.listActiveElementsByBiomeAndAge(eq(Biome.MEADOW), anyInt()))
            .thenReturn(Collections.emptyList());
        when(engagementThresholdConfigUseCase.engagementThresholdConfig(any()))
            .thenReturn(new WorldEngagementThresholdConfig());
        when(worldEngagementEvaluator.evaluatePatterns(any(), any()))
            .thenReturn(Collections.emptyList());
        when(worldEngagementEvaluator.evaluateAdjustments(any()))
            .thenReturn(Collections.emptyList());

        WorldDestinationSelectionResult result = orchestrator.selectDestination(100L, 1L, null, 5);

        assertNotNull(result.getSelectedActivity());
        assertEquals(1L, result.getSelectedActivity().getActivityId());
        assertEquals("PUZZLE", result.getSelectedActivity().getEngineType());
    }

    @Test
    void selectDestination_priorityAdjustment_affectsSelection() {
        when(selectTopicsForDifficultyUseCase.selectTopicsForDifficulty(any(), any(), any()))
            .thenReturn(new TopicSelectionResult(List.of(10L)));
        when(worldCatalogUseCase.listCompatibleActivitiesByTopic(any(), anyInt()))
            .thenReturn(List.of(createActivity(1L, "PUZZLE"), createActivity(2L, "STORY")));
        when(worldCatalogUseCase.listActiveHostsForAge(anyInt()))
            .thenReturn(List.of(createHost(1L)));
        when(worldCatalogUseCase.listActiveSituationsForAge(anyInt()))
            .thenReturn(List.of(createSituation(1L)));
        when(worldCatalogUseCase.listActiveElementsByBiomeAndAge(eq(Biome.MEADOW), anyInt()))
            .thenReturn(Collections.emptyList());
        when(engagementThresholdConfigUseCase.engagementThresholdConfig(any()))
            .thenReturn(new WorldEngagementThresholdConfig());
        when(worldEngagementEvaluator.evaluatePatterns(any(), any()))
            .thenReturn(Collections.emptyList());
        when(worldEngagementEvaluator.evaluateAdjustments(any()))
            .thenReturn(List.of(new es.vargontoc.educational.framework.world.model.WorldEnginePriorityAdjustment(
                "PUZZLE", new BigDecimal("0.6"), "test")));

        WorldDestinationSelectionResult result = orchestrator.selectDestination(100L, 1L, null, 5);

        assertNotNull(result.getSelectedActivity());
        assertTrue(result.isPriorityAdjustmentApplied());
    }

    @Test
    void selectDestination_noActivity_returnsDecorativeDestination() {
        when(selectTopicsForDifficultyUseCase.selectTopicsForDifficulty(any(), any(), any()))
            .thenReturn(new TopicSelectionResult(List.of(10L)));
        when(worldCatalogUseCase.listCompatibleActivitiesByTopic(any(), anyInt()))
            .thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveHostsForAge(anyInt()))
            .thenReturn(List.of(createHost(1L)));
        when(worldCatalogUseCase.listActiveSituationsForAge(anyInt()))
            .thenReturn(List.of(createSituation(1L)));
        when(worldCatalogUseCase.listActiveElementsByBiomeAndAge(eq(Biome.MEADOW), anyInt()))
            .thenReturn(Collections.emptyList());
        when(engagementThresholdConfigUseCase.engagementThresholdConfig(any()))
            .thenReturn(new WorldEngagementThresholdConfig());
        when(worldEngagementEvaluator.evaluatePatterns(any(), any()))
            .thenReturn(Collections.emptyList());
        when(worldEngagementEvaluator.evaluateAdjustments(any()))
            .thenReturn(Collections.emptyList());

        WorldDestinationSelectionResult result = orchestrator.selectDestination(100L, 1L, null, 5);

        assertNull(result.getSelectedActivity());
        assertNotNull(result.getDestination());
    }

    @Test
    void selectDestination_destinationHasNoDiagnosticLabels() {
        when(selectTopicsForDifficultyUseCase.selectTopicsForDifficulty(any(), any(), any()))
            .thenReturn(new TopicSelectionResult(List.of(10L)));
        when(worldCatalogUseCase.listCompatibleActivitiesByTopic(any(), anyInt()))
            .thenReturn(List.of(createActivity(1L, "PUZZLE")));
        when(worldCatalogUseCase.listActiveHostsForAge(anyInt()))
            .thenReturn(List.of(createHost(1L)));
        when(worldCatalogUseCase.listActiveSituationsForAge(anyInt()))
            .thenReturn(List.of(createSituation(1L)));
        when(worldCatalogUseCase.listActiveElementsByBiomeAndAge(eq(Biome.MEADOW), anyInt()))
            .thenReturn(Collections.emptyList());
        when(engagementThresholdConfigUseCase.engagementThresholdConfig(any()))
            .thenReturn(new WorldEngagementThresholdConfig());
        when(worldEngagementEvaluator.evaluatePatterns(any(), any()))
            .thenReturn(Collections.emptyList());
        when(worldEngagementEvaluator.evaluateAdjustments(any()))
            .thenReturn(Collections.emptyList());

        WorldDestinationSelectionResult result = orchestrator.selectDestination(100L, 1L, null, 5);

        WorldDestination dest = result.getDestination();
        assertNotNull(dest);
        assertNotNull(dest.getHostCode());
        assertNotNull(dest.getNarrativeSituationCode());
    }

    @Test
    void selectDestination_anyBiomeHost_canBeSelected() {
        when(selectTopicsForDifficultyUseCase.selectTopicsForDifficulty(any(), any(), any()))
            .thenReturn(new TopicSelectionResult(List.of(10L)));
        when(worldCatalogUseCase.listCompatibleActivitiesByTopic(any(), anyInt()))
            .thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveHostsForAge(anyInt()))
            .thenReturn(List.of(createHostWithBiome(1L, Biome.WOODS), createHostWithBiome(2L, Biome.MEADOW)));
        when(worldCatalogUseCase.listActiveSituationsForAge(anyInt()))
            .thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveElementsByBiomeAndAge(eq(Biome.WOODS), anyInt()))
            .thenReturn(Collections.emptyList());
        when(engagementThresholdConfigUseCase.engagementThresholdConfig(any()))
            .thenReturn(new WorldEngagementThresholdConfig());
        when(worldEngagementEvaluator.evaluatePatterns(any(), any()))
            .thenReturn(Collections.emptyList());
        when(worldEngagementEvaluator.evaluateAdjustments(any()))
            .thenReturn(Collections.emptyList());

        WorldDestinationSelectionResult result = orchestrator.selectDestination(100L, 1L, null, 5);

        assertEquals(1L, result.getDestination().getHostId());
        assertEquals(Biome.WOODS.name(), result.getDestination().getBiome());
    }

    @Test
    void selectDestination_noHostAvailable_doesNotFailAndDefaultsToMeadowBiome() {
        when(selectTopicsForDifficultyUseCase.selectTopicsForDifficulty(any(), any(), any()))
            .thenReturn(new TopicSelectionResult(List.of(10L)));
        when(worldCatalogUseCase.listCompatibleActivitiesByTopic(any(), anyInt()))
            .thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveHostsForAge(anyInt()))
            .thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveSituationsForAge(anyInt()))
            .thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveElementsByBiomeAndAge(eq(Biome.MEADOW), anyInt()))
            .thenReturn(Collections.emptyList());
        when(engagementThresholdConfigUseCase.engagementThresholdConfig(any()))
            .thenReturn(new WorldEngagementThresholdConfig());
        when(worldEngagementEvaluator.evaluatePatterns(any(), any()))
            .thenReturn(Collections.emptyList());
        when(worldEngagementEvaluator.evaluateAdjustments(any()))
            .thenReturn(Collections.emptyList());

        WorldDestinationSelectionResult result = orchestrator.selectDestination(100L, 1L, null, 5);

        assertNotNull(result.getDestination());
        assertNull(result.getDestination().getHostId());
        assertEquals(Biome.MEADOW.name(), result.getDestination().getBiome());
    }

    @Test
    void selectDestination_discoveryProposals_neverExceedConfiguredMax() {
        worldExplorationConfig.setMaxVisibleElements(3);

        when(selectTopicsForDifficultyUseCase.selectTopicsForDifficulty(any(), any(), any()))
            .thenReturn(new TopicSelectionResult(List.of(10L)));
        when(worldCatalogUseCase.listCompatibleActivitiesByTopic(any(), anyInt()))
            .thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveHostsForAge(anyInt()))
            .thenReturn(List.of(createHost(1L)));
        when(worldCatalogUseCase.listActiveSituationsForAge(anyInt()))
            .thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveElementsByBiomeAndAge(eq(Biome.MEADOW), anyInt()))
            .thenReturn(List.of(
                createElement(1L, 1), createElement(2L, 2), createElement(3L, 3),
                createElement(4L, 4), createElement(5L, 5), createElement(6L, 6)));
        when(engagementThresholdConfigUseCase.engagementThresholdConfig(any()))
            .thenReturn(new WorldEngagementThresholdConfig());
        when(worldEngagementEvaluator.evaluatePatterns(any(), any()))
            .thenReturn(Collections.emptyList());
        when(worldEngagementEvaluator.evaluateAdjustments(any()))
            .thenReturn(Collections.emptyList());
        when(worldStateRegistry.findByChildSessionId(100L)).thenReturn(Optional.empty());

        WorldDestinationSelectionResult result = orchestrator.selectDestination(100L, 1L, null, 5);

        assertEquals(3, result.getDestination().getDiscoveryProposals().size());
    }

    @Test
    void selectDestination_poolSmallerThanMax_showsAllElements() {
        worldExplorationConfig.setMaxVisibleElements(3);

        when(selectTopicsForDifficultyUseCase.selectTopicsForDifficulty(any(), any(), any()))
            .thenReturn(new TopicSelectionResult(List.of(10L)));
        when(worldCatalogUseCase.listCompatibleActivitiesByTopic(any(), anyInt()))
            .thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveHostsForAge(anyInt()))
            .thenReturn(List.of(createHost(1L)));
        when(worldCatalogUseCase.listActiveSituationsForAge(anyInt()))
            .thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveElementsByBiomeAndAge(eq(Biome.MEADOW), anyInt()))
            .thenReturn(List.of(createElement(1L, 1), createElement(2L, 2)));
        when(engagementThresholdConfigUseCase.engagementThresholdConfig(any()))
            .thenReturn(new WorldEngagementThresholdConfig());
        when(worldEngagementEvaluator.evaluatePatterns(any(), any()))
            .thenReturn(Collections.emptyList());
        when(worldEngagementEvaluator.evaluateAdjustments(any()))
            .thenReturn(Collections.emptyList());

        WorldDestinationSelectionResult result = orchestrator.selectDestination(100L, 1L, null, 5);

        assertEquals(2, result.getDestination().getDiscoveryProposals().size());
    }

    @Test
    void selectDestination_rotatesAwayFromPreviouslyVisibleElements_whenUnseenCandidatesExist() {
        worldExplorationConfig.setMaxVisibleElements(3);

        when(selectTopicsForDifficultyUseCase.selectTopicsForDifficulty(any(), any(), any()))
            .thenReturn(new TopicSelectionResult(List.of(10L)));
        when(worldCatalogUseCase.listCompatibleActivitiesByTopic(any(), anyInt()))
            .thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveHostsForAge(anyInt()))
            .thenReturn(List.of(createHost(1L)));
        when(worldCatalogUseCase.listActiveSituationsForAge(anyInt()))
            .thenReturn(Collections.emptyList());
        List<WorldDiscoveryElementProjection> pool = List.of(
            createElement(1L, 1), createElement(2L, 2), createElement(3L, 3),
            createElement(4L, 4), createElement(5L, 5), createElement(6L, 6));
        when(worldCatalogUseCase.listActiveElementsByBiomeAndAge(eq(Biome.MEADOW), anyInt()))
            .thenReturn(pool);
        when(engagementThresholdConfigUseCase.engagementThresholdConfig(any()))
            .thenReturn(new WorldEngagementThresholdConfig());
        when(worldEngagementEvaluator.evaluatePatterns(any(), any()))
            .thenReturn(Collections.emptyList());
        when(worldEngagementEvaluator.evaluateAdjustments(any()))
            .thenReturn(Collections.emptyList());

        // First build: no previous state yet, expect elements 1,2,3 (lowest sortOrder).
        when(worldStateRegistry.findByChildSessionId(100L)).thenReturn(Optional.empty());
        WorldDestinationSelectionResult first = orchestrator.selectDestination(100L, 1L, null, 5);
        Set<Long> firstIds = idsOf(first.getDestination().getDiscoveryProposals());
        assertEquals(Set.of(1L, 2L, 3L), firstIds);

        // Simulate the caller persisting the first selection as "last shown".
        WorldState stateAfterFirstBuild = new WorldState();
        stateAfterFirstBuild.setVisibleDiscoveryElements(first.getDestination().getDiscoveryProposals());
        when(worldStateRegistry.findByChildSessionId(100L)).thenReturn(Optional.of(stateAfterFirstBuild));

        WorldDestinationSelectionResult second = orchestrator.selectDestination(100L, 1L, null, 5);
        Set<Long> secondIds = idsOf(second.getDestination().getDiscoveryProposals());

        assertEquals(Set.of(4L, 5L, 6L), secondIds);
        assertTrue(Collections.disjoint(firstIds, secondIds));
    }

    @Test
    void selectDestination_rotation_fillsWithAlreadyShownElements_whenNotEnoughUnseenCandidates() {
        worldExplorationConfig.setMaxVisibleElements(3);

        when(selectTopicsForDifficultyUseCase.selectTopicsForDifficulty(any(), any(), any()))
            .thenReturn(new TopicSelectionResult(List.of(10L)));
        when(worldCatalogUseCase.listCompatibleActivitiesByTopic(any(), anyInt()))
            .thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveHostsForAge(anyInt()))
            .thenReturn(List.of(createHost(1L)));
        when(worldCatalogUseCase.listActiveSituationsForAge(anyInt()))
            .thenReturn(Collections.emptyList());
        // Pool of 4: after excluding 3 previously-shown, only 1 unseen candidate remains,
        // but the configured max is 3 — the remaining 2 slots must be filled with shown ones.
        List<WorldDiscoveryElementProjection> pool = List.of(
            createElement(1L, 1), createElement(2L, 2), createElement(3L, 3), createElement(4L, 4));
        when(worldCatalogUseCase.listActiveElementsByBiomeAndAge(eq(Biome.MEADOW), anyInt()))
            .thenReturn(pool);
        when(engagementThresholdConfigUseCase.engagementThresholdConfig(any()))
            .thenReturn(new WorldEngagementThresholdConfig());
        when(worldEngagementEvaluator.evaluatePatterns(any(), any()))
            .thenReturn(Collections.emptyList());
        when(worldEngagementEvaluator.evaluateAdjustments(any()))
            .thenReturn(Collections.emptyList());

        WorldState previousState = new WorldState();
        previousState.setVisibleDiscoveryElements(List.of(
            proposalFor(1L), proposalFor(2L), proposalFor(3L)));
        when(worldStateRegistry.findByChildSessionId(100L)).thenReturn(Optional.of(previousState));

        WorldDestinationSelectionResult result = orchestrator.selectDestination(100L, 1L, null, 5);

        List<WorldDiscoveryProposal> proposals = result.getDestination().getDiscoveryProposals();
        assertEquals(3, proposals.size());
        assertTrue(idsOf(proposals).contains(4L), "the only unseen candidate must always be included");
    }

    @Test
    void selectDestination_hostWithWorldWidth_isExposedOnDestination() {
        when(selectTopicsForDifficultyUseCase.selectTopicsForDifficulty(any(), any(), any()))
            .thenReturn(new TopicSelectionResult(List.of(10L)));
        when(worldCatalogUseCase.listCompatibleActivitiesByTopic(any(), anyInt()))
            .thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveHostsForAge(anyInt()))
            .thenReturn(List.of(createHostWithWorldWidth(1L, 4000)));
        when(worldCatalogUseCase.listActiveSituationsForAge(anyInt()))
            .thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveElementsByBiomeAndAge(eq(Biome.MEADOW), anyInt()))
            .thenReturn(Collections.emptyList());
        when(engagementThresholdConfigUseCase.engagementThresholdConfig(any()))
            .thenReturn(new WorldEngagementThresholdConfig());
        when(worldEngagementEvaluator.evaluatePatterns(any(), any()))
            .thenReturn(Collections.emptyList());
        when(worldEngagementEvaluator.evaluateAdjustments(any()))
            .thenReturn(Collections.emptyList());

        WorldDestinationSelectionResult result = orchestrator.selectDestination(100L, 1L, null, 5);

        assertEquals(4000, result.getDestination().getWorldWidth());
    }

    @Test
    void selectDestination_hostWithoutWorldWidth_defaultsTo2560() {
        when(selectTopicsForDifficultyUseCase.selectTopicsForDifficulty(any(), any(), any()))
            .thenReturn(new TopicSelectionResult(List.of(10L)));
        when(worldCatalogUseCase.listCompatibleActivitiesByTopic(any(), anyInt()))
            .thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveHostsForAge(anyInt()))
            .thenReturn(List.of(createHostWithWorldWidth(1L, null)));
        when(worldCatalogUseCase.listActiveSituationsForAge(anyInt()))
            .thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveElementsByBiomeAndAge(eq(Biome.MEADOW), anyInt()))
            .thenReturn(Collections.emptyList());
        when(engagementThresholdConfigUseCase.engagementThresholdConfig(any()))
            .thenReturn(new WorldEngagementThresholdConfig());
        when(worldEngagementEvaluator.evaluatePatterns(any(), any()))
            .thenReturn(Collections.emptyList());
        when(worldEngagementEvaluator.evaluateAdjustments(any()))
            .thenReturn(Collections.emptyList());

        WorldDestinationSelectionResult result = orchestrator.selectDestination(100L, 1L, null, 5);

        assertEquals(2560, result.getDestination().getWorldWidth());
    }

    @Test
    void selectDestination_elementPosition_survivesUnchangedIntoProposal() {
        when(selectTopicsForDifficultyUseCase.selectTopicsForDifficulty(any(), any(), any()))
            .thenReturn(new TopicSelectionResult(List.of(10L)));
        when(worldCatalogUseCase.listCompatibleActivitiesByTopic(any(), anyInt()))
            .thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveHostsForAge(anyInt()))
            .thenReturn(List.of(createHost(1L)));
        when(worldCatalogUseCase.listActiveSituationsForAge(anyInt()))
            .thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveElementsByBiomeAndAge(eq(Biome.MEADOW), anyInt()))
            .thenReturn(List.of(createElement(1L, 1, 0.32, 0.64)));
        when(engagementThresholdConfigUseCase.engagementThresholdConfig(any()))
            .thenReturn(new WorldEngagementThresholdConfig());
        when(worldEngagementEvaluator.evaluatePatterns(any(), any()))
            .thenReturn(Collections.emptyList());
        when(worldEngagementEvaluator.evaluateAdjustments(any()))
            .thenReturn(Collections.emptyList());

        WorldDestinationSelectionResult result = orchestrator.selectDestination(100L, 1L, null, 5);

        WorldDiscoveryProposal proposal = result.getDestination().getDiscoveryProposals().get(0);
        assertEquals(0.32, proposal.getPositionX());
        assertEquals(0.64, proposal.getPositionY());
    }

    @Test
    void selectDestination_candidateTooCloseToSelected_isSkippedForNextCandidate() {
        worldExplorationConfig.setMaxVisibleElements(2);

        when(selectTopicsForDifficultyUseCase.selectTopicsForDifficulty(any(), any(), any()))
            .thenReturn(new TopicSelectionResult(List.of(10L)));
        when(worldCatalogUseCase.listCompatibleActivitiesByTopic(any(), anyInt()))
            .thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveHostsForAge(anyInt()))
            .thenReturn(List.of(createHost(1L)));
        when(worldCatalogUseCase.listActiveSituationsForAge(anyInt()))
            .thenReturn(Collections.emptyList());
        // Element 2 is within the default 0.15 minimum separation of element 1; element 3 is far.
        List<WorldDiscoveryElementProjection> pool = List.of(
            createElement(1L, 1, 0.10, 0.10),
            createElement(2L, 2, 0.12, 0.10),
            createElement(3L, 3, 0.80, 0.80),
            createElement(4L, 4, 0.50, 0.50));
        when(worldCatalogUseCase.listActiveElementsByBiomeAndAge(eq(Biome.MEADOW), anyInt()))
            .thenReturn(pool);
        when(engagementThresholdConfigUseCase.engagementThresholdConfig(any()))
            .thenReturn(new WorldEngagementThresholdConfig());
        when(worldEngagementEvaluator.evaluatePatterns(any(), any()))
            .thenReturn(Collections.emptyList());
        when(worldEngagementEvaluator.evaluateAdjustments(any()))
            .thenReturn(Collections.emptyList());
        when(worldStateRegistry.findByChildSessionId(100L)).thenReturn(Optional.empty());

        WorldDestinationSelectionResult result = orchestrator.selectDestination(100L, 1L, null, 5);

        assertEquals(Set.of(1L, 3L), idsOf(result.getDestination().getDiscoveryProposals()),
            "element 2 must be skipped for being too close to element 1, element 3 tried next");
    }

    @Test
    void selectDestination_noUnseenCandidateSatisfiesSeparation_repeatsSeenElementBeforeGivingUpCount() {
        worldExplorationConfig.setMaxVisibleElements(2);

        when(selectTopicsForDifficultyUseCase.selectTopicsForDifficulty(any(), any(), any()))
            .thenReturn(new TopicSelectionResult(List.of(10L)));
        when(worldCatalogUseCase.listCompatibleActivitiesByTopic(any(), anyInt()))
            .thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveHostsForAge(anyInt()))
            .thenReturn(List.of(createHost(1L)));
        when(worldCatalogUseCase.listActiveSituationsForAge(anyInt()))
            .thenReturn(Collections.emptyList());
        // Element 2 (unseen) is too close to element 1; element 3 (already shown) is far enough.
        List<WorldDiscoveryElementProjection> pool = List.of(
            createElement(1L, 1, 0.10, 0.10),
            createElement(2L, 2, 0.11, 0.11),
            createElement(3L, 3, 0.90, 0.90));
        when(worldCatalogUseCase.listActiveElementsByBiomeAndAge(eq(Biome.MEADOW), anyInt()))
            .thenReturn(pool);
        when(engagementThresholdConfigUseCase.engagementThresholdConfig(any()))
            .thenReturn(new WorldEngagementThresholdConfig());
        when(worldEngagementEvaluator.evaluatePatterns(any(), any()))
            .thenReturn(Collections.emptyList());
        when(worldEngagementEvaluator.evaluateAdjustments(any()))
            .thenReturn(Collections.emptyList());

        WorldState previousState = new WorldState();
        previousState.setVisibleDiscoveryElements(List.of(proposalFor(3L)));
        when(worldStateRegistry.findByChildSessionId(100L)).thenReturn(Optional.of(previousState));

        WorldDestinationSelectionResult result = orchestrator.selectDestination(100L, 1L, null, 5);

        assertEquals(Set.of(1L, 3L), idsOf(result.getDestination().getDiscoveryProposals()),
            "already-shown element 3 must fill the slot instead of the too-close unseen element 2");
    }

    @Test
    void selectDestination_noCandidateSatisfiesSeparation_stillFillsConfiguredMax() {
        worldExplorationConfig.setMaxVisibleElements(2);

        when(selectTopicsForDifficultyUseCase.selectTopicsForDifficulty(any(), any(), any()))
            .thenReturn(new TopicSelectionResult(List.of(10L)));
        when(worldCatalogUseCase.listCompatibleActivitiesByTopic(any(), anyInt()))
            .thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveHostsForAge(anyInt()))
            .thenReturn(List.of(createHost(1L)));
        when(worldCatalogUseCase.listActiveSituationsForAge(anyInt()))
            .thenReturn(Collections.emptyList());
        // Every candidate is clustered within the minimum separation of every other one.
        List<WorldDiscoveryElementProjection> pool = List.of(
            createElement(1L, 1, 0.10, 0.10),
            createElement(2L, 2, 0.11, 0.10),
            createElement(3L, 3, 0.12, 0.10));
        when(worldCatalogUseCase.listActiveElementsByBiomeAndAge(eq(Biome.MEADOW), anyInt()))
            .thenReturn(pool);
        when(engagementThresholdConfigUseCase.engagementThresholdConfig(any()))
            .thenReturn(new WorldEngagementThresholdConfig());
        when(worldEngagementEvaluator.evaluatePatterns(any(), any()))
            .thenReturn(Collections.emptyList());
        when(worldEngagementEvaluator.evaluateAdjustments(any()))
            .thenReturn(Collections.emptyList());
        when(worldStateRegistry.findByChildSessionId(100L)).thenReturn(Optional.empty());

        WorldDestinationSelectionResult result = orchestrator.selectDestination(100L, 1L, null, 5);

        assertEquals(2, result.getDestination().getDiscoveryProposals().size(),
            "a badly distributed pool must not block reaching the configured cap");
    }

    @Test
    void selectDestination_hostWithSequenceOrder_isExposedOnDestination() {
        when(selectTopicsForDifficultyUseCase.selectTopicsForDifficulty(any(), any(), any()))
            .thenReturn(new TopicSelectionResult(List.of(10L)));
        when(worldCatalogUseCase.listCompatibleActivitiesByTopic(any(), anyInt()))
            .thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveHostsForAge(anyInt()))
            .thenReturn(List.of(createHostWithSortOrder(1L, Biome.BEACH, 4)));
        when(worldCatalogUseCase.listActiveSituationsForAge(anyInt()))
            .thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveElementsByBiomeAndAge(eq(Biome.BEACH), anyInt()))
            .thenReturn(Collections.emptyList());
        when(engagementThresholdConfigUseCase.engagementThresholdConfig(any()))
            .thenReturn(new WorldEngagementThresholdConfig());
        when(worldEngagementEvaluator.evaluatePatterns(any(), any()))
            .thenReturn(Collections.emptyList());
        when(worldEngagementEvaluator.evaluateAdjustments(any()))
            .thenReturn(Collections.emptyList());

        WorldDestinationSelectionResult result = orchestrator.selectDestination(100L, 1L, null, 5);

        assertEquals(4, result.getDestination().getHostSequenceOrder());
        assertEquals(Biome.BEACH.name(), result.getDestination().getBiome());
    }

    @Test
    void selectDestination_hostWithoutSequenceOrder_exposesNullSequenceOrder() {
        when(selectTopicsForDifficultyUseCase.selectTopicsForDifficulty(any(), any(), any()))
            .thenReturn(new TopicSelectionResult(List.of(10L)));
        when(worldCatalogUseCase.listCompatibleActivitiesByTopic(any(), anyInt()))
            .thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveHostsForAge(anyInt()))
            .thenReturn(List.of(createHostWithSortOrder(1L, Biome.FARM, null)));
        when(worldCatalogUseCase.listActiveSituationsForAge(anyInt()))
            .thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveElementsByBiomeAndAge(eq(Biome.FARM), anyInt()))
            .thenReturn(Collections.emptyList());
        when(engagementThresholdConfigUseCase.engagementThresholdConfig(any()))
            .thenReturn(new WorldEngagementThresholdConfig());
        when(worldEngagementEvaluator.evaluatePatterns(any(), any()))
            .thenReturn(Collections.emptyList());
        when(worldEngagementEvaluator.evaluateAdjustments(any()))
            .thenReturn(Collections.emptyList());

        WorldDestinationSelectionResult result = orchestrator.selectDestination(100L, 1L, null, 5);

        assertNull(result.getDestination().getHostSequenceOrder());
    }

    @Test
    void selectDestination_multipleBiomeHosts_selectsLowestSortOrder() {
        when(selectTopicsForDifficultyUseCase.selectTopicsForDifficulty(any(), any(), any()))
            .thenReturn(new TopicSelectionResult(List.of(10L)));
        when(worldCatalogUseCase.listCompatibleActivitiesByTopic(any(), anyInt()))
            .thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveHostsForAge(anyInt()))
            .thenReturn(List.of(
                createHostWithSortOrder(3L, Biome.SPACE, 5),
                createHostWithSortOrder(1L, Biome.MEADOW, 1),
                createHostWithSortOrder(2L, Biome.FARM, 2)));
        when(worldCatalogUseCase.listActiveSituationsForAge(anyInt()))
            .thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveElementsByBiomeAndAge(eq(Biome.MEADOW), anyInt()))
            .thenReturn(Collections.emptyList());
        when(engagementThresholdConfigUseCase.engagementThresholdConfig(any()))
            .thenReturn(new WorldEngagementThresholdConfig());
        when(worldEngagementEvaluator.evaluatePatterns(any(), any()))
            .thenReturn(Collections.emptyList());
        when(worldEngagementEvaluator.evaluateAdjustments(any()))
            .thenReturn(Collections.emptyList());

        WorldDestinationSelectionResult result = orchestrator.selectDestination(100L, 1L, null, 5);

        assertEquals(1L, result.getDestination().getHostId());
        assertEquals(1, result.getDestination().getHostSequenceOrder());
        assertEquals(Biome.MEADOW.name(), result.getDestination().getBiome());
    }

    @Test
    void selectDestination_sequenceOrder_doesNotAffectElementSelection() {
        worldExplorationConfig.setMaxVisibleElements(3);

        when(selectTopicsForDifficultyUseCase.selectTopicsForDifficulty(any(), any(), any()))
            .thenReturn(new TopicSelectionResult(List.of(10L)));
        when(worldCatalogUseCase.listCompatibleActivitiesByTopic(any(), anyInt()))
            .thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveHostsForAge(anyInt()))
            .thenReturn(List.of(createHostWithSortOrder(1L, Biome.PREHISTORY, 6)));
        when(worldCatalogUseCase.listActiveSituationsForAge(anyInt()))
            .thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveElementsByBiomeAndAge(eq(Biome.PREHISTORY), anyInt()))
            .thenReturn(List.of(createElementWithBiome(1L, 1, Biome.PREHISTORY), createElementWithBiome(2L, 2, Biome.PREHISTORY)));
        when(engagementThresholdConfigUseCase.engagementThresholdConfig(any()))
            .thenReturn(new WorldEngagementThresholdConfig());
        when(worldEngagementEvaluator.evaluatePatterns(any(), any()))
            .thenReturn(Collections.emptyList());
        when(worldEngagementEvaluator.evaluateAdjustments(any()))
            .thenReturn(Collections.emptyList());

        WorldDestinationSelectionResult result = orchestrator.selectDestination(100L, 1L, null, 5);

        assertEquals(2, result.getDestination().getDiscoveryProposals().size());
        assertEquals(6, result.getDestination().getHostSequenceOrder());
        assertEquals(Biome.PREHISTORY.name(), result.getDestination().getBiome());
    }

    private Set<Long> idsOf(List<WorldDiscoveryProposal> proposals) {
        return proposals.stream().map(WorldDiscoveryProposal::getDiscoveryElementId).collect(Collectors.toSet());
    }

    private WorldDiscoveryProposal proposalFor(Long discoveryElementId) {
        WorldDiscoveryProposal proposal = new WorldDiscoveryProposal();
        proposal.setDiscoveryElementId(discoveryElementId);
        return proposal;
    }

    private CompatibleActivityProjection createActivity(Long id, String engineType) {
        return new CompatibleActivityProjection(id, "Activity " + id, engineType, List.of(10L), 3, 10, List.of(1L));
    }

    private WorldHostProjection createHost(Long id) {
        return new WorldHostProjection(id, "HOST_" + id, "Host " + id, Biome.MEADOW, "Desc", 3, 10, "asset_key", 1, null);
    }

    private WorldHostProjection createHostWithBiome(Long id, Biome biome) {
        return new WorldHostProjection(id, "HOST_" + id, "Host " + id, biome, "Desc", 3, 10, "asset_key", 1, null);
    }

    private WorldHostProjection createHostWithWorldWidth(Long id, Integer worldWidth) {
        return new WorldHostProjection(id, "HOST_" + id, "Host " + id, Biome.MEADOW, "Desc", 3, 10, "asset_key", 1, worldWidth);
    }

    private WorldHostProjection createHostWithSortOrder(Long id, Biome biome, Integer sortOrder) {
        return new WorldHostProjection(id, "HOST_" + id, "Host " + id, biome, "Desc", 3, 10, "asset_key", sortOrder, null);
    }

    private WorldNarrativeSituationProjection createSituation(Long id) {
        return new WorldNarrativeSituationProjection(id, "SIT_" + id, "Text", null, null, 3, 10, 1);
    }

    private WorldDiscoveryElementProjection createElement(Long id, int sortOrder) {
        return createElement(id, sortOrder, null, null);
    }

    private WorldDiscoveryElementProjection createElement(Long id, int sortOrder, Double positionX, Double positionY) {
        return new WorldDiscoveryElementProjection(id, "EL_" + id, "Element " + id, ElementType.DISCOVERY,
            Biome.MEADOW, 3, 10, null, null, "asset", InteractionCueType.BREATHING_GLOW, sortOrder,
            positionX, positionY);
    }

    private WorldDiscoveryElementProjection createElementWithBiome(Long id, int sortOrder, Biome biome) {
        return new WorldDiscoveryElementProjection(id, "EL_" + id, "Element " + id, ElementType.DISCOVERY,
            biome, 3, 10, null, null, "asset", InteractionCueType.BREATHING_GLOW, sortOrder,
            null, null);
    }
}
