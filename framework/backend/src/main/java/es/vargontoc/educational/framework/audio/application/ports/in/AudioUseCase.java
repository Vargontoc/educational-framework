package es.vargontoc.educational.framework.audio.application.ports.in;

import es.vargontoc.educational.framework.audio.domain.AudioRequest;
import es.vargontoc.educational.framework.audio.domain.ToneParams;

/**
 * Interface que describe los casos de uso del servicio de audio
 * AudioUseCase
 */
public interface AudioUseCase {

    byte[] getAudio(AudioRequest request);

    void cleanAudioByName(String text, ToneParams params);
}
