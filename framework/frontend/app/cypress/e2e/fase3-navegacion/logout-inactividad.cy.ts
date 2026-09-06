import { createFamilyViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'
const INACTIVITY_TIMEOUT = 180000

describe('Logout automatico por inactividad (SPRINT-016)', () => {
  before(() => {
    createFamilyViaApi({ name: uniqueFamilyName('Inact'), pin: PIN })
  })

  beforeEach(() => {
    cy.viewport(1280, 800)
  })

  it('positivo: tras 3 minutos sin actividad, overlay de inactividad cierra sesion', () => {
    cy.clock()

    cy.visit('/')
    cy.tick(500)
    cy.get('.home-view').should('exist')
    cy.get('[aria-label="Configuración"]').should('be.visible').click()
    cy.tick(500)
    cy.contains('Acceso parental').should('be.visible')
    cy.get('.nubi-pin-input__digit').first().type(PIN)
    cy.tick(1000)
    cy.url().should('include', '/panel')

    cy.tick(500)
    cy.get('.parent-sidebar', { timeout: 10000 }).should('be.visible')

    cy.intercept('POST', '**/api/v1/auth/logout').as('logout')

    cy.tick(INACTIVITY_TIMEOUT)

    cy.get('.inactivity-overlay', { timeout: 5000 }).should('be.visible')
    cy.contains('Tu sesión ha finalizado por inactividad').should('be.visible')

    cy.tick(5000)

    cy.wait('@logout')
    cy.location('pathname').should('eq', '/')
  })

  it('negativo: actividad del usuario antes del umbral reinicia el contador', () => {
    cy.clock()

    cy.visit('/')
    cy.tick(500)
    cy.get('.home-view').should('exist')
    cy.get('[aria-label="Configuración"]').should('be.visible').click()
    cy.tick(500)
    cy.contains('Acceso parental').should('be.visible')
    cy.get('.nubi-pin-input__digit').first().type(PIN)
    cy.tick(1000)
    cy.url().should('include', '/panel')

    cy.tick(500)
    cy.get('.parent-sidebar', { timeout: 10000 }).should('be.visible')

    cy.tick(120000)

    cy.get('.parent-panel-layout__content').click()

    cy.tick(120000)

    cy.get('.inactivity-overlay').should('not.exist')
  })
})
