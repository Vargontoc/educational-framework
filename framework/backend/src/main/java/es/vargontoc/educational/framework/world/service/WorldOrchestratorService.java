package es.vargontoc.educational.framework.world.service;

import es.vargontoc.educational.framework.content.model.Biome;
import es.vargontoc.educational.framework.content.model.CompatibleActivityProjection;
import es.vargontoc.educational.framework.content.model.ElementType;
import es.vargontoc.educational.framework.content.model.WorldDiscoveryElementProjection;
import es.vargontoc.educational.framework.content.model.WorldHostProjection;
import es.vargontoc.educational.framework.content.model.WorldNarrativeSituationProjection;
import es.vargontoc.educational.framework.content.ports.in.WorldCatalogUseCase;
import es.vargontoc.educational.framework.tracking.model.DifficultyLevel;
import es.vargontoc.educational.framework.tracking.model.TopicSelectionResult;
import es.vargontoc.educational.framework.tracking.ports.in.SelectTopicsForDifficultyUseCase;
import es.vargontoc.educational.framework.world.model.SelectedWorldActivity;
import es.vargontoc.educational.framework.world.model.WorldDestination;
import es.vargontoc.educational.framework.world.model.WorldDestinationSelectionResult;
import es.vargontoc.educational.framework.world.model.WorldDiscoveryProposal;
import es.vargontoc.educational.framework.world.model.WorldEngineEngagementPattern;
import es.vargontoc.educational.framework.world.model.WorldEnginePriorityAdjustment;
import es.vargontoc.educational.framework.world.model.WorldEngagementThresholdConfig;
import es.vargontoc.educational.framework.world.model.WorldEngagementWindow;
import es.vargontoc.educational.framework.world.ports.in.EngagementThresholdConfigUseCase;
import es.vargontoc.educational.framework.world.ports.in.WorldOrchestrator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WorldOrchestratorService implements WorldOrchestrator {

    private final SelectTopicsForDifficultyUseCase selectTopicsForDifficultyUseCase;
    private final WorldCatalogUseCase worldCatalogUseCase;
    private final EngagementThresholdConfigUseCase engagementThresholdConfigUseCase;
    private final WorldEngagementEvaluator worldEngagementEvaluator;

    public WorldOrchestratorService(SelectTopicsForDifficultyUseCase selectTopicsForDifficultyUseCase,
                                   WorldCatalogUseCase worldCatalogUseCase,
                                   EngagementThresholdConfigUseCase engagementThresholdConfigUseCase,
                                   WorldEngagementEvaluator worldEngagementEvaluator) {
        this.selectTopicsForDifficultyUseCase = selectTopicsForDifficultyUseCase;
        this.worldCatalogUseCase = worldCatalogUseCase;
        this.engagementThresholdConfigUseCase = engagementThresholdConfigUseCase;
        this.worldEngagementEvaluator = worldEngagementEvaluator;
    }

    @Override
    public WorldDestinationSelectionResult selectDestination(Long childSessionId, Long childProfileId,
                                                            WorldEngagementWindow engagementWindow, Integer childAge) {
        Long topicId = selectTopic(childProfileId);
        List<CompatibleActivityProjection> compatibleActivities = getCompatibleActivities(topicId, childAge);

        WorldEngagementThresholdConfig config = engagementThresholdConfigUseCase.engagementThresholdConfig(childProfileId);
        List<WorldEngineEngagementPattern> patterns = worldEngagementEvaluator.evaluatePatterns(engagementWindow, config);
        List<WorldEnginePriorityAdjustment> adjustments = worldEngagementEvaluator.evaluateAdjustments(patterns);

        SelectedWorldActivity selectedActivity = selectActivity(compatibleActivities, adjustments);
        WorldDestination destination = buildDestination(compatibleActivities, selectedActivity, childAge);

        boolean priorityAdjustmentApplied = !adjustments.isEmpty();

        return new WorldDestinationSelectionResult(childSessionId, topicId, destination, selectedActivity, priorityAdjustmentApplied);
    }

    private Long selectTopic(Long childProfileId) {
        TopicSelectionResult result = selectTopicsForDifficultyUseCase.selectTopicsForDifficulty(
            childProfileId, DifficultyLevel.EASY, 1);

        if (result != null && result.getSelectedTopicIds() != null && !result.getSelectedTopicIds().isEmpty()) {
            return result.getSelectedTopicIds().get(0);
        }
        return null;
    }

    private List<CompatibleActivityProjection> getCompatibleActivities(Long topicId, Integer childAge) {
        if (topicId == null) {
            return Collections.emptyList();
        }
        return worldCatalogUseCase.listCompatibleActivitiesByTopic(topicId, childAge);
    }

    private SelectedWorldActivity selectActivity(List<CompatibleActivityProjection> activities,
                                                 List<WorldEnginePriorityAdjustment> adjustments) {
        if (activities == null || activities.isEmpty()) {
            return null;
        }

        Map<String, BigDecimal> adjustmentByEngine = buildAdjustmentMap(adjustments);

        if (adjustmentByEngine.isEmpty()) {
            CompatibleActivityProjection first = activities.get(0);
            return new SelectedWorldActivity(first.activityId(), first.engineType(), first.topicIds().isEmpty() ? null : first.topicIds().get(0),
                SelectedWorldActivity.Source.TOPIC_RECOMMENDATION);
        }

        CompatibleActivityProjection best = null;
        BigDecimal bestScore = BigDecimal.ZERO;

        for (CompatibleActivityProjection activity : activities) {
            BigDecimal baseScore = BigDecimal.ONE;
            BigDecimal engineMultiplier = adjustmentByEngine.getOrDefault(activity.engineType(), BigDecimal.ONE);
            BigDecimal score = baseScore.multiply(engineMultiplier);

            if (score.compareTo(bestScore) > 0) {
                bestScore = score;
                best = activity;
            }
        }

        if (best != null) {
            return new SelectedWorldActivity(best.activityId(), best.engineType(),
                best.topicIds().isEmpty() ? null : best.topicIds().get(0),
                SelectedWorldActivity.Source.TOPIC_RECOMMENDATION);
        }

        return null;
    }

    private Map<String, BigDecimal> buildAdjustmentMap(List<WorldEnginePriorityAdjustment> adjustments) {
        Map<String, BigDecimal> map = new HashMap<>();
        if (adjustments != null) {
            for (WorldEnginePriorityAdjustment adj : adjustments) {
                map.put(adj.getEngineType(), adj.getPriorityMultiplier());
            }
        }
        return map;
    }

    private WorldDestination buildDestination(List<CompatibleActivityProjection> activities,
                                              SelectedWorldActivity selectedActivity, Integer childAge) {
        WorldDestination destination = new WorldDestination();
        destination.setDestinationId(UUID.randomUUID().toString());

        List<WorldHostProjection> hosts = worldCatalogUseCase.listActiveHostsForAge(childAge);
        if (hosts != null && !hosts.isEmpty()) {
            WorldHostProjection host = hosts.get(0);
            destination.setHostId(host.id());
            destination.setHostCode(host.code());
            destination.setHostDisplayName(host.displayName());
            destination.setBiome(host.biome() != null ? host.biome().name() : Biome.MEADOW.name());
        } else {
            destination.setBiome(Biome.MEADOW.name());
        }

        List<WorldNarrativeSituationProjection> situations = worldCatalogUseCase.listActiveSituationsForAge(childAge);
        if (situations != null && !situations.isEmpty()) {
            WorldNarrativeSituationProjection situation = situations.get(0);
            destination.setNarrativeSituationId(situation.id());
            destination.setNarrativeSituationCode(situation.code());
            destination.setDisplayText(situation.displayText());
        }

        List<WorldDiscoveryElementProjection> elements = worldCatalogUseCase.listActiveElementsForAge(childAge);
        List<WorldDiscoveryProposal> proposals = new ArrayList<>();

        if (elements != null) {
            for (WorldDiscoveryElementProjection element : elements) {
                WorldDiscoveryProposal proposal = new WorldDiscoveryProposal();
                proposal.setProposalRuntimeId(UUID.randomUUID().toString());
                proposal.setDiscoveryElementId(element.id());
                proposal.setDiscoveryElementCode(element.code());
                proposal.setDisplayName(element.displayName());
                proposal.setElementType(element.elementType() != null ? element.elementType().name() : ElementType.DISCOVERY.name());
                proposal.setActivityId(element.activityId());
                proposal.setTopicId(element.topicId());
                proposal.setVisualAssetKey(element.visualAssetKey());
                proposal.setInteractionCueType(element.interactionCueType() != null ? element.interactionCueType().name() : null);
                proposals.add(proposal);
            }
        }

        destination.setDiscoveryProposals(proposals);
        return destination;
    }
}