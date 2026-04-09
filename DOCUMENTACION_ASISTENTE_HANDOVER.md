# Documentación del Proyecto y Contexto para Asistentes de IA

Este documento sirve como resumen del estado actual del proyecto `ProyectoBackendJava` (Document API) y contiene las pautas técnicas y de comportamiento esenciales que cualquier Asistente de IA debe seguir al trabajar en este código.

---

## 1. Descripción del Proyecto

El proyecto es una **API REST desarrollada en Spring Boot 3 con Java 21**. Su objetivo principal es la generación de documentos legales y técnicos en formato PDF para el sector fotovoltaico y de puntos de recarga de vehículos eléctricos.

**Stack Técnico:**
- **Framework Base:** Spring Boot, Spring Web.
- **Motor de Plantillas:** Thymeleaf.
- **Renderizado PDF:** Playwright (headless browser).
- **Base de Datos:** PostgreSQL remoto.
- **Gestión de JSON:** Utilidades para parsear JSON almacenado en BD y mapearlos al modelo `Form`.

### 1.1 Funcionalidad Principal
El flujo del sistema se basa en un `DocumentController` que recibe un ID, consulta a la base de datos para obtener un objeto de datos (JSON) asociado a ese ID, lo inyecta en una plantilla HTML procesada por Thymeleaf, y finalmente utiliza Playwright para convertir ese HTML en un documento PDF.

### 1.2 Archivos Clave
- `src/main/java/com/proyecto/document_api/controller/DocumentController.java`: Centraliza todos los endpoints. Gestiona las inyecciones de datos y recursos (imágenes en Base64).
- `src/main/java/com/proyecto/document_api/utils/JsonUtils.java`: Clase utilitaria fundamental. Incluye métodos para procesar JSONs y cargar imágenes estáticas desde `src/main/resources` como cadenas Base64 (`getResourceAsBase64`).
- `src/main/resources/templates/*.html`: Colección de plantillas PDF. Incluye Certificados de Solidez, Declaraciones Responsables y Memorias Técnicas (FT y Puntos de Recarga).

---

## 2. Pautas Técnicas y "Gotchas" (Handover para IA)

Para evitar errores críticos recurrentes que ya han sido solucionados, el asistente de IA **debe cumplir obligatoriamente** las siguientes normas técnicas:

### 2.1. Protección contra Nulos en Thymeleaf/SpEL
En la base de datos abundan los campos opcionales que llegan a Thymeleaf como nulos. Intentar evaluar un campo nulo como booleano en SpEL provoca el error fatal: `cannot convert from null to boolean`.
- **Regla Estricta:** Todas las condiciones booleanas o mapeos de checkboxes en Thymeleaf (`th:text`, `th:if`, etc.) que impliquen evaluar un mapa (`form['variable']`) **deben** validarse explícitamente:
  - ❌ Incorrecto: `th:if="${form['booleano']}"`
  - ✅ Correcto: `th:if="${form['booleano'] == true}"`
  - ✅ Correcto (Ternario con checkbox): `th:text="${form['booleano'] == true ? 'X' : ''}"`

### 2.2. Manejo de Imágenes y Firmas
No se pueden utilizar paths estáticos de tipo `src="/imagen.png"` porque Playwright generará el PDF sin capturarlas o habrá bloqueos por CORS/rutas locales. 
- **Regla Estricta:** Toda imagen (logos, firmas, esquemas) se inyecta desde el controlador (`DocumentController`) como cadena base64 pura concatenando su prefijo data-uri (`data:image/jpeg;base64,...`).
- En el HTML no se debe usar base64 harcodeado si este excede los límites (error de strings largos en SpEL), sino siempre pasarlo como variable de contexto desde Java:
  - ✅ Correcto: `<img th:src="${esquemaUnifilarBase64}" />`

### 2.3. Integridad de Textos Legales ("Copia Literal")
Las plantillas representan documentos oficiales que se entregan a Industria.
- **Regla Estricta:** Prohibido rotundamente utilizar resúmenes o escribir puntos suspensivos ("...") en textos legales, apartados de normativas, párrafos estructurales largos o declaraciones de responsabilidad. El documento siempre debe renderizar la **Copia Literal** exigida.
- Para evitar saltos violentos o cortes de texto en medio de secciones en los PDF largos, hacer uso prudente de `page-break-inside: avoid` o `page-break-before: always` en la maquetación CSS de los HTML.

### 2.4. Mapeo Correcto de Variables usando Logs del Servidor ("Chivatos")
Es habitual que las plantillas HTML inicialmente asuman nombres lógicos, pero que la Base de Datos devuelva claves distintas. Para solucionar esto, la IA debe seguir estrictamente este **flujo de trabajo colaborativo**:

1. La IA propondrá la estructura de la plantilla HTML basándose lógicamente en los requerimientos.
2. El **USUARIO** procederá a ejecutar el endpoint en Swagger en su entorno local.
3. La ejecución provocará que el controlador imprima por consola los "chivatos" JSON (sección "DATOS RECUPERADOS").
4. El **USUARIO** copiará y pegará en el chat estos logs.
5. **Acción de la IA:** Una vez reciba los logs, la IA **DEBE** tomar esos registros como la única fuente absoluta de verdad y corregir automáticamente el mapeo de todas las variables en la plantilla (Ej. Cambiar de `numeroExpediente` a `expedienteEco`, o de `domicilio` a `direccionCompleta`).

- **Bajo ninguna circunstancia** la IA debe asumir que existen variables mágicas o mantenerlas sin arreglar; los chivatos proporcionados por el usuario tras probar con Swagger son la ley para el mapeo SpEL definitivo.

---

## 3. Pautas de Comportamiento Global del Asistente

El usuario tiene un sistema de comportamiento predefinido que gobierna todas las respuestas. El asistente debe adherirse a estas **10 Reglas de Oro**:

1. **Comprensión antes de respuesta:** Analizar completamente el contexto y si algo es incierto, preguntar antes de hacer una asunción.
2. **Razonamiento estructurado:** Desglosar, pensar internamente por pasos y luego presentar la solución.
3. **Modo investigador:** Sopesar varias alternativas. No quedarse solo con la primera idea.
4. **Precisión y claridad:** Cero código "placeholder" o genérico, cada bloque modificado debe ser concreto.
5. **Manejo de incertidumbre:** Admitir de frente si una herramienta genera outputs que no se pueden evaluar o si hay datos incompletos.
6. **Adaptación de rol:** Si la consulta es técnica, responder muy técnico.
7. **Uso de restricciones:** Apegarse escrupulosamente al formato exigido.
8. **Validación interna:** Solo responder tras comprobar iterativamente el código escrito.
9. **Iteración activa:** Atender siempre las correciones.
10. **Idioma:** Comunicar SIEMPRE en español. No usar jerga mezclada, explicar claro. **Nunca asumir.**
