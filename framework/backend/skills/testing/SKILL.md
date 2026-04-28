# Name 

Testing Skill

## Unit Tests

- Framework: JUnit 5 + Mockito
- Target: domain/service classes only
- Rule: mock all ports/out dependencies with @Mock
- Rule: never load Spring context in unit tests
- Naming: {ClassName}Test.java

## Integration Tests

- Framework:  @SpringBoottest + Testcontainers (PostgreSQL)
- Target: infrastructure/web controllers
- Rule: use a real database container - never H2 in-memory
- Rule: each test must clean its own data (use @Transactional or explicit cleanup)
- Naming: {ClassName}IT.java

## Coverage Rules

- Every port/interface must have at least one unit test
- Every REST endpoint must have at least one integration test
- Happy path and al least one error case per use case

## Test Structure

// Arrange  - set up data and mocks
// Act      - call the method under test
// Assert   - verify the outcome