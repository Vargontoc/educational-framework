## Name
Testing skill

## Unit Tests
- Framework: Vitest
- Target: composables, stores, services
- Rule: mock Axios with vi.mock — never make real HTTP calls in tests
- Rule: mock Pinia stores with createTestingPinia when testing components
- Naming: {filename}.test.ts

## Component Tests
- Framework: Vitest + Vue Test Utils
- Target: components in src/components/ and views in src/views/
- Rule: test user interactions (clicks, form inputs) not implementation details
- Rule: stub child components when testing parent logic in isolation

## Coverage Rules
- Every service function must have at least one unit test
- Every store action must have at least one unit test
- Every protected route must have a navigation guard test
- Happy path and at least one error case per service call

## Test Structure (AAA)
// Arrange — set up mocks, props, store state
// Act     — trigger user interaction or call function
// Assert  — verify DOM output or store state change