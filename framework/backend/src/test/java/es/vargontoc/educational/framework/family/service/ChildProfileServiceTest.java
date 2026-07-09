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
import static org.junit.jupiter.api.Assertions.assertNull;
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
        family.setTtsEnabled(false);
        family.setAgentEnabled(false);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(childProfileRepository.save(any(ChildProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var child = childProfileService.createChild(1L, "Kid", LocalDate.now().minusYears(10), null, true, true, null);

        assertFalse(child.isTtsEnabled());
        assertFalse(child.isAgentEnabled());
        assertTrue(child.isActive());
        assertEquals(ColorVisionMode.NONE, child.getColorVisionMode());
    }

    @Test
    void createChild_keepsEnabledFlagsWhenFamilyAllows() {
        var family = new Family();
        family.setId(1L);
        family.setTtsEnabled(true);
        family.setAgentEnabled(true);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(childProfileRepository.save(any(ChildProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var child = childProfileService.createChild(1L, "Kid", LocalDate.now().minusYears(8), "avatar", true, true, ColorVisionMode.NONE);

        assertTrue(child.isTtsEnabled());
        assertTrue(child.isAgentEnabled());
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
    void updateChild_reappliesCeilingAfterFamilyChange() {
        var family = new Family();
        family.setTtsEnabled(false);
        family.setAgentEnabled(true);

        var child = new ChildProfile();
        child.setId(2L);
        child.setFamilyId(1L);
        child.setTtsEnabled(true);
        child.setAgentEnabled(true);
        child.setColorVisionMode(ColorVisionMode.NONE);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(childProfileRepository.findById(2L)).thenReturn(Optional.of(child));
        when(childProfileRepository.save(any(ChildProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(childSessionRepository.findActiveByChildProfileId(2L)).thenReturn(Optional.empty());

        var updated = childProfileService.updateChild(
            2L,
            "Kid Updated",
            LocalDate.now().minusYears(9),
            null,
            true,
            true,
            ColorVisionMode.NONE
        );

        assertFalse(updated.isTtsEnabled());
        assertTrue(updated.isAgentEnabled());
    }

    @Test
    void createChild_defaultsColorVisionModeToNoneWhenNull() {
        var family = new Family();
        family.setId(1L);
        family.setTtsEnabled(true);
        family.setAgentEnabled(true);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(childProfileRepository.save(any(ChildProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var child = childProfileService.createChild(1L, "Kid", LocalDate.now().minusYears(7), "avatar", true, true, null);

        assertEquals(ColorVisionMode.NONE, child.getColorVisionMode());
    }

    @Test
    void createChild_storesNonDefaultColorVisionMode() {
        var family = new Family();
        family.setId(1L);
        family.setTtsEnabled(true);
        family.setAgentEnabled(true);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(childProfileRepository.save(any(ChildProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var child = childProfileService.createChild(1L, "Kid", LocalDate.now().minusYears(7), "avatar", true, true, ColorVisionMode.DEUTERANOMALY);

        assertEquals(ColorVisionMode.DEUTERANOMALY, child.getColorVisionMode());
    }

    @Test
    void updateChild_preservesColorVisionModeWhenNull() {
        var family = new Family();
        family.setTtsEnabled(true);
        family.setAgentEnabled(true);

        var child = new ChildProfile();
        child.setId(3L);
        child.setFamilyId(1L);
        child.setTtsEnabled(true);
        child.setAgentEnabled(true);
        child.setColorVisionMode(ColorVisionMode.PROTANOPIA);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(childProfileRepository.findById(3L)).thenReturn(Optional.of(child));
        when(childProfileRepository.save(any(ChildProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var updated = childProfileService.updateChild(
            3L,
            "Kid Updated",
            LocalDate.now().minusYears(8),
            null,
            true,
            true,
            null
        );

        assertEquals(ColorVisionMode.PROTANOPIA, updated.getColorVisionMode());
    }

    @Test
    void updateChild_updatesColorVisionModeWhenProvided() {
        var family = new Family();
        family.setTtsEnabled(true);
        family.setAgentEnabled(true);

        var child = new ChildProfile();
        child.setId(4L);
        child.setFamilyId(1L);
        child.setTtsEnabled(true);
        child.setAgentEnabled(true);
        child.setColorVisionMode(ColorVisionMode.NONE);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(childProfileRepository.findById(4L)).thenReturn(Optional.of(child));
        when(childProfileRepository.save(any(ChildProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var updated = childProfileService.updateChild(
            4L,
            "Kid Updated",
            LocalDate.now().minusYears(8),
            null,
            true,
            true,
            ColorVisionMode.TRITANOPIA
        );

        assertEquals(ColorVisionMode.TRITANOPIA, updated.getColorVisionMode());
    }
}
