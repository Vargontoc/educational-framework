# Propuesta técnica frontend — FEAT-005: Configuración global de audio, NPC y PIN

## 1. Capa principal

**Frontend** — Vue 3 + TypeScript + Pinia + Tailwind CSS + vue-i18n

## 2. Objetivo técnico

Implementar la vista de Configuración global dentro del panel parental, permitiendo al adulto autenticado controlar audio general, NPC, voz del NPC, voz narrativa (estados y porcentajes) y cambiar el PIN familiar, con una única acción «Guardar cambios» y cierre de sesión parental tras cambio de PIN exitoso.

## 3. Diseño funcional-técnico

### 3.1 Estructura de la vista de configuración

La vista `ConfiguracionView.vue` (actualmente placeholder) se reemplazará por un layout con cinco secciones visualmente diferenciadas, cada una encapsulada en un `NubiCard` o contenedor equivalente:

| # | Sección | Controles |
|---|---------|-----------|
| 1 | Audio general | Toggle on/off + slider/numberInput porcentaje (0-100) |
| 2 | NPC | Toggle on/off (sin porcentaje) |
| 3 | Voz del NPC | Toggle on/off + slider/numberInput porcentaje (0-100) |
| 4 | Voz narrativa | Toggle on/off + slider/numberInput porcentaje (0-100) |
| 5 | PIN familiar | Dos NubiPinInput (nuevo PIN + confirmación) |

Al final de la vista: un único `NubiButton` con texto **«Guardar cambios»**.

**Componente nuevo reutilizable: `ConfigSection`**
- Wrapper con título de sección, descripción breve y slot para controles.
- Proporciona separación visual consistente entre secciones.

**Componente nuevo reutilizable: `ToggleWithPercentage`**
- Combina `NubiToggle` + control de porcentaje (slider nativo con `NubiNumberInput` como fallback de precisión).
- Encapsula la lógica de conservación del último valor ≠ 0.
- Props: `modelEnabled` (boolean), `modelPercentage` (number), `label` (string), `disabled` (boolean).
- Emits: `update:enabled`, `update:percentage`.

### 3.2 Estados y gestión de datos

#### 3.2.1 Modelo de estado local

```
ConfiguracionState {
  // Valores cargados desde API (snapshot inicial)
  persisted: FamilyGlobalConfig

  // Valores editados por el usuario (working copy)
  draft: FamilyGlobalConfig

  // Valores conservados (último valor ≠ 0 por cada control con porcentaje)
  lastNonZero: {
    audioGeneralVolume: number
    npcVoiceVolume: number
    narrativeVoiceVolume: number
  }

  // Estado de la vista
  loading: boolean
  saving: boolean
  saveError: string | null
  pinNew: string
  pinConfirm: string
  pinMismatch: boolean
}
```

#### 3.2.2 Inicialización

1. Al montar la vista, invocar `GET /api/v1/family` (via `useFamilyStatus.fetchFamilyStatus` o servicio directo).
2. Mapear la respuesta a `FamilyGlobalConfig` y copiar a `persisted` y `draft`.
3. Inicializar `lastNonZero` con los valores de volumen actuales si son ≠ 0.

#### 3.2.3 Lógica de conservación de valores

Regla: al apagar un toggle con porcentaje, se conserva el último valor ≠ 0 en `lastNonZero`. Al reactivar, se restaura.

```
onToggleChange(section, enabled):
  draft[section + 'Enabled'] = enabled
  if (!enabled):
    // Conservar valor actual si es ≠ 0
    if (draft[section + 'Volume'] !== 0):
      lastNonZero[section + 'Volume'] = draft[section + 'Volume']
  else:
    // Restaurar último valor ≠ 0
    if (lastNonZero[section + 'Volume']):
      draft[section + 'Volume'] = lastNonZero[section + 'Volume']
```

```
onPercentageChange(section, value):
  draft[section + 'Volume'] = value
  if (value === 0):
    draft[section + 'Enabled'] = false  // Acción rápida de apagado
  else:
    lastNonZero[section + 'Volume'] = value
```

#### 3.2.4 Independencia de controles

- Audio general NO modifica automáticamente voz NPC ni voz narrativa (requisito 3).
- NPC desactivado NO modifica la configuración de voz NPC (requisito 10).
- Voz narrativa es independiente de NPC y voz NPC (requisito 6).

### 3.3 Flujo de interacción

```
[Entrar en Configuración]
  → GET /api/v1/family
  → Cargar valores actuales en draft y persisted
  → Mostrar vista con 5 secciones

[Usuario modifica controles]
  → draft se actualiza en tiempo real
  → persisted permanece inalterado (referencia)
  → lastNonZero se actualiza según reglas de conservación

[Usuario pulsa «Guardar cambios»]
  → Validar PIN (si hay campos PIN rellenados):
    - Ambos campos deben tener exactamente 4 dígitos
    - Ambos deben coincidir
    - Si no coinciden → mostrar aviso, bloquear envío
  → Construir payload PATCH con campos modificados (diff draft vs persisted)
  → PATCH /api/v1/family
  → Éxito:
    - Si hubo cambio de PIN → logout + navigateTo('Home')
    - Si no → toast éxito + actualizar persisted = draft
  → Error → toast error + mantener vista
```

### 3.4 Validaciones y mensajes de error

| Escenario | Validación | Mensaje (i18n) |
|-----------|-----------|----------------|
| PIN nuevo sin completar (1+ campos) | Longitud < 4 en alguno | No se incluye en PATCH; ignorar sección PIN |
| PIN confirmación sin completar | Longitud < 4 | No se incluye en PATCH; ignorar sección PIN |
| PIN nuevo ≠ PIN confirmación | `pinNew !== pinConfirm` | «Los PINs no coinciden. Inténtalo de nuevo.» |
| PIN nuevo con caracteres no numéricos | Regex `/^\d{4}$/` | Rechazado por NubiPinInput (solo numéricos) |
| PIN nuevo = PIN anterior | Permitido (requisito 12) | Sin aviso; se envía normalmente |
| Error de red en PATCH | status === 0 | Toast error: «No se pudo guardar. Revisa tu conexión.» |
| Error 400 en PATCH | validation | Toast error con detalle del backend |
| Error 401 en PATCH | token expirado | Logout automático + redirect a Home |

**Regla de envío PIN:** Solo se incluye `pin` en el payload PATCH si al menos uno de los dos campos (nuevo/confirmación) tiene contenido. Si solo uno está relleno y no completa 4 dígitos, se ignora la sección PIN sin error. Si ambos están completos pero no coinciden, se bloquea el envío con aviso.

### 3.5 Integración con logout tras cambio PIN

```typescript
async function handleSave() {
  // ... validaciones PIN ...
  
  const pinChanged = pinNew.value.length === 4 && pinConfirm.value.length === 4 
                     && pinNew.value === pinConfirm.value

  const result = await updateFamilyConfig(buildPayload())
  
  if (result.success) {
    if (pinChanged) {
      await logout()           // useParentalSession.logout()
      navigateTo('Home')       // router.replace → Home
    } else {
      toast.success('Configuración guardada')
      persisted.value = { ...draft.value }
    }
  }
}
```

El `logout()` existente en `useParentalSession` ya invoca `POST /api/v1/auth/logout` y limpia `parentalAuthStore`. El guard del router redirigiría a Home automáticamente al perder autenticación, pero se hace explícito con `navigateTo('Home')` para claridad de flujo.

## 4. Contratos y dependencias externas

### 4.1 Propuesta de ampliación de `update-family-request.yaml`

```yaml
type: object
additionalProperties: false

properties:
  name:
    type: string
  pin:
    type: string
    pattern: '^\d{4}$'
  ttsEnabled:
    type: boolean
  agentEnabled:
    type: boolean
  audioGeneralEnabled:
    type: boolean
  audioGeneralVolume:
    type: integer
    minimum: 0
    maximum: 100
  npcEnabled:
    type: boolean
  npcVoiceEnabled:
    type: boolean
  npcVoiceVolume:
    type: integer
    minimum: 0
    maximum: 100
  narrativeVoiceEnabled:
    type: boolean
  narrativeVoiceVolume:
    type: integer
    minimum: 0
    maximum: 100
```

**Notas:**
- Todos los campos son opcionales (PATCH parcial). Solo se envían los modificados.
- `pin` mantiene compatibilidad con el campo existente; se añade `pattern` para validación.
- `ttsEnabled` y `agentEnabled` se mantienen por compatibilidad hacia atrás. El backend debe decidir si se mapean a los nuevos campos o coexisten.

### 4.2 Propuesta de ampliación de `family-response.yaml`

```yaml
type: object
additionalProperties: false

properties:
  id:
    type: integer
    format: int64
  name:
    type: string
  ttsEnabled:
    type: boolean
  agentEnabled:
    type: boolean
  audioGeneralEnabled:
    type: boolean
  audioGeneralVolume:
    type: integer
  npcEnabled:
    type: boolean
  npcVoiceEnabled:
    type: boolean
  npcVoiceVolume:
    type: integer
  narrativeVoiceEnabled:
    type: boolean
  narrativeVoiceVolume:
    type: integer
  createdAt:
    type: string
    format: date-time
  updatedAt:
    type: string
    format: date-time
```

### 4.3 Endpoints que consume

| Método | Endpoint | Schema request | Schema response | Uso |
|--------|----------|---------------|-----------------|-----|
| GET | `/api/v1/family` | — | `api-family-response.yaml` | Cargar configuración actual |
| PATCH | `/api/v1/family` | `update-family-request.yaml` | `api-family-response.yaml` | Guardar cambios |
| POST | `/api/v1/auth/logout` | — (header Authorization) | — | Cierre sesión tras cambio PIN |

### 4.4 Modificación interna requerida: `api.ts`

El cliente API actual (`services/api.ts`) no dispone de método `patch`. Es necesario añadirlo:

```typescript
patch<T>(endpoint: string, data?: unknown): Promise<T> {
  return request<T>(endpoint, {
    method: 'PATCH',
    body: data ? JSON.stringify(data) : undefined,
  })
}
```

### 4.5 Handoffs detallados a otras capas

#### Backend debe:

1. **Ampliar `update-family-request.yaml`** con los nuevos campos de configuración global (audio general, NPC, voces con estados y porcentajes).
2. **Ampliar `family-response.yaml`** para devolver los mismos campos.
3. **Persistir** los nuevos campos en el modelo de familia (tabla `family` o equivalente).
4. **Validar** en PATCH:
   - `pin`: exactamente 4 dígitos numéricos si se proporciona.
   - Volúmenes: entero 0-100.
   - Campos booleanos: tipo boolean estricto.
5. **Compatibilidad hacia atrás**: decidir si `ttsEnabled`/`agentEnabled` se mapean a los nuevos campos o coexisten como aliases.
6. **Valores por defecto** para familias existentes sin configuración previa: sugerir `audioGeneralEnabled: true`, `audioGeneralVolume: 100`, `npcEnabled: true`, `npcVoiceEnabled: true`, `npcVoiceVolume: 100`, `narrativeVoiceEnabled: true`, `narrativeVoiceVolume: 100`.

#### Agents debe:

1. **Respetar `npcEnabled`**: si es `false`, el NPC no aparece, no se anima, no interviene en juego.
2. **Respetar `npcVoiceEnabled`**: si es `false` (con NPC activo), el NPC está presente pero no habla.
3. **Respetar `narrativeVoiceEnabled`**: independiente del NPC; controla la voz de lectura familiar.
4. **Respetar `audioGeneralEnabled`**: control maestro de audio sin afectar voces individualmente.
5. **No bloquear al niño**: la desactivación de voces o NPC no genera errores, bloqueos ni mensajes negativos en la experiencia de juego.

#### TTS debe:

1. **Aplicar `npcVoiceEnabled` y `npcVoiceVolume`**: silenciar generación de voz NPC cuando `enabled=false`; aplicar volumen proporcional cuando `enabled=true`.
2. **Aplicar `narrativeVoiceEnabled` y `narrativeVoiceVolume`**: independiente de voz NPC.
3. **Aplicar `audioGeneralEnabled`**: silencio global de audio cuando `enabled=false`.
4. **Independencia**: los tres controles de voz/audio son ortogonales; desactivar uno no afecta a los otros.

## 5. Riesgos y mitigaciones

| Riesgo | Impacto | Mitigación |
|--------|---------|------------|
| Coexistencia confusa entre `ttsEnabled`/`agentEnabled` (legacy) y nuevos campos | Campos duplicados, comportamiento ambiguo | Backend aclara mapeo o deprecia campos legacy. Frontend envía ambos durante transición. |
| Ausencia de método `patch` en `api.ts` | Imposible enviar PATCH | Añadir `patch()` al cliente API (cambio trivial, sin riesgo). |
| Campos nuevos no presentes en respuesta GET para familias existentes | Vista muestra valores undefined | Backend provee valores por defecto. Frontend aplica fallback en mapeo si algún campo es `null`/`undefined`. |
| Cambio de PIN + otros cambios: el logout puede sorprender al adulto | Pérdida de percepción de control | Toast previo al redirect: «PIN actualizado. Sesión cerrada por seguridad.» |
| Slider de porcentaje no accesible en táctil | Dificultad de uso en móvil/tableta | Combinar slider + `NubiNumberInput` como entrada precisa. Objetivo táctil ≥ 48dp. |
| PIN se almacena en estado plano en memoria | Exposición en devtools | No mostrar PIN como texto legible. Usar `NubiPinInput` con `masked=true`. Limpiar estado de PIN tras guardar. |
| Race condition: PATCH en vuelo + logout | Estado inconsistente | Bloquear botón «Guardar cambios» durante envío (`saving=true`). Logout solo tras confirmación de éxito. |

## 6. Preguntas de decisión al usuario

1. **Mapeo de campos legacy:** ¿`ttsEnabled` y `agentEnabled` deben mapearse a `audioGeneralEnabled`/`npcEnabled` respectivamente, o coexisten como campos independientes? La propuesta asume coexistencia durante transición, pero requiere confirmación del backend.

2. **Componente de porcentaje:** ¿Se prefiere un slider nativo (`<input type="range">`) combinado con `NubiNumberInput`, o se diseña un componente de slider personalizado con la estética Nubi? La propuesta usa slider nativo + number input por pragmatismo.

3. **Valores por defecto:** ¿Los valores sugeridos (todo activo al 100%) son correctos para familias existentes que no tengan configuración previa?

4. **Texto de ayuda por sección:** El FEAT-005 menciona «breve ayuda» para diferenciar Audio general, Voz NPC y Voz narrativa. ¿Se valida el siguiente texto?
   - Audio general: «Controla todo el sonido de la aplicación.»
   - Voz del NPC: «Voz de Nubi durante el juego. Si la apagas, Nubi sigue presente pero en silencio.»
   - Voz narrativa: «Voz de la lectura familiar. Independiente del NPC.»

## 7. Sprints propuestos

### Sprint 1 — Infraestructura de contratos y cliente API

**Objetivo:** Preparar la base técnica para que el frontend pueda consumir y enviar la configuración global completa.

**Tareas técnicas frontend:**

| # | Tarea | Descripción |
|---|-------|-------------|
| 1.1 | Ampliar `update-family-request.yaml` | Añadir los 8 nuevos campos (audio general, NPC, voces con estados y porcentajes) |
| 1.2 | Ampliar `family-response.yaml` | Añadir los 8 nuevos campos de respuesta |
| 1.3 | Añadir método `patch` a `api.ts` | Implementar `patch<T>()` en el cliente API |
| 1.4 | Ampliar interfaz `FamilyData` | Actualizar `useFamilyStatus.ts` con los nuevos campos |
| 1.5 | Crear interfaz `FamilyGlobalConfig` | Tipo interno para el estado de configuración |
| 1.6 | Crear `updateFamilyConfig` en `familyService.ts` | Función que invoca `PATCH /api/v1/family` con el payload parcial |

**Criterios de aceptación:**
- Los contratos YAML incluyen todos los campos nuevos con tipos y restricciones.
- `apiClient.patch()` funciona correctamente.
- `FamilyData` y `FamilyGlobalConfig` tipados correctamente.
- `updateFamilyConfig` realiza PATCH y maneja errores (0, 400, 401, 5xx).

**Evidencias esperadas:**
- Contratos YAML actualizados y coherentes.
- Test manual: `updateFamilyConfig({ audioGeneralVolume: 50 })` envía PATCH correcto.
- TypeScript compila sin errores (`tsc`).

---

### Sprint 2 — Componentes de configuración y estado local

**Objetivo:** Construir los componentes visuales reutilizables y la lógica de estado (draft, conservación de valores, independencia de controles).

**Tareas técnicas frontend:**

| # | Tarea | Descripción |
|---|-------|-------------|
| 2.1 | Crear componente `ConfigSection` | Wrapper con título, descripción breve y slot para controles |
| 2.2 | Crear componente `ToggleWithPercentage` | Combina NubiToggle + slider + NubiNumberInput; lógica de conservación |
| 2.3 | Implementar composable `useGlobalConfig` | Estado draft/persisted/lastNonZero, lógica de toggles y porcentajes |
| 2.4 | Implementar lógica de conservación | Último valor ≠ 0, restauración al reactivar, apagado automático al poner 0 |
| 2.5 | Implementar independencia de controles | Audio general no afecta voces; NPC no afecta voz NPC al desactivarse |
| 2.6 | Crear historias Histoire para `ConfigSection` y `ToggleWithPercentage` | Catálogo de componentes para desarrollo y revisión visual |

**Criterios de aceptación:**
- `ToggleWithPercentage` permite toggle on/off, ajuste de porcentaje 0-100, establece 0 = apaga toggle.
- Al apagar y reactivar, se recupera el último valor ≠ 0.
- `ConfigSection` muestra título, descripción y slot consistentes.
- `useGlobalConfig` inicializa desde datos de familia, gestiona draft y conservación.
- Los tres controles con porcentaje (audio general, voz NPC, voz narrativa) son independientes entre sí.
- Componentes visibles en Histoire (`histoire:dev`).

**Evidencias esperadas:**
- Historias Histoire funcionales para ambos componentes.
- Test manual: secuencia toggle off → toggle on recupera volumen.
- Test manual: poner slider a 0 → toggle se apaga.
- TypeScript compila sin errores.

---

### Sprint 3 — Vista de configuración completa e integración con API

**Objetivo:** Ensamblar la vista `ConfiguracionView.vue` con las 5 secciones, conexión a API, validación de PIN, acción «Guardar cambios» y logout tras cambio de PIN.

**Tareas técnicas frontend:**

| # | Tarea | Descripción |
|---|-------|-------------|
| 3.1 | Implementar `ConfiguracionView.vue` | Layout con 5 secciones usando ConfigSection y ToggleWithPercentage |
| 3.2 | Sección Audio general | Toggle + porcentaje, independiente de voces |
| 3.3 | Sección NPC | Toggle on/off sin porcentaje |
| 3.4 | Sección Voz del NPC | Toggle + porcentaje, independiente de NPC |
| 3.5 | Sección Voz narrativa | Toggle + porcentaje, independiente de NPC y voz NPC |
| 3.6 | Sección PIN familiar | Dos NubiPinInput (masked=true): nuevo PIN + confirmación |
| 3.7 | Acción «Guardar cambios» | NubiButton que envía PATCH con diff draft vs persisted |
| 3.8 | Validación de PIN | Coincidencia, 4 dígitos, bloqueo de envío si no coincide |
| 3.9 | Integración con logout | Tras PATCH exitoso con cambio PIN → logout + navigateTo('Home') |
| 3.10 | Estados de carga y error | Loading en carga inicial, saving en botón, toasts de éxito/error |
| 3.11 | i18n completo | Todas las etiquetas, descripciones, mensajes de error y toasts |
| 3.12 | Verificación de accesibilidad | Labels, aria-labels, objetivos táctiles ≥ 48dp, estados sin depender solo de color |

**Criterios de aceptación:**
- Vista muestra 5 secciones diferenciadas en móvil y tableta.
- Cada sección funciona según su lógica específica (independencia de controles).
- «Guardar cambios» envía solo campos modificados.
- PIN no coincidente muestra aviso y bloquea envío.
- PIN correcto → PATCH → logout → Home.
- Vista no muestra datos infantiles, progreso ni clasificaciones.
- Controles táctiles ≥ 48dp, etiquetas visibles, estados distinguibles sin solo color.
- i18n completo en español.
- TypeScript compila sin errores (`tsc`).

**Evidencias esperadas:**
- Test manual: flujo completo de carga → modificación → guardado → verificación en backend.
- Test manual: flujo de cambio de PIN → logout automático → retorno a Home.
- Test manual: PIN no coincidente → aviso visible → envío bloqueado.
- Test manual: apagado de NPC → voz NPC conservada → reactivación de NPC restaura voz.
- Test manual: porcentaje a 0 → toggle se apaga → reactivar recupera último valor ≠ 0.
- Verificación responsive en móvil portrait, móvil landscape y tableta.
- `tsc` y `vite build` sin errores.
