# Estrategia de Pruebas - Orientación y PWA

## Orientación

### Objetivo

Verificar que la aplicación mantiene la composición horizontal permanente en todos los escenarios de orientación, sin mostrar indicaciones para girar el dispositivo.

### Escenarios de Prueba

#### 1. Orientación Horizontal (Esperada)

**Pasos:**
1. Abrir aplicación en dispositivo Android
2. Sostener dispositivo en orientación horizontal
3. Navegar por las distintas vistas

**Resultado Esperado:**
- Composición horizontal correcta
- Elementos visibles y accesibles
- Sin escalado adicional
- Objetivos táctiles en tamaño adecuado

#### 2. Orientación Vertical (Fallback)

**Pasos:**
1. Abrir aplicación en dispositivo Android
2. Sostener dispositivo en orientación vertical
3. Navegar por las distintas vistas
4. Intentar interacción con elementos

**Resultado Esperado:**
- Composición horizontal se mantiene (escalada)
- No se muestra mensaje de "gire el dispositivo"
- Contenido visible y legible
- Objetivos táctiles pueden estar reducidos (aceptable)

#### 3. Cambio de Orientación Dinámico

**Pasos:**
1. Abrir aplicación en orientación horizontal
2. Rotar dispositivo a vertical
3. Rotar dispositivo a horizontal nuevamente
4. Repetir múltiples veces

**Resultado Esperado:**
- Transición suave entre orientaciones
- Estado de la aplicación se preserva
- No se pierden datos de formularios
- WebSocket se mantiene conectado

#### 4. Segundo Plano y Retorno

**Pasos:**
1. Abrir aplicación
2. Navegar a una vista con estado (ej: GameView)
3. Presionar botón Home (segundo plano)
4. Esperar 10-30 segundos
5. Retornar a la aplicación

**Resultado Esperado:**
- Estado de la aplicación se preserva
- WebSocket reconecta si se desconectó
- No se pierde progreso en formularios
- Vista actual se mantiene

### Dispositivos de Prueba

| Dispositivo | Prioridad | Escenarios |
|-------------|-----------|------------|
| Samsung Galaxy A15 | Alta | Todos |
| Galaxy Tab S4 (emulado) | Media | 1, 2, 3 |
| Pixel 8 (emulado) | Media | 1, 2, 3 |
| Galaxy S20 (emulado) | Media | 1, 2, 3 |

### Métricas a Monitorear

- Tiempo de transición entre orientaciones
- Factor de escala aplicado en vertical
- Tamaño de objetivos táctiles (mínimo 44x44px recomendado)
- Estado de conexión WebSocket durante transiciones

---

## PWA

### Objetivo

Verificar que la PWA es instalable opcionalmente y funciona correctamente sin interferir con el acceso por URL.

### Escenarios de Prueba

#### 1. Instalación Manual

**Pasos:**
1. Abrir aplicación en Chrome Android
2. Acceder al menú del navegador (tres puntos)
3. Buscar opción "Instalar aplicación" o "Añadir a pantalla principal"
4. Confirmar instalación
5. Verificar icono en pantalla principal

**Resultado Esperado:**
- Opción de instalación disponible en menú
- Icono se añade a pantalla principal
- Al abrir desde icono, se lanza en modo standalone
- Sin barra de navegador visible

#### 2. Acceso sin Instalación

**Pasos:**
1. Abrir aplicación en Chrome Android
2. NO instalar la PWA
3. Navegar por la aplicación
4. Cerrar y reabrir navegador
5. Acceder nuevamente via URL

**Resultado Esperado:**
- Aplicación funciona normalmente sin instalación
- No se muestra prompt automático de instalación
- URL se mantiene accesible
- Estado se preserva entre sesiones (sessionStorage)

#### 3. Caché de Recursos Estáticos

**Pasos:**
1. Abrir aplicación con conexión
2. Navegar por varias vistas
3. Desactivar conexión (modo avión)
4. Recargar página
5. Verificar que recursos estáticos cargan

**Resultado Esperado:**
- HTML, CSS, JS cargan desde caché
- Iconos y recursos estáticos disponibles
- API calls fallan con error 503 (offline)
- Aplicación muestra estado de conexión

#### 4. Actualización de Caché

**Pasos:**
1. Instalar PWA
2. Realizar deploy de nueva versión
3. Abrir aplicación nuevamente
4. Verificar que nueva versión se carga

**Resultado Esperado:**
- Service Worker detecta nueva versión
- Caché se actualiza automáticamente
- Usuario ve nueva versión sin intervención manual
- No se requiere desinstalar/reinstalar

#### 5. Flujo Infantil (No Promoción)

**Pasos:**
1. Abrir aplicación
2. Navegar por flujo infantil (Home → Game)
3. Verificar que no se muestra prompt de instalación
4. Verificar que no hay elementos promocionando instalación

**Resultado Esperado:**
- No se muestra prompt automático
- No hay banners promocionando instalación
- Flujo infantil no se interrumpe
- Instalación solo accesible desde menú del navegador

### Criterios de Aceptación PWA

- [ ] PWA instalable desde menú del navegador
- [ ] No se muestra prompt automático de instalación
- [ ] Aplicación funciona sin instalación
- [ ] Icono y nombre correctos en pantalla principal
- [ ] Modo standalone funciona (sin barra de navegador)
- [ ] Caché de recursos estáticos funciona
- [ ] Actualización de caché funciona
- [ ] Flujo infantil no se interrumpe por PWA

### Herramientas de Prueba

- **Chrome DevTools** → Application → Manifest
- **Chrome DevTools** → Application → Service Workers
- **Chrome DevTools** → Application → Cache Storage
- **Lighthouse** → PWA audit
- **Modo avión** para pruebas offline

---

## WebSocket

### Objetivo

Verificar que el cliente WebSocket reconecta automáticamente tras pérdida de conexión con backoff exponencial.

### Escenarios de Prueba

#### 1. Reconexión Automática

**Pasos:**
1. Abrir aplicación con WebSocket conectado
2. Desactivar conexión (modo avión)
3. Esperar 5 segundos
4. Reactivar conexión
5. Verificar que WebSocket reconecta

**Resultado Esperado:**
- WebSocket detecta pérdida de conexión
- Intenta reconectar con backoff exponencial
- Reconecta automáticamente cuando hay conexión
- Estado de conexión se actualiza en UI

#### 2. Backoff Exponencial

**Pasos:**
1. Desactivar conexión
2. Monitorear intentos de reconexión en consola
3. Verificar intervalos entre intentos

**Resultado Esperado:**
- Primer intento: ~1 segundo
- Segundo intento: ~2 segundos
- Tercer intento: ~4 segundos
- Intervalo máximo: 30 segundos
- Jitter aplicado (aleatoriedad)

#### 3. Sesión Expirada

**Pasos:**
1. Abrir aplicación con WebSocket conectado
2. Esperar a que sesión expire (o forzar desde backend)
3. Verificar que se recibe evento SESSION_EXPIRED
4. Verificar que se redirige a Home

**Resultado Esperado:**
- Evento SESSION_EXPIRED recibido
- Usuario redirigido a Home
- Mensaje apropiado mostrado
- WebSocket se desconecta limpiamente

### Métricas a Monitorear

- Tiempo de detección de desconexión
- Número de intentos de reconexión
- Intervalo entre intentos
- Tiempo total de reconexión
- Eventos recibidos durante reconexión

---

## Guards de Navegación

### Objetivo

Verificar que los guards de navegación protegen correctamente las rutas y redirigen a Home cuando no hay sesión/PIN.

### Escenarios de Prueba

#### 1. Acceso Directo a /panel sin Autenticación

**Pasos:**
1. Cerrar sesión (o usar sesión nueva)
2. Escribir URL `/panel` directamente en navegador
3. Presionar Enter

**Resultado Esperado:**
- Guard intercepta navegación
- Redirige a Home con `router.replace()`
- No se muestra contenido de /panel
- Historial de navegador no incluye /panel

#### 2. Acceso Directo a /game/:childId sin Sesión

**Pasos:**
1. Cerrar sesión (o usar sesión nueva)
2. Escribir URL `/game/123` directamente en navegador
3. Presionar Enter

**Resultado Esperado:**
- Guard intercepta navegación
- Redirige a Home con `router.replace()`
- No se muestra contenido de /game
- Historial de navegador no incluye /game

#### 3. Navegación Interna con router.replace()

**Pasos:**
1. Estar en Home
2. Navegar a /panel (con autenticación)
3. Navegar a /game/123 (con sesión activa)
4. Presionar botón "atrás" del navegador

**Resultado Esperado:**
- Navegación usa `replace()` en lugar de `push()`
- Historial no acumula rutas
- Botón "atrás" no tiene efecto funcional
- No se puede volver a rutas anteriores

#### 4. Recarga de Página

**Pasos:**
1. Estar en /panel o /game
2. Recargar página (F5 o recargar en móvil)
3. Verificar comportamiento

**Resultado Esperado:**
- Recarga redirige a Home
- No se recupera ruta anterior
- Sesión se preserva en sessionStorage
- Usuario debe navegar nuevamente

### Criterios de Aceptación Guards

- [ ] /panel redirige a Home si no hay autenticación
- [ ] /game/:childId redirige a Home si no hay sesión de niño
- [ ] Navegación usa `router.replace()` consistentemente
- [ ] Historial de navegador no acumula rutas
- [ ] Recarga siempre redirige a Home
- [ ] Botón "atrás" no tiene efecto funcional

---

## Ejecución de Pruebas

### Checklist Pre-Pruebas

- [ ] Samsung Galaxy A15 cargado y disponible
- [ ] Chrome actualizado a versión 143+
- [ ] Backend levantado y accesible
- [ ] URL de backend configurada en `.env`
- [ ] Service Worker registrado correctamente
- [ ] Manifest PWA válido (verificar con Lighthouse)

### Orden de Ejecución

1. **Pruebas de Orientación** (Galaxy A15 físico)
2. **Pruebas de PWA** (Galaxy A15 físico)
3. **Pruebas de WebSocket** (Galaxy A15 físico)
4. **Pruebas de Guards** (Galaxy A15 físico)
5. **Pruebas en Emuladores** (Tab S4, Pixel 8, S20)

### Registro de Resultados

Crear informe de pruebas con:
- Dispositivo y versión de SO/navegador
- Escenario probado
- Resultado (pass/fail)
- Evidencia (screenshots, videos, logs)
- Incidencias encontradas

---

## Notas

- Las pruebas en dispositivo físico son obligatorias
- Las pruebas en emuladores son complementarias
- Documentar cualquier comportamiento inesperado
- Reportar problemas de rendimiento o usabilidad
- Validar que objetivos táctiles son adecuados para 3-4 años
