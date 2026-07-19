# ADR-007 — Backend Stack: Spring Boot + Spring AI
# ─────────────────────────────────────────────

## Status

status:        accepted
date:          2026-04-30
superseded_by: —

## Context

The application requires a robust, scalable, and maintainable backend layer capable of supporting the child agent features, the TTS service, and database interaction. Adopting a modular monolithic architecture maintains code simplicity, eases development and integration of new features, and guarantees a coherent runtime in a single-user private environment.

Java 21 is chosen as the primary language for its support of modern features such as pattern matching, switch expressions, and concurrency improvements, which facilitate writing clean and efficient code.

Spring Boot is selected for its ability to create enterprise applications with minimal configuration, straightforward database integration, and microservices support — although in this case it will serve as the foundation for a modular monolith.

Spring AI is adopted to simplify integration with AI models such as Ollama and to enable efficient management of the child agent logic while maintaining consistency and scalability.

## Decision

The backend will be implemented using **Spring Boot** as the primary framework, **Java 21** as the programming language, and **Spring AI** for artificial intelligence model integration. The application will be a **modular monolith**, structured under the `es.vargontoc.educational.framework` root package, with well-defined modules separated by functional domain.

Alternative frameworks and distributed architectures will not be considered, as they are not required for this project context.

## Consequences

positive:
  - Higher development productivity thanks to Spring Boot and its integrated tooling.
  - Improved code readability and maintainability through modularity.
  - Straightforward AI model integration via Spring AI.
  - Java 21 provides modern language improvements and concurrency enhancements.
  - Consistent and scalable architecture for a private single-user environment.

negative:
  - Higher dependency and configuration management complexity compared to simpler alternatives.
  - Distributed architecture advantages are not fully leveraged, though they are unnecessary for this project context.
  - Initial effort required to structure the application into well-defined modules.

neutral:
  - Spring AI adoption does not directly affect TTS logic but simplifies future AI model integration.

## Alternatives considered

alternative: Use a different framework (Quarkus or Micronaut)
reason_rejected: Spring Boot offers a lower learning curve and better compatibility with the existing Java ecosystem tooling.

alternative: Use Java 17 or 20
reason_rejected: Java 21 delivers significant language and concurrency improvements relevant to this project.

alternative: Use a distributed architecture
reason_rejected: Not required for a private single-user project; would increase complexity unnecessarily.

alternative: Use Spring AI in development environment only
reason_rejected: Not necessary in development, as the local model can be used directly without Spring AI integration.

## References
- ADR-004: TTS Service — Coqui TTS on-premise
- ADR-005: Cloudflare Deployment for Production
- FEAT-001: 300-char limit on `content_text`
- FEAT-002: `agent_name` sanitization
- FEAT-003: Prosody presets by age
- FEAT-004, FEAT-005, FEAT-006: Finite and curated catalog content