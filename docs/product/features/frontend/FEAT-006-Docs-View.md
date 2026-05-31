# FEAT-006 - Frontend: Docs View Shell

## Status

state: proposal
user_history: Public documentation view for the application
depends_on: FEAT-001-Base-Styles, FEAT-005-Parent-Control-View
owned_by: frontend
scope: frontend view + routing only. No backend implementation, API integration, websocket integration, or Markdown rendering implementation is included in this feature.
test: component + routing + manual responsive checks

## Description

This feature defines the public documentation shell available at `/docs`.

The documentation route is accessible directly by URL and does not require parental PIN, FamilySession, Bearer token, or any other authentication state. The Parent Control Shell links to this route from its Documentation section, but documentation is rendered as its own public view, not inside the protected panel.

This feature only establishes the view, route, layout, navigation structure, and placeholder content. Real documentation content and future Markdown loading are intentionally deferred.

## Product Rules

- `/docs` is public and must be accessible without authentication.
- The view is adult-facing and uses the adult UI register.
- The view must not call backend APIs.
- The view must not open WebSocket or ParentChannel connections.
- The view must not depend on a FamilySession token.
- The Parent Control Shell may link to `/docs`, but `/docs` remains independently accessible.
- All visible copy must come from Vue i18n or local static content approved for this shell.
- Sustained uppercase labels must be avoided.

## Future Markdown Preparation

The shell should be structured so future work can replace placeholder content with static Markdown content.

Future direction:
- Documentation content may later be stored as static Markdown files.
- A future feature may add a Markdown parser/renderer and static content loading strategy.
- A future feature may define content folders, slug routing, table of contents generation, and safe rendering rules.

Out of scope for this feature:
- Markdown parser dependency.
- Markdown file loading.
- Dynamic slug routing.
- Search indexing.
- Backend documentation endpoints.
- Editing documentation from the UI.

## UX Flow

### Direct URL Access

- User opens `/docs` directly.
- The documentation shell renders without checking auth state.
- If no family exists, the route still renders.
- If a parent session exists, the route still behaves as a public page.

### Parent Panel Entry

- From the Parent Control Shell Documentation item, the app navigates to `/docs`.
- Navigation should use a standard route link or router navigation.
- Returning to `/panel` still requires an active in-memory token because `/panel` remains protected.

## View Structure

The initial Docs shell contains:
- Header with documentation title and short description.
- Navigation/index for documentation sections.
- Main content region.
- Placeholder content for each section.
- Optional link back to Home.
- Optional link back to Panel only when an authenticated in-memory parent session exists.

Initial sections:
- Getting started.
- Family and profiles.
- Parent control panel.
- Family experiences.
- Privacy and security.
- Support.

Section behavior:
- Section navigation is local to the Docs view.
- Selecting a section changes the visible placeholder content without backend calls.
- Active section state must be visible and not color-only.
- The structure should be easy to replace with static Markdown-driven content later.

## Routing

- Add route `/docs` with name `docs`.
- The route is public and must not use the parental auth guard.
- The route must not require `VITE_ENABLE_DEV_CONTENT`.
- Unknown routes continue to redirect according to the existing router policy.

## State Management

- Prefer local component state for the active documentation section.
- Do not add a Pinia store for this feature unless later requirements make state shared.
- Do not persist documentation navigation state.
- Do not call services or Axios.

## Visual Requirements

- Use Nunito and global design tokens from `FEAT-001-Base-Styles`.
- Use adult panel background `#F4F6F9`.
- Use white surfaces for cards/content blocks.
- Use cobalt blue `#2B5BE0` for primary active states and links.
- Use warm/gold `#F5A623` sparingly for helpful highlights.
- Keep the visual language aligned with the Parent Control Shell, not GameView.
- Do not include child-game feedback colors or immersive game-world decorations.

## Responsive Requirements

- Desktop/tablet landscape can use a two-column layout with section navigation and content.
- Mobile landscape can stack navigation and content or use a compact horizontal section list.
- Portrait orientation should follow the app's existing rotation overlay behavior if the global shell applies it.
- Text must remain readable without horizontal scrolling.

## Accessibility

- Use semantic headings in order.
- Section navigation must be keyboard operable.
- Active section state must not rely on color only.
- Links must have clear translated labels.
- Text contrast must meet WCAG AA for adult UI.
- Touch targets must be at least 44px.
- The page must remain useful without audio.

## Out Of Scope

- Real documentation content authoring.
- Markdown rendering implementation.
- Markdown parser dependencies.
- Backend integration.
- WebSocket integration.
- Search.
- Versioned documentation.
- Download/export.
- Authentication or parental access checks.
- Editing documentation from the UI.

## Acceptance Criteria

- `/docs` route exists and renders without authentication.
- `/docs` is accessible when no parent token is present.
- Parent Control Shell Documentation item can navigate to `/docs`.
- The docs view renders the title, description, section navigation, and placeholder content.
- Initial sections include Getting started, Family and profiles, Parent control panel, Family experiences, Privacy and security, and Support.
- Selecting a section updates visible content locally without API calls.
- Active section state is visible and not color-only.
- No backend API calls are made by the docs view.
- No WebSocket connections are opened by the docs view.
- All visible strings are provided by Vue i18n or approved local static placeholders.
- The view works in desktop/tablet landscape and mobile landscape.
- Portrait behavior remains consistent with the existing app rotation overlay.

## Testing Notes

Required tests:
- `/docs` renders without authenticated session.
- `/docs` renders when no family state is loaded.
- Documentation link from Parent Control Shell points to `/docs`.
- Section navigation changes visible placeholder content locally.
- No Axios/API call is triggered by opening or navigating inside Docs view.
- All visible labels resolve through i18n keys where applicable.

Manual checks:
- Desktop/tablet landscape layout.
- Mobile landscape layout.
- Portrait rotation overlay behavior.
- Keyboard navigation through section links and page links.

## Risks And Mitigations

- Risk: Implementing Markdown rendering too early.
  Mitigation: document Markdown as future preparation only and do not add parser dependencies in this feature.
- Risk: Accidentally protecting `/docs` behind parent auth.
  Mitigation: keep `/docs` outside the `/panel` guard and test direct access without token.
- Risk: Scope creep into full documentation content.
  Mitigation: keep placeholder content minimal and create later content features.
- Risk: Docs view starts calling backend endpoints for content.
  Mitigation: make this a frontend-only shell with no services or Axios calls.
- Risk: Adult docs UI drifts into GameView styling.
  Mitigation: align with Parent Control Shell visual language and adult design tokens.
