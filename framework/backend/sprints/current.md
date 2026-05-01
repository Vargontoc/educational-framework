# Sprint 004 - backend
# -----------------------------------------------

## Goal
Implement the Family domain layer for FEAT-001: Liquibase migrations for the three tables, pure domain models, hexagonal ports (use cases + repository interfaces), service implementations with PIN hashing and flag propagation, validators, and full unit test coverage — no HTTP layer in this sprint.

## Status
status: active
started_at: 2026-05-01 00:00:00
closed_at:
blocked_by:
waiting_for:

## Tasks

### Liquibase Migrations
- [ ] Create `migrations/002__create_family.xml` — table `family`: columns `id` (bigserial PK), `name` (varchar 100 NOT NULL), `pin_hash` (varchar 255 NOT NULL), `tts_enabled` (boolean NOT NULL DEFAULT true), `agent_enabled` (boolean NOT NULL DEFAULT true), `created_at` (timestamptz NOT NULL), `updated_at` (timestamptz); add `UNIQUE` constraint on `(id)` is implicit — add a `CHECK (id = 1)` constraint to enforce single-row invariant at DB level
- [ ] Create `migrations/003__create_child_profile.xml` — table `child_profile`: columns `id` (bigserial PK), `family_id` (bigint NOT NULL), `name` (varchar 100 NOT NULL), `active` (boolean NOT NULL DEFAULT true), `birthday` (date NOT NULL), `avatar` (varchar 100 NOT NULL DEFAULT 'default-child'), `tts_enabled` (boolean NOT NULL DEFAULT true), `agent_enabled` (boolean NOT NULL DEFAULT true), `created_at` (timestamptz NOT NULL), `updated_at` (timestamptz); FK `family_id → family(id) ON DELETE RESTRICT`; index on `family_id`; CHECK `birthday <= CURRENT_DATE AND birthday >= CURRENT_DATE - INTERVAL '18 years'`
- [ ] Create `migrations/004__create_adult_profile.xml` — table `adult_profile`: columns `id` (bigserial PK), `family_id` (bigint NOT NULL), `name` (varchar 100 NOT NULL), `birthday` (date NOT NULL), `avatar` (varchar 100 NOT NULL DEFAULT 'default-adult'), `created_at` (timestamptz NOT NULL), `updated_at` (timestamptz); FK `family_id → family(id) ON DELETE RESTRICT`; index on `family_id`; CHECK `birthday <= CURRENT_DATE`
- [ ] Register the three new changeSets in `db.changelog-master.xml`

### Domain Models
- [ ] Create `family/model/Family.java` — plain Java class (no JPA), fields: `Long id`, `String name`, `String pinHash`, `boolean ttsEnabled`, `boolean agentEnabled`, `LocalDateTime createdAt`, `LocalDateTime updatedAt`
- [ ] Create `family/model/ChildProfile.java` — plain Java class, fields: `Long id`, `Long familyId`, `String name`, `boolean active`, `LocalDate birthday`, `String avatar`, `boolean ttsEnabled`, `boolean agentEnabled`, `LocalDateTime createdAt`, `LocalDateTime updatedAt`
- [ ] Create `family/model/AdultProfile.java` — plain Java class, fields: `Long id`, `Long familyId`, `String name`, `LocalDate birthday`, `String avatar`, `LocalDateTime createdAt`, `LocalDateTime updatedAt`

### Ports — In (Use Cases)
- [ ] Create `family/ports/in/FamilyUseCase.java` — interface: `Family createFamily(String name, String rawPin, boolean ttsEnabled, boolean agentEnabled)`, `Family getFamily()`, `Family updateFamily(String name, String rawPin, boolean ttsEnabled, boolean agentEnabled)`, `boolean familyExists()`
- [ ] Create `family/ports/in/ChildProfileUseCase.java` — interface: `ChildProfile createChild(Long familyId, String name, LocalDate birthday, String avatar, boolean ttsEnabled, boolean agentEnabled)`, `ChildProfile getChild(Long id)`, `List<ChildProfile> getAllChildren()`, `ChildProfile updateChild(Long id, String name, LocalDate birthday, String avatar, boolean ttsEnabled, boolean agentEnabled)`, `void deactivateChild(Long id)`
- [ ] Create `family/ports/in/AdultProfileUseCase.java` — interface: `AdultProfile createAdult(Long familyId, String name, LocalDate birthday, String avatar)`, `AdultProfile getAdult(Long id)`, `List<AdultProfile> getAllAdults()`, `AdultProfile updateAdult(Long id, String name, LocalDate birthday, String avatar)`, `void deleteAdult(Long id)`

### Ports — Out (Repository Interfaces)
- [ ] Create `family/ports/out/FamilyRepository.java` — interface: `Optional<Family> findFamily()`, `boolean exists()`, `Family save(Family family)`
- [ ] Create `family/ports/out/ChildProfileRepository.java` — interface: `Optional<ChildProfile> findById(Long id)`, `List<ChildProfile> findAll()`, `ChildProfile save(ChildProfile child)`, `void deleteById(Long id)`
- [ ] Create `family/ports/out/AdultProfileRepository.java` — interface: `Optional<AdultProfile> findById(Long id)`, `List<AdultProfile> findAll()`, `AdultProfile save(AdultProfile adult)`, `void deleteById(Long id)`

### Validators
- [ ] Create `family/validation/FamilyValidator.java` — extends `AbstractValidator<FamilyUseCase parameters>` or implements `IValidator`; validates: `name` non-blank, max 100 chars; `rawPin` exactly 4 digits (`\d{4}`)
- [ ] Create `family/validation/ChildProfileValidator.java` — validates: `name` non-blank max 100; `birthday` non-null, not in future, not before `LocalDate.now().minusYears(18)`; `avatar` max 100 chars (nullable, defaults applied in service)
- [ ] Create `family/validation/AdultProfileValidator.java` — validates: `name` non-blank max 100; `birthday` non-null, not in future

### Services
- [ ] Create `family/service/FamilyService.java` — `@Service`, implements `FamilyUseCase`; `createFamily`: check `familyRepository.exists()` → throw `ConflictException` if true; validate via `FamilyValidator`; hash rawPin with `BCryptPasswordEncoder`; save and return; `updateFamily`: load existing (404 if not found), validate, hash new PIN if provided, propagate flag changes to all `ChildProfile` records in the same `@Transactional` call; `getFamily`: `findFamily()` or throw `ResourceNotFoundException`
- [ ] Create `family/service/ChildProfileService.java` — `@Service`, implements `ChildProfileUseCase`; `createChild`: load family (404 if missing), validate, apply flag ceiling (if `family.ttsEnabled == false` then force `ttsEnabled = false`; same for `agentEnabled`), save; `updateChild`: load child (404 if missing), re-apply flag ceiling against current family state, save; `deactivateChild`: load child (404), set `active = false`, save
- [ ] Create `family/service/AdultProfileService.java` — `@Service`, implements `AdultProfileUseCase`; standard CRUD with 404 on missing records; no flag logic

### Unit Tests
- [ ] Unit test: `FamilyServiceTest` — mock `FamilyRepository` and `ChildProfileRepository`; test: createFamily happy path, createFamily throws `ConflictException` when family exists, updateFamily propagates `ttsEnabled=false` to all children, updateFamily does NOT re-enable children when family re-enables flag, getFamily throws `ResourceNotFoundException` when no family
- [ ] Unit test: `ChildProfileServiceTest` — mock `FamilyRepository` and `ChildProfileRepository`; test: createChild applies flag ceiling (family disabled → child forced false), createChild passes enabled flag when family allows, deactivateChild sets active=false, updateChild re-applies ceiling after family change
- [ ] Unit test: `AdultProfileServiceTest` — mock `AdultProfileRepository`; test: createAdult happy path, getAdult 404, deleteAdult 404
- [ ] Unit test: `FamilyValidatorTest` — test: valid name+PIN pass, blank name throws `ValidationException`, PIN not 4 digits throws `ValidationException`, PIN with letters throws `ValidationException`
- [ ] Unit test: `ChildProfileValidatorTest` — test: valid inputs pass, birthday in future throws, birthday older than 18 years throws, blank name throws
- [ ] Unit test: `AdultProfileValidatorTest` — test: valid inputs pass, birthday in future throws, blank name throws

## Risks
- `CHECK (id = 1)` on `family` is the simplest DB-level single-row guard; the service also rejects duplicates via `ConflictException` — both layers enforce the invariant independently.
- `birthday` CHECK constraint in Liquibase uses `CURRENT_DATE` which is evaluated at insert/update time — this is correct for PostgreSQL.
- PIN hashing: `BCryptPasswordEncoder` is already on the classpath via Spring Security. Never log or store the `rawPin`.
- Flag ceiling logic: when `Family.ttsEnabled` is set to `true`, existing children keep their own value — enabling the family does not automatically re-enable children. Only disabling the family forces children to false.
- `@Transactional` on `FamilyService.updateFamily` is mandatory — the child flag propagation and the family update must succeed or fail together.
- Domain models are plain Java classes — they must not import any JPA or Spring annotation. Mappers in the persistence adapter handle the JPA ↔ domain conversion.

## Dependencies
- Sprint 003 completed: `GlobalExceptionHandler` must exist so services can throw `AppException` subclasses freely.
- Sprint 002 completed: `BaseEntity`, `AppException` hierarchy, `AbstractValidator`, `ApiResponse` all required.
- No changes to `docs/contracts/api/openapi.json` in this sprint — no REST endpoints are added.

## Agent Instruction
- All classes go under `es.vargontoc.educational.framework.family`.
- Domain models (`Family`, `ChildProfile`, `AdultProfile`) must be plain Java — no `@Entity`, no `@Component`, no Spring imports.
- Ports (`ports/in/`, `ports/out/`) are Java interfaces only — no implementation code.
- Services are annotated `@Service` and `@Transactional` (class-level); use constructor injection, never `@Autowired` field injection.
- `FamilyService.updateFamily` must update child flags inside the same transaction using `ChildProfileRepository.findAll()` and saving each updated child — do not call `ChildProfileService` from `FamilyService` (avoid circular dependencies between services).
- Validators extend `AbstractValidator` or implement `IValidator` using guard methods from `AbstractValidator` — never throw raw `IllegalArgumentException`.
- PIN validation regex: `^\d{4}$` — exactly 4 digits, no letters, no spaces.
- Use `new BCryptPasswordEncoder()` directly in `FamilyService` — do not declare it as a Spring Bean in this sprint (avoid touching `SecurityConfig`).
- Unit tests use Mockito (`@ExtendWith(MockitoExtension.class)`) — no Spring context.
- Liquibase changeSet IDs must follow the pattern `{file-name-without-xml}` (e.g., `002__create_family`).
- Never modify existing migration files (`001__init_schema.xml`).
- After completing all tasks, mark status as `completed` and fill the Review section.

## Notes
FEAT-001 domain decisions applied here:
- Single-row family enforced at two levels: DB CHECK + service ConflictException.
- Flag propagation is business logic only — no DB triggers.
- PIN: 4 digits, BCrypt hash, never stored in plain text.
- ChildProfile deactivation is soft-delete (`active = false`), not physical delete.
- AdultProfile is physically deleted.
- Birthday range for children: [today − 18y, today]. Adults: any past date.

### Package layout after this sprint
```
family/
  model/
    Family.java
    ChildProfile.java
    AdultProfile.java
  ports/
    in/
      FamilyUseCase.java
      ChildProfileUseCase.java
      AdultProfileUseCase.java
    out/
      FamilyRepository.java
      ChildProfileRepository.java
      AdultProfileRepository.java
  service/
    FamilyService.java
    ChildProfileService.java
    AdultProfileService.java
  validation/
    FamilyValidator.java
    ChildProfileValidator.java
    AdultProfileValidator.java
```

### Flag ceiling logic (FamilyService.updateFamily)
```java
if (!request.ttsEnabled()) {
    childProfileRepository.findAll().stream()
        .filter(ChildProfile::isTtsEnabled)
        .map(c -> { c.setTtsEnabled(false); return c; })
        .forEach(childProfileRepository::save);
}
// same block for agentEnabled
```

### PIN hashing
```java
private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
// in createFamily / updateFamily:
String pinHash = encoder.encode(rawPin);
```

## Review

completed_tasks:
incomplete_tasks:
contract_changes:
learnings:
next_sprint_suggestions:
