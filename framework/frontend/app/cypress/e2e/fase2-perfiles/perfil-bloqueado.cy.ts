import { createFamilyViaApi, createChildViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'

describe('Perfil bloqueado — aviso neutral desde HomeView (SPRINT-041)', () => {
  it('seleccionar un perfil habilitado navega a GameView', () => {
    const familyName = uniqueFamilyName('BlockOk')
    createFamilyViaApi({ name: familyName, pin: PIN })
    createChildViaApi({ name: 'Carla', birthday: '2022-04-10', avatar: 'avatar-1' })

    cy.visit('/')
    cy.contains('button', /Bienvenida familia/, { timeout: 8000 })
      .should('be.visible')
      .click()

    cy.get('.child-selection-modal__content, .nubi-info-modal', { timeout: 8000 })
      .should('be.visible')

    cy.intercept('POST', '**/api/v1/sessions/children').as('openSession')

    cy.contains('.child-profile-card__name', 'Carla')
      .should('be.visible')
      .click({ force: true })

    cy.wait('@openSession')
    cy.url().should('include', '/game/')
  })

  it('seleccionar un perfil bloqueado muestra aviso neutral y permanece en HomeView', () => {
    const familyName = uniqueFamilyName('BlockNo')
    createFamilyViaApi({ name: familyName, pin: PIN })
    createChildViaApi({ name: 'Dani', birthday: '2022-08-20', avatar: 'avatar-1' })

    cy.intercept('POST', '**/api/v1/sessions/children', {
      statusCode: 200,
      body: { success: false, message: 'Profile blocked', errors: [], data: null }
    }).as('blockedSession')

    cy.visit('/')
    cy.contains('button', /Bienvenida familia/, { timeout: 8000 })
      .should('be.visible')
      .click()

    cy.get('.child-selection-modal__content, .nubi-info-modal', { timeout: 8000 })
      .should('be.visible')

    cy.contains('.child-profile-card__name', 'Dani')
      .should('be.visible')
      .click({ force: true })

    cy.wait('@blockedSession')

    cy.contains('Este perfil no puede jugar ahora', { timeout: 8000 })
      .should('be.visible')

    cy.url().should('not.include', '/game/')

    cy.contains('button', 'Cerrar').click({ force: true })
    cy.contains('Este perfil no puede jugar ahora').should('not.exist')
  })
})
