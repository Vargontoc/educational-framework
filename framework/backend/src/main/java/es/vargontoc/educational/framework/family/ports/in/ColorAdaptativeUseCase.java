package es.vargontoc.educational.framework.family.ports.in;

import es.vargontoc.educational.framework.family.model.ColorVisionMode;

public interface ColorAdaptativeUseCase {
    
    /**
     * Obtener color simulado adaptado a tipo visión
     * @param vision
     * @param colorValue
     * @return
     */
    String getColorAdaptive(ColorVisionMode vision, String colorValue);

    /**
     * Comprueba si dos colores son demasiado similares para el tipo de vision
     * @param mode Tipo visión a comprobar
     * @param color Valor del color de refercencia en hexadecimal
     * @param distractor  Valor del color a comparar en hexadecimal
     * @return True, si son demasiado parecidos
     */
    boolean isTooSimilar(ColorVisionMode mode, String color, String distractor);
}
