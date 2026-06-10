package es.vargontoc.educational.framework.avatar.ports.out;

import es.vargontoc.educational.framework.content.model.AvatarTone;

public interface TtsClient {

    byte[] synthesize(String text, String locale, AvatarTone tone, String voiceProfile);
}