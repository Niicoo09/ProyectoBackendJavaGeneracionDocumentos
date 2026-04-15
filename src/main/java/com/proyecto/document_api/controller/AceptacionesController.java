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
    @Operation(summary = "Aceptación de Subvención", description = "Genera el documento de aceptación de subvención con posicionamiento absoluto.")
    @GetMapping("/aceptacion-subvencion/{id}")
    public ResponseEntity<byte[]> generateAceptacionSubvencion(@PathVariable UUID id) {
        return processDocumentResponse(id, "aceptaciones/AceptacionSubvencion", "Aceptacion_Subvencion", formData -> {
            Map<String, String> extraImages = new HashMap<>();
            String base64 = jsonUtils.getResourceAsBase64("static/images/aceptaciones/aceptacion-subvencion.jpg");
            extraImages.put("fondoStyle", "background-image: url(data:image/jpeg;base64," + base64 + ");");
            mapDynamicImageWithFallback(extraImages, formData, "firmaBase64", "firma", "firmaImagen", "firmaCliente");
            return extraImages;
        });
    }

    @GetMapping("/declaracion-cesion-tratamiento/{id}")
    public ResponseEntity<byte[]> generateDeclaracionCesionTratamiento(@PathVariable UUID id) {
        return processDocumentResponse(id, "aceptaciones/DeclaracionCesionTratamiento", "AnexoA_Cesion_Tratamiento", formData -> {
            Map<String, String> extraImages = new HashMap<>();
            String b1 = jsonUtils.getResourceAsBase64("static/images/aceptaciones/declaracio-cesion-tratamiento-1.jpg");
            String b2 = jsonUtils.getResourceAsBase64("static/images/aceptaciones/declaracio-cesion-tratamiento-2.jpg");
            extraImages.put("fondoStyle1", "background-image: url(data:image/jpeg;base64," + b1 + ");");
            extraImages.put("fondoStyle2", "background-image: url(data:image/jpeg;base64," + b2 + ");");
            mapDynamicImageWithFallback(extraImages, formData, "firmaBase64", "firma", "firmaImagen", "firmaCliente");
            return extraImages;
        });
    }

    @GetMapping("/declaracion-compromiso-derechos/{id}")
    public ResponseEntity<byte[]> generateDeclaracionCompromisoDerechos(@PathVariable UUID id) {
        return processDocumentResponse(id, "aceptaciones/DeclaracionCompromisoDerechos", "Compromiso_Derechos_Sociales", formData -> {
            Map<String, String> extraImages = new HashMap<>();
            String base64 = jsonUtils.getResourceAsBase64("static/images/aceptaciones/declaración-de-compromiso.jpg");
            extraImages.put("fondoStyle", "background-image: url(data:image/jpeg;base64," + base64 + ");");
            mapDynamicImageWithFallback(extraImages, formData, "firmaBase64", "firma", "firmaImagen", "firmaCliente");
            return extraImages;
        });
    }

    @GetMapping("/declaracion-compromiso-transversales/{id}")
    public ResponseEntity<byte[]> generateDeclaracionCompromisoTransversales(@PathVariable UUID id) {
        return processDocumentResponse(id, "aceptaciones/DeclaracionCompromisoTransversales", "Compromiso_Principios_Transversales", formData -> {
            Map<String, String> extraImages = new HashMap<>();
            String base64 = jsonUtils.getResourceAsBase64("static/images/aceptaciones/compromiso-cumplimiento-principios-transv.jpg");
            extraImages.put("fondoStyle", "background-image: url(data:image/jpeg;base64," + base64 + ");");
            mapDynamicImageWithFallback(extraImages, formData, "firmaBase64", "firma", "firmaImagen", "firmaCliente");
            return extraImages;
        });
    }

    @GetMapping("/declaracion-ausencia-conflicto/{id}")
    public ResponseEntity<byte[]> generateDeclaracionAusenciaConflicto(@PathVariable UUID id) {
        return processDocumentResponse(id, "aceptaciones/DeclaracionAusenciaConflicto", "DACI_Conflicto_Intereses", formData -> {
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

    private ResponseEntity<byte[]> processDocumentResponse(UUID id, String templateName, String filePrefix, Function<Map<String, Object>, Map<String, String>> extraImagesProvider) {
        DocumentEntity doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("¡Error! No existe ningún cliente con el ID: " + id));

        Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());

        // Calculamos las imágenes adicionales y la firma con el formData ya disponible
        Map<String, String> extraImages = null;
        if (extraImagesProvider != null) {
            extraImages = extraImagesProvider.apply(formData);
        }

        Map<String, Object> enrichedFormData = documentConfigService.enrich(templateName, formData);

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
