# SKILL — agents/coding
# ─────────────────────────────────────────────
# Use this skill for: creating a new agent from scratch, adding MCP tools,
# writing system prompts, defining workflows and few-shot examples.

## Creating a new agent
Follow this sequence — do not skip steps:
1. Read framework/agents/agent.md for layer context
2. Read framework/skills/design/SKILL.md and produce a written proposal first
3. Wait for human approval of the proposal before writing any file
4. Create the agent directory: framework/agents/{agent-name}/
5. Write Modelfile — base model, parameters, system prompt
6. Write context/rules.md — hard constraints and tone
7. Write context/examples.md — minimum 3 few-shot examples
8. Write context/workflows.md — at least 1 workflow
9. Write tools/mcp-tools.json — only tools the agent genuinely needs
10. Publish contract to docs/contracts/agents/{agent-name}.json
11. Mark tasks complete in agents/sprints/current.md

## System prompt rules
- Open with role definition in one sentence: "You are a {role} that {purpose}"
- State RULES explicitly in a numbered or bulleted block
- State RESPONSE FORMAT explicitly — never leave it implicit
- State SCOPE explicitly — what the agent can and cannot do
- Keep the system prompt under 500 words
- Never put examples inside the system prompt — use context/examples.md

## MCP tool rules
- Only define tools the agent will actually use
- Each tool must have a clear, unambiguous description
- Input schemas must be strict — use required fields, avoid optional when possible
- Mark breaking_if_removed: true for any tool backend actively calls
- Never define a tool that performs destructive operations

## Modelfile parameters
temperature:    0.1–0.3 for task-focused agents, 0.5–0.7 for creative agents
top_p:          0.9 default — lower for more deterministic output
num_ctx:        4096 minimum — increase only if workflows require long context
repeat_penalty: 1.1 default — prevents repetitive outputs

## After coding
- Build and smoke-test locally: ollama create {name} -f Modelfile
- Run at least one manual test from context/examples.md
- Update docs/contracts/agents/{name}.json
- Check agents/sprints/current.md and mark completed tasks