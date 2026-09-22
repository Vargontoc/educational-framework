package es.vargontoc.educational.framework.game.engine;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import es.vargontoc.educational.framework.game.model.ActionResult;
import es.vargontoc.educational.framework.game.model.ActionResultType;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.GameStatus;
import es.vargontoc.educational.framework.game.model.enums.EngineType;
import es.vargontoc.educational.framework.game.model.memory.MemoryAttemptContext;
import es.vargontoc.educational.framework.game.model.memory.MemoryBoardConfig;
import es.vargontoc.educational.framework.game.model.memory.MemoryCard;
import es.vargontoc.educational.framework.game.model.memory.MemoryContentMode;
import es.vargontoc.educational.framework.game.model.memory.MemoryDefaults;
import es.vargontoc.educational.framework.game.model.memory.MemoryDifficultyLadder;
import es.vargontoc.educational.framework.game.model.memory.MemoryRoundAttemptRecord;
import es.vargontoc.educational.framework.game.model.memory.MemoryState;
import es.vargontoc.educational.framework.game.model.recognition.CandidateMetadata;
import es.vargontoc.educational.framework.game.ports.in.GameEnginePort;
import es.vargontoc.educational.framework.tracking.model.AttemptResult;

/**
 * Classic pairs game (ADR-030). The board is dealt at {@link #initGame}; each {@link #processAction} turns one card.
 *
 * <p>Neutral turn-back: a non-matching pair is not reported as a failure to the child. The pair stays face up
 * ({@code waitingForFlipBack}) for {@code flipDelayMs} and turns back on its own. The engine keeps no timers:
 * the turn-back is resolved lazily: once its time is due, the next {@link #processAction} applies it first and
 * {@link #getNextElement} presents the pair as already turned back. Touching another card before its time is up
 * also turns the pair back, so no tap is lost.
 *
 * <p>The engine is stateless (everything lives in {@code GameState.enginePayload}), so one instance is shared
 * by all games.
 */
public class MemoryEngine implements GameEnginePort {

    /** What a processed action did. Carried in the attempt context; the result type alone cannot tell them apart. */
    public static final String EVENT_FIRST_FLIP = "FIRST_FLIP";
    public static final String EVENT_MATCH = "MATCH";
    public static final String EVENT_MISMATCH = "MISMATCH";
    public static final String EVENT_IGNORED = "IGNORED";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Random random;
    private final Clock clock;

    public MemoryEngine() {
        this(new Random(), Clock.systemDefaultZone());
    }

    public MemoryEngine(Random random) {
        this(random, Clock.systemDefaultZone());
    }

    public MemoryEngine(Random random, Clock clock) {
        this.random = random;
        this.clock = clock;
    }

    @Override
    public void initGame(GameState gameState, String engineParams) {
        gameState.setStatus(GameStatus.IN_PROGRESS);
        gameState.setAttempts(0);
        gameState.setCorrectAttempts(0);
        gameState.setIncorrectAttempts(0);
        gameState.setTimeoutAttempts(0);
        gameState.setCurrentScore(BigDecimal.ZERO);
        gameState.setCurrentStreak(0);
        gameState.setStarsEarned(0);
        gameState.setSequenceNumber(0);
        gameState.setSystemEventPending(false);
        gameState.setStartedAt(LocalDateTime.now(clock));
        gameState.setEngine(EngineType.MEMORY);

        MemoryBoardConfig config = parseBoardConfig(engineParams);
        List<String> candidates = parseCandidates(engineParams);
        Map<String, String> groupByElementId = parseGroupsByElementId(engineParams);

        gameState.setEnginePayload(serializeState(buildInitialState(config, candidates, groupByElementId)));
    }

    @Override
    public ActionResult processAction(GameState gameState, String actionPayload) {
        MemoryState state = deserializeState(gameState.getEnginePayload());

        if (isComplete(state)) {
            return buildAlreadyCompleteResult(gameState);
        }

        // A non-matching pair whose time is up is already face down for the child: apply it before reading the tap.
        if (state.isWaitingForFlipBack() && isFlipBackDue(state)) {
            turnBackPendingPair(state);
        }

        String cardId = parseCardId(actionPayload);
        Integer responseTimeMs = parseResponseTimeMs(actionPayload);

        MemoryCard card = findCard(state, cardId);
        if (card == null || card.isMatched() || card.isFaceUp()) {
            // Touching a card that is not there, already matched or still face up (the pair being shown) changes nothing.
            MemoryAttemptContext ignored = newContext(state, EVENT_IGNORED, responseTimeMs);
            ignored.setCardId1(cardId);
            return buildResult(gameState, state, ActionResultType.CORRECT, responseTimeMs, ignored);
        }

        turnBackPendingPair(state);

        state.setLastActionAt(LocalDateTime.now(clock));
        if (responseTimeMs != null) {
            state.setTotalResponseTimeMs(state.getTotalResponseTimeMs() + responseTimeMs);
        }
        card.setFaceUp(true);

        ActionResultType resultType;
        MemoryAttemptContext context;
        String firstCardId = state.getFirstFlippedCardId();
        if (firstCardId == null) {
            state.setFirstFlippedCardId(card.getCardId());
            resultType = ActionResultType.CORRECT;
            context = newContext(state, EVENT_FIRST_FLIP, responseTimeMs);
            context.setAttemptNumber(state.getPairAttempts() + 1);
            context.setCardId1(card.getCardId());
        } else {
            MemoryCard first = findCard(state, firstCardId);
            state.setFirstFlippedCardId(null);
            state.setPairAttempts(state.getPairAttempts() + 1);
            if (first != null && first.getElementId().equals(card.getElementId())) {
                first.setMatched(true);
                card.setMatched(true);
                state.setMatchedPairs(state.getMatchedPairs() + 1);
                resultType = ActionResultType.CORRECT;
                context = newContext(state, EVENT_MATCH, responseTimeMs);
                context.setMatch(true);
                context.setElementId(card.getElementId());
                context.setFirstTry(!state.getMismatchedElementIds().contains(card.getElementId()));
                if (context.isFirstTry()) {
                    state.setTotalCorrectFirstTry(state.getTotalCorrectFirstTry() + 1);
                }
            } else {
                state.setMismatchedAttempts(state.getMismatchedAttempts() + 1);
                state.setWaitingForFlipBack(true);
                List<String> pending = new ArrayList<>();
                pending.add(firstCardId);
                pending.add(card.getCardId());
                state.setFlipBackCardIds(pending);
                state.setFlipBackAt(LocalDateTime.now(clock).plusNanos(state.getFlipDelayMs() * 1_000_000L));
                resultType = ActionResultType.INCORRECT;
                context = newContext(state, EVENT_MISMATCH, responseTimeMs);
                // Neither element is "the" element of a pair that did not match: no elementId to attribute it to.
                for (String elementId : new String[] {first != null ? first.getElementId() : null, card.getElementId()}) {
                    if (elementId != null && !state.getMismatchedElementIds().contains(elementId)) {
                        state.getMismatchedElementIds().add(elementId);
                    }
                }
            }
            context.setCardId1(firstCardId);
            context.setCardId2(card.getCardId());
            bufferPairAttempt(state, resultType, responseTimeMs, context);
        }

        boolean completed = isComplete(state);
        gameState.setAttempts(state.getPairAttempts());
        gameState.setCorrectAttempts(state.getMatchedPairs());
        gameState.setIncorrectAttempts(state.getMismatchedAttempts());
        if (completed) {
            gameState.setStarsEarned(calculateStars(state));
        }
        return buildResult(gameState, state, resultType, responseTimeMs, context);
    }

    /** Builds the context of the action being processed; the caller fills in the cards and the outcome. */
    private MemoryAttemptContext newContext(MemoryState state, String event, Integer responseTimeMs) {
        MemoryAttemptContext context = new MemoryAttemptContext();
        context.setMemoryCategory(state.getMemoryCategory());
        context.setEvent(event);
        context.setAttemptNumber(state.getPairAttempts());
        context.setResponseTimeMs(responseTimeMs != null ? responseTimeMs : 0L);
        return context;
    }

    /**
     * Keeps a resolved pair in the buffer. Nothing reaches tracking until the game completes (the orchestrator
     * flushes it then); an abandoned game just drops it.
     */
    private void bufferPairAttempt(MemoryState state, ActionResultType resultType, Integer responseTimeMs,
                                   MemoryAttemptContext context) {
        AttemptResult result = resultType == ActionResultType.CORRECT ? AttemptResult.CORRECT : AttemptResult.INCORRECT;
        state.getRoundAttempts().add(new MemoryRoundAttemptRecord(
                context.getElementId(), result, responseTimeMs, serializeAttemptContext(context)));
    }

    /**
     * Board as the client must draw it. The element of a face-down card is left out so the layout is not
     * disclosed; {@code elementIds} lists the distinct elements of the board for preloading. A non-matching pair
     * whose time is due is presented already turned back.
     */
    @Override
    public String getNextElement(GameState gameState) {
        MemoryState state = deserializeState(gameState.getEnginePayload());
        if (isComplete(state)) {
            return null;
        }
        boolean turnBackDue = state.isWaitingForFlipBack() && isFlipBackDue(state);
        List<String> turningBack = turnBackDue ? state.getFlipBackCardIds() : List.of();

        List<Map<String, Object>> cards = new ArrayList<>();
        for (MemoryCard card : state.getCards()) {
            boolean faceUp = card.isFaceUp() && !turningBack.contains(card.getCardId());
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("cardId", card.getCardId());
            entry.put("row", card.getRow());
            entry.put("column", card.getColumn());
            entry.put("faceUp", faceUp);
            entry.put("matched", card.isMatched());
            entry.put("elementId", faceUp || card.isMatched() ? card.getElementId() : null);
            cards.add(entry);
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("rows", state.getRows());
        map.put("columns", state.getColumns());
        map.put("totalPairs", state.getTotalPairs());
        map.put("matchedPairs", state.getMatchedPairs());
        map.put("flipDelayMs", state.getFlipDelayMs());
        map.put("waitingForFlipBack", state.isWaitingForFlipBack() && !turnBackDue);
        map.put("elementIds", distinctElementIds(state));
        map.put("cards", cards);
        try {
            return OBJECT_MAPPER.writeValueAsString(map);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize next element", e);
        }
    }

    @Override
    public boolean isGameComplete(GameState gameState) {
        return isComplete(deserializeState(gameState.getEnginePayload()));
    }

    @Override
    public ActionResult buildSummary(GameState gameState) {
        MemoryState state = deserializeState(gameState.getEnginePayload());

        gameState.setStatus(GameStatus.COMPLETED);
        gameState.setStarsEarned(calculateStars(state));
        gameState.setCompletedAt(LocalDateTime.now(clock));
        gameState.setAttempts(state.getPairAttempts());
        gameState.setCorrectAttempts(state.getMatchedPairs());
        gameState.setIncorrectAttempts(state.getMismatchedAttempts());

        ActionResult result = new ActionResult();
        result.setResultType(ActionResultType.CORRECT);
        result.setNewState(gameState);
        result.setCompleted(true);
        return result;
    }

    /**
     * Stars by how many pair attempts the child needed per pair (the minimum is 1). Never shown as a score
     * of mistakes: it is only a completion reward.
     */
    int calculateStars(MemoryState state) {
        if (state.getTotalPairs() <= 0) {
            return 3;
        }
        double attemptsPerPair = (double) state.getPairAttempts() / state.getTotalPairs();
        if (attemptsPerPair <= MemoryDefaults.THREE_STARS_MAX_ATTEMPTS_PER_PAIR) {
            return 3;
        }
        if (attemptsPerPair <= MemoryDefaults.TWO_STARS_MAX_ATTEMPTS_PER_PAIR) {
            return 2;
        }
        return 1;
    }

    // --- Board -------------------------------------------------------------------------------------------------

    private MemoryState buildInitialState(
            MemoryBoardConfig config, List<String> candidates, Map<String, String> groupByElementId) {
        List<String> elements = selectElements(candidates, groupByElementId, config.pairCount(), config.contentMode());
        int pairs = elements.size();

        // Fewer elements than the level asks for: keep two rows and shrink the board instead of failing.
        int rows = config.rows();
        int columns = config.columns();
        if (pairs < config.pairCount()) {
            rows = pairs == 0 ? 0 : 2;
            columns = pairs;
        }

        List<String> deck = new ArrayList<>();
        for (String element : elements) {
            deck.add(element);
            deck.add(element);
        }
        Collections.shuffle(deck, random);

        List<MemoryCard> cards = new ArrayList<>();
        for (int i = 0; i < deck.size(); i++) {
            cards.add(new MemoryCard("card-" + i, deck.get(i), i / columns, i % columns));
        }

        MemoryState state = new MemoryState();
        state.setRows(rows);
        state.setColumns(columns);
        state.setTotalPairs(pairs);
        state.setFlipDelayMs(config.flipDelayMs());
        state.setContentMode(config.contentMode());
        state.setMemoryCategory(memoryCategoryOf(elements, groupByElementId));
        state.setCards(cards);
        state.setLastActionAt(LocalDateTime.now(clock));
        return state;
    }

    /** The group all the elements share, "MIXED" when there are several, null when none is known. */
    private String memoryCategoryOf(List<String> elements, Map<String, String> groupByElementId) {
        Set<String> groups = new LinkedHashSet<>();
        for (String element : elements) {
            String group = groupByElementId.get(element);
            if (group != null && !group.isBlank()) {
                groups.add(group);
            }
        }
        if (groups.isEmpty()) {
            return null;
        }
        return groups.size() == 1 ? groups.iterator().next() : "MIXED";
    }

    /**
     * Picks the elements of the board according to the level's content mode. When the candidates cannot honour the
     * mode (no group with enough elements, elements without group) it degrades to a plain random selection.
     */
    List<String> selectElements(
            List<String> candidates, Map<String, String> groupByElementId, int pairs, MemoryContentMode mode) {
        List<String> distinct = new ArrayList<>(new LinkedHashSet<>(candidates));
        int wanted = Math.min(pairs, distinct.size());
        if (wanted == 0) {
            return new ArrayList<>();
        }

        MemoryContentMode effective = mode != null ? mode : MemoryContentMode.STANDARD;
        if (effective == MemoryContentMode.SAME_CATEGORY) {
            List<String> sameGroup = selectFromOneGroup(distinct, groupByElementId, wanted);
            if (sameGroup != null) {
                return sameGroup;
            }
        } else if (effective == MemoryContentMode.HIGH_CONTRAST) {
            return selectAcrossGroups(distinct, groupByElementId, wanted);
        }

        Collections.shuffle(distinct, random);
        return new ArrayList<>(distinct.subList(0, wanted));
    }

    /** All the elements from one randomly chosen group that has enough of them, or null when no group does. */
    private List<String> selectFromOneGroup(List<String> elements, Map<String, String> groupByElementId, int wanted) {
        Map<String, List<String>> byGroup = new LinkedHashMap<>();
        for (String element : elements) {
            String group = groupByElementId.get(element);
            if (group != null && !group.isBlank()) {
                byGroup.computeIfAbsent(group, g -> new ArrayList<>()).add(element);
            }
        }
        List<List<String>> eligible = byGroup.values().stream().filter(members -> members.size() >= wanted).toList();
        if (eligible.isEmpty()) {
            return null;
        }
        List<String> members = new ArrayList<>(eligible.get(random.nextInt(eligible.size())));
        Collections.shuffle(members, random);
        return new ArrayList<>(members.subList(0, wanted));
    }

    /** Takes elements round-robin over the groups, so the board mixes as many different groups as possible. */
    private List<String> selectAcrossGroups(List<String> elements, Map<String, String> groupByElementId, int wanted) {
        Map<String, List<String>> byGroup = new LinkedHashMap<>();
        for (String element : elements) {
            String group = groupByElementId.get(element);
            // An element without group counts as a group of its own.
            String key = group != null && !group.isBlank() ? "group:" + group : "element:" + element;
            byGroup.computeIfAbsent(key, k -> new ArrayList<>()).add(element);
        }
        List<List<String>> groups = new ArrayList<>(byGroup.values());
        Collections.shuffle(groups, random);
        groups.forEach(members -> Collections.shuffle(members, random));

        List<String> selected = new ArrayList<>();
        for (int round = 0; selected.size() < wanted; round++) {
            for (List<String> members : groups) {
                if (round < members.size() && selected.size() < wanted) {
                    selected.add(members.get(round));
                }
            }
        }
        return selected;
    }

    // --- Turn-back ---------------------------------------------------------------------------------------------

    private boolean isFlipBackDue(MemoryState state) {
        return state.getFlipBackAt() == null || !LocalDateTime.now(clock).isBefore(state.getFlipBackAt());
    }

    /** Turns the non-matching pair back face down and clears the wait. No-op when there is no pending pair. */
    private void turnBackPendingPair(MemoryState state) {
        if (!state.isWaitingForFlipBack()) {
            return;
        }
        for (String pendingCardId : state.getFlipBackCardIds()) {
            MemoryCard pending = findCard(state, pendingCardId);
            if (pending != null && !pending.isMatched()) {
                pending.setFaceUp(false);
            }
        }
        state.setWaitingForFlipBack(false);
        state.setFlipBackCardIds(new ArrayList<>());
        state.setFlipBackAt(null);
    }

    // --- Helpers -----------------------------------------------------------------------------------------------

    private boolean isComplete(MemoryState state) {
        return state.getMatchedPairs() >= state.getTotalPairs();
    }

    private MemoryCard findCard(MemoryState state, String cardId) {
        if (cardId == null) {
            return null;
        }
        for (MemoryCard card : state.getCards()) {
            if (cardId.equals(card.getCardId())) {
                return card;
            }
        }
        return null;
    }

    private List<String> distinctElementIds(MemoryState state) {
        return new ArrayList<>(state.getCards().stream()
                .map(MemoryCard::getElementId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)));
    }

    private ActionResult buildResult(GameState gameState, MemoryState state, ActionResultType resultType,
                                     Integer responseTimeMs, MemoryAttemptContext context) {
        gameState.setEnginePayload(serializeState(state));

        ActionResult result = new ActionResult();
        result.setResultType(resultType);
        result.setResponseTimeMs(responseTimeMs);
        result.setNewState(gameState);
        result.setAttemptContext(serializeAttemptContext(context));
        result.setCompleted(isComplete(state));
        return result;
    }

    private ActionResult buildAlreadyCompleteResult(GameState gameState) {
        ActionResult result = new ActionResult();
        result.setResultType(ActionResultType.CORRECT);
        result.setNewState(gameState);
        result.setCompleted(true);
        return result;
    }

    private String serializeAttemptContext(MemoryAttemptContext context) {
        try {
            return OBJECT_MAPPER.writeValueAsString(context);
        } catch (JacksonException e) {
            return null;
        }
    }

    // --- Serialisation and parsing -----------------------------------------------------------------------------

    private String serializeState(MemoryState state) {
        try {
            return OBJECT_MAPPER.writeValueAsString(state);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize MemoryState", e);
        }
    }

    private MemoryState deserializeState(String payload) {
        if (payload == null || payload.isBlank()) {
            return new MemoryState();
        }
        try {
            return OBJECT_MAPPER.readValue(payload, MemoryState.class);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to deserialize MemoryState", e);
        }
    }

    private JsonNode readTree(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (JacksonException e) {
            return null;
        }
    }

    /** Board of the level from {@code memoryParams}; EASY when absent or incomplete. */
    private MemoryBoardConfig parseBoardConfig(String engineParams) {
        MemoryBoardConfig fallback = MemoryDifficultyLadder.EASY;
        JsonNode params = readTree(engineParams);
        JsonNode node = params != null ? params.get("memoryParams") : null;
        if (node == null || node.isNull()) {
            return fallback;
        }
        int rows = node.hasNonNull("rows") ? node.get("rows").asInt(fallback.rows()) : fallback.rows();
        int columns = node.hasNonNull("columns") ? node.get("columns").asInt(fallback.columns()) : fallback.columns();
        int flipDelayMs = node.hasNonNull("flipDelayMs")
                ? node.get("flipDelayMs").asInt(fallback.flipDelayMs()) : fallback.flipDelayMs();
        MemoryContentMode mode = fallback.contentMode();
        if (node.hasNonNull("contentMode")) {
            try {
                mode = MemoryContentMode.valueOf(node.get("contentMode").asString());
            } catch (IllegalArgumentException e) {
                mode = fallback.contentMode();
            }
        }
        if (rows <= 0 || columns <= 0 || (rows * columns) % 2 != 0) {
            return new MemoryBoardConfig(fallback.rows(), fallback.columns(), flipDelayMs, mode);
        }
        return new MemoryBoardConfig(rows, columns, flipDelayMs, mode);
    }

    private List<String> parseCandidates(String engineParams) {
        JsonNode node = readTree(engineParams);
        JsonNode candidates = node != null ? node.get("candidates") : null;
        if (candidates == null || !candidates.isArray()) {
            return List.of();
        }
        return OBJECT_MAPPER.convertValue(candidates, new TypeReference<List<String>>() {});
    }

    private Map<String, String> parseGroupsByElementId(String engineParams) {
        JsonNode node = readTree(engineParams);
        JsonNode metadata = node != null ? node.get("candidateMetadata") : null;
        Map<String, String> groups = new LinkedHashMap<>();
        if (metadata == null || !metadata.isArray()) {
            return groups;
        }
        List<CandidateMetadata> parsed = OBJECT_MAPPER.convertValue(
                metadata, new TypeReference<List<CandidateMetadata>>() {});
        for (CandidateMetadata candidate : parsed) {
            if (candidate.id() != null && candidate.similarityGroup() != null) {
                groups.put(candidate.id(), candidate.similarityGroup());
            }
        }
        return groups;
    }

    String parseCardId(String actionPayload) {
        JsonNode node = readTree(actionPayload);
        JsonNode cardNode = node != null ? node.get("cardId") : null;
        return cardNode == null || cardNode.isNull() ? null : cardNode.asString();
    }

    Integer parseResponseTimeMs(String actionPayload) {
        JsonNode node = readTree(actionPayload);
        JsonNode timeNode = node != null ? node.get("responseTimeMs") : null;
        return timeNode == null || timeNode.isNull() ? null : timeNode.asInt();
    }
}
