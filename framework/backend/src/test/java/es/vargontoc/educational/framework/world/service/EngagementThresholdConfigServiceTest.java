package es.vargontoc.educational.framework.world.service;

import es.vargontoc.educational.framework.world.model.WorldEngagementThresholdConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EngagementThresholdConfigServiceTest {

    private EngagementThresholdConfigService service;

    @BeforeEach
    void setUp() {
        service = new EngagementThresholdConfigService();
    }

    @Test
    void engagementThresholdConfig_returnsNEquals3() {
        WorldEngagementThresholdConfig config = service.engagementThresholdConfig(1L);

        assertEquals(3, config.getWindowSize());
    }

    @Test
    void engagementThresholdConfig_returnsMEquals2() {
        WorldEngagementThresholdConfig config = service.engagementThresholdConfig(1L);

        assertEquals(2, config.getAbandonmentThreshold());
    }

    @Test
    void engagementThresholdConfig_ignoresChildProfileId() {
        WorldEngagementThresholdConfig config1 = service.engagementThresholdConfig(1L);
        WorldEngagementThresholdConfig config2 = service.engagementThresholdConfig(999L);

        assertNotNull(config1.getChildProfileId());
        assertNotNull(config2.getChildProfileId());
        assertEquals(3, config1.getWindowSize());
        assertEquals(3, config2.getWindowSize());
    }
}