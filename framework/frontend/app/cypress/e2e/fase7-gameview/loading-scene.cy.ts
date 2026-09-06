import { createFamilyViaApi, createChildViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'
const GAME_TIMEOUT = 20000

describe('LoadingScene — placeholder de carga (SPRINT-042)', () => {
  let childId: number

  before(() => {
    createFamilyViaApi({ name: uniqueFamilyName('F7Load'), pin: PIN })
    createChildViaApi({ name: 'Leo', birthday: '2022-03-15', avatar: 'avatar-01' })
      .then((res) => { childId = res.body.data.id })
  })

  beforeEach(() => {
    cy.intercept('POST', '**/api/v1/sessions/children').as('openSession')
  })

  it('positivo: LoadingScene se muestra sin porcentaje y transiciona a BaseStateScene', () => {
    cy.selectChildProfile('Leo')
    cy.visit(`/game/${childId}`)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state).to.exist
      expect(state.activeScene).to.eq('loading')
    })

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('base-state')
    })
  })

  it('negativo: si la apertura de sesión falla, no se alcanza BaseStateScene', () => {
    cy.intercept('POST', '**/api/v1/sessions/children', {
      statusCode: 200,
      body: { success: false, message: 'Error', errors: [], data: null }
    }).as('failedSession')

    cy.selectChildProfile('Leo')
    cy.visit(`/game/${childId}`)

    cy.url({ timeout: GAME_TIMEOUT }).should('not.include', '/game/')
  })
})
