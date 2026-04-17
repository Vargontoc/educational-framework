# SKILL — infrastructure/refactor
# ─────────────────────────────────────────────
# Use this skill for: improving existing docker-compose configuration,
# cleaning up env files, reorganising service definitions.

## Before refactoring
1. Run syntax validation — both files must pass before any change
2. Identify the specific problem — do not refactor without a clear reason
3. Check that the refactor does not affect service contracts with other layers
   (port changes affect backend.env, image changes affect reproducibility)

## Common refactors

Hardcoded values → env vars
  Any value that differs between local and production must be an env var.
  Move it to the appropriate .env.example and reference it in docker-compose.yml.

Missing healthchecks
  Any service that other services depend_on must have a healthcheck.
  Use condition: service_healthy in depends_on — never bare service name.

Anonymous volumes → named volumes
  Replace any anonymous volume (- /var/lib/data) with a named volume.
  Named volumes: declare under top-level volumes:, reference as name:/path.

Duplicate config between local and prod
  If docker-compose.prod.yml repeats entire service definitions,
  extract only the differing fields. The prod file must be a minimal override.

Missing .env.example entries
  If a service uses an env var not documented in its .env.example, add it.
  Every variable a service reads must be in its .env.example with a comment.

## Rules
- One refactor commit per logical change — do not batch unrelated changes
- Both docker-compose files must pass validation after every commit
- Port changes require notifying the affected layer via their sprints/current.md
- Image version changes (e.g. postgres:16 → postgres:17) are breaking —
  treat as a new coding task, not a refactor

## After refactoring
- Run full validation: docker compose config --quiet
- Run service startup test from testing/SKILL.md level 2
- Update Notes in framework/infrastructure/sprints/current.md with what changed and why