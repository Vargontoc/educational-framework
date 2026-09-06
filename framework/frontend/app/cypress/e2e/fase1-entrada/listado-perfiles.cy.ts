import { createFamilyViaApi, createChildViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'

describe('Listado de perfiles (SPRINT-012)', () => {
  it('tras dar de alta perfiles, el listado los muestra con nombre', () => {
    createFamilyViaApi({ name: uniqueFamilyName('List'), pin: PIN })
    createChildViaApi({ name: 'Marco' })
    createChildViaApi({ name: 'Sofía' })

    cy.visit('/')
    cy.contains('button', /Bienvenida familia/, { timeout: 8000 }).should('be.visible').click()
    cy.contains('Marco').should('be.visible')
    cy.contains('Sofía').should('be.visible')
  })

  it('con cero perfiles, muestra estado vacio comprensible', () => {
    createFamilyViaApi({ name: uniqueFamilyName('Empty'), pin: PIN })

    cy.visit('/')
    cy.contains('button', /Bienvenida familia/, { timeout: 8000 }).should('be.visible').click()

    cy.get('.nubi-info-modal, .home-view__content').should('exist')
    cy.get('.home-view').should('be.visible')
    cy.contains('button', /Bienvenida familia|Registrar niño/).should('exist')
  })
})
