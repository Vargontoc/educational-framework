import { createFamilyViaApi } from '../../support/testData'

describe('Harness: sesión parental', () => {
  before(() => {
    createFamilyViaApi({ name: 'Sesion E2E', pin: '1234' })
  })

  it('loginAsParent deja al usuario autenticado y puede navegar al panel', () => {
    cy.loginAsParent()
    cy.visit('/panel')
    cy.url().should('include', '/panel')
  })

  it('la sesión se mantiene entre llamadas (cy.session cachea el estado)', () => {
    cy.loginAsParent()
    cy.visit('/panel')
    cy.url().should('include', '/panel')
    cy.window().then((win) => {
      const token = win.sessionStorage.getItem('nubi-parental-token')
      expect(token).to.not.be.null
      expect(token).to.not.be.empty
    })
  })
})
