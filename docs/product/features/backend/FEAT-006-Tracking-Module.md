# FEAT-006 - Backend: Tracking Module

## Status

state: proposal
user_history: Track child attempts, adaptive difficulty, achievements, learning progress, and parental dashboard information.
depends_on: family, session, content
future_depends_on: game, agent, notifications
blocked_by: none
test: unit + integration + contract
sprints:

## Description

The tracking module records child progress during educational activities. It serves two main consumers with different needs:

- Real-time adaptive difficulty, which needs recent attempt data.
- Parental dashboards, agents, and future notifications, which need historical and aggregated progress data.

Tracking owns all child-specific runtime progress. Content keeps static catalog data only, while tracking stores attempts, activity summaries, topic summaries, curiosity views, achievements, and learning path progress.

## Module Responsibilities

- Register child attempts, correct answers, incorrect answers, and timeouts by child, activity, topic, session, and difficulty.
- Store normalized attempt metrics while allowing engine-specific context through flexible metadata.
- Update activity and topic summaries after each attempt to avoid expensive dashboard queries.
- Evaluate reactive adaptive difficulty from recent attempts, response speed, and timeout frequency.
- Provide the current difficulty for a child and activity.
- Register viewed curiosities by child and topic for anti-repetition.
- Reset curiosity cycles when all curiosities for a topic have been viewed.
- Register child achievements.
- Track child progress through learning paths and completed learning path steps.
- Classify topics by performance as weak, medium, or strong for future game engines and the adult agent.
- Provide dashboard read models for parents.
- Delete old atomic attempts using the tracking retention policy.

## Scope Boundaries

In scope:

- Tracking domain model, use cases, persistence, retention job, and dashboard read endpoints.
- Business operations used internally by future game engines.
- Aggregated dashboard queries exposed through REST endpoints.

Out of scope:

- Game engine implementation.
- WebSocket event emission.
- Avatar or TTS orchestration.
- Agent prompt construction.
- Notification delivery.

Tracking may return information that other modules use, but it must not emit `GAME_DIFFICULTY_CHANGED`, `GAME_ACHIEVEMENT_UNLOCKED`, avatar events, or notification events directly.

## Adaptive Difficulty

Reactive adaptive difficulty lives in tracking as a service that the future game module will call after registering an attempt.

The v1.5 algorithm uses the latest N attempts for the child and activity. It remains simple, but avoids increasing difficulty only because the child eventually answers correctly.

- Increase threshold: increase difficulty only when accuracy is high and average response time is at or below the configured target.
- Maintain threshold: keep the current difficulty when accuracy is high but average response time is still elevated.
- Decrease threshold: decrease difficulty when accuracy is low or timeout frequency is high.
- Sliding window: the latest N attempts, configured per activity difficulty level, prevents single good or bad moments from changing the difficulty abruptly.
- Minimum attempts: difficulty must not change until enough attempts exist for a reliable evaluation.
- Cooldown attempts: after a difficulty change, wait a configured number of attempts before changing again.

Metrics considered:

- Accuracy score: success percentage in the sliding window.
- Response speed score: average response time compared with the configured target response time.
- Timeout penalty: penalty applied when timeout frequency is too high.

Tracking may compute an internal `adaptiveScore` normalized from 0 to 100:

- `0-39`: easier difficulty is recommended.
- `40-69`: current difficulty is maintained.
- `70-100`: harder difficulty is recommended only if response time is acceptable.

The visible difficulty still remains the catalog difficulty code, such as `EASY`, `MEDIUM`, or `HARD`.

Expected keys in `DifficultyLevel.adaptiveThresholdConfig`:

- `slidingWindowAttempts`
- `increaseThresholdPercent`
- `decreaseThresholdPercent`
- `minAttemptsBeforeChange`
- `cooldownAttempts`
- `targetResponseTimeMs`
- `timeoutRateThresholdPercent`
- `timeoutPenaltyWeight`
- `accuracyWeight`
- `speedWeight`

The tracking service evaluates the rule after each registered attempt and returns the result to the caller. If there is a change, the future game module will emit `GAME_DIFFICULTY_CHANGED` through WebSocket.

## Response Time And Engine Metrics

`responseTimeMs` is valuable for children aged 3-4 because reaction speed helps identify whether a difficulty level is appropriate. However, not every game interaction is a single stimulus and a single answer.

Tracking rules:

- For simple attempts, `responseTimeMs` stores the time between showing the stimulus and receiving the child answer.
- For multi-interaction rounds, `responseTimeMs` stores an aggregate duration only when it is meaningful for dashboards or adaptive difficulty.
- Engine-specific details are stored in `attemptContext` as JSON/text.
- The game engine decides what counts as one attempt and what result it sends.
- Tracking persists and aggregates normalized data, but does not interpret engine-specific gameplay rules.

Examples:

- Recognition with 2-3 options: `responseTimeMs` is the time until the selected option; `attemptContext` may include `optionCount`, `selectedTopicId`, and `correctTopicId`.
- Bubble recognition variant: `responseTimeMs` may be the round duration; `attemptContext` may include `poppedCorrectCount`, `poppedIncorrectCount`, `targetTopicId`, and `roundDurationMs`.
- Future timing games: `attemptContext` may include timing deltas or aggregate accuracy metrics.

## Parental Dashboard

Dashboard data is built from `ActivitySummary`, `TopicSummary`, `ActivityAttempt`, `ChildAchievement`, `ChildLearningProgress`, and `ChildLearningCompletedStep`.

Main dashboard queries:

- General child summary: global accuracy, played time, completed activities, and recent progress.
- Activity detail: performance evolution over time.
- Topic detail: weak, medium, and strong topics.
- Difficulty evolution: how the child progresses through levels.
- Average response time: confidence and safety indicator.
- Recent sessions: duration and activity by session.
- Achievements: unlocked achievements for the child.
- Learning path progress: current step and completed steps.

Expensive queries should not be recalculated from atomic attempts when a summary can answer them directly. `ActivitySummary` and `TopicSummary` are updated after each attempt so most dashboard reads remain cheap.

## Topic Selection Service

`TopicSelectionService` is a tracking-owned service used by future cognitive engines to select pedagogically useful topics.

Classification by topic performance:

- `WEAK`: more than 40% failures.
- `MEDIUM`: 20% to 40% failures.
- `STRONG`: less than 20% failures.

Initial selection distribution by difficulty:

- `EASY`: 50% weak, 30% medium, 20% strong.
- `MEDIUM`: 60% weak, 30% medium, 10% strong.
- `HARD`: 70% weak, 20% medium, 10% strong.

The service should consume `TopicSummary` records and stable content identifiers. It must not read game state or frontend state.

## Retention Policy

Following the `AbstractRetentionJob` pattern defined in shared:

- `ActivityAttempt`: delete records older than 180 days.
- `ActivitySummary`: do not delete through normal retention; it is the long-term child progress summary.
- `TopicSummary`: do not delete through normal retention; it is the long-term topic performance summary.
- `ChildAchievement`: do not delete through normal retention.
- `ChildLearningProgress`: do not delete through normal retention.
- `ChildLearningCompletedStep`: do not delete through normal retention.
- `CuriosityViewed`: reset by completed curiosity cycle, not by time.

The 180-day retention value is intentionally hardcoded for v1 to keep the feature simple and generous for a single-family application.

## Proposed Schemas

Use the currently implemented backend identifier strategy: `Long` identifiers through the existing `BaseEntity`.

### ActivityAttempt

Atomic record of one attempt or one game-defined round.

- `id`
- `childProfileId`
- `activityId`
- `childSessionId`
- `topicId`
- `difficultyLevelId`
- `result`: `CORRECT`, `INCORRECT`, `TIMEOUT`
- `responseTimeMs`
- `attemptContext`: JSON/text with engine-specific metadata
- `createdAt`
- `updatedAt`

Suggested indexes:

- `childProfileId`, `activityId`, `createdAt`
- `childProfileId`, `topicId`, `createdAt`
- `childSessionId`, `createdAt`
- `createdAt` for retention deletion

### ActivitySummary

Aggregated activity-level progress updated after each attempt.

- `id`
- `childProfileId`
- `activityId`
- `totalAttempts`
- `totalCorrect`
- `totalIncorrect`
- `totalTimeouts`
- `successRatePercent`
- `averageResponseTimeMs`
- `currentDifficultyLevelId`
- `createdAt`
- `updatedAt`

Suggested uniqueness:

- One summary per `childProfileId` and `activityId`.

### TopicSummary

Aggregated topic-level progress updated after each attempt. This aggregate feeds dashboard topic metrics and `TopicSelectionService`.

- `id`
- `childProfileId`
- `topicId`
- `totalAttempts`
- `totalCorrect`
- `totalIncorrect`
- `totalTimeouts`
- `successRatePercent`
- `failureRatePercent`
- `averageResponseTimeMs`
- `performanceBand`: `WEAK`, `MEDIUM`, `STRONG`
- `createdAt`
- `updatedAt`

Suggested uniqueness:

- One summary per `childProfileId` and `topicId`.

### CuriosityViewed

Viewed curiosities by child and topic for anti-repetition.

- `id`
- `childProfileId`
- `topicId`
- `curiosityId`
- `cycleNumber`
- `viewedAt`
- `createdAt`
- `updatedAt`

Suggested indexes:

- `childProfileId`, `topicId`, `cycleNumber`
- `childProfileId`, `curiosityId`

### ChildAchievement

Achievements earned by a child.

- `id`
- `childProfileId`
- `achievementCode`
- `activityId`: nullable
- `topicId`: nullable
- `earnedAt`
- `createdAt`
- `updatedAt`

Suggested uniqueness:

- `childProfileId`, `achievementCode`, nullable `activityId`, nullable `topicId`

### ChildLearningProgress

Current runtime progress for a child in a learning path.

- `id`
- `childProfileId`
- `learningPathId`
- `currentLearningPathStepId`
- `createdAt`
- `updatedAt`

Suggested uniqueness:

- One progress row per `childProfileId` and `learningPathId`.

### ChildLearningCompletedStep

Relational history of completed learning path steps.

- `id`
- `childProfileId`
- `learningPathId`
- `learningPathStepId`
- `completedAt`
- `createdAt`
- `updatedAt`

Suggested uniqueness:

- One completed step per `childProfileId`, `learningPathId`, and `learningPathStepId`.

Suggested indexes:

- `childProfileId`, `learningPathId`, `completedAt`
- `childProfileId`, `learningPathStepId`

## Internal Business Use Cases

- Register an activity attempt.
- Update activity summary after an attempt.
- Update topic summary after an attempt.
- Evaluate adaptive difficulty after an attempt.
- Get the current difficulty for a child and activity.
- Classify topics by performance.
- Register a viewed curiosity.
- Get viewed curiosities for a child and topic.
- Reset a curiosity cycle for a child and topic.
- Register an achievement.
- Get child achievements.
- Update learning path progress.
- Register a completed learning path step.
- Get child learning path progress.
- Delete old activity attempts through the retention job.

## Dashboard REST Operations

REST endpoints should be limited to read operations needed by the parental dashboard.

- Get general child tracking summary.
- Get child performance by activity.
- Get child performance by topic.
- Get child difficulty evolution.
- Get child average response time metrics.
- Get child recent activity/session summary.
- Get child achievements.
- Get child learning path progress.

Any REST endpoint added by this feature requires updating `docs/contracts/api/openapi.json`.

## Contract Impact

- `docs/contracts/api/openapi.json` must be updated only when dashboard REST endpoints are implemented.
- `docs/contracts/api/websocket.json` is not changed by this feature because tracking does not emit WebSocket events directly.
- No dependency on `openapi_tts.json` is required.

## Acceptance Criteria

- Tracking schema is created through a new Liquibase migration.
- Existing migration files are not modified.
- JPA entities use the existing `BaseEntity` with `Long` identifiers.
- Tracking domain follows the backend hexagonal package structure.
- Every port/in use case has at least one unit test.
- Dashboard REST endpoints have positive and negative integration tests.
- `ActivityAttempt` records are deleted when older than 180 days.
- `ActivitySummary`, `TopicSummary`, `ChildAchievement`, `ChildLearningProgress`, and `ChildLearningCompletedStep` survive attempt retention deletion.
- `TopicSummary` feeds `TopicSelectionService` without requiring JSON parsing or attempt recomputation.
- `ChildLearningCompletedStep` stores completed steps relationally without using a JSON list in `ChildLearningProgress`.
- Curiosity anti-repetition works by child, topic, cycle number, and explicit `viewedAt` timestamp.
- Adaptive difficulty uses `DifficultyLevel.adaptiveThresholdConfig` keys documented in this feature.
- Adaptive difficulty considers accuracy, response speed, and timeout penalties.
- Registering an attempt returns whether difficulty changed, without emitting WebSocket events.
- Dashboard endpoints return aggregated data without requiring full recomputation from all attempts where summaries exist.
- OpenAPI is updated after dashboard REST endpoints are added.
- Tracking does not depend on TTS, avatar, or game implementation packages.
