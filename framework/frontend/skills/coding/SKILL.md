## Name
Coding

## Before Writing Any API Call
1. Verify docs/contracts/openapi.json exists
2. Derive request/response types from openapi.json
3. Place TypeScript interfaces in src/shared/types/

## Component Conventions
- Use Composition API with <script setup lang="ts"> — never Options API
- One component per file, PascalCase naming
- Components in src/components/ are reusable — no direct API calls inside them
- Views in src/views/ are page-level — they orchestrate components and call stores

## Service Conventions
- All API calls go through the shared Axios instance (src/shared/api/axios.ts)
- Services return typed promises — never return raw AxiosResponse
- One service file per domain
- Services do not access Pinia stores directly

## Store Conventions
- Use defineStore with Composition API style (not Options style)
- Stores call services — never call Axios directly
- Auth store holds: user info, isAuthenticated flag — never the JWT token
- Persist only what is safe to persist (never tokens)

## Auth Conventions
- Axios instance must have withCredentials: true globally
- Never store JWT in localStorage, sessionStorage or Pinia
- On 401 response, interceptor redirects to /login automatically

## After Coding
- Check frontend/sprints/current.md and mark completed tasks
- If a new route was added, update router/index.ts navigation guard