import { createFamilyViaApi, createChildViaApi, uniqueFamilyName } from '../../support/testData'
import { ResponsiveLayout, MEMORY_CARD_ASPECT } from '../../../src/game/utils/ResponsiveLayout'
import { profileFor } from '../../../src/game/utils/DeviceProfile'

const PIN = '1234'
const GAME_TIMEOUT = 20000
const GAME_W = 1280
const GAME_H = 720
const MIN_TOUCH_CSS = 44
const AUDIO_ID = 'memory-prompt-1'

// Mismas resoluciones objetivo que SPRINT-080 (movil/tablet en apaisado: el minijuego solo se juega asi).
const TARGETS = [
  { name: 'movil pequeno 320x568', width: 568, height: 320, coarse: true },
  { name: 'movil estandar 375x667', width: 667, height: 375, coarse: true },
  { name: 'tablet 768x1024', width: 1024, height: 768, coarse: true },
  { name: 'desktop 1920x1080', width: 1920, height: 1080, coarse: false }
]

const BOARDS = [
  { name: 'EASY 2x2', rows: 2, columns: 2, delay: 2000 },
  { name: 'MEDIUM 2x3', rows: 2, columns: 3, delay: 1500 },
  { name: 'HARD 2x4', rows: 2, columns: 4, delay: 1000 }
]

interface Box { x: number, y: number, w: number, h: number }
const overlaps = (a: Box, b: Box) => a.x < b.x + b.w && b.x < a.x + a.w && a.y < b.y + b.h && b.y < a.y + a.h

// Logica pura: no necesita backend ni navegador de la app.
describe('ResponsiveLayout — rejilla del juego de memoria (SPRINT-084)', () => {
  TARGETS.forEach((target) => {
    BOARDS.forEach((board) => {
      it(`${target.name} ${board.name}: cartas tactiles, dentro de la pantalla y sin solapes`, () => {
        const canvasCssWidth = Math.min(target.width, target.height * 16 / 9)
        const displayScale = GAME_W / canvasCssWidth
        const profile = profileFor({ shortSide: Math.min(target.width, target.height), coarsePointer: target.coarse })
        const layout = new ResponsiveLayout(profile)
        const sizes = layout.calculateSizes({ viewportWidth: GAME_W, viewportHeight: GAME_H, displayScale })
        const grid = layout.getMemoryGrid(board.rows, board.columns)

        expect(grid.cardWidth / grid.cardHeight).to.be.closeTo(MEMORY_CARD_ASPECT, 0.0001)
        expect(grid.cardWidth / displayScale, 'ancho tactil de la carta (px CSS)').to.be.at.least(MIN_TOUCH_CSS)

        const boxes: Box[] = []
        for (let r = 0; r < board.rows; r++) {
          for (let c = 0; c < board.columns; c++) {
            const p = ResponsiveLayout.memoryCardCenter(grid, r, c)
            boxes.push({ x: p.x - grid.cardWidth / 2, y: p.y - grid.cardHeight / 2, w: grid.cardWidth, h: grid.cardHeight })
          }
        }

        const nubi: Box = {
          x: GAME_W - sizes.nubiMargin - sizes.nubiSize, y: GAME_H - sizes.nubiMargin - sizes.nubiSize,
          w: sizes.nubiSize, h: sizes.nubiSize
        }
        const bar: Box = {
          x: (GAME_W - sizes.progressBarWidth) / 2, y: sizes.progressBarTop, w: sizes.progressBarWidth, h: sizes.progressBarHeight
        }
        boxes.forEach((box, i) => {
          expect(box.x, `carta ${i} dentro por la izquierda`).to.be.at.least(0)
          expect(box.x + box.w, `carta ${i} dentro por la derecha`).to.be.at.most(GAME_W)
          expect(box.y, `carta ${i} dentro por arriba`).to.be.at.least(0)
          expect(box.y + box.h, `carta ${i} dentro por abajo`).to.be.at.most(GAME_H)
          expect(overlaps(box, nubi), `carta ${i} no tapa a Nubi`).to.eq(false)
          expect(overlaps(box, bar), `carta ${i} no tapa la barra de progreso`).to.eq(false)
          boxes.slice(i + 1).forEach((other, j) => {
            expect(overlaps(box, other), `cartas ${i} y ${i + j + 1} no se solapan`).to.eq(false)
          })
        })

        // la rejilla queda centrada
        const minX = Math.min(...boxes.map(b => b.x))
        const maxX = Math.max(...boxes.map(b => b.x + b.w))
        expect((minX + maxX) / 2).to.be.closeTo(GAME_W / 2, 0.5)
      })
    })
  })
})

const state = (win: Cypress.AUTWindow) => (win as any).__NUBI_GAME_STATE__

const ELEMENTS = [
  { id: '1', code: 'banana', displayValue: 'Platano', resourceRefs: { 'nubi-audio': 'Encuentra las parejas', image: 'banana' } },
  { id: '2', code: 'pear', displayValue: 'Pera', resourceRefs: { 'nubi-audio': 'Encuentra las parejas', image: 'pear' } },
  { id: '3', code: 'orange', displayValue: 'Naranja', resourceRefs: { 'nubi-audio': 'Encuentra las parejas', image: 'orange' } }
]

type CardSpec = { elementId: string, faceUp?: boolean, matched?: boolean }

/** Estado del tablero como lo envia el backend: el elemento solo va en las cartas boca arriba o emparejadas. */
function memoryState(rows: number, columns: number, cards: CardSpec[], extra: Record<string, unknown> = {}) {
  const matchedCards = cards.filter(c => c.matched).length
  return {
    rows,
    columns,
    totalPairs: cards.length / 2,
    matchedPairs: matchedCards / 2,
    flipBackDelayMs: 1000,
    waitingForFlipBack: false,
    flipBackCardIds: [] as string[],
    cards: cards.map((c, i) => ({
      cardId: `card-${i}`,
      row: Math.floor(i / columns),
      column: i % columns,
      faceUp: !!c.faceUp || !!c.matched,
      matched: !!c.matched,
      elementId: c.faceUp || c.matched ? c.elementId : null
    })),
    elements: ELEMENTS,
    ...extra
  }
}

const payload = (memory: unknown, status = 'IN_PROGRESS') => ({
  engine: 'MEMORY', activityId: 1, gameId: 1, difficultyLevelId: 1, status, memoryState: memory
})

const gameReady = (memory: unknown) => ({ event: 'GAME_READY', sessionId: 1, payload: payload(memory) })

const actionResult = (resultType: string, memory: unknown, gameCompleted = false) => ({
  event: 'GAME_ACTION_RESULT',
  sessionId: 1,
  payload: {
    resultType, gameCompleted, difficultyChanged: false, newDifficultyLevelId: 1, attemptContext: '{}',
    updatedState: payload(memory, gameCompleted ? 'COMPLETED' : 'IN_PROGRESS')
  }
})

// Tablero 2x2 con la disposicion [banana, pera, pera, banana]
const LAYOUT_2X2: CardSpec[] = [{ elementId: '1' }, { elementId: '2' }, { elementId: '2' }, { elementId: '1' }]
const with2x2 = (patch: Record<number, Partial<CardSpec>>): CardSpec[] =>
  LAYOUT_2X2.map((c, i) => ({ ...c, ...(patch[i] ?? {}) }))

function openMemoryScene(childId: number, prefs: { npc: boolean, audio: boolean } = { npc: true, audio: true }) {
  cy.selectChildProfile('Nubi')
  cy.visit(`/game/${childId}`)
  cy.window({ timeout: GAME_TIMEOUT }).should((win) => expect(state(win).activeScene).to.eq('world-map'))
  cy.window().then((win) => {
    const s = state(win)
    s.setPreference('npcEnabled', prefs.npc)
    s.setPreference('ttsEnabled', prefs.audio)
    s.setPreference('audioGeneralEnabled', prefs.audio)
    s.spyDynamicAudio()
    s.startMemoryScene()
  })
  cy.window({ timeout: GAME_TIMEOUT }).should((win) => expect(state(win).activeScene).to.eq('memory-game'))
  cy.window().then((win) => state(win).spyWsSend())
}

const board = (win: Cypress.AUTWindow) => state(win).getMemoryData()
const card = (win: Cypress.AUTWindow, id: string) => board(win).cards.find((c: any) => c.cardId === id)
const sent = (win: Cypress.AUTWindow): string[] => (win as any).__NUBI_SENT__

function readyWith(memory: unknown) {
  cy.window().then((win) => state(win).injectWsEvent(gameReady(memory)))
  cy.window({ timeout: GAME_TIMEOUT }).should((win) => expect(board(win).cards.length).to.be.greaterThan(0))
  // hasta que el tablero se pinta las cartas no se pueden tocar
  cy.window({ timeout: GAME_TIMEOUT }).should((win) => expect(board(win).blockActions).to.eq(false))
}

describe('MemoryGameScene — tablero y volteo de cartas (SPRINT-084)', () => {
  let childId: number

  before(() => {
    createFamilyViaApi({ name: uniqueFamilyName('F7Memory'), pin: PIN })
    createChildViaApi({ name: 'Nubi', birthday: '2022-03-15', avatar: 'avatar-01' })
      .then((res) => { childId = res.body.data.id })
  })

  it('la escena memory-game esta registrada', () => {
    openMemoryScene(childId)
    cy.window().should((win) => expect(state(win).sceneKeys).to.include('memory-game'))
  })

  BOARDS.forEach((b) => {
    it(`tablero ${b.name}: ${b.rows}x${b.columns} cartas boca abajo, ordenadas por fila y columna`, () => {
      openMemoryScene(childId)
      const cards: CardSpec[] = Array.from({ length: b.rows * b.columns }, (_, i) => ({ elementId: String((i % 3) + 1) }))
      readyWith(memoryState(b.rows, b.columns, cards, { flipBackDelayMs: b.delay }))

      cy.window().should((win) => {
        const data = board(win)
        expect(data.rows).to.eq(b.rows)
        expect(data.columns).to.eq(b.columns)
        expect(data.cards).to.have.length(b.rows * b.columns)
        data.cards.forEach((c: any) => {
          expect(c.shown, `${c.cardId} boca abajo`).to.eq(false)
          expect(c.coverVisible).to.eq(true)
          expect(c.faceVisible).to.eq(false)
          expect(c.elementId, 'no se revela lo que hay debajo').to.eq(null)
          expect(c.inputEnabled).to.eq(true)
        })
        const rows = new Set(data.cards.map((c: any) => c.y.toFixed(1)))
        const cols = new Set(data.cards.map((c: any) => c.x.toFixed(1)))
        expect(rows.size).to.eq(b.rows)
        expect(cols.size).to.eq(b.columns)
      })
    })
  })

  it('las texturas de las cartas y de los elementos del tablero se cargan bajo demanda', () => {
    openMemoryScene(childId)
    readyWith(memoryState(2, 2, LAYOUT_2X2))

    cy.window().should((win) => {
      ;['memory-card-cover', 'memory-card-reverse', 'banana', 'pear'].forEach(k =>
        expect(state(win).textureExists(k), k).to.eq(true))
      expect(state(win).textureExists('cherry'), 'solo los elementos del tablero').to.eq(false)
    })
  })

  it('al tocar una carta se envia la accion con su cardId y no se admiten mas toques hasta la respuesta', () => {
    openMemoryScene(childId)
    readyWith(memoryState(2, 2, LAYOUT_2X2))

    cy.window().then((win) => {
      state(win).tapCard('card-1')
      state(win).tapCard('card-2')
    })
    cy.window().should((win) => {
      const actions = sent(win).map(s => JSON.parse(s)).filter(m => m.type === 'game_action')
      expect(actions).to.have.length(1)
      const action = JSON.parse(actions[0].action)
      expect(action.cardId).to.eq('card-1')
      expect(action.responseTimeMs).to.be.a('number')
    })
  })

  it('la carta se voltea con animacion y muestra su elemento al llegar el estado del servidor', () => {
    openMemoryScene(childId)
    readyWith(memoryState(2, 2, LAYOUT_2X2))

    cy.window().then((win) => {
      state(win).tapCard('card-0')
      state(win).injectWsEvent(actionResult('CORRECT', memoryState(2, 2, with2x2({ 0: { faceUp: true } }))))
    })

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const c = card(win, 'card-0')
      expect(c.shown).to.eq(true)
      expect(c.faceVisible).to.eq(true)
      expect(c.coverVisible).to.eq(false)
      expect(c.contentKey).to.eq('banana')
      expect(c.scaleX, 'el volteo termina con la carta entera').to.be.closeTo(1, 0.001)
      expect(card(win, 'card-1').shown).to.eq(false)
      expect(board(win).blockActions).to.eq(false)
    })
  })

  it('una pareja que coincide permanece boca arriba', () => {
    openMemoryScene(childId)
    readyWith(memoryState(2, 2, LAYOUT_2X2))
    cy.window().then((win) => state(win).injectWsEvent(actionResult('CORRECT', memoryState(2, 2, with2x2({ 0: { faceUp: true } })))))
    cy.window({ timeout: GAME_TIMEOUT }).should((win) => expect(card(win, 'card-0').shown).to.eq(true))

    cy.window().then((win) => state(win).injectWsEvent(
      actionResult('CORRECT', memoryState(2, 2, with2x2({ 0: { matched: true }, 3: { matched: true } })))))

    cy.wait(1800) // mas que cualquier tiempo de visibilidad
    cy.window().should((win) => {
      ;['card-0', 'card-3'].forEach(id => {
        expect(card(win, id).matched).to.eq(true)
        expect(card(win, id).shown, id).to.eq(true)
        expect(card(win, id).faceVisible).to.eq(true)
      })
      expect(card(win, 'card-1').shown).to.eq(false)
    })
    // una carta emparejada ya no hace nada al tocarla
    cy.window().then((win) => {
      const before = sent(win).length
      state(win).tapCard('card-0')
      expect(sent(win).length).to.eq(before)
    })
  })

  it('una pareja que no coincide se voltea sola tras el tiempo de visibilidad, sin bloquear el juego', () => {
    openMemoryScene(childId)
    readyWith(memoryState(2, 2, LAYOUT_2X2))
    cy.window().then((win) => state(win).injectWsEvent(actionResult('CORRECT', memoryState(2, 2, with2x2({ 0: { faceUp: true } })))))
    cy.window({ timeout: GAME_TIMEOUT }).should((win) => expect(card(win, 'card-0').shown).to.eq(true))

    cy.window().then((win) => state(win).injectWsEvent(actionResult('INCORRECT', memoryState(2, 2,
      with2x2({ 0: { faceUp: true }, 1: { faceUp: true } }),
      { waitingForFlipBack: true, flipBackCardIds: ['card-0', 'card-1'], flipBackDelayMs: 1000 }))))

    // las dos a la vista mientras dura el tiempo
    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      expect(card(win, 'card-1').shown).to.eq(true)
      expect(board(win).flipBackPending).to.eq(true)
      expect(board(win).blockActions, 'se puede seguir jugando durante la espera').to.eq(false)
    })
    cy.wait(400)
    cy.window().should((win) => expect(card(win, 'card-0').shown).to.eq(true))

    // pasado el tiempo vuelven boca abajo por si solas
    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      ;['card-0', 'card-1'].forEach(id => {
        expect(card(win, id).shown, id).to.eq(false)
        expect(card(win, id).coverVisible).to.eq(true)
        expect(card(win, id).elementId).to.eq(null)
      })
      expect(board(win).flipBackPending).to.eq(false)
    })
    // y se pueden volver a tocar
    cy.window().then((win) => {
      state(win).tapCard('card-0')
      const actions = sent(win).map(s => JSON.parse(s)).filter(m => m.type === 'game_action')
      expect(JSON.parse(actions[actions.length - 1].action).cardId).to.eq('card-0')
    })
  })

  it('tocar otra carta con una pareja a la vista la voltea al instante y no se pierde el toque', () => {
    openMemoryScene(childId)
    readyWith(memoryState(2, 2, LAYOUT_2X2))
    cy.window().then((win) => state(win).injectWsEvent(actionResult('INCORRECT', memoryState(2, 2,
      with2x2({ 0: { faceUp: true }, 1: { faceUp: true } }),
      { waitingForFlipBack: true, flipBackCardIds: ['card-0', 'card-1'], flipBackDelayMs: 5000 }))))
    cy.window({ timeout: GAME_TIMEOUT }).should((win) => expect(board(win).flipBackPending).to.eq(true))

    cy.window().then((win) => state(win).tapCard('card-2'))

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      expect(card(win, 'card-0').shown).to.eq(false)
      expect(card(win, 'card-1').shown).to.eq(false)
      expect(board(win).flipBackPending).to.eq(false)
      const actions = sent(win).map(s => JSON.parse(s)).filter(m => m.type === 'game_action')
      expect(JSON.parse(actions[actions.length - 1].action).cardId).to.eq('card-2')
    })
  })

  it('tocar una carta que sigue a la vista (la pareja que no coincide) no hace nada', () => {
    openMemoryScene(childId)
    readyWith(memoryState(2, 2, LAYOUT_2X2))
    cy.window().then((win) => state(win).injectWsEvent(actionResult('INCORRECT', memoryState(2, 2,
      with2x2({ 0: { faceUp: true }, 1: { faceUp: true } }),
      { waitingForFlipBack: true, flipBackCardIds: ['card-0', 'card-1'], flipBackDelayMs: 5000 }))))
    cy.window({ timeout: GAME_TIMEOUT }).should((win) => expect(board(win).flipBackPending).to.eq(true))

    cy.window().then((win) => {
      const before = sent(win).length
      state(win).tapCard('card-0')
      expect(sent(win).length).to.eq(before)
      expect(card(win, 'card-0').shown).to.eq(true)
    })
  })

  it('audio de Nubi: la consigna se reproduce al inicio y un toque en Nubi la repite', () => {
    openMemoryScene(childId)
    readyWith(memoryState(2, 2, LAYOUT_2X2))

    cy.window().then((win) => {
      state(win).seedAudioBuffer(AUDIO_ID)
      state(win).injectWsEvent({
        event: 'GAME_AVATAR_EVENT', sessionId: 1, eventType: 'ROUND_PROMPT',
        audioAvailable: true, audioId: AUDIO_ID, text: 'Encuentra las parejas'
      })
    })
    cy.window().should((win) => expect((win as any).__NUBI_PLAYED_AUDIO__).to.deep.eq([AUDIO_ID]))

    cy.window().then((win) => state(win).tapNubi())
    cy.window().should((win) => expect((win as any).__NUBI_PLAYED_AUDIO__).to.deep.eq([AUDIO_ID, AUDIO_ID]))
  })

  it('funciona sin audio: el tablero se juega igual', () => {
    openMemoryScene(childId, { npc: false, audio: false })
    readyWith(memoryState(2, 2, LAYOUT_2X2))

    cy.window().then((win) => {
      state(win).injectWsEvent({
        event: 'GAME_AVATAR_EVENT', sessionId: 1, eventType: 'ROUND_PROMPT',
        audioAvailable: false, text: 'Encuentra las parejas'
      })
      state(win).tapCard('card-0')
      state(win).injectWsEvent(actionResult('CORRECT', memoryState(2, 2, with2x2({ 0: { faceUp: true } }))))
    })
    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      expect(card(win, 'card-0').shown).to.eq(true)
      expect((win as any).__NUBI_PLAYED_AUDIO__).to.have.length(0)
    })
  })

  it('doble-tap en el boton de salida abandona la partida y vuelve al mapa', () => {
    openMemoryScene(childId)
    readyWith(memoryState(2, 2, LAYOUT_2X2))

    cy.window().then((win) => {
      state(win).tapExitButton()
      state(win).tapExitButton()
    })

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      expect(state(win).activeScene).to.eq('world-map')
      expect(sent(win).map(s => JSON.parse(s).type)).to.include('game_abandon')
    })
  })

  it('un solo toque en el boton de salida no abandona', () => {
    openMemoryScene(childId)
    readyWith(memoryState(2, 2, LAYOUT_2X2))

    cy.window().then((win) => state(win).tapExitButton())
    cy.wait(600)
    cy.window().should((win) => {
      expect(state(win).activeScene).to.eq('memory-game')
      expect(sent(win).map(s => JSON.parse(s).type)).to.not.include('game_abandon')
    })
  })

  it('al encontrar todas las parejas se celebra y se vuelve al mapa', () => {
    openMemoryScene(childId)
    readyWith(memoryState(2, 2, LAYOUT_2X2))
    cy.window().then((win) => state(win).injectWsEvent(
      actionResult('CORRECT', memoryState(2, 2, with2x2({ 0: { matched: true }, 3: { matched: true } })))))
    cy.window({ timeout: GAME_TIMEOUT }).should((win) => expect(card(win, 'card-0').matched).to.eq(true))

    cy.window().then((win) => state(win).injectWsEvent(
      actionResult('CORRECT', memoryState(2, 2, with2x2({ 0: { matched: true }, 1: { matched: true }, 2: { matched: true }, 3: { matched: true } })), true)))

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      expect(board(win).celebrationBurstCount, 'fuegos artificiales de la celebracion').to.be.within(5, 7)
      expect(board(win).cards.every((c: any) => c.matched && c.shown)).to.eq(true)
      expect(board(win).blockActions).to.eq(true)
    })
    cy.window({ timeout: GAME_TIMEOUT }).should((win) => expect(state(win).activeScene).to.eq('world-map'))
  })
})
