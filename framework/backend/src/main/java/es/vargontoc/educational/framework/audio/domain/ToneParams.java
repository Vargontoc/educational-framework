package es.vargontoc.educational.framework.audio.domain;

public record ToneParams(double exageration, double cfgWeight, double temperature) {
    public ToneParams {
        if(exageration <= .25) exageration = .25;
        if(exageration >= 2.00) exageration = 2.00;

        if(cfgWeight <= 0.0) exageration = 0.0;
        if(cfgWeight >= 1.0) exageration = 1.0;

        if(temperature <= 0.05) temperature = 0.05;
        if(temperature >= 5.0) temperature = 5.0;
    }
}
