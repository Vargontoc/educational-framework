# Name

Design Skill

## When to use this skill

- Before creating a new domain - design ports/in and ports/out first
- When a feature touche multiple domains - design the interaction first
- When a contract change is needed - evaluate impact on frontend before acting

## Design output format

Always produce a written proposal in backend/sprints/current.md Notes with:
- What domain or feature is beign designed
- Proposed ports/in (use cases)
- Proposed ports/ut (repositories or external services)
- Contract impact (does openapi.json need to change?)
- Risks and open questions

## Decision rules

- Prefer extending an existing domain over creating a new one for small features
- A new domain is justified when it has its own lifecycle and bounded context
- Never design a solution that creates a direct dependency between two domain - use application services to orchestrate cross-domain logic
- If a design decision affects docs/contracts, document it before implementing