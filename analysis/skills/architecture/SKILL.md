# SKILL — analysis/architecture
# ─────────────────────────────────────────────
# Use this skill to: evaluate architectural decisions, propose new
# layers or domains, assess impact of structural changes.

## When to Use This Skill
- Before adding a new domain to backend or frontend
- Before introducing a new contract type in docs/contracts/
- When a feature touches more than one layer simultaneously
- When technical debt accumulates across multiple layers

## Before Evaluating
1. Read agnet.md of all affected layers
2. Read docs/contracts/ to understand current inter-layer agreements
3. Read docs/architecture/ for prior decisions (if present)

## Evaluation Output Format
Produce a written proposal for docs/architecture/ with:
  - Context:      what problem or opportunity is being addressed
  - Options:      at least two alternatives with trade-offs
  - Recommendation: which option and why
  - Impact:       which layers are affected and how
  - Contract changes: does docs/contracts/ need to change?
  - Risks:        what could go wrong
  - Decision:     left blank — to be filled by the human

## Rules
- Never recommend a solution that creates a direct dependency
  between two sibling layers (e.g. frontend importing backend types)
- Always evaluate contract impact before recommending structural changes
- Document every architectural decision in docs/architecture/
  regardless of whether it was accepted or rejected
- Rejected decisions are as valuable as accepted ones —
  they prevent revisiting the same discussion in the future