import { createFamilyViaApi, createChildViaApi, uniqueFamilyName } from '../../support/testData'

const PIN = '1234'
const GAME_TIMEOUT = 20000
const ITEM_RATIO = 0.55

const state = (win: Cypress.AUTWindow) => (win as any).__NUBI_GAME_STATE__

function colorElements(colors: string[], extra: (color: string) => object = () => ({})) {
  return colors.map(c => ({
    id: `color_${c}`,
    code: `color_${c}`,
    displayValue: c,
    resourceRefs: { color: '#000000', 'nubi-audio': 'texto', icon: 'icono' },
    ...extra(c)
  }))
}

function recognitionState(elements: any[], roundIndex: number, showIcon: boolean) {
  return {
    recognitionCategory: 'COLOR',
    roundIndex,
    totalRounds: 6,
    hintActive: false,
    targetElementId: elements[0].id,
    optionIds: elements.map(e => e.id),
    elements,
    showIcon
  }
}

const gameReady = (elements: any[], showIcon: boolean) => ({
  event: 'GAME_READY',
  sessionId: 1,
  payload: {
    engine: 'RECOGNITION', activityId: 1, gameId: 1, difficultyLevelId: 1, status: 'IN_PROGRESS',
    recognitionState: recognitionState(elements, 0, showIcon)
  }
})

const correctResult = (elements: any[], roundIndex: number, showIcon: boolean) => ({
  event: 'GAME_ACTION_RESULT',
  sessionId: 1,
  payload: {
    resultType: 'CORRECT', gameCompleted: false, difficultyChanged: false, newDifficultyLevelId: 1, attemptContext: '',
    updatedState: {
      engine: 'RECOGNITION', activityId: 1, gameId: 1, difficultyLevelId: 1, status: 'IN_PROGRESS',
      recognitionState: recognitionState(elements, roundIndex, showIcon)
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

const images = (win: Cypress.AUTWindow): any[] => state(win).getSceneData().images
const options = (win: Cypress.AUTWindow) => images(win).filter(i => !i.isStimulus && i.type === 'Container')
const stimulus = (win: Cypress.AUTWindow) => images(win).find(i => i.isStimulus)
const part = (composite: any, name: string) => composite.children?.find((c: any) => c.name === name)
const itemName = (composite: any): string | undefined => part(composite, 'item')?.key?.split('/')[1]

describe('RecognitionGameScene — colores: assets dinamicos y splash + item (SPRINT-082)', () => {
  let childId: number

  before(() => {
    createFamilyViaApi({ name: uniqueFamilyName('F7ColorSplash'), pin: PIN })
    createChildViaApi({ name: 'Nubi', birthday: '2022-03-15', avatar: 'avatar-01' })
      .then((res) => { childId = res.body.data.id })
  })

  function play(elements: any[], showIcon: boolean) {
    openRecognitionScene(childId)
    cy.window().then((win) => state(win).injectWsEvent(gameReady(elements, showIcon)))
    cy.window({ timeout: GAME_TIMEOUT }).should((win) => expect(options(win)).to.have.length(elements.length))
  }

  it('carga dinamica: solo los bloques de la ronda (code -> recognition-color-[color])', () => {
    play(colorElements(['red', 'blue', 'green']), true)

    cy.window().should((win) => {
      ;['red', 'blue', 'green'].forEach(c =>
        expect(state(win).textureExists(`recognition-color-${c}/splash`), c).to.eq(true))
      expect(state(win).textureExists('recognition-color-purple/splash')).to.eq(false)
    })
  })

  it('splash: cada opcion usa el splash de su bloque', () => {
    play(colorElements(['red', 'blue', 'green']), true)

    cy.window().should((win) => {
      options(win).forEach((o) => {
        const block = o.elementId.replace('color_', '')
        expect(part(o, 'splash').key).to.eq(`recognition-color-${block}/splash`)
      })
      expect(part(stimulus(win), 'splash').key).to.eq('recognition-color-red/splash')
    })
  })

  it('item: el mismo item (indice) en el estimulo y en todas las opciones', () => {
    play(colorElements(['red', 'blue', 'green']), true)

    cy.window().should((win) => {
      const item = itemName(options(win)[0])
      expect(item).to.match(/^item_\d+$/)
      options(win).forEach(o => expect(itemName(o)).to.eq(item))
      expect(itemName(stimulus(win))).to.eq(item)
      expect(state(win).getSceneData().selectedColorItem).to.eq(item)
    })
  })

  it('item comun a todos los bloques (purple solo tiene item_1..3)', () => {
    play(colorElements(['purple', 'red', 'white']), true)

    cy.window().should((win) => {
      const item = itemName(options(win)[0])
      expect(['item_1', 'item_2', 'item_3']).to.include(item)
      options(win).forEach(o => expect(itemName(o)).to.eq(item))
    })
  })

  it('el item se pinta centrado sobre el splash al 55% y deja ver el splash', () => {
    play(colorElements(['red', 'blue']), true)

    cy.window().should((win) => {
      options(win).forEach((o) => {
        const splash = part(o, 'splash')
        const item = part(o, 'item')
        expect(item.x).to.eq(0)
        expect(item.y).to.eq(0)
        expect(Math.max(item.width, item.height))
          .to.be.closeTo(Math.max(splash.width, splash.height) * ITEM_RATIO, 1)
        expect(item.height).to.be.lessThan(splash.height)
      })
    })
  })

  it('EASY/MEDIUM (showIcon=true): el target muestra el item, una sola vez (sin icono duplicado)', () => {
    play(colorElements(['red', 'blue', 'green']), true)

    cy.window().should((win) => {
      const target = stimulus(win)
      const item = itemName(options(win)[0])
      expect(part(target, 'item').key).to.eq(`recognition-color-red/${item}`)

      // Un unico elemento de estimulo y ningun otro objeto pintado con la textura del item del target
      expect(images(win).filter(i => i.isStimulus)).to.have.length(1)
      const targetItemKey = part(target, 'item').key
      const standalone = images(win).filter(i => i.textureKey === targetItemKey)
      expect(standalone, 'item suelto duplicado').to.have.length(0)
    })
  })

  it('HARD (showIcon=false): el target es solo el splash, sin item; las opciones conservan el suyo', () => {
    play(colorElements(['red', 'blue', 'green']), false)

    cy.window().should((win) => {
      expect(part(stimulus(win), 'splash').key).to.eq('recognition-color-red/splash')
      expect(part(stimulus(win), 'item')).to.not.exist
      expect(images(win).filter(i => i.isStimulus)).to.have.length(1)
      expect(options(win)).to.have.length(3)
      options(win).forEach(o => expect(part(o, 'item')).to.exist)
    })
  })

  it('showIcon puede cambiar entre rondas y cada ronda carga sus propios bloques', () => {
    play(colorElements(['red', 'blue', 'green']), true)

    cy.window().then((win) => state(win).injectWsEvent(correctResult(colorElements(['yellow', 'orange']), 1, false)))

    cy.window({ timeout: GAME_TIMEOUT }).should((win) => {
      expect(options(win).map(o => o.elementId).sort()).to.deep.eq(['color_orange', 'color_yellow'])
      expect(part(stimulus(win), 'item')).to.not.exist
      expect(state(win).textureExists('recognition-color-yellow/splash')).to.eq(true)
    })
  })

  it('las opciones compuestas siguen siendo tactiles', () => {
    play(colorElements(['red', 'blue', 'green']), true)

    cy.window().should((win) => {
      options(win).forEach(o => expect(o.inputEnabled).to.eq(true))
    })
  })
})
