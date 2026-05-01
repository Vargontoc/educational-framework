package es.vargontoc.educational.framework.family.infrastructure.persistence;

import es.vargontoc.educational.framework.family.model.ChildProfile;
import es.vargontoc.educational.framework.family.ports.out.ChildProfileRepository;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ChildProfilePersistenceAdapter implements ChildProfileRepository {

    private final ChildProfileJpaRepository jpaRepository;

    public ChildProfilePersistenceAdapter(ChildProfileJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<ChildProfile> findById(Long id) {
        return jpaRepository.findById(id).map(ChildProfilePersistenceAdapter::toDomain);
    }

    @Override
    public List<ChildProfile> findAll() {
        return jpaRepository.findAll().stream()
            .map(ChildProfilePersistenceAdapter::toDomain)
            .toList();
    }

    @Override
    public ChildProfile save(ChildProfile child) {
        return toDomain(jpaRepository.save(toJpa(child)));
    }

    @Override
    public void deleteById(Long id) {
        if (!jpaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Child profile not found");
        }
        jpaRepository.deleteById(id);
    }

    private static ChildProfile toDomain(ChildProfileJpaEntity source) {
        var target = new ChildProfile();
        target.setId(source.getId());
        target.setFamilyId(source.getFamilyId());
        target.setName(source.getName());
        target.setActive(source.isActive());
        target.setBirthday(source.getBirthday());
        target.setAvatar(source.getAvatar());
        target.setTtsEnabled(source.isTtsEnabled());
        target.setAgentEnabled(source.isAgentEnabled());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    private static ChildProfileJpaEntity toJpa(ChildProfile source) {
        var target = new ChildProfileJpaEntity();
        target.setId(source.getId());
        target.setFamilyId(source.getFamilyId());
        target.setName(source.getName());
        target.setActive(source.isActive());
        target.setBirthday(source.getBirthday());
        target.setAvatar(source.getAvatar());
        target.setTtsEnabled(source.isTtsEnabled());
        target.setAgentEnabled(source.isAgentEnabled());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }
}
