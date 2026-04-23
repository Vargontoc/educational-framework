# Sprint 001 - agents
# Layer: agents | Archived: 2026-04-23

## Goal
Design and draft agent contracts and context for child (`education-framework-agent-child`) and adult (`education-framework-agent-adult`).

## Status
status: completed
started_at: 2026-04-21 09:00:00
closed_at: 2026-04-23 00:00:00
blocked_by:
waiting_for:

## Tasks
- [x] Draft design proposals for both agents (child and adult) in sprint Notes
- [ ] Draft contract JSONs for both agents (`docs/contracts/agents/...json`)
- [ ] Write `context/rules.md` and `context/examples.md` (>=3 each) in `framework/agents/{agent-name}/context/`
- [ ] Human review & approve before Modelfile creation

## Risks
- PII leakage if backend fails to sanitize inputs or history
- Prompt-injection via event payloads or crafted chat messages
- Scope creep into clinical/legal advice (must refuse)

## Dependencies
- Backend: memory/context API, MCP Tools, PII redaction, and audit logging services
- Docs: `docs/contracts/` must accept new agent contract files

## Agent Instruction
- Follow `framework/agents/skills/design/SKILL.md`. Stop and request human approval before creating any Modelfile or publishing contract changes.

## Notes
### Proposal: education-framework-agent-child ("Nubi")
name: education-framework-agent-child
purpose: Event-driven responder that converts structured event input into a strict, age-appropriate JSON response for child-facing interactions. Not a chatbot; deterministic and constrained.
consumed_by: backend/event-processor service (Ollama local runtime)
input: event object { event_type, event_payload, child_profile {id, age, consent_flags}, request_id, timestamp, truncated_context }
output: JSON response { version, response_type (text|activity|refusal), content_text, content_type (tts|visual|mixed), suggested_actions[], actions_meta[], safety_flags, tool_calls[] }
tools_needed: [fetch_child_profile, fetch_recommended_activity, record_event, send_parent_notification] — minimal, strictly typed, append-only where applicable
scope:
  - Produce short, age-appropriate messages and activity suggestions triggered by events
  - Follow strict response format and length constraints
  - Announce any tool calls it intends to make and include results in response
out_of_scope:
  - No long-form conversational chat
  - No medical/legal diagnosis or professional advice
  - No disclosure of PII beyond allowed profile identifiers
risks:
  - Hallucinated facts about child abilities or safety
  - Prompt-injection via event payloads
  - Overly-prescriptive advice that should be handled by caregivers
contract_impact: Requires `docs/contracts/agents/education-framework-agent-child.json` (input/output schemas and tool definitions)

Notes:
- System prompt must begin with a concise role sentence and an explicit RULES block.
- Temperature should be low (0.1–0.25). Backend enforces schema validation and PII redaction.

---

### Proposal: education-framework-agent-adult
name: education-framework-agent-adult
purpose: Conversational assistant for parents/adults to query child progress, retrieve summaries, and ask general questions related to child activities and development. Provides broad guidance and consistently recommends professional help when appropriate.
consumed_by: backend/chat-service (mediated chat history and child summaries)
input: chat message { message_id, parent_id, message_text, child_ids[], context_summary, request_id, timestamp }
output: chat response { message_id, reply_text, disclaimer, resources[], suggested_next_steps[], referral_flag }
tools_needed: [fetch_child_summary, list_local_professionals, create_referral_request, fetch_activity_history]
scope:
  - Answer questions about child progress and activities using available summaries
  - Provide educational suggestions, resources, and non-diagnostic guidance
  - Always include a disclaimer when topics touch clinical/diagnostic areas
out_of_scope:
  - Providing clinical diagnoses, legal advice, or substitution for professional services
  - Returning raw PII beyond permitted identifiers
risks:
  - Overstepping into clinical advice territory
  - Revealing sensitive data through verbatim history if backend fails to sanitize
contract_impact: Requires `docs/contracts/agents/education-framework-agent-adult.json` (chat IO, tool list, versioning)

Notes:
- Use a capable instruction model with moderate temperature (0.25–0.6).
- Backend holds responsibility for session memory, consent, PII redaction, and audit logs.

## Review

completed_tasks:
    - Design proposal for education-framework-agent-child ("Nubi") written and approved (see Notes)
    - Design proposal for education-framework-agent-adult written and approved (see Notes)
    - ADR-002 created documenting the child agent architecture decision

incomplete_tasks:
    - Contract JSON files not created (docs/contracts/agents/*.json) — deferred to sprint 002
    - context/rules.md and context/examples.md not created — deferred to sprint 002

contract_changes:
    - None (design-only sprint)

learnings:
    - Agent proposals were refined into ADR-002, which serves as the architecture reference for sprint 002
    - Child agent must be deterministic and event-driven, not conversational — this distinction is critical for safety

next_sprint_suggestions:
    - Sprint 002: implement FEAT-001 — build Modelfile, finalize schemas, validate against test events
