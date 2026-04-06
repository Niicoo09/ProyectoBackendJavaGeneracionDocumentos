# Document API - Generador de Documentos PDF

Este proyecto es una API REST construida con **Spring Boot 3.x** diseñada para generar documentos PDF profesionales a partir de plantillas HTML y CSS dinámicas. Utiliza **Playwright** para garantizar un renderizado idéntico al de un navegador moderno ("Efecto Ctrl+P").

## 🛠️ Stack Tecnológico

- **Lenguaje:** Java 21
- **Framework:** Spring Boot 3.4.4
- **Persistencia:** Spring Data JPA + PostgreSQL
- **Motor de Plantillas:** Thymeleaf (para inyección de datos en HTML)
- **Motor de PDF:** Playwright Java (Chromium Headless)
- **Herramientas:** Maven, Lombok, Docker

## 📁 Estructura del Proyecto

```text
src/main/java/com/proyecto/document_api/
├── controller/      # Endpoints REST
├── model/           # Entidades JPA (PostgreSQL)
├── repository/      # Interfaces de acceso a datos 
└── service/         # Lógica de negocio (Generación PDF)
src/main/resources/
├── templates/       # Plantillas HTML/CSS (Thymeleaf)
└── application.yml  # Configuración del entorno
```

## 🚀 Guía de Inicio Rápido

### Prerrequisitos
- JDK 21
- Maven 3.9+
- PostgreSQL en ejecución

### Configuración
Ajusta las credenciales de tu base de datos en `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/documentos_db
    username: tu_usuario
    password: tu_password
```

### Ejecución Local
```bash
./mvnw spring-boot:run
```

## 📡 Referencia de la API

### 1. Generar PDF de Prueba
**GET** `/api/v1/documents/test-pdf`

Genera un documento PDF dinámico basado en parámetros de consulta.

**Parámetros:**
- `title` (opcional): Título del documento.
- `name` (opcional): Nombre a mostrar en el saludo.
- `description` (opcional): Texto descriptivo.

**Ejemplo:**
`http://localhost:8080/api/v1/documents/test-pdf?name=Nico&title=Factura%20001`

### 2. Guardar Datos de Documento
**POST** `/api/v1/documents`

Guarda un registro de documento en la base de datos PostgreSQL.

**Body (JSON):**
```json
{
  "name": "Reporte Mensual",
  "description": "Datos de ventas de Marzo"
}
```

### 3. Listar Documentos
**GET** `/api/v1/documents`

Retorna todos los registros de documentos guardados en la BD.

## 🐳 Despliegue en Coolify

El proyecto incluye un `Dockerfile` optimizado para entornos de producción.

1. **Repisitorio Git:** Sube el código a GitHub o GitLab.
2. **Coolify:** Crea un nuevo recurso "Public Repository".
3. **Configuración:** Coolify detectará el `Dockerfile` automáticamente.
4. **Variables de Entorno:** Asegúrate de configurar en Coolify las variables:
   - `SPRING_DATASOURCE_URL` (Ej: `jdbc:postgresql://db:5432/documentos_db`)
   - `SPRING_DATASOURCE_USERNAME`
   - `SPRING_DATASOURCE_PASSWORD`

> [!NOTE]
> La imagen Docker utiliza `mcr.microsoft.com/playwright/java`, la cual ya contiene todas las dependencias necesarias para ejecutar Chromium en Linux sin errores.
