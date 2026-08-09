# SPRINT-079 — Persistencia de historial de conversaciones del chatbot

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-08-09
- **Responsable principal:** backend
- **Prioridad:** ALTA
- **Dependencias:** ADR-003 (aceptada), decisión de producto sobre historial
- **Impacto estimado:** Schema DDL para conversaciones y mensajes del chatbot. API REST para recuperar historial por familia. Preparación para streaming en SPRINT-080.

## Objetivo

Implementar la persistencia del historial de conversaciones del chatbot parental que:
- Almacene conversaciones completas con sus mensajes asociados.
- Permita recuperar el historial de conversaciones de una familia autenticada.
- Aísle las conversaciones entre familias (ninguna familia ve datos de otra).
- Minimice datos almacenados (solo contenido necesario para funcionalidad).
- Prepare la base de datos para el streaming de SPRINT-080.

## Contexto

**ADR-003** define el chatbot parental conversacional exclusivo para adultos autenticados. La decisión de producto confirma que se requiere persistir el historial de conversaciones para:
- Permitir continuidad tras reconexiones del cliente.
- Mantener contexto conversacional (futuro).
- Auditoría y trazabilidad (limitada).

**Estado actual:** El chatbot existe en `AgentsService.sendMessage()` pero devuelve respuesta en bloque sin persistencia. No hay schema DDL para conversaciones.

**Decisión de producto (2026-08-09):**
- Historial de conversaciones: SÍ, como sprint previo al streaming.
- Thinking/reasoning: DESCARTADO para esta versión.
- Endpoint REST: DEPRECAR inmediatamente tras migración a STOMP.
- Timeout streaming: 60s por intento.
- Topic STOMP: Separado `/topic/family/{familyId}/chatbot`.

## Diseño funcional-técnico

### 1. Modelo de dominio — Conversación y Mensaje

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/model/ChatbotConversation.java` (nuevo)

**Responsabilidad:** Entidad de dominio pura, sin anotaciones JPA. Representa una conversación completa del chatbot.

**Especificación:**
```java
package es.vargontoc.educational.framework.agents.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatbotConversation {

    private Long id;
    private UUID conversationId;
    private Long familyId;
    private OffsetDateTime startedAt;
    private OffsetDateTime lastMessageAt;
    private List<ChatbotMessage> messages;

    public ChatbotConversation() {
        this.messages = new ArrayList<>();
    }

    public ChatbotConversation(Long familyId) {
        this();
        this.conversationId = UUID.randomUUID();
        this.familyId = familyId;
        this.startedAt = OffsetDateTime.now();
        this.lastMessageAt = OffsetDateTime.now();
    }

    // Getters y setters
    public void addMessage(ChatbotMessage message) {
        this.messages.add(message);
        this.lastMessageAt = OffsetDateTime.now();
    }
}
```

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/model/ChatbotMessage.java` (nuevo)

**Responsabilidad:** Entidad de dominio pura. Representa un mensaje individual dentro de una conversación.

**Especificación:**
```java
package es.vargontoc.educational.framework.agents.model;

import java.time.OffsetDateTime;

public class ChatbotMessage {

    private Long id;
    private Long conversationId;
    private String role; // "USER" o "ASSISTANT"
    private String content;
    private OffsetDateTime createdAt;

    public ChatbotMessage() {}

    public ChatbotMessage(Long conversationId, String role, String content) {
        this.conversationId = conversationId;
        this.role = role;
        this.content = content;
        this.createdAt = OffsetDateTime.now();
    }

    // Getters y setters
}
```

**Minimización de datos:**
- Solo se almacena: `id`, `conversationId`, `familyId`, `role`, `content`, timestamps.
- No se almacena: datos personales de menores, contexto de sesión, metadata adicional.
- `content` es el texto sanitizado del mensaje.

### 2. Puerto de entrada — ChatbotHistoryUseCase

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/ports/in/ChatbotHistoryUseCase.java` (nuevo)

**Responsabilidad:** Interface del caso de uso para gestión de historial.

**Especificación:**
```java
package es.vargontoc.educational.framework.agents.ports.in;

import es.vargontoc.educational.framework.agents.model.ChatbotConversation;
import es.vargontoc.educational.framework.agents.model.ChatbotMessage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatbotHistoryUseCase {

    ChatbotConversation createConversation(Long familyId);

    Optional<ChatbotConversation> getConversation(UUID conversationId, Long familyId);

    List<ChatbotConversation> listConversations(Long familyId, int limit);

    ChatbotMessage addMessage(Long conversationId, String role, String content);
}
```

### 3. Puerto de salida — ChatbotConversationRepository

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/ports/out/ChatbotConversationRepository.java` (nuevo)

**Responsabilidad:** Interface del repositorio. Abstrae la persistencia de conversaciones.

**Especificación:**
```java
package es.vargontoc.educational.framework.agents.ports.out;

import es.vargontoc.educational.framework.agents.model.ChatbotConversation;
import es.vargontoc.educational.framework.agents.model.ChatbotMessage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatbotConversationRepository {

    ChatbotConversation save(ChatbotConversation conversation);

    Optional<ChatbotConversation> findByConversationIdAndFamilyId(UUID conversationId, Long familyId);

    List<ChatbotConversation> findByFamilyIdOrderByLastMessageAtDesc(Long familyId, int limit);

    ChatbotMessage saveMessage(ChatbotMessage message);

    List<ChatbotMessage> findMessagesByConversationId(Long conversationId);
}
```

### 4. Servicio — ChatbotHistoryService

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/service/ChatbotHistoryService.java` (nuevo)

**Responsabilidad:** Implementación del caso de uso.

**Especificación:**
```java
package es.vargontoc.educational.framework.agents.service;

import es.vargontoc.educational.framework.agents.model.ChatbotConversation;
import es.vargontoc.educational.framework.agents.model.ChatbotMessage;
import es.vargontoc.educational.framework.agents.ports.in.ChatbotHistoryUseCase;
import es.vargontoc.educational.framework.agents.ports.out.ChatbotConversationRepository;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ChatbotHistoryService implements ChatbotHistoryUseCase {

    private final ChatbotConversationRepository repository;

    public ChatbotHistoryService(ChatbotConversationRepository repository) {
        this.repository = repository;
    }

    @Override
    public ChatbotConversation createConversation(Long familyId) {
        ChatbotConversation conversation = new ChatbotConversation(familyId);
        return repository.save(conversation);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ChatbotConversation> getConversation(UUID conversationId, Long familyId) {
        Optional<ChatbotConversation> conversation = repository.findByConversationIdAndFamilyId(conversationId, familyId);
        conversation.ifPresent(c -> {
            List<ChatbotMessage> messages = repository.findMessagesByConversationId(c.getId());
            c.getMessages().addAll(messages);
        });
        return conversation;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatbotConversation> listConversations(Long familyId, int limit) {
        return repository.findByFamilyIdOrderByLastMessageAtDesc(familyId, limit);
    }

    @Override
    public ChatbotMessage addMessage(Long conversationId, String role, String content) {
        ChatbotMessage message = new ChatbotMessage(conversationId, role, content);
        return repository.saveMessage(message);
    }
}
```

### 5. DTOs — ConversationResponse y MessageResponse

**Archivos:**
- `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/dto/ConversationResponse.java` (nuevo)
- `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/dto/MessageResponse.java` (nuevo)

**Especificación:**
```java
// ConversationResponse.java
package es.vargontoc.educational.framework.agents.infrastructure.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ConversationResponse(
    UUID conversationId,
    OffsetDateTime startedAt,
    OffsetDateTime lastMessageAt,
    List<MessageResponse> messages
) {}
```

```java
// MessageResponse.java
package es.vargontoc.educational.framework.agents.infrastructure.dto;

import java.time.OffsetDateTime;

public record MessageResponse(
    String role,
    String content,
    OffsetDateTime createdAt
) {}
```

### 6. Controlador — ChatbotHistoryController

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/web/ChatbotHistoryController.java` (nuevo)

**Responsabilidad:** Adaptador REST para historial de conversaciones.

**Especificación:**
```java
package es.vargontoc.educational.framework.agents.infrastructure.web;

import es.vargontoc.educational.framework.agents.infrastructure.dto.ConversationResponse;
import es.vargontoc.educational.framework.agents.infrastructure.dto.MessageResponse;
import es.vargontoc.educational.framework.agents.model.ChatbotConversation;
import es.vargontoc.educational.framework.agents.ports.in.ChatbotHistoryUseCase;
import es.vargontoc.educational.framework.shared.api.ApiResponse;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agents/conversations")
@Tag(name = "Chatbot History", description = "Historial de conversaciones del chatbot parental")
public class ChatbotHistoryController {

    private final ChatbotHistoryUseCase historyUseCase;

    public ChatbotHistoryController(ChatbotHistoryUseCase historyUseCase) {
        this.historyUseCase = historyUseCase;
    }

    @GetMapping
    @Operation(summary = "Listar conversaciones de la familia autenticada")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> listConversations(
            @RequestParam(defaultValue = "10") int limit,
            @RequestAttribute("familyId") Long familyId) {
        
        List<ChatbotConversation> conversations = historyUseCase.listConversations(familyId, limit);
        List<ConversationResponse> responses = conversations.stream()
            .map(this::toResponse)
            .toList();
        
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{conversationId}")
    @Operation(summary = "Obtener conversación específica con todos sus mensajes")
    public ResponseEntity<ApiResponse<ConversationResponse>> getConversation(
            @PathVariable UUID conversationId,
            @RequestAttribute("familyId") Long familyId) {
        
        ChatbotConversation conversation = historyUseCase.getConversation(conversationId, familyId)
            .orElseThrow(() -> new ResourceNotFoundException("Conversación no encontrada"));
        
        return ResponseEntity.ok(ApiResponse.ok(toResponse(conversation)));
    }

    private ConversationResponse toResponse(ChatbotConversation conversation) {
        List<MessageResponse> messages = conversation.getMessages().stream()
            .map(m -> new MessageResponse(m.getRole(), m.getContent(), m.getCreatedAt()))
            .toList();
        
        return new ConversationResponse(
            conversation.getConversationId(),
            conversation.getStartedAt(),
            conversation.getLastMessageAt(),
            messages
        );
    }
}
```

**Seguridad:** Requiere autenticación de familia. `familyId` se extrae del token JWT (ya implementado en `SecurityConfig`).

### 7. Entidad JPA — ChatbotConversationJpaEntity y ChatbotMessageJpaEntity

**Archivos:**
- `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/persistence/ChatbotConversationJpaEntity.java` (nuevo)
- `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/persistence/ChatbotMessageJpaEntity.java` (nuevo)

**Especificación:**
```java
// ChatbotConversationJpaEntity.java
package es.vargontoc.educational.framework.agents.infrastructure.persistence;

import es.vargontoc.educational.framework.shared.persistence.BaseEntity;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "chatbot_conversation")
public class ChatbotConversationJpaEntity extends BaseEntity {

    @Column(name = "conversation_id", nullable = false, unique = true)
    private UUID conversationId;

    @Column(name = "family_id", nullable = false)
    private Long familyId;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "last_message_at", nullable = false)
    private OffsetDateTime lastMessageAt;

    // Getters y setters
}
```

```java
// ChatbotMessageJpaEntity.java
package es.vargontoc.educational.framework.agents.infrastructure.persistence;

import es.vargontoc.educational.framework.shared.persistence.BaseEntity;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "chatbot_message")
public class ChatbotMessageJpaEntity extends BaseEntity {

    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @Column(name = "content", nullable = false, length = 4000)
    private String content;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    // Getters y setters
}
```

### 8. Repositorio JPA

**Archivos:**
- `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/persistence/ChatbotConversationJpaRepository.java` (nuevo)
- `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/persistence/ChatbotMessageJpaRepository.java` (nuevo)

**Especificación:**
```java
// ChatbotConversationJpaRepository.java
package es.vargontoc.educational.framework.agents.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatbotConversationJpaRepository extends JpaRepository<ChatbotConversationJpaEntity, Long> {

    Optional<ChatbotConversationJpaEntity> findByConversationIdAndFamilyId(UUID conversationId, Long familyId);

    @Query("SELECT c FROM ChatbotConversationJpaEntity c WHERE c.familyId = :familyId ORDER BY c.lastMessageAt DESC")
    List<ChatbotConversationJpaEntity> findByFamilyIdOrderByLastMessageAtDesc(@Param("familyId") Long familyId);
}
```

```java
// ChatbotMessageJpaRepository.java
package es.vargontoc.educational.framework.agents.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatbotMessageJpaRepository extends JpaRepository<ChatbotMessageJpaEntity, Long> {

    List<ChatbotMessageJpaEntity> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
}
```

### 9. Adaptador de persistencia — ChatbotConversationPersistenceAdapter

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/persistence/ChatbotConversationPersistenceAdapter.java` (nuevo)

**Responsabilidad:** Implementa el puerto `ChatbotConversationRepository`.

**Especificación:**
```java
package es.vargontoc.educational.framework.agents.infrastructure.persistence;

import es.vargontoc.educational.framework.agents.infrastructure.mapper.ChatbotConversationMapper;
import es.vargontoc.educational.framework.agents.infrastructure.mapper.ChatbotMessageMapper;
import es.vargontoc.educational.framework.agents.model.ChatbotConversation;
import es.vargontoc.educational.framework.agents.model.ChatbotMessage;
import es.vargontoc.educational.framework.agents.ports.out.ChatbotConversationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class ChatbotConversationPersistenceAdapter implements ChatbotConversationRepository {

    private final ChatbotConversationJpaRepository conversationRepository;
    private final ChatbotMessageJpaRepository messageRepository;
    private final ChatbotConversationMapper conversationMapper;
    private final ChatbotMessageMapper messageMapper;

    public ChatbotConversationPersistenceAdapter(
            ChatbotConversationJpaRepository conversationRepository,
            ChatbotMessageJpaRepository messageRepository,
            ChatbotConversationMapper conversationMapper,
            ChatbotMessageMapper messageMapper) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
    }

    @Override
    public ChatbotConversation save(ChatbotConversation conversation) {
        ChatbotConversationJpaEntity entity = conversationMapper.toJpa(conversation);
        ChatbotConversationJpaEntity saved = conversationRepository.save(entity);
        return conversationMapper.toDomain(saved);
    }

    @Override
    public Optional<ChatbotConversation> findByConversationIdAndFamilyId(UUID conversationId, Long familyId) {
        return conversationRepository.findByConversationIdAndFamilyId(conversationId, familyId)
            .map(conversationMapper::toDomain);
    }

    @Override
    public List<ChatbotConversation> findByFamilyIdOrderByLastMessageAtDesc(Long familyId, int limit) {
        return conversationRepository.findByFamilyIdOrderByLastMessageAtDesc(familyId).stream()
            .limit(limit)
            .map(conversationMapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public ChatbotMessage saveMessage(ChatbotMessage message) {
        ChatbotMessageJpaEntity entity = messageMapper.toJpa(message);
        ChatbotMessageJpaEntity saved = messageRepository.save(entity);
        return messageMapper.toDomain(saved);
    }

    @Override
    public List<ChatbotMessage> findMessagesByConversationId(Long conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
            .map(messageMapper::toDomain)
            .collect(Collectors.toList());
    }
}
```

### 10. Mappers

**Archivos:**
- `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/mapper/ChatbotConversationMapper.java` (nuevo)
- `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/mapper/ChatbotMessageMapper.java` (nuevo)

**Especificación:**
```java
// ChatbotConversationMapper.java
package es.vargontoc.educational.framework.agents.infrastructure.mapper;

import es.vargontoc.educational.framework.agents.infrastructure.persistence.ChatbotConversationJpaEntity;
import es.vargontoc.educational.framework.agents.model.ChatbotConversation;
import es.vargontoc.educational.framework.shared.mapper.AbstractMapper;
import org.springframework.stereotype.Component;

@Component
public class ChatbotConversationMapper extends AbstractMapper<ChatbotConversation, ChatbotConversationJpaEntity> {

    @Override
    public ChatbotConversation toDomain(ChatbotConversationJpaEntity source) {
        ChatbotConversation target = new ChatbotConversation();
        target.setId(source.getId());
        target.setConversationId(source.getConversationId());
        target.setFamilyId(source.getFamilyId());
        target.setStartedAt(source.getStartedAt());
        target.setLastMessageAt(source.getLastMessageAt());
        return target;
    }

    @Override
    public ChatbotConversationJpaEntity toJpa(ChatbotConversation source) {
        ChatbotConversationJpaEntity target = new ChatbotConversationJpaEntity();
        target.setId(source.getId());
        target.setConversationId(source.getConversationId());
        target.setFamilyId(source.getFamilyId());
        target.setStartedAt(source.getStartedAt());
        target.setLastMessageAt(source.getLastMessageAt());
        return target;
    }
}
```

```java
// ChatbotMessageMapper.java
package es.vargontoc.educational.framework.agents.infrastructure.mapper;

import es.vargontoc.educational.framework.agents.infrastructure.persistence.ChatbotMessageJpaEntity;
import es.vargontoc.educational.framework.agents.model.ChatbotMessage;
import es.vargontoc.educational.framework.shared.mapper.AbstractMapper;
import org.springframework.stereotype.Component;

@Component
public class ChatbotMessageMapper extends AbstractMapper<ChatbotMessage, ChatbotMessageJpaEntity> {

    @Override
    public ChatbotMessage toDomain(ChatbotMessageJpaEntity source) {
        ChatbotMessage target = new ChatbotMessage();
        target.setId(source.getId());
        target.setConversationId(source.getConversationId());
        target.setRole(source.getRole());
        target.setContent(source.getContent());
        target.setCreatedAt(source.getCreatedAt());
        return target;
    }

    @Override
    public ChatbotMessageJpaEntity toJpa(ChatbotMessage source) {
        ChatbotMessageJpaEntity target = new ChatbotMessageJpaEntity();
        target.setId(source.getId());
        target.setConversationId(source.getConversationId());
        target.setRole(source.getRole());
        target.setContent(source.getContent());
        target.setCreatedAt(source.getCreatedAt());
        return target;
    }
}
```

### 11. Schema DDL — Migration

**Archivo:** `framework/backend/src/main/resources/db/migration/V{next}__create_chatbot_history.xml` (nuevo)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.9.xsd">

    <changeSet id="028_create_chatbot_conversation" author="vargontoc">
        <createTable tableName="chatbot_conversation">
            <column name="id" type="BIGSERIAL">
                <constraints nullable="false" primaryKey="true"/>
            </column>
            <column name="conversation_id" type="UUID">
                <constraints nullable="false" unique="true"/>
            </column>
            <column name="family_id" type="BIGINT">
                <constraints nullable="false"/>
            </column>
            <column name="started_at" type="TIMESTAMPTZ">
                <constraints nullable="false"/>
            </column>
            <column name="last_message_at" type="TIMESTAMPTZ">
                <constraints nullable="false"/>
            </column>
            <column name="created_at" type="TIMESTAMPTZ">
                <constraints nullable="false"/>
            </column>
            <column name="updated_at" type="TIMESTAMPTZ"/>
        </createTable>
        
        <createIndex indexName="idx_chatbot_conversation_family_id" tableName="chatbot_conversation">
            <column name="family_id"/>
        </createIndex>
        
        <createIndex indexName="idx_chatbot_conversation_last_message" tableName="chatbot_conversation">
            <column name="family_id"/>
            <column name="last_message_at"/>
        </createIndex>
    </changeSet>

    <changeSet id="029_create_chatbot_message" author="vargontoc">
        <createTable tableName="chatbot_message">
            <column name="id" type="BIGSERIAL">
                <constraints nullable="false" primaryKey="true"/>
            </column>
            <column name="conversation_id" type="BIGINT">
                <constraints nullable="false"/>
            </column>
            <column name="role" type="VARCHAR(20)">
                <constraints nullable="false"/>
            </column>
            <column name="content" type="VARCHAR(4000)">
                <constraints nullable="false"/>
            </column>
            <column name="created_at" type="TIMESTAMPTZ">
                <constraints nullable="false"/>
            </column>
            <column name="created_at" type="TIMESTAMPTZ">
                <constraints nullable="false"/>
            </column>
            <column name="updated_at" type="TIMESTAMPTZ"/>
        </createTable>
        
        <addForeignKeyConstraint
            baseTableName="chatbot_message"
            baseColumnNames="conversation_id"
            constraintName="fk_chatbot_message_conversation"
            referencedTableName="chatbot_conversation"
            referencedColumnNames="id"
            onDelete="CASCADE"/>
        
        <createIndex indexName="idx_chatbot_message_conversation_id" tableName="chatbot_message">
            <column name="conversation_id"/>
        </createIndex>
    </changeSet>
</databaseChangeLog>
```

## Contratos y dependencias externas

### Contratos OpenAPI

Nuevos schemas en `docs/contracts/api/openapi/schemas/agents/`:
- `chatbot-conversation-response.yaml`
- `chatbot-message-response.yaml`

Nuevos paths en `docs/contracts/api/openapi/paths/agents/`:
- `get-conversations.yaml`
- `get-conversation-by-id.yaml`

### Dependencias externas

| Capa | Dependencia | Estado |
|------|-------------|--------|
| Frontend | Consumirá API de historial en SPRINT-080+ | ⏳ Pendiente |
| Agents | Ninguna | ✅ Sin dependencia |
| TTS | Ninguna | ✅ Sin dependencia |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Crecimiento indefinido de historial | MEDIA | Definir política de retención (fuera de este sprint). Limitar consultas a 50 conversaciones. |
| R2 | Exposición de datos entre familias | ALTA | Validación estricta de `familyId` en todos los queries. Tests de aislamiento. |
| R3 | Contenido malicioso en mensajes | MEDIA | Sanitización con Jsoup antes de almacenar (reutilizar lógica de `AgentsService`). |

---

## Tareas del sprint

### Tarea 79.1: Crear modelos de dominio ChatbotConversation y ChatbotMessage

**Descripción:** Entidades de dominio puras, sin anotaciones JPA.

**Archivos:**
- `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/model/ChatbotConversation.java` (nuevo)
- `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/model/ChatbotMessage.java` (nuevo)

**Criterios de aceptación:**
- POJOs puros sin anotaciones JPA.
- Campos según especificación.
- Métodos getters/setters.
- Compilación sin errores.

---

### Tarea 79.2: Implementar puertos ChatbotHistoryUseCase y ChatbotConversationRepository

**Descripción:** Interfaces de puertos de entrada y salida.

**Archivos:**
- `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/ports/in/ChatbotHistoryUseCase.java` (nuevo)
- `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/ports/out/ChatbotConversationRepository.java` (nuevo)

**Criterios de aceptación:**
- `ChatbotHistoryUseCase` define operaciones de negocio.
- `ChatbotConversationRepository` abstrae persistencia.
- Compilación sin errores.

---

### Tarea 79.3: Implementar servicio ChatbotHistoryService

**Descripción:** Implementación del caso de uso con lógica de negocio.

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/service/ChatbotHistoryService.java` (nuevo)

**Criterios de aceptación:**
- Implementa `ChatbotHistoryUseCase`.
- Inyecta `ChatbotConversationRepository`.
- `@Service` y `@Transactional`.
- Compilación sin errores.

---

### Tarea 79.4: Implementar DTOs ConversationResponse y MessageResponse

**Descripción:** Records para response del endpoint.

**Archivos:**
- `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/dto/ConversationResponse.java` (nuevo)
- `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/dto/MessageResponse.java` (nuevo)

**Criterios de aceptación:**
- Records con campos correctos.
- Compilación sin errores.

---

### Tarea 79.5: Implementar entidades JPA y migration DDL

**Descripción:** Entidades JPA para persistencia y migration SQL.

**Archivos:**
- `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/persistence/ChatbotConversationJpaEntity.java` (nuevo)
- `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/persistence/ChatbotMessageJpaEntity.java` (nuevo)
- `framework/backend/src/main/resources/db/migration/V{next}__create_chatbot_history.xml` (nuevo)

**Criterios de aceptación:**
- Entidades JPA con anotaciones correctas.
- Migration crea tablas con columnas y constraints correctos.
- Índices creados para optimización de queries.
- Flyway ejecuta migration sin errores.

---

### Tarea 79.6: Implementar repositorios JPA y adaptador de persistencia

**Descripción:** Interfaces Spring Data JPA y adaptador que implementa el puerto.

**Archivos:**
- `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/persistence/ChatbotConversationJpaRepository.java` (nuevo)
- `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/persistence/ChatbotMessageJpaRepository.java` (nuevo)
- `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/persistence/ChatbotConversationPersistenceAdapter.java` (nuevo)

**Criterios de aceptación:**
- Repositorios extienden `JpaRepository`.
- Adaptador implementa `ChatbotConversationRepository`.
- Métodos de conversión entre dominio y JPA.
- Compilación sin errores.

---

### Tarea 79.7: Implementar mappers

**Descripción:** Mappers entre modelos de dominio y entidades JPA.

**Archivos:**
- `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/mapper/ChatbotConversationMapper.java` (nuevo)
- `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/mapper/ChatbotMessageMapper.java` (nuevo)

**Criterios de aceptación:**
- Extienden `AbstractMapper`.
- Métodos `toDomain()` y `toJpa()` copian todos los campos.
- `@Component`.
- Compilación sin errores.

---

### Tarea 79.8: Implementar controlador ChatbotHistoryController

**Descripción:** Adaptador REST con endpoints para listar y obtener conversaciones.

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/web/ChatbotHistoryController.java` (nuevo)

**Criterios de aceptación:**
- `@RestController` con `@RequestMapping("/api/v1/agents/conversations")`.
- Inyecta `ChatbotHistoryUseCase`.
- Endpoints `GET /` y `GET /{conversationId}`.
- Extrae `familyId` del `@RequestAttribute`.
- Respuestas con `ApiResponse`.
- Swagger documentado.
- Compilación sin errores.

---

### Tarea 79.9: Actualizar contratos OpenAPI

**Descripción:** Crear schemas y paths en `docs/contracts/api/openapi/`.

**Archivos:**
- `docs/contracts/api/openapi/schemas/agents/chatbot-conversation-response.yaml` (nuevo)
- `docs/contracts/api/openapi/schemas/agents/chatbot-message-response.yaml` (nuevo)
- `docs/contracts/api/openapi/paths/agents/get-conversations.yaml` (nuevo)
- `docs/contracts/api/openapi/paths/agents/get-conversation-by-id.yaml` (nuevo)

**Criterios de aceptación:**
- Schemas definen estructura de respuesta.
- Paths definen endpoints con parámetros y respuestas.
- Coherencia con implementación.

---

### Tarea 79.10: Pruebas de integración

**Descripción:** Tests de integración para validar funcionalidad y aislamiento entre familias.

**Archivo:** `framework/backend/src/test/java/es/vargontoc/educational/framework/agents/ChatbotHistoryIntegrationTest.java` (nuevo)

**Criterios de aceptación:**
- Test: crear conversación y recuperar con mensajes.
- Test: listar conversaciones de una familia.
- Test: aislamiento — familia A no ve conversaciones de familia B.
- Test: conversación no encontrada devuelve 404.
- Tests pasan con Testcontainers.

---

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|---------------|
| `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/model/ChatbotConversation.java` | Nuevo |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/model/ChatbotMessage.java` | Nuevo |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/ports/in/ChatbotHistoryUseCase.java` | Nuevo |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/ports/out/ChatbotConversationRepository.java` | Nuevo |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/service/ChatbotHistoryService.java` | Nuevo |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/dto/ConversationResponse.java` | Nuevo |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/dto/MessageResponse.java` | Nuevo |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/web/ChatbotHistoryController.java` | Nuevo |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/persistence/ChatbotConversationJpaEntity.java` | Nuevo |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/persistence/ChatbotMessageJpaEntity.java` | Nuevo |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/persistence/ChatbotConversationJpaRepository.java` | Nuevo |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/persistence/ChatbotMessageJpaRepository.java` | Nuevo |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/persistence/ChatbotConversationPersistenceAdapter.java` | Nuevo |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/mapper/ChatbotConversationMapper.java` | Nuevo |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/mapper/ChatbotMessageMapper.java` | Nuevo |
| `framework/backend/src/main/resources/db/migration/V{next}__create_chatbot_history.xml` | Nuevo |
| `framework/backend/src/test/java/es/vargontoc/educational/framework/agents/ChatbotHistoryIntegrationTest.java` | Nuevo |
| `docs/contracts/api/openapi/schemas/agents/chatbot-conversation-response.yaml` | Nuevo |
| `docs/contracts/api/openapi/schemas/agents/chatbot-message-response.yaml` | Nuevo |
| `docs/contracts/api/openapi/paths/agents/get-conversations.yaml` | Nuevo |
| `docs/contracts/api/openapi/paths/agents/get-conversation-by-id.yaml` | Nuevo |

## Estimación

- **Duración:** 2 días
- **Complejidad:** Media
- **Riesgo:** Medio (aislamiento entre familias, persistencia)

## Criterios de aceptación del sprint

1. Se pueden crear conversaciones asociadas a una familia. *(Funcionalidad)*
2. Se pueden añadir mensajes a una conversación existente. *(Funcionalidad)*
3. `GET /api/v1/agents/conversations` lista las conversaciones de la familia autenticada. *(Contrato)*
4. `GET /api/v1/agents/conversations/{conversationId}` obtiene una conversación específica con todos sus mensajes. *(Contrato)*
5. Familia A no puede ver conversaciones de familia B. *(Seguridad)*
6. Conversación no encontrada devuelve 404. *(Contrato)*
7. Los mensajes se almacenan sanitizados. *(Seguridad)*
8. Compilación sin errores. *(Calidad)*
9. Tests de integración pasando. *(Calidad)*

## Dependencias bloqueantes

- [x] ADR-003 aceptada.
- [x] Decisión de producto sobre historial confirmada.

## Handoffs a otras capas

### Frontend:
- SPRINT-080 consumirá esta API para mostrar historial en panel parental.

### Agents/TTS:
- Sin dependencia.

## Notas adicionales

### Privacidad infantil

- Solo se almacena contenido de conversaciones del chatbot parental (adultos).
- No se almacenan datos de menores ni contexto de sesión infantil.
- Aislamiento estricto entre familias.
- Política de retención pendiente de definir (fuera de este sprint).

### Preparación para SPRINT-080

Este sprint prepara la base de datos para el streaming de SPRINT-080:
- Schema DDL listo para persistir mensajes en tiempo real.
- API de historial permite recuperar conversaciones tras reconexión.
- `conversationId` se usará como correlador en eventos STOMP.
