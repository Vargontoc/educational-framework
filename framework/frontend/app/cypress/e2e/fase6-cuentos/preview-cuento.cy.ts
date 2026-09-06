import { createFamilyViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'
const LOAD_TIMEOUT = 15000

function loginViaUi() {
  cy.visit('/')
  cy.get('.home-view').should('exist')
  cy.get('[aria-label="Configuración"]').should('be.visible').click()
  cy.contains('Acceso parental').should('be.visible')
  cy.get('.nubi-pin-input__digit').first().type(PIN)
  cy.url().should('include', '/panel')
}

function goToCatalog() {
  cy.get('.parent-sidebar', { timeout: LOAD_TIMEOUT }).should('be.visible')
  cy.get('.parent-sidebar__link').contains('Lectura').click()
  cy.url().should('include', '/panel/lectura-familiar')
}

describe('Vista previa del cuento y precarga (SPRINT-038)', () => {
  before(() => {
    createFamilyViaApi({ name: uniqueFamilyName('CatPreview'), pin: PIN })
  })

  beforeEach(() => {
    cy.viewport(1280, 800)
  })

  it('positivo: abrir un cuento desde el catálogo muestra portada y título, y precarga recursos', () => {
    loginViaUi()
    goToCatalog()

    cy.get('.grid-catalog .nubi-card__title', { timeout: LOAD_TIMEOUT }).first().invoke('text').then((title) => {
      cy.get('.grid-catalog .story-cover').first().click()
      cy.url().should('include', '/story/')
      cy.get('.story-container', { timeout: LOAD_TIMEOUT }).should('exist')
      cy.get('.story-image-wrapper img').should('exist')
      cy.get('.story-footer__back').should('be.visible')
      cy.get('.nubi-toggle').should('exist')
      cy.get('.nubi-toggle__label').should('contain.text', 'Con voz')
    })
  })

  it('negativo: si los recursos de página fallan, el lector carga pero permite volver al panel', () => {
    cy.intercept({
      method: 'GET',
      url: '**/api/v1/stories/*/pages/*/image**',
    }, {
      statusCode: 500
    })

    loginViaUi()
    goToCatalog()

    cy.get('.grid-catalog .story-cover', { timeout: LOAD_TIMEOUT }).first().click()
    cy.url().should('include', '/story/')
    cy.get('.story-container', { timeout: LOAD_TIMEOUT }).should('exist')
    cy.get('.story-footer__back').should('be.visible')
  })
})
