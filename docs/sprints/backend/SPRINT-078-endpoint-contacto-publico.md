# SPRINT-078 — Endpoint público de contacto para mensajes

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-08-03
- **Responsable principal:** backend
- **Prioridad:** ALTA
- **Dependencias:** FEAT-007 (aceptada), contratos OpenAPI de contacto
- **Impacto estimado:** Nuevo endpoint público `POST /api/v1/contact` para recepción de comentarios, sugerencias y errores. Sin autenticación. Con rate limiting, sanitización y minimización de datos. Schema DDL para almacenamiento de mensajes.

## Objetivo

Implementar el endpoint `POST /api/v1/contact` que:
- Reciba mensajes de contacto sin requerir autenticación.
- Valide el request body conforme al contrato OpenAPI.
- Aplique rate limiting para prevenir abuso.
- Sanitice el contenido del mensaje.
- Almacene el mensaje conforme a minimización de datos.
- Responda con 202 Accepted y timestamp de recepción.
- No exponga datos de otros usuarios ni contenido protegido.

## Contexto

**FEAT-007** requiere un canal de contacto público mediante textarea que:
- No solicite datos personales ni de menores.
- No permita adjuntos.
- Muestre aviso de privacidad y requiera confirmación adulta.
- Los mensajes no son públicos, no se comparten entre familias, no se usan para publicidad, perfilado ni entrenamiento de IA.

**Contratos OpenAPI:** Ya definidos en:
- `docs/contracts/api/openapi/paths/contact/post-contact.yaml`
- `docs/contracts/api/openapi/schemas/contact/contact-request.yaml`
- `docs/contracts/api/openapi/schemas/contact/contact-response.yaml`

**Estado actual:** No existe ningún módulo ni endpoint de contacto en el backend.

## Diseño funcional-técnico

### 1. Módulo `contact` — Estructura hexagonal

**Estructura según patrón hexagonal del proyecto:**
```
framework/backend/src/main/java/es/vargontoc/educational/framework/contact/
├── model/                              # Dominio - POJO puro sin JPA
│   └── ContactMessage.java
├── ports/                              # Interfaces (contratos)
│   ├── in/                             # Puertos de entrada (casos de uso)
│   │   └── ContactUseCase.java
│   └── out/                            # Puertos de salida (repositorios)
│       └── ContactMessageRepository.java
├── service/                            # Implementación de casos de uso
│   └── ContactService.java
└── infrastructure/                     # Adaptadores de infraestructura
    ├── web/                            # Controladores REST
    │   └── ContactController.java
    ├── dto/                            # DTOs para la capa web
    │   ├── ContactRequest.java
    │   └── ContactResponse.java
    └── persistence/                    # Implementación de repositorios
        ├── ContactMessageJpaEntity.java
        ├── ContactMessageJpaRepository.java
        └── ContactMessagePersistenceAdapter.java
```

### 2. Modelo de dominio `ContactMessage.java`

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/model/ContactMessage.java` (nuevo)

**Responsabilidad:** Entidad de dominio pura, sin anotaciones JPA. Representa un mensaje de contacto.

**Especificación:**
```java
package es.vargontoc.educational.framework.contact.model;

import java.time.OffsetDateTime;

public class ContactMessage {

    private Long id;
    private String message;
    private String clientIp;
    private OffsetDateTime createdAt;

    public ContactMessage() {}

    public ContactMessage(String message, String clientIp) {
        this.message = message;
        this.clientIp = clientIp;
    }

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
```

**Minimización de datos:**
- Solo se almacena: `id`, `message`, `clientIp`, `createdAt`.
- No se almacena: nombre, email, teléfono, datos de menores, PIN, etc.
- `clientIp` se usa para rate limiting y auditoría de seguridad.

### 3. Puerto de entrada `ContactUseCase.java`

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/ports/in/ContactUseCase.java` (nuevo)

**Responsabilidad:** Interface del caso de uso. Define las operaciones de negocio para mensajes de contacto.

**Especificación:**
```java
package es.vargontoc.educational.framework.contact.ports.in;

import es.vargontoc.educational.framework.contact.model.ContactMessage;

public interface ContactUseCase {

    ContactMessage submitMessage(String rawMessage, String clientIp);
}
```

### 4. Puerto de salida `ContactMessageRepository.java`

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/ports/out/ContactMessageRepository.java` (nuevo)

**Responsabilidad:** Interface del repositorio. Abstrae la persistencia de mensajes de contacto.

**Especificación:**
```java
package es.vargontoc.educational.framework.contact.ports.out;

import es.vargontoc.educational.framework.contact.model.ContactMessage;

import java.time.OffsetDateTime;

public interface ContactMessageRepository {

    ContactMessage save(ContactMessage message);

    long countByClientIpAndCreatedAtAfter(String clientIp, OffsetDateTime since);
}
```

### 5. Servicio `ContactService.java`

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/service/ContactService.java` (nuevo)

**Responsabilidad:** Implementación del caso de uso. Contiene la lógica de negocio: sanitización, validación y almacenamiento.

**Especificación:**
```java
package es.vargontoc.educational.framework.contact.service;

import es.vargontoc.educational.framework.contact.model.ContactMessage;
import es.vargontoc.educational.framework.contact.ports.in.ContactUseCase;
import es.vargontoc.educational.framework.contact.ports.out.ContactMessageRepository;
import es.vargontoc.educational.framework.shared.exception.InvalidRequestException;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@Transactional
public class ContactService implements ContactUseCase {

    private static final int MAX_MESSAGE_LENGTH = 2000;
    private final ContactMessageRepository contactMessageRepository;

    public ContactService(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    @Override
    public ContactMessage submitMessage(String rawMessage, String clientIp) {
        // 1. Sanitizar el mensaje (eliminar HTML, scripts, etc.)
        String sanitized = sanitize(rawMessage);

        // 2. Validar longitud después de sanitización
        if (sanitized.isBlank() || sanitized.length() > MAX_MESSAGE_LENGTH) {
            throw new InvalidRequestException("Mensaje inválido o vacío tras sanitización");
        }

        // 3. Crear modelo de dominio
        ContactMessage message = new ContactMessage(sanitized, clientIp);
        message.setCreatedAt(OffsetDateTime.now());

        // 4. Guardar
        return contactMessageRepository.save(message);
    }

    private String sanitize(String input) {
        return Jsoup.clean(input, Safelist.none());
    }
}
```

### 6. DTOs `ContactRequest.java` y `ContactResponse.java`

**Archivos:**
- `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/infrastructure/dto/ContactRequest.java` (nuevo)
- `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/infrastructure/dto/ContactResponse.java` (nuevo)

**Especificación:**
```java
// ContactRequest.java
package es.vargontoc.educational.framework.contact.infrastructure.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactRequest(
    @NotBlank
    @Size(min = 1, max = 2000)
    String message
) {}
```

```java
// ContactResponse.java
package es.vargontoc.educational.framework.contact.infrastructure.dto;

import java.time.OffsetDateTime;

public record ContactResponse(
    String status,
    OffsetDateTime timestamp
) {}
```

**Validaciones:**
- `message` no puede ser blank.
- `message` longitud entre 1 y 2000 caracteres.
- `additionalProperties: false` → Jackson configurado para rechazar campos desconocidos.

### 7. Controlador `ContactController.java`

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/infrastructure/web/ContactController.java` (nuevo)

**Responsabilidad:** Adaptador REST. Recibe requests HTTP, delega al caso de uso y devuelve responses HTTP.

**Especificación:**
```java
package es.vargontoc.educational.framework.contact.infrastructure.web;

import es.vargontoc.educational.framework.contact.infrastructure.dto.ContactRequest;
import es.vargontoc.educational.framework.contact.infrastructure.dto.ContactResponse;
import es.vargontoc.educational.framework.contact.model.ContactMessage;
import es.vargontoc.educational.framework.contact.ports.in.ContactUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contact")
@Tag(name = "Contact", description = "Endpoint público para comentarios y sugerencias")
public class ContactController {

    private final ContactUseCase contactUseCase;

    public ContactController(ContactUseCase contactUseCase) {
        this.contactUseCase = contactUseCase;
    }

    @PostMapping
    @Operation(summary = "Enviar mensaje de contacto")
    public ResponseEntity<ContactResponse> sendContactMessage(
            @Valid @RequestBody ContactRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        ContactMessage saved = contactUseCase.submitMessage(request.message(), clientIp);
        ContactResponse response = new ContactResponse("received", saved.getCreatedAt());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
```

**Sin autenticación:** No requiere `@PreAuthorize` ni token.

### 8. Entidad JPA `ContactMessageJpaEntity.java`

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/infrastructure/persistence/ContactMessageJpaEntity.java` (nuevo)

**Responsabilidad:** Entidad JPA para persistencia. Separada del modelo de dominio.

**Especificación:**
```java
package es.vargontoc.educational.framework.contact.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "contact_messages")
public class ContactMessageJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_seq")
    @SequenceGenerator(name = "contact_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(name = "client_ip", length = 45)
    private String clientIp;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
```

### 9. Repositorio JPA `ContactMessageJpaRepository.java`

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/infrastructure/persistence/ContactMessageJpaRepository.java` (nuevo)

**Responsabilidad:** Interfaz Spring Data JPA para operaciones de base de datos.

**Especificación:**
```java
package es.vargontoc.educational.framework.contact.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;

public interface ContactMessageJpaRepository extends JpaRepository<ContactMessageJpaEntity, Long> {
    long countByClientIpAndCreatedAtAfter(String clientIp, OffsetDateTime since);
}
```

### 10. Adaptador de persistencia `ContactMessagePersistenceAdapter.java`

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/infrastructure/persistence/ContactMessagePersistenceAdapter.java` (nuevo)

**Responsabilidad:** Implementa el puerto `ContactMessageRepository`. Convierte entre modelo de dominio y entidad JPA.

**Especificación:**
```java
package es.vargontoc.educational.framework.contact.infrastructure.persistence;

import es.vargontoc.educational.framework.contact.model.ContactMessage;
import es.vargontoc.educational.framework.contact.ports.out.ContactMessageRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public class ContactMessagePersistenceAdapter implements ContactMessageRepository {

    private final ContactMessageJpaRepository jpaRepository;

    public ContactMessagePersistenceAdapter(ContactMessageJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ContactMessage save(ContactMessage message) {
        ContactMessageJpaEntity saved = jpaRepository.save(toJpa(message));
        return toDomain(saved);
    }

    @Override
    public long countByClientIpAndCreatedAtAfter(String clientIp, OffsetDateTime since) {
        return jpaRepository.countByClientIpAndCreatedAtAfter(clientIp, since);
    }

    private static ContactMessage toDomain(ContactMessageJpaEntity source) {
        ContactMessage target = new ContactMessage();
        target.setId(source.getId());
        target.setMessage(source.getMessage());
        target.setClientIp(source.getClientIp());
        target.setCreatedAt(source.getCreatedAt());
        return target;
    }

    private static ContactMessageJpaEntity toJpa(ContactMessage source) {
        ContactMessageJpaEntity target = new ContactMessageJpaEntity();
        target.setId(source.getId());
        target.setMessage(source.getMessage());
        target.setClientIp(source.getClientIp());
        target.setCreatedAt(source.getCreatedAt());
        return target;
    }
}
```

### 11. Schema DDL

**Archivo:** `framework/backend/src/main/resources/db/migration/V{next}__create_contact_messages.sql` (nuevo)

```sql
CREATE TABLE contact_messages (
    id BIGINT PRIMARY KEY,
    message VARCHAR(2000) NOT NULL,
    client_ip VARCHAR(45),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE SEQUENCE contact_seq START WITH 1 INCREMENT BY 1;

CREATE INDEX idx_contact_messages_client_ip_created_at
    ON contact_messages (client_ip, created_at);
```

## Contratos y dependencias externas

### Contratos

- **POST /api/v1/contact**: definido en `docs/contracts/api/openapi/paths/contact/post-contact.yaml`.
- **contact-request.yaml**: schema del request body.
- **contact-response.yaml**: schema del response body.

### Dependencias externas

| Capa | Dependencia | Estado |
|------|-------------|--------|
| Frontend | SPRINT-033 consume este endpoint. | ⏳ Pendiente |
| Agents | Ninguna. | ✅ Sin dependencia |
| TTS | Ninguna. | ✅ Sin dependencia |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Endpoint público sin rate limiting → abuso. | ALTA | Rate limiting estricto: 5 msg/min/IP. Respuesta 429 clara. |
| R2 | XSS almacenado si no se sanitiza. | ALTA | Sanitización con Jsoup `Safelist.none()` antes de almacenar. |
| R3 | Usuario envía datos personales por error. | MEDIA | El aviso de privacidad en frontend es la primera línea de defensa. Backend no puede prevenirlo completamente, pero sanitiza y no expone datos. |
| R4 | Exposición de mensajes entre familias. | ALTA | Los mensajes no se leen por API. Solo acceso administrativo interno. No hay endpoint GET. |
| R5 | Almacenamiento indefinido de mensajes. | MEDIA | Definir política de retención y eliminación periódica (fuera de este sprint). |

---

## Tareas del sprint

### Tarea 78.1: Crear estructura hexagonal del módulo `contact`

**Descripción:** Crear la estructura de paquetes del módulo `contact` siguiendo el patrón hexagonal del proyecto.

**Archivos (directorios):**
- `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/model/` (nuevo)
- `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/ports/in/` (nuevo)
- `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/ports/out/` (nuevo)
- `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/service/` (nuevo)
- `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/infrastructure/web/` (nuevo)
- `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/infrastructure/dto/` (nuevo)
- `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/infrastructure/persistence/` (nuevo)

**Criterios de aceptación:**
- Estructura de paquetes creada siguiendo patrón hexagonal.
- Compilación sin errores.

---

### Tarea 78.2: Implementar modelo de dominio `ContactMessage`

**Descripción:** Entidad de dominio pura, sin anotaciones JPA.

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/model/ContactMessage.java` (nuevo)

**Especificación completa:** Ver sección 2 del diseño funcional-técnico.

**Criterios de aceptación:**
- POJO puro sin anotaciones JPA.
- Campos: `id`, `message`, `clientIp`, `createdAt`.
- Getters y setters.
- Compilación sin errores.

---

### Tarea 78.3: Implementar puertos `ContactUseCase` y `ContactMessageRepository`

**Descripción:** Interfaces de puertos de entrada (caso de uso) y salida (repositorio).

**Archivos:**
- `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/ports/in/ContactUseCase.java` (nuevo)
- `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/ports/out/ContactMessageRepository.java` (nuevo)

**Especificación completa:** Ver secciones 3 y 4 del diseño funcional-técnico.

**Criterios de aceptación:**
- `ContactUseCase` define `submitMessage(String rawMessage, String clientIp)`.
- `ContactMessageRepository` define `save()` y `countByClientIpAndCreatedAtAfter()`.
- Compilación sin errores.

---

### Tarea 78.4: Implementar DTOs `ContactRequest` y `ContactResponse`

**Descripción:** Records para request y response del endpoint.

**Archivos:**
- `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/infrastructure/dto/ContactRequest.java` (nuevo)
- `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/infrastructure/dto/ContactResponse.java` (nuevo)

**Especificación completa:** Ver sección 6 del diseño funcional-técnico.

**Criterios de aceptación:**
- `ContactRequest` con `@NotBlank @Size(min=1, max=2000) String message`.
- `ContactResponse` con `String status` y `OffsetDateTime timestamp`.
- Jackson configurado para rechazar campos desconocidos (`FAIL_ON_UNKNOWN_PROPERTIES`).
- Compilación sin errores.

---

### Tarea 78.5: Implementar servicio `ContactService`

**Descripción:** Implementación del caso de uso con sanitización, validación y almacenamiento.

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/service/ContactService.java` (nuevo)

**Especificación completa:** Ver sección 5 del diseño funcional-técnico.

**Criterios de aceptación:**
- Implementa `ContactUseCase`.
- Inyecta `ContactMessageRepository` (puerto de salida).
- Sanitización con Jsoup `Safelist.none()`.
- Validación de longitud después de sanitización.
- Lanza `InvalidRequestException` si el mensaje es inválido.
- `@Service` y `@Transactional`.
- Compilación sin errores.

---

### Tarea 78.6: Implementar entidad JPA y migration DDL

**Descripción:** Entidad JPA para persistencia y migration SQL para la tabla `contact_messages`.

**Archivos:**
- `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/infrastructure/persistence/ContactMessageJpaEntity.java` (nuevo)
- `framework/backend/src/main/resources/db/migration/V{next}__create_contact_messages.sql` (nuevo)

**Especificación completa:** Ver secciones 8 y 12 del diseño funcional-técnico.

**Criterios de aceptación:**
- `ContactMessageJpaEntity` con anotaciones JPA (`@Entity`, `@Table`, `@Column`).
- Usa `@SequenceGenerator` para generación de IDs (consistente con `BaseEntity`).
- Migration crea tabla `contact_messages` con columnas correctas.
- Secuencia `contact_seq` creada.
- Índice compuesto en `(client_ip, created_at)` para rate limiting.
- Flyway ejecuta la migration sin errores.

---

### Tarea 78.7: Implementar repositorio JPA y adaptador de persistencia

**Descripción:** Interfaz Spring Data JPA y adaptador que implementa el puerto `ContactMessageRepository`.

**Archivos:**
- `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/infrastructure/persistence/ContactMessageJpaRepository.java` (nuevo)
- `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/infrastructure/persistence/ContactMessagePersistenceAdapter.java` (nuevo)

**Especificación completa:** Ver secciones 9 y 10 del diseño funcional-técnico.

**Criterios de aceptación:**
- `ContactMessageJpaRepository` extiende `JpaRepository<ContactMessageJpaEntity, Long>`.
- `ContactMessagePersistenceAdapter` implementa `ContactMessageRepository`.
- Métodos de conversión `toDomain()` y `toJpa()` entre modelo de dominio y entidad JPA.
- `@Repository` en el adaptador.
- Compilación sin errores.

---

### Tarea 78.8: Implementar controlador `ContactController`

**Descripción:** Adaptador REST con endpoint `POST /api/v1/contact`.

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/infrastructure/web/ContactController.java` (nuevo)

**Especificación completa:** Ver sección 7 del diseño funcional-técnico.

**Criterios de aceptación:**
- `@RestController` con `@RequestMapping("/api/v1/contact")`.
- Inyecta `ContactUseCase` (puerto de entrada).
- `@PostMapping` sin autenticación requerida.
- `@Valid @RequestBody ContactRequest`.
- Extrae `clientIp` del `HttpServletRequest`.
- Respuesta 202 Accepted con `ContactResponse`.
- Respuesta 400 si validación falla.
- Swagger/OpenAPI documentado con `@Tag` y `@Operation`.
- Compilación sin errores.

---

### Tarea 78.9: Implementar rate limiting

**Descripción:** Mecanismo de rate limiting para el endpoint de contacto.

**Especificación completa:** Ver sección 11 del diseño funcional-técnico.

**Criterios de aceptación:**
- 5 mensajes por minuto por IP.
- Respuesta 429 Too Many Requests cuando se excede.
- El rate limiting se aplica antes de la lógica de negocio.
- Consulta `ContactMessageRepository.countByClientIpAndCreatedAtAfter()`.
- Compilación sin errores.

---

### Tarea 78.10: Tests de integración

**Descripción:** Tests de integración del endpoint completo.

**Archivo:** `framework/backend/src/test/java/es/vargontoc/educational/framework/contact/ContactControllerIntegrationTest.java` (nuevo)

**Casos de prueba:**
1. Envío válido → 202 Accepted con `status: received`.
2. Mensaje vacío → 400 Bad Request.
3. Mensaje > 2000 caracteres → 400 Bad Request.
4. Campos adicionales → 400 Bad Request.
5. Rate limiting excedido → 429 Too Many Requests.
6. Mensaje con HTML → almacenado sanitizado.
7. Verificar que no hay endpoint GET para leer mensajes.

**Criterios de aceptación:**
- Todos los casos de prueba pasan.
- Cobertura > 80% del módulo `contact`.
- Compilación sin errores.

---

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|---------------|
| `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/model/ContactMessage.java` | Nuevo archivo |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/ports/in/ContactUseCase.java` | Nuevo archivo |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/ports/out/ContactMessageRepository.java` | Nuevo archivo |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/service/ContactService.java` | Nuevo archivo |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/infrastructure/web/ContactController.java` | Nuevo archivo |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/infrastructure/dto/ContactRequest.java` | Nuevo archivo |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/infrastructure/dto/ContactResponse.java` | Nuevo archivo |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/infrastructure/persistence/ContactMessageJpaEntity.java` | Nuevo archivo |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/infrastructure/persistence/ContactMessageJpaRepository.java` | Nuevo archivo |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/contact/infrastructure/persistence/ContactMessagePersistenceAdapter.java` | Nuevo archivo |
| `framework/backend/src/main/resources/db/migration/V{next}__create_contact_messages.sql` | Nuevo archivo |
| `framework/backend/src/test/java/es/vargontoc/educational/framework/contact/ContactControllerIntegrationTest.java` | Nuevo archivo |
| `build.gradle` o `pom.xml` | Añadir dependencia Jsoup si no existe |

## Estimación

- **Duración:** 2 días
- **Complejidad:** Media
- **Riesgo:** Medio (endpoint público, rate limiting, sanitización)

## Criterios de aceptación del sprint

1. `POST /api/v1/contact` acepta mensajes válidos sin autenticación y responde 202 Accepted. *(Contrato)*
2. El request body valida `message` con longitud 1-2000 caracteres. *(Contrato)*
3. Campos adicionales en el request son rechazados con 400. *(Seguridad)*
4. Rate limiting: 5 mensajes por minuto por IP, respuesta 429. *(Seguridad)*
5. El mensaje se sanitiza antes de almacenarse (sin HTML/scripts). *(Seguridad)*
6. Solo se almacena: `id`, `message`, `createdAt`, `clientIp`. *(Minimización)*
7. No existe endpoint GET para leer mensajes. *(Privacidad)*
8. La respuesta incluye `status: received` y `timestamp`. *(Contrato)*
9. Tests de integración con cobertura > 80%. *(Calidad)*
10. Compilación sin errores. *(Calidad)*

## Evidencias esperadas

- Test integración: POST válido → 202 con `status: received`.
- Test integración: mensaje vacío → 400.
- Test integración: mensaje > 2000 chars → 400.
- Test integración: campos adicionales → 400.
- Test integración: 6 requests en 1 minuto → 429 en el 6º.
- Test integración: mensaje con `<script>alert('xss')</script>` → almacenado como texto plano.
- Test integración: verificar que no hay endpoint GET `/api/v1/contact`.
- Swagger UI muestra el endpoint documentado.
- Flyway ejecuta la migration sin errores.

## Dependencias bloqueantes

- [x] FEAT-007 aceptada.
- [x] Contratos OpenAPI definidos.

## Handoffs a otras capas

### Frontend:
- SPRINT-033 consume este endpoint. Debe estar disponible antes del Sprint 33.

### Agents/TTS:
- Sin dependencia.

## Notas adicionales

### Privacidad infantil

- El endpoint no solicita ni almacena datos personales ni de menores.
- Los mensajes no se comparten entre familias.
- No se usan para publicidad, perfilado ni entrenamiento de IA.
- `clientIp` se almacena solo para rate limiting y auditoría de seguridad.
- Se debe definir una política de retención y eliminación periódica (fuera de este sprint).

### Seguridad

- Rate limiting previene abuso del endpoint público.
- Sanitización previene XSS almacenado.
- Rechazo de campos adicionales previene inyección de datos no esperados.
- No hay endpoint GET para leer mensajes (solo almacenamiento).

### Relación con FEAT-007

Este sprint satisface los requisitos de backend del FEAT-007:
- Req. 7: Contacto ofrece textarea para envío de comentarios.
- Req. 8: No solicita datos personales como condición de uso.
- Req. 9: Aviso de privacidad y confirmación adulta (implementado en frontend).
- Req. 10: No admite adjuntos.
- Req. 11: Estados comprensibles (implementado en frontend).
