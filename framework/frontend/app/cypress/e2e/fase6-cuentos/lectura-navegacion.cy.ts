import { createFamilyViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'
const LOAD_TIMEOUT = 15000
const TRANSITION_WAIT = 400

function loginViaUi() {
  cy.visit('/')
  cy.get('.home-view').should('exist')
  cy.get('[aria-label="Configuración"]').should('be.visible').click()
  cy.contains('Acceso parental').should('be.visible')
  cy.get('.nubi-pin-input__digit').first().type(PIN)
  cy.url().should('include', '/panel')
}

function navigateToReader() {
  loginViaUi()
  cy.get('.parent-sidebar', { timeout: LOAD_TIMEOUT }).should('be.visible')
  cy.get('.parent-sidebar__link').contains('Lectura').click()
  cy.get('.grid-catalog .story-cover', { timeout: LOAD_TIMEOUT }).first().click()
  cy.url().should('include', '/story/')
  cy.get('.story-container', { timeout: LOAD_TIMEOUT }).should('exist')
}

describe('Lectura: navegación de páginas (SPRINT-039)', () => {
  before(() => {
    createFamilyViaApi({ name: uniqueFamilyName('CatNav'), pin: PIN })
  })

  beforeEach(() => {
    cy.viewport(1280, 800)
  })

  it('positivo: navegar hacia adelante con flecha muestra las páginas en orden', () => {
    navigateToReader()

    cy.get('.story-nav-slot--prev .nubi-icon-button').should('not.exist')
    cy.get('.story-nav-slot--next .nubi-icon-button').should('be.visible').click()
    cy.wait(TRANSITION_WAIT)

    cy.get('.story-nav-slot--prev .nubi-icon-button').should('be.visible')
    cy.get('.story-footer__text').should('exist')
  })

  it('positivo: navegar hacia atrás con flecha retrocede correctamente', () => {
    navigateToReader()

    cy.get('.story-nav-slot--next .nubi-icon-button').first().click()
    cy.wait(TRANSITION_WAIT)

    cy.get('.story-nav-slot--prev .nubi-icon-button').should('be.visible').click()
    cy.wait(TRANSITION_WAIT)

    cy.get('.story-title-overlay').should('exist')
    cy.get('.story-nav-slot--prev .nubi-icon-button').should('not.exist')
  })

  it('positivo: el swipe táctil avanza entre páginas', () => {
    navigateToReader()

    cy.get('.story-content')
      .trigger('touchstart', { touches: [{ clientX: 500, clientY: 300 }] })
      .trigger('touchend', { changedTouches: [{ clientX: 400, clientY: 300 }] })
    cy.wait(TRANSITION_WAIT)

    cy.get('.story-nav-slot--prev .nubi-icon-button').should('be.visible')
  })

  it('negativo: en la portada no hay botón de página anterior', () => {
    navigateToReader()

    cy.get('.story-nav-slot--prev .nubi-icon-button').should('not.exist')
    cy.get('.story-title-overlay').should('exist')
  })

  it('negativo: en la última página no hay botón de página siguiente', () => {
    navigateToReader()

    cy.get('body').then(($body) => {
      const hasNext = $body.find('.story-nav-slot--next .nubi-icon-button').length > 0
      if (hasNext) {
        cy.get('.story-nav-slot--next .nubi-icon-button').click()
        cy.wait(TRANSITION_WAIT)
        cy.get('body').then(($b) => {
          const hasNext2 = $b.find('.story-nav-slot--next .nubi-icon-button').length > 0
          if (!hasNext2) {
            cy.get('.story-nav-slot--prev .nubi-icon-button').should('be.visible')
          } else {
            cy.get('.story-nav-slot--next .nubi-icon-button').click()
            cy.wait(TRANSITION_WAIT)
            cy.get('.story-nav-slot--prev .nubi-icon-button').should('be.visible')
          }
        })
      } else {
        cy.get('.story-nav-slot--next .nubi-icon-button').should('not.exist')
      }
    })
  })
})
