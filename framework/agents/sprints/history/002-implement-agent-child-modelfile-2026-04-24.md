# Sprint 002 - agents
# Layer: agents | Feature: FEAT-001

## Goal
Build, validate, and document the Modelfile for `education-framework-agent-child` ("Nubi") so the agent runs on Ollama and returns schema-valid JSON responses.

## Status
status: closed
started_at: 2026-04-23 00:00:00
closed_at: 2026-04-24 00:00:00
blocked_by:
waiting_for:

## Tasks
- [x] Create `framework/agents/education-framework-agent-child/Modelfile` with system prompt, model base, and parameters
- [x] Finalize and write input JSON schema to `docs/contracts/agents/education-framework-agent-child.json`
- [x] Finalize and write output JSON schema to `docs/contracts/agents/education-framework-agent-child.json`
- [x] Load the agent into the Ollama container (`docker exec ollama-educational ollama create education-framework-agent-child -f /tmp/Modelfile`)
- [x] Validate agent returns schema-compliant JSON for: `activity_completed`, `help_requested`, `out_of_scope_query`
- [x] Create `framework/agents/education-framework-agent-child/context/rules.md`
- [x] Create `framework/agents/education-framework-agent-child/context/examples.md` (≥3 input→output pairs)
- [x] Create `framework/agents/education-framework-agent-child/tools/mcp-tools.json`
- [x] Write `framework/agents/education-framework-agent-child/tests/test_availability.sh` — asserts container is running and model is loaded
- [x] Write `framework/agents/education-framework-agent-child/tests/test_schema.sh` — sends a synthetic payload and validates required JSON fields and constraints
- [x] Write `framework/agents/education-framework-agent-child/tests/test_functional.sh` — asserts correct response_type for `activity_completed`, `help_requested`, `out_of_scope_query`
- [x] Add test execution step to `.github/workflows/ci-infrastructure.yml` (availability test only; schema/functional require running container)

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

## Review

completed_tasks:
    - Modelfile for education-framework-agent-child (Nubi) built, loaded, and validated in Ollama container
    - Input/output JSON schema finalized and promoted to approved in docs/contracts/agents/
    - context/rules.md and context/examples.md (5 examples) documented
    - tools/mcp-tools.json with 4 typed, non-destructive tools
    - Three-tier test suite: test_availability.sh, test_schema.sh, test_functional.sh — all passed locally
    - CI job agent-availability added to ci-infrastructure.yml (tier 1, container check only)
    - docker-compose.ci.yml override created to allow Ollama to start without NVIDIA runtime in CI

incomplete_tasks:
    - Tier 2 and tier 3 tests not wired to CI (requires live model in runner — out of scope by design)

contract_changes:
    - docs/contracts/agents/education-framework-agent-child.json created (status: approved, version: v1)
    - Output schema Rule 2 tightened: all 7 fields mandatory even when empty (safety_flags omission bug)

learnings:
    - Git Bash on Windows translates absolute Linux paths in docker commands to Windows paths; use PowerShell for docker cp and docker exec with Linux paths
    - LLMs tend to omit empty array fields unless the system prompt is explicit — "NEVER omit these keys" language was required
    - docker-compose runtime: nvidia must be overridden for CI; a ci override file is the cleanest approach

next_sprint_suggestions:
    - Sprint 003 (agents): implement agent_name customization per child profile (FEAT-002)
    - Sprint 003 (backend): implement Spring AI integration that calls education-framework-agent-child via HTTP on educational-network-dev
    - Add CI secret scanning step to prevent real env values reaching the repository
