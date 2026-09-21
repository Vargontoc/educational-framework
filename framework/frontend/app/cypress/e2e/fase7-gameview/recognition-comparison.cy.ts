import { createFamilyViaApi, createChildViaApi, uniqueFamilyName } from '../../support/testData'
import { ResponsiveLayout } from '../../../src/game/utils/ResponsiveLayout'
import { profileFor } from '../../../src/game/utils/DeviceProfile'

const PIN = '1234'
const GAME_TIMEOUT = 20000
const GAME_W = 1280
const GAME_H = 720
const MIN_TOUCH_CSS = 44

const LADDERS = [
  { name: 'EASY', scales: [100, 40] },
  { name: 'MEDIUM', scales: [100, 65] },
  { name: 'HARD', scales: [100, 75, 50] }
]

// Mismas resoluciones objetivo que SPRINT-080 (movil/tablet en apaisado: el minijuego solo se juega asi).
const TARGETS = [
  { name: 'movil pequeno 320x568', width: 568, height: 320, coarse: true },
  { name: 'movil estandar 375x667', width: 667, height: 375, coarse: true },
  { name: 'tablet 768x1024', width: 1024, height: 768, coarse: true },
  { name: 'desktop 1920x1080', width: 1920, height: 1080, coarse: false }
]

interface Box { x: number, y: number, w: number, h: number }
const overlaps = (a: Box, b: Box) => a.x < b.x + b.w && b.x < a.x + a.w && a.y < b.y + b.h && b.y < a.y + a.h

function layoutFor(target: { width: number, height: number, coarse: boolean }) {
  const canvasCssWidth = Math.min(target.width, target.height * 16 / 9)
  const displayScale = GAME_W / canvasCssWidth
  const profile = profileFor({ shortSide: Math.min(target.width, target.height), coarsePointer: target.coarse })
  const layout = new ResponsiveLayout(profile)
  const sizes = layout.calculateSizes({ viewportWidth: GAME_W, viewportHeight: GAME_H, displayScale })
  return { layout, sizes, displayScale }
}

// Logica pura: no necesita backend ni navegador de la app.
describe('ResponsiveLayout — opciones de comparacion (SPRINT-083)', () => {
  TARGETS.forEach((target) => {
    LADDERS.forEach((ladder) => {
      it(`${target.name} ${ladder.name}: proporciones, area tactil y sin solapes`, () => {
        const { layout, sizes, displayScale } = layoutFor(target)
        const slots = layout.getComparisonSlots(ladder.scales)

        expect(slots).to.have.length(ladder.scales.length)

        // el mismo objeto a la escala pedida: cada lado es el del 100 % por su porcentaje
        const big = slots[0].size
        slots.forEach((slot, i) => {
          expect(slot.size).to.be.closeTo(big * ladder.scales[i] / 100, 0.001)
          expect(slot.size / displayScale, 'la opcion mayor no pasa de 240 px CSS').to.be.at.most(240 + 0.001)
          // incluso la opcion mas pequena se puede tocar con comodidad
          expect(slot.hitSize / displayScale, `area tactil de la opcion ${ladder.scales[i]} %`).to.be.at.least(MIN_TOUCH_CSS - 0.001)
          expect(slot.hitSize).to.be.at.least(slot.size - 0.001)
        })

        const boxes = (side: (s: typeof slots[number]) => number): Box[] => slots.map(s => ({
          x: s.x - side(s) / 2, y: s.y - side(s) / 2, w: side(s), h: side(s)
        }))
        const drawn = boxes(s => s.size)
        const touch = boxes(s => s.hitSize)

        const nubi: Box = {
          x: GAME_W - sizes.nubiMargin - sizes.nubiSize,
          y: GAME_H - sizes.nubiMargin - sizes.nubiSize,
          w: sizes.nubiSize,
          h: sizes.nubiSize
        }
        const exit: Box = { x: sizes.buttonMargin, y: sizes.buttonMargin, w: sizes.buttonSize, h: sizes.buttonSize }
        const stimulus: Box = {
          x: sizes.stimulusCenter.x - sizes.stimulusSize / 2,
          y: sizes.stimulusCenter.y - sizes.stimulusSize / 2,
          w: sizes.stimulusSize,
          h: sizes.stimulusSize
        }

        ;[drawn, touch].forEach((group, g) => {
          const kind = g === 0 ? 'dibujada' : 'tactil'
          group.forEach((box, i) => {
            expect(box.x, `${kind} ${i} dentro`).to.be.at.least(0)
            expect(box.x + box.w).to.be.at.most(GAME_W)
            expect(box.y).to.be.at.least(0)
            expect(box.y + box.h).to.be.at.most(GAME_H)
            expect(overlaps(box, nubi), `${kind} ${i} vs Nubi`).to.eq(false)
            expect(overlaps(box, exit), `${kind} ${i} vs salir`).to.eq(false)
            expect(overlaps(box, stimulus), `${kind} ${i} vs consigna`).to.eq(false)
            group.slice(i + 1).forEach((other, j) => {
              expect(overlaps(box, other), `${kind} ${i} vs ${i + 1 + j}`).to.eq(false)
            })
          })
        })
      })
    })
  })

  it('sin opciones no devuelve nada', () => {
    expect(layoutFor(TARGETS[0]).layout.getComparisonSlots([])).to.deep.eq([])
  })
})

const state = (win: Cypress.AUTWindow) => (win as any).__NUBI_GAME_STATE__

const ELEMENT = {
  id: 'apple',
  code: 'apple',
  displayValue: 'Manzana',
  resourceRefs: { 'nubi-audio': '¿Cuál es la manzana más grande?', image: 'apple' }
}

function comparisonState(scales: number[], roundIndex: number, hintActive = false) {
  return {
    recognitionCategory: 'COMPARISON',
    roundIndex,
    totalRounds: 5,
    hintActive,
    targetElementId: ELEMENT.id,
    optionIds: scales.map(() => ELEMENT.id),
    elements: [ELEMENT],
    guideChromEnabled: false,
    touchEnableDelayMs: 0,
    nonChromaticKeyRequired: false,
    showIcon: true,
    comparisonMode: true,
    comparisonOptions: scales.map(scalePercent => ({ elementId: ELEMENT.id, scalePercent }))
  }
}

const gameReady = (scales: number[], hintActive = false) => ({
  event: 'GAME_READY',
  sessionId: 1,
  payload: {
    engine: 'RECOGNITION', activityId: 1, gameId: 1, difficultyLevelId: 1, status: 'IN_PROGRESS',
    recognitionState: comparisonState(scales, 0, hintActive)
  }
})

const actionResult = (resultType: 'CORRECT' | 'INCORRECT', scales: number[], roundIndex: number, hintActive = false) => ({
  event: 'GAME_ACTION_RESULT',
  sessionId: 1,
  payload: {
    resultType, gameCompleted: false, difficultyChanged: false, newDifficultyLevelId: 1, attemptContext: '',
    updatedState: {
      engine: 'RECOGNITION', activityId: 1, gameId: 1, difficultyLevelId: 1, status: 'IN_PROGRESS',
      recognitionState: comparisonState(scales, roundIndex, hintActive)
    }
  }
})

function openRecognitionScene(childId: number) {
  cy.selectChildProfile('Nubi')
  cy.visit(`/game/${childId}`)
  cy.window({ timeout: GAME_TIMEOUT }).should((win) => expect(state(win).activeScene).to.eq('world-map'))
  cy.window().then((win) => state(win).startRecognitionScene())
  cy.window({ timeout: GAME_TIMEOUT }).should((win) => expect(state(win).activeScene).to.eq('recognition-game'))
}

const options = (win: Cypress.AUTWindow): any[] =>
  state(win).getSceneData().images.filter((i: any) => !i.isStimulus && i.scalePercent !== null && i.type !== 'Text')

describe('RecognitionGameScene — modo comparacion grande/pequeno (SPRINT-083)', () => {
  let childId: number

  before(() => {
    createFamilyViaApi({ name: uniqueFamilyName('F7Comparison'), pin: PIN })
    createChildViaApi({ name: 'Nubi', birthday: '2022-03-15', avatar: 'avatar-01' })
      .then((res) => { childId = res.body.data.id })
  })

  LADDERS.forEach((ladder) => {
    it(`${ladder.name}: se detecta el modo y se dibujan ${ladder.scales.length} opciones con sus escalas`, () => {
      openRecognitionScene(childId)
      cy.window().then((win) => state(win).injectWsEvent(gameReady(ladder.scales)))

      cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
        expect(state(win).getSceneData().comparisonMode).to.eq(true)
        const opts = options(win)
        expect(opts).to.have.length(ladder.scales.length)
        expect(opts.map((o: any) => o.scalePercent).sort((a: number, b: number) => a - b))
          .to.deep.eq([...ladder.scales].sort((a, b) => a - b))

        // mismo objeto, solo cambia el tamano
        const big = opts.find((o: any) => o.scalePercent === 100)
        opts.forEach((o: any) => {
          expect(o.elementId).to.eq('apple')
          expect(o.displayWidth).to.be.closeTo(big.displayWidth * o.scalePercent / 100, 1)
        })
        // sin tarjeta de objeto en la cabecera: la consigna es un icono
        expect(state(win).getSceneData().images.filter((i: any) => i.isStimulus)).to.have.length(0)
      })
    })
  })

  it('las opciones pequenas conservan un area tactil de al menos 44 px CSS', () => {
    cy.viewport(1000, 660)
    openRecognitionScene(childId)
    cy.window().then((win) => state(win).injectWsEvent(gameReady([100, 40])))

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const scale = state(win).getScaleInfo().displayScale
      options(win).forEach((o: any) => {
        expect(o.inputEnabled).to.eq(true)
        expect(o.hitDisplayWidth / scale, `area tactil de la opcion ${o.scalePercent} %`).to.be.at.least(MIN_TOUCH_CSS - 1)
        expect(o.hitDisplayWidth).to.be.at.least(o.displayWidth - 1)
      })
    })
  })

  it('tocar el mayor envia su escala; tocar uno menor tambien y la ronda sigue', () => {
    openRecognitionScene(childId)
    cy.window().then((win) => {
      state(win).injectWsEvent(gameReady([100, 40]))
      state(win).spyWsSend()
    })
    cy.window({ timeout: GAME_TIMEOUT }).should((win) => expect(options(win)).to.have.length(2))

    cy.window().then((win) => {
      const small = options(win).find((o: any) => o.scalePercent === 40)
      state(win).tapOption(small.optionIndex)
    })
    cy.window().should((win) => {
      const sent = (win as any).__NUBI_SENT__ as string[]
      expect(sent.some(m => m.includes('selectedScalePercent') && m.includes('40'))).to.eq(true)
    })

    cy.window().then((win) => state(win).injectWsEvent(actionResult('INCORRECT', [100, 40], 0)))
    cy.window({ timeout: GAME_TIMEOUT }).should((win) => expect(options(win)).to.have.length(2))
  })

  it('acierto: avanza y la siguiente ronda usa otra escalera', () => {
    openRecognitionScene(childId)
    cy.window().then((win) => state(win).injectWsEvent(gameReady([100, 40])))
    cy.window({ timeout: GAME_TIMEOUT }).should((win) => expect(options(win)).to.have.length(2))

    cy.window().then((win) => state(win).injectWsEvent(actionResult('CORRECT', [100, 75, 50], 1)))
    cy.window({ timeout: GAME_TIMEOUT }).should((win) => expect(options(win)).to.have.length(3))
  })

  it('negativo: una ronda de reconocimiento no dibuja el modo comparacion', () => {
    openRecognitionScene(childId)
    const letters = ['a', 'b'].map(l => ({ id: `letter_${l}`, code: `letter_${l}`, displayValue: l, resourceRefs: {} }))
    cy.window().then((win) => state(win).injectWsEvent({
      event: 'GAME_READY',
      sessionId: 1,
      payload: {
        engine: 'RECOGNITION', activityId: 1, gameId: 1, difficultyLevelId: 1, status: 'IN_PROGRESS',
        recognitionState: {
          recognitionCategory: 'LETTER', roundIndex: 0, totalRounds: 5, hintActive: false,
          targetElementId: 'letter_a', optionIds: letters.map(l => l.id), elements: letters
        }
      }
    }))

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      expect(state(win).getSceneData().comparisonMode).to.eq(false)
      expect(state(win).getSceneData().images.filter((i: any) => i.isStimulus)).to.have.length(1)
    })
  })
})
