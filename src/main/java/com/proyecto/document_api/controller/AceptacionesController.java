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
import java.util.function.Function;

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
    @Operation(summary = "Aceptación de Subvención")
    @GetMapping("/aceptacion-subvencion/{id}")
    public ResponseEntity<byte[]> generateAceptacionSubvencion(@PathVariable UUID id) {
        return processDocumentResponse(id, "aceptaciones/AceptacionSubvencion", "1.-Aceptacion_de_la_Subvencion_Concedida", "aceptacion-subvencion", formData -> {
            Map<String, String> extraImages = new HashMap<>();
            String base64 = jsonUtils.getResourceAsBase64("static/images/aceptaciones/aceptacion-subvencion.jpg");
            extraImages.put("fondoStyle", "background-image: url(data:image/jpeg;base64," + base64 + ");");
            mapDynamicImageWithFallback(extraImages, formData, "firmaBase64", "firma", "firmaImagen", "firmaCliente");
            return extraImages;
        });
    }

    @Operation(summary = "Cesión de Tratamiento de Datos")
    @GetMapping("/declaracion-cesion-tratamiento/{id}")
    public ResponseEntity<byte[]> generateDeclaracionCesionTratamiento(@PathVariable UUID id) {
        return processDocumentResponse(id, "aceptaciones/DeclaracionCesionTratamiento", "2.-ANEXO_A-Declaracion_de_Cesion_y_Tratamiento_de_Datos", "declaracion-cesion-tratamiento", formData -> {
            Map<String, String> extraImages = new HashMap<>();
            String b1 = jsonUtils.getResourceAsBase64("static/images/aceptaciones/declaracio-cesion-tratamiento-1.jpg");
            String b2 = jsonUtils.getResourceAsBase64("static/images/aceptaciones/declaracio-cesion-tratamiento-2.jpg");
            extraImages.put("fondoStyle1", "background-image: url(data:image/jpeg;base64," + b1 + ");");
            extraImages.put("fondoStyle2", "background-image: url(data:image/jpeg;base64," + b2 + ");");
            mapDynamicImageWithFallback(extraImages, formData, "firmaBase64", "firma", "firmaImagen", "firmaCliente");
            return extraImages;
        });
    }

    @Operation(summary = "Compromiso de Derechos")
    @GetMapping("/declaracion-compromiso-derechos/{id}")
    public ResponseEntity<byte[]> generateDeclaracionCompromisoDerechos(@PathVariable UUID id) {
        return processDocumentResponse(id, "aceptaciones/DeclaracionCompromisoDerechos", "3.-ANEXO_B-Declaracion_de_Compromiso_y_Cumplimiento_de_Principios_Transversales", "declaracion-compromiso-derechos", formData -> {
            Map<String, String> extraImages = new HashMap<>();
            String base64 = jsonUtils.getResourceAsBase64("static/images/aceptaciones/declaración-de-compromiso.jpg");
            extraImages.put("fondoStyle", "background-image: url(data:image/jpeg;base64," + base64 + ");");
            mapDynamicImageWithFallback(extraImages, formData, "firmaBase64", "firma", "firmaImagen", "firmaCliente");
            return extraImages;
        });
    }

    @Operation(summary = "Compromiso Principios Transversales")
    @GetMapping("/declaracion-compromiso-transversales/{id}")
    public ResponseEntity<byte[]> generateDeclaracionCompromisoTransversales(@PathVariable UUID id) {
        return processDocumentResponse(id, "aceptaciones/DeclaracionCompromisoTransversales", "4.-ANEXO_C-Declaracion_de_Compromiso_y_Cumplimiento_de_Principios_Transversales_(DNSH)", "declaracion-compromiso-transversales", formData -> {
            Map<String, String> extraImages = new HashMap<>();
            String base64 = jsonUtils.getResourceAsBase64("static/images/aceptaciones/compromiso-cumplimiento-principios-transv.jpg");
            extraImages.put("fondoStyle", "background-image: url(data:image/jpeg;base64," + base64 + ");");
            mapDynamicImageWithFallback(extraImages, formData, "firmaBase64", "firma", "firmaImagen", "firmaCliente");
            return extraImages;
        });
    }

    @Operation(summary = "DACI")
    @GetMapping("/declaracion-ausencia-conflicto/{id}")
    public ResponseEntity<byte[]> generateDeclaracionAusenciaConflicto(@PathVariable UUID id) {
        return processDocumentResponse(id, "aceptaciones/DeclaracionAusenciaConflicto", "5.-Declaracion_de_Ausencia_de_Conflicto_de_Intereses_(DACI)", "declaracion-ausencia-conflicto", formData -> {
            Map<String, String> extraImages = new HashMap<>();
            String b1 = jsonUtils.getResourceAsBase64("static/images/aceptaciones/DACI1.jpg");
            String b2 = jsonUtils.getResourceAsBase64("static/images/aceptaciones/DACI2.jpg");
            extraImages.put("fondoStyle1", "background-image: url(data:image/jpeg;base64," + b1 + ");");
            extraImages.put("fondoStyle2", "background-image: url(data:image/jpeg;base64," + b2 + ");");
            mapDynamicImageWithFallback(extraImages, formData, "firmaBase64", "firma", "firmaImagen", "firmaCliente");
            return extraImages;
        });
    }

    // =========================================================================
    // LÓGICA INTERNA COMÚN
    // =========================================================================

    private ResponseEntity<byte[]> processDocumentResponse(UUID id, String templateName, String filePrefix, String configId, Function<Map<String, Object>, Map<String, String>> extraImagesProvider) {
        DocumentEntity doc = documentRepository.findById(id).orElseThrow(() -> new RuntimeException("No existe el ID: " + id));
        Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
        
        Map<String, String> extraImages = (extraImagesProvider != null) ? extraImagesProvider.apply(formData) : null;
        Map<String, Object> enrichedFormData = documentConfigService.enrich(configId != null ? configId : templateName, formData);

        Map<String, Object> data = new HashMap<>();
        data.put("form", enrichedFormData);
        data.put("name", doc.getNombre() != null ? doc.getNombre() : "Cliente");
        data.put("logoBase64",  "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/logo-solay.png"));
        data.put("firmaBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/firma-solay.png"));

        if (extraImages != null) data.putAll(extraImages);

        byte[] pdfBytes = documentService.generatePdf(templateName, data);
        String fileName = filePrefix + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
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
