package es.vargontoc.educational.framework.family.model;

public enum ColorVisionMode {
    NONE(new double[][] {
        {1,0,0},
        {0,1,0},
        {0,0,1}
    }),
    PROTANOPIA(new double[][]{
        {0.152286, 1.052583, -0.204868},
        {0.114503, 0.786281, 0.099216},
        {-0.003882, -0.048116, 1.051998}
    }),
    PROTANOMALY(new double[][] {
        {0.385450, 0.769005, -0.154455},
        {0.100526, 0.829802, 0.069673},
        {-0.007442, -0.022190, 1.029632}
    }),
    DEUTERANOMALY(new double[][] {
        {0.367322, 0.860646, -0.227968},
        {0.280085, 0.672501, 0.047413},
        {-0.011820, 0.042940, 0.968881}
    }),
    DEUTERANOPIA(new double[][]{
            {0.367322, 0.860646, -0.227968},
            {0.280085, 0.672501, 0.047413},
            {-0.011820, 0.042940, 0.968881}
    }),
    TRITANOPIA(new double[][] {
        {1.255528, -0.076749, -0.178779},
        {-0.078411, 0.930809, 0.147602},
        {0.004733, 0.691367, 0.303900}
    }),
    TRITANOMALY(new double[][] {

        {1.104996, -0.046633, -0.058363},
        {-0.032137, 0.971635, 0.060503},
        {0.001336, 0.317922, 0.680742}
    }),
    ACHROMATOMALY(null),
    ACHROMATOPSIA(null);

    private final double[][] mutation;

    private static final double XN = 0.95047;
    private static final double YN = 1.00000;
    private static final double ZN = 1.08883;

    ColorVisionMode(double[][] mutation){
        this.mutation = mutation;
    }

    /**
     * Simula el color percibido para el tipo de visión, en RGB lineal.
     * @param rgb Color de entrada en RGB lineal (0-1)
     * @return Color simulado en RGB lineal (0-1)
     */
    public double[] simulate(double[] rgb) {

        double gray = 0.2126729 * rgb[0] + 0.7151522 * rgb[1] + 0.0721750 * rgb[2];
        double[] simulated = new double[3];
        switch (this) {
            case ACHROMATOPSIA -> {
                simulated[0] = gray;
                simulated[1] = gray;
                simulated[2] = gray;
            }
            case ACHROMATOMALY -> {
                simulated[0] = rgb[0] * 0.5 + gray * 0.5;
                simulated[1] = rgb[1] * 0.5 + gray * 0.5;
                simulated[2] = rgb[2] * 0.5 + gray * 0.5;
            }
            default -> {
                for(int i = 0; i < 3; i++) {
                    simulated[i] = mutation[i][0] * rgb[0] + mutation[i][1] * rgb[1] +mutation[i][2] * rgb[2];
                }
            }
        }

        for(int i = 0; i < 3; i++) {
            if(simulated[i] < 0.0) simulated[i] = 0.0;
            if(simulated[i] > 1.0) simulated[i] = 1.0;
        }
        return simulated;
    }

    /**
     * Transforma el color al tipo de visión y lo expresa en espacio Lab,
     * para poder comparar distancias perceptuales (ver {@link #simulate(double[])} para RGB).
     * @param rgb Color que se va a transformar, en RGB lineal (0-1)
     * @return Color adaptado al tipo de visión, en espacio Lab
     */
    public double[] adaptation(double[] rgb) {
        return rgbToLab(simulate(rgb));
    }



    private double[] rgbToLab(double[] rgb) {
        double x = 0.4124564 * rgb[0] + 0.3575761 * rgb[1] + 0.1804375 * rgb[2];
        double y = 0.2126729 * rgb[0] + 0.7151522 * rgb[1] + 0.0721750 * rgb[2];
        double z = 0.0193339 * rgb[0] + 0.1191920 * rgb[1] + 0.9503041 * rgb[2];

        double xr = fCie(x / XN);
        double yr = fCie(y / YN);
        double zr = fCie(z / ZN);

        return new double[]{
            (116.0 * yr) - 16.0,    // L*
            500.0 * (xr - yr),       // a*
            200.0 * (yr - zr)        // b*
        };
    }


    private double fCie(double t) {
         return (t > 0.008856) ? Math.pow(t, 1.0 / 3.0) : (7.787 * t) + (16.0 / 116.0);
    }

}
