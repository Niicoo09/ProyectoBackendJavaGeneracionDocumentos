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
     */
    @Operation(summary = "Cartel Publicitario L3")
    @GetMapping("/cartel-l3/{id}")
    public ResponseEntity<byte[]> generateCartelL3(@PathVariable UUID id) {
        return processDocumentResponse(id, "justificaciones/CartelL3", "5.- Modelo de cartel Publicitario L3", "cartel-l3", formData -> {
            Map<String, String> extraImages = new HashMap<>();
            String base64 = jsonUtils.getResourceAsBase64("/static/images/justificaciones/cartel-l3.png");
            extraImages.put("fondoStyle", "background-image: url(data:image/png;base64," + base64 + ");");
            return extraImages;
        });
    }

    @Operation(summary = "Cartel Publicitario L4")
    @GetMapping("/cartel-l4/{id}")
    public ResponseEntity<byte[]> generateCartelL4(@PathVariable UUID id) {
        return processDocumentResponse(id, "justificaciones/CartelL4", "6.- Modelo de cartel Publicitario L4", "cartel-l4", formData -> {
            Map<String, String> extraImages = new HashMap<>();
            String base64 = jsonUtils.getResourceAsBase64("/static/images/justificaciones/cartel-l4.png");
            extraImages.put("fondoStyle", "background-image: url(data:image/png;base64," + base64 + ");");
            return extraImages;
        });
    }

    @Operation(summary = "Declaración de Compromiso Corriente")
    @GetMapping("/declaracion-compromiso-corriente/{id}")
    public ResponseEntity<byte[]> generateDeclaracionCompromisoCorriente(@PathVariable UUID id) {
        return processDocumentResponse(id, "justificaciones/DeclaracionCompromisoCorriente", "9.- Declaracion de compromiso de pago - Corriente", "declaracion-compromiso-corriente", formData -> {
            Map<String, String> extraImages = new HashMap<>();
            String base64 = jsonUtils.getResourceAsBase64("/static/images/justificaciones/declaracion-compromiso-corriente.jpg");
            extraImages.put("fondoStyle", "background-image: url(data:image/jpeg;base64," + base64 + ");");
            mapDynamicImageWithFallback(extraImages, formData, "firma", "firma", "firmaImagen", "firmaCliente");
            return extraImages;
        });
    }

    @Operation(summary = "Justificación Pago Subvención L3")
    @GetMapping("/justificacion-pago-subvencion-l3/{id}")
    public ResponseEntity<byte[]> generateJustificacionPagoSubvencionL3(@PathVariable UUID id) {
        return processDocumentResponse(id, "justificaciones/JustificacionPagoSubvencionL3", "1.- Formato para Pago previa justificacion (100%) L3", "justificacion-pago-subvencion-l3", formData -> {
            Map<String, String> extraImages = new HashMap<>();
            String b1 = jsonUtils.getResourceAsBase64("/static/images/justificaciones/formato-pago-justificacion-linea3-1.jpg");
            String b2 = jsonUtils.getResourceAsBase64("/static/images/justificaciones/formato-pago-justificacion-linea3-2.jpg");
            extraImages.put("fondoStyle1", "background-image: url(data:image/jpeg;base64," + b1 + ");");
            extraImages.put("fondoStyle2", "background-image: url(data:image/jpeg;base64," + b2 + ");");
            mapDynamicImageWithFallback(extraImages, formData, "firma", "firma", "firmaImagen", "firmaCliente");
            return extraImages;
        });
    }

    @Operation(summary = "Justificación Pago Subvención L4")
    @GetMapping("/justificacion-pago-subvencion-l4/{id}")
    public ResponseEntity<byte[]> generateJustificacionPagoSubvencionL4(@PathVariable UUID id) {
        return processDocumentResponse(id, "justificaciones/JustificacionPagoSubvencionL4", "2.- Formato para Pago previa justificacion (100%) L4", "justificacion-pago-subvencion-l4", formData -> {
            Map<String, String> extraImages = new HashMap<>();
            String b1 = jsonUtils.getResourceAsBase64("/static/images/justificaciones/formato-pago-justificacion-linea4-1.jpg");
            String b2 = jsonUtils.getResourceAsBase64("/static/images/justificaciones/formato-pago-justificacion-linea4-2.jpg");
            extraImages.put("fondoStyle1", "background-image: url(data:image/jpeg;base64," + b1 + ");");
            extraImages.put("fondoStyle2", "background-image: url(data:image/jpeg;base64," + b2 + ");");
            mapDynamicImageWithFallback(extraImages, formData, "firma", "firma", "firmaImagen", "firmaCliente");
            return extraImages;
        });
    }

    @Operation(summary = "Memoria Económica")
    @GetMapping("/memoria-economica/{id}")
    public ResponseEntity<byte[]> generateMemoriaEconomica(@PathVariable UUID id) {
        return processDocumentResponse(id, "justificaciones/MemoriaEconomica", "3.- Memoria Económica Justificativa", "memoria-economica", formData -> {
            Map<String, String> extraImages = new HashMap<>();
            extraImages.put("logoOrganizaciones", "data:image/png;base64," + jsonUtils.getResourceAsBase64("/static/logos/iconos-organizaciones.png"));
            extraImages.put("logoJunta",           "data:image/png;base64," + jsonUtils.getResourceAsBase64("/static/logos/icono-junta-andalucia.png"));
            mapDynamicImageWithFallback(extraImages, formData, "firmaImagen", "firma", "firmaImagen", "firmaCliente");
            return extraImages;
        });
    }

    @Operation(summary = "Memoria FV AER")
    @GetMapping("/memoria-fv-aer/{id}")
    public ResponseEntity<byte[]> generateMemoriaFvAer(@PathVariable UUID id) {
        return processDocumentResponse(id, "justificaciones/MemoriaFvAer", "4.- Memoria Tecnica de la Actuacion Realizada (FV+AER)", "memoria-fv-aer", formData -> {
            Map<String, String> extraImages = new HashMap<>();
            extraImages.put("logoOrganizaciones", "data:image/png;base64," + jsonUtils.getResourceAsBase64("/static/logos/iconos-organizaciones.png"));
            extraImages.put("logoJunta",           "data:image/png;base64," + jsonUtils.getResourceAsBase64("/static/logos/icono-junta-andalucia.png"));
            mapDynamicImageWithFallback(extraImages, formData, "firmaImagen", "firma", "firmaImagen", "firmaCliente");
            return extraImages;
        });
    }

    @Operation(summary = "Declaración Inicio de Obras Massol")
    @GetMapping("/obra-massol/{id}")
    public ResponseEntity<byte[]> generateObraMassol(@PathVariable UUID id) {
        return processDocumentResponse(id, "justificaciones/ObraMassol", "8.- Declaracion inicio de obras-Massol", "obra-massol", formData -> {
            Map<String, String> extraImages = new HashMap<>();
            extraImages.put("logoSolay",  "data:image/png;base64," + jsonUtils.getResourceAsBase64("/static/logo-solay.png"));
            extraImages.put("firmaSolay", "data:image/png;base64," + jsonUtils.getResourceAsBase64("/static/firma-solay.png"));
            mapDynamicImageWithFallback(extraImages, formData, "firma", "firma", "firmaImagen", "firmaCliente");
            return extraImages;
        });
    }

    @Operation(summary = "Certificado de Pedidos")
    @GetMapping("/certificado-pedidos/{id}")
    public ResponseEntity<byte[]> generateCertificadoPedidos(@PathVariable UUID id) {
        return processDocumentResponse(id, "justificaciones/CertificadoPedidos", "7.- Certificado de pedidos y contratos", "certificado-pedidos", formData -> {
            Map<String, String> extraImages = new HashMap<>();
            extraImages.put("logoOrganizaciones", "data:image/png;base64," + jsonUtils.getResourceAsBase64("/static/logos/iconos-organizaciones.png"));
            extraImages.put("logoJunta",           "data:image/png;base64," + jsonUtils.getResourceAsBase64("/static/logos/icono-junta-andalucia.png"));
            mapDynamicImageWithFallback(extraImages, formData, "firmaImagen", "firma", "firmaImagen", "firmaCliente");
            return extraImages;
        });
    }
    @Operation(summary = "Anexo A - Cesión de Datos")
    @GetMapping("/anexo-a/{id}")
    public ResponseEntity<byte[]> generateAnexoA(@PathVariable UUID id) {
        return processDocumentResponse(id, "aceptaciones/DeclaracionCesionTratamiento", "10.- ANEXO A - Declaracion de cesion y tratamiento de datos", "cesion-tratamiento-datos", formData -> {
            Map<String, String> extraImages = new HashMap<>();
            String b1 = jsonUtils.getResourceAsBase64("/static/images/aceptaciones/declaracio-cesion-tratamiento-1.jpg");
            String b2 = jsonUtils.getResourceAsBase64("/static/images/aceptaciones/declaracio-cesion-tratamiento-2.jpg");
            extraImages.put("fondoStyle1", "background-image: url(data:image/jpeg;base64," + b1 + ");");
            extraImages.put("fondoStyle2", "background-image: url(data:image/jpeg;base64," + b2 + ");");
            mapDynamicImageWithFallback(extraImages, formData, "firmaBase64", "firma", "firmaImagen", "firmaCliente");
            return extraImages;
        });
    }

    @Operation(summary = "Anexo B - Principios Transversales")
    @GetMapping("/anexo-b/{id}")
    public ResponseEntity<byte[]> generateAnexoB(@PathVariable UUID id) {
        return processDocumentResponse(id, "aceptaciones/DeclaracionCompromisoDerechos", "11.- ANEXO B - Declaracion compromiso cumplimiento principios transversales", "compromiso-principios-transversales", formData -> {
            Map<String, String> extraImages = new HashMap<>();
            String base64 = jsonUtils.getResourceAsBase64("static/images/aceptaciones/declaración-de-compromiso.jpg");
            extraImages.put("fondoStyle", "background-image: url(data:image/jpeg;base64," + base64 + ");");
            mapDynamicImageWithFallback(extraImages, formData, "firmaBase64", "firma", "firmaImagen", "firmaCliente");
            return extraImages;
        });
    }

    @Operation(summary = "Anexo C - Principios Transversales (DNSH)")
    @GetMapping("/anexo-c/{id}")
    public ResponseEntity<byte[]> generateAnexoC(@PathVariable UUID id) {
        return processDocumentResponse(id, "aceptaciones/DeclaracionCompromisoTransversales", "12.- ANEXO C - Declaracion compromiso cumplimiento de principios transversales (DNSH)", "compromiso-transversales-dnsh", formData -> {
            Map<String, String> extraImages = new HashMap<>();
            String base64 = jsonUtils.getResourceAsBase64("static/images/aceptaciones/compromiso-cumplimiento-principios-transv.jpg");
            extraImages.put("fondoStyle", "background-image: url(data:image/jpeg;base64," + base64 + ");");
            mapDynamicImageWithFallback(extraImages, formData, "firmaBase64", "firma", "firmaImagen", "firmaCliente");
            return extraImages;
        });
    }

    // =========================================================================
    // LÓGICA INTERNA
    // =========================================================================

    private ResponseEntity<byte[]> processDocumentResponse(UUID id, String templateName, String filePrefix, String configId, Function<Map<String, Object>, Map<String, String>> extraImagesProvider) {
        DocumentEntity doc = documentRepository.findById(id).orElseThrow(() -> new RuntimeException("No existe el ID: " + id));
        Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
        
        Map<String, String> extraImages = (extraImagesProvider != null) ? extraImagesProvider.apply(formData) : null;
        Map<String, Object> enrichedFormData = documentConfigService.enrich(configId != null ? configId : templateName, formData);

        Map<String, Object> data = new HashMap<>();
        data.put("form", enrichedFormData);
        data.put("name", doc.getNombre() != null ? doc.getNombre() : "Cliente");
        data.put("logoBase64",  "data:image/png;base64," + jsonUtils.getResourceAsBase64("/static/logo-solay.png"));
        data.put("firmaBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("/static/firma-solay.png"));

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
