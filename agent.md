# Project context

name: ai-educational-framework
purpose: Base template to validate a layered multi-agent worklow structure
scope: Minigames and interactive activities with AI-powered agents and TTS narration
languague: All code and comments must be in English

## Stack

agents: Ollama - qwen2.5:7b-instruct-q5_K_M


## Layer Structure

analysis/           -> read-only agent: observes, plans, reports
docs/               -> architecture decisions, contractsm sprint management
framework/agents    -> domain AI agents, Modelfiles, context, MCP Tools

## Contracts

Single source of truth for all inter-layer arguments
No layer duplicates contract files locally
docs/contracts/agents/{name}.json   -> agent capability contract (agents =/= backend)

## Sprint Structure

Sprints are managed pewr layer to reflect independent progress.

docs/sprints/sprint_template.md     -> global reusable template for all layers
docs/sprints/history/               -> closed sprints archive (global)
{layer}/sprints/current.md          -> active sprint for that layer
{layer}/sprints/history/            -> closed sprint archive for that layer

If a layer is blocked by another, current.md must declare
    status: blocked
    blocked_by: {layer}/sprints/current.md
    waiting_for: description of what is needed

## Global Rules

- All code, comments, variable names and documentation must be in English
- Commit messages follow Conventional Commits: type(scope): short description
- Branch naming: {layer}/type/short-description
- No secrets or credentials committed to the repository
- Each layer is independently deployable
- Changes to one layer must not break the contracts in docs/contracts
- Frontend must necer reference backend source directly, only via docs/contracts/openapi.json and docs/contracts/websocket.json
- Backend mustr never reference agent Modelfiles directly, only via docs/contracts/agents/

## Available Agents

framework/agents/agent.md           -> manages domain AI agents, Modelfiles and capability contracts

## Workflow

1. Read this file first to understand the global project context
2. Identify wich layer the task belongs to
3. Check {layer}/sprints/current.md for active sprint status
4. If status is blocked, do not proceed - rport the blocker
5. Navigate to that layer and read its agent.md before acting
6. Load the relevant skill from that layer's skills/ folder
7. Follow the Agent Instructions in current.md for this sprint
8. After any contract change, update the relevant file in docs/contracts
9. Commit changes following the Global Roles above



## Git Workflow

base_branch: develop
protected: true - no direct push allowed
rule: Bever push directly to develop
rule: Always pull develop before starting a new sprint branch
rule: Branch name must follow {layer}/type/short-description
rule: One branch per sprint task - do not mix layer changes in one branch

## Sprint Start Sequence

1. git checkout develop
2. git pull origin develop
3. git checkout -b {layer}/type/short-description
4. Read {layer}/sprints/current.md
5. Begin work

## Sprint End Sequence

1. git add .
2. git commit -m "type(scope): short description"
3. git push origin {current-branch}
4. Open PR to develop via GitHub
5. Notify human to review and merge
6. Do not merge without human confirmation

## Agent Compatibility

Plain structured natural language. No tool-specific syntax
Compatible with: Claude Code, Gemini, ChatGPT, and local models


