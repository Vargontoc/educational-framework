package es.vargontoc.educational.framework.audio.domain.enums;

import es.vargontoc.educational.framework.audio.domain.ToneParams;

/**
 * Enumerado de distintos presets para tonos de voz
 * TonePreset
 */
public enum TonePreset {
    
    CALM(0.45, 0.5, 0.8),
    ADVENTURE(0.85, 0.4, 0.9),
    INTENSE(0.3, 0.6, 0.6),
    NEUTRAL(0.5, 0.5, 0.8),
    CUSTOM(null, null, null);

    private final Double exaggeration;
    private final Double cfgWeight;
    private final Double temperature;
    
    TonePreset(Double exaggeration, Double cfgWeight, Double temperature) {
        this.exaggeration = exaggeration;
        this.cfgWeight = cfgWeight;
        this.temperature = temperature;
    }

    public ToneParams toParams() {
        if(this == CUSTOM)
            throw new IllegalStateException("CUSTOM no tiene parametros fijos, usa los del comando");
        return  new ToneParams(exaggeration, cfgWeight, temperature);
    }

    
}
