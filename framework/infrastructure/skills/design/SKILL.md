# SKILL — infrastructure/design
# ─────────────────────────────────────────────
# Use this skill for: proposing new services, evaluating infrastructure
# changes, assessing impact on other layers before any file is modified.

## When to use this skill
- Before adding a new service to docker-compose.yml
- Before changing a port, image version or volume strategy
- When a backend or frontend sprint requests a new infrastructure dependency
- When evaluating whether a new tool (cache, queue, proxy) is justified

## The first question to answer
Does this change affect another layer's contract or configuration?

  Port change        → affects {service}.env.example consumed by other layers
  New service        → requires new .env.example and possible sprint dependency
  Image upgrade      → may require migration or config changes in backend
  Volume strategy    → affects data persistence guarantees for backend and db

If the answer is yes, the design proposal must include an explicit
impact assessment for every affected layer.

## Design output format
Produce a written proposal in infrastructure/sprints/current.md Notes with:

  change:          what is being added or modified
  reason:          why this change is needed
  services_affected: which services change and how
  layers_affected:  which other layers need to update their config
  env_changes:      new or modified variables in .env.example files
  port_changes:     any port additions, removals or modifications
  volume_changes:   any new, removed or modified named volumes
  risks:            what could break and how to detect it
  rollback:         how to revert if the change causes problems

## Decision rules
- Prefer official images from Docker Hub over custom builds
- Prefer Alpine-based images for smaller footprint
- A new service is justified when no existing service can cover the need
- Never add a service just because it might be useful later —
  add it when a sprint task explicitly requires it
- If a design requires changes in backend/ or frontend/,
  document them as dependencies in infrastructure/sprints/current.md
  and do not proceed until those layers acknowledge the dependency

## Decision
# Left blank — to be filled by the human after reviewing the proposal.
# Agent does not proceed to coding/SKILL.md until human approves.