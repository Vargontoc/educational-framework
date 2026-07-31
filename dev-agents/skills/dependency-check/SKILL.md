# SKILL — dependency-check

## Objetivo
Detectar dependencias reales entre capas y evitar un orden global rigido.

## Procedimiento
1. Lee el sprint y sus contratos.
2. Identifica entradas necesarias, capa propietaria y consumidores.
3. Clasifica cada dependencia como `available`, `mockable`, `blocked` o `decision_required`.
4. Indica que trabajos pueden ejecutarse en paralelo.
5. Registra handoffs concretos sin disenar el sprint de otra capa.

## Regla
Una capa puede avanzar cuando sus entradas contractuales estan aprobadas, aunque el proveedor aun use mocks, siempre que el sprint lo permita.
