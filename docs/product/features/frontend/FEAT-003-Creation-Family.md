# FEAT-003 - Frontend: Family Creation

## Status

state: accepted
user_history: Family creation modal from Home when no family is registered
depends_on: FEAT-001-Base-Styles, FEAT-002-Home-View, docs/contracts/api/openapi.json
owned_by: frontend
scope: frontend view + API integration using `docs/contracts/api/openapi.json`. No backend implementation is included in this feature.
test: component + integration + manual responsive checks

## Description

This feature defines the family creation flow opened from the `Bienvenida familia` CTA in `HomeView` when the initial family bootstrap state indicates that no family exists.

The flow must follow the product design principle from the frontend and design decisions documents: family registration is a short conversation, not a dense form. Each screen asks for a single decision.

The modal uses a two-step stepper:
- Step 1: family name.
- Step 2: parent PIN creation and confirmation using a custom numeric keypad.

After successful creation, the frontend refreshes the Home family state and transitions to the registered-family state. The feature does not include child profile creation, parent panel access, or child session creation.

## Product Rules

- Family creation is only available from Home when no family is registered.
- The flow is adult-facing, so validation errors use standard adult UI semantics.
- Adult error color is `#E53935`; the child-friendly orange retry color must not be used for this flow.
- The modal must feel warm and simple, but remain a functional adult interface.
- The PIN keypad is custom and must not rely on a native text input keyboard.
- Sustained uppercase labels must be avoided.
- All visible copy must come from Vue i18n.

## UX Flow

### Entry Point

- `HomeView` loads the family state through the existing family bootstrap flow.
- If no family exists, Home shows the avatar and the `Bienvenida familia` CTA.
- Pressing the CTA opens the family creation modal at step 1.

### Step 1 - Family Name

- Show one text field for the family display name.
- The primary action advances to step 2.
- The field is required.
- Empty or whitespace-only values must show an inline validation message.
- The modal can be closed before submission; closing clears local draft state.

### Step 2 - PIN Creation

- Show a numeric keypad with digits 0-9 and a delete action.
- Show PIN indicators as dots; digits are never displayed as plain text.
- Use a 4-digit PIN for v1, matching the mobile PIN mental model in the design decisions.
- Ask the user to enter the PIN once and then confirm it.
- Auto-advance from first entry to confirmation when the fourth digit is entered.
- Submit automatically when the confirmation PIN reaches four digits and matches.
- If confirmation does not match, show a red adult error state, shake the dot indicators, clear only the confirmation entry, and keep the user on step 2.
- Provide a secondary action to go back to step 1 without losing the family name.

## API Integration

Family creation must call `POST /api/v1/family` through the shared Axios client.

Request shape is derived from `CreateFamilyRequest` in `docs/contracts/api/openapi.json`:
- `name`: family display name from step 1.
- `pin`: confirmed PIN from step 2.
- `ttsEnabled`: `true` by default in v1.
- `agentEnabled`: `true` by default in v1.

Response handling:
- `201`: close modal, clear draft state, refresh the Home family state, and show the registered-family UI.
- `400`: show inline validation feedback in the modal.
- `409`: show a conflict message indicating that a family already exists, then refresh Home state.
- Network or `5xx`: show a recoverable adult-facing error with a retry action.

The feature must not invent local request or response models that diverge from `openapi.json`.

## State Management

- Keep stepper draft state local to the modal unless another component needs it.
- Use the existing family store or service layer for the actual API call and Home refresh.
- Stores must call services only; components and stores must not call Axios directly.
- No PIN value may be persisted in Pinia persisted state, localStorage, sessionStorage, or logs.
- The PIN must be cleared from component state after success, close, or unrecoverable error.

## Accessibility

- The modal must use accessible dialog semantics through the shared modal component.
- Focus moves to the first actionable field/control when the modal opens.
- Focus is trapped while the modal is open.
- `Escape` closes the modal before submission.
- Focus returns to the `Bienvenida familia` CTA after close.
- Keypad buttons must have translated accessible labels.
- Dot indicators must expose progress to assistive technology without exposing digits.
- Adult touch targets must be at least 44px.
- Primary submit/keypad controls should remain comfortable on tablet landscape and mobile landscape.

## Visual Requirements

- Use Nunito and the global design tokens from `FEAT-001-Base-Styles`.
- Modal/card radius follows the accepted card/modal radius token.
- Primary action uses cobalt blue `#2B5BE0`.
- Adult validation error uses red `#E53935`.
- The modal sits over the Home sky/grass world without making the flow feel like GameView.
- Loading state disables actions and prevents double submission.

## Out Of Scope

- Child profile creation.
- Parent PIN login for panel access.
- Changing an existing family PIN.
- Selecting PIN length in UI.
- Child session creation or navigation to GameView.
- Backend implementation or contract changes.
- Audio, TTS, avatar speech, or agent calls.

## Acceptance Criteria

- When Home is in `noFamily` state, pressing `Bienvenida familia` opens the family creation modal.
- The modal starts at step 1 and asks only for the family name.
- Empty family name cannot advance and shows an inline translated validation message.
- Step 2 uses a custom numeric keypad; no native PIN input is displayed.
- PIN digits are masked as dots and never shown as text.
- The first PIN entry auto-advances to confirmation after four digits.
- A mismatched confirmation shows a red adult error state, shakes indicators, clears confirmation, and keeps the original first PIN entry.
- A matching confirmation submits `POST /api/v1/family` with `name`, `pin`, `ttsEnabled: true`, and `agentEnabled: true`.
- On `201`, the modal closes, draft state is cleared, Home refreshes family state, and the family name appears in Home.
- On `400`, validation feedback is shown inline without leaving the modal.
- On `409`, the modal shows a conflict message and refreshes Home state.
- On network or server error, the modal shows a retryable error without losing the valid family name.
- Closing the modal clears PIN data and returns focus to the Home CTA.
- All visible strings are provided by Vue i18n.
- The flow works in tablet landscape and mobile landscape.
- Portrait orientation continues to show the existing rotation overlay.

## Testing Notes

Required tests:
- Opens modal from Home `noFamily` state.
- Step 1 required-field validation.
- Keypad digit entry, delete action, dot indicators, and auto-advance.
- Mismatched PIN confirmation behavior.
- Successful submission payload matches `CreateFamilyRequest`.
- `201`, `400`, `409`, and network error handling.
- Modal close clears PIN state.
- All visible labels resolve through i18n keys.

Manual checks:
- Landscape tablet layout.
- Landscape mobile layout.
- Portrait rotation overlay still covers the flow.
- Focus trap and keyboard operation in the modal.

## Risks And Mitigations

- Risk: Treating the flow as a generic form with too many controls.
  Mitigation: Keep the two-step conversational model and default `ttsEnabled` and `agentEnabled` to `true` in v1.
- Risk: Leaking PIN values through persisted state or logs.
  Mitigation: Keep PIN local and ephemeral, clear it aggressively, and never log request payloads containing PINs.
- Risk: Contract drift between frontend payload and backend schema.
  Mitigation: Derive request typing from `docs/contracts/api/openapi.json` and block implementation if required fields change.
- Risk: Confusing adult error semantics with child GameView feedback.
  Mitigation: Use adult red validation states only in this modal.
- Risk: Double submission while the backend is processing.
  Mitigation: Disable keypad and actions while submitting.
