# Cypress E2E — My Friend Nubi

## Organización de specs

Los specs se organizan en subcarpetas por fase funcional, alineadas con los sprints E2E (049–055):

```
cypress/e2e/
├── _harness/              # SPRINT-048: arnés de pruebas (infraestructura)
├── fase1-entrada/         # SPRINT-049: registro familiar, login, selección de niño
├── fase2-perfiles/        # SPRINT-050: gestión de perfiles infantiles
├── fase3-config/          # SPRINT-051: configuración global familiar
├── fase4-progreso/        # SPRINT-052: progreso orientativo
├── fase5-contenidos/      # SPRINT-053: contenidos educativos
├── fase6-audio-tts/       # SPRINT-054: audio y TTS
└── fase7-npc-juego/       # SPRINT-055: NPC, Phaser, juego
```

## Convenciones

- Un spec por flujo o criterio de aceptación crítico.
- Cada spec crea sus propias precondiciones (no depende de datos de otro spec).
- Usar `cy.loginAsParent()` y `cy.selectChildProfile(name)` para autenticación y selección de niño.
- Usar `cy.intercept` para stub de TTS/audio (TTS_ENABLED=false en el stack E2E).
- No testear contenido visual pixel a pixel en Phaser; usar `window.__NUBI_GAME_STATE__` para inspección.
- Smoke test global en `fase1-entrada/smoke.cy.ts`.

## Comandos custom

| Comando | Descripción |
|---------|-------------|
| `cy.loginAsParent(pin?)` | Login parental con caché de sesión (`cy.session()`). PIN por defecto: `1234`. |
| `cy.selectChildProfile(name)` | Selecciona perfil infantil y abre sesión de juego con caché. |

## Hook de inspección Phaser

En modo no-producción, `window.__NUBI_GAME_STATE__` expone:
- `childId`: ID del niño activo
- `npcEnabled`: si el NPC está habilitado
- `ttsEnabled`: si TTS está habilitado
- `activeScene`: nombre de la escena Phaser activa

## Estrategia de datos

- **Decisión**: alta vía API directa contra el stack E2E (más rápido, contratos disponibles).
- Helpers en `cypress/support/testData.ts`.
- Cada spec crea sus propios datos en `before()` o `beforeEach()`.

## Stub de audio/TTS

```typescript
cy.intercept('POST', '**/tts/**', { statusCode: 200, body: { audioContent: '' } }).as('ttsRequest')
```

## Ejecución

```bash
# Stack E2E (iterativo)
scripts/e2e-up.sh
scripts/e2e-test.sh
scripts/e2e-down.sh

# Local sin Docker
npm run cy:open   # interactivo
npm run cy:run    # headless
```
