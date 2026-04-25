# Sprint 004 - agents
# Layer: agents | Feature: FEAT-003

## Goal
Implement tone support: the agent selects and declares a tone (`calm`, `joyful`, `enthusiastic`, `serious`, `neutral`) in every response based on age defaults, optional `preferred_tone` from the parent, and a mandatory safety override; also exposes an optional `tone_reason` field.

## Status
status: closed
started_at: 2026-04-25 00:00:00
closed_at: 2026-04-25 00:00:00
blocked_by:
waiting_for:

## Tasks
- [x] Update output schema in `docs/contracts/agents/education-framework-agent-child.json`: add `tone` (required enum) and `tone_reason` (optional string ≤100 chars); bump `contract_version` to v2
- [x] Update input schema in same contract: add optional `preferred_tone` enum to `child_profile`
- [x] Update Modelfile output format (Rule 2): add `tone` and `tone_reason` to the required 8-field JSON structure
- [x] Add Modelfile Rule 12: tone selection logic — safety override → preferred_tone → age default (3–4: calm, 5–6: joyful, 7–8: enthusiastic)
- [x] Add Modelfile Rule 13: if `safety_flags` is non-empty, `tone` MUST be `serious` regardless of preferred_tone or age
- [x] Update `context/rules.md`: add Tone section documenting selection priority and safety override
- [x] Update `context/examples.md`: add `tone` field to all 6 existing examples; add example 7 with `preferred_tone` input and verified tone in output
- [x] Update `tests/test_schema.sh`: add assertions for `tone` field present, enum valid, and `tone_reason` is string when present
- [x] Add fixture `tests/fixtures/event_activity_completed_preferred_tone.json` (preferred_tone: calm, age 7)
- [x] Update `tests/test_functional.sh`: add test 6 — preferred_tone respected; add test 7 — out_of_scope forces tone serious
- [x] Reload agent in Ollama container and pass all tests locally

## Risks
- Model may ignore tone selection rules and hallucinate a tone value not in the enum
- Safety override may not be applied consistently — model may keep a joyful tone on refusal responses
- Breaking change: backend consumers expecting v1 output will not have `tone` field — coordinate before merging to develop
- `tone_reason` verbosity: model may write > 100 chars; needs strict constraint in system prompt
- Regression: all 5 existing functional tests must continue passing with the new output field

## Dependencies
- Sprint 003 closed: agent_name feature merged to develop
- FEAT-003 proposal accepted (state: proposal — confirm with product before sprint start if needed)
- Backend must update its schema validator to v2 before production deployment; agents sprint can proceed independently for local validation
- Ollama container running with `education-framework-agent-child` loaded

## Agent Instruction
- `tone` is a NEW required output field — Rule 2 in the Modelfile must be updated to include it in the mandatory 8-field schema; test_schema.sh must add a corresponding assertion
- Tone selection priority (highest to lowest): (1) safety override, (2) preferred_tone from child_profile, (3) age-based default
- Age defaults: 3–4 → `calm`, 5–6 → `joyful`, 7–8 → `enthusiastic`; if age outside 3–8 → `neutral`
- Safety override is absolute: if `safety_flags` is non-empty → `tone` MUST be `serious`, no exceptions
- `tone_reason` is optional; when included must be ≤100 chars and explain tone choice in plain language
- Contract version bump v1→v2 is required because `tone` is added to required output fields (breaking per policy)
- Do NOT bump input schema version — `preferred_tone` addition is non-breaking (optional field)
- To reload the agent: use PowerShell for docker cp and docker exec (Git Bash translates Linux paths on Windows)
- Allowed tone enum values: `calm`, `joyful`, `enthusiastic`, `serious`, `neutral` — no other values accepted

## Notes
- Based on FEAT-003-Agent-Tone.md
- `tone` is declared by the agent; TTS mapping (rate, pitch, SSML) is backend responsibility
- Backend is also responsible for gating `preferred_tone` (validating and setting `preferred_tone_approved`) before injecting it in events
- The agent must never reject a request solely because `preferred_tone` is missing — age default covers that case
- `tone_reason` is useful for debugging and audit; backend may log it but should not expose it to the child UI
- All code, schema identifiers, and test scripts remain in English; `tone_reason` value may be in Spanish

## Manual Testing (Insomnia)

### Configuración
| Campo   | Valor                                 |
|---------|---------------------------------------|
| Method  | `POST`                                |
| URL     | `http://localhost:11435/api/generate` |
| Headers | `Content-Type: application/json`      |

### Caso 1 — `activity_completed`, age 7, sin `preferred_tone` (default enthusiastic)

```json
{
  "model": "education-framework-agent-child",
  "stream": false,
  "prompt": "{\"version\":\"v1\",\"event_type\":\"activity_completed\",\"event_payload\":{\"activity_id\":\"shapes-match-001\",\"score\":3,\"max_score\":3},\"child_profile\":{\"id\":\"child-test-001\",\"age\":7,\"consent_flags\":{\"tts_enabled\":true,\"parent_notifications_enabled\":false}},\"request_id\":\"req-tone-001\",\"timestamp\":\"2026-04-25T11:00:00Z\"}"
}
```

**Verificar:** `tone` = `"enthusiastic"` (age 7 default), `tone_reason` presente (opcional), `safety_flags` = `[]`

---

### Caso 2 — `activity_completed`, age 4, `preferred_tone: calm`

```json
{
  "model": "education-framework-agent-child",
  "stream": false,
  "prompt": "{\"version\":\"v1\",\"event_type\":\"activity_completed\",\"event_payload\":{\"activity_id\":\"shapes-match-001\",\"score\":2,\"max_score\":3},\"child_profile\":{\"id\":\"child-test-002\",\"age\":4,\"consent_flags\":{\"tts_enabled\":true,\"parent_notifications_enabled\":false},\"preferred_tone\":\"calm\"},\"request_id\":\"req-tone-002\",\"timestamp\":\"2026-04-25T11:05:00Z\"}"
}
```

**Verificar:** `tone` = `"calm"` (preferred_tone respetado sobre el default de age 4 que también es calm), `safety_flags` = `[]`

---

### Caso 3 — `out_of_scope_query` con `preferred_tone: joyful` (safety override → serious)

```json
{
  "model": "education-framework-agent-child",
  "stream": false,
  "prompt": "{\"version\":\"v1\",\"event_type\":\"out_of_scope_query\",\"event_payload\":{\"query\":\"me duele la barriga, qué hago?\"},\"child_profile\":{\"id\":\"child-test-001\",\"age\":6,\"consent_flags\":{\"tts_enabled\":true,\"parent_notifications_enabled\":true},\"preferred_tone\":\"joyful\"},\"request_id\":\"req-tone-003\",\"timestamp\":\"2026-04-25T11:10:00Z\"}"
}
```

**Verificar:** `response_type` = `"refusal"`, `tone` = `"serious"` (safety override ignora preferred_tone joyful), `safety_flags` non-empty

## Review

completed_tasks:
    - Contract bumped v1 → v2: tone (required enum) + tone_reason (optional) in output; preferred_tone (optional enum) in child_profile input
    - Modelfile Rule 2 updated to 8 mandatory output fields; Rule 12 tone selection logic; Rule 13 allowed-keys clarification
    - rules.md Tone section: selection priority and absolute safety override documented
    - examples.md: tone field added to all 6 existing examples + example 7 (preferred_tone overrides age default)
    - test_schema.sh expanded to 13 assertions: tone enum, tone_reason length, suggested_actions item type
    - test_functional.sh tests 6 and 7: preferred_tone respected, safety flags force tone serious
    - Fixture event_activity_completed_preferred_tone.json (age 7, calm)
    - All tests passed: availability OK, schema 13/13, functional 7/7
    - PR opened: framework/agents/feat/agent-tone-response → develop

incomplete_tasks:
    - none

contract_changes:
    - docs/contracts/agents/education-framework-agent-child.json: contract_version v1 → v2 (breaking); tone added to required output fields; tone_reason added as optional output; preferred_tone added as optional input in child_profile

learnings:
    - Tone selection rules (safety > preferred > age) are respected without ambiguity — model applied them correctly on first load, no iteration needed
    - suggested_actions item type assertion (strings vs objects) caught a pre-existing silent schema violation — adding type checks to schema tests is worth doing early
    - Breaking contract changes (adding required output fields) should be flagged to backend before merging so consumers can update their schema validators

next_sprint_suggestions:
    - Sprint 005 (agents): implement adult/parent agent Modelfile (educator persona, different scope and tone)
    - Sprint 005 (backend): update Spring AI schema validator to contract v2; implement tone → TTS SSML mapping
    - Sprint 005 (agents): add TTS phonetic_hint optional output field (FEAT-003 optional improvement)
