# ADR-011 — Development Content Manager

## Status

status:        accepted
date:          2026-05-26
superseded_by: —

---

## 1. Context

The application needs an internal mini-app to manage catalog content during development. This tool
will live inside the existing Vue frontend under `framework/frontend/app`, and will consume the
backend development-only API exposed under `/api/v1/dev/content/**`.

The tool is not part of the production user experience. It exists only to accelerate development,
validation, seeding, and manual inspection of content entities such as categories, topics,
activities, difficulty levels, resources, locales, curiosities, and avatar event catalog entries.

Frontend assets may be shared between development and production deployments, so production safety
must not depend on assets being physically absent from the bundle.

---

## 2. Decision Summary

| Decision | Resolution |
|---|---|
| **Mini-app location** | Implement inside the existing frontend app under `framework/frontend/app`. |
| **Route** | Use a dedicated development route, for example `/dev/content`. |
| **Default availability** | Disabled by default. The frontend must require explicit configuration to enable it. |
| **Frontend flag** | Use an environment flag such as `VITE_ENABLE_DEV_CONTENT=true`. Missing or any other value means disabled. |
| **Security model** | No user-level security restrictions inside the mini-app. It is a development tool only. |
| **Production protection** | Production must run with the frontend flag disabled and backend dev endpoints unavailable. |
| **Backend API** | Consume only `/api/v1/dev/content/**`, already marked as dev-only in `openapi.json`. |
| **Contracts** | Types and request/response shapes must be derived from `docs/contracts/api/openapi.json`. |
| **Assets** | Shared assets between development and production are acceptable. No sensitive or privileged seed data may be embedded in frontend assets. |

---

## 3. Decision Detail

### 3.1 Development-only activation

The Content Manager will be disabled by default.

The frontend route and visible navigation entry are enabled only when:

```ts
import.meta.env.VITE_ENABLE_DEV_CONTENT === 'true'
```

This explicit opt-in prevents accidental exposure when frontend assets are reused between
environments.

### 3.2 Backend profile separation

The backend remains the authoritative boundary.

In the development profile, the backend exposes:

```text
/api/v1/dev/content/**
```

In production, these endpoints are not available.

The frontend does not implement additional authentication or authorization for this mini-app because
environment/profile separation is the intended control.

### 3.3 Route behaviour

When disabled, `/dev/content` must not be reachable through normal navigation.

A direct URL access should redirect to `/` or show a simple unavailable state.

When enabled, the mini-app may operate without parental PIN/session restrictions because it is a
development-only operational tool.

### 3.4 Functional scope

The first version manages existing dev-content entities from the OpenAPI contract:

- Categories
- Topics
- Activities
- Difficulty levels
- Activity resources
- Locales
- Curiosities
- Avatar event catalog entries

The first version should support:

- Listing
- Creating
- Editing
- Parent-child filtering
- Inline API validation errors
- Empty states

Delete operations are out of scope unless explicitly supported by the backend contract.

---

## 4. Consequences and Implications

### Positive

- Faster development and validation of educational content.
- No need to manually seed or edit data outside the application.
- Keeps content management close to the frontend experience being validated.
- Default-disabled flag reduces accidental exposure risk.
- Backend profile remains the final production boundary.

### Negative

- Frontend production bundles may still contain the code if assets are shared.
- The mini-app depends on correct environment configuration.
- No user-level security means it must never be enabled against production data.

### Neutral

- The feature is operational tooling, not part of the product UX.
- Production safety is achieved through deployment/profile configuration, not through component-level
  access control.

---

## 5. Alternatives Considered and Discarded

| Alternative | Reason for discarding |
|---|---|
| **Separate frontend application** | Adds build, deployment, routing, and maintenance overhead for a small internal tool. |
| **Always register the route and rely only on backend 404** | Backend would still protect production, but the UI could appear accidentally. Default-disabled frontend configuration gives clearer intent. |
| **Require parental PIN/session** | Not needed for a development-only tool and would slow development workflows. |
| **Remove the code from production bundles entirely** | Harder to guarantee if assets are shared; explicit disabled-by-default configuration is simpler and sufficient with backend profile separation. |
| **Manage content only through database scripts** | Slower feedback loop, higher error risk, and poor validation of real API contracts. |

---

## 6. Risks and Mitigations

- Risk: The frontend flag is accidentally enabled in production.
  Mitigation: Default value is always disabled. Production environment files must not define
  `VITE_ENABLE_DEV_CONTENT=true`.

- Risk: Shared frontend assets include the mini-app code.
  Mitigation: Acceptable because code presence is not the access boundary. Backend production
  profile must not expose `/api/v1/dev/content/**`.

- Risk: The mini-app is used against production data.
  Mitigation: Backend profile configuration must make dev-content endpoints unavailable outside
  development.

- Risk: Frontend types drift from backend API.
  Mitigation: All request and response types must be derived from
  `docs/contracts/api/openapi.json`.

---

## 7. References

- ADR-007: Backend Stack: Spring Boot + Spring AI
- ADR-010: Frontend Layer Architecture
- `docs/contracts/api/openapi.json`
- `framework/frontend/agent.md`
