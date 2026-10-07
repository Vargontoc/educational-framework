package es.vargontoc.educational.framework.audio.application.ports.in;

import es.vargontoc.educational.framework.audio.domain.AudioRequest;
import es.vargontoc.educational.framework.audio.domain.ToneParams;

/**
 * Interface que describe los casos de uso del servicio de audio
 * AudioUseCase
 */
public interface AudioUseCase {

    byte[] getAudio(AudioRequest request);

    /**
     * Pide que el audio quede en cache sin esperarlo: para contenido curado conocido de antemano (arranque,
     * semillas). Va detras de lo que se pide a demanda y no hace nada si ya esta en cache.
     */
    void warm(AudioRequest request);

    void cleanAudioByName(String text, ToneParams params);
}
