# Sprint 012 - frontend
# -----------------------------------------------

## Goal
Implement the Docs View Shell from `docs/product/features/frontend/FEAT-006-Docs-View.md`: public `/docs` route, adult-facing documentation layout, local section navigation, placeholder content, and Parent Control Shell link integration without authentication, backend calls, WebSocket connections, or Markdown rendering implementation.

## Status
status: active
started_at: 2026-05-30 00:00:00
closed_at:
blocked_by:
waiting_for:

## Tasks

### Feature And Existing Flow Review
- [ ] Review `docs/product/features/frontend/FEAT-006-Docs-View.md` before editing.
- [ ] Review existing router configuration in `src/router/index.ts`.
- [ ] Review `src/views/PanelControlView.vue` to link Documentation to `/docs` from the Parent Control Shell.
- [ ] Confirm no backend API, service, Axios, WebSocket, or ParentChannel integration is needed.
- [ ] Do not change backend contracts in this sprint.

### Public Docs Route
- [ ] Add route `/docs` with name `docs`.
- [ ] Ensure `/docs` is public and has no parental auth guard.
- [ ] Ensure `/docs` does not require `VITE_ENABLE_DEV_CONTENT`.
- [ ] Ensure `/docs` renders when no family state or auth token exists.
- [ ] Preserve existing unknown-route redirect policy.

### Docs View Shell
- [ ] Create `DocsView.vue` or equivalent route component.
- [ ] Add adult-facing header with documentation title and short description.
- [ ] Add local section navigation/index.
- [ ] Add main content region with placeholder content.
- [ ] Include initial sections: Getting started, Family and profiles, Parent control panel, Family experiences, Privacy and security, Support.
- [ ] Section selection must be local to the Docs view and must not require backend data.
- [ ] Active section state must be visible and not color-only.
- [ ] Add optional link back to Home.
- [ ] Add optional link back to Panel only when an authenticated in-memory parent session exists.

### Parent Control Shell Integration
- [ ] Update the Documentation item in the Parent Control Shell to navigate to `/docs`.
- [ ] Do not render documentation content inside `/panel`.
- [ ] Returning to `/panel` remains protected by the in-memory token guard.
- [ ] Do not add documentation API calls or Markdown loading to the panel.

### Future Markdown Preparation
- [ ] Keep the Docs shell structure easy to replace with static Markdown-driven content later.
- [ ] Do not add Markdown parser dependencies.
- [ ] Do not implement Markdown file loading.
- [ ] Do not implement dynamic slug routing, search, versioning, or generated table of contents.
- [ ] Do not create backend documentation endpoints.

### Responsive Behavior
- [ ] Desktop/tablet landscape can use a two-column layout with section navigation and content.
- [ ] Mobile landscape can stack navigation and content or use a compact horizontal section list.
- [ ] Portrait orientation follows the existing app rotation overlay behavior if global shell applies it.
- [ ] Text remains readable without horizontal scrolling.
- [ ] Adult touch targets are at least 44px.

### i18n And Accessibility
- [ ] Add all visible labels, section titles, placeholder text, and aria labels to `src/i18n/es.ts`.
- [ ] Do not hardcode visible text in Vue templates unless it is approved local static placeholder content.
- [ ] Use semantic heading order.
- [ ] Section navigation is keyboard operable.
- [ ] Active section state does not rely on color only.
- [ ] Links have clear translated labels.
- [ ] Text contrast meets WCAG AA for adult UI.
- [ ] Avoid sustained uppercase visible labels.

### Testing And Verification
- [ ] Add or update component/routing tests if the project has a test harness available for this area.
- [ ] Verify `/docs` renders without authenticated session.
- [ ] Verify `/docs` renders when no family state is loaded.
- [ ] Verify Documentation link from Parent Control Shell points to `/docs`.
- [ ] Verify section navigation changes visible placeholder content locally.
- [ ] Verify no Axios/API call is triggered by opening or navigating inside Docs view.
- [ ] Verify no WebSocket connection is opened by Docs view.
- [ ] Verify all visible strings resolve through i18n keys where applicable.
- [ ] Verify desktop/tablet landscape, mobile landscape, and portrait overlay behavior manually.
- [ ] Run `npm run build` from `framework/frontend/app`.

## Risks
- **Markdown scope creep**: the shell may add Markdown rendering too early.
  Mitigation: document Markdown as future preparation only; do not add parser dependencies in this sprint.
- **Accidental authentication guard**: `/docs` may be protected like `/panel`.
  Mitigation: keep `/docs` outside the panel guard and verify direct access without token.
- **Backend/API creep**: docs may start calling backend endpoints for content.
  Mitigation: implement as frontend-only shell with no services or Axios calls.
- **Panel coupling**: documentation may be rendered inside the protected panel instead of public route.
  Mitigation: link from Parent Control Shell to `/docs`; keep Docs as independent public view.
- **Adult UI drift**: Docs view may inherit GameView visuals.
  Mitigation: align with Parent Control Shell visual language and adult design tokens.

## Dependencies
- `docs/product/features/frontend/FEAT-006-Docs-View.md` - source feature.
- `docs/product/features/frontend/FEAT-001-Base-Styles.md` - design tokens and UI component baseline.
- `docs/product/features/frontend/FEAT-005-Parent-Control-View.md` - Parent Control Shell documentation entry point.
- `docs/design/frontend_design_v1.docx` - frontend behavior and route decisions.
- `docs/design/design_decisions_v1.docx` - adult UI visual and accessibility decisions.

## Agent Instruction
- Implement only `FEAT-006-Docs-View` as a public Docs View Shell.
- Do not implement Markdown rendering, Markdown loading, dynamic slug routing, search, versioned docs, backend endpoints, WebSocket integration, or documentation editing.
- Do not use Axios or services from the Docs view.
- `/docs` must be public and must not require parent auth, FamilySession, Bearer token, or `VITE_ENABLE_DEV_CONTENT`.
- Parent Control Shell should link to `/docs` rather than render docs internally.
- All visible strings and aria labels should go through Vue i18n where applicable.
- Keep the UI aligned with adult-facing design tokens and accessibility rules.
- Commit: `feat(frontend): add docs view shell`

## Notes
Derived from `docs/product/features/frontend/FEAT-006-Docs-View.md`.

Design output:
- View/feature: public Docs View Shell.
- Data flow: none beyond local component state for active documentation section; no service or API calls.
- Component tree: router `/docs` -> `DocsView` -> section navigation + placeholder content; Parent Control Shell Documentation item links to `/docs`.
- Contract dependency: none for this sprint.
- Future direction: static Markdown content loading and rendering may be added in a separate feature.
- Risks: auth guard leakage, Markdown scope creep, accidental API calls, panel coupling, adult UI drift.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
