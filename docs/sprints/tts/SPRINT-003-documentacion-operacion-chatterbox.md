# Sprint 003 - TTS

## Goal
Crear la documentación técnica y preparar los handoffs operativos para el servicio TTS nuevo con Chatterbox como único proveedor.

## Status
status: completed
started_at: 2026-07-22 00:00:00
closed_at: 2026-07-22 00:00:00
blocked_by:
waiting_for:

## Tasks
- [x] Crear la documentación de `framework/tts` para describir Chatterbox, sus perfiles de voz y el formato de salida. (verificado)
- [x] Actualizar README raíz y documentación relacionada para declarar Chatterbox como único proveedor y ausencia de fallback automático. (verificado)
- [x] Documentar las variables de entorno requeridas para TTS y Chatterbox único. (verificado)
- [x] Documentar la diferencia entre ejecución local de TTS en host (`127.0.0.1:4123`) y ejecución de TTS en Docker con red independiente. (verificado)
- [x] Registrar el handoff a infraestructura: debe proporcionar una URL/ruta alcanzable desde la red TTS hacia Chatterbox; TTS no define redes Docker externas. (verificado)
- [x] Documentar que backend valida texto/locales y que frontend/backend mantienen continuidad cuando no hay audio. (verificado)
- [x] Verificar que la documentación no presenta selección de proveedor como opción para familias o menores. (verificado)

## Risks
- Documentar `127.0.0.1` sin contexto puede inducir configuraciones fallidas en Docker.
- Puede quedar una dependencia operativa de Coqui fuera del repositorio que requiera confirmación de infraestructura.

## Dependencies
- Sprint 001 y Sprint 002 completados.
- `docs/product/features/tts/FEAT-001-Chatterbox-unico-proveedor-TTS.md`.
- `docs/product/decisions/ADR-013-Chatterbox-unico-proveedor-TTS.md`.
- Infraestructura debe confirmar el mecanismo de acceso desde la red TTS al contenedor Chatterbox.

## Agent Instruction
- Actualizar únicamente documentación relacionada con el alcance de TTS y sus handoffs.
- No tomar decisiones de red, Docker Compose o infraestructura; registrar dependencias y responsables.
- Mantener la trazabilidad de ADR-013 y FEAT-001 al documentar Chatterbox como proveedor único.
- No prometer fallback automático de proveedor ni ocultar que el audio puede no estar disponible.
- Expresar que el juego y lectura deben continuar sin audio como dependencia de backend/frontend, no como responsabilidad del servicio TTS.
- Actualizar Review con enlaces, documentos revisados y bloqueos que sigan abiertos.

## Notes
- Chatterbox único reduce complejidad para una aplicación monofamiliar, pero no elimina la necesidad de conectividad entre redes Docker.
- La configuración de proveedor debe ser externa al código y apta para host o contenedor según el entorno.

## Review

completed_tasks:
  - Documentación completa de `framework/tts/README.md` con descripción del servicio, perfiles de voz, tonos, formato de salida, endpoints, códigos de error, variables de entorno, ejecución host vs Docker, handoff a infraestructura, responsabilidades de backend/frontend y notas de transparencia.
  - Actualización del README raíz con sección TTS Service revisada (Chatterbox único, sin fallback, ADR-013), requisitos del sistema ampliados (conectividad Chatterbox) e instalación TTS documentada.
  - Variables de entorno documentadas con descripción, valor por defecto y ejemplo.
  - Diferencia host vs Docker documentada con responsabilidades claras.
  - Handoff a infraestructura registrado: URL alcanzable, mecanismo de acceso, dependencias operativas.
  - Responsabilidades de backend/frontend documentadas con criterio de protección infantil.
  - Verificado que la documentación no presenta selección de proveedor como opción para familias o menores.

incomplete_tasks:

contract_changes:
  - Ningún cambio de contrato. La documentación referencia el contrato existente `docs/contracts/api/openapi_tts.json`.

learnings:
  - La documentación técnica del servicio TTS debe explicitar que `127.0.0.1:4123` solo funciona en host, no en Docker, para evitar configuraciones fallidas.
  - Es necesario registrar el handoff a infraestructura de forma explícita para que la dependencia de conectividad sea visible.

next_sprint_suggestions:
  - Infraestructura debe confirmar el mecanismo de acceso desde la red TTS al contenedor Chatterbox (DNS, red compartida, proxy).
  - Backend debe implementar y documentar la continuidad de experiencia sin audio (juego/lectura continúan).
  - Frontend debe implementar y documentar la continuidad de experiencia sin audio (sin bloqueo infantil).

### Reviewer verification — APPROVED (2026-07-22)

Revisado por reviewer-tts independiente. Verificación de documentación completada:

**Archivos modificados**:
- `framework/tts/README.md`: Documentación técnica completa del servicio TTS (228 líneas)
- `README.md`: Sección TTS Service actualizada con Chatterbox único y referencias a ADR-013

**Verificación de criterios**:

✅ **Documentación de framework/tts/README.md**:
- Descripción del servicio y contexto (monofamiliar, 3-4 años, 5-6 usuarios)
- Referencias a ADR-013, FEAT-001 y contrato OpenAPI
- Perfiles de voz (npc, storyteller) con variables de entorno
- Tonos soportados (5 tonos) con parámetros de prosodia
- Formato de salida (audio/mpeg, TTSError)
- Endpoints (health, status, synthesize)
- Códigos de error (10 códigos documentados en 4 categorías)
- Variables de entorno (7 variables con descripción, default y ejemplo)
- Ejecución host vs Docker con responsabilidades claras
- Handoff a infraestructura detallado
- Responsabilidades backend/frontend con criterio de protección infantil

✅ **README raíz actualizado**:
- Sección TTS Service revisada con Chatterbox como único proveedor
- Referencia explícita a ADR-013
- Sin fallback automático documentado
- Requisitos del sistema ampliados (conectividad Chatterbox)
- Instalación TTS documentada con CHATTERBOX_BASE_URL

✅ **Variables de entorno documentadas**:
- 7 variables con descripción, valor por defecto y ejemplo
- Nota sobre CHATTERBOX_BASE_URL y alcance (host vs Docker)

✅ **Diferencia host vs Docker**:
- Sección dedicada con responsabilidades claras
- TTS: consume URL configurada, no define redes
- Infraestructura: proporciona conectividad
- Backend/frontend: gestionan continuidad sin audio

✅ **Handoff a infraestructura**:
- Lo que TTS no hace (redes, firewall, Docker Compose)
- Lo que infraestructura debe proporcionar (URL alcanzable, mecanismo de acceso)
- Dependencias operativas (Chatterbox, FFmpeg, conectividad)
- Contacto responsable identificado

✅ **Responsabilidades backend/frontend**:
- Backend: validar texto/locales, gestionar errores, continuidad sin audio
- Frontend: mostrar estado audio, continuidad sin audio, no bloquear experiencia infantil
- Criterio de protección infantil explícito: ausencia de audio no debe bloquear juego/lectura

✅ **Transparencia y configuración**:
- Proveedor único: "No existe opción para familias o menores de seleccionar proveedor"
- Sin fallback automático: documentado explícitamente
- Configuración externa: "No es una opción configurable por usuarios finales"

**Sin cambios de código de producción**: Solo documentación modificada.

**Coherencia con ADR-013 y FEAT-001**: Documentación alineada con decisiones arquitectónicas y especificación funcional.

**Protección infantil**: Criterio explícito en sección de responsabilidades y notas de transparencia.

sprint_verdict: APPROVED (2026-07-22)
