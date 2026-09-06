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

describe('Volver contextual en documentación — detección de origen (SPRINT-032)', () => {
  before(() => {
    createFamilyViaApi({ name: uniqueFamilyName('Volver'), pin: PIN })
  })

  it('positivo: desde panel con query.from, boton Volver visible y retorna al panel', () => {
    cy.viewport(1280, 800)
    loginViaUi()
    cy.get('.parent-sidebar', { timeout: 10000 }).should('be.visible')

    cy.contains('.parent-sidebar__link', 'Documentación').click()
    cy.url().should('include', '/docs')
    cy.url().should('include', 'from=')

    cy.viewport(1000, 660)
    cy.get('.documentation-layout__back-button', { timeout: 10000 })
      .should('be.visible')

    cy.get('.documentation-layout__back-button').click()

    cy.url().should('include', '/panel')
    cy.url().should('not.include', 'from=')
  })

  it('negativo: acceso publico sin query.from no muestra boton Volver', () => {
    cy.viewport(1000, 660)
    cy.visit('/docs/quien-soy')

    cy.get('.documentation-layout__back-button', { timeout: 8000 })
      .should('not.exist')
  })

  it('negativo: query.from con URL externa no muestra boton Volver (open redirect)', () => {
    cy.viewport(1000, 660)
    cy.visit('/docs/quien-soy?from=https://evil.com')

    cy.get('.documentation-layout__back-button', { timeout: 8000 })
      .should('not.exist')
  })
})
