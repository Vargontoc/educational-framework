# Sprint 005 - backend
# -----------------------------------------------

## Goal
Complete FEAT-001 (Family Module) by implementing the infrastructure layer: JPA entities, persistence adapters, DTOs, REST controllers, and integration tests — delivering a fully working REST API for Family, ChildProfile, and AdultProfile.

## Status
status: active
started_at: 2026-05-01 00:00:00
closed_at:
blocked_by:
waiting_for:

## Tasks

### Security — permit family endpoints
- [ ] Update `shared/config/SecurityConfig.java` — add `.requestMatchers("/api/v1/family/**").permitAll()` before `anyRequest().authenticated()` so that family endpoints are reachable without JWT; JWT authentication is planned for a future sprint

### JPA Entities
- [ ] Create `family/infrastructure/persistence/FamilyJpaEntity.java` — `@Entity @Table(name = "family")`, extends `BaseEntity`; fields: `name` (varchar 100), `pinHash` (`@Column(name = "pin_hash")`), `ttsEnabled` (`@Column(name = "tts_enabled")`), `agentEnabled` (`@Column(name = "agent_enabled")`); no `@OneToMany` — children loaded via their own repository
- [ ] Create `family/infrastructure/persistence/ChildProfileJpaEntity.java` — `@Entity @Table(name = "child_profile")`, extends `BaseEntity`; fields: `familyId` (`@Column(name = "family_id")`), `name`, `active`, `birthday` (`LocalDate`), `avatar`, `ttsEnabled` (`@Column(name = "tts_enabled")`), `agentEnabled` (`@Column(name = "agent_enabled")`)
- [ ] Create `family/infrastructure/persistence/AdultProfileJpaEntity.java` — `@Entity @Table(name = "adult_profile")`, extends `BaseEntity`; fields: `familyId` (`@Column(name = "family_id")`), `name`, `birthday` (`LocalDate`), `avatar`

### Spring Data Repositories
- [ ] Create `family/infrastructure/persistence/FamilyJpaRepository.java` — `JpaRepository<FamilyJpaEntity, Long>`; no custom query methods — the adapter will use `findAll().stream().findFirst()` to avoid non-deterministic derived-query ordering
- [ ] Create `family/infrastructure/persistence/ChildProfileJpaRepository.java` — `JpaRepository<ChildProfileJpaEntity, Long>` with `List<ChildProfileJpaEntity> findByFamilyId(Long familyId)`
- [ ] Create `family/infrastructure/persistence/AdultProfileJpaRepository.java` — `JpaRepository<AdultProfileJpaEntity, Long>` with `List<AdultProfileJpaEntity> findByFamilyId(Long familyId)`

### Persistence Adapters
- [ ] Create `family/infrastructure/persistence/FamilyPersistenceAdapter.java` — `@Repository`, implements `FamilyRepository` (port/out); `findFamily()` uses `jpaRepository.findAll().stream().findFirst()` then maps to domain; `exists()` uses `jpaRepository.count() > 0`; `save(Family)` maps domain → JPA entity, saves, maps back; include private static `toDomain(FamilyJpaEntity)` and `toJpa(Family)` mapper methods
- [ ] Create `family/infrastructure/persistence/ChildProfilePersistenceAdapter.java` — `@Repository`, implements `ChildProfileRepository`; `findAll()` uses `jpaRepository.findAll()` (returns all regardless of `active` — service filters); `deleteById(Long)` uses `jpaRepository.deleteById(id)` with an existence check first (throw `ResourceNotFoundException` if not found); include private static `toDomain` and `toJpa` mappers
- [ ] Create `family/infrastructure/persistence/AdultProfilePersistenceAdapter.java` — `@Repository`, implements `AdultProfileRepository`; same pattern as child adapter; `deleteById(Long)` includes existence check

### DTOs
- [ ] Create `family/infrastructure/dto/CreateFamilyRequest.java` — record: `String name`, `String pin`, `boolean ttsEnabled`, `boolean agentEnabled`
- [ ] Create `family/infrastructure/dto/UpdateFamilyRequest.java` — record: `String name`, `String pin` (nullable — null means keep current PIN, passed as-is to `updateFamily`; service handles null = keep existing `pinHash`), `Boolean ttsEnabled`, `Boolean agentEnabled`
- [ ] Create `family/infrastructure/dto/FamilyResponse.java` — record: `Long id`, `String name`, `boolean ttsEnabled`, `boolean agentEnabled`, `LocalDateTime createdAt`, `LocalDateTime updatedAt` — never include `pinHash`
- [ ] Create `family/infrastructure/dto/CreateChildProfileRequest.java` — record: `String name`, `LocalDate birthday`, `String avatar` (nullable), `boolean ttsEnabled`, `boolean agentEnabled` — no `familyId` field; the controller resolves it via `familyUseCase.getFamily().getId()`
- [ ] Create `family/infrastructure/dto/UpdateChildProfileRequest.java` — record: `String name`, `LocalDate birthday`, `String avatar` (nullable), `Boolean ttsEnabled`, `Boolean agentEnabled`
- [ ] Create `family/infrastructure/dto/ChildProfileResponse.java` — record: `Long id`, `Long familyId`, `String name`, `boolean active`, `LocalDate birthday`, `String avatar`, `boolean ttsEnabled`, `boolean agentEnabled`, `LocalDateTime createdAt`, `LocalDateTime updatedAt`
- [ ] Create `family/infrastructure/dto/CreateAdultProfileRequest.java` — record: `String name`, `LocalDate birthday`, `String avatar` (nullable) — no `familyId`; controller resolves via `familyUseCase.getFamily().getId()`
- [ ] Create `family/infrastructure/dto/UpdateAdultProfileRequest.java` — record: `String name`, `LocalDate birthday`, `String avatar` (nullable)
- [ ] Create `family/infrastructure/dto/AdultProfileResponse.java` — record: `Long id`, `Long familyId`, `String name`, `LocalDate birthday`, `String avatar`, `LocalDateTime createdAt`, `LocalDateTime updatedAt`

### REST Controllers
- [ ] Create `family/infrastructure/web/FamilyController.java` — `@RestController @RequestMapping("/api/v1/family")`; inject `FamilyUseCase`; endpoints:
    - `POST /` → `createFamily(@RequestBody CreateFamilyRequest)` → calls `familyUseCase.createFamily(req.name(), req.pin(), req.ttsEnabled(), req.agentEnabled())` → `ResponseEntity.status(201).body(ApiResponse.created(toResponse(family)))`
    - `GET /` → `getFamily()` → `ResponseEntity.ok(ApiResponse.ok(toResponse(family)))`
    - `PATCH /` → `updateFamily(@RequestBody UpdateFamilyRequest)` → calls `familyUseCase.updateFamily(req.name(), req.pin(), req.ttsEnabled(), req.agentEnabled())` → `ResponseEntity.ok(ApiResponse.ok(toResponse(family)))`; add private static `toResponse(Family)` mapper
- [ ] Create `family/infrastructure/web/ChildProfileController.java` — `@RestController @RequestMapping("/api/v1/family/children")`; inject `FamilyUseCase` and `ChildProfileUseCase`; endpoints:
    - `POST /` → resolve `familyId = familyUseCase.getFamily().getId()`; call `createChild(familyId, req.name(), req.birthday(), req.avatar(), req.ttsEnabled(), req.agentEnabled())` → 201
    - `GET /` → `getAllChildren()` → 200 `ApiResponse<List<ChildProfileResponse>>`
    - `GET /{id}` → `getChild(Long id)` → 200
    - `PATCH /{id}` → `updateChild(id, req.name(), req.birthday(), req.avatar(), req.ttsEnabled(), req.agentEnabled())` → 200
    - `DELETE /{id}` → `deactivateChild(id)` → `ResponseEntity.noContent().build()` (204)
- [ ] Create `family/infrastructure/web/AdultProfileController.java` — `@RestController @RequestMapping("/api/v1/family/adults")`; inject `FamilyUseCase` and `AdultProfileUseCase`; endpoints:
    - `POST /` → resolve `familyId = familyUseCase.getFamily().getId()`; call `createAdult(familyId, req.name(), req.birthday(), req.avatar())` → 201
    - `GET /` → `getAllAdults()` → 200
    - `GET /{id}` → `getAdult(Long id)` → 200
    - `PATCH /{id}` → `updateAdult(id, req.name(), req.birthday(), req.avatar())` → 200
    - `DELETE /{id}` → `deleteAdult(id)` → 204

### Integration Tests
- [ ] Create shared test base `AbstractIntegrationTest.java` — `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `@AutoConfigureMockMvc`; use Testcontainers `@Container PostgreSQLContainer` with `@DynamicPropertySource` to inject JDBC URL; annotate with `@Transactional` so each test rolls back automatically
- [ ] Integration test: `FamilyControllerTest` — test: POST `/api/v1/family` → 201 with `FamilyResponse` body (no `pin`/`pinHash` field in JSON); second POST → 409; GET `/api/v1/family` → 200; GET when no family → 404; PATCH name → 200 with updated name; PATCH with null pin → 200 (PIN unchanged)
- [ ] Integration test: `ChildProfileControllerTest` — set up family before each test via direct service call; test: POST → 201; POST with `ttsEnabled=true` when family `ttsEnabled=false` → 201 with `ttsEnabled=false` in response; GET all → list; GET by id → 200; GET unknown id → 404; PATCH → 200; DELETE → 204; GET after DELETE → 200 with `active=false`
- [ ] Integration test: `AdultProfileControllerTest` — set up family before each test; test: POST → 201; GET all; GET by id; GET unknown → 404; PATCH → 200; DELETE → 204; GET after DELETE → 404 (physical delete)

### Contract Update
- [ ] Update `docs/contracts/api/openapi.json` — add all Family, ChildProfile, and AdultProfile paths and schemas; include request body schemas, `FamilyResponse`/`ChildProfileResponse`/`AdultProfileResponse` schemas, and error responses (400, 404, 409) using the existing `ApiResponse` error schema; mark `pin`/`pinHash` as absent from all response schemas

## Risks
- JPA entities extend `BaseEntity` which uses `"default_seq"` — IDs will interleave across the three tables (e.g., family=1, child=2, adult=3). This is acceptable for a single-node private app; do not add per-table sequences unless explicitly requested.
- `FamilyJpaRepository.findAll().stream().findFirst()` is safe but will load all rows; since the DB enforces at most one row via `CHECK (id = 1)`, the cost is negligible.
- `UpdateFamilyRequest.pin` is nullable — the controller passes it directly to `updateFamily`; the service checks `rawPin != null && !rawPin.isBlank()` before re-hashing. Never call `encoder.encode(null)`.
- `CreateChildProfileRequest` and `CreateAdultProfileRequest` do NOT contain `familyId` — controllers call `familyUseCase.getFamily()` to resolve it; this adds one DB read per create request, which is acceptable.
- `SecurityConfig` currently blocks all non-actuator/swagger requests; the security task (first in this sprint) must be completed before any controller can be tested.
- Integration tests with `@Transactional` roll back after each test — verify that Testcontainers state (schema) persists across tests; only data is rolled back, not the schema.
- `FamilyResponse` must never serialize `pinHash` — assert in `FamilyControllerTest` that the response JSON does not contain the key `pin` or `pinHash`.

## Dependencies
- Sprint 004 completed: domain models, ports, services, and validators implemented and tested.
- Sprint 003 completed: `GlobalExceptionHandler` active; controllers can throw `AppException` subclasses freely.
- Sprint 002 completed: `BaseEntity` required by JPA entities.

## Agent Instruction
- JPA entities stay inside `infrastructure/persistence` — controllers never reference them; domain models flow between layers.
- Persistence adapters use private static `toDomain` / `toJpa` methods — no MapStruct, plain Java field-by-field mapping.
- Controllers inject use case interfaces, not service classes — `FamilyUseCase`, `ChildProfileUseCase`, `AdultProfileUseCase`.
- All controller responses use `ResponseEntity<ApiResponse<T>>`: 201 → `ApiResponse.created(data)`, 200 → `ApiResponse.ok(data)`, 204 → `ResponseEntity.noContent().build()`.
- `ChildProfileController` and `AdultProfileController` must inject `FamilyUseCase` only to call `getFamily().getId()` on create — they must not call any other family operation.
- `UpdateFamilyRequest` fields `ttsEnabled` and `agentEnabled` are boxed `Boolean` — if null in request body, treat as the existing value (controller reads them with `req.ttsEnabled() != null ? req.ttsEnabled() : existing.isTtsEnabled()`). Alternatively, require non-null in the DTO.
- Security task is the first task — do not implement controllers before updating `SecurityConfig`.
- Integration tests: use `@Autowired MockMvc` + `ObjectMapper`; do not use `RestTemplate`.
- After all tasks, update `docs/contracts/api/openapi.json` and mark sprint as `completed`.

## Notes
FEAT-001 completion checklist:
- [ ] Security: `/api/v1/family/**` permitted (JWT to be added in future sprint)
- [ ] Family: create, read, update (name, PIN, flags with child propagation)
- [ ] ChildProfile: create (flag ceiling), read all/by-id, update (flag ceiling), deactivate (soft)
- [ ] AdultProfile: full CRUD (physical delete)
- [ ] PIN never in any API response
- [ ] `docs/contracts/api/openapi.json` reflects all 13 endpoints

### Package layout after this sprint
```
family/
  infrastructure/
    persistence/
      FamilyJpaEntity.java
      ChildProfileJpaEntity.java
      AdultProfileJpaEntity.java
      FamilyJpaRepository.java
      ChildProfileJpaRepository.java
      AdultProfileJpaRepository.java
      FamilyPersistenceAdapter.java
      ChildProfilePersistenceAdapter.java
      AdultProfilePersistenceAdapter.java
    web/
      FamilyController.java
      ChildProfileController.java
      AdultProfileController.java
    dto/
      CreateFamilyRequest.java
      UpdateFamilyRequest.java
      FamilyResponse.java
      CreateChildProfileRequest.java
      UpdateChildProfileRequest.java
      ChildProfileResponse.java
      CreateAdultProfileRequest.java
      UpdateAdultProfileRequest.java
      AdultProfileResponse.java
```

### Endpoint summary
| Method | Path                          | Status | Description                                  |
|--------|-------------------------------|--------|----------------------------------------------|
| POST   | /api/v1/family                | 201    | Create family (409 if exists)                |
| GET    | /api/v1/family                | 200    | Get family (404 if not exists)               |
| PATCH  | /api/v1/family                | 200    | Update family + propagate flags to children  |
| POST   | /api/v1/family/children       | 201    | Create child (flag ceiling applied)          |
| GET    | /api/v1/family/children       | 200    | List all children (including inactive)       |
| GET    | /api/v1/family/children/{id}  | 200    | Get child by id                              |
| PATCH  | /api/v1/family/children/{id}  | 200    | Update child (flag ceiling re-applied)       |
| DELETE | /api/v1/family/children/{id}  | 204    | Deactivate child (soft delete, active=false) |
| POST   | /api/v1/family/adults         | 201    | Create adult                                 |
| GET    | /api/v1/family/adults         | 200    | List all adults                              |
| GET    | /api/v1/family/adults/{id}    | 200    | Get adult by id                              |
| PATCH  | /api/v1/family/adults/{id}    | 200    | Update adult                                 |
| DELETE | /api/v1/family/adults/{id}    | 204    | Delete adult (physical)                      |

### FamilyJpaRepository — safe findFirst pattern
```java
// In FamilyPersistenceAdapter:
@Override
public Optional<Family> findFamily() {
    return jpaRepository.findAll().stream()
        .findFirst()
        .map(FamilyPersistenceAdapter::toDomain);
}
```

### Controller familyId resolution pattern (child/adult create)
```java
// In ChildProfileController.createChild:
Long familyId = familyUseCase.getFamily().getId();
ChildProfile child = childProfileUseCase.createChild(
    familyId, req.name(), req.birthday(), req.avatar(), req.ttsEnabled(), req.agentEnabled()
);
```

## Review

completed_tasks:
incomplete_tasks:
contract_changes:
learnings:
next_sprint_suggestions:
