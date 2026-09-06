import { createFamilyViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'

function loginAndGoToConfig() {
  cy.visit('/')
  cy.get('.home-view').should('exist')
  cy.get('[aria-label="Configuración"]').should('be.visible').click()
  cy.contains('Acceso parental').should('be.visible')
  cy.get('.nubi-pin-input__digit').first().type(PIN)
  cy.url().should('include', '/panel')
  cy.contains('Panel parental').should('be.visible')
  cy.get('.panel-cover-view__card').contains('Configuración').click({ force: true })
  cy.url().should('include', '/panel/configuracion')
  cy.get('.configuracion-view').should('be.visible')
}

describe('Configuracion global — audio, NPC, voces, PIN (SPRINT-023/024/025)', () => {
  it('la vista de configuracion carga las 5 secciones y el boton guardar', () => {
    const familyName = uniqueFamilyName('Config')
    createFamilyViaApi({ name: familyName, pin: PIN })

    cy.intercept('GET', '**/api/v1/family').as('getFamily')

    loginAndGoToConfig()
    cy.wait('@getFamily')

    cy.contains('.config-section', 'Audio general').should('be.visible')
    cy.contains('.config-section', 'NPC').should('be.visible')
    cy.contains('.config-section', 'Voz del NPC').should('be.visible')
    cy.contains('.config-section', 'Voz narrativa').should('be.visible')
    cy.contains('.config-section', 'PIN familiar').should('be.visible')
    cy.contains('button', 'Guardar cambios').should('exist')
  })

  it('PIN con confirmacion que no coincide bloquea el guardado', () => {
    const familyName = uniqueFamilyName('ConfigPin')
    createFamilyViaApi({ name: familyName, pin: PIN })

    cy.intercept('GET', '**/api/v1/family').as('getFamily')

    loginAndGoToConfig()
    cy.wait('@getFamily')

    cy.contains('.config-section', 'PIN familiar').within(() => {
      cy.get('.nubi-pin-input').eq(0).find('.nubi-pin-input__digit').first().type('5678')
      cy.get('.nubi-pin-input').eq(1).find('.nubi-pin-input__digit').first().type('9999')
    })

    cy.contains('button', 'Guardar cambios').should('be.disabled')
  })

  it('sin cambios, el boton guardar esta deshabilitado', () => {
    const familyName = uniqueFamilyName('ConfigNav')
    createFamilyViaApi({ name: familyName, pin: PIN })

    cy.intercept('GET', '**/api/v1/family').as('getFamily')

    loginAndGoToConfig()
    cy.wait('@getFamily')

    cy.contains('button', 'Guardar cambios').should('be.disabled')
  })
})
