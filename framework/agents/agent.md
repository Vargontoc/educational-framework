# agent.md - agents layer
# -----------------------------------

## Layer context

layer: agents
purpose: Define, version and maintain domain-specific AI agents that are consumed by the backend as application feature
runtime: Ollama (local) - model: qwen2.5:7b-instruct-q5_K_M
does_not_own: API endpoints, business logic, frontend components
consumed_by: backend layer via Ollama API (http://localhost:11434)

## Agent Structure

Each domain agent lives in its own subdirectory:
    {agent-name}/
        Modelfile           -> base model, system prompt, parametes
        context/
            rules.md        -> behaviour rules and hard constraints
            examples.md     -> few-shot examples (input -> expected output)
            workflows.md    -> multi-step task definitions
        tools/
            mcp-tools.json  -> MCP tool definition available to this agent

## Contract

output: docs/contracts/agents/{agent-name}.json
rule: Every agent must have a contract file in docs/contracts/agent
rule: The contract describes capabilities, input/output shape and tool list
rule: Any change to modelfile or mcp-tools.json requires contract update
rule: Backend must be notified when contract changes - set backend sprint waiting_for field if the change is breaking

## Versioning

rule: Modelfile changes are breaking if they alter
    
    - System prompt scope
    - Available MCP tools (add or remove)
    - Response format or output shape

rule: Non breaking changes: tone adjustments, example additions, parameter tuning (temperature, top_p)
rule: Breaking changes require a new contract version and backend sprint task

## Skills Available

coding:     framework/agents/skills/coding/SKILL.md
refactor:   framework/agents/skills/refactor/SKILL.md
design:     framework/agents/skills/design/SKILL.md

## Sprint Context

current_sprint: framework/agents/sprints/current.md

## Workflow

1. Read root agent.md for global context
2. Read this file for agents layer context
3. Check framework/agents/sprints/current.md - if blocked, stop and report
4. Identify wich agent is being worked on
5. Load the matching skill from framework/agents/skills/
6. Never modify other layers source files directly
7. After any braking change, update docs/contracts/agents/{name}.json
8. Notify framework/backend/server via Sprint dependency if contract changed
9. Commit following: agents/type/shot-description

## Agent Compatibility

Plain structured natural language. No tool-specific syntax
Compatible with: Claude Code, Gemini, ChatGPT, and local models
