# SKILL — agents/design
# ─────────────────────────────────────────────
# Use this skill for: proposing a new agent, evaluating whether an agent
# is the right solution, designing scope, tools and interaction model
# before any Modelfile is written.

## When to use this skill
- Before creating any new agent — design always precedes coding
- When an existing agent's scope needs to expand significantly
- When backend requests a capability that does not exist yet
- When evaluating whether a task needs an agent or a simpler solution

## The first question to answer
Does this actually need an agent?

An agent is justified when:
  - The task requires natural language understanding or generation
  - The task is open-ended and cannot be solved by a fixed algorithm
  - The task benefits from few-shot examples or contextual reasoning

An agent is NOT justified when:
  - A deterministic function or API call is sufficient
  - The output is always the same given the same input
  - The task is purely computational (use backend logic instead)

## Design output format
Produce a written proposal in agents/sprints/current.md Notes with:

  name:          {agent-name} — kebab-case, descriptive
  purpose:       one sentence describing what the agent does
  consumed_by:   which backend service or endpoint will call this agent
  input:         what the agent receives (user message, context, data)
  output:         what the agent returns (format, length, structure)
  tools_needed:  list of MCP tools required and why
  scope:         what the agent can do
  out_of_scope:  what the agent must explicitly refuse
  risks:         hallucination surface, tool misuse, prompt injection vectors
  contract_impact: does this require a new file in docs/contracts/agents/?

## Scope design rules
- Narrow scope beats wide scope — an agent that does one thing well
  is more reliable than one that tries to do everything
- Every capability must be testable — if you cannot write a test case
  for a capability, the capability is too vague to implement
- Every out_of_scope item must have a corresponding rule in rules.md
- Prompt injection is a risk for every agent — design rules.md
  to explicitly reject instructions embedded in user input

## Decision
# Left blank — to be filled by the human after reviewing the proposal.
# Agent does not proceed to coding/SKILL.md until human approves.