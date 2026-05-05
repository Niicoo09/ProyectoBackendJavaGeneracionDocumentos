# STAGE 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# STAGE 2: Runtime
# Usamos la imagen oficial de Playwright para Java porque ya trae
# todos los navegadores y librerías de sistema necesarias.
FROM mcr.microsoft.com/playwright/java:v1.42.0-jammy
WORKDIR /app

# Copiamos el JAR generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Variables de entorno por defecto (Coolify las sobreescribirá)
ENV SERVER_PORT=3000

EXPOSE 3000

# Ejecutamos la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
