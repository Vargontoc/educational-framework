# Sprint 003 - frontend
# -----------------------------------------------

## Goal
Establish the base visual design system: CSS variables, global styles, and core UI components (Button, Card, Badge) with a dev-only demo route. Derived from FEAT-001.

## Status
status: completed
started_at: 2026-05-06 00:00:00
closed_at: 2026-05-09 00:00:00
blocked_by:
waiting_for:

## Tasks

### Design Tokens
- [x] Create `src/styles/variables.css` — CSS custom properties:
      colors: --color-primary, --color-primary-dark, --color-secondary, --color-neutral;
      typography: --font-size-sm (14px), --font-size-md (18px), --font-size-lg (24px);
      spacing: --space-sm (8px), --space-md (16px), --space-lg (32px);
      radii: --radius-sm (8px), --radius-md (16px), --radius-lg (32px).
- [x] Create `src/styles/global.css` — base reset, body font, box-sizing rule, and prefers-reduced-motion override.
- [x] Import both files in `src/main.ts` before app mount.

### UI Components
- [x] Create `src/components/ui/Button.vue` — props: `variant` (primary | secondary), `disabled`; min touch target 44x44px; WCAG contrast >= 4.5:1 for text.
- [x] Create `src/components/ui/Card.vue` — props: `variant` (primary | secondary), `disabled`; uses CSS variables for colors and radii.
- [x] Create `src/components/ui/Badge.vue` — props: `variant` (primary | secondary), `disabled`; inline/compact display; uses CSS variables.

### Mock Data
- [x] Create `src/mock/index.ts` — export sample data arrays used only by DesignSystemView; no network calls; no business logic.

### Dev-Only Demo Route
- [x] Add route to `src/router/index.ts`:
      path: '/design-system', name: 'design-system',
      component: lazy import of DesignSystemView,
      guarded by `beforeEnter: () => import.meta.env.DEV || '/'`.
- [x] Create `src/views/DesignSystemView.vue` — renders Button, Card and Badge in all states (primary, secondary, disabled) using mock data from `src/mock/index.ts`; no real API calls.

### Accessibility Checklist
- [x] Verify color contrast >= 4.5:1 for all text on component backgrounds (browser DevTools or axe).
- [x] Verify all interactive elements have touch target >= 44x44px.
- [x] Confirm `prefers-reduced-motion` override is present in global.css and disables transitions.

## Risks
- **Demo route exposed in production**: guarded by `import.meta.env.DEV` in beforeEnter — Vite strips the dead branch at build time.
- **Contrast failures**: palette chosen at dev time may not meet WCAG 4.5:1 under actual renders — validate before closing sprint.
- **Tap target regressions on RotationOverlay**: existing component is not part of this sprint scope — do not modify it.

## Dependencies
- No backend contract required — this sprint is frontend-only.
- `src/main.ts` — add CSS imports without altering existing store/router bootstrap order.

## Agent Instruction
- Demo route (`/design-system`) must be unreachable in production: use `beforeEnter` guard checking `import.meta.env.DEV`, not a comment or flag.
- All demo data lives in `src/mock/index.ts` — never import from a view or store directly.
- Components go in `src/components/ui/` — do NOT place them in `src/components/` root (reserved for layout-level components like RotationOverlay).
- Use only CSS custom properties defined in `variables.css` — no hardcoded hex values inside component `<style>` blocks.
- Do NOT modify HomeView, GameView, PanelControlView, or RotationOverlay.
- Do NOT add any backend or Axios calls.
- Target audience: children aged 3–8 — colors must be vivid, font sizes large, interactive surfaces generously sized.
- Commit: `feat(frontend): add base design system tokens and UI components`

## Notes
Derived from docs/product/features/frontend/FEAT-001-Base-Styles.md.
Scope: CSS variables + Button/Card/Badge components + dev-only design system demo. No backend integration.

## Review

completed_tasks:
    - src/styles/variables.css — CSS custom properties (colors, typography, spacing, radii, touch target min).
    - src/styles/global.css — base reset, prefers-reduced-motion, body font.
    - src/main.ts — both CSS files imported before app mount.
    - src/components/ui/Button.vue — primary/secondary/disabled variants; 44px touch target; WCAG-compliant contrast.
    - src/components/ui/Card.vue — primary/secondary/disabled variants; accent border; hover animation.
    - src/components/ui/Badge.vue — primary/secondary/disabled variants; inline chip display.
    - src/mock/index.ts — mockCards and mockBadges arrays; no network calls.
    - src/views/DesignSystemView.vue — color palette, typography, and all component states rendered.
    - src/router/index.ts — /design-system route added with beforeEnter DEV guard.

incomplete_tasks:
    None. All sprint tasks completed.

contract_changes:
    None. This sprint did not touch docs/contracts/.

learnings:
    - --color-secondary (#F59E0B amber) requires dark text (--color-text-on-secondary: #1f2937) to meet
      WCAG 4.5:1; white text on amber falls below threshold.
    - Mock data module (src/mock/) should remain isolated from stores and services to avoid
      accidental use in production paths.
    - beforeEnter guard returning '/' correctly redirects to home in production builds;
      Vite tree-shakes the import.meta.env.DEV branch at build time.

next_sprint_suggestions:
    - Sprint 004: FEAT-002 Home View — family state detection, family registration modal,
      parental PIN authentication, child selector, child session opening wired to openapi.json.
    - Resolve product decisions (P-1 to P-4) before starting Sprint 004 implementation.
