package es.vargontoc.educational.framework.family.infrastructure.persistence;

import es.vargontoc.educational.framework.family.model.AdultProfile;
import es.vargontoc.educational.framework.family.ports.out.AdultProfileRepository;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AdultProfilePersistenceAdapter implements AdultProfileRepository {

    private final AdultProfileJpaRepository jpaRepository;

    public AdultProfilePersistenceAdapter(AdultProfileJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<AdultProfile> findById(Long id) {
        return jpaRepository.findById(id).map(AdultProfilePersistenceAdapter::toDomain);
    }

    @Override
    public List<AdultProfile> findAll() {
        return jpaRepository.findAll().stream()
            .map(AdultProfilePersistenceAdapter::toDomain)
            .toList();
    }

    @Override
    public AdultProfile save(AdultProfile adult) {
        return toDomain(jpaRepository.save(toJpa(adult)));
    }

    @Override
    public void deleteById(Long id) {
        if (!jpaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Adult profile not found");
        }
        jpaRepository.deleteById(id);
    }

    private static AdultProfile toDomain(AdultProfileJpaEntity source) {
        var target = new AdultProfile();
        target.setId(source.getId());
        target.setFamilyId(source.getFamilyId());
        target.setName(source.getName());
        target.setBirthday(source.getBirthday());
        target.setAvatar(source.getAvatar());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    private static AdultProfileJpaEntity toJpa(AdultProfile source) {
        var target = new AdultProfileJpaEntity();
        target.setId(source.getId());
        target.setFamilyId(source.getFamilyId());
        target.setName(source.getName());
        target.setBirthday(source.getBirthday());
        target.setAvatar(source.getAvatar());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }
}
