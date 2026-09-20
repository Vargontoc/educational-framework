import { createFamilyViaApi, createChildViaApi, uniqueFamilyName } from '../../support/testData'
import { ResponsiveLayout } from '../../../src/game/utils/ResponsiveLayout'
import { profileFor } from '../../../src/game/utils/DeviceProfile'

const PIN = '1234'
const GAME_TIMEOUT = 20000
const GAME_W = 1280
const GAME_H = 720
const MIN_TOUCH_CSS = 44

// Resoluciones objetivo del sprint. Las de movil/tablet se dan en vertical; el minijuego
// solo se juega en horizontal (en vertical aparece OrientationRequiredScene), asi que
// aqui se usan sus equivalentes apaisados.
const TARGETS = [
  { name: 'movil pequeno 320x568', width: 568, height: 320, coarse: true },
  { name: 'movil estandar 375x667', width: 667, height: 375, coarse: true },
  { name: 'tablet 768x1024', width: 1024, height: 768, coarse: true },
  { name: 'desktop 1920x1080', width: 1920, height: 1080, coarse: false }
]

interface Box { x: number, y: number, w: number, h: number }
const overlaps = (a: Box, b: Box) => a.x < b.x + b.w && b.x < a.x + a.w && a.y < b.y + b.h && b.y < a.y + a.h

/** Con Scale.FIT el canvas 16:9 cabe en la ventana: mide el ancho visible en px CSS. */
function layoutFor(target: { width: number, height: number, coarse: boolean }) {
  const canvasCssWidth = Math.min(target.width, target.height * 16 / 9)
  const displayScale = GAME_W / canvasCssWidth
  const profile = profileFor({ shortSide: Math.min(target.width, target.height), coarsePointer: target.coarse })
  const layout = new ResponsiveLayout(profile)
  const sizes = layout.calculateSizes({ viewportWidth: GAME_W, viewportHeight: GAME_H, displayScale })
  return { layout, sizes, displayScale }
}

function fixedBoxes(sizes: ReturnType<ResponsiveLayout['getSizes']>) {
  const exit: Box = { x: sizes.buttonMargin, y: sizes.buttonMargin, w: sizes.buttonSize, h: sizes.buttonSize }
  const nubi: Box = {
    x: GAME_W - sizes.nubiMargin - sizes.nubiSize,
    y: GAME_H - sizes.nubiMargin - sizes.nubiSize,
    w: sizes.nubiSize,
    h: sizes.nubiSize
  }
  const stimulus: Box = {
    x: sizes.stimulusCenter.x - sizes.stimulusSize / 2,
    y: sizes.stimulusCenter.y - sizes.stimulusSize / 2,
    w: sizes.stimulusSize,
    h: sizes.stimulusSize
  }
  const bar: Box = {
    x: GAME_W / 2 - sizes.progressBarWidth / 2,
    y: sizes.progressBarTop,
    w: sizes.progressBarWidth,
    h: sizes.progressBarHeight
  }
  return { exit, nubi, stimulus, bar }
}

// Logica pura: no necesita backend ni navegador de la app.
describe('ResponsiveLayout — tamaños en resoluciones objetivo (SPRINT-080)', () => {
  TARGETS.forEach((target) => {
    describe(target.name, () => {
      it('elementos tactiles >= 44px CSS y boton de salir accesible', () => {
        const { layout, sizes, displayScale } = layoutFor(target)
        layout.getOptionSlots(4).forEach((slot) => {
          expect(slot.size / displayScale).to.be.at.least(MIN_TOUCH_CSS)
        })
        expect(sizes.buttonSize / displayScale).to.be.at.least(MIN_TOUCH_CSS)
        expect(sizes.buttonSize / displayScale).to.be.at.most(80)
        expect(fixedBoxes(sizes).exit.x).to.be.at.least(0)
        expect(fixedBoxes(sizes).exit.y).to.be.at.least(0)
      })

      it('Nubi mide entre 80 y 120px CSS', () => {
        const { sizes, displayScale } = layoutFor(target)
        expect(sizes.nubiSize / displayScale).to.be.within(80, 120)
      })

      it('la tarjeta del estimulo es cuadrada y mide entre 100 y 200px CSS', () => {
        const { layout, sizes, displayScale } = layoutFor(target)
        expect(layout.getStimulusCardSize()).to.eq(sizes.stimulusSize)
        expect(sizes.stimulusSize / displayScale).to.be.within(100, 200)
      })

      it('ningun elemento se solapa (2 a 6 opciones)', () => {
        const { layout, sizes } = layoutFor(target)
        const fixed = fixedBoxes(sizes)

        for (let count = 2; count <= 6; count++) {
          const options: Box[] = layout.getOptionSlots(count)
            .map(s => ({ x: s.x - s.size / 2, y: s.y - s.size / 2, w: s.size, h: s.size }))

          options.forEach((box, i) => {
            expect(box.x, `opcion ${i} dentro (${count} opciones)`).to.be.at.least(0)
            expect(box.x + box.w).to.be.at.most(GAME_W)
            expect(box.y + box.h).to.be.at.most(GAME_H)
            Object.entries(fixed).forEach(([name, other]) => {
              expect(overlaps(box, other), `opcion ${i} vs ${name} (${count} opciones)`).to.eq(false)
            })
            options.slice(i + 1).forEach((next, j) => {
              expect(overlaps(box, next), `opcion ${i} vs opcion ${i + 1 + j}`).to.eq(false)
            })
          })
        }

        const { exit, bar, stimulus, nubi } = fixed
        expect(overlaps(exit, bar), 'salir vs barra').to.eq(false)
        expect(overlaps(exit, stimulus), 'salir vs estimulo').to.eq(false)
        expect(overlaps(bar, stimulus), 'barra vs estimulo').to.eq(false)
        expect(overlaps(nubi, stimulus), 'Nubi vs estimulo').to.eq(false)
      })
    })
  })

  it('la fuente minima legible es >= 14px CSS', () => {
    TARGETS.forEach((target) => {
      const { sizes, displayScale } = layoutFor(target)
      expect(sizes.minFontSize / displayScale).to.be.closeTo(14, 0.001)
    })
  })
})

const state = (win: Cypress.AUTWindow) => (win as any).__NUBI_GAME_STATE__

const LETTERS = ['a', 'b', 'c'].map(l => ({
  id: `letter_${l}`, code: `letter_${l}`, displayValue: l.toUpperCase(), resourceRefs: { image: `letter_${l}` }
}))

const GAME_READY = {
  event: 'GAME_READY',
  sessionId: 1,
  payload: {
    engine: 'RECOGNITION',
    activityId: 1,
    gameId: 1,
    difficultyLevelId: 1,
    status: 'IN_PROGRESS',
    recognitionState: {
      recognitionCategory: 'LETTER',
      roundIndex: 0,
      totalRounds: 5,
      hintActive: false,
      targetElementId: 'letter_a',
      optionIds: LETTERS.map(l => l.id),
      elements: LETTERS
    }
  }
}

function openRecognition(childId: number) {
  cy.selectChildProfile('Nubi')
  cy.visit(`/game/${childId}`)
  cy.window({ timeout: GAME_TIMEOUT }).should((win) => expect(state(win).activeScene).to.eq('world-map'))
  cy.window().then((win) => state(win).startRecognitionScene())
  cy.window({ timeout: GAME_TIMEOUT }).should((win) => expect(state(win).activeScene).to.eq('recognition-game'))
  cy.window().then((win) => state(win).injectWsEvent(GAME_READY))
  cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
    expect(state(win).getSceneData().images.filter((i: any) => !i.isStimulus)).to.have.length(3)
  })
}

const options = (win: Cypress.AUTWindow) =>
  state(win).getSceneData().images.filter((i: any) => !i.isStimulus)

describe('RecognitionGameScene — renderizado adaptable en el juego (SPRINT-080)', () => {
  let childId: number

  before(() => {
    createFamilyViaApi({ name: uniqueFamilyName('F7Responsive'), pin: PIN })
    createChildViaApi({ name: 'Nubi', birthday: '2022-03-15', avatar: 'avatar-01' })
      .then((res) => { childId = res.body.data.id })
  })

  TARGETS.forEach((target) => {
    it(`${target.name}: opciones tactiles >= 44px CSS y sin solape`, () => {
      cy.viewport(target.width, target.height)
      openRecognition(childId)

      cy.window().should((win) => {
        const scale = state(win).getScaleInfo()
        const opts = options(win)
        opts.forEach((o: any) => {
          expect(o.displayWidth / scale.displayScale, 'px CSS').to.be.at.least(MIN_TOUCH_CSS - 1)
        })
        const boxes: Box[] = opts.map((o: any) => ({ x: o.x - o.displayWidth / 2, y: o.y - o.displayHeight / 2, w: o.displayWidth, h: o.displayHeight }))
        boxes.forEach((a, i) => boxes.slice(i + 1).forEach(b => expect(overlaps(a, b)).to.eq(false)))
      })
    })
  })

  it('resize: cambiar el tamaño de la ventana recoloca y reescala los elementos', () => {
    cy.viewport(1920, 1080)
    openRecognition(childId)

    let before: { y: number, width: number }
    cy.window().then((win) => {
      const o = options(win)[0]
      before = { y: o.y, width: o.displayWidth }
    })

    cy.viewport(568, 320)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      const scale = state(win).getScaleInfo()
      const o = options(win)[0]
      // en pantalla pequeña el juego se reduce por CSS: en unidades logicas los elementos crecen para seguir siendo tactiles
      expect(o.displayWidth).to.be.greaterThan(before.width)
      expect(o.displayWidth / scale.displayScale).to.be.at.least(MIN_TOUCH_CSS - 1)
      expect(o.y).to.not.eq(before.y)
    })
  })

  it('boton de salir y Nubi tienen tamaño adecuado en el juego', () => {
    cy.viewport(568, 320)
    openRecognition(childId)

    cy.window().should((win) => {
      const scale = state(win).getScaleInfo()
      const sizes = state(win).getLayoutSizes()
      expect(sizes.buttonSize / scale.displayScale).to.be.at.least(MIN_TOUCH_CSS - 1)
      expect(sizes.nubiSize / scale.displayScale).to.be.within(79, 121)
    })
  })

  it('el minijuego usa Scale.FIT y lo libera al salir', () => {
    cy.viewport(1000, 660)
    openRecognition(childId)

    cy.window().then((win) => {
      const fit = 3 // Phaser.Scale.FIT
      expect(state(win).getScaleInfo().mode).to.eq(fit)
    })

    cy.window().then((win) => {
      state(win).tapExitButton()
      state(win).tapExitButton()
    })

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      expect(state(win).activeScene).to.eq('world-map')
      expect(state(win).getScaleInfo().mode).to.not.eq(3)
    })
  })

  it('orientacion vertical muestra el mensaje de rotacion', () => {
    cy.viewport(667, 375)
    openRecognition(childId)

    cy.viewport(375, 667)

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      expect(state(win).activeScene).to.eq('orientation-required')
    })
  })
})
