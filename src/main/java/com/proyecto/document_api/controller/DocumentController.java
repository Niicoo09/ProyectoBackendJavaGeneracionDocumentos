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

    @GetMapping("/test-pdf")
    public ResponseEntity<byte[]> generateTestPdf(
            @RequestParam(defaultValue = "Nombre del Documento") String title,
            @RequestParam(defaultValue = "Usuario") String name,
            @RequestParam(defaultValue = "Contenido de Formulario") String description
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("name", name);
        data.put("description", description);

        byte[] pdfBytes = documentService.generatePdf("example", data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("documento.pdf").build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/generate/{id}")
    public ResponseEntity<byte[]> generatePdfFromDb(@PathVariable UUID id) {
        DocumentEntity doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado con ID: " + id));

        Map<String, Object> data = new HashMap<>();
        data.put("title", "Documento Oficial de Generación");
        data.put("name", doc.getNombre());
        data.put("description", doc.getFormulario());

        byte[] pdfBytes = documentService.generatePdf("example", data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("documento_" + id + ".pdf").build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping
    public List<DocumentEntity> getAllDocuments() {
        return documentRepository.findAll();
    }
}
