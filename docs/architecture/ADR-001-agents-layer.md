# ADR-001 — Agents layer for domain-specific AI features
# ─────────────────────────────────────────────

## Status
status:        accepted
date:          2026-04-17
superseded_by: —

## Context
The project needs to host domain-specific AI agents that are application features consumed by the backend.
These agents have their own lifecycle — system prompts, tools and workflows
change independently of business logic code. Without a dedicated layer,
Modelfiles end up inside backend/ mixing infrastructure concerns with
AI behaviour definitions.

## Decision
Create a dedicated agents/ layer following the same pattern as other layers:
AGENT.md, skills/, sprints/, and one subdirectory per domain agent.
Each agent has a Modelfile, context files (rules, examples, workflows)
and an MCP tools definition. Every agent publishes a capability contract
to docs/contracts/agents/ that backend consumes.
Breaking changes to an agent contract trigger a backend sprint dependency.

## Consequences
positive:
  - Agent behaviour is versioned and auditable separately from code
  - Breaking changes are explicit and controlled via contract versioning
  - New domain agents can be added without touching other layers
  - The agents/ layer follows the same workflow pattern as all other layers

negative:
  - Adds a new layer to coordinate in the sprint workflow
  - Backend must be updated when agent contracts change

neutral:
  - Ollama must be running locally for agents to be testable

## Alternatives considered
alternative:      Modelfiles inside backend/
reason_rejected: Mixes AI behaviour definitions with business logic.
                  Agent changes trigger backend CI unnecessarily.

alternative:      External agent registry (separate repo)
reason_rejected: Adds coordination overhead not justified for a
                  single-developer monorepo

## References