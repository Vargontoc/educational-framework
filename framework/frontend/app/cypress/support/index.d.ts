declare namespace Cypress {
  interface Chainable {
    loginAsParent(pin?: string): Chainable<void>
    selectChildProfile(name: string): Chainable<void>
  }
}
