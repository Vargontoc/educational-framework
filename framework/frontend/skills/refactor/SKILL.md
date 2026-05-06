## Name
Refactor Sill

## Before Refactoring
1. Confirm existing tests pass before making any change
2. Identify scope — do not refactor beyond the sprint task
3. Check that no change affects the contract in docs/contracts/openapi.json

## Layer Violations to Fix
- Direct Axios calls inside components → move to services/
- Business logic inside views → extract to composables or stores
- JWT or token handling in localStorage → remove and align with httpOnly cookie
- Hardcoded API URLs → move to shared Axios instance base URL

## Rules
- One refactor commit per logical change
- All tests must pass after every commit
- Update Notes in frontend/sprints/current.md with what changed and why

## After Refactoring
- Run full test suite with: npm run test
- Verify the app still builds with: npm run build