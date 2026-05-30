# Sprint 010 - frontend
# -----------------------------------------------

## Goal
Implement the child selector and child creation flow from `docs/product/features/frontend/FEAT-004-Modal-Creation-Child.md`: a Home child selector modal with avatar cards and a `+` action, plus a three-step adult-facing child creation stepper for name, birthday, and avatar selection using OpenAPI-derived request payloads.

## Status
status: active
started_at: 2026-05-30 00:00:00
closed_at:
blocked_by:
waiting_for:

## Tasks

### Contract And Existing Flow Review
- [ ] Review `docs/contracts/api/openapi.json` for `CreateChildProfileRequest`, `ChildProfileResponse`, `ApiResponseChildProfile`, `ApiResponseChildProfileList`, and `/api/v1/family/children` responses.
- [ ] Verify existing `src/services/childService.ts`, `src/services/familyService.ts`, `src/stores/useFamilyStore.ts`, `src/views/HomeView.vue`, `src/components/home/ChildSelectorModal.vue`, and `src/components/home/AddChildModal.vue` before editing.
- [ ] Confirm the current Home `familyReady` flow opens the child selector from the family name button.
- [ ] Do not change backend contracts in this sprint.

### Child List And Selector
- [ ] Ensure child list loading uses the existing service/store layer and shared Axios client only.
- [ ] Render registered children as centered avatar cards in the child selector modal.
- [ ] Each child card shows only avatar image and child name.
- [ ] Keep child card size at least `120x140px` where viewport allows.
- [ ] Keep avatar image at least `96px` where viewport allows.
- [ ] Render child name in Nunito Bold.
- [ ] Use the avatar identifier color as a visual accent when available.
- [ ] Add a `+` card/action that opens the child creation stepper.
- [ ] Do not implement child session creation or GameView navigation in this sprint.

### Child Creation Service And Store
- [ ] Ensure child creation uses only the shared Axios client from `src/shared/api/axios.ts`.
- [ ] Ensure request typing is derived from `docs/contracts/api/openapi.json`: `name`, `birthday`, optional/nullable `avatar`, `ttsEnabled`, and `agentEnabled`.
- [ ] Default `ttsEnabled` to `true` in v1 and keep it hidden from the UI.
- [ ] Default `agentEnabled` to `true` in v1 and keep it hidden from the UI.
- [ ] Handle `201`, `400`, `404`, network, and `5xx` cases distinctly enough for the modal UX.
- [ ] Refresh the child list after successful creation.
- [ ] Refresh Home/family state after `404` family-not-found recovery.
- [ ] Do not persist child creation draft state in localStorage, sessionStorage, route state, or persisted Pinia slices.

### Three-Step Creation Stepper
- [ ] Implement child creation as a three-step conversational flow opened from the selector `+` action.
- [ ] Step 1 asks only for the child display name.
- [ ] Step 1 blocks empty or whitespace-only values with inline translated validation.
- [ ] Step 2 asks only for birthday.
- [ ] Step 2 requires an OpenAPI-compatible ISO date value.
- [ ] Step 2 blocks empty or invalid dates with inline translated validation.
- [ ] Do not add local age-range domain validation unless the contract requires it.
- [ ] Step 3 shows avatar placeholder options in a grid.
- [ ] Avatar selection must be pointer and keyboard accessible.
- [ ] Selected avatar state must not rely on color only.
- [ ] Use stable placeholder avatar identifiers if final assets are not available.
- [ ] Submit `POST /api/v1/family/children` with `name`, `birthday`, `avatar`, `ttsEnabled: true`, and `agentEnabled: true`.
- [ ] On success, close the creation stepper, clear draft state, refresh child list, and return to the child selector modal.

### Accessibility And UX
- [ ] Use the shared modal component and preserve dialog semantics.
- [ ] Move focus to the first field/control when opening the child creation stepper.
- [ ] Keep focus trapped while selector/stepper modal is open.
- [ ] Allow `Escape` to close before submission.
- [ ] Return focus to the `+` action after closing the creation stepper.
- [ ] Add translated accessible labels for child cards, the `+` action, avatar options, back, close, and submit/progress states.
- [ ] Expose selected avatar state through ARIA state and visible border/icon/text, not color alone.
- [ ] Keep adult touch targets at least 44px and selector cards comfortable in tablet/mobile landscape.
- [ ] Use adult error color `#E53935`; do not use child retry orange for this modal.
- [ ] Avoid sustained uppercase visible labels.

### i18n
- [ ] Add all visible labels, helper text, validation messages, API error messages, and aria labels to `src/i18n/es.ts`.
- [ ] Do not hardcode visible text in Vue templates.
- [ ] Keep copy warm and simple without adding extra decisions beyond the current step.

### Testing And Verification
- [ ] Add or update component/integration tests if the project has a test harness available for this area.
- [ ] Verify child selector opens from Home `familyReady` state.
- [ ] Verify child cards render avatar and name only.
- [ ] Verify `+` opens the child creation stepper.
- [ ] Verify required child name validation.
- [ ] Verify required and invalid birthday validation.
- [ ] Verify avatar grid selection state and keyboard selection.
- [ ] Verify successful submission payload matches `CreateChildProfileRequest`, including `ttsEnabled: true` and `agentEnabled: true`.
- [ ] Verify `201`, `400`, `404`, and network/server error handling.
- [ ] Verify child list refresh after successful creation.
- [ ] Verify closing the stepper clears draft state.
- [ ] Verify all visible strings resolve through i18n.
- [ ] Verify landscape tablet, landscape mobile, and portrait rotation overlay behavior manually.
- [ ] Run `npm run build` from `framework/frontend/app`.

## Risks
- **Session scope creep**: child creation may be mixed with starting a child session or GameView navigation.
  Mitigation: implement only profile creation and child-list refresh; leave session creation and navigation out of scope.
- **Configuration overload**: the modal may expose audio/avatar toggles too early.
  Mitigation: keep `ttsEnabled` and `agentEnabled` hidden and default both to `true` in v1.
- **Contract drift**: frontend may invent request shapes or omit required fields.
  Mitigation: derive request types from `docs/contracts/api/openapi.json` and send all required `CreateChildProfileRequest` fields.
- **Avatar asset assumptions**: final avatar assets may not exist yet.
  Mitigation: use stable placeholder identifiers and keep the replacement path documented.
- **Color-only selection**: avatar selection may be inaccessible if indicated only by color.
  Mitigation: combine color with border, icon/text, and ARIA selected state.
- **Double submission**: submit can race if controls remain active.
  Mitigation: disable actions while submitting and ignore duplicate submit attempts.

## Dependencies
- `docs/product/features/frontend/FEAT-004-Modal-Creation-Child.md` - source feature.
- `docs/product/features/frontend/FEAT-001-Base-Styles.md` - design tokens and UI component baseline.
- `docs/product/features/frontend/FEAT-002-Home-View.md` - Home child selector entry point.
- `docs/product/features/frontend/FEAT-003-Creation-Family.md` - family-ready state dependency.
- `docs/design/frontend_design_v1.docx` - frontend behavior and architecture decisions.
- `docs/design/design_decisions_v1.docx` - visual, typography, interaction, and accessibility decisions.
- `docs/contracts/api/openapi.json` - source of truth for `/api/v1/family/children` request/response shapes.

## Agent Instruction
- Implement only `FEAT-004-Modal-Creation-Child`.
- Do not implement family creation, parent PIN login, panel access, child session creation, GameView navigation changes, backend logic, audio, TTS, or agent calls.
- Use `GET /api/v1/family/children` and `POST /api/v1/family/children` through `src/shared/api/axios.ts` only.
- Stores call services; services call Axios.
- Derive TypeScript request/response types from `docs/contracts/api/openapi.json`.
- Keep `ttsEnabled: true` and `agentEnabled: true` hidden from the UI for v1.
- Do not persist child creation draft data in localStorage, sessionStorage, route state, or persisted Pinia slices.
- All visible strings and aria labels must go through Vue i18n.
- Keep the UI aligned with accepted design tokens, child avatar card sizing, and adult-facing validation semantics.
- Commit: `feat(frontend): add child creation modal`

## Notes
Derived from `docs/product/features/frontend/FEAT-004-Modal-Creation-Child.md`.

Design output:
- View/feature: Home child selector and child creation modal.
- Data flow: `HomeView` opens `ChildSelectorModal`; selector uses family/child store to list children; `+` opens `AddChildModal`; modal keeps draft step state locally; submission goes through child/family service; service calls `POST /api/v1/family/children`; store refreshes child list.
- Component tree: `HomeView` -> `ChildSelectorModal` -> child avatar card grid + add action -> `AddChildModal` -> shared `Modal` + local step content + avatar picker.
- Contract dependency: `GET /api/v1/family/children`, `POST /api/v1/family/children`, `CreateChildProfileRequest`, `ApiResponseChildProfile`, `ApiResponseChildProfileList`, `Error400`, `Error404` from `docs/contracts/api/openapi.json`.
- Risks: session scope creep, hidden default configuration, avatar placeholder replacement, inaccessible selection state, double submission.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
