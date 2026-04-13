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
 * Controlador para documentos de la categoría "Justificaciones".
 */
@RestController
@RequestMapping("/api/v1/justificaciones")
@Tag(name = "Justificaciones", description = "Endpoints para documentos de justificación de subvención")
public class JustificacionesController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentConfigService documentConfigService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * JUSTIFICACIÓN 1: Cartel Publicitario L3.
     * URL: http://localhost:8080/api/v1/justificaciones/cartel-l3/{uuid}
     */
    @Operation(summary = "Cartel Publicitario L3", description = "Genera el cartel L3 (formato 210x210mm) para justificación.")
    @GetMapping("/cartel-l3/{id}")
    public ResponseEntity<byte[]> generateCartelL3(@PathVariable UUID id) {
        Map<String, String> extraImages = new HashMap<>();
        // Fondo pre-formateado como estilo para evitar límite SpEL
        String base64 = jsonUtils.getResourceAsBase64("static/images/justificaciones/cartel-l3.png");
        extraImages.put("fondoStyle", "background-image: url(data:image/png;base64," + base64 + ");");
        
        return processDocumentResponse(id, "justificaciones/CartelL3", "Cartel_L3", extraImages);
    }

    @GetMapping("/cartel-l4/{id}")
    public ResponseEntity<byte[]> generateCartelL4(@PathVariable UUID id) {
        Map<String, String> extraImages = new HashMap<>();
        // Fondo pre-formateado
        String base64 = jsonUtils.getResourceAsBase64("static/images/justificaciones/cartel-l4.png");
        extraImages.put("fondoStyle", "background-image: url(data:image/png;base64," + base64 + ");");
        
        return processDocumentResponse(id, "justificaciones/CartelL4", "Cartel_L4", extraImages);
    }

    @GetMapping("/declaracion-compromiso-corriente/{id}")
    public ResponseEntity<byte[]> generateDeclaracionCompromisoCorriente(@PathVariable UUID id) {
        Map<String, String> extraImages = new HashMap<>();
        // Fondo pre-formateado
        String base64 = jsonUtils.getResourceAsBase64("static/images/justificaciones/declaracion-compromiso-corriente.jpg");
        extraImages.put("fondoStyle", "background-image: url(data:image/jpeg;base64," + base64 + ");");
        
        // Firma del cliente con fallback
        documentRepository.findById(id).ifPresent(doc -> {
            Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
            mapDynamicImageWithFallback(extraImages, formData, "firma", "firma", "firmaImagen", "firmaCliente");
        });
        
        return processDocumentResponse(id, "justificaciones/DeclaracionCompromisoCorriente", "Compromiso_Al_Corriente", extraImages);
    }

    @GetMapping("/justificacion-pago-subvencion-l3/{id}")
    public ResponseEntity<byte[]> generateJustificacionPagoSubvencionL3(@PathVariable UUID id) {
        Map<String, String> extraImages = new HashMap<>();
        // Dos páginas de fondo pre-formateadas
        String b1 = jsonUtils.getResourceAsBase64("static/images/justificaciones/formato-pago-justificacion-linea3-1.jpg");
        String b2 = jsonUtils.getResourceAsBase64("static/images/justificaciones/formato-pago-justificacion-linea3-2.jpg");
        extraImages.put("fondoStyle1", "background-image: url(data:image/jpeg;base64," + b1 + ");");
        extraImages.put("fondoStyle2", "background-image: url(data:image/jpeg;base64," + b2 + ");");
        
        // Firma del cliente con fallback
        documentRepository.findById(id).ifPresent(doc -> {
            Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
            mapDynamicImageWithFallback(extraImages, formData, "firma", "firma", "firmaImagen", "firmaCliente");
        });
        
        return processDocumentResponse(id, "justificaciones/JustificacionPagoSubvencionL3", "Justificacion_Pago_L3", extraImages);
    }

    @GetMapping("/justificacion-pago-subvencion-l4/{id}")
    public ResponseEntity<byte[]> generateJustificacionPagoSubvencionL4(@PathVariable UUID id) {
        Map<String, String> extraImages = new HashMap<>();
        // Dos páginas de fondo pre-formateadas
        String b1 = jsonUtils.getResourceAsBase64("static/images/justificaciones/formato-pago-justificacion-linea4-1.jpg");
        String b2 = jsonUtils.getResourceAsBase64("static/images/justificaciones/formato-pago-justificacion-linea4-2.jpg");
        extraImages.put("fondoStyle1", "background-image: url(data:image/jpeg;base64," + b1 + ");");
        extraImages.put("fondoStyle2", "background-image: url(data:image/jpeg;base64," + b2 + ");");
        
        // Firma del cliente con fallback
        documentRepository.findById(id).ifPresent(doc -> {
            Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
            mapDynamicImageWithFallback(extraImages, formData, "firma", "firma", "firmaImagen", "firmaCliente");
        });
        
        return processDocumentResponse(id, "justificaciones/JustificacionPagoSubvencionL4", "Justificacion_Pago_L4", extraImages);
    }

    @GetMapping("/memoria-economica/{id}")
    public ResponseEntity<byte[]> generateMemoriaEconomica(@PathVariable UUID id) {
        Map<String, String> extraImages = new HashMap<>();
        // Logotipos
        extraImages.put("logoOrganizaciones", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/logos/iconos-organizaciones.png"));
        extraImages.put("logoJunta",           "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/logos/icono-junta-andalucia.png"));
        
        // Firma del cliente (del JSON) con fallback
        documentRepository.findById(id).ifPresent(doc -> {
            Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
            mapDynamicImageWithFallback(extraImages, formData, "firmaImagen", "firma", "firmaImagen", "firmaCliente");
        });
        
        return processDocumentResponse(id, "justificaciones/MemoriaEconomica", "Memoria_Economica", extraImages);
    }

    @GetMapping("/memoria-fv-aer/{id}")
    public ResponseEntity<byte[]> generateMemoriaFvAer(@PathVariable UUID id) {
        Map<String, String> extraImages = new HashMap<>();
        // Logotipos
        extraImages.put("logoOrganizaciones", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/logos/iconos-organizaciones.png"));
        extraImages.put("logoJunta",           "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/logos/icono-junta-andalucia.png"));
        
        // Firma del cliente (del JSON) con fallback
        documentRepository.findById(id).ifPresent(doc -> {
            Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
            mapDynamicImageWithFallback(extraImages, formData, "firmaImagen", "firma", "firmaImagen", "firmaCliente");
        });
        
        return processDocumentResponse(id, "justificaciones/MemoriaFvAer", "Memoria_FV_AER", extraImages);
    }

    @GetMapping("/obra-massol/{id}")
    public ResponseEntity<byte[]> generateObraMassol(@PathVariable UUID id) {
        Map<String, String> extraImages = new HashMap<>();
        // Logos y Firma Solay
        extraImages.put("logoSolay",  "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/logo-solay.png"));
        extraImages.put("firmaSolay", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/firma-solay.png"));
        
        // Firma del cliente (del JSON) con fallback
        documentRepository.findById(id).ifPresent(doc -> {
            Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
            mapDynamicImageWithFallback(extraImages, formData, "firma", "firma", "firmaImagen", "firmaCliente");
        });
        
        return processDocumentResponse(id, "justificaciones/ObraMassol", "Declaracion_Inicio_Obra", extraImages);
    }

    // =========================================================================
    // LÓGICA INTERNA COMÚN
    // =========================================================================

    private void loadSignature(UUID id, Map<String, String> extraImages) {
        documentRepository.findById(id).ifPresent(doc -> {
            Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
            mapDynamicImage(extraImages, formData, "firma", "firma");
            mapDynamicImage(extraImages, formData, "firmaImagen", "firma");
            mapDynamicImage(extraImages, formData, "firmaBase64", "firma");
        });
    }

    private ResponseEntity<byte[]> processDocumentResponse(UUID id, String templateName, String filePrefix, Map<String, String> extraImages) {
        DocumentEntity doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("¡Error! No existe ningún cliente con el ID: " + id));

        Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
        Map<String, Object> enrichedFormData = documentConfigService.enrich(templateName, formData);

        Map<String, Object> data = new HashMap<>();
        data.put("form", enrichedFormData);
        data.put("name", doc.getNombre() != null ? doc.getNombre() : "Cliente");
        
        if (extraImages != null) {
            data.putAll(extraImages);
        }

        // Imágenes corporativas estándar (Solay)
        data.put("logoBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/logo-solay.png"));
        data.put("firmaBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/firma-solay.png"));

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
                base64 = "data:image/png;base64," + base64; // Fallback a png si falta prefijo
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

    private void appendIfNotEmpty(StringBuilder sb, String value) {
        if (value != null && !value.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(value);
        }
    }

    private double parseDouble(String value) {
        if (value == null || value.isEmpty()) return 0.0;
        try {
            // Limpiamos posibles símbolos de moneda o espacios
            String clean = value.replace("€", "").replace(" ", "").replace(",", ".");
            return Double.parseDouble(clean);
        } catch (Exception e) {
            return 0.0;
        }
    }
}
