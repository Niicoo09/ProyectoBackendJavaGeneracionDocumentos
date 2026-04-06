# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM mcr.microsoft.com/playwright/java:v1.42.0-jammy
WORKDIR /app

# Instalar Java 21 en la imagen de Playwright (que viene con Java 17 por defecto)
# O bien, usar directamente la imagen de Playwright Java que ya trae lo necesario.
# La imagen mcr.microsoft.com/playwright/java:v1.42.0-jammy ya trae Java y Playwright preinstalado.

COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto 8080
EXPOSE 8080

# Definir variables de entorno para DB (Coolify las inyectará)
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/documentos_db
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=password

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
