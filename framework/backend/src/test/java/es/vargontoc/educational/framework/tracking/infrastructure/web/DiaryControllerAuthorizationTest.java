package es.vargontoc.educational.framework.tracking.infrastructure.web;

import es.vargontoc.educational.framework.family.model.ChildProfile;
import es.vargontoc.educational.framework.family.model.Family;
import es.vargontoc.educational.framework.family.ports.in.ChildProfileUseCase;
import es.vargontoc.educational.framework.family.ports.in.FamilyUseCase;
import es.vargontoc.educational.framework.shared.exception.ForbiddenException;
import es.vargontoc.educational.framework.tracking.service.DiaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiaryControllerAuthorizationTest {

    private static final Long CURRENT_FAMILY_ID = 1L;
    private static final Long OTHER_FAMILY_CHILD_ID = 99L;

    @Mock
    private DiaryService diaryService;

    @Mock
    private FamilyUseCase familyUseCase;

    @Mock
    private ChildProfileUseCase childProfileUseCase;

    @InjectMocks
    private DiaryController controller;

    @BeforeEach
    void setUp() {
        var family = new Family();
        family.setId(CURRENT_FAMILY_ID);
        when(familyUseCase.getFamily()).thenReturn(family);

        var otherFamilyChild = new ChildProfile();
        otherFamilyChild.setId(OTHER_FAMILY_CHILD_ID);
        otherFamilyChild.setFamilyId(2L);
        when(childProfileUseCase.getChild(OTHER_FAMILY_CHILD_ID)).thenReturn(otherFamilyChild);
    }

    @Test
    void getSummary_unauthorizedChildProfile_throwsForbidden() {
        assertThrows(ForbiddenException.class,
            () -> controller.getSummary(OTHER_FAMILY_CHILD_ID, null));
        verifyNoInteractions(diaryService);
    }

    @Test
    void getActivities_unauthorizedChildProfile_throwsForbidden() {
        assertThrows(ForbiddenException.class,
            () -> controller.getActivities(OTHER_FAMILY_CHILD_ID, null));
        verifyNoInteractions(diaryService);
    }

    @Test
    void getAbandonmentSignal_unauthorizedChildProfile_throwsForbidden() {
        assertThrows(ForbiddenException.class,
            () -> controller.getAbandonmentSignal(OTHER_FAMILY_CHILD_ID, 1L));
        verifyNoInteractions(diaryService);
    }
}
