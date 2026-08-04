package es.vargontoc.educational.framework.family.service;

import es.vargontoc.educational.framework.family.model.ChildProfile;
import es.vargontoc.educational.framework.family.model.ColorVisionMode;
import es.vargontoc.educational.framework.family.model.Family;
import es.vargontoc.educational.framework.family.ports.out.ChildProfileRepository;
import es.vargontoc.educational.framework.family.ports.out.FamilyRepository;
import es.vargontoc.educational.framework.session.ports.in.ChildSessionUseCase;
import es.vargontoc.educational.framework.session.ports.out.ChildSessionRepository;
import es.vargontoc.educational.framework.session.infrastructure.websocket.SessionEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChildProfileServiceTest {

    @Mock
    private FamilyRepository familyRepository;

    @Mock
    private ChildProfileRepository childProfileRepository;

    @Mock
    private ChildSessionUseCase childSessionUseCase;

    @Mock
    private ChildSessionRepository childSessionRepository;

    @Mock
    private SessionEventPublisher sessionEventPublisher;

    @InjectMocks
    private ChildProfileService childProfileService;

    @Test
    void createChild_appliesFlagCeilingWhenFamilyDisabled() {
        var family = new Family();
        family.setId(1L);
        family.setNpcVoiceEnabled(false);
        family.setNpcEnabled(false);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(childProfileRepository.save(any(ChildProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var child = childProfileService.createChild(1L, "Kid", LocalDate.now().minusYears(3), null, true, true, 80, null);

        assertFalse(child.isNpcVoiceEnabled());
        assertFalse(child.isNpcEnabled());
        assertEquals(0, child.getNpcVoiceVolume());
        assertTrue(child.isActive());
        assertEquals(ColorVisionMode.NONE, child.getColorVisionMode());
    }

    @Test
    void createChild_keepsEnabledFlagsWhenFamilyAllows() {
        var family = new Family();
        family.setId(1L);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(childProfileRepository.save(any(ChildProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var child = childProfileService.createChild(1L, "Kid", LocalDate.now().minusYears(3), "avatar", true, true, 80, ColorVisionMode.NONE);

        assertTrue(child.isNpcVoiceEnabled());
        assertTrue(child.isNpcEnabled());
        assertEquals(80, child.getNpcVoiceVolume());
    }

    @Test
    void createChild_appliesVolumeCeilingFromFamily() {
        var family = new Family();
        family.setId(1L);
        family.setNpcVoiceVolume(50);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(childProfileRepository.save(any(ChildProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var child = childProfileService.createChild(1L, "Kid", LocalDate.now().minusYears(3), "avatar", true, true, 80, ColorVisionMode.NONE);

        assertTrue(child.isNpcVoiceEnabled());
        assertEquals(50, child.getNpcVoiceVolume());
    }

    @Test
    void createChild_volumeIsZeroWhenNpcVoiceDisabled() {
        var family = new Family();
        family.setId(1L);
        family.setNpcVoiceEnabled(true);
        family.setNpcVoiceVolume(100);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(childProfileRepository.save(any(ChildProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var child = childProfileService.createChild(1L, "Kid", LocalDate.now().minusYears(3), "avatar", false, true, 80, ColorVisionMode.NONE);

        assertFalse(child.isNpcVoiceEnabled());
        assertEquals(0, child.getNpcVoiceVolume());
    }

    @Test
    void createChild_volumeIsZeroWhenFamilyVolumeIsZero() {
        var family = new Family();
        family.setId(1L);
        family.setNpcVoiceVolume(0);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(childProfileRepository.save(any(ChildProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var child = childProfileService.createChild(1L, "Kid", LocalDate.now().minusYears(3), "avatar", true, true, 80, ColorVisionMode.NONE);

        assertTrue(child.isNpcVoiceEnabled());
        assertEquals(0, child.getNpcVoiceVolume());
    }

    @Test
    void changeActiveState_setsActiveFalseWhenWasTrue() {
        var child = new ChildProfile();
        child.setId(7L);
        child.setFamilyId(1L);
        child.setActive(true);

        when(childProfileRepository.findById(7L)).thenReturn(Optional.of(child));
        when(childProfileRepository.save(any(ChildProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(childSessionUseCase.getActiveSessions(1L)).thenReturn(List.of());

        childProfileService.changeActiveState(7L);

        assertFalse(child.isActive());
    }

    @Test
    void createChild_defaultsColorVisionModeToNoneWhenNull() {
        var family = new Family();
        family.setId(1L);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(childProfileRepository.save(any(ChildProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var child = childProfileService.createChild(1L, "Kid", LocalDate.now().minusYears(3), "avatar", true, true, 100, null);

        assertEquals(ColorVisionMode.NONE, child.getColorVisionMode());
    }

    @Test
    void createChild_storesNonDefaultColorVisionMode() {
        var family = new Family();
        family.setId(1L);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(childProfileRepository.save(any(ChildProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var child = childProfileService.createChild(1L, "Kid", LocalDate.now().minusYears(3), "avatar", true, true, 100, ColorVisionMode.DEUTERANOMALY);

        assertEquals(ColorVisionMode.DEUTERANOMALY, child.getColorVisionMode());
    }


    @Test
    void createChild_clampsNegativeVolumeToZero() {
        var family = new Family();
        family.setId(1L);
        family.setNpcVoiceVolume(100);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(childProfileRepository.save(any(ChildProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var child = childProfileService.createChild(1L, "Kid", LocalDate.now().minusYears(3), "avatar", true, true, -10, ColorVisionMode.NONE);

        assertEquals(0, child.getNpcVoiceVolume());
    }

    @Test
    void createChild_clampsVolumeAbove100To100() {
        var family = new Family();
        family.setId(1L);
        family.setNpcVoiceVolume(100);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(childProfileRepository.save(any(ChildProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var child = childProfileService.createChild(1L, "Kid", LocalDate.now().minusYears(3), "avatar", true, true, 150, ColorVisionMode.NONE);

        assertEquals(100, child.getNpcVoiceVolume());
    }

    @Test
    void createChild_clampsVolumeAbove100RespectingFamilyCeiling() {
        var family = new Family();
        family.setId(1L);
        family.setNpcVoiceVolume(60);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(childProfileRepository.save(any(ChildProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var child = childProfileService.createChild(1L, "Kid", LocalDate.now().minusYears(3), "avatar", true, true, 150, ColorVisionMode.NONE);

        assertEquals(60, child.getNpcVoiceVolume());
    }

    @Test
    void createChild_keepsVolume50WithinRange() {
        var family = new Family();
        family.setId(1L);
        family.setNpcVoiceVolume(100);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(childProfileRepository.save(any(ChildProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var child = childProfileService.createChild(1L, "Kid", LocalDate.now().minusYears(3), "avatar", true, true, 50, ColorVisionMode.NONE);

        assertEquals(50, child.getNpcVoiceVolume());
    }

}
