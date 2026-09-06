import { createFamilyViaApi, createChildViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'

describe('HomeView — accesos iniciales y error API (SPRINT-010)', () => {
  it('con familia y perfiles, muestra bienvenida y abre seleccion de perfiles', () => {
    const familyName = uniqueFamilyName('Home')
    createFamilyViaApi({ name: familyName, pin: PIN })
    createChildViaApi({ name: 'Lucía' })

    cy.visit('/')
    cy.contains('button', new RegExp(`Bienvenida familia`), { timeout: 8000 })
      .should('be.visible')
      .click()

    cy.get('.child-selection-modal__content, .nubi-info-modal', { timeout: 8000 })
      .should('be.visible')
    cy.contains('Lucía').should('be.visible')
  })

  it('con familia, muestra acceso a configuracion (ajustes)', () => {
    createFamilyViaApi({ name: uniqueFamilyName('HomeCfg'), pin: PIN })

    cy.visit('/')
    cy.get('[aria-label="Configuración"]', { timeout: 8000 }).should('be.visible')
  })

  it('con API de familia fallando (500), muestra estado de error con boton Reintentar', () => {
    createFamilyViaApi({ name: uniqueFamilyName('HomeErr'), pin: PIN })

    cy.intercept('GET', '**/api/v1/family', {
      statusCode: 500,
      body: { success: false, message: 'Internal Server Error', errors: [], data: null }
    }).as('familyError')

    cy.visit('/')
    cy.wait('@familyError')

    cy.get('.home-view__error, [role="alert"]', { timeout: 8000 }).should('exist')
    cy.contains('No se pudo cargar', { timeout: 8000 }).should('be.visible')
    cy.contains('button', 'Reintentar').should('be.visible')
  })
})
