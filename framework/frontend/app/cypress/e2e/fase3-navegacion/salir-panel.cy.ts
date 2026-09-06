import { createFamilyViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'

function loginViaUi() {
  cy.visit('/')
  cy.get('.home-view').should('exist')
  cy.get('[aria-label="Configuración"]').should('be.visible').click()
  cy.contains('Acceso parental').should('be.visible')
  cy.get('.nubi-pin-input__digit').first().type(PIN)
  cy.url().should('include', '/panel')
}

describe('Salir del panel parental — logout y protección de rutas (SPRINT-015)', () => {
  before(() => {
    createFamilyViaApi({ name: uniqueFamilyName('Salir'), pin: PIN })
  })

  beforeEach(() => {
    cy.viewport(1280, 800)
  })

  it('positivo: pulsar Salir cierra la sesión y redirige a Home', () => {
    loginViaUi()
    cy.get('.parent-sidebar', { timeout: 10000 }).should('be.visible')

    cy.intercept('POST', '**/api/v1/auth/logout').as('logout')

    cy.get('.parent-sidebar__logout').should('be.visible').click()

    cy.wait('@logout')
    cy.location('pathname').should('eq', '/')
    cy.window().then((win) => {
      expect(win.sessionStorage.getItem('nubi-parental-token')).to.be.null
    })
  })

  it('negativo: tras Salir, navegar a /panel redirige a Home (guard protege la ruta)', () => {
    loginViaUi()
    cy.get('.parent-sidebar', { timeout: 10000 }).should('be.visible')

    cy.intercept('POST', '**/api/v1/auth/logout').as('logout')
    cy.get('.parent-sidebar__logout').should('be.visible').click()
    cy.wait('@logout')

    cy.location('pathname').should('eq', '/')

    cy.visit('/panel')
    cy.location('pathname').should('eq', '/')
  })
})
