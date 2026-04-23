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

@RestController
@RequestMapping("/api/v1/pac")
@Tag(name = "Documentos PAC", description = "Endpoints para la generación de documentos de la sección PAC")
public class PACController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentConfigService documentConfigService;

    @Autowired
    private JsonUtils jsonUtils;

    @Operation(summary = "Generar Declaración del Propietario")
    @GetMapping("/declaracion-propietario/{id}")
    public ResponseEntity<byte[]> generateDeclaracionPropietario(@PathVariable UUID id) {
        return processPacResponse(id, "pac/DeclaracionPropietario", "Declaracion_Propietario", "declaracion-propietario", formData -> {
            Map<String, String> extraImages = new HashMap<>();
            String base64 = jsonUtils.getResourceAsBase64("static/images/pac/Modelodeclaracionpropietario20180515_pages-to-jpg-0001.jpg");
            extraImages.put("fondoStyle", "background-image: url(data:image/jpeg;base64," + base64 + ");");
            return extraImages;
        });
    }

    @Operation(summary = "Generar Autorización de Facturación")
    @GetMapping("/autorizacion-facturacion/{id}")
    public ResponseEntity<byte[]> generateAutorizacionFacturacion(@PathVariable UUID id) {
        return processPacResponse(id, "pac/AutorizacionFacturacion", "Autorizacion_Facturacion", "autorizacion-facturacion", formData -> {
            Map<String, String> extraImages = new HashMap<>();
            String base64Fondo1 = jsonUtils.getResourceAsBase64("static/images/pac/Autorizacion_representante_receptor_factura_es_v2_page-0001.jpg");
            String base64Fondo2 = jsonUtils.getResourceAsBase64("static/images/pac/Autorizacion_representante_receptor_factura_es_v2_page-0002.jpg");
            extraImages.put("fondoStyle1", "background-image: url(data:image/jpeg;base64," + base64Fondo1 + ");");
            extraImages.put("fondoStyle2", "background-image: url(data:image/jpeg;base64," + base64Fondo2 + ");");
            return extraImages;
        });
    }

    private ResponseEntity<byte[]> processPacResponse(UUID id, String templateName, String filePrefix, String configId, Function<Map<String, Object>, Map<String, String>> extraImagesProvider) {
        System.out.println("[DEBUG-PAC] Generando documento para ID: " + id);
        
        DocumentEntity doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("¡Error! No existe el registro con ID: " + id));

        System.out.println("[DEBUG-PAC] Documento encontrado: " + doc.getNombre());
        System.out.println("[DEBUG-PAC] JSON original en BD: " + doc.getFormulario());

        Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
        Map<String, Object> enrichedFormData = documentConfigService.enrich(configId, formData);

        System.out.println("[DEBUG-PAC] Formulario Enriquecido (el que va a la plantilla): " + enrichedFormData);

        Map<String, Object> data = new HashMap<>();
        data.put("form", enrichedFormData);
        data.put("name", doc.getNombre());
        
        // Firma del cliente si existe
        Object firma = formData.get("firmaBase64");
        if (firma != null && !firma.toString().isEmpty()) {
            data.put("firmaBase64", firma.toString().startsWith("data:image") ? firma.toString() : "data:image/png;base64," + firma.toString());
        }

        if (extraImagesProvider != null) {
            data.putAll(extraImagesProvider.apply(formData));
        }

        byte[] pdfBytes = documentService.generatePdf(templateName, data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(filePrefix + "_" + doc.getNombre().replace(" ", "_") + ".pdf").build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
