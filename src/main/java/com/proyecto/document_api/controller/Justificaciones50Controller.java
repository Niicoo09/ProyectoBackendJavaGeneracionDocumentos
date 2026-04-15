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
 * Controlador para la nueva categoría "Justificación (50%)".
 */
@RestController
@RequestMapping("/api/v1/justificaciones50")
@Tag(name = "Justificaciones 50%", description = "Endpoints para documentos de pagos anticipados y restantes")
public class Justificaciones50Controller {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentConfigService documentConfigService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private JsonUtils jsonUtils;

    @Operation(summary = "Pago Anticipado 50% L3")
    @GetMapping("/pago-anticipado-50-l3/{id}")
    public ResponseEntity<byte[]> generatePagoAnticipado50L3(@PathVariable UUID id) {
        Map<String, String> extraImages = new HashMap<>();
        String basePath = "static/images/justificaciones/justificaciones_50/pago-anticipado-50-l3/";

        extraImages.put("fondoStyle1", "background-image: url(data:image/jpeg;base64," +
                jsonUtils.getResourceAsBase64(basePath + "p1.jpg") + ");");
        extraImages.put("fondoStyle2", "background-image: url(data:image/jpeg;base64," +
                jsonUtils.getResourceAsBase64(basePath + "p2.jpg") + ");");
        extraImages.put("fondoStyle3", "background-image: url(data:image/jpeg;base64," +
                jsonUtils.getResourceAsBase64(basePath + "p3.jpg") + ");");

        loadSignature(id, extraImages);

        return processDocumentResponse(id, "justificaciones50/L3PagoAnticipado50", "Pago_Anticipado_50_L3", extraImages,
                "L3PagoAnticipado50");
    }

    @Operation(summary = "Pago Restante 50% L3")
    @GetMapping("/pago-restante-50-l3/{id}")
    public ResponseEntity<byte[]> generatePagoRestante50L3(@PathVariable UUID id) {
        Map<String, String> extraImages = new HashMap<>();
        String basePath = "static/images/justificaciones/justificaciones_50/pago-restante-50-l3/";

        extraImages.put("fondoStyle1", "background-image: url(data:image/jpeg;base64," +
                jsonUtils.getResourceAsBase64(basePath + "p1.jpg") + ");");
        extraImages.put("fondoStyle2", "background-image: url(data:image/jpeg;base64," +
                jsonUtils.getResourceAsBase64(basePath + "p2.jpg") + ");");

        loadSignature(id, extraImages);

        return processDocumentResponse(id, "justificaciones50/L3PagoRestante50", "Pago_Restante_50_L3", extraImages,
                "L3PagoRestante50");
    }

    @Operation(summary = "Pago Anticipado 100% L4")
    @GetMapping("/pago-anticipado-100-l4/{id}")
    public ResponseEntity<byte[]> generatePagoAnticipado100L4(@PathVariable UUID id) {
        Map<String, String> extraImages = new HashMap<>();
        String basePath = "static/images/justificaciones/justificaciones_50/pago-anticipado-100-l4/";

        extraImages.put("fondoStyle1", "background-image: url(data:image/jpeg;base64," +
                jsonUtils.getResourceAsBase64(basePath + "p1.jpg") + ");");
        extraImages.put("fondoStyle2", "background-image: url(data:image/jpeg;base64," +
                jsonUtils.getResourceAsBase64(basePath + "p2.jpg") + ");");

        loadSignature(id, extraImages);

        return processDocumentResponse(id, "justificaciones50/L4PagoAnticipado100", "Pago_Anticipado_100_L4",
                extraImages, "L4PagoAnticipado100");
    }

    // =========================================================================
    // LÓGICA INTERNA
    // =========================================================================

    private void loadSignature(UUID id, Map<String, String> extraImages) {
        documentRepository.findById(id).ifPresent(doc -> {
            Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
            mapDynamicImageWithFallback(extraImages, formData, "firmaBase64", "firma", "firmaImagen", "firmaCliente");
        });
    }

    private void mapDynamicImageWithFallback(Map<String, String> extraImages, Map<String, Object> formData,
            String templateKey, String... jsonKeys) {
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

    private ResponseEntity<byte[]> processDocumentResponse(UUID id, String templateName, String filePrefix,
            Map<String, String> extraImages, String configId) {
        DocumentEntity doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("¡Error! No existe el registro con ID: " + id));

        Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());

        // Enriquecemos usando el ID de configuración para el mapeo correcto
        Map<String, Object> enrichedFormData = documentConfigService.enrich(configId, formData);

        Map<String, Object> data = new HashMap<>();
        data.put("form", enrichedFormData);
        data.put("name", doc.getNombre() != null ? doc.getNombre() : "Cliente");

        data.put("logoBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/logo-solay.png"));
        // Aquí pasamos la firma del técnico de Solay como 'firmaSolay' para no
        // confundirla con la del cliente
        data.put("firmaSolay", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/firma-solay.png"));

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
}
