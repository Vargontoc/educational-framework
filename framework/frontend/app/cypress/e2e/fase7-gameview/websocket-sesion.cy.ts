import { createFamilyViaApi, createChildViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'
const GAME_TIMEOUT = 20000
const WS_OPEN = 1

describe('Señal de actividad WebSocket y expulsión (SPRINT-045)', () => {
  let childId: number

  before(() => {
    createFamilyViaApi({ name: uniqueFamilyName('F7Ws'), pin: PIN })
    createChildViaApi({ name: 'Ana', birthday: '2022-01-10', avatar: 'avatar-03' })
      .then((res) => { childId = res.body.data.id })
  })

  beforeEach(() => {
    cy.intercept('POST', '**/api/v1/sessions/children').as('openSession')
  })

  it('positivo: la señal de actividad WebSocket se mantiene durante GameView', () => {
    cy.selectChildProfile('Ana')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('base-state')
      expect(state.wsReadyState).to.eq(WS_OPEN)
    })
  })

  it('negativo: un evento de expulsión saca al niño de GameView de forma controlada', () => {
    cy.selectChildProfile('Ana')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('base-state')
    })

    cy.window().then((win) => {
      ;(win as any).__NUBI_GAME_STATE__.injectWsEvent({
        event: 'CHILD_EXPELLED',
        sessionId: 0,
        payload: null
      })
    })

    cy.url({ timeout: GAME_TIMEOUT }).should('not.include', '/game/')
  })
})
