package es.vargontoc.educational.framework.family.service;

import es.vargontoc.educational.framework.family.model.ChildProfile;
import es.vargontoc.educational.framework.family.model.Family;
import es.vargontoc.educational.framework.family.ports.out.ChildProfileRepository;
import es.vargontoc.educational.framework.family.ports.out.FamilyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

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

        var child = childProfileService.createChild(1L, "Kid", LocalDate.now().minusYears(10), null, true, true);

        assertFalse(child.isTtsEnabled());
        assertFalse(child.isAgentEnabled());
        assertTrue(child.isActive());
    }

    @Test
    void createChild_keepsEnabledFlagsWhenFamilyAllows() {
        var family = new Family();
        family.setId(1L);
        family.setTtsEnabled(true);
        family.setAgentEnabled(true);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(childProfileRepository.save(any(ChildProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var child = childProfileService.createChild(1L, "Kid", LocalDate.now().minusYears(8), "avatar", true, true);

        assertTrue(child.isTtsEnabled());
        assertTrue(child.isAgentEnabled());
    }

    @Test
    void deactivateChild_setsActiveFalse() {
        var child = new ChildProfile();
        child.setActive(true);

        when(childProfileRepository.findById(7L)).thenReturn(Optional.of(child));
        when(childProfileRepository.save(any(ChildProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        childProfileService.deactivateChild(7L);

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

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(childProfileRepository.findById(2L)).thenReturn(Optional.of(child));
        when(childProfileRepository.save(any(ChildProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var updated = childProfileService.updateChild(
            2L,
            "Kid Updated",
            LocalDate.now().minusYears(9),
            null,
            true,
            true
        );

        assertFalse(updated.isTtsEnabled());
        assertTrue(updated.isAgentEnabled());
    }
}
