# Sprint 089 - backend
# -----------------------------------------------

## Goal
Migrar la integracion de Telegram desde la linea discontinada `telegrambots-spring-boot-starter`/`telegrambots-abilities` (6.7.0) hacia `telegrambots-springboot-longpolling-starter`/`telegrambots-client` (10.2.1), reescribiendo el adaptador contra la nueva API de interfaces (`SpringLongPollingBot`, `LongPollingUpdateConsumer`) y eliminando la dependencia del modulo `abilities` (incluyendo `AbilityBot`, `MapDBContext` y el patron `Ability`).

## Status
status: proposed
started_at:
closed_at:
blocked_by:
waiting_for: decision sobre version objetivo y estrategia de persistencia de chatId

## Context

### Estado actual (linea discontinada)

El proyecto usa `telegrambots-spring-boot-starter:6.7.0` + `telegrambots-abilities:6.7.0`. Esta linea de artefactos esta **discontinuada upstream**: la ultima version fue 6.9.7.1 (feb 2024) y fue reemplazada por un par de starters separados (`telegrambots-springboot-longpolling-starter` / `telegrambots-springboot-webhook-starter`), actualmente en 10.2.1 (ago 2026). No recibira mas correcciones de seguridad, compatibilidad con Jakarta EE 11 ni alineamiento con Jackson 3.

SPRINT-088 identifico este riesgo y lo registro como follow-up explicito: _"Decide separately (own sprint, not this one) whether to migrate off `telegrambots-spring-boot-starter`/`-abilities` to `telegrambots-springboot-longpolling-starter`"_.

### Superficie de codigo afectada

| Archivo | Rol actual | Impacto |
|---|---|---|
| `TelegramAdapter.java` | Extiende `AbilityBot`, implementa `TelegramPort`. Usa `MapDBContext` para persistir `chatId`, patron `Ability` para `/start`, `silent.send()` para notificar. | **Reescritura completa.** `AbilityBot` y `MapDBContext` desaparecen en la nueva linea. |
| `TelegramBotRegistration.java` | Crea manualmente `TelegramBotsApi` con `DefaultBotSession.class` y registra el bot. | **Eliminar.** El nuevo starter autoconfigura `TelegramBotsLongPollingApplication` + `TelegramBotInitializer`. |
| `TelegramBotConfig.java` | Inyecta `app.telegram.bot.name` y `app.telegram.bot.token` via `@Value`. | **Adaptar.** El token pasa a `SpringLongPollingBot.getBotToken()`. El nombre puede conservarse o eliminarse segun decision. |
| `NoOpTelegramAdapter.java` | Fallback cuando `app.telegram.bot.token` esta vacio. | **Mantener.** Logica independiente de la libreria Telegram. |
| `TelegramPort.java` | Interfaz de salida con `sendToTelegram(ContactMessage)`. | **Sin cambios.** El puerto del dominio no depende de la libreria. |
| `ContactService.java` | Usa `TelegramPort` para notificar. | **Sin cambios.** |
| `pom.xml` | Declara `telegrambots-spring-boot-starter:6.7.0` + `telegrambots-abilities:6.7.0`. | **Reemplazar dependencias.** |
| `application.yml` | Seccion `app.telegram.bot.name` / `app.telegram.bot.token`. | **Posible ajuste** de propiedades segun convencion del nuevo starter. |

### Cambios de API entre linea 6.x y 10.x

| Concepto | Linea 6.x (actual) | Linea 10.x (objetivo) |
|---|---|---|
| Bot base class | `AbilityBot` (abstract, con DB integrada) | `SpringLongPollingBot` (interfaz: `getBotToken()` + `getUpdatesConsumer()`) |
| Consumo de updates | `Ability` pattern + `onUpdateReceived()` | `LongPollingUpdateConsumer.consume(Update)` o `consume(List<Update>)` |
| Envio de mensajes | `silent.send(text, chatId)` | `TelegramClient.execute(new SendMessage(chatId, text))` |
| Cliente HTTP | Integrado en `AbilityBot` | `OkHttpTelegramClient` (artefacto separado `telegrambots-client`) |
| Persistencia interna | `MapDBContext` + `Var<T>` | **No incluida.** Cada bot gestiona su propio estado. |
| Registro del bot | Manual: `TelegramBotsApi` + `DefaultBotSession` | Autoconfiguracion: `TelegramBotsLongPollingApplication` + `TelegramBotInitializer` |
| Comandos | `Ability.builder().name("start")...` | Manejo manual en `consume(Update)` verificando `update.getMessage().isCommand()` |
| Propiedad de activacion | `@ConditionalOnExpression` sobre token | `@ConditionalOnProperty(prefix = "telegrambots", name = "enabled")` (del starter) |

### Estado upstream (2026-09-04)

- **`telegrambots-springboot-longpolling-starter:10.2.1`** — ultima version estable (ago 2026). POM declara `spring.version=3.5.5` (Spring Boot 3.x, no 4.x).
- **Issue #1561** — _"Migrate from Jackson 2 to Jackson 3"_ — **ABIERTO**, sin asignar, sin PR. Nota importante: originalmente incluia Spring Boot 4, pero ahora **solo cubre Jackson 3**. Spring Boot 4 se tratara en un issue separado.
- **Issue #1513** — _"Java process never stops after SpringApplication.exit"_ — **ABIERTO**. Workaround documentado: usar `LongPollingUpdateConsumer` (no la variante deprecated `LongPollingSingleThreadUpdateConsumer`) + `ScheduledExecutorService` bean propio.
- **Issue #1563** — _"LongPollingSingleThreadUpdateConsumer should not be a static variable"_ — **ABIERTO**. La interfaz esta **@Deprecated** desde 10.x; se recomienda `DefaultLongPollingUpdateConsumer` o implementar `LongPollingUpdateConsumer` directamente.
- **Issue #1526** — _"@AfterBotRegistration not working with CGLIB proxy"_ — **ABIERTO** pero el codigo actual en master ya usa `AnnotationUtils.findAnnotation()` (fix aplicado).

### Riesgo de compatibilidad Jackson 2/3 con Spring Boot 4.1.0

El starter 10.2.1 declara Spring Boot 3.5.5 en su POM. Sin embargo, sus dependencias reales (`spring-boot`, `spring-boot-autoconfigure`) se resuelven contra el BOM del consumidor. En nuestro caso, Spring Boot 4.1.0 gestionara las versiones de `spring-boot` y `spring-boot-autoconfigure`. El riesgo real es si `telegrambots-longpolling` o `telegrambots-client` declaran dependencias transitorias con Jackson 2 que colisionen con el modo Jackson-2-preferred de nuestra app (`spring-boot-jackson2` + `preferred-json-mapper: jackson2`). Dado que el starter solo depende de `telegrambots-longpolling` (que a su vez depende de `telegrambots-meta` y `telegrambots-client`), y este ultimo usa OkHttp + Jackson para serializar requests HTTP hacia la Telegram Bot API, la coexistencia deberia ser segura siempre que el `ObjectMapper` de Jackson 2 que proporciona Spring Boot sea el que el starter inyecta.

**Verificacion obligatoria**: `mvn dependency:tree` despues del cambio para confirmar ausencia de conflictos Jackson 2/3, y prueba manual de envio de mensaje.

## Technical Design

### Arquitectura post-migracion

```
ContactService → TelegramPort → TelegramAdapter (implementa SpringLongPollingBot + LongPollingUpdateConsumer + TelegramPort)
                                       ↓
                              TelegramClient (OkHttpTelegramClient) → Telegram Bot API
                                       ↓
                              consume(Update) → manejo manual de /start → persistencia de chatId
```

### Decisiones de diseno pendientes

#### 1. Persistencia de chatId (reemplazo de MapDBContext)

El adaptador actual persiste el `chatId` del chat registrado via `/start` usando `MapDBContext.offlineInstance("cache/telegram-bot-db")` + `Var<Long>`. Este mecanismo desaparece con la eliminacion de `telegrambots-abilities`.

**Opciones**:

| Opcion | Pros | Contras |
|---|---|---|
| **A) Tabla JPA `telegram_notification_chat`** | Consistente con el patron de persistencia del proyecto. Transaccional. Sobrevive restarts. | Overkill para un solo valor Long. Requiere migracion Liquibase. |
| **B) Fichero simple (`cache/telegram-chat-id.txt`)** | Minimal. Sin dependencias nuevas. Sobrevive restarts. | No transaccional. Gestion de concurrencia manual. |
| **C) Propiedad de aplicacion mutable en runtime** | Simple. | No sobrevive restarts a menos que se persista externamente. Requiere mecanismo de recarga. |
| **D) Variable de entorno / config estatica** | Zero estado runtime. | El chatId se pierde en cada deploy. No permite registro dinamico via `/start`. |

**Recomendacion**: **Opcion A** (tabla JPA). Es la mas consistente con el patron hexagonal del proyecto, permite tests con H2/Testcontainers, y la complejidad adicional es minima (una entidad, un repositorio, una migracion). La tabla tendria una sola fila con el chatId registrado.

#### 2. Version objetivo

| Version | Spring Boot target | Jackson | Notas |
|---|---|---|---|
| 10.2.1 (actual) | 3.5.5 (BOM) | 2 | Ultima estable. Issue #1561 (Jackson 3) abierto. Boot 4 diferido. |
| Esperar a Boot 4 | — | 3 | Sin fecha conocida. Issue separado aun no creado. |

**Recomendacion**: **Migrar ahora a 10.2.1**. Razones:
1. La linea 6.x esta discontinuada y no recibira parches de seguridad.
2. El starter 10.2.1 funciona contra Spring Boot 4.1.0 siempre que se verifique la coexistencia Jackson (verificacion obligatoria en criterios de aceptacion).
3. Cuando upstream migre a Boot 4, sera un cambio de BOM (nuestro `spring-boot-starter-parent` ya gestiona las versiones de `spring-boot`/`spring-boot-autoconfigure`), no una reescritura de API.
4. El coste de esperar es mantener una dependencia discontinuada sin parches de seguridad.

#### 3. Manejo del comando `/start`

El patron `Ability` desaparece. El nuevo adaptador debe:
1. Implementar `LongPollingUpdateConsumer.consume(Update)`.
2. Verificar `update.hasMessage() && update.getMessage().isCommand()`.
3. Extraer el texto del comando y comparar con `/start`.
4. Si coincide, persistir el `chatId` y responder con un mensaje de confirmacion via `TelegramClient.execute(new SendMessage(...))`.

#### 4. Activacion condicional

El starter nuevo usa `@ConditionalOnProperty(prefix = "telegrambots", name = "enabled", havingValue = "true", matchIfMissing = true)`. Esto significa que el autoconfiguracion se activa por defecto. Para mantener la semantica actual (bot solo activo cuando `app.telegram.bot.token` no esta vacio), hay dos opciones:

- **A)** Desactivar la autoconfiguracion del starter (`telegrambots.enabled=false`) y registrar el bot manualmente como hoy.
- **B)** Mantener la autoconfiguracion pero hacer que `TelegramAdapter` solo se declare como bean cuando el token no esta vacio (via `@ConditionalOnExpression` como hoy). El starter encontrara el bean `SpringLongPollingBot` y lo registrara automaticamente.

**Recomendacion**: **Opcion B**. Es mas limpia y aprovecha la autoconfiguracion. El `TelegramAdapter` ya tiene `@ConditionalOnExpression("'${app.telegram.bot.token:}' != ''")`, lo cual garantiza que solo existe como bean cuando hay token.

#### 5. Propiedades de configuracion

El nuevo starter no lee `app.telegram.bot.name` ni `app.telegram.bot.token` — espera que cada `SpringLongPollingBot` devuelva su token via `getBotToken()`. El nombre del bot no es necesario en la nueva API (no se usa para registro).

**Propuesta**: Conservar `app.telegram.bot.token` en `application.yml` (inyectado en `TelegramAdapter`). Eliminar `app.telegram.bot.name` si no se usa en otro sitio.

### Contratos

- **Sin cambios en `docs/contracts`**. La migracion es interna al adaptador de infraestructura. El puerto `TelegramPort` y el caso de uso `ContactUseCase` no cambian.
- **Nueva migracion Liquibase** (si se opta por tabla JPA para chatId).

### Dependencias externas

- `telegrambots-springboot-longpolling-starter:10.2.1` — reemplaza `telegrambots-spring-boot-starter:6.7.0`.
- `telegrambots-client:10.2.1` — nuevo. Proporciona `OkHttpTelegramClient`.
- **Eliminar**: `telegrambots-abilities:6.7.0`.

## Tasks

- [ ] **T1**: Actualizar `pom.xml` — reemplazar `telegrambots-spring-boot-starter:6.7.0` + `telegrambots-abilities:6.7.0` por `telegrambots-springboot-longpolling-starter:10.2.1` + `telegrambots-client:10.2.1`.
- [ ] **T2**: Crear entidad JPA `TelegramNotificationChatJpaEntity` + repositorio `TelegramNotificationChatJpaRepository` para persistir el chatId (tabla `telegram_notification_chat`, una sola fila).
- [ ] **T3**: Crear migracion Liquibase para la tabla `telegram_notification_chat`.
- [ ] **T4**: Reescribir `TelegramAdapter.java` — implementar `SpringLongPollingBot` + `LongPollingUpdateConsumer` + `TelegramPort`. Inyectar `TelegramClient` (bean) y repositorio de chatId. Manejar `/start` en `consume(Update)`. Enviar mensajes via `TelegramClient.execute(new SendMessage(...))`.
- [ ] **T5**: Declarar bean `TelegramClient` (`OkHttpTelegramClient`) en `TelegramBotConfig.java`.
- [ ] **T6**: Eliminar `TelegramBotRegistration.java` (autoconfiguracion del nuevo starter).
- [ ] **T7**: Adaptar `TelegramBotConfig.java` — eliminar `name` si no se usa, mantener `token`.
- [ ] **T8**: Actualizar `application.yml` — evaluar si `app.telegram.bot.name` sigue siendo necesario. Documentar que `telegrambots.enabled=true` es el default del starter.
- [ ] **T9**: Verificar `mvn dependency:tree` — confirmar ausencia de conflictos Jackson 2/3 entre el starter y `spring-boot-jackson2`.
- [ ] **T10**: Verificar `mvn clean test` — 854/854 tests verdes (el bot no tiene tests unitarios actualmente, pero la compilacion y el contexto Spring no deben romperse).
- [ ] **T11**: Prueba manual end-to-end: arrancar la app con token configurado, enviar `/start` al bot desde Telegram, verificar que el chatId se persiste, enviar un mensaje de contacto via la API y verificar que llega al chat de Telegram.
- [ ] **T12**: Prueba manual de shutdown: verificar que `Ctrl+C` / `SpringApplication.exit` detiene el proceso limpiamente (mitigar riesgo #1513).

## Acceptance Criteria

1. `pom.xml` no contiene `telegrambots-spring-boot-starter` ni `telegrambots-abilities`.
2. `pom.xml` contiene `telegrambots-springboot-longpolling-starter:10.2.1` + `telegrambots-client:10.2.1`.
3. `TelegramAdapter` no extiende `AbilityBot` ni usa ninguna clase del paquete `org.telegram.abilitybots.*`.
4. `TelegramBotRegistration.java` no existe.
5. `mvn dependency:tree` no muestra conflictos Jackson 2/3.
6. `mvn clean test` pasa con 854/854 tests verdes.
7. Manual: `/start` registra el chatId y responde confirmacion.
8. Manual: envio de contacto via API notifica al chat de Telegram.
9. Manual: shutdown limpio sin procesos colgados.
10. `NoOpTelegramAdapter` sigue funcionando cuando el token esta vacio.

## Manual Tests

1. **Arranque con token**: `mvn spring-boot:run` con `TELEGRAM_TOKEN=<token>` y `TELEGRAM_BOT=<nombre>`. Confirmar en logs que el bot se registra sin errores.
2. **Comando /start**: Enviar `/start` al bot desde una cuenta de Telegram. Verificar respuesta de confirmacion y que la tabla `telegram_notification_chat` tiene una fila con el chatId.
3. **Notificacion de contacto**: Enviar un POST a `/api/v1/contact` con un mensaje valido. Verificar que el chat de Telegram recibe la notificacion con formato `[TYPE] message`.
4. **Sin token**: Arrancar sin `TELEGRAM_TOKEN`. Verificar en logs el warn de `NoOpTelegramAdapter` y que el envio de contacto no falla (solo guarda en BD).
5. **Shutdown**: Tras arrancar con token, hacer `Ctrl+C`. Verificar que el proceso termina en < 5 segundos sin hilos colgados.
6. **Restart con chatId persistido**: Tras registrar chatId y hacer restart, verificar que las notificaciones siguen llegando al mismo chat sin necesidad de re-enviar `/start`.

## Risks

- **Jackson 2/3 coexistence**: El starter 10.2.1 usa Jackson 2 internamente (para serializar requests HTTP hacia la Telegram API). Nuestro proyecto usa `spring-boot-jackson2` + `preferred-json-mapper: jackson2`. Si el starter inyecta su propio `ObjectMapper` en vez de usar el de Spring, podria haber dos `ObjectMapper` en el contexto. **Mitigacion**: `mvn dependency:tree` + prueba manual de envio. Si hay conflicto, declarar el bean `TelegramClient` explicitamente con el `ObjectMapper` de Spring.
- **Issue #1513 (process never stops)**: Reportado contra versiones 8.x-9.x. Puede estar resuelto en 10.x. **Mitigacion**: prueba manual de shutdown (T12). Si persiste, aplicar workaround: bean `ScheduledExecutorService` propio + `LongPollingUpdateConsumer` (no la variante deprecated).
- **Issue #1561 (Jackson 3 migration)**: Abierto sin fecha. Cuando se resuelva, sera necesario verificar compatibilidad con nuestro Jackson-2-preferred. **Mitigacion**: la migracion a 10.2.1 ahora no bloquea una futura actualizacion; el cambio sera de version, no de API.
- **Perdida de datos en cache/telegram-bot-db**: El fichero MapDB actual (`cache/telegram-bot-db`) contiene el chatId registrado. Tras la migracion, este fichero ya no se lee. **Mitigacion**: si existe un chatId registrado en produccion, extraerlo del MapDB antes del deploy y insertarlo manualmente en la nueva tabla `telegram_notification_chat`.
- **`LongPollingSingleThreadUpdateConsumer` esta @Deprecated**: No usar. Implementar `LongPollingUpdateConsumer` directamente.

## Dependencies

- Este sprint es independiente de frontend, agents y tts.
- La migracion no afecta contratos en `docs/contracts`.
- Si se opta por tabla JPA (opcion A para chatId), requiere migracion Liquibase (DDL en `docs/contracts/ddl`).

## Agent Instruction

- No modificar `TelegramPort.java` ni `ContactService.java` — el puerto y el caso de uso no cambian.
- No modificar `NoOpTelegramAdapter.java` — sigue siendo el fallback cuando no hay token.
- Mantener `@ConditionalOnExpression("'${app.telegram.bot.token:}' != ''")` en `TelegramAdapter` para la activacion condicional.
- No usar `LongPollingSingleThreadUpdateConsumer` (deprecated). Implementar `LongPollingUpdateConsumer` directamente.
- Verificar `mvn dependency:tree` antes de considerar la migracion completa.
- Si la prueba de shutdown (T12) falla, documentar el workaround aplicado y abrir un follow-up trackeando el issue #1513.
- Keep code, comments, and names in English.

## Notes

- Sprint solicitado como follow-up de SPRINT-088 (dependency audit), que identifico la linea 6.x como discontinuada y recomendo sprint dedicado.
- Version 10.2.1 verificada en Maven Central (ago 2026). Re-verificar version disponible antes de ejecutar si el sprint se pick-up con retraso significativo.
- Issue upstream #1561 (Jackson 3) sigue abierto. Spring Boot 4 se tratara en un issue separado segun la descripcion actualizada del issue. Migrar ahora a 10.2.1 no genera deuda adicional de reescritura de API.
- La tabla `telegram_notification_chat` es minima (una sola fila con un BIGINT). Si en el futuro se soportan multiples chats de notificacion, la tabla ya esta preparada para crecer.
