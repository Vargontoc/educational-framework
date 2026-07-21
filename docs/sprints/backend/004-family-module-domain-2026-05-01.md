# Sprint 004 - backend
# -----------------------------------------------

## Goal
Implement the Family domain layer for FEAT-001: Liquibase migrations for the three tables, pure domain models, hexagonal ports (use cases + repository interfaces), service implementations with PIN hashing and flag propagation, validators, and full unit test coverage - no HTTP layer in this sprint.

## Status
status: completed
started_at: 2026-05-01 00:00:00
closed_at: 2026-05-01 11:45:00
blocked_by:
waiting_for:

## Tasks

### Liquibase Migrations
- [x] Create `migrations/002__create_family.xml` - table `family`: columns `id` (bigserial PK), `name` (varchar 100 NOT NULL), `pin_hash` (varchar 255 NOT NULL), `tts_enabled` (boolean NOT NULL DEFAULT true), `agent_enabled` (boolean NOT NULL DEFAULT true), `created_at` (timestamptz NOT NULL), `updated_at` (timestamptz); add `UNIQUE` constraint on `(id)` is implicit - add a `CHECK (id = 1)` constraint to enforce single-row invariant at DB level
- [x] Create `migrations/003__create_child_profile.xml` - table `child_profile`: columns `id` (bigserial PK), `family_id` (bigint NOT NULL), `name` (varchar 100 NOT NULL), `active` (boolean NOT NULL DEFAULT true), `birthday` (date NOT NULL), `avatar` (varchar 100 NOT NULL DEFAULT 'default-child'), `tts_enabled` (boolean NOT NULL DEFAULT true), `agent_enabled` (boolean NOT NULL DEFAULT true), `created_at` (timestamptz NOT NULL), `updated_at` (timestamptz); FK `family_id -> family(id) ON DELETE RESTRICT`; index on `family_id`; CHECK `birthday <= CURRENT_DATE AND birthday >= CURRENT_DATE - INTERVAL '18 years'`
- [x] Create `migrations/004__create_adult_profile.xml` - table `adult_profile`: columns `id` (bigserial PK), `family_id` (bigint NOT NULL), `name` (varchar 100 NOT NULL), `birthday` (date NOT NULL), `avatar` (varchar 100 NOT NULL DEFAULT 'default-adult'), `created_at` (timestamptz NOT NULL), `updated_at` (timestamptz); FK `family_id -> family(id) ON DELETE RESTRICT`; index on `family_id`; CHECK `birthday <= CURRENT_DATE`
- [x] Register the three new changeSets in `db.changelog-master.xml`

### Domain Models
- [x] Create `family/model/Family.java` - plain Java class (no JPA), fields: `Long id`, `String name`, `String pinHash`, `boolean ttsEnabled`, `boolean agentEnabled`, `LocalDateTime createdAt`, `LocalDateTime updatedAt`
- [x] Create `family/model/ChildProfile.java` - plain Java class, fields: `Long id`, `Long familyId`, `String name`, `boolean active`, `LocalDate birthday`, `String avatar`, `boolean ttsEnabled`, `boolean agentEnabled`, `LocalDateTime createdAt`, `LocalDateTime updatedAt`
- [x] Create `family/model/AdultProfile.java` - plain Java class, fields: `Long id`, `Long familyId`, `String name`, `LocalDate birthday`, `String avatar`, `LocalDateTime createdAt`, `LocalDateTime updatedAt`

### Ports - In (Use Cases)
- [x] Create `family/ports/in/FamilyUseCase.java` - interface: `Family createFamily(String name, String rawPin, boolean ttsEnabled, boolean agentEnabled)`, `Family getFamily()`, `Family updateFamily(String name, String rawPin, boolean ttsEnabled, boolean agentEnabled)`, `boolean familyExists()`
- [x] Create `family/ports/in/ChildProfileUseCase.java` - interface: `ChildProfile createChild(Long familyId, String name, LocalDate birthday, String avatar, boolean ttsEnabled, boolean agentEnabled)`, `ChildProfile getChild(Long id)`, `List<ChildProfile> getAllChildren()`, `ChildProfile updateChild(Long id, String name, LocalDate birthday, String avatar, boolean ttsEnabled, boolean agentEnabled)`, `void deactivateChild(Long id)`
- [x] Create `family/ports/in/AdultProfileUseCase.java` - interface: `AdultProfile createAdult(Long familyId, String name, LocalDate birthday, String avatar)`, `AdultProfile getAdult(Long id)`, `List<AdultProfile> getAllAdults()`, `AdultProfile updateAdult(Long id, String name, LocalDate birthday, String avatar)`, `void deleteAdult(Long id)`

### Ports - Out (Repository Interfaces)
- [x] Create `family/ports/out/FamilyRepository.java` - interface: `Optional<Family> findFamily()`, `boolean exists()`, `Family save(Family family)`
- [x] Create `family/ports/out/ChildProfileRepository.java` - interface: `Optional<ChildProfile> findById(Long id)`, `List<ChildProfile> findAll()`, `ChildProfile save(ChildProfile child)`, `void deleteById(Long id)`
- [x] Create `family/ports/out/AdultProfileRepository.java` - interface: `Optional<AdultProfile> findById(Long id)`, `List<AdultProfile> findAll()`, `AdultProfile save(AdultProfile adult)`, `void deleteById(Long id)`

### Validators
- [x] Create `family/validation/FamilyValidator.java` - validates `name` and 4-digit PIN regex (`^\d{4}$`), allowing empty PIN on update
- [x] Create `family/validation/ChildProfileValidator.java` - validates child name, birthday range, and avatar max length
- [x] Create `family/validation/AdultProfileValidator.java` - validates adult name, non-future birthday, and avatar max length

### Services
- [x] Create `family/service/FamilyService.java` - `@Service`, `@Transactional`, create/get/update/exists, PIN hashing with `BCryptPasswordEncoder`, child flag propagation in transaction
- [x] Create `family/service/ChildProfileService.java` - `@Service`, `@Transactional`, child CRUD-style methods with flag ceiling and deactivation
- [x] Create `family/service/AdultProfileService.java` - `@Service`, `@Transactional`, adult CRUD-style methods with 404 behavior

### Unit Tests
- [x] Unit test: `FamilyServiceTest`
- [x] Unit test: `ChildProfileServiceTest`
- [x] Unit test: `AdultProfileServiceTest`
- [x] Unit test: `FamilyValidatorTest`
- [x] Unit test: `ChildProfileValidatorTest`
- [x] Unit test: `AdultProfileValidatorTest`

## Risks
- `CHECK (id = 1)` on `family` enforces single-row invariant at DB level and service-level conflict check remains in place.
- Child flag ceiling keeps children disabled when family is disabled; re-enabling family does not force children back to true.
- PIN is validated and stored only as BCrypt hash.

## Dependencies
- Sprint 003 completed.
- Sprint 002 completed.
- No OpenAPI contract changes in this sprint.

## Agent Instruction
Implemented according to sprint instructions for package, domain purity, transactional services, validator behavior, and unit-test approach.

## Notes
Domain layer only. No HTTP controllers or persistence adapters were added in this sprint.

## Review

completed_tasks:
  - Implemented all Liquibase migration files and registered them in master changelog.
  - Implemented family domain models, input/output ports, validators, and services.
  - Added full unit test suite for family services and validators.
incomplete_tasks:
  - None.
contract_changes:
  - None.
learnings:
  - Keeping family flag propagation in `FamilyService` avoids service-level circular dependencies.
  - Validator input records keep service methods clean while using shared validation guards.
  - `FamilyValidator` has two entry points: `validateForCreate` (PIN required) and `validateForUpdate` (PIN optional — null/blank = keep existing hash).
  - `ChildProfileService.createChild` receives `familyId` as a parameter and also loads the family to apply the flag ceiling — the caller (controller) is responsible for resolving the family ID.
  - `applyFamilyCeiling(childValue, familyEnabled)` returns `familyEnabled && childValue` — the simplest expression of the ceiling rule.
next_sprint_suggestions:
  - Sprint 005 should implement persistence adapters (JPA entities/repositories/mappers) and web controllers/DTOs for Family module.
  - Controllers for child/adult creation must NOT include `familyId` in the request DTO — it is a single-family app; the controller fetches the family via `familyUseCase.getFamily()` and passes its ID to the use case.
  - `SecurityConfig` must permit `/api/v1/family/**` before integration tests can pass — JWT auth is planned for a future sprint.
