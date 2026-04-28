# Name
Coding Skill

## Pattern
Always follow hexagonal architecture strictly
1. Define the use case interface in {domain}/ports/in/
2. Implement businness logic in {domain}/service/
3. Define repository interface {domain}/ports/out/
4. Implement persistence adapter in {domain}/infrastructure/persistence/
5. Expose via REST controller in {domain}/infrastructure/web/
6. Use DTOs for all request and response objects - never expose domain models

## Conventions

- Controllers are thin - no business logic, only delegation to application service
- Services contain business logic - no JPA or HTTP dependencies
- Domain model objects must not import Spring or JPA annotations
- Use constructor injection - never field injection (@Autowired)
- All endpoints must be documented with @Operation (SpringDoc OpenAPI)

## Migrations

- Create a new xml migration  file in db/changelog/migrations for every schema change
- Never modify existing migration files
- Register the new migration in db.changelog-master.xml
- Naming: {next_version}__{what_it_does}.xml

## After Coding

- Regenerate openapi.json if any endpoint was added or modified
- Regenerate websocket.json if any websocket endpoint was added or modified
- Place updated openapi.json in docs/contracts/
- Place updateed websocket.json in docs/contracts/
- Write al least one unit test for every new use case
- Check backend/sprints/current.md and mark completed tasks