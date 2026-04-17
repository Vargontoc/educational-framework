# SKILL — agents/refactor
# ─────────────────────────────────────────────
# Use this skill for: improving an existing agent without changing its
# observable behaviour — cleaner prompts, better examples, parameter tuning.

## Before refactoring
1. Run all existing tests from context/examples.md — all must pass first
2. Identify the specific problem to fix — do not refactor without a clear reason
3. Check if the change is breaking or non-breaking (see agents/AGENT.md)
4. If breaking: follow coding/SKILL.md contract update process instead

## Non-breaking refactors (safe to do)
System prompt clarity:
  - Remove redundant or contradictory rules
  - Rewrite vague instructions as specific, testable rules
  - Shorten without losing meaning — every sentence must earn its place

Example quality:
  - Replace weak examples with ones that better illustrate edge cases
  - Add examples for cases where the agent currently underperforms
  - Ensure every example follows the exact format: user / assistant / expected

Parameter tuning:
  - Adjust temperature if responses are too random or too repetitive
  - Adjust repeat_penalty if the agent loops or echoes the user
  - Document the change and reason in agents/sprints/current.md Notes

Workflow clarity:
  - Rewrite ambiguous steps as explicit numbered actions
  - Add missing output format definition to any workflow that lacks one

## Breaking refactors (require contract update)
These are NOT refactors — treat them as new coding tasks:
  - Changing the agent's scope or persona
  - Adding or removing MCP tools
  - Changing the output format shape

## After refactoring
- Rebuild the agent: ollama create {name} -f Modelfile
- Run all existing tests — all must still pass
- Update framework/agents/sprints/current.md Notes with what changed and why
- If parameters changed, document old vs new values and observed effect