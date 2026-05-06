## Name
Design Skill

## When to Use This Skill
- Before creating a new view — design the data flow first
- When a feature needs new API calls — verify openapi.json first
- When state management gets complex — design store shape before coding

## Design Output Format
Always produce a written proposal in frontend/sprints/current.md Notes with:
  - What view or feature is being designed
  - Data flow: which store, which service, which endpoint
  - Component tree: which components are needed
  - Contract dependency: which openapi.json endpoints are consumed
  - Risks and open questions

## Decision Rules
- Prefer a composable over a store for local or single-component state
- A store is justified when state is shared across multiple views
- Never design a solution that bypasses the shared Axios instance
- If a design requires a new endpoint, check backend/sprints/current.md and add the dependency to frontend/sprints/current.md before proceeding