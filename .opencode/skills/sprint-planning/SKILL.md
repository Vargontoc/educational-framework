# SKILL — analysis/sprint-planning
# ─────────────────────────────────────────────
# Use this skill to: generate a sprint proposal for one or more layers
# based on current project state, pending tasks and detected blockers.

## Before Planning
1. Read {layer}/sprints/current.md for every layer
2. Read docs/contracts/openapi.json to understand contract state
3. Run dependency-check skill to detect active blockers
4. Read {layer}/sprints/history/ to understand what was done before

## Planning Output
Produce a sprint proposal following docs/sprints/sprint_template.md exactly.
The proposal must include:
  - A clear one-sentence Goal
  - A concrete Task list derived from pending work
  - Identified Risks based on inter-layer dependencies
  - Explicit Dependencies referencing other layers or contracts
  - Agent Instructions derived from the layer AGENT.md rules

## Ordering Rule

When multiple layers need a sprint, propose them in this order:
  1. infrastructure — environment must be ready first
  2. agents         -> Agent AI domain
  3. backend        — contract must exist before frontend can start
  4. frontend       — depends on backend contract
  5. If backend sprint is not closed, mark frontend sprint as blocked

## Output Format

Present the proposal as a ready-to-copy sprint file.
State clearly: "Copy this to {layer}/sprints/current.md to activate the sprint."
Do not write to the file directly — wait for human confirmation.