import { createFamilyViaApi, createChildViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'
const GAME_TIMEOUT = 20000

describe('WorldMapScene — paisaje placeholder y desplazamiento (SPRINT-063)', () => {
  let childId: number

  before(() => {
    createFamilyViaApi({ name: uniqueFamilyName('F7Map'), pin: PIN })
    createChildViaApi({ name: 'Mia', birthday: '2022-06-20', avatar: 'avatar-02' })
      .then((res) => { childId = res.body.data.id })
  })

  beforeEach(() => {
    cy.intercept('POST', '**/api/v1/sessions/children').as('openSession')
  })

  it('positivo: WorldMapScene es alcanzada tras la carga, sin controles ni marcadores de progreso', () => {
    cy.selectChildProfile('Mia')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })

    cy.get('canvas').should('exist')
    cy.get('button', { timeout: 2000 }).should('not.exist')
  })

  it('positivo: la escena world-map queda registrada junto al resto del flujo de juego', () => {
    cy.selectChildProfile('Mia')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      const keys: string[] = state.sceneKeys
      expect(keys).to.include('loading')
      expect(keys).to.include('world-map')
      expect(keys).to.include('farewell')
      expect(keys).to.include('orientation-required')
    })
  })

  it('negativo: no se abre ningún minijuego sin evento WORLD_ACTIVITY_STARTED', () => {
    cy.selectChildProfile('Mia')
    cy.visit(`/game/${childId}`)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })

    cy.wait(500)

    cy.window().should((win) => {
      const state = (win as any).__NUBI_GAME_STATE__
      expect(state.activeScene).to.eq('world-map')
    })
  })
})
