package es.vargontoc.educational.framework.family.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import es.vargontoc.educational.framework.family.infrastructure.dto.AdaptativeColor;
import es.vargontoc.educational.framework.family.model.ColorVisionMode;
import es.vargontoc.educational.framework.family.ports.in.ColorAdaptativeUseCase;

@Service
public class AdaptativeColorService implements ColorAdaptativeUseCase {


    @Override
    public String getColorAdaptive(ColorVisionMode vision, String colorValue) {
        
        double[] rgb = hexToLinear(colorValue);
        double[] simulated = vision.simulate(rgb);
        return lineartoHex(simulated);
    }

    @Override
    public boolean isTooSimilar(ColorVisionMode mode, String color, String distractor) {
        
        double[] rgbColor = hexToLinear(color);
        double[] rgbDestractor = hexToLinear(distractor);


        return calculateDelta(mode.adaptation(rgbColor), mode.adaptation(rgbDestractor))  <= 20.0;
    }

    private double calculateDelta(double[] color, double[] distractor) {
        double dL = color[0] - distractor[0];
        double da = color[1] - distractor[1];
        double db = color[2] - distractor[2];

        return Math.sqrt(dL * dL + da * da + db * db);
    }

    /**
     * Convierte valor hexadecimal a RGB
     * @param color Valor hexadecimal a convertir
     * @return RGB resultante conversión
     */
    private double[] hexToLinear(String color) {
        if(color.startsWith("#")) color = color.substring(1);
        int rgb = Integer.parseInt(color,16);
        double[] lin = {
            ((rgb >> 16) & 0xFF) / 255.0,
            ((rgb >> 8) & 0xFF) / 255.0,
            (rgb & 0xFF) / 255.0
        };

        for (int i = 0; i < 3; i++) {
            lin[i] = (lin[i] <= 0.04045) ? lin[i] / 12.92 : Math.pow((lin[i] + 0.055) / 1.055, 2.4);
        }
        return lin;
    }

    

    /**
     * Convierte RGB a cadena hezadecimal
     * @param rgb Valor RGB a convertir
     * @return Valor hexadecimal resultante de la conversión
     */
    private String lineartoHex(double[] rgb) {
        double[] srgb = new double[3];

        // 1. Deshacer la linealización aplicando la corrección Gamma inversa de sRGB
        for (int i = 0; i < 3; i++) {
            if (rgb[i] <= 0.0031308) {
                srgb[i] = rgb[i] * 12.92;
            } else {
                srgb[i] = 1.055 * Math.pow(rgb[i], 1.0 / 2.4) - 0.055;
            }

            // 2. Control de límites (clamping) por seguridad matemática
            if (srgb[i] < 0.0) srgb[i] = 0.0;
            if (srgb[i] > 1.0) srgb[i] = 1.0;
        }

        // 3. Escalar al rango entero 0-255 con redondeo correcto
        int r = (int) Math.round(srgb[0] * 255.0);
        int g = (int) Math.round(srgb[1] * 255.0);
        int b = (int) Math.round(srgb[2] * 255.0);

        // 4. Formatear a String hexadecimal de 6 dígitos con almohadilla (#)
        // %02X asegura dos caracteres en mayúsculas por canal, rellenando con '0' si es necesario
        return String.format("#%02X%02X%02X", r, g, b);
    }

    @Override
    public Map<ColorVisionMode, List<AdaptativeColor>> getVisions() {
        Map<ColorVisionMode, List<AdaptativeColor>> result = new HashMap<>();

        String[] primaryColors = new String[]{
            "#FF0000", "#0000FF","#00FF00","#FFFF00","#FF8000","#FF69B4","#800080"
        };

        for(ColorVisionMode cvm: ColorVisionMode.values())
        {
            result.put(cvm, new ArrayList<>());
            for(String color: primaryColors) 
            {
                result.get(cvm).add(new AdaptativeColor(color, getColorAdaptive(cvm, color)));
            }
        }

        return result;
    }


}
