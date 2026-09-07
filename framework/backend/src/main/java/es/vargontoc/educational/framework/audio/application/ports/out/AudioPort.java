package es.vargontoc.educational.framework.audio.application.ports.out;

import es.vargontoc.educational.framework.audio.domain.AudioRequest;

/**
 * Interface conexion e integracion con servicio de audio
 * ChatterboxPort
 */
public interface AudioPort {

    /**
     * Comprueba la disponibilidad del servicio
     * @return True - Servicio activo
     */
    boolean isAvailable();

    /**
     * Generacion de audio en servicio
     * @param request Peticion con el texto y parametros de audio
     * @return Los bytes de audio
     */
    byte[] synthesizeAudio(AudioRequest request);
}
