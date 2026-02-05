# =============================================================================
# API Franquicias - Spring WebFlux + MongoDB
# Dockerfile multi-stage: build con Gradle (Java 21) y etapa final con JRE 21.
# Etapa 5 - Punto 1 del plan (Docker).
# =============================================================================

# -----------------------------------------------------------------------------
# Etapa 1: Build
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copiar wrapper y configuración de Gradle (para cache de dependencias)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle .

# Dar permisos de ejecución al wrapper (necesario en contexto Linux del contenedor)
RUN chmod +x gradlew

# Descargar dependencias (aprovecha cache de capas si no cambian build.gradle/settings.gradle)
RUN ./gradlew dependencies --no-daemon

# Copiar código fuente y construir JAR
COPY src src
RUN ./gradlew bootJar --no-daemon

# -----------------------------------------------------------------------------
# Etapa 2: Runtime (imagen final con solo JRE)
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jre

WORKDIR /app

# Usuario no root para ejecutar la aplicación (buena práctica de seguridad)
# UID/GID 10000 para evitar conflicto con usuarios ya existentes en la imagen base (p. ej. 1000)
RUN groupadd --gid 10000 appgroup && \
    useradd --uid 10000 --gid appgroup --shell /bin/bash --create-home appuser
USER appuser

# Copiar solo el JAR generado en la etapa de build
COPY --from=build /app/build/libs/*.jar app.jar

# Puerto por defecto de la API
EXPOSE 8080

# La URI de MongoDB se puede sobrescribir con variable de entorno en tiempo de ejecución:
# -e SPRING_MONGODB_URI=mongodb://host:27017/franquicias
ENTRYPOINT ["java", "-jar", "app.jar"]
