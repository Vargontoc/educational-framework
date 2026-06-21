package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.shared.exception.ValidationException;
import es.vargontoc.educational.framework.tracking.model.ChildAchievement;
import es.vargontoc.educational.framework.tracking.ports.out.ChildAchievementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChildAchievementServiceTest {

    @Mock
    private ChildAchievementRepository repository;

    @InjectMocks
    private ChildAchievementService service;

    @Test
    void registerGlobalAchievement_createsRecord() {
        when(repository.findByChildProfileIdAndAchievementCodeAndActivityIdAndTopicId(1L, "FIRST_LOGIN", null, null))
                .thenReturn(Optional.empty());
        when(repository.save(any(ChildAchievement.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean registered = service.registerAchievement(1L, "FIRST_LOGIN", null, null);

        assertTrue(registered);
        var captor = ArgumentCaptor.forClass(ChildAchievement.class);
        verify(repository).save(captor.capture());
        var saved = captor.getValue();
        assertEquals(1L, saved.getChildProfileId());
        assertEquals("FIRST_LOGIN", saved.getAchievementCode());
        assertEquals(null, saved.getActivityId());
        assertEquals(null, saved.getTopicId());
        assertNotNull(saved.getEarnedAt());
    }

    @Test
    void registerActivitySpecificAchievement_createsRecord() {
        when(repository.findByChildProfileIdAndAchievementCodeAndActivityIdAndTopicId(1L, "GAME_MASTER", 10L, null))
                .thenReturn(Optional.empty());
        when(repository.save(any(ChildAchievement.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean registered = service.registerAchievement(1L, "GAME_MASTER", 10L, null);

        assertTrue(registered);
        var captor = ArgumentCaptor.forClass(ChildAchievement.class);
        verify(repository).save(captor.capture());
        var saved = captor.getValue();
        assertEquals(1L, saved.getChildProfileId());
        assertEquals("GAME_MASTER", saved.getAchievementCode());
        assertEquals(10L, saved.getActivityId());
        assertEquals(null, saved.getTopicId());
    }

    @Test
    void registerDuplicate_ignored() {
        var existing = new ChildAchievement();
        existing.setChildProfileId(1L);
        existing.setAchievementCode("FIRST_LOGIN");

        when(repository.findByChildProfileIdAndAchievementCodeAndActivityIdAndTopicId(1L, "FIRST_LOGIN", null, null))
                .thenReturn(Optional.of(existing));

        boolean registered = service.registerAchievement(1L, "FIRST_LOGIN", null, null);

        assertFalse(registered);
        verify(repository, never()).save(any());
    }

    @Test
    void getChildAchievements_returnsAll() {
        var achievement = new ChildAchievement();
        achievement.setChildProfileId(1L);
        achievement.setAchievementCode("FIRST_LOGIN");
        achievement.setEarnedAt(LocalDateTime.now());

        when(repository.findByChildProfileId(1L)).thenReturn(List.of(achievement));

        List<ChildAchievement> result = service.getChildAchievements(1L);

        assertEquals(1, result.size());
        assertEquals("FIRST_LOGIN", result.get(0).getAchievementCode());
    }

    @Test
    void getChildAchievements_withActivityFilter() {
        var achievement = new ChildAchievement();
        achievement.setChildProfileId(1L);
        achievement.setAchievementCode("GAME_MASTER");
        achievement.setActivityId(10L);

        when(repository.findByChildProfileIdAndActivityId(1L, 10L))
                .thenReturn(List.of(achievement));

        List<ChildAchievement> result = service.getChildAchievements(1L, 10L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getActivityId());
    }

    @Test
    void getChildAchievements_withTopicFilter() {
        var achievement = new ChildAchievement();
        achievement.setChildProfileId(1L);
        achievement.setAchievementCode("TOPIC_EXPLORER");
        achievement.setTopicId(5L);

        when(repository.findByChildProfileIdAndTopicId(1L, 5L))
                .thenReturn(List.of(achievement));

        List<ChildAchievement> result = service.getChildAchievements(1L, null, 5L);

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getTopicId());
    }

    @Test
    void getChildAchievements_withBothFilters() {
        var achievement = new ChildAchievement();
        achievement.setChildProfileId(1L);
        achievement.setAchievementCode("ACTIVITY_COMPLETE");
        achievement.setActivityId(10L);
        achievement.setTopicId(5L);

        when(repository.findByChildProfileIdAndActivityIdAndTopicId(1L, 10L, 5L))
                .thenReturn(List.of(achievement));

        List<ChildAchievement> result = service.getChildAchievements(1L, 10L, 5L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getActivityId());
        assertEquals(5L, result.get(0).getTopicId());
    }

    @Test
    void registerAchievement_blankCode_throwsValidationException() {
        assertThrows(ValidationException.class, () ->
                service.registerAchievement(1L, "", null, null));
    }

    @Test
    void registerAchievement_nullCode_throwsValidationException() {
        assertThrows(ValidationException.class, () ->
                service.registerAchievement(1L, null, null, null));
    }
}
