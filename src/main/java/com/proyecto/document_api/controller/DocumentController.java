package com.proyecto.document_api.controller;

import com.proyecto.document_api.model.DocumentEntity;
import com.proyecto.document_api.repository.DocumentRepository;
import com.proyecto.document_api.service.DocumentService;
import com.proyecto.document_api.utils.JsonUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Punto de entrada de la API.
 * Define las rutas (URLs) que podemos usar en el navegador para ver o descargar PDFs.
 * 
 * @author Nicolas Navarro Contreras
 */
@RestController
@RequestMapping("/api/v1/documents")
@Tag(name = "Documentos", description = "Endpoints para la gestión y generación de archivos PDF")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Endpoint de PRUEBA: Genera un PDF rápido sin consultar la base de datos.
     * Es útil para verificar que el diseño HTML y los colores funcionan bien.
     * 
     * URL: http://localhost:8080/api/v1/documents/test-pdf
     */
    @Operation(summary = "Generar PDF de prueba", description = "Crea un PDF genérico sin consultar la base de datos, útil para pruebas de diseño.")
    @GetMapping("/test-pdf")
    public ResponseEntity<byte[]> generateTestPdf(
            @RequestParam(defaultValue = "Nombre del Documento") String title,
            @RequestParam(defaultValue = "Usuario") String name,
            @RequestParam(defaultValue = "Contenido de Formulario") String description
    ) {
        // Creamos un mapa temporal con datos ficticios
        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("name", name);
        data.put("description", description);

        // Llamamos al servicio para que haga la magia de convertir a PDF
        byte[] pdfBytes = documentService.generatePdf("example", data);

        // Preparamos la respuesta para que el navegador sepa que es un archivo descargable
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("prueba_tecnica.pdf").build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    /**
     * Endpoint REAL: Busca un cliente por su ID en Coolify y genera su PDF oficial.
     * Este es el que usará el Dashboard de la web.
     * 
     * URL: http://localhost:8080/api/v1/documents/generate/{uuid}
     */
    @Operation(summary = "Generar PDF por Cliente", description = "Recupera los datos de un cliente de la tabla generacion_docs y genera su PDF dinámico.")
    @GetMapping("/generate/{id}")
    public ResponseEntity<byte[]> generatePdfFromDb(@PathVariable UUID id) {
        
        // 1. Consultar la base de datos de producción
        DocumentEntity doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("¡Error! No existe ningún cliente con el ID: " + id));

        // 2. Extraer y traducir los datos del JSON a un Mapa
        Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());

        // 3. Preparar los datos para la plantilla
        Map<String, Object> data = new HashMap<>();
        data.put("form", formData);
        data.put("name", doc.getNombre());
        data.put("title", "CERTIFICADO DE ADECUACIÓN");

        // 4. Crear el documento usando la lógica del PASO 2 y 3 del Servicio
        byte[] pdfBytes = documentService.generatePdf("certificado-adecuacion", data);

        // 5. Configurar la descarga del archivo con el NOMBRE de la persona (limpiando espacios)
        String fileName = doc.getNombre().replace(" ", "_") + ".pdf";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    /**
     * Endpoint de CONSULTA: Muestra la lista de todos los registros que hay en Coolify.
     * Sirve para ver qué IDs tenemos disponibles y verificar la conexión.
     */
    @Operation(summary = "Listar Clientes", description = "Devuelve una lista con todos los registros encontrados en la tabla generacion_docs.")
    @GetMapping
    public List<DocumentEntity> getAllDocuments() {
        // Simplemente le pedimos al repositorio que nos traiga la lista completa
        return documentRepository.findAll();
    }
}
