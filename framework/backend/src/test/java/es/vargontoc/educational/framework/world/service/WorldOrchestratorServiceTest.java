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
import es.vargontoc.educational.framework.world.model.WorldEngagementThresholdConfig;

import es.vargontoc.educational.framework.world.ports.in.EngagementThresholdConfigUseCase;
import es.vargontoc.educational.framework.world.ports.in.WorldOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

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

    private WorldOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new WorldOrchestratorService(
            selectTopicsForDifficultyUseCase,
            worldCatalogUseCase,
            engagementThresholdConfigUseCase,
            worldEngagementEvaluator
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
        when(worldCatalogUseCase.listActiveElementsForAge(anyInt()))
            .thenReturn(List.of(createElement(1L)));
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
        when(worldCatalogUseCase.listActiveElementsForAge(anyInt()))
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
        when(worldCatalogUseCase.listActiveElementsForAge(anyInt()))
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
        when(worldCatalogUseCase.listActiveElementsForAge(anyInt()))
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
        when(worldCatalogUseCase.listActiveElementsForAge(anyInt()))
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

    private CompatibleActivityProjection createActivity(Long id, String engineType) {
        return new CompatibleActivityProjection(id, "Activity " + id, engineType, List.of(10L), 3, 10, List.of(1L));
    }

    private WorldHostProjection createHost(Long id) {
        return new WorldHostProjection(id, "HOST_" + id, "Host " + id, Biome.MEADOW, "Desc", 3, 10, "asset_key", 1);
    }

    private WorldNarrativeSituationProjection createSituation(Long id) {
        return new WorldNarrativeSituationProjection(id, "SIT_" + id, "Text", null, null, 3, 10, 1);
    }

    private WorldDiscoveryElementProjection createElement(Long id) {
        return new WorldDiscoveryElementProjection(id, "EL_" + id, "Element", ElementType.DISCOVERY,
            Biome.MEADOW, 3, 10, 1L, 10L, "asset", InteractionCueType.BREATHING_GLOW, 1);
    }
}