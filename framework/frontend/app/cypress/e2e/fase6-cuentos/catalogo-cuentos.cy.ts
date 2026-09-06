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

describe('Catálogo de cuentos (SPRINT-037)', () => {
  before(() => {
    createFamilyViaApi({ name: uniqueFamilyName('CatCatalogo'), pin: PIN })
  })

  beforeEach(() => {
    cy.viewport(1280, 800)
  })

  it('positivo: el catálogo consume GET /api/v1/stories y muestra los cuentos disponibles', () => {
    loginViaUi()
    goToCatalog()

    cy.get('.lecture-view', { timeout: LOAD_TIMEOUT }).should('exist')
    cy.get('.grid-catalog', { timeout: LOAD_TIMEOUT }).should('exist')
    cy.get('.grid-catalog .nubi-card').should('have.length.gte', 1)
    cy.get('.nubi-card__title').should('have.length.gte', 1)
    cy.get('.grid-catalog .story-cover img').should('have.length.gte', 1)
  })

  it('positivo: hacer clic en un cuento navega al lector', () => {
    loginViaUi()
    goToCatalog()

    cy.get('.grid-catalog .story-cover', { timeout: LOAD_TIMEOUT }).first().click()
    cy.url().should('include', '/story/')
    cy.get('.story-container', { timeout: LOAD_TIMEOUT }).should('exist')
  })

  it('negativo: con el catálogo vacío (API devuelve []) no se muestra la cuadrícula ni lista rota', () => {
    cy.intercept({
      method: 'GET',
      url: '**/api/v1/stories**',
    }, {
      statusCode: 200,
      body: [],
      headers: { 'content-type': 'application/json' }
    }).as('getCatalogEmpty')

    loginViaUi()
    goToCatalog()

    cy.get('.lecture-view', { timeout: LOAD_TIMEOUT }).should('exist')
    cy.get('.grid-catalog').should('not.exist')
    cy.get('.nubi-card').should('not.exist')
  })

  it('negativo: con la API fallando (500) no se muestra una lista rota', () => {
    cy.intercept({
      method: 'GET',
      url: '**/api/v1/stories**',
    }, {
      statusCode: 500,
      body: { success: false, message: 'Internal Server Error', errors: [] }
    }).as('getCatalogFail')

    loginViaUi()
    goToCatalog()

    cy.get('.lecture-view', { timeout: LOAD_TIMEOUT }).should('exist')
    cy.get('.grid-catalog').should('not.exist')
    cy.get('.nubi-card').should('not.exist')
  })
})
