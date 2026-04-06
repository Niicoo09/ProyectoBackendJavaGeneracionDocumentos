package com.proyecto.document_api.controller;

import com.proyecto.document_api.model.DocumentEntity;
import com.proyecto.document_api.repository.DocumentRepository;
import com.proyecto.document_api.service.DocumentService;
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
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepository documentRepository;

    /**
     * Endpoint de PRUEBA: Genera un PDF rápido sin consultar la base de datos.
     * Es útil para verificar que el diseño HTML y los colores funcionan bien.
     * 
     * URL: http://localhost:8080/api/v1/documents/test-pdf
     */
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
    @GetMapping("/generate/{id}")
    public ResponseEntity<byte[]> generatePdfFromDb(@PathVariable UUID id) {
        
        // 1. Consultar la base de datos de producción
        // Si el cliente con ese UUID no existe, lanzamos un error claro
        DocumentEntity doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("¡Error! No existe ningún cliente con el ID: " + id));

        // 2. Extraer y organizar los datos reales del cliente
        Map<String, Object> data = new HashMap<>();
        data.put("title", "Documento Oficial de Generación");
        data.put("name", doc.getNombre());        // Nombre del cliente
        data.put("description", doc.getFormulario()); // Contenido JSON completo (por ahora sin parsear)

        // 3. Crear el documento usando la lógica del PASO 2 y 3 del Servicio
        byte[] pdfBytes = documentService.generatePdf("example", data);

        // 4. Configurar la descarga del archivo con un nombre único basado en el ID
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("documento_oficial_" + id + ".pdf").build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    /**
     * Endpoint de CONSULTA: Muestra la lista de todos los registros que hay en Coolify.
     * Sirve para ver qué IDs tenemos disponibles y verificar la conexión.
     */
    @GetMapping
    public List<DocumentEntity> getAllDocuments() {
        // Simplemente le pedimos al repositorio que nos traiga la lista completa
        return documentRepository.findAll();
    }
}
