# ADR-003 — Agent: education-framework-agent-adult
# ─────────────────────────────────────────────

## Status
status: proposed
date: 2026-04-23
superseded_by:

## Context
Parents and caregivers need a conversational interface to query child progress, retrieve summaries, and ask general (non-clinical) questions. This interface must balance helpfulness with safety: it should never provide clinical diagnoses or legal advice, must avoid revealing identifying PII, and should consistently recommend professional help when topics approach clinical scope. Backend services will mediate history, consent, and PII redaction before providing condensed context to the agent.

## Decision
Create a dedicated conversational agent named `education-framework-agent-adult`. The agent will:
- Operate as a chat-style assistant consumed by the backend chat-service.
- Receive sanitized `context_summary` and message payloads; the backend will manage full history, consent, and PII redaction.
- Return structured chat responses including: { message_id, reply_text, disclaimer, resources[], suggested_next_steps[], referral_flag }.
- Use a capable instruction model (locally via Ollama where feasible or upgraded model) with moderate temperature (0.25–0.6) to balance helpfulness and creativity.
- Offer optional MCP tools for permitted operations: `fetch_child_summary`, `list_local_professionals`, `fetch_activity_history`, and `create_referral_request` (the latter gated by backend approvals/workflow).
- Always include a clear disclaimer when responses touch on clinical or diagnostic topics and set `referral_flag` when professional referral is indicated.

## Consequences
positive:
  - Parents receive helpful, contextualized information and actionable next steps.
  - Clear separation of roles reduces risk compared to a single-agent design.

negative:
  - Risk of providing overly confident medical-like language; strict rules and tests are required to catch drift.
  - Additional backend responsibilities to ensure proper sanitization and gating for referral operations.

neutral:
  - Potential need to scale model capability depending on user expectations; may require resource planning.

## Alternatives considered
alternative: Allow adults to use the child agent for queries
reason_rejected: The child agent is event-driven and constrained; it lacks the conversational format and broader knowledge necessary for adult queries.

alternative: Use a hosted cloud LLM for adult agent
reason_rejected: While more powerful, cloud LLMs raise privacy/PII and cost concerns; prefer local models where feasible and allow upgrade path if needed.

alternative: Read-only API (no agent) that returns raw stored events
reason_rejected: Raw events are not user-friendly; a conversational agent provides summarization and actionable guidance while backend enforces privacy.

## References
ADR-001, docs/contracts/agents (planned)
