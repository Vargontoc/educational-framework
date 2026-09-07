package es.vargontoc.educational.framework.audio.application.ports.in;

import es.vargontoc.educational.framework.audio.domain.AudioRequest;

/**
 * Interface que describe los casos de uso del servicio de audio
 * AudioUseCase
 */
public interface AudioUseCase {

    byte[] getAudio(AudioRequest request);
}
