# SKILL — contract-validation

## Objetivo
Validar que esquemas, endpoints y DDL compartidos son coherentes y no se duplican.

## Procedimiento
1. Identifica contratos afectados en `docs/contracts`.
2. Verifica sintaxis, version, compatibilidad y referencias cruzadas.
3. Comprueba entradas, salidas, errores, obligatoriedad, nulabilidad y ejemplos.
4. Contrasta implementacion y pruebas con el contrato.
5. Lista consumidores potencialmente afectados.

## Resultado
`VALID`, `VALID_WITH_WARNINGS` o `INVALID`, incluyendo incompatibilidades y handoffs.
