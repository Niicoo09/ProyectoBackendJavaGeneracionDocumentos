package com.proyecto.document_api.controller;

import com.proyecto.document_api.model.DocumentEntity;
import com.proyecto.document_api.repository.DocumentRepository;
import com.proyecto.document_api.service.DocumentConfigService;
import com.proyecto.document_api.service.DocumentService;
import com.proyecto.document_api.utils.JsonUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controlador para documentos de la categoría "Aceptaciones".
 */
@RestController
@RequestMapping("/api/v1/aceptaciones")
@Tag(name = "Aceptaciones", description = "Endpoints para documentos de aceptación y firma del cliente")
public class AceptacionesController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentConfigService documentConfigService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * ACEPTACIÓN 1: Aceptación de Subvención.
     * URL: http://localhost:8080/api/v1/aceptaciones/aceptacion-subvencion/{uuid}
     */
    @Operation(summary = "Aceptación de Subvención", description = "Genera el documento de aceptación de subvención con posicionamiento absoluto.")
    @GetMapping("/aceptacion-subvencion/{id}")
    public ResponseEntity<byte[]> generateAceptacionSubvencion(@PathVariable UUID id) {
        Map<String, String> extraImages = new HashMap<>();
        // Fondo pre-formateado
        String base64 = jsonUtils.getResourceAsBase64("static/images/aceptaciones/aceptacion-subvencion.jpg");
        extraImages.put("fondoStyle", "background-image: url(data:image/jpeg;base64," + base64 + ");");
        
        // Firma del cliente con fallback
        documentRepository.findById(id).ifPresent(doc -> {
            Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
            mapDynamicImageWithFallback(extraImages, formData, "firmaBase64", "firma", "firmaImagen", "firmaCliente");
        });
        
        return processDocumentResponse(id, "aceptaciones/AceptacionSubvencion", "Aceptacion_Subvencion", extraImages);
    }

    @GetMapping("/declaracion-cesion-tratamiento/{id}")
    public ResponseEntity<byte[]> generateDeclaracionCesionTratamiento(@PathVariable UUID id) {
        Map<String, String> extraImages = new HashMap<>();
        // Cargamos los fondos pre-formateados
        String b1 = jsonUtils.getResourceAsBase64("static/images/aceptaciones/declaracio-cesion-tratamiento-1.jpg");
        String b2 = jsonUtils.getResourceAsBase64("static/images/aceptaciones/declaracio-cesion-tratamiento-2.jpg");
        extraImages.put("fondoStyle1", "background-image: url(data:image/jpeg;base64," + b1 + ");");
        extraImages.put("fondoStyle2", "background-image: url(data:image/jpeg;base64," + b2 + ");");
        
        // Firma del cliente con fallback
        documentRepository.findById(id).ifPresent(doc -> {
            Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
            mapDynamicImageWithFallback(extraImages, formData, "firmaBase64", "firma", "firmaImagen", "firmaCliente");
        });
        
        return processDocumentResponse(id, "aceptaciones/DeclaracionCesionTratamiento", "AnexoA_Cesion_Tratamiento", extraImages);
    }

    @GetMapping("/declaracion-compromiso-derechos/{id}")
    public ResponseEntity<byte[]> generateDeclaracionCompromisoDerechos(@PathVariable UUID id) {
        Map<String, String> extraImages = new HashMap<>();
        String base64 = jsonUtils.getResourceAsBase64("static/images/aceptaciones/declaración-de-compromiso.jpg");
        extraImages.put("fondoStyle", "background-image: url(data:image/jpeg;base64," + base64 + ");");
        
        // Firma del cliente con fallback
        documentRepository.findById(id).ifPresent(doc -> {
            Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
            mapDynamicImageWithFallback(extraImages, formData, "firmaBase64", "firma", "firmaImagen", "firmaCliente");
        });
        
        return processDocumentResponse(id, "aceptaciones/DeclaracionCompromisoDerechos", "Compromiso_Derechos_Sociales", extraImages);
    }

    @GetMapping("/declaracion-compromiso-transversales/{id}")
    public ResponseEntity<byte[]> generateDeclaracionCompromisoTransversales(@PathVariable UUID id) {
        Map<String, String> extraImages = new HashMap<>();
        String base64 = jsonUtils.getResourceAsBase64("static/images/aceptaciones/compromiso-cumplimiento-principios-transv.jpg");
        extraImages.put("fondoStyle", "background-image: url(data:image/jpeg;base64," + base64 + ");");
        
        // Firma del cliente con fallback
        documentRepository.findById(id).ifPresent(doc -> {
            Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
            mapDynamicImageWithFallback(extraImages, formData, "firmaBase64", "firma", "firmaImagen", "firmaCliente");
        });
        
        return processDocumentResponse(id, "aceptaciones/DeclaracionCompromisoTransversales", "Compromiso_Principios_Transversales", extraImages);
    }

    @GetMapping("/declaracion-ausencia-conflicto/{id}")
    public ResponseEntity<byte[]> generateDeclaracionAusenciaConflicto(@PathVariable UUID id) {
        Map<String, String> extraImages = new HashMap<>();
        String b1 = jsonUtils.getResourceAsBase64("static/images/aceptaciones/DACI1.jpg");
        String b2 = jsonUtils.getResourceAsBase64("static/images/aceptaciones/DACI2.jpg");
        extraImages.put("fondoStyle1", "background-image: url(data:image/jpeg;base64," + b1 + ");");
        extraImages.put("fondoStyle2", "background-image: url(data:image/jpeg;base64," + b2 + ");");
        
        // Firma del cliente con fallback
        documentRepository.findById(id).ifPresent(doc -> {
            Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
            mapDynamicImageWithFallback(extraImages, formData, "firmaBase64", "firma", "firmaImagen", "firmaCliente");
        });
        
        return processDocumentResponse(id, "aceptaciones/DeclaracionAusenciaConflicto", "DACI_Conflicto_Intereses", extraImages);
    }

    // =========================================================================
    // LÓGICA INTERNA COMÚN (Replicada para autonomía del controlador)
    // =========================================================================

    private ResponseEntity<byte[]> processDocumentResponse(UUID id, String templateName, String filePrefix, Map<String, String> extraImages) {
        DocumentEntity doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("¡Error! No existe ningún cliente con el ID: " + id));

        Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
        
        System.out.println("--- DATOS RECUPERADOS (" + templateName + ") ---");
        formData.forEach((k, v) -> System.out.println("CAMPO: [" + k + "] -> VALOR: [" + v + "]"));
        System.out.println("----------------------------------------------");

        Map<String, Object> enrichedFormData = documentConfigService.enrich(templateName, formData);

        System.out.println("--- DATOS ENRIQUECIDOS (" + templateName + ") ---");
        enrichedFormData.forEach((k, v) -> {
            if (!formData.containsKey(k) || !String.valueOf(formData.get(k)).equals(String.valueOf(v))) {
                System.out.println("  [ENRIQ] " + k + " -> " + v);
            }
        });
        System.out.println("----------------------------------------------");

        Map<String, Object> data = new HashMap<>();
        data.put("form", enrichedFormData);
        data.put("name", doc.getNombre() != null ? doc.getNombre() : "Cliente");
        
        data.put("logoBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/logo-solay.png"));
        data.put("firmaBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/firma-solay.png"));

        if (extraImages != null) {
            data.putAll(extraImages);
        }

        byte[] pdfBytes = documentService.generatePdf(templateName, data);

        String safeName = (doc.getNombre() != null) ? doc.getNombre().replace(" ", "_") : "Documento";
        String fileName = filePrefix + "_" + safeName + ".pdf";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    private void mapDynamicImage(Map<String, String> extraImages, Map<String, Object> formData, String jsonKey, String templateKey) {
        Object img = formData.get(jsonKey);
        if (img != null && !img.toString().isEmpty()) {
            String base64 = img.toString();
            if (!base64.startsWith("data:image")) {
                base64 = "data:image/png;base64," + base64;
            }
            extraImages.put(templateKey, base64);
        }
    }

    /**
     * Intenta mapear una imagen dinámica desde varias claves posibles en el JSON.
     */
    private void mapDynamicImageWithFallback(Map<String, String> extraImages, Map<String, Object> formData, String templateKey, String... jsonKeys) {
        for (String key : jsonKeys) {
            Object img = formData.get(key);
            if (img != null && !img.toString().isEmpty()) {
                String base64 = img.toString();
                if (!base64.startsWith("data:image")) {
                    base64 = "data:image/png;base64," + base64;
                }
                extraImages.put(templateKey, base64);
                return;
            }
        }
    }
}
