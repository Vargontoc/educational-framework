# ADR-002 — Agent: education-framework-agent-child ("Nubi")
# ─────────────────────────────────────────────

## Status
status: proposed
date: 2026-04-23
superseded_by:

## Context
The product requires a deterministic, event-driven agent that produces short, age-appropriate responses for children (ages 3–8). This agent will be invoked by backend event processors (not by end-users directly) and must never behave like an open-ended chatbot. Key constraints include strict safety, privacy (minimal PII exposure), predictable output format (JSON), and low-latency local operation. The backend is responsible for memory/context management, consent flags, PII redaction, MCP tools, and audit logging.

## Decision
Create a dedicated event-driven agent named `education-framework-agent-child` (alias "Nubi"). The agent will:
- Be invoked with a structured event payload and an explicit, backend-provided `truncated_context` (no direct long-term memory inside the agent).
- Produce a validated JSON response following a fixed schema (versioned): { version, response_type, content_text, content_type, suggested_actions[], safety_flags[], tool_calls[] }.
- Use a compact, instruction-tuned local model (run via Ollama) with deterministic parameters (temperature 0.1–0.25, top_p 0.9, num_ctx >= 4096).
- Expose a minimal set of MCP tools that are strictly typed and non-destructive (e.g., `fetch_child_profile`, `fetch_recommended_activity`, `record_event` (append-only), `send_parent_notification`).
- Refuse requests outside scope (medical/diagnostic/legal advice) and always include parental/actionable guidance when appropriate.
- Announce any tool calls it intends to make and include tool results in its structured response.

The backend will enforce JSON schema validation, PII redaction, consent checks, and any required truncation or summarization of history before sending data to the agent.

## Consequences
positive:
  - Predictable, testable outputs suitable for TTS and UI rendering.
  - Safer interactions for children because of constrained scope and schema validation.
  - Lower risk of hallucination by using deterministic settings and a narrow scope.

negative:
  - More development work to define and maintain strict schemas and tool contracts.
  - Agents may require frequent tuning for edge cases in child communication.

neutral:
  - Backend takes on more responsibility (memory, redaction, audit), which centralizes compliance but increases backend complexity.

## Alternatives considered
alternative: Single unified "family" agent (one agent for children and adults)
reason_rejected: Mixing event-driven, deterministic child responses with open-ended adult chat increases accidental disclosure risk, complicates response formats, and makes safety testing harder.

alternative: Fully backend templating (no LLM for child responses)
reason_rejected: Templates are simple and safe but cannot provide the contextualized, varied pedagogical responses desired for a richer user experience.

alternative: Remote cloud LLM instead of local Ollama
reason_rejected: Privacy, latency, and cost concerns make remote models less suitable for child-facing interactions containing sensitive data.

## References
ADR-001, Ollama deployment notes, docs/contracts/agents (planned)
