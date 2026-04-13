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
 * Controlador para documentos de la categoría "Legalización", Proyectos y Administrativos.
 */
@RestController
@RequestMapping("/api/v1/legalizacion")
@Tag(name = "Legalización", description = "Endpoints para memorias técnicas, certificados y documentos administrativos")
public class LegalizacionController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentConfigService documentConfigService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private JsonUtils jsonUtils;

    // --- MEMORIAS TÉCNICAS (MTD) ---

    @Operation(summary = "MTD Autoconsumo Monofásica con Batería")
    @GetMapping("/mtd-monofasica-con-bateria/{id}")
    public ResponseEntity<byte[]> generateMtdMonofasica(@PathVariable UUID id) {
        return generateMtdVariant("mtd-instalacion-autoconsumo-monofasica-con-bateria", id);
    }

    @Operation(summary = "MTD Aislada con Batería")
    @GetMapping("/mtd-aislada-con-bateria/{id}")
    public ResponseEntity<byte[]> generateMtdAislada(@PathVariable UUID id) {
        return generateMtdVariant("mtd-instalacion-aislada-con-bateria", id);
    }

    @Operation(summary = "MTD Autoconsumo Trifásica con Batería")
    @GetMapping("/mtd-trifasica-con-bateria/{id}")
    public ResponseEntity<byte[]> generateMtdTrifasica(@PathVariable UUID id) {
        return generateMtdVariant("mtd-instalacion-autoconsumo-trifasica-con-bateria", id);
    }

    @Operation(summary = "MTD Autoconsumo Sin Batería")
    @GetMapping("/mtd-sin-bateria/{id}")
    public ResponseEntity<byte[]> generateMtdSinBateria(@PathVariable UUID id) {
        return generateMtdVariant("mtd-instalacion-autoconsumo-sin-bateria", id);
    }

    @Operation(summary = "MTD Punto de Recarga")
    @GetMapping("/mtd-punto-recarga/{id}")
    public ResponseEntity<byte[]> generateMtdPuntoRecarga(@PathVariable UUID id) {
        return generateMtdVariant("mtd-instalacion-puntos-recarga", id);
    }

    /**
     * Helper genérico para todas las variantes de MTD.
     */
    private ResponseEntity<byte[]> generateMtdVariant(String mtdId, UUID id) {
        Map<String, String> extraImages = new HashMap<>();
        documentRepository.findById(id).ifPresent(doc -> {
            Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
            mapDynamicImageWithFallback(extraImages, formData, "esquemaUnifilarBase64", "h_esquemaUnifilar", "esquemaUnifilar");
            mapDynamicImageWithFallback(extraImages, formData, "planoEmplazamientoBase64", "otros_imagenPlanoEmplazamiento", "planoEmplazamiento");
        });
        return processDocumentResponse(id, "MemoriaTecnica", mtdId.toUpperCase(), extraImages, mtdId);
    }

    // --- DOCUMENTOS ADMINISTRATIVOS ---

    @GetMapping("/autorizacion-representacion/{id}")
    public ResponseEntity<byte[]> generateAutorizacionRepresentacion(@PathVariable UUID id) {
        Map<String, String> extraImages = new HashMap<>();
        String base64 = jsonUtils.getResourceAsBase64("static/images/administrativos/autorizacion-representacion.jpg");
        extraImages.put("fondoStyle", "background-image: url(data:image/jpeg;base64," + base64 + ");");
        
        loadSignature(id, extraImages);
        
        return processDocumentResponse(id, "administrativos/AutorizacionRepresentacion", "Autorizacion_Representacion", extraImages, "autorizacion-representacion");
    }

    @GetMapping("/anexo-iii/{id}")
    public ResponseEntity<byte[]> generateAnexoIii(@PathVariable UUID id) {
        Map<String, String> extraImages = new HashMap<>();
        // Multi-página (3 páginas según Vue)
        for (int i = 1; i <= 3; i++) {
            String b = jsonUtils.getResourceAsBase64("static/images/administrativos/anexo-iii-" + i + ".jpg");
            extraImages.put("fondoStyle" + i, "background-image: url(data:image/jpeg;base64," + b + ");");
        }
        
        loadSignature(id, extraImages);
        
        return processDocumentResponse(id, "administrativos/AnexoIii", "Anexo_III", extraImages, "autorizacion-comunicacion");
    }

    @GetMapping("/no-generacion-rcds/{id}")
    public ResponseEntity<byte[]> generateNoGeneracionRcds(@PathVariable UUID id) {
        Map<String, String> extraImages = new HashMap<>();
        String base64 = jsonUtils.getResourceAsBase64("static/images/administrativos/declaracion-no-generacion-rcds.jpg");
        extraImages.put("fondoStyle", "background-image: url(data:image/jpeg;base64," + base64 + ");");
        
        loadSignature(id, extraImages);
        
        return processDocumentResponse(id, "administrativos/DeclaracionNoGeneracionRcds", "Declaracion_No_RCDs", extraImages, "declaracion-no-generacion-rcds");
    }

    // --- CERTIFICADOS TÉCNICOS (Z-*) ---

    @GetMapping("/cie/{id}")
    public ResponseEntity<byte[]> generateCie(@PathVariable UUID id) {
        Map<String, String> extraImages = new HashMap<>();
        String base64 = jsonUtils.getResourceAsBase64("static/images/legalizacion/cie.jpg");
        extraImages.put("fondoStyle", "background-image: url(data:image/jpeg;base64," + base64 + ");");
        
        loadSignature(id, extraImages);
        
        return processDocumentResponse(id, "legalizacion/Cie", "Certificado_Instalacion_Electrica", extraImages, "z-certificado-br");
    }

    @GetMapping("/certificado-adecuacion/{id}")
    public ResponseEntity<byte[]> generateCertificadoAdecuacion(@PathVariable UUID id) {
        return processDocumentResponse(id, "CertificadoAdecuacion", "Certificado_Adecuacion", null, "certificado-adecuacion");
    }

    @GetMapping("/doacfv/{id}")
    public ResponseEntity<byte[]> generateDoacfv(@PathVariable UUID id) {
        Map<String, String> extraImages = new HashMap<>();
        String base64 = jsonUtils.getResourceAsBase64("static/images/legalizacion/z-certificado-doacfv.jpg");
        extraImages.put("fondoStyle", "background-image: url(data:image/jpeg;base64," + base64 + ");");
        
        loadSignature(id, extraImages);
        
        return processDocumentResponse(id, "legalizacion/Doacfv", "Certificado_DOACFV", extraImages, "z-certificado-doacfv");
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

    private ResponseEntity<byte[]> processDocumentResponse(UUID id, String templateName, String filePrefix, Map<String, String> extraImages, String configId) {
        DocumentEntity doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("¡Error! No existe el registro con ID: " + id));

        Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
        
        // Enriquecemos usando el ID de configuración (configId) para que el switch de DocumentConfigService acierte
        Map<String, Object> enrichedFormData = documentConfigService.enrich(configId != null ? configId : templateName, formData);

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
