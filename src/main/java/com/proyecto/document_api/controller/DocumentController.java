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
     * CERTIFICADO 1: Adecuación al Real Decreto 1699/2011.
     */
    @Operation(summary = "Certificado de Adecuación", description = "Genera el certificado de adecuación oficial.")
    @GetMapping("/adecuacion/{id}")
    public ResponseEntity<byte[]> generateAdecuacion(@PathVariable UUID id) {
        return processDocumentResponse(id, "CertificadoAdecuacion", "Certificado_Adecuacion", null);
    }

    /**
     * CERTIFICADO 2: Solidez y Seguridad - Aporticada Teja.
     * URL: http://localhost:8080/api/v1/documents/aporticada-teja/{uuid}
     */
    @Operation(summary = "Certificado Aporticada Teja", description = "Genera el certificado de solidez para cubiertas de teja aporticada.")
    @GetMapping("/aporticada-teja/{id}")
    public ResponseEntity<byte[]> generateAporticadaTeja(@PathVariable UUID id) {
        // Esta plantilla necesita una imagen técnica específica
        Map<String, String> extraImages = new HashMap<>();
        // Pre-concatenamos el prefijo para evitar el límite de SpEL (100k chars)
        extraImages.put("imagenTecnicaBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/teja-aporticada.png"));
        
        return processDocumentResponse(id, "CertificadoAporticadaTeja", "Certificado_Solidez_Teja", extraImages);
    }

    /**
     * CERTIFICADO 3: Solidez y Seguridad - Chapa Grecada Aporticada.
     * URL: http://localhost:8080/api/v1/documents/chapas-grecadas/{uuid}
     */
    @Operation(summary = "Certificado Chapa Grecada Aporticada", description = "Genera el certificado de solidez para cubiertas de chapa grecada.")
    @GetMapping("/chapas-grecadas/{id}")
    public ResponseEntity<byte[]> generateChapasGrecadas(@PathVariable UUID id) {
        Map<String, String> extraImages = new HashMap<>();
        // Usamos la imagen técnica correspondiente ya concatenada
        extraImages.put("imagenTecnicaBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/cubierta-plana-aporticada.png"));
        
        return processDocumentResponse(id, "CertificadoChapasGrecadas", "Certificado_Solidez_Grecada", extraImages);
    }

    // =========================================================================
    // LÓGICA INTERNA COMÚN
    // =========================================================================

    private ResponseEntity<byte[]> processDocumentResponse(UUID id, String templateName, String filePrefix, Map<String, String> extraImages) {
        // 1. Consultar la base de datos
        DocumentEntity doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("¡Error! No existe ningún cliente con el ID: " + id));

        // 2. Extraer datos del JSON
        Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
        
        System.out.println("--- DATOS RECUPERADOS (" + templateName + ") ---");
        formData.forEach((k, v) -> System.out.println("CAMPO: [" + k + "] -> VALOR: [" + v + "]"));
        System.out.println("----------------------------------------------");

        // 3. Preparar los datos comunes
        Map<String, Object> data = new HashMap<>();
        data.put("form", formData);
        data.put("name", doc.getNombre() != null ? doc.getNombre() : "Cliente");
        
        // Imágenes corporativas estándar CON PREFIJO (para evitar errores de SpEL)
        data.put("logoBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/logo-solay.png"));
        data.put("firmaBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/firma-solay.png"));

        // Añadir imágenes extras si existen
        if (extraImages != null) {
            data.putAll(extraImages);
        }

        // 4. Generar el PDF
        byte[] pdfBytes = documentService.generatePdf(templateName, data);

        // 5. Configurar respuesta (manejo seguro de nombre de archivo)
        String safeName = (doc.getNombre() != null) ? doc.getNombre().replace(" ", "_") : "Documento";
        String fileName = filePrefix + "_" + safeName + ".pdf";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    /**
     * Endpoint de CONSULTA: Muestra la lista de todos los registros.
     */
    @Operation(summary = "Listar Clientes", description = "Devuelve una lista con todos los registros encontrados.")
    @GetMapping
    public List<DocumentEntity> getAllDocuments() {
        return documentRepository.findAll();
    }
}
