import { createFamilyViaApi, createChildViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'

function loginAndGoToEdit(childName: string) {
  cy.visit('/')
  cy.get('.home-view').should('exist')
  cy.get('[aria-label="Configuración"]').should('be.visible').click()
  cy.contains('Acceso parental').should('be.visible')
  cy.get('.nubi-pin-input__digit').first().type(PIN)
  cy.url().should('include', '/panel')
  cy.contains('Panel parental').should('be.visible')
  cy.get('.panel-cover-view__card').contains('Niños').click({ force: true })
  cy.url().should('include', '/panel/ninos')

  cy.intercept('GET', '**/api/v1/family/children/*').as('getChild')
  cy.intercept('GET', '**/api/v1/family').as('getFamily')

  cy.contains('.parental-child-card', childName).click({ force: true })
  cy.url().should('include', '/panel/ninos/')
  cy.wait('@getChild')
  cy.get('.child-profile-edit-view').should('be.visible')
}

describe('Selector de accesibilidad cromatica (SPRINT-030)', () => {
  it('la seccion de accesibilidad visual existe en la edicion de perfil', () => {
    const familyName = uniqueFamilyName('Color')
    createFamilyViaApi({ name: familyName, pin: PIN })
    createChildViaApi({ name: 'Ana', birthday: '2022-03-15', avatar: 'avatar-1' })

    loginAndGoToEdit('Ana')

    cy.contains('Accesibilidad visual').should('be.visible')
    cy.contains('Activar ajuste visual').should('be.visible')
  })

  it('sin cambios, el boton guardar esta deshabilitado', () => {
    const familyName = uniqueFamilyName('ColorNone')
    createFamilyViaApi({ name: familyName, pin: PIN })
    createChildViaApi({ name: 'Bruno', birthday: '2022-07-01', avatar: 'avatar-1' })

    loginAndGoToEdit('Bruno')

    cy.contains('button', 'Guardar cambios').should('be.disabled')
  })
})
