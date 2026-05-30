# Sprint 009 - frontend
# -----------------------------------------------

## Goal
Implement the family creation flow from `docs/product/features/frontend/FEAT-003-Creation-Family.md`: a two-step adult-facing modal opened from Home when no family exists, with family name entry, custom PIN creation/confirmation keypad, OpenAPI-derived request payload, inline validation, and Home state refresh after successful creation.

## Status
status: archived
started_at: 2026-05-30 00:00:00
closed_at: 2026-05-30 00:00:00
blocked_by:
waiting_for:

## Tasks

### Contract And Existing Flow Review
- [ ] Review `docs/contracts/api/openapi.json` for `CreateFamilyRequest`, `FamilyResponse`, `ApiResponseFamily`, and `/api/v1/family` responses.
- [ ] Verify existing `src/services/familyService.ts`, `src/stores/useFamilyStore.ts`, `src/views/HomeView.vue`, and `src/components/home/FamilyRegistrationModal.vue` before editing.
- [ ] Confirm the current Home bootstrap flow can distinguish `loading`, `noFamily`, `familyReady`, and `error`.
- [ ] Do not change backend contracts in this sprint.

### Family Creation Service And Store
- [ ] Ensure family creation uses only the shared Axios client from `src/shared/api/axios.ts`.
- [ ] Ensure request typing is derived from `docs/contracts/api/openapi.json`: `name`, `pin`, `ttsEnabled`, and `agentEnabled`.
- [ ] Default `ttsEnabled` to `true` in v1.
- [ ] Default `agentEnabled` to `true` in v1.
- [ ] Handle `201`, `400`, `409`, network, and `5xx` cases distinctly enough for the modal UX.
- [ ] Ensure the store refreshes Home family state after successful creation and after `409` conflict recovery.
- [ ] Do not persist PIN values in Pinia persisted state, localStorage, sessionStorage, logs, or route state.

### Two-Step Modal UI
- [ ] Implement the family creation modal as a two-step conversational flow.
- [ ] Step 1 asks only for the family display name.
- [ ] Step 1 blocks empty or whitespace-only values with inline translated validation.
- [ ] Step 2 creates and confirms a 4-digit PIN.
- [ ] Step 2 uses a custom numeric keypad with digits 0-9 and delete; do not use a native PIN input keyboard.
- [ ] Mask PIN digits as dot indicators; never render entered digits as visible text.
- [ ] Auto-advance from first PIN entry to confirmation when four digits are entered.
- [ ] Auto-submit when confirmation reaches four digits and matches the first PIN entry.
- [ ] On mismatch, show adult red error feedback, shake indicators, clear confirmation only, and keep the first PIN entry.
- [ ] Provide a secondary action to return to step 1 without losing the valid family name.
- [ ] Clear local name and PIN draft state on success, close, and unrecoverable reset.

### Accessibility And UX
- [ ] Use the shared modal component and preserve dialog semantics.
- [ ] Move focus to the first field/control when opening the modal.
- [ ] Keep focus trapped while open and return focus to `Bienvenida familia` after close.
- [ ] Allow `Escape` to close before submission.
- [ ] Add translated accessible labels for keypad digits, delete, back, close, and submit/progress states.
- [ ] Expose PIN progress to assistive technology without exposing digits.
- [ ] Keep adult touch targets at least 44px and keypad controls comfortable in tablet/mobile landscape.
- [ ] Use adult error color `#E53935`; do not use child retry orange for this modal.
- [ ] Avoid sustained uppercase visible labels.

### i18n
- [ ] Add all visible labels, helper text, validation messages, API error messages, and aria labels to `src/i18n/es.ts`.
- [ ] Do not hardcode visible text in Vue templates.
- [ ] Keep copy warm and simple without adding extra decisions beyond the current step.

### Testing And Verification
- [ ] Add or update component/integration tests if the project has a test harness available for this area.
- [ ] Verify modal opens from Home `noFamily` state.
- [ ] Verify required family name validation.
- [ ] Verify keypad digit entry, delete action, dot indicators, and auto-advance.
- [ ] Verify mismatched PIN behavior.
- [ ] Verify successful submission payload matches `CreateFamilyRequest`.
- [ ] Verify `201`, `400`, `409`, and network/server error handling.
- [ ] Verify closing the modal clears PIN state.
- [ ] Verify all visible strings resolve through i18n.
- [ ] Verify landscape tablet, landscape mobile, and portrait rotation overlay behavior manually.
- [ ] Run `npm run build` from `framework/frontend/app`.

## Risks
- **Form scope creep**: family creation may expand into child creation, panel login, or configuration.
  Mitigation: implement only `FEAT-003-Creation-Family`; keep child creation, parent login, PIN changes, and GameView navigation out of scope.
- **PIN leakage**: PIN values may be accidentally persisted or logged.
  Mitigation: keep PIN state local and ephemeral; clear it aggressively and never log payloads containing PINs.
- **Contract drift**: frontend may invent request shapes or omit required fields.
  Mitigation: derive request types from `docs/contracts/api/openapi.json` and send `name`, `pin`, `ttsEnabled: true`, `agentEnabled: true`.
- **Adult/child feedback confusion**: GameView child retry colors may leak into adult validation.
  Mitigation: use adult red `#E53935` for validation and standard adult UI semantics.
- **Double submission**: auto-submit can race if controls remain active.
  Mitigation: disable keypad/actions while submitting and ignore duplicate submit attempts.

## Dependencies
- `docs/product/features/frontend/FEAT-003-Creation-Family.md` - source feature.
- `docs/product/features/frontend/FEAT-001-Base-Styles.md` - design tokens and UI component baseline.
- `docs/product/features/frontend/FEAT-002-Home-View.md` - Home entry state and modal entry point.
- `docs/design/frontend_design_v1.docx` - frontend behavior and architecture decisions.
- `docs/design/design_decisions_v1.docx` - visual, typography, interaction, and accessibility decisions.
- `docs/contracts/api/openapi.json` - source of truth for `/api/v1/family` request/response shapes.

## Agent Instruction
- Archived to start Sprint 010 for child creation modal.
- Preserve existing implementation work and do not revert related files without explicit human confirmation.

## Notes
Derived from `docs/product/features/frontend/FEAT-003-Creation-Family.md`.

Design output:
- View/feature: Home family creation modal.
- Data flow: `HomeView` opens `FamilyRegistrationModal`; modal keeps draft step/PIN state locally; submission goes through family store/service; service calls `POST /api/v1/family`; store refreshes Home family state.
- Component tree: `HomeView` -> `FamilyRegistrationModal` -> shared `Modal` + local step content + custom keypad controls.
- Contract dependency: `POST /api/v1/family`, `CreateFamilyRequest`, `ApiResponseFamily`, `Error400`, `Error409` from `docs/contracts/api/openapi.json`.
- Risks: PIN leakage, contract drift, double submission, accidental expansion into child/panel flows.

## Review

completed_tasks:

incomplete_tasks:
- Sprint archived before checklist completion to prioritize `FEAT-004-Modal-Creation-Child`.

contract_changes:

learnings:

next_sprint_suggestions:
- Re-open family creation flow as a later sprint if still required.
