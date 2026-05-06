## Layer Context
layer: frontend
stack: Vue 3 + TypeScript + Vite + Pinia + Axios + Vue Router
structure: Standard Vue 3 - reorganise by domain only if packages grow large
does_not_own: backend logic, database JWT issuance, Docker Compose

## Contract dependency
source_of_truth: [docs/contracts/api/openapi.json, docs/contracts/api/websocket.json]
rule: Never hardcode API URLs or response shapes - derive them from openapi.json
rule: If openapi.json does not exist yet, set sprint status to blocked

## Authentication Strategy

method: opaque Bearer token — issued by backend on POST /api/v1/auth/login, returned in response body as LoginResponse.token
storage: in-memory only — stored as a non-persisted ref inside useSessionStore; never written to localStorage, sessionStorage, or any persisted Pinia slice
transport: Axios request interceptor adds `Authorization: Bearer <token>` header on every request when token is present in store
on_401: interceptor clears useSessionStore and calls router.replace('/') — forces re-login
on_refresh: token is lost — user must log in again (by design; see ADR-010 section 3.3)
localstorage: never used for auth — not even as fallback
note: withCredentials is not required for Bearer token auth — omit unless a cookie-based mechanism is added later

## HTTP Client

tool: Axios with a shared instance in src/shared/api/axios.ts
rule: All API calls go through the shared Axios instance
rule: Use request interceptor to handle auth headers if needed

## State Management

tool: Pinia
rule: One store per feature
rule: Stores must not call Axios directly - use services in src/services/

## Routing

tool: Vue Router
rule: Protected routes use a navigation guard that checks auth store

## Skils Available

coding: ./skills/coding/SKILL.md
testing: ./skills/testing/SKILL.md
refactor: ./skills/refactor/SKILL.md
design: ./skills/design/SKILL.md

## Sprint Context

current_sprint: ./sprints/current.md

## Workflow

1. Read root AGENT.md for global context
2. Read this file for frontend context
3. Check ./sprints/current.md — if blocked, stop and report
4. Verify docs/contracts/openapi.json exists before writing any API call
5. Identify task type (coding, testing, refactor, design)
6. Load the matching skill from ./skills/
7. Follow Agent Instructions in current.md for this sprint
8. Never store JWT in localStorage or Pinia
9. Commit following: frontend/type/short-description

## Agent Compatibility
# Plain structured natural language.
# Compatible with: Claude Code, Gemini, and local models.