package es.vargontoc.educational.framework.content.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CuriosityJpaRepository extends JpaRepository<CuriosityJpaEntity, Long> {

    List<CuriosityJpaEntity> findByTopicId(Long topicId);

    List<CuriosityJpaEntity> findByStatusAndLocaleAndMinAgeLessThanEqualAndMaxAgeGreaterThanEqual(
        String status, String locale, Integer age, Integer age2);
}
