# Solay Fotovoltaica - API de Generación de Documentos

Backend robusto desarrollado en **Spring Boot** para la generación automatizada de certificados técnicos, memorias y documentos administrativos.

## 🚀 Estado Actual del Proyecto
El proyecto se encuentra en una fase avanzada de migración, habiendo consolidado toda la lógica de generación de documentos del ecosistema Vue original e integrado mejoras críticas de seguridad y normalización de datos.

### Funcionalidades Clave
- **Generación Multinúcleo**: Soporte para MTDs (Monofásicas, Trifásicas, Aisladas), Aceptaciones (Cesiones, Compromisos) y Justificaciones de obra.
- **Motor Playwright**: Conversión de precisión de HTML a PDF utilizando Chromium Headless para un renderizado perfecto de tablas y gráficos.
- **Sanitización Global de IDs**: Algoritmo preventivo que garantiza la limpieza de formatos (sin puntos ni guiones) en DNI, NIF y CIF de forma automática.
- **Arquitectura de Mapeo Dinámico**: Servicio centralizado que traduce los datos del formulario original al formato requerido por las plantillas legales.

## 🛠️ Stack Tecnológico
- **Lenguaje**: Java 17+
- **Framework**: Spring Boot 3.x
- **Plantillas**: Thymeleaf 3.0
- **PDF Engine**: Playwright
- **Persistencia**: Spring Data JPA / PostgreSQL

## 📂 Estructura de Documentación
Para facilitar el handover y el desarrollo continuo, hemos habilitado una documentación detallada interna:
- [Estado del Proyecto (Walkthrough)](file:///C:/Users/Solay/.gemini/antigravity/brain/e4ab4eb4-6399-455e-b932-73c96a290e4a/walkthrough.md)
- [Plan de Implementación: Formulario Maestro](file:///C:/Users/Solay/.gemini/antigravity/brain/e4ab4eb4-6399-455e-b932-73c96a290e4a/implementation_plan.md)
- [Seguimiento de Tareas](file:///C:/Users/Solay/.gemini/antigravity/brain/e4ab4eb4-6399-455e-b932-73c96a290e4a/task.md)

## 🗺️ Próximos Pasos (Roadmap)
- [ ] **Persistencia del Formulario Maestro**: Implementación de endpoints CRUD para guardar los ~500 campos dinámicos definidos en `masterFormFields.js`.
- [ ] **Gestión de Binarios**: Lógica de almacenamiento para firmas y fotos de planos.
- [ ] **Dashboard de Control**: Integración total con `ProyectoFrontEndVue`.

---
© 2026 Solay Fotovoltaica - Ingeniería y Automatización.
