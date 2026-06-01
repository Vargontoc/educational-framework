# FEAT-004 - Frontend: Child Creation Modal

## Status

state: accepted
user_history: Child creation modal from the Home child selector
depends_on: FEAT-001-Base-Styles, FEAT-002-Home-View, FEAT-003-Creation-Family, docs/contracts/api/openapi.json
owned_by: frontend
scope: frontend view + API integration using `docs/contracts/api/openapi.json`. No backend implementation is included in this feature.
test: component + integration + manual responsive checks

## Description

This feature defines the child creation flow opened from the Home child selector when a family already exists.

When the user presses the family name button in `HomeView`, the child selector modal shows a centered grid of registered child avatar cards. Each card shows only the child's avatar and name. The same modal includes a `+` action to add a new child profile.

Pressing `+` opens a child creation stepper. The flow remains adult-facing and follows the design principle of one decision per screen.

The stepper uses three steps:
- Step 1: child name.
- Step 2: child birthday.
- Step 3: avatar selection from a placeholder grid.

After successful creation, the frontend refreshes the child list and returns to the child selector modal. This feature does not include child session creation or navigation to GameView.

## Product Rules

- Child creation is only available when a family is already registered.
- The entry point is the `+` action inside the child selector modal.
- The selector grid cards show only avatar image and child name.
- Child avatar cards use the avatar identifier color as a visual accent when available.
- The flow is adult-facing, so validation errors use standard adult UI semantics.
- Adult error color is `#E53935`; the child-friendly orange retry color must not be used for this modal.
- `ttsEnabled` and `agentEnabled` are enabled by default in v1 and must not appear as form controls.
- Sustained uppercase labels must be avoided.
- All visible copy must come from Vue i18n.

## UX Flow

### Entry Point - Child Selector

- `HomeView` is in `familyReady` state.
- Pressing the family name opens the child selector modal.
- The selector fetches or uses the current registered child list.
- Registered children are shown in a centered grid.
- Each child card contains:
  - Avatar image.
  - Child name in Nunito Bold.
  - Visual accent using the avatar identifier color when available.
- Each child card must be at least `120x140px`.
- Avatar image must be at least `96px` where the viewport allows it.
- A `+` card/action opens the child creation stepper.

### Step 1 - Child Name

- Show one text field for the child display name.
- The primary action advances to step 2.
- The field is required.
- Empty or whitespace-only values must show an inline validation message.
- The modal can be closed before submission; closing clears local draft state.

### Step 2 - Birthday

- Show one birthday field.
- The value must be sent as an ISO date string matching the `date` format in the OpenAPI contract.
- The field is required because `CreateChildProfileRequest` requires `birthday`.
- Invalid or empty dates must show inline validation.
- Do not add local age-range domain validation unless required by the contract or a later product decision.

### Step 3 - Avatar Selection

- Show a grid of available avatar placeholders.
- Each avatar option must be selectable by pointer and keyboard.
- The selected avatar must have a clear visual state that is not color-only.
- The selected avatar value is sent as `avatar` when available.
- If product assets are not final yet, use stable placeholder identifiers rather than external URLs.
- If no avatar is selected, use a documented default placeholder value or omit `avatar` only if the current service implementation handles the nullable contract safely.

## API Integration

Child creation must call `POST /api/v1/family/children` through the shared Axios client.

Request shape is derived from `CreateChildProfileRequest` in `docs/contracts/api/openapi.json`:
- `name`: child display name from step 1.
- `birthday`: ISO date string from step 2.
- `avatar`: selected avatar placeholder identifier or nullable value allowed by the contract.
- `ttsEnabled`: `true` by default in v1.
- `agentEnabled`: `true` by default in v1.

Response handling:
- `201`: close the child creation stepper, clear draft state, refresh the child list, and return to the child selector modal.
- `400`: show inline validation feedback in the stepper.
- `404`: show a family-not-found message, refresh Home/family state, and avoid keeping stale modal state.
- Network or `5xx`: show a recoverable adult-facing error with a retry action.

The feature must not invent local request or response models that diverge from `openapi.json`.

## State Management

- Keep child creation draft state local to the modal unless another component needs it.
- Use the existing family or child store/service layer for API calls and child-list refresh.
- Stores must call services only; components and stores must not call Axios directly.
- Do not persist draft child name, birthday, or avatar selection in localStorage, sessionStorage, route state, or persisted Pinia slices.
- Clear draft state after success, close, or unrecoverable reset.

## Accessibility

- The child selector and child creation stepper must use accessible dialog semantics through the shared modal component.
- Focus moves to the first actionable field/control when the creation stepper opens.
- Focus is trapped while the modal is open.
- `Escape` closes the active modal before submission.
- Focus returns to the `+` action after closing the creation stepper.
- Child avatar cards and avatar picker options must have translated accessible labels.
- The selected avatar state must be exposed with ARIA state and visible shape/icon/border, not color alone.
- Adult touch targets must be at least 44px.
- Child selector cards should remain comfortable in tablet landscape and mobile landscape.

## Visual Requirements

- Use Nunito and the global design tokens from `FEAT-001-Base-Styles`.
- Use card/modal radii from the accepted design tokens.
- The child selector grid should be centered and visually simple.
- Child names use Nunito Bold.
- Avatar cards use rounded, warm surfaces and an avatar color accent.
- Primary action uses cobalt blue `#2B5BE0`.
- Adult validation error uses red `#E53935`.
- Loading state disables actions and prevents double submission.

## Out Of Scope

- Child session creation.
- Navigation to GameView after creating a child.
- Starting a child session by pressing a child card.
- Editing child profiles.
- Deleting child profiles.
- Parent PIN login or panel access.
- Selecting `ttsEnabled` or `agentEnabled` in the UI.
- Backend implementation or contract changes.
- Audio, TTS, avatar speech, or agent calls.

## Acceptance Criteria

- When Home is in `familyReady` state, pressing the family name opens the child selector modal.
- The child selector shows registered children as avatar cards with only image and name.
- The child selector includes a `+` action to open the child creation stepper.
- The child creation stepper starts at step 1 and asks only for the child name.
- Empty child name cannot advance and shows an inline translated validation message.
- Step 2 asks only for birthday and sends a contract-compatible date value.
- Empty or invalid birthday cannot advance and shows inline translated validation.
- Step 3 shows avatar placeholder options in a grid.
- Avatar selection has a visible selected state that is not color-only.
- Submitting calls `POST /api/v1/family/children` with `name`, `birthday`, `avatar`, `ttsEnabled: true`, and `agentEnabled: true`.
- On `201`, the creation stepper closes, draft state is cleared, the child list refreshes, and the child selector remains available.
- On `400`, validation feedback is shown inline without leaving the stepper.
- On `404`, stale family state is handled by refreshing Home/family data.
- On network or server error, the stepper shows a retryable error without losing valid previous-step data.
- Closing the stepper clears draft data and returns focus to the `+` action.
- All visible strings are provided by Vue i18n.
- The flow works in tablet landscape and mobile landscape.
- Portrait orientation continues to show the existing rotation overlay.

## Testing Notes

Required tests:
- Opens child selector from Home `familyReady` state.
- Renders child cards with avatar and name only.
- Opens child creation stepper from the `+` action.
- Step 1 required-field validation.
- Step 2 required and invalid birthday validation.
- Avatar grid selection state and keyboard selection.
- Successful submission payload matches `CreateChildProfileRequest`, including `ttsEnabled: true` and `agentEnabled: true`.
- `201`, `400`, `404`, and network/server error handling.
- Child list refresh after successful creation.
- Modal close clears draft state.
- All visible labels resolve through i18n keys.

Manual checks:
- Landscape tablet layout.
- Landscape mobile layout.
- Portrait rotation overlay still covers the flow.
- Focus trap and keyboard operation in selector and creation stepper.

## Risks And Mitigations

- Risk: Treating child creation as session start.
  Mitigation: keep this feature limited to creating the profile and refreshing the selector; child session creation belongs to another flow.
- Risk: Adding too many configuration controls for the adult.
  Mitigation: hide `ttsEnabled` and `agentEnabled` and send both as `true` by default in v1.
- Risk: Contract drift between frontend payload and backend schema.
  Mitigation: derive request typing from `docs/contracts/api/openapi.json` and block implementation if required fields change.
- Risk: Using final-avatar assumptions before assets are available.
  Mitigation: use stable placeholder identifiers and document the future asset replacement path.
- Risk: Selected avatar state relies only on color.
  Mitigation: combine color with border, checkmark/text, and ARIA selected state.
- Risk: Double submission while the backend is processing.
  Mitigation: disable actions while submitting and ignore duplicate submit attempts.
