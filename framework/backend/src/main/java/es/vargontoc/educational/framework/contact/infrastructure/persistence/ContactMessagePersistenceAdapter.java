package es.vargontoc.educational.framework.contact.infrastructure.persistence;

import java.time.LocalDateTime;

import org.springframework.stereotype.Repository;

import es.vargontoc.educational.framework.contact.infrastructure.mapper.ContactMessageMapper;
import es.vargontoc.educational.framework.contact.model.ContactMessage;
import es.vargontoc.educational.framework.contact.ports.out.ContactMessageRepository;
import io.micrometer.common.lang.NonNull;

@Repository
public class ContactMessagePersistenceAdapter implements ContactMessageRepository {

    private final ContactMessageJpaRepository jpaRepository;
    private final ContactMessageMapper mapper;
    ContactMessagePersistenceAdapter(ContactMessageJpaRepository jpaRepository, ContactMessageMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }


    @SuppressWarnings("null")
    @Override
    public ContactMessage save(@NonNull ContactMessage message) {
        return mapper.toDomain(jpaRepository.save(mapper.toJpa(message)));
    }

    @Override
    public long countByClientIpAndCreatedAtAfter(String clientIp, LocalDateTime since) {
        return jpaRepository.countByClientIpAndCreatedAtAfter(clientIp, since);
    }
    
}
