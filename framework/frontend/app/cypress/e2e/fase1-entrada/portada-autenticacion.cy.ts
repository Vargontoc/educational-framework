import { createFamilyViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'

describe('Portada neutral y autenticacion (SPRINT-014)', () => {
  before(() => {
    createFamilyViaApi({ name: uniqueFamilyName('Auth'), pin: PIN })
  })

  it('PIN correcto navega al panel parental', () => {
    cy.visit('/')
    cy.get('.home-view').should('exist')
    cy.get('[aria-label="Configuración"]').should('be.visible').click()
    cy.contains('Acceso parental').should('be.visible')

    cy.get('.nubi-pin-input__digit').first().type(PIN)

    cy.url().should('include', '/panel')
    cy.contains('Panel parental').should('be.visible')
  })

  it('PIN incorrecto muestra error y no navega', () => {
    cy.visit('/')
    cy.get('.home-view').should('exist')
    cy.get('[aria-label="Configuración"]').should('be.visible').click()
    cy.contains('Acceso parental').should('be.visible')

    cy.get('.nubi-pin-input__digit').first().type('9999')

    cy.contains('PIN incorrecto').should('be.visible')
    cy.url().should('not.include', '/panel')

    cy.contains('button', 'Cancelar').click()
    cy.contains('Acceso parental').should('not.exist')
  })
})
