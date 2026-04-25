# Sprint 006 - agents
# Layer: agents | Feature: FEAT-005

## Goal
Implement character muletilla injection (Option A — strict narration): when the backend injects an optional `muletilla` object into an existing event's payload, the agent naturally weaves the curated muletilla text into its response. The agent must NEVER generate its own muletillas — only use the text provided by the backend. Option B (paraphrase mode) is deferred to a future sprint.

## Status
status: pending
started_at:
closed_at:
blocked_by:
waiting_for: Sprint 003 and Sprint 004 PRs merged to develop

## Tasks
- [ ] Update contract: document `muletilla` optional object structure in `event_payload` description (non-breaking — event_payload already additionalProperties: true; no version bump)
- [ ] Update Modelfile: add Rule 15 — when `event_payload.muletilla.text` is present, incorporate it naturally at the start or end of content_text; NEVER generate own muletilla; keep total ≤ 300 chars
- [ ] Update `context/rules.md`: add Muletilla Injection Rules section (use only provided text, natural placement, no generation)
- [ ] Update `context/examples.md`: add Example 9 — `activity_completed` with `muletilla` field; output weaves muletilla naturally into content_text
- [ ] Add fixture `tests/fixtures/event_activity_completed_with_muletilla.json`
- [ ] Update `tests/test_functional.sh`: add Test 9 — muletilla present → content_text contains muletilla keyword; response_type narration or prompt; safety_flags empty
- [ ] Rebuild model in Ollama container and run all tests (availability + schema tier 2 + functional tier 3)

## Risks
- Model may ignore `event_payload.muletilla.text` entirely when other event fields are richer → Rule 15 must be explicit: if muletilla is present it MUST appear in content_text
- Muletilla text + normal response may exceed 300 chars → Rule 15 must instruct: if combined text would exceed limit, shorten the narrative part, never truncate the muletilla
- Model may creatively rewrite the muletilla text instead of using it verbatim → WRONG/CORRECT examples required
- Regression: all 8 existing functional tests must continue passing after Modelfile update

## Dependencies
- Sprint 003, 004, 005 PRs available in develop (can proceed from current develop)
- Ollama container running with `education-framework-agent-child` loaded for local validation
- Backend is responsible for character catalog management, rate limiting, parental controls, and muletilla selection (FEAT-005 analysis); agent only incorporates provided text
- Option B (allow_paraphrase) explicitly deferred — backend validation infrastructure (embeddings + NER) not yet built; FEAT-005 recommendation is to start with allow_paraphrase=false

## Agent Instruction
- Muletillas are injected as an optional `muletilla` object inside `event_payload` of any existing event type (activity_completed, activity_started, help_requested, etc.)
- Input structure: `event_payload.muletilla = { id: string, text: string }` — backend has already curated and rate-limited the selection
- When present: agent MUST include `muletilla.text` verbatim (or near-verbatim) in content_text — natural placement at the start preferred (e.g. "¡Canasta! Lo hiciste genial. ¿Quieres jugar otra vez?")
- When absent: agent responds normally without any muletilla
- WRONG: inventing "¡Súper! Lo hiciste fenomenal" when the input muletilla says "¡Canasta!"
- CORRECT: "¡Canasta! Lo hiciste genial. ¿Quieres intentarlo otra vez?"
- 300-char limit applies to the full content_text (muletilla + narrative); if needed, shorten the narrative, never cut the muletilla
- No new event_type needed → contract_version stays at v3 (non-breaking addition)
- No new output fields needed → output schema unchanged
- Option B fields (`allow_paraphrase`, `paraphrase_constraints`, `paraphrase_quality_score`) are out of scope for this sprint
- Git branch from `develop`: `framework/agents/feat/agent-character-muletillas`
- To rebuild agent: PowerShell for `docker cp` and `docker exec` (Git Bash translates Linux paths on Windows)

## Notes
- FEAT-005 explicitly recommends starting with `allow_paraphrase=false` and Option A before enabling paraphrase in staging
- `character_id`, `display_name`, `tags`, `context_tags`, `frequency` fields are backend-only concerns for this sprint — agent does not receive or use them, only `muletilla.text`
- `safety_flags` on the muletilla object (FEAT-005 schema) are a backend pre-filter; if a muletilla reaches the agent it is considered safe (same contract boundary as curiosity_requested)
- Muletilla placement rule: beginning of content_text is preferred because it mimics how a character would exclaim before commenting; allow end if event context flows better (e.g. help_requested: "¿Lo intentamos juntos? ¡Canasta!")

## Manual Testing (Insomnia)

### Configuración
| Campo   | Valor                                 |
|---------|---------------------------------------|
| Method  | `POST`                                |
| URL     | `http://localhost:11435/api/generate` |
| Headers | `Content-Type: application/json`      |

### Caso 1 — `activity_completed` con muletilla "¡Canasta!"

```json
{
  "model": "education-framework-agent-child",
  "stream": false,
  "prompt": "{\"version\":\"v1\",\"event_type\":\"activity_completed\",\"event_payload\":{\"activity_id\":\"shapes-match-001\",\"score\":3,\"max_score\":3,\"muletilla\":{\"id\":\"m001\",\"text\":\"¡Canasta!\"}},\"child_profile\":{\"id\":\"child-test-001\",\"age\":6,\"consent_flags\":{\"tts_enabled\":true,\"parent_notifications_enabled\":false}},\"request_id\":\"req-mul-001\",\"timestamp\":\"2026-04-25T13:00:00Z\"}"
}
```

**Verificar:** `content_text` contiene "Canasta" (muletilla usada, no inventada), `response_type` = `"narration"`, `tone` = `"joyful"` (age 6 default), `safety_flags` = `[]`

---

### Caso 2 — `help_requested` con muletilla al final

```json
{
  "model": "education-framework-agent-child",
  "stream": false,
  "prompt": "{\"version\":\"v1\",\"event_type\":\"help_requested\",\"event_payload\":{\"activity_id\":\"colors-paint-003\",\"step\":2,\"muletilla\":{\"id\":\"m002\",\"text\":\"¡Tú puedes!\"}},\"child_profile\":{\"id\":\"child-test-002\",\"age\":4,\"consent_flags\":{\"tts_enabled\":true,\"parent_notifications_enabled\":false}},\"request_id\":\"req-mul-002\",\"timestamp\":\"2026-04-25T13:05:00Z\"}"
}
```

**Verificar:** `content_text` contiene "puedes" (muletilla incorporada), `response_type` = `"prompt"` o `"narration"`, `tone` = `"calm"` (age 4 default), `safety_flags` = `[]`

---

### Caso 3 — `activity_completed` SIN muletilla (regression)

```json
{
  "model": "education-framework-agent-child",
  "stream": false,
  "prompt": "{\"version\":\"v1\",\"event_type\":\"activity_completed\",\"event_payload\":{\"activity_id\":\"shapes-match-001\",\"score\":2,\"max_score\":3},\"child_profile\":{\"id\":\"child-test-001\",\"age\":6,\"consent_flags\":{\"tts_enabled\":true,\"parent_notifications_enabled\":false}},\"request_id\":\"req-mul-003\",\"timestamp\":\"2026-04-25T13:10:00Z\"}"
}
```

**Verificar:** respuesta normal sin muletilla forzada, `response_type` = `"narration"` o `"prompt"`, `safety_flags` = `[]`

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
