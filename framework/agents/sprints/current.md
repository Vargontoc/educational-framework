# Sprint 002 - agents
# Layer: agents | Feature: FEAT-001

## Goal
Build, validate, and document the Modelfile for `education-framework-agent-child` ("Nubi") so the agent runs on Ollama and returns schema-valid JSON responses.

## Status
status: active
started_at: 2026-04-23 00:00:00
closed_at:
blocked_by:
waiting_for:

## Tasks
- [ ] Create `framework/agents/education-framework-agent-child/Modelfile` with system prompt, model base, and parameters
- [ ] Finalize and write input JSON schema to `docs/contracts/agents/education-framework-agent-child.json`
- [ ] Finalize and write output JSON schema to `docs/contracts/agents/education-framework-agent-child.json`
- [ ] Load the agent into the Ollama container (`docker exec ollama-educational ollama create education-framework-agent-child -f /tmp/Modelfile`)
- [ ] Validate agent returns schema-compliant JSON for: `activity_completed`, `help_requested`, `out_of_scope_query`
- [ ] Create `framework/agents/education-framework-agent-child/context/rules.md`
- [ ] Create `framework/agents/education-framework-agent-child/context/examples.md` (≥3 input→output pairs)
- [ ] Create `framework/agents/education-framework-agent-child/tools/mcp-tools.json`
- [ ] Write `framework/agents/education-framework-agent-child/tests/test_availability.sh` — asserts container is running and model is loaded
- [ ] Write `framework/agents/education-framework-agent-child/tests/test_schema.sh` — sends a synthetic payload and validates required JSON fields and constraints
- [ ] Write `framework/agents/education-framework-agent-child/tests/test_functional.sh` — asserts correct response_type for `activity_completed`, `help_requested`, `out_of_scope_query`
- [ ] Add test execution step to `.github/workflows/ci-infrastructure.yml` (availability test only; schema/functional require running container)

## Risks
- Hallucination: agent may produce incorrect or misleading information for children (ages 3–8)
- PII exposure: test payloads must not include real child data
- Out-of-scope responses: agent may answer prompts it must refuse (medical, legal)
- Prompt injection via crafted event_payload values
- Model context overflow under load (num_ctx must be ≥ 4096)

## Dependencies
- Ollama running in Docker: container `ollama-educational` via `framework/infrastructure/docker-compose.yml`, network `educational-network-dev` (port NOT exposed to host)
- Base model available inside container: `qwen2.5:7b-instruct-q5_K_M`
- Sprint 001 design proposals are the approved basis for this implementation
- `docs/contracts/agents/` directory must exist before writing contract file

## Agent Instruction
- Follow `framework/agents/skills/coding/SKILL.md` for all file creation
- Modelfile parameters: temperature 0.1–0.25, top_p 0.9, num_ctx ≥ 4096
- System prompt structure: (1) role sentence, (2) explicit RULES block, (3) output format instruction
- Agent output must always be a single valid JSON object matching the v1 schema from FEAT-001 — no prose, no markdown fences
- content_text must be ≤ 300 characters (TTS constraint)
- Allowed MCP tools only: `fetch_child_profile`, `fetch_recommended_activity`, `record_event`, `send_parent_notification`
- After Modelfile and contract are stable, set backend sprint `waiting_for` if the contract is a breaking change
- To load the agent: copy Modelfile into the container (`docker cp`) then run `docker exec ollama-educational ollama create`; verify with `docker exec ollama-educational ollama list`
- To test from host: route requests through `docker exec ollama-educational ollama run` or from a container on `educational-network-dev` at `http://ollama-educational:11434`
- Minimum passing test: all three test scripts must exit 0 before the sprint closes
- Tests are bash + jq scripts; no runtime stack decision required at this stage
- Tests live in `framework/agents/education-framework-agent-child/tests/`; each script is independently executable

## Notes
- Based on FEAT-001 and ADR-002
- Input and output schemas in FEAT-001 are marked "proposed" — finalize them during this sprint and promote to "approved" in the contract file
- Backend owns PII redaction, consent checks, schema validation, and audit logging — do not replicate these inside the agent
- The "invalid" example in FEAT-001 is actually a valid refusal response — the label in the feature file is misleading; treat it as a valid example
- Response language for content_text may be localized but all code, comments, and schema identifiers must remain in English

### Proposed test structure

```
framework/agents/education-framework-agent-child/tests/
  test_availability.sh    # tier 1: no model call needed
  test_schema.sh          # tier 2: one model call, structural assertions
  test_functional.sh      # tier 3: three model calls, behavioral assertions
  fixtures/
    event_activity_completed.json
    event_help_requested.json
    event_out_of_scope_query.json
```

**test_availability.sh** (tier 1 — CI-safe)
```
1. docker ps --filter name=ollama-educational --filter status=running → exit 1 if not found
2. docker exec ollama-educational ollama list | grep education-framework-agent-child → exit 1 if not found
```

**test_schema.sh** (tier 2 — requires running container)
```
Send fixtures/event_activity_completed.json via:
  docker exec ollama-educational ollama run education-framework-agent-child < payload
Assert with jq:
  - .version == "v1"
  - .response_type ∈ [narration, prompt, action, tool_call, refusal]
  - .content_text is string and length ≤ 300
  - .content_type ∈ [plain_text, tts_snippet, structured_activity]
  - .safety_flags is array
  - .tool_calls is array
  - no unexpected top-level keys (additionalProperties: false)
```

**test_functional.sh** (tier 3 — requires running container)
```
event_activity_completed  → assert response_type ∈ [narration, prompt]
event_help_requested      → assert response_type ∈ [narration, action] and safety_flags == []
event_out_of_scope_query  → assert response_type == refusal and safety_flags is not empty
```

**CI integration:** only tier 1 runs in `.github/workflows/ci-infrastructure.yml` (no Docker daemon with GPU in CI). Tier 2 and 3 are run locally or in a dedicated integration environment.

## Review

completed_tasks:
    -

incomplete_tasks:
    -

contract_changes:
    -

learnings:
    -

next_sprint_suggestions:
    -
