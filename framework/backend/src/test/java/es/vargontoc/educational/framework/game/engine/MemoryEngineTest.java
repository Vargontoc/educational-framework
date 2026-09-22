package es.vargontoc.educational.framework.game.engine;

import es.vargontoc.educational.framework.content.model.DifficultyCode;
import es.vargontoc.educational.framework.game.model.ActionResult;
import es.vargontoc.educational.framework.game.model.ActionResultType;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.GameStatus;
import es.vargontoc.educational.framework.game.model.enums.EngineType;
import es.vargontoc.educational.framework.game.model.memory.MemoryBoardConfig;
import es.vargontoc.educational.framework.game.model.memory.MemoryCard;
import es.vargontoc.educational.framework.game.model.memory.MemoryContentMode;
import es.vargontoc.educational.framework.game.model.memory.MemoryDifficultyLadder;
import es.vargontoc.educational.framework.game.model.memory.MemoryState;
import es.vargontoc.educational.framework.tracking.model.AttemptResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** SPRINT-109: board, turns, neutral turn-back and completion of the MemoryEngine (ADR-030). */
class MemoryEngineTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Ten elements in two thematic groups of five: "1".."5" fruit, "6".."10" vehicle. */
    private static final List<String> CANDIDATES = List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");

    private MutableClock clock;
    private MemoryEngine engine;

    @BeforeEach
    void setUp() {
        clock = new MutableClock();
        engine = new MemoryEngine(new Random(7), clock);
    }

    // --- helpers -----------------------------------------------------------------------------------------------

    private static String groupOf(String elementId) {
        return Integer.parseInt(elementId) <= 5 ? "fruit" : "vehicle";
    }

    private String engineParams(DifficultyCode difficulty, List<String> candidates) throws Exception {
        MemoryBoardConfig board = MemoryDifficultyLadder.forDifficulty(difficulty);
        Map<String, Object> memory = new LinkedHashMap<>();
        memory.put("rows", board.rows());
        memory.put("columns", board.columns());
        memory.put("flipDelayMs", board.flipDelayMs());
        memory.put("contentMode", board.contentMode().name());

        List<Map<String, Object>> metadata = new ArrayList<>();
        for (String id : candidates) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", id);
            entry.put("topicId", 1);
            entry.put("similarityGroup", groupOf(id));
            metadata.add(entry);
        }

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("candidates", candidates);
        root.put("candidateMetadata", metadata);
        root.put("memoryParams", memory);
        return MAPPER.writeValueAsString(root);
    }

    private GameState init(DifficultyCode difficulty) throws Exception {
        GameState gameState = new GameState();
        engine.initGame(gameState, engineParams(difficulty, CANDIDATES));
        return gameState;
    }

    private MemoryState state(GameState gameState) throws Exception {
        return MAPPER.readValue(gameState.getEnginePayload(), MemoryState.class);
    }

    private static String tap(String cardId) {
        return "{\"cardId\":\"" + cardId + "\",\"responseTimeMs\":800}";
    }

    private MemoryCard card(GameState gameState, String cardId) throws Exception {
        return state(gameState).getCards().stream()
                .filter(c -> c.getCardId().equals(cardId)).findFirst().orElseThrow();
    }

    /** The two cards of one pair, in board order. */
    private List<String> pairOf(GameState gameState, int pairIndex) throws Exception {
        Map<String, List<String>> byElement = new LinkedHashMap<>();
        for (MemoryCard c : state(gameState).getCards()) {
            byElement.computeIfAbsent(c.getElementId(), k -> new ArrayList<>()).add(c.getCardId());
        }
        return new ArrayList<>(byElement.values()).get(pairIndex);
    }

    /** Two cards that are not a pair. */
    private List<String> mismatchingCards(GameState gameState) throws Exception {
        List<MemoryCard> cards = state(gameState).getCards();
        MemoryCard first = cards.get(0);
        MemoryCard other = cards.stream()
                .filter(c -> !c.getElementId().equals(first.getElementId())).findFirst().orElseThrow();
        return List.of(first.getCardId(), other.getCardId());
    }

    private Set<String> elementsOf(MemoryState state) {
        return state.getCards().stream().map(MemoryCard::getElementId).collect(Collectors.toSet());
    }

    // --- initGame: board per difficulty ------------------------------------------------------------------------

    @Test
    void initGame_easy_dealsA2x2BoardWithTwoPairsAnd2000msVisibility() throws Exception {
        GameState gameState = init(DifficultyCode.EASY);
        MemoryState state = state(gameState);

        assertEquals(EngineType.MEMORY, gameState.getEngine());
        assertEquals(GameStatus.IN_PROGRESS, gameState.getStatus());
        assertEquals(2, state.getRows());
        assertEquals(2, state.getColumns());
        assertEquals(4, state.getCards().size());
        assertEquals(2, state.getTotalPairs());
        assertEquals(2000, state.getFlipDelayMs());
    }

    @Test
    void initGame_medium_dealsA2x3BoardWithThreePairsAnd1500msVisibility() throws Exception {
        MemoryState state = state(init(DifficultyCode.MEDIUM));

        assertEquals(2, state.getRows());
        assertEquals(3, state.getColumns());
        assertEquals(6, state.getCards().size());
        assertEquals(3, state.getTotalPairs());
        assertEquals(1500, state.getFlipDelayMs());
    }

    @Test
    void initGame_hard_dealsA2x4BoardWithFourPairsAnd1000msVisibility() throws Exception {
        MemoryState state = state(init(DifficultyCode.HARD));

        assertEquals(2, state.getRows());
        assertEquals(4, state.getColumns());
        assertEquals(8, state.getCards().size());
        assertEquals(4, state.getTotalPairs());
        assertEquals(1000, state.getFlipDelayMs());
    }

    @Test
    void initGame_everyElementAppearsExactlyTwiceAndAllCardsStartFaceDown() throws Exception {
        for (DifficultyCode difficulty : DifficultyCode.values()) {
            MemoryState state = state(init(difficulty));

            Map<String, Long> count = state.getCards().stream()
                    .collect(Collectors.groupingBy(MemoryCard::getElementId, Collectors.counting()));
            assertEquals(state.getTotalPairs(), count.size(), difficulty.name());
            assertTrue(count.values().stream().allMatch(n -> n == 2), difficulty.name());
            assertTrue(state.getCards().stream().noneMatch(c -> c.isFaceUp() || c.isMatched()), difficulty.name());
            assertEquals(0, state.getMatchedPairs());
            assertFalse(state.isWaitingForFlipBack());
            assertNull(state.getFirstFlippedCardId());
        }
    }

    @Test
    void initGame_cardsHaveUniqueIdsAndTheirPositionInTheGrid() throws Exception {
        MemoryState state = state(init(DifficultyCode.HARD));

        Set<String> ids = new HashSet<>();
        Set<String> positions = new HashSet<>();
        for (MemoryCard c : state.getCards()) {
            ids.add(c.getCardId());
            positions.add(c.getRow() + ":" + c.getColumn());
            assertTrue(c.getRow() >= 0 && c.getRow() < state.getRows());
            assertTrue(c.getColumn() >= 0 && c.getColumn() < state.getColumns());
        }
        assertEquals(8, ids.size());
        assertEquals(8, positions.size());
    }

    @Test
    void initGame_easy_mixesElementsFromDifferentGroups() throws Exception {
        for (int seed = 0; seed < 30; seed++) {
            engine = new MemoryEngine(new Random(seed), clock);
            MemoryState state = state(init(DifficultyCode.EASY));

            Set<String> groups = elementsOf(state).stream().map(MemoryEngineTest::groupOf).collect(Collectors.toSet());
            assertEquals(2, groups.size(), "seed " + seed + ": high contrast needs two different groups");
        }
    }

    @Test
    void initGame_hard_usesElementsOfOneSingleGroup() throws Exception {
        for (int seed = 0; seed < 30; seed++) {
            engine = new MemoryEngine(new Random(seed), clock);
            MemoryState state = state(init(DifficultyCode.HARD));

            Set<String> groups = elementsOf(state).stream().map(MemoryEngineTest::groupOf).collect(Collectors.toSet());
            assertEquals(1, groups.size(), "seed " + seed + ": every pair must come from the same group");
        }
    }

    @Test
    void initGame_hard_withoutAGroupBigEnoughFallsBackToAnyElements() throws Exception {
        // Groups of 3 and 3: none has the four elements HARD needs.
        List<String> candidates = List.of("1", "2", "3", "6", "7", "8");
        GameState gameState = new GameState();
        engine.initGame(gameState, engineParams(DifficultyCode.HARD, candidates));

        MemoryState state = state(gameState);
        assertEquals(4, state.getTotalPairs());
        assertEquals(8, state.getCards().size());
    }

    @Test
    void initGame_fewerElementsThanThePairsOfTheLevel_shrinksTheBoardInsteadOfFailing() throws Exception {
        GameState gameState = new GameState();
        engine.initGame(gameState, engineParams(DifficultyCode.HARD, List.of("1", "2", "3")));

        MemoryState state = state(gameState);
        assertEquals(3, state.getTotalPairs());
        assertEquals(6, state.getCards().size());
        assertEquals(2, state.getRows());
        assertEquals(3, state.getColumns());
    }

    @Test
    void initGame_duplicatedCandidatesAreDealtOnlyOnce() throws Exception {
        GameState gameState = new GameState();
        engine.initGame(gameState, engineParams(DifficultyCode.EASY, List.of("1", "1", "6", "6")));

        MemoryState state = state(gameState);
        assertEquals(Set.of("1", "6"), elementsOf(state));
        assertEquals(4, state.getCards().size());
    }

    @Test
    void initGame_withoutParams_defaultsToTheEasyBoard() {
        GameState gameState = new GameState();
        engine.initGame(gameState, null);

        assertEquals(GameStatus.IN_PROGRESS, gameState.getStatus());
        assertNotNull(gameState.getEnginePayload());
        assertTrue(engine.isGameComplete(gameState), "no elements to deal: nothing left to find");
    }

    @Test
    void ladder_matchesAdr030() {
        assertEquals(new MemoryBoardConfig(2, 2, 2000, MemoryContentMode.HIGH_CONTRAST),
                MemoryDifficultyLadder.forDifficulty(DifficultyCode.EASY));
        assertEquals(new MemoryBoardConfig(2, 3, 1500, MemoryContentMode.STANDARD),
                MemoryDifficultyLadder.forDifficulty(DifficultyCode.MEDIUM));
        assertEquals(new MemoryBoardConfig(2, 4, 1000, MemoryContentMode.SAME_CATEGORY),
                MemoryDifficultyLadder.forDifficulty(DifficultyCode.HARD));
        assertEquals(MemoryDifficultyLadder.EASY, MemoryDifficultyLadder.forDifficulty(null));
    }

    // --- processAction -----------------------------------------------------------------------------------------

    @Test
    void processAction_firstCard_turnsItFaceUp() throws Exception {
        GameState gameState = init(DifficultyCode.MEDIUM);
        String cardId = state(gameState).getCards().get(0).getCardId();

        ActionResult result = engine.processAction(gameState, tap(cardId));

        assertTrue(card(gameState, cardId).isFaceUp());
        assertFalse(card(gameState, cardId).isMatched());
        assertEquals(cardId, state(gameState).getFirstFlippedCardId());
        assertEquals(ActionResultType.CORRECT, result.getResultType());
        assertFalse(result.isCompleted());
        assertEquals(MemoryEngine.EVENT_FIRST_FLIP, MAPPER.readTree(result.getAttemptContext()).get("event").asString());
        assertEquals(800, result.getResponseTimeMs());
    }

    @Test
    void processAction_secondCardThatMatches_marksBothMatchedAndKeepsThemFaceUp() throws Exception {
        GameState gameState = init(DifficultyCode.MEDIUM);
        List<String> pair = pairOf(gameState, 0);

        engine.processAction(gameState, tap(pair.get(0)));
        ActionResult result = engine.processAction(gameState, tap(pair.get(1)));

        for (String cardId : pair) {
            assertTrue(card(gameState, cardId).isMatched());
            assertTrue(card(gameState, cardId).isFaceUp(), "matched cards stay face up");
        }
        MemoryState state = state(gameState);
        assertEquals(1, state.getMatchedPairs());
        assertNull(state.getFirstFlippedCardId());
        assertFalse(state.isWaitingForFlipBack());
        assertEquals(ActionResultType.CORRECT, result.getResultType());
        assertEquals(MemoryEngine.EVENT_MATCH, MAPPER.readTree(result.getAttemptContext()).get("event").asString());
    }

    @Test
    void processAction_secondCardThatDoesNotMatch_waitsForFlipBackWithBothCardsFaceUp() throws Exception {
        GameState gameState = init(DifficultyCode.MEDIUM);
        List<String> wrong = mismatchingCards(gameState);

        engine.processAction(gameState, tap(wrong.get(0)));
        ActionResult result = engine.processAction(gameState, tap(wrong.get(1)));

        MemoryState state = state(gameState);
        assertTrue(state.isWaitingForFlipBack());
        assertEquals(wrong, state.getFlipBackCardIds());
        assertNotNull(state.getFlipBackAt());
        assertTrue(card(gameState, wrong.get(0)).isFaceUp());
        assertTrue(card(gameState, wrong.get(1)).isFaceUp());
        assertFalse(card(gameState, wrong.get(0)).isMatched());
        assertFalse(card(gameState, wrong.get(1)).isMatched());
        assertEquals(0, state.getMatchedPairs());
        assertNull(state.getFirstFlippedCardId());
        assertEquals(ActionResultType.INCORRECT, result.getResultType());
        assertEquals(MemoryEngine.EVENT_MISMATCH, MAPPER.readTree(result.getAttemptContext()).get("event").asString());
    }

    @Test
    void processAction_afterAMismatch_turnsThePairBackAndPlaysTheNewCard() throws Exception {
        GameState gameState = init(DifficultyCode.MEDIUM);
        List<String> wrong = mismatchingCards(gameState);
        engine.processAction(gameState, tap(wrong.get(0)));
        engine.processAction(gameState, tap(wrong.get(1)));
        String next = state(gameState).getCards().stream()
                .map(MemoryCard::getCardId).filter(id -> !wrong.contains(id)).findFirst().orElseThrow();

        engine.processAction(gameState, tap(next));

        MemoryState state = state(gameState);
        assertFalse(state.isWaitingForFlipBack());
        assertTrue(state.getFlipBackCardIds().isEmpty());
        assertFalse(card(gameState, wrong.get(0)).isFaceUp());
        assertFalse(card(gameState, wrong.get(1)).isFaceUp());
        assertTrue(card(gameState, next).isFaceUp());
        assertEquals(next, state.getFirstFlippedCardId());
    }

    @Test
    void processAction_tapOnACardThatIsAlreadyFaceUpOrMatchedOrUnknown_changesNothing() throws Exception {
        GameState gameState = init(DifficultyCode.MEDIUM);
        List<String> pair = pairOf(gameState, 0);
        engine.processAction(gameState, tap(pair.get(0)));
        engine.processAction(gameState, tap(pair.get(1)));
        String before = gameState.getEnginePayload();

        for (String cardId : new String[] {pair.get(0), pair.get(1), "card-99", null}) {
            ActionResult result = engine.processAction(gameState, "{\"cardId\":" + (cardId == null ? "null" : "\"" + cardId + "\"") + "}");
            assertEquals(MemoryEngine.EVENT_IGNORED, MAPPER.readTree(result.getAttemptContext()).get("event").asString());
            assertEquals(ActionResultType.CORRECT, result.getResultType());
        }
        assertEquals(before, gameState.getEnginePayload());
    }

    @Test
    void processAction_tapOnTheFirstCardAgain_isIgnored() throws Exception {
        GameState gameState = init(DifficultyCode.MEDIUM);
        String cardId = state(gameState).getCards().get(0).getCardId();
        engine.processAction(gameState, tap(cardId));

        engine.processAction(gameState, tap(cardId));

        assertEquals(cardId, state(gameState).getFirstFlippedCardId());
        assertEquals(0, state(gameState).getPairAttempts());
    }

    @Test
    void processAction_tapOnAFaceUpMismatchedCardWhileWaiting_isIgnoredAndKeepsTheWait() throws Exception {
        GameState gameState = init(DifficultyCode.MEDIUM);
        List<String> wrong = mismatchingCards(gameState);
        engine.processAction(gameState, tap(wrong.get(0)));
        engine.processAction(gameState, tap(wrong.get(1)));

        engine.processAction(gameState, tap(wrong.get(0)));

        assertTrue(state(gameState).isWaitingForFlipBack());
        assertTrue(card(gameState, wrong.get(0)).isFaceUp());
    }

    @Test
    void processAction_onceTheTimeIsUp_theMismatchedCardsCanBeTappedAgain() throws Exception {
        GameState gameState = init(DifficultyCode.MEDIUM);
        List<String> wrong = mismatchingCards(gameState);
        engine.processAction(gameState, tap(wrong.get(0)));
        engine.processAction(gameState, tap(wrong.get(1)));

        clock.advance(Duration.ofMillis(1600));
        ActionResult result = engine.processAction(gameState, tap(wrong.get(0)));

        assertEquals(MemoryEngine.EVENT_FIRST_FLIP, MAPPER.readTree(result.getAttemptContext()).get("event").asString());
        assertFalse(state(gameState).isWaitingForFlipBack());
        assertTrue(card(gameState, wrong.get(0)).isFaceUp());
        assertFalse(card(gameState, wrong.get(1)).isFaceUp());
        assertEquals(wrong.get(0), state(gameState).getFirstFlippedCardId());
    }

    @Test
    void processAction_flipBackAtIsFlipDelayAfterTheMismatch() throws Exception {
        GameState gameState = init(DifficultyCode.HARD);
        List<String> wrong = mismatchingCards(gameState);
        engine.processAction(gameState, tap(wrong.get(0)));
        engine.processAction(gameState, tap(wrong.get(1)));

        MemoryState state = state(gameState);
        assertEquals(Duration.ofMillis(1000), Duration.between(
                java.time.LocalDateTime.now(clock), state.getFlipBackAt()));
    }

    @Test
    void processAction_updatesTheGameCounters() throws Exception {
        GameState gameState = init(DifficultyCode.MEDIUM);
        List<String> wrong = mismatchingCards(gameState);
        engine.processAction(gameState, tap(wrong.get(0)));
        engine.processAction(gameState, tap(wrong.get(1)));
        clock.advance(Duration.ofSeconds(3));
        List<String> pair = pairOf(gameState, 0);
        engine.processAction(gameState, tap(pair.get(0)));
        engine.processAction(gameState, tap(pair.get(1)));

        assertEquals(2, gameState.getAttempts());
        assertEquals(1, gameState.getCorrectAttempts());
        assertEquals(1, gameState.getIncorrectAttempts());
    }

    // --- tracking: attempt context and buffer (SPRINT-110) -----------------------------------------------------

    private JsonNode context(ActionResult result) throws Exception {
        return MAPPER.readTree(result.getAttemptContext());
    }

    @Test
    void attemptContext_ofAMatch_describesThePairAndItsElement() throws Exception {
        GameState gameState = init(DifficultyCode.MEDIUM);
        List<String> pair = pairOf(gameState, 0);
        engine.processAction(gameState, tap(pair.get(0)));

        JsonNode ctx = context(engine.processAction(gameState,
                "{\"cardId\":\"" + pair.get(1) + "\",\"responseTimeMs\":1234}"));

        assertEquals("MEMORY", ctx.get("engineType").asString());
        assertEquals(pair.get(0), ctx.get("cardId1").asString());
        assertEquals(pair.get(1), ctx.get("cardId2").asString());
        assertEquals(card(gameState, pair.get(0)).getElementId(), ctx.get("elementId").asString());
        assertTrue(ctx.get("match").asBoolean());
        assertTrue(ctx.get("firstTry").asBoolean());
        assertEquals(1, ctx.get("attemptNumber").asInt());
        assertEquals(1234, ctx.get("responseTimeMs").asLong());
        assertNotNull(ctx.get("memoryCategory"));
    }

    @Test
    void attemptContext_ofAMismatch_hasBothCardsAndNoElement() throws Exception {
        GameState gameState = init(DifficultyCode.MEDIUM);
        List<String> wrong = mismatchingCards(gameState);
        engine.processAction(gameState, tap(wrong.get(0)));

        JsonNode ctx = context(engine.processAction(gameState, tap(wrong.get(1))));

        assertEquals(wrong.get(0), ctx.get("cardId1").asString());
        assertEquals(wrong.get(1), ctx.get("cardId2").asString());
        assertTrue(ctx.get("elementId").isNull(), "no single element to attribute a non-matching pair to");
        assertFalse(ctx.get("match").asBoolean());
        assertFalse(ctx.get("firstTry").asBoolean());
        assertEquals(1, ctx.get("attemptNumber").asInt());
    }

    @Test
    void attemptContext_ofAFirstFlip_hasOnlyThatCardAndDoesNotRevealItsElement() throws Exception {
        GameState gameState = init(DifficultyCode.MEDIUM);
        String cardId = state(gameState).getCards().get(0).getCardId();

        JsonNode ctx = context(engine.processAction(gameState, tap(cardId)));

        assertEquals(MemoryEngine.EVENT_FIRST_FLIP, ctx.get("event").asString());
        assertEquals(cardId, ctx.get("cardId1").asString());
        assertTrue(ctx.get("cardId2").isNull());
        assertTrue(ctx.get("elementId").isNull());
    }

    @Test
    void buffer_holdsOnlyResolvedPairs_inOrder_notFirstFlipsNorIgnoredTaps() throws Exception {
        GameState gameState = init(DifficultyCode.MEDIUM);
        List<String> wrong = mismatchingCards(gameState);
        engine.processAction(gameState, tap(wrong.get(0)));
        assertEquals(0, state(gameState).getRoundAttempts().size(), "a first flip is not an attempt");
        engine.processAction(gameState, tap(wrong.get(0)));
        engine.processAction(gameState, tap("card-99"));
        engine.processAction(gameState, tap(wrong.get(1)));
        clock.advance(Duration.ofSeconds(3));
        List<String> pair = pairOf(gameState, 0);
        engine.processAction(gameState, tap(pair.get(0)));
        engine.processAction(gameState, tap(pair.get(1)));

        MemoryState state = state(gameState);
        assertEquals(2, state.getRoundAttempts().size());
        assertEquals(AttemptResult.INCORRECT, state.getRoundAttempts().get(0).result());
        assertNull(state.getRoundAttempts().get(0).elementId());
        assertEquals(AttemptResult.CORRECT, state.getRoundAttempts().get(1).result());
        assertEquals(card(gameState, pair.get(0)).getElementId(), state.getRoundAttempts().get(1).elementId());
        assertEquals(800, state.getRoundAttempts().get(1).responseTimeMs());
        assertEquals(2, state.getPairAttempts());
        assertEquals(800L * 4, state.getTotalResponseTimeMs(), "only the taps that turn a card add their response time");
    }

    @Test
    void firstTry_isLostOnceAnElementWasPartOfAMismatch() throws Exception {
        GameState gameState = init(DifficultyCode.EASY);
        MemoryCard a = state(gameState).getCards().get(0);
        MemoryCard b = state(gameState).getCards().stream()
                .filter(c -> !c.getElementId().equals(a.getElementId())).findFirst().orElseThrow();
        engine.processAction(gameState, tap(a.getCardId()));
        engine.processAction(gameState, tap(b.getCardId()));
        clock.advance(Duration.ofSeconds(3));
        // Both elements were in the mismatch: neither pair is a first-try match any more.
        for (int i = 0; i < 2; i++) {
            List<String> pair = pairOf(gameState, i);
            engine.processAction(gameState, tap(pair.get(0)));
            ActionResult result = engine.processAction(gameState, tap(pair.get(1)));
            assertFalse(context(result).get("firstTry").asBoolean(), "pair " + i);
        }
        assertEquals(0, state(gameState).getTotalCorrectFirstTry());
    }

    @Test
    void metrics_aGameWithoutMistakesCountsEveryMatchAsFirstTry() throws Exception {
        GameState gameState = init(DifficultyCode.MEDIUM);
        solveAll(gameState);

        MemoryState state = state(gameState);
        assertEquals(3, state.getTotalPairs());
        assertEquals(3, state.getPairAttempts());
        assertEquals(3, state.getTotalCorrectFirstTry());
        assertEquals(3, state.getRoundAttempts().size());
        assertTrue(state.getRoundAttempts().stream().allMatch(r -> r.result() == AttemptResult.CORRECT));
        assertEquals(800L * 6, state.getTotalResponseTimeMs());
    }

    @Test
    void memoryCategory_isTheSharedGroupInHardAndMixedInEasy() throws Exception {
        String hard = state(init(DifficultyCode.HARD)).getMemoryCategory();
        String easy = state(init(DifficultyCode.EASY)).getMemoryCategory();

        assertTrue(Set.of("fruit", "vehicle").contains(hard), hard);
        assertEquals("MIXED", easy);
    }

    // --- getNextElement ----------------------------------------------------------------------------------------

    @Test
    void getNextElement_describesTheBoardWithoutRevealingFaceDownCards() throws Exception {
        GameState gameState = init(DifficultyCode.MEDIUM);
        String firstCard = state(gameState).getCards().get(0).getCardId();
        engine.processAction(gameState, tap(firstCard));

        JsonNode board = MAPPER.readTree(engine.getNextElement(gameState));

        assertEquals(2, board.get("rows").asInt());
        assertEquals(3, board.get("columns").asInt());
        assertEquals(3, board.get("totalPairs").asInt());
        assertEquals(1500, board.get("flipDelayMs").asInt());
        assertEquals(3, board.get("elementIds").size());
        assertEquals(6, board.get("cards").size());
        for (JsonNode c : board.get("cards")) {
            boolean shown = c.get("faceUp").asBoolean() || c.get("matched").asBoolean();
            assertEquals(shown, !c.get("elementId").isNull(), "only visible cards disclose their element");
            assertEquals(c.get("cardId").asString().equals(firstCard), c.get("faceUp").asBoolean());
        }
    }

    @Test
    void getNextElement_presentsTheMismatchedPairAsTurnedBackOnceItsTimeIsDue() throws Exception {
        GameState gameState = init(DifficultyCode.MEDIUM);
        List<String> wrong = mismatchingCards(gameState);
        engine.processAction(gameState, tap(wrong.get(0)));
        engine.processAction(gameState, tap(wrong.get(1)));

        clock.advance(Duration.ofMillis(1400));
        JsonNode waiting = MAPPER.readTree(engine.getNextElement(gameState));
        assertTrue(waiting.get("waitingForFlipBack").asBoolean());
        assertEquals(2, countFaceUp(waiting));

        clock.advance(Duration.ofMillis(100));
        JsonNode turnedBack = MAPPER.readTree(engine.getNextElement(gameState));
        assertFalse(turnedBack.get("waitingForFlipBack").asBoolean());
        assertEquals(0, countFaceUp(turnedBack));
        assertTrue(state(gameState).isWaitingForFlipBack(), "reading does not change the stored state");
    }

    private int countFaceUp(JsonNode board) {
        int n = 0;
        for (JsonNode c : board.get("cards")) {
            if (c.get("faceUp").asBoolean()) {
                n++;
            }
        }
        return n;
    }

    @Test
    void getNextElement_keepsMatchedPairsFaceUpAfterTheTurnBack() throws Exception {
        GameState gameState = init(DifficultyCode.MEDIUM);
        List<String> pair = pairOf(gameState, 0);
        engine.processAction(gameState, tap(pair.get(0)));
        engine.processAction(gameState, tap(pair.get(1)));
        List<MemoryCard> rest = state(gameState).getCards().stream().filter(c -> !c.isMatched()).toList();
        MemoryCard a = rest.get(0);
        MemoryCard b = rest.stream().filter(c -> !c.getElementId().equals(a.getElementId())).findFirst().orElseThrow();
        engine.processAction(gameState, tap(a.getCardId()));
        engine.processAction(gameState, tap(b.getCardId()));

        clock.advance(Duration.ofSeconds(5));
        JsonNode board = MAPPER.readTree(engine.getNextElement(gameState));

        assertEquals(2, countFaceUp(board), "only the matched pair remains face up");
        assertEquals(1, board.get("matchedPairs").asInt());
    }

    // --- completion --------------------------------------------------------------------------------------------

    private void solveAll(GameState gameState) throws Exception {
        int pairs = state(gameState).getTotalPairs();
        for (int i = 0; i < pairs; i++) {
            List<String> pair = pairOf(gameState, i);
            engine.processAction(gameState, tap(pair.get(0)));
            engine.processAction(gameState, tap(pair.get(1)));
        }
    }

    @Test
    void isGameComplete_isTrueOnlyWhenEveryPairIsMatched() throws Exception {
        GameState gameState = init(DifficultyCode.MEDIUM);
        assertFalse(engine.isGameComplete(gameState));

        for (int i = 0; i < 3; i++) {
            List<String> pair = pairOf(gameState, i);
            engine.processAction(gameState, tap(pair.get(0)));
            ActionResult result = engine.processAction(gameState, tap(pair.get(1)));
            assertEquals(i == 2, engine.isGameComplete(gameState), "after pair " + i);
            assertEquals(i == 2, result.isCompleted(), "after pair " + i);
        }
    }

    @Test
    void completedBoard_leavesEveryCardFaceUpAndMatched() throws Exception {
        GameState gameState = init(DifficultyCode.HARD);
        solveAll(gameState);

        MemoryState state = state(gameState);
        assertEquals(4, state.getMatchedPairs());
        assertTrue(state.getCards().stream().allMatch(c -> c.isFaceUp() && c.isMatched()));
        assertNull(engine.getNextElement(gameState));
    }

    @Test
    void processAction_afterCompletion_doesNothing() throws Exception {
        GameState gameState = init(DifficultyCode.EASY);
        solveAll(gameState);
        String before = gameState.getEnginePayload();

        ActionResult result = engine.processAction(gameState, tap("card-0"));

        assertTrue(result.isCompleted());
        assertEquals(before, gameState.getEnginePayload());
    }

    @Test
    void stars_aGameWithoutMistakesEarnsThree() throws Exception {
        GameState perfect = init(DifficultyCode.MEDIUM);
        solveAll(perfect);

        assertEquals(3, perfect.getStarsEarned());
    }

    @Test
    void calculateStars_bandsByAttemptsPerPair() {
        MemoryState state = new MemoryState();
        state.setTotalPairs(4);

        state.setPairAttempts(4);
        assertEquals(3, engine.calculateStars(state));
        state.setPairAttempts(6);
        assertEquals(3, engine.calculateStars(state));
        state.setPairAttempts(7);
        assertEquals(2, engine.calculateStars(state));
        state.setPairAttempts(10);
        assertEquals(2, engine.calculateStars(state));
        state.setPairAttempts(11);
        assertEquals(1, engine.calculateStars(state));
    }

    @Test
    void buildSummary_completesTheGameWithItsStars() throws Exception {
        GameState gameState = init(DifficultyCode.EASY);
        solveAll(gameState);

        ActionResult summary = engine.buildSummary(gameState);

        assertEquals(GameStatus.COMPLETED, gameState.getStatus());
        assertNotNull(gameState.getCompletedAt());
        assertEquals(3, gameState.getStarsEarned());
        assertTrue(summary.isCompleted());
    }

    /** Clock the tests can move. */
    private static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-09-21T10:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
