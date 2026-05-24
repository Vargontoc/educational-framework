# FEAT-003 - Backend: Content Module

## Status

state: accepted
user_history: Administrative content catalog management
depends_on: shared, family
owned_by: backend
test: unit + integration + contract
sprints:
- framework/backend/sprints/current.md
- framework/backend/sprints/planned/010-dev-admin-core-catalog-apis.md
- framework/backend/sprints/planned/011-curiosities-and-avatar-fallback-catalog.md
- framework/backend/sprints/planned/012-learning-paths-and-tracing-patterns.md
- framework/backend/sprints/planned/013-stories-catalog-with-parental-access.md
- framework/backend/sprints/planned/014-seeds-and-runtime-content-read-services.md
- framework/backend/sprints/planned/015-contract-hardening-and-integration-readiness.md

## Description

The content module centralizes the static and administrative catalog required by the application: activities, topics, resources, avatar event fallback messages, curiosities, learning paths, tracing patterns, and stories.

This feature focuses on the administrative catalog side of the module. It must provide CRUD and read-only access to reference content used later by game, avatar, reading, agent, and tracking modules.

The tracking module is not implemented yet and is explicitly out of scope for this feature. Content must expose stable identifiers and static configuration so tracking can reference them later, but it must not store child-specific progress, attempt history, viewed curiosities, adaptive difficulty state, or achievements.

Responsibilities:
- Define what content exists.
- Define how content is configured.
- Provide stable catalog identifiers for future modules.
- Provide administrative APIs for managing catalog data.
- Provide read-only APIs for consumers that need active catalog data.

## Scope

In scope:
- Administrative CRUD for content catalog entities.
- Read-only catalog queries for active content.
- Liquibase schema for content tables.
- Seed data loading order and idempotency rules.
- OpenAPI contract updates for content endpoints.
- Backend validation for catalog consistency.

Out of scope:
- Child-specific activity attempts.
- Curiosities viewed by child.
- Learning path progress by child.
- Adaptive difficulty decisions based on attempts.
- Achievements and progress metrics.
- Game runtime state.
- TTS generation, audio cache, or avatar playback.
- Agent prompt execution or LLM calls.

## Ownership Boundaries

Content owns:
- Categories, topics, activities, difficulty configuration, resources, avatar fallback messages, curiosities, learning paths, tracing patterns, stories, and story pages.

Tracking will own in a future feature:
- `CuriosityViewed`.
- `ActivityAttempt`.
- `ActivitySummary`.
- `ChildLearningProgress`.
- `ChildAchievement`.
- Runtime adaptive difficulty decisions.

Avatar owns:
- TTS invocation.
- Audio cache.
- Audio playback protocol.
- Runtime fallback to text/audio.

Game owns:
- Game engines.
- Game loop.
- Game state.
- Runtime selection of activities and elements.

Agent owns:
- LLM interaction.
- Prompt execution.
- Agent health and resilience.
- Generated text, constrained by backend-provided context.

## Schemas

- [ ] Category: groups topics and activities.
    - [ ] Name: ANIMALS, SHAPES, NATURE, NUMBERS, etc.
    - [ ] Display name.
    - [ ] Description.
    - [ ] Status: ACTIVE, INACTIVE, DRAFT.

- [ ] Topic: shared vocabulary used by games, curiosities, and stories.
    - [ ] Name: DOG, CAT, TRIANGLE, SUN, etc.
    - [ ] Category reference.
    - [ ] Recommended age range.
    - [ ] Compatible variants: RECOGNITION, MEMORY, SEQUENCE, TRACING, etc.
    - [ ] Representative image or icon reference, optional.
    - [ ] Status: ACTIVE, INACTIVE, DRAFT.

- [ ] Activity: available activity in the application.
    - [ ] Name and description.
    - [ ] Game engine type.
    - [ ] Recommended age range, coherent with the 3-8 product range and 3-4 MVP focus.
    - [ ] Related topics.
    - [ ] Status: ACTIVE, INACTIVE, DRAFT.
    - [ ] Available difficulty levels.

- [ ] DifficultyLevel: static difficulty configuration for one activity.
    - [ ] Level: EASY, MEDIUM, HARD.
    - [ ] Activity reference.
    - [ ] Parameters by game engine type: time limit, number of elements, speed, tolerance, BPM, etc.
    - [ ] Adaptive thresholds as configuration only: success threshold up, success threshold down, window size.
    - [ ] Status: ACTIVE, INACTIVE, DRAFT.

- [ ] ActivityResource: multimedia resources associated with an activity.
    - [ ] Resource type: IMAGE, AUDIO, VIDEO.
    - [ ] URL or path reference.
    - [ ] Activity reference.
    - [ ] Optional topic reference.
    - [ ] Metadata: size, format, duration, dimensions, alt text.
    - [ ] Status: ACTIVE, INACTIVE, DRAFT.

- [ ] AvatarEventCatalog: extensible catalog of predefined avatar fallback messages.
    - [ ] Event type: GREET, REWARD, HELP, ENCOURAGE.
    - [ ] Tone: FUN, ENERGETIC, CALM, SERIOUS, aligned with the family/avatar design.
    - [ ] Locale: es-ES for v1.
    - [ ] Message text optimized for TTS.
    - [ ] Status: ACTIVE, INACTIVE, DRAFT.

- [ ] Curiosity: short educational fact associated with a topic.
    - [ ] Text optimized for TTS, without complex punctuation or long sentence structures.
    - [ ] Topic reference.
    - [ ] Recommended age range.
    - [ ] Locale: es-ES for v1.
    - [ ] Status: ACTIVE, INACTIVE, DRAFT.

- [ ] LearningPath: static learning path catalog.
    - [ ] Name and description.
    - [ ] Recommended age range.
    - [ ] Ordered steps.
    - [ ] Status: ACTIVE, INACTIVE, DRAFT.

- [ ] LearningPathStep: static step inside a learning path.
    - [ ] Learning path reference.
    - [ ] Activity reference.
    - [ ] Position/order.
    - [ ] Declarative unlock condition.
    - [ ] Optional visual metadata.
    - [ ] Status: ACTIVE, INACTIVE, DRAFT.

- [ ] TracingPattern: normalized control points for DotConnectionEngine.
    - [ ] Topic reference.
    - [ ] Pattern type.
    - [ ] Ordered normalized points.
    - [ ] Recommended age range.
    - [ ] Status: ACTIVE, INACTIVE, DRAFT.

- [ ] Story: story catalog entry for the future reading module.
    - [ ] Title and description.
    - [ ] Recommended age range.
    - [ ] Estimated duration.
    - [ ] Optional related topics.
    - [ ] Optional background music resource.
    - [ ] Status: ACTIVE, INACTIVE, DRAFT.

- [ ] StoryPage: page inside a story.
    - [ ] Story reference.
    - [ ] Page order.
    - [ ] Text.
    - [ ] Image resource reference.
    - [ ] Optional prerecorded audio resource.
    - [ ] Status: ACTIVE, INACTIVE, DRAFT.

## Enums

- [ ] ContentStatus: ACTIVE, INACTIVE, DRAFT.
- [ ] DifficultyCode: EASY, MEDIUM, HARD.
- [ ] ResourceType: IMAGE, AUDIO, VIDEO.
- [ ] AvatarEventType: GREET, REWARD, HELP, ENCOURAGE.
- [ ] Tone: FUN, ENERGETIC, CALM, SERIOUS.
- [ ] Locale: es-ES in v1.

## Seed Loading Order

Seeds must be idempotent and safe to run multiple times.

1. Categories.
2. Topics, referencing categories.
3. Curiosities, referencing topics.
4. Activities, referencing topics.
5. DifficultyLevels, referencing activities.
6. AvatarEventCatalog.
7. LearningPaths and LearningPathSteps.
8. TracingPatterns.
9. Stories and StoryPages.

## API Plan

Development administrative APIs:
- [ ] POST /api/v1/dev/content/categories
- [ ] PUT /api/v1/dev/content/categories/{id}
- [ ] GET /api/v1/dev/content/categories
- [ ] POST /api/v1/dev/content/topics
- [ ] PUT /api/v1/dev/content/topics/{id}
- [ ] GET /api/v1/dev/content/topics
- [ ] POST /api/v1/dev/content/activities
- [ ] PUT /api/v1/dev/content/activities/{id}
- [ ] GET /api/v1/dev/content/activities
- [ ] POST /api/v1/dev/content/curiosities
- [ ] PUT /api/v1/dev/content/curiosities/{id}
- [ ] GET /api/v1/dev/content/curiosities
- [ ] POST /api/v1/dev/content/learning-paths
- [ ] PUT /api/v1/dev/content/learning-paths/{id}
- [ ] GET /api/v1/dev/content/learning-paths
- [ ] POST /api/v1/dev/content/stories
- [ ] PUT /api/v1/dev/content/stories/{id}
- [ ] GET /api/v1/dev/content/stories

Development API rules:
- [ ] Development administrative controllers must be registered only with Spring profile `dev`.
- [ ] Outside profile `dev`, `/api/v1/dev/content/**` endpoints must not exist.
- [ ] Development administrative APIs are not part of the production product surface.

Read-only APIs:
- [ ] GET /api/v1/content/categories
- [ ] GET /api/v1/content/topics
- [ ] GET /api/v1/content/activities
- [ ] GET /api/v1/content/activities/{id}
- [ ] GET /api/v1/content/activities/{id}/difficulty-levels
- [ ] GET /api/v1/content/topics/{id}/curiosities
- [ ] GET /api/v1/content/learning-paths
- [ ] GET /api/v1/content/stories
- [ ] GET /api/v1/content/stories/{id}

Production runtime consumption:
- [ ] In production, game engines and WebSocket flows consume content through backend services, not development administrative APIs.
- [ ] Public read-only APIs should exist only when required by a runtime/frontend flow.
- [ ] Productive story endpoints require parental PIN/session authorization.

Contract rules:
- [ ] All REST endpoints must return `ApiResponse<T>` where applicable.
- [ ] After endpoint changes, update `docs/contracts/api/openapi.json`.
- [ ] Frontend must consume only OpenAPI-derived types.

## Persistence Plan

- [ ] Add a new Liquibase migration after the current latest backend migration.
- [ ] Do not modify existing migration files.
- [ ] JPA entities must extend the existing `BaseEntity` with `Long` identifiers.
- [ ] Add indexes for common filters: status, categoryId, topicId, activityId, locale, age range.
- [ ] Use foreign keys for catalog relationships.
- [ ] Store flexible engine parameters and metadata as JSON/text only where the structure is intentionally engine-specific.

## Future Tracking Integration

Tracking will consume content identifiers but will own all child-specific runtime state.

Stable identifiers exposed by content for future tracking references:
- `activityId`.
- `topicId`.
- `curiosityId`.
- `difficultyLevelId`.
- `learningPathId`.
- `learningPathStepId`.

Future tracking-owned entities:
- `CuriosityViewed`: childProfileId, curiosityId, viewedAt.
- `ActivityAttempt`: childProfileId, activityId, childSessionId, topicId, difficultyLevelId, result, responseTimeMs.
- `ActivitySummary`: aggregated progress by child/activity/topic.
- `ChildLearningProgress`: childProfileId, learningPathId, current step, completed steps.
- `ChildAchievement`: childProfileId, achievementId, earnedAt.

Rules:
- [ ] Do not create tracking tables in this feature.
- [ ] Do not add child progress columns to content tables.
- [ ] Do not add fields such as viewed, completed, attempts, score, progress, or lastPlayedAt to content entities.
- [ ] LearningPathStep in content is static; per-child step status belongs to tracking later.
- [ ] DifficultyLevel stores thresholds as configuration; adaptive decisions belong to tracking/game later.

## Mitigations By Layer

### Agents

- Agents must not read content persistence directly.
- Backend must pass only filtered catalog context to agents.
- Agent responses must not invent topics, activities, curiosities, or event types outside the content catalog.
- `AvatarEventCatalog` must be available as fallback when the GameAgent is down or times out.
- Avatar fallback messages must respect the existing `content_text` maximum length used by the agent/TTS flow.

### Backend

- Keep the content module under the backend hexagonal structure: `content/model`, `content/ports/in`, `content/ports/out`, `content/service`, `content/application`, `content/infrastructure`.
- Reuse shared exceptions, validators, `ApiResponse`, and `BaseEntity`.
- Use a new Liquibase migration for content schema changes.
- Keep content catalog state separate from child runtime state.
- Validate age ranges, statuses, enum values, locale, and required relationships.
- Ensure seed loading is idempotent.
- Update OpenAPI after adding or changing endpoints.
- Add unit tests for each use case and integration tests for REST controllers.
- Register development administrative controllers only under Spring profile `dev`.
- Verify `/api/v1/dev/content/**` is unavailable outside profile `dev`.
- Protect productive story read endpoints with parental PIN/session authorization.

### Frontend

- Frontend must consume content APIs only through `docs/contracts/api/openapi.json`.
- Development admin UI must treat content status as editorial/catalog status, not child progress.
- Development admin UI must not be part of the production product surface.
- Frontend must not duplicate backend enums manually if they can be derived from the API contract.
- Resource paths or URLs must be treated as opaque references.
- If avatar audio is unavailable, UI must support text fallback from backend/avatar flows.
- Do not implement tracking dashboards or child progress screens as part of this feature.

## Acceptance Criteria

- [ ] Content schema exists through a new Liquibase migration.
- [ ] Content domain model, ports, services, application layer, and persistence adapters are implemented.
- [ ] Development administrative CRUD endpoints exist for the required catalog entities under `/api/v1/dev/content/**`.
- [ ] Development administrative endpoints are available only with Spring profile `dev`.
- [ ] Development administrative endpoints are unavailable outside Spring profile `dev`.
- [ ] Read-only content endpoints return active catalog data.
- [ ] Productive story endpoints require parental PIN/session authorization.
- [ ] Seed loading order is documented and implemented idempotently.
- [ ] Tracking-specific state is not implemented in content.
- [ ] OpenAPI contract is updated.
- [ ] Unit tests cover content use cases.
- [ ] Integration tests cover positive and negative REST scenarios.
- [ ] Frontend-facing responses use `ApiResponse<T>` consistently.
