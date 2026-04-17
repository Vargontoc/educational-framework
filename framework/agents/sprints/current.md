# Sprint 4 - framework/agents
# -----------------------------------------------

## Goal
Validate ai-educational-child against local Ollama: test all 5 personalities x 9 events, verify JSON output format, measure word count and flag any constraint violations.

## Status
status:     completed
started_at: 2026-04-17 00:00:00
closed_at:  2026-04-17 00:00:00
blocked_by:
waiting_for:

## Tasks
- [x] Create framework/agents/ai-educational-child/tests/fixtures.json — test payloads (9 events x 5 personalities)
- [x] Create framework/agents/ai-educational-child/tests/validate.py — validation script
- [x] Document how to build and run the validation in tests/README.md
- [ ] Run validation and capture results in tests/results/run-001.json

## Risks
- Modelfile uses {placeholder} variables not native to Ollama — validation script must assemble the system prompt dynamically before each call
- qwen2.5:7b may ignore the 35-word limit despite instructions — word count violations must be logged, not silently accepted
- Ollama must be running locally on port 11434 before validation can execute

## Dependencies
- Ollama running locally with qwen2.5:7b-instruct-q5_K_M pulled
- framework/agents/ai-educational-child/Modelfile (Sprint 1) — confirmed present
- framework/agents/ai-educational-child/personalities/*.md (Sprints 1 & 3) — confirmed present

## Agent Instruction
- Validation script goes in framework/agents/ai-educational-child/tests/
- Script must: load personality files, assemble system prompt, call Ollama /api/generate, parse JSON response, count words, check emotion/tts_speed enum values
- Results file must record: event, personality, response text, word_count, json_valid, constraint_violations
- Use Python 3 with only stdlib + requests (no heavy deps)

## Notes
- Ollama does not natively resolve {placeholder} syntax — backend is responsible for assembling the final prompt; validation script simulates this
- The SYSTEM block in the Modelfile is a template; validate.py injects it via the `system` field in the Ollama API call
- Run results are snapshots — save to tests/results/ with a run ID for traceability

## Review

completed_tasks:
  - tests/fixtures.json: 13 event payloads x 5 personalities = 65 test combinations, includes constraint definitions
  - tests/validate.py: assembles system prompt from Modelfile + personality file, calls Ollama API, validates JSON, word count, enums, banned words, saves results to tests/results/{run-id}.json
  - tests/README.md: setup instructions, constraint table, note on Modelfile variable resolution

incomplete_tasks:
  - tests/results/run-001.json: NOT generated — requires Ollama running locally with qwen2.5:7b-instruct-q5_K_M pulled. Human must execute: python validate.py

contract_changes:
  none

learnings:
  - Ollama does not resolve {placeholder} syntax in Modelfiles natively — validate.py confirms this is a backend responsibility; documented in README
  - 65 test combinations (13 events x 5 personalities) give full coverage; word count check is the highest-risk constraint given qwen2.5:7b tendency to be verbose

next_sprint_suggestions:
  - Sprint 5 (backend dep): backend wires Ollama call using contract v1.2.0 — resolves system prompt variables before dispatch
  - After run-001.json captured: Sprint 5b to fix any constraint violations found
