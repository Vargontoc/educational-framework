package es.vargontoc.educational.framework.avatar.infrastructure.tts;

import es.vargontoc.educational.framework.content.model.AvatarTone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TtsToneMapperTest {

    private TtsToneMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TtsToneMapper();
    }

    @Test
    void calm_mapsToCalm() {
        assertEquals("calm", mapper.map(AvatarTone.CALM));
    }

    @Test
    void joyful_mapsToJoyful() {
        assertEquals("joyful", mapper.map(AvatarTone.JOYFUL));
    }

    @Test
    void enthusiastic_mapsToEnthusiastic() {
        assertEquals("enthusiastic", mapper.map(AvatarTone.ENTHUSIASTIC));
    }

    @Test
    void serious_mapsToSerious() {
        assertEquals("serious", mapper.map(AvatarTone.SERIOUS));
    }

    @Test
    void neutral_mapsToCalm() {
        assertEquals("calm", mapper.map(AvatarTone.NEUTRAL));
    }

    @Test
    void tender_mapsToTender() {
        assertEquals("tender", mapper.map(AvatarTone.TENDER));
    }

    @Test
    void mysterious_mapsToMysterious() {
        assertEquals("mysterious", mapper.map(AvatarTone.MYSTERIOUS));
    }

    @Test
    void null_tone_mapsToCalm() {
        assertEquals("calm", mapper.map(null));
    }
}