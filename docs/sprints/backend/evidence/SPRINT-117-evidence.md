# SPRINT-117 Evidence

## Technical Summary

Implemented server-side caching of static/semi-static data and query optimizations to reduce redundant DB access in the WebSocket game flow.

### Cache Infrastructure
- `GameCacheStorage`: central Caffeine-based cache holder with per-cache size/TTL configuration via `application.yml`
- `CacheProperties`: configurable max sizes and TTLs for each cache region
- `CacheConfiguration`: wires the `GameCacheStorage` bean with individual Caffeine instances
- Cache metrics via Micrometer counters (`game.cache.hits`, `game.cache.misses`, `ws.data.cache`)

### Caches Implemented
1. **RecognitionElement** by id (includes parsed resourceRefs)
2. **AccessibleColor** by id
3. **AccessibleColorPalette** by (accessibleColorId, colorVisionMode)
4. **World catalog** (hosts, situations, elements, compatible activities) by (biome/age/topic)
5. **Avatar events** by event type and filters
6. **ChildProfile** by id with invalidation on adult edit

### Invalidation
- `ChildProfileService.updateChild`, `changeActiveState`, `deleteChild` invalidate the profile cache
- `AvatarEventCatalogService.createAvatarEvent`, `updateAvatarEvent` invalidate avatar event caches
- `GameCacheStorage.invalidateAll()` available for content manager CRUD invalidation
- TTL acts as a safety net for any missed invalidation

### Query Optimizations
- N+1 in `resolveCandidates`/`resolveMemoryCandidates` replaced with `findByTopicIdInAndStatus` batch query
- `RoundAudioService` accepts pre-loaded `ChildProfile` and `RecognitionElement` to avoid re-querying
- `resolveColorVisionMode` in handler uses cached ChildProfile
- `resolveAccessibleColor` uses cached AccessibleColor and AccessibleColorPalette

### Age Centralization
- `ChildAgeResolver` in `world` module: returns 3 today, extensible for birth date derivation
- `GameWebSocketHandler.getNewWorld` and `handleWorldTravel` use `childAgeResolver.resolveEffectiveAgeBoxed()` instead of literal `3`
- Cache keys include age parameter

## Files Modified

### New Files
- `shared/config/CacheProperties.java` - cache configuration properties
- `shared/config/CacheConfiguration.java` - Spring cache configuration
- `shared/infrastructure/GameCacheStorage.java` - central cache holder
- `world/service/ChildAgeResolver.java` - centralized age resolver
- `test/.../world/service/ChildAgeResolverTest.java` - age resolver tests
- `test/.../shared/infrastructure/GameCacheStorageTest.java` - cache storage tests

### Modified Files
- `session/infrastructure/websocket/GameWebSocketHandler.java` - uses cache + ChildAgeResolver
- `session/infrastructure/websocket/WebSocketConfig.java` - wires new dependencies
- `session/infrastructure/websocket/WebSocketMetrics.java` - data cache counters
- `content/service/WorldCatalogService.java` - caches all catalog queries
- `content/service/AvatarEventCatalogService.java` - caches event lookups, invalidates on CRUD
- `content/service/RecognitionElementService.java` - (no changes needed, caching in handler)
- `content/application/ContentModuleConfiguration.java` - wires GameCacheStorage
- `content/ports/out/RecognitionElementRepository.java` - added `findByTopicIdInAndStatus`
- `content/infrastructure/persistence/RecognitionElementJpaRepository.java` - added batch query
- `content/infrastructure/persistence/RecognitionElementPersistenceAdapter.java` - implemented batch query
- `family/service/ChildProfileService.java` - caches getChild, invalidates on update/delete
- `game/service/GameOrchestratorService.java` - batch candidate queries
- `game/service/RoundAudioService.java` - accepts pre-loaded data
- `world/application/WorldModuleConfiguration.java` - registers ChildAgeResolver bean
- `resources/application.yml` - cache configuration properties
- Test files updated for new constructor signatures

## Migrations and Contracts
- No database migrations required
- No contract changes (WS messages unchanged)

## Tests Executed
- `mvn test -Dtest="ChildAgeResolverTest,GameCacheStorageTest,GameWebSocketHandlerTest,GameWebSocketHandlerSprint115Test,GameWebSocketHandlerSprint116Test,WorldCatalogServiceTest,AvatarEventCatalogServiceTest,RoundAudioServiceTest,GameOrchestratorServiceTest,ChildProfileServiceTest"`
- Result: 149 tests, 0 failures, 0 errors

## Manual Test Guide

### 1. Change color mode from parental panel
1. Start the server and connect a child session
2. Start a game with COLOR engine
3. From the parental panel, change the child's color vision mode
4. Start a new game or trigger a new round
5. Verify: the new palette is used without server restart

### 2. Edit element from content manager
1. Start the server and connect a child session
2. From the dev content manager, edit a RecognitionElement
3. Wait for TTL (30 min default) or restart server
4. Verify: the edited element appears in the game

### 3. Compare perceived time of auth and first touch
1. Baseline: measure `auth` and first `game_action` duration from SPRINT-114 metrics
2. After cache warm-up (second connection), measure again
3. Verify: `auth` p95 < 300ms, `game_action` p95 < 100ms
4. Check `ws.data.cache` metric for hits increasing

## Decisions Taken
1. Used direct Caffeine caches instead of Spring `@Cacheable` for finer control over per-cache settings and invalidation
2. Cache keys are simple strings for world catalog (e.g., "hosts:age=3")
3. ChildProfile cache uses the profile ID as key; invalidation is explicit on update
4. TTLs are conservative (10-60 min) as safety net; invalidation is the primary mechanism
5. `GameCacheStorage` is a singleton shared across all services

## Risks and Debt
- Content manager CRUD endpoints don't yet call `gameCacheStorage.invalidateAll()` for all content types (only avatar events). World catalog and recognition element caches rely on TTL for content changes from dev CRUD.
- The `ChildAgeResolver` is hardcoded to 3; future sprint needs to derive from profile birth date.
- Integration tests that require testcontainers were not run (pre-existing infrastructure requirement).
