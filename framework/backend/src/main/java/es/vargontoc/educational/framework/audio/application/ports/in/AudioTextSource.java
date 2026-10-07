package es.vargontoc.educational.framework.audio.application.ports.in;

import java.util.List;

import es.vargontoc.educational.framework.audio.domain.AudioRequest;

/**
 * Fuente de textos curados que deben tener audio antes de que un niño los necesite.
 * Cada modulo que posee textos (avatar, juegos...) publica una implementacion; el calentamiento de arranque
 * (AudioWarmupService) las recorre todas en cada inicio, de modo que lo que falte en cache se vuelve a pedir.
 */
public interface AudioTextSource {

    /** Nombre corto para los logs. */
    String name();

    /** Peticiones de audio actuales de esta fuente (se evalua en cada arranque, con los datos vigentes). */
    List<AudioRequest> requests();
}
