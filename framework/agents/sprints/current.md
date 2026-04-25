# Sprint 007 - agents
# Layer: agents | Feature: FEAT-006

## Goal
Implement motivation_action injection (Option A — strict narration): when the backend injects an optional `motivation_action` object into an existing event's payload, the agent uses the curated `motivation_action.text` verbatim in its response and, when `external_suggestion` is present, naturally encourages the child to ask an adult. The agent must NEVER generate its own motivational text — only use the text provided by the backend. The agent must NEVER make the child feel the app is the only source of learning.

## Status
status: pending
started_at:
closed_at:
blocked_by:
waiting_for:

## Tasks
- [ ] Update contract: document `motivation_action` optional object structure in `event_payload` description (non-breaking — event_payload already additionalProperties: true; no version bump)
- [ ] Update Modelfile: add Rule 16 — when `event_payload.motivation_action.text` is present, include it verbatim; adapt warmth to `type` (praise/reward/suggestion/challenge); when `external_suggestion` present, add ask-an-adult phrase using the hint; NEVER create app dependency; 300-char limit applies
- [ ] Update `context/rules.md`: add Motivation Action Rules section (after Muletilla Injection Rules)
- [ ] Update `context/examples.md`: add Example 10 — `activity_completed` with `motivation_action` (type: praise, external_suggestion present); output uses motivation text and surfaces external suggestion naturally
- [ ] Add fixture `tests/fixtures/event_activity_completed_with_motivation.json`
- [ ] Update `tests/test_functional.sh`: add Test 10 — motivation_action.text keyword in content_text; response_type narration or prompt; safety_flags empty
- [ ] Rebuild model in Ollama container and run all tests (availability + schema tier 2 + functional tier 3)

## Risks
- Model may ignore `motivation_action.text` when event context fields are richer → Rule 16 WRONG/CORRECT examples required
- `external_suggestion.hint` may not surface in content_text → explicit rule: when external_suggestion is present, the hint MUST appear naturally in content_text
- Both `muletilla` + `motivation_action` in same event payload → combined text may exceed 300 chars → 300-char limit is the natural guard; backend should avoid sending both simultaneously (document this)
- Regression: all 9 existing functional tests must continue passing after Modelfile update

## Dependencies
- Sprint 003–006 PRs available in develop (can proceed from current develop)
- Ollama container running with `education-framework-agent-child` loaded for local validation
- Backend is responsible for: rotation pool, rate-limiting, intensity selection, parental controls, and motivation_action selection before sending to the agent
- Option B (allow_paraphrase) explicitly deferred — backend embedding + NER infrastructure not yet built

## Agent Instruction
- motivation_action is injected as an optional object inside `event_payload` of any existing event type
- Input structure: `event_payload.motivation_action = { action_id: string, type: "praise"|"reward"|"suggestion"|"challenge", text: string, intensity: "low"|"medium"|"high", external_suggestion?: { label: string, hint: string } }`
- When present: agent MUST include `motivation_action.text` verbatim (or near-verbatim) in content_text
- Adapt warmth to `type`: praise → warm celebration; reward → celebrate achievement; suggestion → gently redirect toward broader curiosity; challenge → pose a playful dare
- When `external_suggestion` present: add a natural phrase encouraging the child to ask an adult (e.g. "¿Le preguntas a alguien en casa [hint]?")
- NEVER make the child feel the app is the only source of learning
- WRONG: ignoring motivation_action.text and writing generic praise ("¡Muy bien!") when the backend sent a specific praise text
- CORRECT: "¡Lo lograste! Hiciste las tres figuras perfectas. ¿Le cuentas a alguien en casa lo que aprendiste?"
- 300-char limit applies to the full content_text; if needed, shorten the framing — never truncate motivation_action.text
- When motivation_action is absent: agent responds normally without any injected motivational text
- No new event_type needed → contract_version stays at v3 (non-breaking addition)
- No new output fields needed → output schema unchanged
- intensity field is informational at agent level — backend selected the right text for the configured intensity; agent does not re-interpret intensity
- Git branch from develop: `framework/agents/feat/agent-motivation-scope`

## Notes
- Pattern mirrors muletilla injection (Sprint 006): backend curates and selects; agent uses verbatim (Option A)
- `curiosity_id` on motivation_action is a backend cross-reference only; agent does not receive or use it
- Both muletilla + motivation_action in the same payload is an edge case the backend should prevent; if both arrive, agent should include both if they fit within 300 chars (muletilla at start, motivation text in body)
- The app-dependency prohibition ("NEVER make child feel app is the only source of learning") is a new explicit behavioral constraint, distinct from safety refusal — it applies to normal responses too

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
