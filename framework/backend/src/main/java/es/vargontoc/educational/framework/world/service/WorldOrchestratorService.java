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
import es.vargontoc.educational.framework.world.ports.out.WorldStateRegistry;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class WorldOrchestratorService implements WorldOrchestrator {

    // Applied when the active host has no authored world_width, so the frontend keeps working
    // exactly as it did before this field existed in the content.
    private static final int DEFAULT_WORLD_WIDTH = 2560;

    private final SelectTopicsForDifficultyUseCase selectTopicsForDifficultyUseCase;
    private final WorldCatalogUseCase worldCatalogUseCase;
    private final EngagementThresholdConfigUseCase engagementThresholdConfigUseCase;
    private final WorldEngagementEvaluator worldEngagementEvaluator;
    private final WorldStateRegistry worldStateRegistry;
    private final WorldExplorationConfig worldExplorationConfig;

    public WorldOrchestratorService(SelectTopicsForDifficultyUseCase selectTopicsForDifficultyUseCase,
                                   WorldCatalogUseCase worldCatalogUseCase,
                                   EngagementThresholdConfigUseCase engagementThresholdConfigUseCase,
                                   WorldEngagementEvaluator worldEngagementEvaluator,
                                   WorldStateRegistry worldStateRegistry,
                                   WorldExplorationConfig worldExplorationConfig) {
        this.selectTopicsForDifficultyUseCase = selectTopicsForDifficultyUseCase;
        this.worldCatalogUseCase = worldCatalogUseCase;
        this.engagementThresholdConfigUseCase = engagementThresholdConfigUseCase;
        this.worldEngagementEvaluator = worldEngagementEvaluator;
        this.worldStateRegistry = worldStateRegistry;
        this.worldExplorationConfig = worldExplorationConfig;
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
        WorldDestination destination = buildDestination(childSessionId, selectedActivity, childAge);

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

    private WorldDestination buildDestination(Long childSessionId, SelectedWorldActivity selectedActivity, Integer childAge) {
        WorldDestination destination = new WorldDestination();
        destination.setDestinationId(UUID.randomUUID().toString());

        // Fase 2 de FEAT-010: el único mapa confirmado es MEADOW. Se filtra explícitamente
        // en vez de depender del fallback (que solo actuaba cuando no había ningún host).
        List<WorldHostProjection> hosts = worldCatalogUseCase.listActiveHostsForAge(childAge);
        List<WorldHostProjection> meadowHosts = (hosts == null) ? Collections.emptyList() :
            hosts.stream().filter(host -> host.biome() == Biome.MEADOW).collect(Collectors.toList());

        if (!meadowHosts.isEmpty()) {
            WorldHostProjection host = meadowHosts.get(0);
            destination.setHostId(host.id());
            destination.setHostCode(host.code());
            destination.setHostDisplayName(host.displayName());
            destination.setWorldWidth(host.worldWidth() != null ? host.worldWidth() : DEFAULT_WORLD_WIDTH);
        } else {
            // No active MEADOW host: keep the existing behavior of not blocking nor throwing a
            // technical error, just leaving host* unset and biome fixed to MEADOW. worldWidth
            // still gets the default so the contract field is always populated.
            destination.setWorldWidth(DEFAULT_WORLD_WIDTH);
        }
        destination.setBiome(Biome.MEADOW.name());

        List<WorldNarrativeSituationProjection> situations = worldCatalogUseCase.listActiveSituationsForAge(childAge);
        if (situations != null && !situations.isEmpty()) {
            WorldNarrativeSituationProjection situation = situations.get(0);
            destination.setNarrativeSituationId(situation.id());
            destination.setNarrativeSituationCode(situation.code());
            destination.setDisplayText(situation.displayText());
        }

        List<WorldDiscoveryElementProjection> elements = worldCatalogUseCase.listActiveElementsByBiomeAndAge(Biome.MEADOW, childAge);
        destination.setDiscoveryProposals(selectVisibleProposals(childSessionId, elements));
        return destination;
    }

    private List<WorldDiscoveryProposal> selectVisibleProposals(Long childSessionId, List<WorldDiscoveryElementProjection> elements) {
        if (elements == null || elements.isEmpty()) {
            return new ArrayList<>();
        }

        List<WorldDiscoveryElementProjection> eligible = elements.stream()
            .sorted(Comparator.comparing(
                WorldDiscoveryElementProjection::sortOrder,
                Comparator.nullsLast(Comparator.naturalOrder())))
            .collect(Collectors.toList());

        int maxVisible = Math.max(1, worldExplorationConfig.getMaxVisibleElements());

        if (eligible.size() <= maxVisible) {
            return eligible.stream().map(this::toProposal).collect(Collectors.toList());
        }

        Set<Long> previouslyVisibleIds = previouslyVisibleElementIds(childSessionId);

        List<WorldDiscoveryElementProjection> notRecentlyShown = new ArrayList<>();
        List<WorldDiscoveryElementProjection> recentlyShown = new ArrayList<>();
        for (WorldDiscoveryElementProjection element : eligible) {
            if (previouslyVisibleIds.contains(element.id())) {
                recentlyShown.add(element);
            } else {
                notRecentlyShown.add(element);
            }
        }

        // Candidate priority order: not-recently-shown first (rotation/variety), then already
        // shown. Both passes below walk this same order.
        List<WorldDiscoveryElementProjection> candidateOrder = new ArrayList<>(notRecentlyShown);
        candidateOrder.addAll(recentlyShown);

        List<WorldDiscoveryElementProjection> selected = new ArrayList<>();
        List<WorldDiscoveryElementProjection> skippedForSeparation = new ArrayList<>();

        // Pass 1: a candidate is only admitted if it respects the minimum separation against
        // what's already selected in this build. This lets an already-shown element (second
        // half of candidateOrder) get picked before accepting two elements that are too close.
        for (WorldDiscoveryElementProjection element : candidateOrder) {
            if (selected.size() >= maxVisible) {
                break;
            }
            if (satisfiesMinimumSeparation(element, selected)) {
                selected.add(element);
            } else {
                skippedForSeparation.add(element);
            }
        }

        // Pass 2: if the pool's position distribution can't satisfy separation for everyone,
        // fill the configured cap anyway (separation is a content-quality goal, not a
        // functional blocking condition).
        for (WorldDiscoveryElementProjection element : skippedForSeparation) {
            if (selected.size() >= maxVisible) {
                break;
            }
            selected.add(element);
        }

        return selected.stream().map(this::toProposal).collect(Collectors.toList());
    }

    private boolean satisfiesMinimumSeparation(WorldDiscoveryElementProjection candidate,
                                               List<WorldDiscoveryElementProjection> alreadySelected) {
        if (candidate.positionX() == null || candidate.positionY() == null) {
            return true;
        }
        double minSeparation = worldExplorationConfig.getMinSeparationNormalized();
        for (WorldDiscoveryElementProjection selected : alreadySelected) {
            if (selected.positionX() == null || selected.positionY() == null) {
                continue;
            }
            double dx = candidate.positionX() - selected.positionX();
            double dy = candidate.positionY() - selected.positionY();
            if (Math.sqrt(dx * dx + dy * dy) < minSeparation) {
                return false;
            }
        }
        return true;
    }

    private Set<Long> previouslyVisibleElementIds(Long childSessionId) {
        if (childSessionId == null) {
            return Collections.emptySet();
        }

        return worldStateRegistry.findByChildSessionId(childSessionId)
            .map(state -> state.getVisibleDiscoveryElements())
            .orElse(Collections.emptyList())
            .stream()
            .map(WorldDiscoveryProposal::getDiscoveryElementId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(HashSet::new));
    }

    private WorldDiscoveryProposal toProposal(WorldDiscoveryElementProjection element) {
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
        proposal.setPositionX(element.positionX());
        proposal.setPositionY(element.positionY());
        return proposal;
    }
}