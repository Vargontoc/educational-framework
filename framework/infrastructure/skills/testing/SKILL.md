# SKILL — infrastructure/testing
# ─────────────────────────────────────────────
# Use this skill for: validating that services start correctly,
# network connectivity works, volumes persist and env vars are complete.

## What to test
Infrastructure testing has four levels — cover all before closing a sprint:

1. Syntax validation
   Verify docker-compose files have no syntax errors.
   docker compose config --quiet
   docker compose -f docker-compose.yml \
     -f docker-compose.prod.yml config --quiet

2. Service startup
   Verify all services start and reach healthy/running state.
   docker compose up -d
   docker compose ps
   Expected: all services show status running or healthy
   docker compose logs --tail=20 {service} for any that fail

3. Network connectivity
   Verify services can reach each other by container name inside app-network.

4. Volume persistence
   Verify named volumes survive a docker compose down and up cycle.
   docker compose down
   docker compose up -d

## env completeness check
Before running any test, verify all .env files exist:
  ls infrastructure/envs/*.env
If any .env is missing, copy from .env.example and fill placeholders.
Never run tests with placeholder values — tests will fail with
misleading errors that hide the real problem.

## Verifying no secrets are committed
git diff --name-only HEAD~1 HEAD | grep -E "\.env$" | grep -v "\.example"
Expected: empty output — any match is a security violation.

## After testing
- docker compose down after every test run — do not leave services running
- Document any unexpected behaviour in infrastructure/sprints/current.md Notes
- If a test reveals a missing env var, add it to the relevant .env.example
- Mark testing tasks complete in infrastructure/sprints/current.md