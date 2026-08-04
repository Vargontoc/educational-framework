package es.vargontoc.educational.framework.contact.infrastructure.mapper;

import org.springframework.stereotype.Component;

import es.vargontoc.educational.framework.contact.infrastructure.persistence.ContactMessageJpaEntity;
import es.vargontoc.educational.framework.contact.model.ContactMessage;
import es.vargontoc.educational.framework.shared.mapper.AbstractMapper;

@Component
public class ContactMessageMapper extends AbstractMapper<ContactMessage, ContactMessageJpaEntity> {

    @Override
    public ContactMessage toDomain(ContactMessageJpaEntity source) {

        ContactMessage target = new ContactMessage();
        target.setId(source.getId());
        target.setType(source.getType());
        target.setClientIp(source.getClientIp());
        target.setCreatedAt(source.getCreatedAt());
        return target;
    }

    @Override
    public ContactMessageJpaEntity toJpa(ContactMessage source) {
        ContactMessageJpaEntity target = new ContactMessageJpaEntity();
        target.setId(source.getId());
        target.setType(source.getType());
        target.setMessage(source.getMessage());
        target.setClientIp(source.getClientIp());
        return target;
    }
    
}
