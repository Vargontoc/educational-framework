import { createFamilyViaApi, createChildViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'
const GAME_TIMEOUT = 20000
const FAREWELL_TIMEOUT = 12000

describe('Recuperación de conexión y escena de despedida (SPRINT-046)', () => {
  let childId: number

  before(() => {
    createFamilyViaApi({ name: uniqueFamilyName('F7Rec'), pin: PIN })
    createChildViaApi({ name: 'Dani', birthday: '2022-07-01', avatar: 'avatar-01' })
      .then((res) => { childId = res.body.data.id })
  })

  beforeEach(() => {
    cy.intercept('POST', '**/api/v1/sessions/children').as('openSession')
  })

  it('positivo: pérdida de conexión que se recupera continúa sin mostrar despedida', () => {
    cy.selectChildProfile('Dani')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('base-state')
    })

    cy.window().then((win) => {
      ;(win as any).__NUBI_GAME_STATE__.closeWs()
    })

    cy.wait(8000)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('base-state')
    })
  })

  it('negativo: pérdida de conexión sin recuperación muestra despedida y vuelve a Home', () => {
    cy.selectChildProfile('Dani')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('base-state')
    })

    cy.window().then((win) => {
      ;(win as any).__NUBI_GAME_STATE__.injectWsEvent({
        event: 'SESSION_EXPIRED',
        sessionId: 0,
        payload: null
      })
    })

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('farewell')
    })

    cy.url({ timeout: FAREWELL_TIMEOUT }).should('not.include', '/game/')
  })

  it('recarga de página en GameView recupera la sesión y continúa', () => {
    cy.selectChildProfile('Dani')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('base-state')
    })

    cy.reload()

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state).to.exist
      expect(state.activeScene).to.eq('base-state')
    })
  })
})
