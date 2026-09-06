import { createFamilyViaApi, createChildViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'
const GAME_TIMEOUT = 20000

describe('Cambios dinámicos de preferencias y errores (SPRINT-047)', () => {
  let childId: number

  before(() => {
    createFamilyViaApi({ name: uniqueFamilyName('F7Pref'), pin: PIN })
    createChildViaApi({ name: 'Elsa', birthday: '2022-09-05', avatar: 'avatar-02' })
      .then((res) => { childId = res.body.data.id })
  })

  beforeEach(() => {
    cy.intercept('POST', '**/api/v1/sessions/children').as('openSession')
  })

  it('positivo: activación de TTS durante la sesión se refleja en el estado de Phaser', () => {
    cy.selectChildProfile('Elsa')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('base-state')
    })

    cy.window().then((win) => {
      ;(win as any).__NUBI_GAME_STATE__.injectWsEvent({
        event: 'CHILD_TTS_ACTIVATED',
        sessionId: 0,
        payload: null
      })
    })

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.ttsEnabled).to.eq(true)
    })
  })

  it('positivo: desactivación de NPC durante la sesión se refleja en el estado de Phaser', () => {
    cy.selectChildProfile('Elsa')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('base-state')
    })

    cy.window().then((win) => {
      ;(win as any).__NUBI_GAME_STATE__.injectWsEvent({
        event: 'CHILD_AGENT_DEACTIVATED',
        sessionId: 0,
        payload: null
      })
    })

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.npcEnabled).to.eq(false)
    })
  })

  it('negativo: GAME_ERROR recuperable no interrumpe la experiencia', () => {
    cy.selectChildProfile('Elsa')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('base-state')
    })

    cy.window().then((win) => {
      ;(win as any).__NUBI_GAME_STATE__.injectWsEvent({
        event: 'GAME_ERROR',
        sessionId: 0,
        payload: { errorCode: 'TEMPORARY_FAILURE', message: 'temp error' }
      })
    })

    cy.wait(1000)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('base-state')
    })
  })

  it('negativo: GAME_ERROR crítico aplica flujo de pérdida de conexión', () => {
    cy.selectChildProfile('Elsa')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('base-state')
    })

    cy.window().then((win) => {
      ;(win as any).__NUBI_GAME_STATE__.injectWsEvent({
        event: 'GAME_ERROR',
        sessionId: 0,
        payload: { errorCode: 'SESSION_NOT_FOUND', message: 'critical' }
      })
    })

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('farewell')
    })

    cy.url({ timeout: 12000 }).should('not.include', '/game/')
  })

  it('flujo sin audio/NPC: GameView funciona sin requerir audio ni NPC', () => {
    cy.selectChildProfile('Elsa')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('base-state')
      expect(state.npcEnabled).to.be.a('boolean')
      expect(state.ttsEnabled).to.be.a('boolean')
    })
  })
})
