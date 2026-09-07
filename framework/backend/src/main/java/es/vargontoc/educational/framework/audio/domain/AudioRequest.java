package es.vargontoc.educational.framework.audio.domain;

import es.vargontoc.educational.framework.audio.domain.enums.TonePreset;

public record AudioRequest(String text, double exageration, double cfg, double temperature) {
    public static final AudioRequest withParams(String text, ToneParams params){
        return new AudioRequest(text, params.exageration(), params.cfgWeight(), params.temperature());
    }

    public static final AudioRequest withPreset(String text, TonePreset preset){
        var params = preset.toParams();
        return new AudioRequest(text, params.exageration(), params.cfgWeight(), params.temperature());
    }
}
