package es.vargontoc.educational.framework.tracking.infrastructure.web;

import es.vargontoc.educational.framework.family.model.ChildProfile;
import es.vargontoc.educational.framework.family.model.Family;
import es.vargontoc.educational.framework.family.ports.in.ChildProfileUseCase;
import es.vargontoc.educational.framework.family.ports.in.FamilyUseCase;
import es.vargontoc.educational.framework.shared.exception.ForbiddenException;
import es.vargontoc.educational.framework.tracking.ports.in.GetActivityEngagementSummaryUseCase;
import es.vargontoc.educational.framework.tracking.service.TrackingDashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit-level counterpart to {@link TrackingDashboardControllerTest}'s single-family happy path
 * tests. Verifies the "child does not belong to the current family" 403 branch of
 * {@code verifyChildBelongsToFamily} without persisting a second {@code family} row, which the
 * schema forbids (see SPRINT-087): {@link FamilyUseCase} and {@link ChildProfileUseCase} are
 * mocked directly instead of going through a real database.
 */
@ExtendWith(MockitoExtension.class)
class TrackingDashboardControllerAuthorizationTest {

    private static final Long CURRENT_FAMILY_ID = 1L;
    private static final Long OTHER_FAMILY_CHILD_ID = 99L;

    @Mock
    private TrackingDashboardService dashboardService;

    @Mock
    private GetActivityEngagementSummaryUseCase engagementSummaryUseCase;

    @Mock
    private FamilyUseCase familyUseCase;

    @Mock
    private ChildProfileUseCase childProfileUseCase;

    @InjectMocks
    private TrackingDashboardController controller;

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
            () -> controller.getChildTrackingSummary(OTHER_FAMILY_CHILD_ID));
        verifyNoInteractions(dashboardService);
    }

    @Test
    void getActivities_unauthorizedChildProfile_throwsForbidden() {
        assertThrows(ForbiddenException.class,
            () -> controller.getActivityPerformance(OTHER_FAMILY_CHILD_ID));
        verifyNoInteractions(dashboardService);
    }

    @Test
    void getTopics_unauthorizedChildProfile_throwsForbidden() {
        assertThrows(ForbiddenException.class,
            () -> controller.getTopicPerformance(OTHER_FAMILY_CHILD_ID));
        verifyNoInteractions(dashboardService);
    }

    @Test
    void getDifficulty_unauthorizedChildProfile_throwsForbidden() {
        assertThrows(ForbiddenException.class,
            () -> controller.getDifficultyEvolution(OTHER_FAMILY_CHILD_ID, null));
        verifyNoInteractions(dashboardService);
    }

    @Test
    void getResponseTime_unauthorizedChildProfile_throwsForbidden() {
        assertThrows(ForbiddenException.class,
            () -> controller.getResponseTimeMetrics(OTHER_FAMILY_CHILD_ID));
        verifyNoInteractions(dashboardService);
    }

    @Test
    void getAchievements_unauthorizedChildProfile_throwsForbidden() {
        assertThrows(ForbiddenException.class,
            () -> controller.getChildAchievements(OTHER_FAMILY_CHILD_ID, null));
        verifyNoInteractions(dashboardService);
    }

    @Test
    void getLearningProgress_unauthorizedChildProfile_throwsForbidden() {
        assertThrows(ForbiddenException.class,
            () -> controller.getChildLearningProgress(OTHER_FAMILY_CHILD_ID, null));
        verifyNoInteractions(dashboardService);
    }

    @Test
    void getEngagement_unauthorizedChildProfile_throwsForbidden() {
        assertThrows(ForbiddenException.class,
            () -> controller.getActivityEngagementSummary(OTHER_FAMILY_CHILD_ID));
        verifyNoInteractions(engagementSummaryUseCase);
    }
}
