# Sprint 1 - framework/agents
# -----------------------------------------------

## Goal
Define and implement the v1 MVP of the ai-educational-child agent (Nubi) with cheerful and calm personalities, covering the core event set and publishing its capability contract.

## Status
status:     completed
started_at: 2026-04-17 00:00:00
closed_at:  2026-04-17 00:00:00
blocked_by:
waiting_for:

## Tasks
- [x] Create Modelfile with base system prompt and parameters
- [x] Write context/rules.md — hard constraints (child safety, response length, tone)
- [x] Write context/examples.md — few-shot examples for all core events, cheerful + calm
- [x] Write context/workflows.md — hint loop and idle recovery sequences
- [x] Write personalities/cheerful.md
- [x] Write personalities/calm.md
- [x] Write tools/mcp-tools.json (v1 minimal — no external tools)
- [x] Publish docs/contracts/agents/ai-educational-child.json

## Risks
- qwen2.5:7b may produce responses longer than 35 words despite instructions — mitigation: enforce max_tokens=40 at backend call level and add truncation rule in rules.md
- Model may break character under unusual event payloads — mitigation: strict unknown-event fallback in rules.md
- Few-shot examples must cover age range 3–8: younger children need even simpler vocabulary — split examples by age_group field

## Dependencies
- No blocking dependencies for v1 (no MCP tools required)
- Backend layer will need this contract before wiring the Ollama call — not yet started, no blocker declared

## Agent Instruction
- All files created under framework/agents/ai-educational-child/
- Modelfile base model: qwen2.5:7b-instruct-q5_K_M
- Default companion name in system prompt: Nubi (injected as {companion_name} variable)
- Personalities cheerful and calm are mandatory for v1; others are future sprints
- examples.md must have at minimum 2 examples per event type per personality
- Contract version starts at 1.0.0
- Do NOT create backend files — only agents layer files and docs/contracts/agents/

## Notes
- Agent name "Nubi" is the default but must be configurable via payload field companion_name
- This agent is event-driven: it does not initiate conversation, only reacts to events
- TTS integration is handled by backend; agent only returns text + emotion + tts_speed metadata
- Age range 3–8 requires hard vocabulary limits; examples.md must reflect this
- Coletillas (basketball + nursing flavor tags) added to both personalities mid-sprint — non-breaking addition

## Review

completed_tasks:
  - Modelfile created with parameterized system prompt ({companion_name}, {child_name}, {personality_prompt}, {language})
  - context/rules.md: 9 hard constraint categories (length, vocabulary, identity, content safety, output format, known events)
  - context/examples.md: 2 examples × 9 events × 2 personalities = 36 base examples + 9 coletilla examples (7 positive, 2 negative)
  - context/workflows.md: hint loop, idle recovery, frustration detection, happy path session flow, event context fields reference
  - personalities/cheerful.md: high-energy tone + basketball/nursing coletillas (max 1/5 responses, banned on fail and emotion.detected)
  - personalities/calm.md: gentle tone + soft coletilla variants (max 1/6 responses, banned on emotion.detected)
  - tools/mcp-tools.json: empty v1 (no external tools required)
  - docs/contracts/agents/ai-educational-child.json: v1.0.0 contract with full input/output schema, constraints, personality inventory, breaking/non-breaking change rules

incomplete_tasks:
  none

contract_changes:
  - docs/contracts/agents/ai-educational-child.json created at v1.0.0
  - No backend sprint dependency declared (backend layer not yet started)

learnings:
  - Coletillas (basketball + nursing) added as unplanned scope mid-sprint — absorbed as non-breaking personality extension
  - Negative few-shot examples (showing what NOT to do with coletillas) are as important as positive ones for event-gated behavior
  - Modelfile uses placeholder variables ({personality_prompt}) that backend must resolve before calling Ollama — this is a backend responsibility, should be noted in contract

next_sprint_suggestions:
  - Sprint 2: Implement remaining personalities — explorer, wise, silly
  - Sprint 3: Validate agent against Ollama locally — test each event type with both personalities, measure response word count
  - Sprint 4 (backend dependency): Backend wires Ollama call using contract v1.0.0 — resolves {personality_prompt} variable before dispatch
