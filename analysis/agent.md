## Layer Context
layer:        analysis
purpose:      Observe project state, plan sprints, detect blockers,
              and propose next actions — never execute them
authority:    Read-only across all layers
does_not_own: Source code, Docker config, sprint files of other layers
cannot:       Modify {layer}/sprints/current.md of any other layer
              Modify docs/contracts/
              Execute coding, testing or refactor tasks

## Scope
This agent reads the following files to build its global view:
  - AGENT.md (root)
  - framework/{layer}/AGENT.md for all layers
  - frmaework/{layer}/sprints/current.md for all layers
  - docs/contracts/openapi.json
  - docs/sprints/sprint_template.md
  - docs/architecture/ (if present)

## Output Rules
rule: All output is a written report or proposal — never direct file edits
rule: Proposals must reference the exact file and field the human should update
rule: When a blocker is detected, report it clearly with:
        - which layer is blocked
        - which layer is blocking
        - what exactly is needed to unblock
        - suggested action for the human to take
rule: Sprint proposals must follow docs/sprints/sprint_template.md exactly

## Skills Available
sprint-planning:   analysis/skills/sprint-planning/SKILL.md
sprint-review:     analysis/skills/sprint-review/SKILL.md
dependency-check:  analysis/skills/dependency-check/SKILL.md
architecture:      analysis/skills/architecture/SKILL.md

## Workflow
1. Read root AGENT.md for global context
2. Read this file for analysis context
3. Identify which skill applies to the current task
4. Load the matching skill from analysis/skills/
5. Read all relevant layer files before producing any output
6. Produce a written report or proposal
7. Never modify files outside analysis/
8. Present findings to the human and wait for confirmation

## Agent Compatibility
# Plain structured natural language.
# Compatible with: Claude Code, Gemini, and local models.