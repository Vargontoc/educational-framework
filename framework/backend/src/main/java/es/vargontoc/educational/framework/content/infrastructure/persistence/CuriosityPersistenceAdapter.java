package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.Curiosity;
import es.vargontoc.educational.framework.content.ports.out.CuriosityRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class CuriosityPersistenceAdapter implements CuriosityRepository {

    private final CuriosityJpaRepository jpaRepository;

    public CuriosityPersistenceAdapter(CuriosityJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Curiosity> findById(Long id) {
        return jpaRepository.findById(id)
            .map(CuriosityPersistenceAdapter::toDomain);
    }

    @Override
    public List<Curiosity> findAll() {
        return jpaRepository.findAll().stream()
            .map(CuriosityPersistenceAdapter::toDomain)
            .toList();
    }

    @Override
    public List<Curiosity> findByTopicId(Long topicId) {
        return jpaRepository.findByTopicId(topicId).stream()
            .map(CuriosityPersistenceAdapter::toDomain)
            .toList();
    }

    @Override
    public List<Curiosity> findActiveByFilters(Long topicId, Integer age, String locale) {
        if (age != null && locale != null) {
            return jpaRepository.findByStatusAndLocaleAndMinAgeLessThanEqualAndMaxAgeGreaterThanEqual(
                    ContentStatus.ACTIVE.name(), locale, age, age).stream()
                .map(CuriosityPersistenceAdapter::toDomain)
                .filter(c -> topicId == null || topicId.equals(c.getTopicId()))
                .toList();
        }
        return Collections.emptyList();
    }

    @Override
    public Curiosity save(Curiosity curiosity) {
        var saved = jpaRepository.save(toJpa(curiosity));
        return toDomain(saved);
    }

    private static Curiosity toDomain(CuriosityJpaEntity source) {
        var target = new Curiosity();
        target.setId(source.getId());
        target.setTopicId(source.getTopicId());
        target.setText(source.getText());
        target.setMinAge(source.getMinAge());
        target.setMaxAge(source.getMaxAge());
        target.setTags(parseTags(source.getTags()));
        target.setLocale(source.getLocale());
        target.setPhoneticHint(source.getPhoneticHint());
        target.setStatus(ContentStatus.valueOf(source.getStatus()));
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    private static CuriosityJpaEntity toJpa(Curiosity source) {
        var target = new CuriosityJpaEntity();
        target.setId(source.getId());
        target.setTopicId(source.getTopicId());
        target.setText(source.getText());
        target.setMinAge(source.getMinAge());
        target.setMaxAge(source.getMaxAge());
        target.setTags(joinTags(source.getTags()));
        target.setLocale(source.getLocale());
        target.setPhoneticHint(source.getPhoneticHint());
        target.setStatus(source.getStatus().name());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    private static List<String> parseTags(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(raw.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
    }

    private static String joinTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return String.join(",", tags);
    }
}
