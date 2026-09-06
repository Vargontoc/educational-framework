# syntax=docker/dockerfile:1
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml ./
# Cache mount persistente para el repositorio local de Maven: sobrevive a la
# invalidación de capas cuando cambia el código fuente, evitando re-descargar
# dependencias/plugins en cada rebuild (clave para iterar rápido en E2E). Se le
# da un id propio para no compartir bucket con la caché .m2 de otros proyectos
# que usen el mismo builder de BuildKit en esta máquina.
RUN --mount=type=cache,id=educational-framework-backend-m2,target=/root/.m2 mvn dependency:go-offline -B
COPY src ./src
RUN --mount=type=cache,id=educational-framework-backend-m2,target=/root/.m2 mvn package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
