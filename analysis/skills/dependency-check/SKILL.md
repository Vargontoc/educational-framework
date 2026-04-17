# SKILL — analysis/dependency-check
# ─────────────────────────────────────────────
# Use this skill to: detect blockers and dependency mismatches
# across all layers before planning or reviewing a sprint.

## What to Check
1. Read {layer}/sprints/current.md for every layer
2. Check status field — report any layer with status: blocked
3. Cross-reference blocked_by and waiting_for fields
4. Verify docs/contracts/openapi.json exists if frontend sprint is active
5. Check that backend sprint is active or completed before frontend starts

## Dependency Matrix
infrastructure → backend:
  backend cannot start if db service is not healthy
infrastructure → frontend:
  frontend cannot build if its Docker service is not configured
backend → frontend:
  frontend is blocked until docs/contracts/openapi.json is published
frontend → backend:
  if frontend needs a new endpoint, backend sprint must include it first

## Report Format
For each detected blocker, report:

  BLOCKER DETECTED
  ────────────────
  blocked_layer:   {layer}
  blocking_layer:  {layer}
  reason:          {what is missing}
  waiting_for:     {exact file or field that needs to change}
  suggested_action: {what the human should do to resolve it}

If no blockers are detected, report: "No blockers detected. All layers are active."

## Rule
This skill only reads and reports.
Never update status fields in any layer sprint file directly.