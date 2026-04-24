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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Punto de entrada de la API.
 * Define las rutas (URLs) que podemos usar en el navegador para ver o descargar
 * PDFs.
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
    private DocumentConfigService documentConfigService;

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
            @RequestParam(defaultValue = "Contenido de Formulario") String description) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("name", name);
        data.put("description", description);

        byte[] pdfBytes = documentService.generatePdf("example", data);

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
        return processDocumentResponse(id, "CertificadoAdecuacion", "Certificado_Adecuacion", "CertificadoAdecuacion",
                null);
    }

    /**
     * CERTIFICADO 1.1: Autorización de Representación.
     */
    @Operation(summary = "Autorización de Representación")
    @GetMapping("/autorizacion-representacion/{id}")
    public ResponseEntity<byte[]> generateAutorizacionRepresentacion(@PathVariable UUID id) {
        return processDocumentResponse(id, "administrativos/AutorizacionRepresentacion", "autorizacion_representacion",
                "autorizacion-representacion", formData -> {
                    Map<String, String> extraImages = new HashMap<>();
                    String base64 = jsonUtils
                            .getResourceAsBase64("static/images/administrativos/autorizacion-representacion.jpg");
                    extraImages.put("fondoStyle", "background-image: url(data:image/jpeg;base64," + base64 + ");");
                    mapDynamicImage(extraImages, formData, "firmaBase64", "firma");
                    mapDynamicImage(extraImages, formData, "firma", "firma");
                    mapDynamicImage(extraImages, formData, "firmaImagen", "firma");
                    mapDynamicImage(extraImages, formData, "firmaCliente", "firma");
                    return extraImages;
                });
    }

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

    private ResponseEntity<byte[]> generateMtdVariant(String mtdId, UUID id) {
        return processDocumentResponse(id, "MemoriaTecnica", mtdId.toUpperCase(), mtdId, formData -> {
            Map<String, String> extraImages = new HashMap<>();
            mapDynamicImage(extraImages, formData, "h_esquemaUnifilar", "esquemaUnifilarBase64");
            mapDynamicImage(extraImages, formData, "esquemaUnifilar", "esquemaUnifilarBase64");
            mapDynamicImage(extraImages, formData, "otros_imagenPlanoEmplazamiento", "planoEmplazamientoBase64");
            mapDynamicImage(extraImages, formData, "planoEmplazamiento", "planoEmplazamientoBase64");
            return extraImages;
        });
    }

    /**
     * CERTIFICADO 2: Solidez y Seguridad - Aporticada Teja.
     */
    @Operation(summary = "Certificado Aporticada Teja")
    @GetMapping({ "/aporticada-teja/{id}", "/aporticado-teja/{id}" })
    public ResponseEntity<byte[]> generateAporticadaTeja(@PathVariable UUID id) {
        return processDocumentResponse(id, "CertificadoAporticadaTeja", "Certificado_Solidez_Teja",
                "CertificadoAporticadaTeja", formData -> {
                    Map<String, String> extraImages = new HashMap<>();
                    extraImages.put("imagenTecnicaBase64",
                            "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/teja-aporticada.png"));
                    return extraImages;
                });
    }

    /**
     * CERTIFICADO 3: Solidez y Seguridad - Chapa Grecada Aporticada.
     */
    @Operation(summary = "Certificado Chapa Grecada Aporticada")
    @GetMapping("/chapas-grecadas/{id}")
    public ResponseEntity<byte[]> generateChapasGrecadas(@PathVariable UUID id) {
        return processDocumentResponse(id, "CertificadoChapasGrecadasAporticadas", "Certificado_Solidez_Grecada",
                "CertificadoChapasGrecadasAporticadas", formData -> {
                    Map<String, String> extraImages = new HashMap<>();
                    extraImages.put("imagenTecnicaBase64", "data:image/png;base64,"
                            + jsonUtils.getResourceAsBase64("static/cubierta-plana-aporticada.png"));
                    return extraImages;
                });
    }

    /**
     * CERTIFICADO 4: Solidez y Seguridad - Chapa Grecada Coplanaria.
     */
    @Operation(summary = "Certificado Chapa Grecada Coplanaria")
    @GetMapping("/chapas-grecadas-coplanaria/{id}")
    public ResponseEntity<byte[]> generateChapasGrecadasCoplanaria(@PathVariable UUID id) {
        return processDocumentResponse(id, "CertificadoChapasGrecadasCoplanaria",
                "Certificado_Solidez_Grecada_Coplanaria", "CertificadoChapasGrecadasCoplanaria", formData -> {
                    Map<String, String> extraImages = new HashMap<>();
                    extraImages.put("imagenTecnicaBase64",
                            "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/grecada-coplanaria.png"));
                    return extraImages;
                });
    }

    /**
     * CERTIFICADO 5: Solidez y Seguridad - Coplanar Teja.
     */
    @Operation(summary = "Certificado Coplanar Teja")
    @GetMapping("/coplanar-teja/{id}")
    public ResponseEntity<byte[]> generateCoplanarTeja(@PathVariable UUID id) {
        return processDocumentResponse(id, "CertificadoCoplanarTeja", "Certificado_Solidez_Teja_Coplanar",
                "CertificadoCoplanarTeja", formData -> {
                    Map<String, String> extraImages = new HashMap<>();
                    extraImages.put("imagenTecnicaBase64",
                            "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/teja-complanaria-1.png"));
                    return extraImages;
                });
    }

    /**
     * CERTIFICADO 6: Solidez y Seguridad - Cubierta Plana Aporticada.
     */
    @Operation(summary = "Certificado Cubierta Plana Aporticada")
    @GetMapping("/cubierta-plana-aporticada/{id}")
    public ResponseEntity<byte[]> generateCubiertaPlanaAporticada(@PathVariable UUID id) {
        return processDocumentResponse(id, "CertificadoCubiertaPlanaAporticada",
                "Certificado_Solidez_Cubierta_Plana_Aporticada", "CertificadoCubiertaPlanaAporticada", formData -> {
                    Map<String, String> extraImages = new HashMap<>();
                    extraImages.put("imagenTecnicaBase64", "data:image/png;base64,"
                            + jsonUtils.getResourceAsBase64("static/cubierta-plana-aporticada.png"));
                    return extraImages;
                });
    }

    /**
     * CERTIFICADO 7: Solidez y Seguridad - Paramento Vertical.
     */
    @Operation(summary = "Certificado Paramento Vertical")
    @GetMapping("/paramento-vertical/{id}")
    public ResponseEntity<byte[]> generateParamentoVertical(@PathVariable UUID id) {
        return processDocumentResponse(id, "CertificadoParamentoVertical", "Certificado_Solidez_Paramento_Vertical",
                "CertificadoParamentoVertical", formData -> {
                    Map<String, String> extraImages = new HashMap<>();
                    extraImages.put("imagenTecnicaBase64",
                            "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/paramento-vertical.png"));
                    return extraImages;
                });
    }

    /**
     * CERTIFICADO 8: Solidez y Seguridad - Pérgola Aporticada.
     */
    @Operation(summary = "Certificado Pérgola Aporticada")
    @GetMapping({ "/pergola-aporticada/{id}", "/pergola/{id}" })
    public ResponseEntity<byte[]> generatePergolaAporticada(@PathVariable UUID id) {
        return processDocumentResponse(id, "CertificadoPergolaAporticada", "Certificado_Solidez_Pergola",
                "CertificadoPergolaAporticada", null);
    }

    /**
     * CERTIFICADO 9: Declaración de Habilitación Profesional.
     */
    @Operation(summary = "Declaración de Habilitación Profesional")
    @GetMapping("/habilitacion-profesional/{id}")
    public ResponseEntity<byte[]> generateHabilitacionProfesional(@PathVariable UUID id) {
        return processDocumentResponse(id, "DeclaracionHabilitacionProfesional", "Declaracion_Habilitacion_Profesional",
                "DeclaracionHabilitacionProfesional", null);
    }

    /**
     * CERTIFICADO 10: Declaración No Generación de RCDs.
     */
    @Operation(summary = "Declaración No Generación RCDs")
    @GetMapping({ "/no-generacion-rcds/{id}", "/no-generacion-residuos/{id}" })
    public ResponseEntity<byte[]> generateNoGeneracionRcds(@PathVariable UUID id) {
        return processDocumentResponse(id, "administrativos/DeclaracionNoGeneracionRcds", "Declaracion_No_Generacion_RCDs",
                "DeclaracionNoGeneracionRcds", null);
    }

    /**
     * CERTIFICADO 11: Memoria Técnica de Diseño (MTD).
     */
    @Operation(summary = "Memoria Técnica de Diseño")
    @GetMapping("/memoria-tecnica/{id}")
    public ResponseEntity<byte[]> generateMemoriaTecnica(@PathVariable UUID id) {
        return processDocumentResponse(id, "MemoriaTecnica", "Memoria_Tecnica_Diseno", "MemoriaTecnica", formData -> {
            Map<String, String> images = new HashMap<>();
            mapDynamicImage(images, formData, "h_esquemaUnifilar", "esquemaUnifilarBase64");
            mapDynamicImage(images, formData, "otros_imagenPlanoEmplazamiento", "planoEmplazamientoBase64");
            return images;
        });
    }

    /**
     * CERTIFICADO 12: Planos de Situación, Emplazamiento y Cubierta.
     */
    @Operation(summary = "Planos de Situación y Cubierta")
    @GetMapping("/planos/{id}")
    public ResponseEntity<byte[]> generatePlanos(@PathVariable UUID id) {
        return processDocumentResponse(id, "PlanosSituacionEmplazamientoCubierta", "Planos",
                "PlanosSituacionEmplazamientoCubierta", formData -> {
                    Map<String, String> images = new HashMap<>();
                    for (int i = 1; i <= 5; i++) {
                        String base64 = jsonUtils.getResourceAsBase64("static/logos/Icono renovables " + i + ".png");
                        if (!base64.isEmpty())
                            images.put("logoRenovables" + i + "Base64", "data:image/png;base64," + base64);
                    }
                    mapDynamicImage(images, formData, "otros_imagenPlanoSituacion", "imagenSituacionBase64");
                    mapDynamicImage(images, formData, "otros_imagenSituacion", "imagenSituacionBase64");
                    mapDynamicImage(images, formData, "otros_foto1", "imagenSituacionBase64");
                    mapDynamicImage(images, formData, "otros_imagenPlanoEmplazamiento", "imagenEmplazamientoBase64");
                    mapDynamicImage(images, formData, "otros_imagenPlanoCubierta", "imagenCubiertaBase64");
                    mapDynamicImage(images, formData, "otros_imagenCubierta", "imagenCubiertaBase64");
                    mapDynamicImage(images, formData, "otros_foto2", "imagenCubiertaBase64");
                    return images;
                });
    }

    @Operation(summary = "Estudio Básico de Seguridad y Salud")
    @GetMapping("/estudio-seguridad/{id}")
    public ResponseEntity<byte[]> generateEstudioSeguridad(@PathVariable UUID id) {
        DocumentEntity doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe el ID: " + id));
        Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
        Map<String, Object> enrichedFormData = documentConfigService.enrich("EstudioSeguridadSalud", formData);

        Map<String, Object> data = new HashMap<>();
        data.put("form", enrichedFormData);
        data.put("logoBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/logo-solay.png"));
        data.put("firmaBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/firma-solay.png"));

        for (int i = 1; i <= 5; i++) {
            String base64 = jsonUtils.getResourceAsBase64("static/logos/Icono renovables " + i + ".png");
            if (!base64.isEmpty())
                data.put("logoRenovables" + i + "Base64", "data:image/png;base64," + base64);
        }
        mapDynamicImage(data, formData, "h_esquemaUnifilar", "esquemaUnifilarBase64");
        mapDynamicImage(data, formData, "otros_imagenPlanoEmplazamiento", "planoEmplazamientoBase64");

        byte[] pdfInicio = documentService.generatePdf("EstudioSeguridadSalud_Inicio", data);
        byte[] pdfFinal = documentService.generatePdf("EstudioSeguridadSalud_Final", data);
        byte[] pdfNucleo = jsonUtils.getResourceAsBytes("static/pdf/11.- Estudio Básico de SYS Nucleo.pdf");

        List<byte[]> pdfsToMerge = new ArrayList<>();
        pdfsToMerge.add(pdfInicio);
        if (pdfNucleo != null && pdfNucleo.length > 0)
            pdfsToMerge.add(pdfNucleo);
        pdfsToMerge.add(pdfFinal);

        byte[] combinedPdf = documentService.mergePdfs(pdfsToMerge);
        String safeName = (doc.getNombre() != null) ? doc.getNombre().replace(" ", "_") : "Documento";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("Estudio_Seguridad_Salud_" + safeName + ".pdf").build());
        return new ResponseEntity<>(combinedPdf, headers, HttpStatus.OK);
    }

    /**
     * ÚLTIMA PÁGINA: Otras informaciones útiles.
     */
    @Operation(summary = "Última Página Informativa")
    @GetMapping("/documento-ultima-pagina/{id}")
    public ResponseEntity<byte[]> generateUltimaPagina(@PathVariable UUID id) {
        return processDocumentResponse(id, "DocumentoUltimaPagina", "Ultima_Pagina", "documento-ultima-pagina", null);
    }

    /**
     * Auxiliar para mapear imágenesBase64 que vienen directamente en el JSON de la
     * BD.
     */
    @SuppressWarnings("unchecked")
    private <T> void mapDynamicImage(Map<String, T> target, Map<String, Object> formData, String jsonKey,
            String templateKey) {
        Object img = formData.get(jsonKey);
        if (img != null && !img.toString().isEmpty()) {
            String base64 = img.toString();
            if (!base64.startsWith("data:image"))
                base64 = "data:image/png;base64," + base64;
            target.put(templateKey, (T) base64);
        }
    }

    private ResponseEntity<byte[]> processDocumentResponse(UUID id, String templateName, String filePrefix,
            String configId, Function<Map<String, Object>, Map<String, String>> extraImagesProvider) {
        DocumentEntity doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe el ID: " + id));
        Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());

        Map<String, String> extraImages = (extraImagesProvider != null) ? extraImagesProvider.apply(formData) : null;
        Map<String, Object> enrichedFormData = documentConfigService.enrich(configId != null ? configId : templateName,
                formData);

        Map<String, Object> data = new HashMap<>();
        data.put("form", enrichedFormData);
        data.put("name", doc.getNombre() != null ? doc.getNombre() : "Cliente");
        data.put("logoBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/logo-solay.png"));
        data.put("firmaBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/firma-solay.png"));

        if (extraImages != null)
            data.putAll(extraImages);

        byte[] pdfBytes = documentService.generatePdf(templateName, data);
        String safeName = (doc.getNombre() != null) ? doc.getNombre().replace(" ", "_") : "Documento";
        String fileName = filePrefix + "_" + safeName + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());

        // --- EVITAR CACHÉ DEL NAVEGADOR ---
        headers.setCacheControl(CacheControl.noCache().mustRevalidate());
        headers.setPragma("no-cache");
        headers.setExpires(0);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
