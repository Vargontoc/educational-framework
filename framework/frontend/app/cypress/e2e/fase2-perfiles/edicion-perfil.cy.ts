import { createFamilyViaApi, createChildViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'

function loginAndGoToNinos() {
  cy.visit('/')
  cy.get('.home-view').should('exist')
  cy.get('[aria-label="Configuración"]').should('be.visible').click()
  cy.contains('Acceso parental').should('be.visible')
  cy.get('.nubi-pin-input__digit').first().type(PIN)
  cy.url().should('include', '/panel')
  cy.contains('Panel parental').should('be.visible')
  cy.get('.panel-cover-view__card').contains('Niños').click({ force: true })
  cy.url().should('include', '/panel/ninos')
}

describe('Edicion de perfil individual (SPRINT-028)', () => {
  it('editar nombre persiste los cambios via PATCH', () => {
    const familyName = uniqueFamilyName('Edit')
    const childOriginalName = `Laura-${Date.now()}`
    const childNewName = `Laura María-${Date.now()}`
    createFamilyViaApi({ name: familyName, pin: PIN })
    createChildViaApi({ name: childOriginalName, birthday: '2022-01-10', avatar: 'avatar-1' })

    loginAndGoToNinos()
    cy.contains('.parental-child-card__name', childOriginalName).should('be.visible')

    cy.intercept('GET', '**/api/v1/family/children/*').as('getChild')
    cy.intercept('GET', '**/api/v1/family').as('getFamily')

    cy.contains('.parental-child-card', childOriginalName).click({ force: true })
    cy.url().should('include', '/panel/ninos/')
    cy.wait('@getChild')
    cy.get('.child-profile-edit-view').should('be.visible')

    cy.intercept('PATCH', '**/api/v1/family/children/*').as('updateChild')

    cy.get('.nubi-text-input input').clear()
    cy.get('.nubi-text-input input').type(childNewName)

    cy.contains('button', 'Guardar cambios').should('be.enabled').click({ force: true })
    cy.wait('@updateChild').its('request.body').then((body) => {
      expect(body.name).to.eq(childNewName)
    })
  })

  it('la vista de edicion muestra los datos basicos del perfil', () => {
    const familyName = uniqueFamilyName('EditView')
    createFamilyViaApi({ name: familyName, pin: PIN })
    createChildViaApi({ name: 'Pedro', birthday: '2022-05-01', avatar: 'avatar-1' })

    loginAndGoToNinos()

    cy.intercept('GET', '**/api/v1/family/children/*').as('getChild')
    cy.intercept('GET', '**/api/v1/family').as('getFamily')

    cy.contains('.parental-child-card', 'Pedro').click({ force: true })
    cy.url().should('include', '/panel/ninos/')
    cy.wait('@getChild')
    cy.get('.child-profile-edit-view').should('be.visible')

    cy.get('.nubi-text-input input').should('have.value', 'Pedro')
    cy.contains('Datos básicos').should('be.visible')
    cy.contains('Audio del NPC').should('be.visible')
    cy.contains('Accesibilidad visual').should('be.visible')
  })
})
