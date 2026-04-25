# Sprint 005 - agents
# Layer: agents | Feature: FEAT-004

## Goal
Implement curiosity narration: when the backend sends a `curiosity_requested` event, the agent wraps the pre-selected curiosity text into a warm, child-appropriate narration. The agent must NEVER generate its own curiosity — it uses only the text provided by the backend.

## Status
status: closed
started_at: 2026-04-25
closed_at: 2026-04-25
blocked_by:
waiting_for:

## Tasks
- [x] Update contract: add `curiosity_requested` to `event_type` enum → BREAKING → bump `contract_version` v2→v3
- [x] Update contract: document `curiosity` object structure in `event_payload` description (fields: `id`, `text`, `locale`, `phonetic_hint`)
- [x] Update Modelfile SCOPE section: add `curiosity_requested` to handled event types
- [x] Update Modelfile: add Rule 14 — for `curiosity_requested`, use ONLY `event_payload.curiosity.text`; add short warm framing; stay within 300 chars total; do NOT generate own curiosity content
- [x] Update `context/rules.md`: add `curiosity_requested` to Scope section; add Curiosity Narration rules (use only provided text, warm framing, no generation)
- [x] Update `context/examples.md`: add Example 8 — `curiosity_requested` event (age 5, topic: animals, correct output narrates the provided text with warm framing)
- [x] Add fixture `tests/fixtures/event_curiosity_requested.json`
- [x] Update `tests/test_functional.sh`: add Test 8 — `curiosity_requested` (response_type=narration, content_text non-empty, safety_flags empty)
- [x] Rebuild model in Ollama container and run all tests (availability + schema tier 2 + functional tier 3)

## Risks
- Model may ignore `event_payload.curiosity.text` and generate its own curiosity text → Rule 14 must be explicit with a WRONG/CORRECT example showing it must copy+frame, not invent
- Curiosity text from backend + warm framing wrapper may exceed 300 chars → Rule 14 must warn: if curiosity.text is already long, omit the intro framing
- Locale mismatch: backend may send `locale: es-ES` but model frames in a different language → Rule should say: respond in the same language as `curiosity.locale` when known, otherwise Spanish
- Regression: all 7 existing functional tests must continue passing after Modelfile update

## Dependencies
- Sprint 003 and Sprint 004 PRs open (not yet merged to develop) — this sprint can proceed on its own branch from `develop`
- Ollama container running with `education-framework-agent-child` loaded for local validation
- Backend is responsible for curating curiosity catalog and selecting entries (FEAT-004 analysis); agent only narrates
- Backend must update schema validator to contract v3 before production deployment

## Agent Instruction
- New event type `curiosity_requested`: the backend sends a pre-selected curiosity with `event_payload.curiosity.text` (the curiosity content already in child-appropriate language)
- The agent's job is to wrap it warmly, NOT rewrite or invent it. Add a short framing (e.g., "¿Sabías que...?") only if it fits within 300 chars; otherwise use the text directly
- WRONG: generating "¿Sabías que los delfines son inteligentes?" when the input has a different text
- CORRECT: using `event_payload.curiosity.text` and wrapping it with a warm phrase
- `response_type` for `curiosity_requested` → `"narration"` (this is a broadcast to the child, not a prompt for action)
- `suggested_actions` → `["explore_more", "continue_activity"]` (or empty if context does not suggest a next step)
- `tool_calls` → `[]` (backend already selected the curiosity; no tools needed)
- `safety_flags` → `[]` (backend already curated and validated the text before sending)
- Adding `curiosity_requested` to the `event_type` enum is a BREAKING CHANGE per policy → bump contract_version v2→v3
- Do NOT add new required output fields — response schema stays the same (no version bump needed on output)
- Git branch from `develop`: `framework/agents/feat/agent-tell-curiosities`
- To rebuild agent: PowerShell for `docker cp` and `docker exec` (Git Bash translates Linux paths on Windows)

## Notes
- FEAT-004 analysis states the backend selects and validates curiosities (age_min/age_max filtering, parental consent gating, rate limiting) before sending to the agent — the agent never calls external sources
- `phonetic_hint` in the curiosity object is optional and intended for TTS backends; the agent may include it in a `tool_call` note if `tts_enabled: true` in consent_flags, but this is optional
- Curiosity text is expected to be 1–2 short sentences (backend catalog spec); 300-char limit should not be an issue for most entries
- `truncated_context_id` mentioned in FEAT-004 analysis maps to existing `truncated_context` field in input — no contract change needed for that field

## Manual Testing (Insomnia)

### Configuración
| Campo   | Valor                                 |
|---------|---------------------------------------|
| Method  | `POST`                                |
| URL     | `http://localhost:11435/api/generate` |
| Headers | `Content-Type: application/json`      |

### Caso 1 — `curiosity_requested`, age 5, tema: animales

```json
{
  "model": "education-framework-agent-child",
  "stream": false,
  "prompt": "{\"version\":\"v1\",\"event_type\":\"curiosity_requested\",\"event_payload\":{\"curiosity\":{\"id\":\"c001\",\"text\":\"Los perros pueden oler hasta 100.000 veces mejor que los humanos.\",\"locale\":\"es-ES\"}},\"child_profile\":{\"id\":\"child-test-001\",\"age\":5,\"consent_flags\":{\"tts_enabled\":true,\"parent_notifications_enabled\":false}},\"request_id\":\"req-cur-001\",\"timestamp\":\"2026-04-25T12:00:00Z\"}"
}
```

**Verificar:** `response_type` = `"narration"`, `content_text` contiene la curiosidad de los perros (no texto inventado), `tone` = `"joyful"` (age 5 default), `safety_flags` = `[]`

---

### Caso 2 — `curiosity_requested`, age 7, tema: espacio

```json
{
  "model": "education-framework-agent-child",
  "stream": false,
  "prompt": "{\"version\":\"v1\",\"event_type\":\"curiosity_requested\",\"event_payload\":{\"curiosity\":{\"id\":\"c042\",\"text\":\"El sol es tan grande que dentro de él cabrían más de un millón de planetas Tierra.\",\"locale\":\"es-ES\"}},\"child_profile\":{\"id\":\"child-test-002\",\"age\":7,\"consent_flags\":{\"tts_enabled\":true,\"parent_notifications_enabled\":false}},\"request_id\":\"req-cur-002\",\"timestamp\":\"2026-04-25T12:05:00Z\"}"
}
```

**Verificar:** `response_type` = `"narration"`, `content_text` menciona el sol (usa el texto provisto, no inventa), `tone` = `"enthusiastic"` (age 7 default), `safety_flags` = `[]`

---

### Caso 3 — `curiosity_requested`, age 4, `preferred_tone: calm`

```json
{
  "model": "education-framework-agent-child",
  "stream": false,
  "prompt": "{\"version\":\"v1\",\"event_type\":\"curiosity_requested\",\"event_payload\":{\"curiosity\":{\"id\":\"c010\",\"text\":\"Las mariposas saborean con sus patas.\",\"locale\":\"es-ES\"}},\"child_profile\":{\"id\":\"child-test-003\",\"age\":4,\"consent_flags\":{\"tts_enabled\":true,\"parent_notifications_enabled\":false},\"preferred_tone\":\"calm\"},\"request_id\":\"req-cur-003\",\"timestamp\":\"2026-04-25T12:10:00Z\"}"
}
```

**Verificar:** `response_type` = `"narration"`, `tone` = `"calm"` (preferred_tone respected), `content_text` incluye las mariposas (texto provisto), `safety_flags` = `[]`

## Review

completed_tasks:
    - Contract bumped v2→v3 (breaking): curiosity_requested added to event_type enum; curiosity object documented in event_payload description
    - Modelfile Rule 14: narrate ONLY event_payload.curiosity.text; WRONG/CORRECT examples prevent content generation; locale-aware language fallback
    - rules.md: curiosity_requested added to Scope; Curiosity Narration Rules section with use-only-provided-text constraint
    - examples.md: Example 8 (curiosity_requested, age 5, topic perros) with full input/output pair
    - Fixture event_curiosity_requested.json (age 5, id c001, locale es-ES)
    - test_functional.sh Test 8: response_type=narration, topic keyword referenced in content_text, safety_flags=[], tool_calls=[]
    - All tests passed: availability OK, schema 13/13, functional 8/8
    - PR opened: framework/agents/feat/agent-tell-curiosities → develop

incomplete_tasks:
    - none

contract_changes:
    - docs/contracts/agents/education-framework-agent-child.json: contract_version v2→v3 (breaking); curiosity_requested added to event_type enum; event_payload description updated with curiosity object schema

learnings:
    - WRONG/CORRECT examples in Rule 14 anchored the model's behavior on first load — no iteration needed; pattern confirmed effective across Sprint 003, 004, and 005
    - content_text keyword check (grep for topic word) is a lightweight but effective functional assertion for narration fidelity
    - Backend responsibility boundary is clear: agent only narrates, never selects or validates curiosity content — keeps the Modelfile rule simple and auditable

next_sprint_suggestions:
    - Sprint 006 (agents): implement adult/parent-facing agent Modelfile (educator persona, different scope and tone)
    - Sprint 006 (backend): update schema validator to contract v3; implement curiosity_requested event routing to agent; build curiosity catalog API
    - Sprint 006 (agents): add phonetic_hint passthrough to tool_calls note when tts_enabled is true (FEAT-004 optional improvement)
