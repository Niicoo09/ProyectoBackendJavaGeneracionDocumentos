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
 * Define las rutas (URLs) que podemos usar en el navegador para ver o descargar PDFs.
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
            @RequestParam(defaultValue = "Contenido de Formulario") String description
    ) {
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
        return processDocumentResponse(id, "CertificadoAdecuacion", "Certificado_Adecuacion", null);
    }

    /**
     * CERTIFICADO 2: Solidez y Seguridad - Aporticada Teja.
     * URL: http://localhost:8080/api/v1/documents/aporticada-teja/{uuid}
     */
    @Operation(summary = "Certificado Aporticada Teja", description = "Genera el certificado de solidez para cubiertas de teja aporticada.")
    @GetMapping("/aporticada-teja/{id}")
    public ResponseEntity<byte[]> generateAporticadaTeja(@PathVariable UUID id) {
        return processDocumentResponse(id, "CertificadoAporticadaTeja", "Certificado_Solidez_Teja", formData -> {
            Map<String, String> extraImages = new HashMap<>();
            extraImages.put("imagenTecnicaBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/teja-aporticada.png"));
            return extraImages;
        });
    }

    /**
     * CERTIFICADO 3: Solidez y Seguridad - Chapa Grecada Aporticada.
     * URL: http://localhost:8080/api/v1/documents/chapas-grecadas/{uuid}
     */
    @Operation(summary = "Certificado Chapa Grecada Aporticada", description = "Genera el certificado de solidez para cubiertas de chapa grecada.")
    @GetMapping("/chapas-grecadas/{id}")
    public ResponseEntity<byte[]> generateChapasGrecadas(@PathVariable UUID id) {
        return processDocumentResponse(id, "CertificadoChapasGrecadasAporticadas", "Certificado_Solidez_Grecada", formData -> {
            Map<String, String> extraImages = new HashMap<>();
            extraImages.put("imagenTecnicaBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/cubierta-plana-aporticada.png"));
            return extraImages;
        });
    }

    /**
     * CERTIFICADO 4: Solidez y Seguridad - Chapa Grecada Coplanaria.
     * URL: http://localhost:8080/api/v1/documents/chapas-grecadas-coplanaria/{uuid}
     */
    @Operation(summary = "Certificado Chapa Grecada Coplanaria", description = "Genera el certificado de solidez para cubiertas de chapa grecada coplanaria.")
    @GetMapping("/chapas-grecadas-coplanaria/{id}")
    public ResponseEntity<byte[]> generateChapasGrecadasCoplanaria(@PathVariable UUID id) {
        return processDocumentResponse(id, "CertificadoChapasGrecadasCoplanaria", "Certificado_Solidez_Grecada_Coplanaria", formData -> {
            Map<String, String> extraImages = new HashMap<>();
            extraImages.put("imagenTecnicaBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/grecada-coplanaria.png"));
            return extraImages;
        });
    }

    /**
     * CERTIFICADO 5: Solidez y Seguridad - Coplanar Teja.
     * URL: http://localhost:8080/api/v1/documents/coplanar-teja/{uuid}
     */
    @Operation(summary = "Certificado Coplanar Teja", description = "Genera el certificado de solidez para cubiertas de teja coplanar.")
    @GetMapping("/coplanar-teja/{id}")
    public ResponseEntity<byte[]> generateCoplanarTeja(@PathVariable UUID id) {
        return processDocumentResponse(id, "CertificadoCoplanarTeja", "Certificado_Solidez_Teja_Coplanar", formData -> {
            Map<String, String> extraImages = new HashMap<>();
            extraImages.put("imagenTecnicaBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/teja-complanaria-1.png"));
            return extraImages;
        });
    }

    /**
     * CERTIFICADO 6: Solidez y Seguridad - Cubierta Plana Aporticada.
     * URL: http://localhost:8080/api/v1/documents/cubierta-plana-aporticada/{uuid}
     */
    @Operation(summary = "Certificado Cubierta Plana Aporticada", description = "Genera el certificado de solidez para cubiertas planas aporticadas.")
    @GetMapping("/cubierta-plana-aporticada/{id}")
    public ResponseEntity<byte[]> generateCubiertaPlanaAporticada(@PathVariable UUID id) {
        return processDocumentResponse(id, "CertificadoCubiertaPlanaAporticada", "Certificado_Solidez_Cubierta_Plana_Aporticada", formData -> {
            Map<String, String> extraImages = new HashMap<>();
            extraImages.put("imagenTecnicaBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/cubierta-plana-aporticada.png"));
            return extraImages;
        });
    }

    /**
     * CERTIFICADO 7: Solidez y Seguridad - Paramento Vertical.
     * URL: http://localhost:8080/api/v1/documents/paramento-vertical/{uuid}
     */
    @Operation(summary = "Certificado Paramento Vertical", description = "Genera el certificado de solidez para instalaciones en paramento vertical.")
    @GetMapping("/paramento-vertical/{id}")
    public ResponseEntity<byte[]> generateParamentoVertical(@PathVariable UUID id) {
        return processDocumentResponse(id, "CertificadoParamentoVertical", "Certificado_Solidez_Paramento_Vertical", formData -> {
            Map<String, String> extraImages = new HashMap<>();
            extraImages.put("imagenTecnicaBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/paramento-vertical.png"));
            return extraImages;
        });
    }

    /**
     * CERTIFICADO 8: Solidez y Seguridad - Pérgola Aporticada.
     * URL: http://localhost:8080/api/v1/documents/pergola-aporticada/{uuid}
     */
    @Operation(summary = "Certificado Pérgola Aporticada", description = "Genera el certificado de solidez para instalaciones sobre pérgola.")
    @GetMapping("/pergola-aporticada/{id}")
    public ResponseEntity<byte[]> generatePergolaAporticada(@PathVariable UUID id) {
        return processDocumentResponse(id, "CertificadoPergolaAporticada", "Certificado_Solidez_Pergola", null);
    }

    /**
     * CERTIFICADO 9: Declaración de Habilitación Profesional.
     * URL: http://localhost:8080/api/v1/documents/habilitacion-profesional/{uuid}
     */
    @Operation(summary = "Declaración de Habilitación Profesional", description = "Genera la declaración responsable de habilitación profesional.")
    @GetMapping("/habilitacion-profesional/{id}")
    public ResponseEntity<byte[]> generateHabilitacionProfesional(@PathVariable UUID id) {
        return processDocumentResponse(id, "DeclaracionHabilitacionProfesional", "Declaracion_Habilitacion_Profesional", null);
    }

    /**
     * CERTIFICADO 10: Declaración No Generación de RCDs.
     * URL: http://localhost:8080/api/v1/documents/no-generacion-rcds/{uuid}
     */
    @Operation(summary = "Declaración No Generación RCDs", description = "Genera la declaración responsable de no generación de residuos.")
    @GetMapping("/no-generacion-rcds/{id}")
    public ResponseEntity<byte[]> generateNoGeneracionRcds(@PathVariable UUID id) {
        return processDocumentResponse(id, "DeclaracionNoGeneracionRcds", "Declaracion_No_Generacion_RCDs", null);
    }

    /**
     * CERTIFICADO 11: Memoria Técnica de Diseño (MTD).
     * URL: http://localhost:8080/api/v1/documents/memoria-tecnica/{uuid}
     */
    @Operation(summary = "Memoria Técnica de Diseño", description = "Genera la memoria técnica de diseño de la instalación.")
    @GetMapping("/memoria-tecnica/{id}")
    public ResponseEntity<byte[]> generateMemoriaTecnica(@PathVariable UUID id) {
        return processDocumentResponse(id, "MemoriaTecnica", "Memoria_Tecnica_Diseno", formData -> {
            Map<String, String> images = new HashMap<>();
            mapDynamicImage(images, formData, "h_esquemaUnifilar", "esquemaUnifilarBase64");
            mapDynamicImage(images, formData, "otros_imagenPlanoEmplazamiento", "planoEmplazamientoBase64");
            return images;
        });
    }

    /**
     * CERTIFICADO 12: Memoria Técnica de Diseño - Punto de Recarga VE.
     * URL: http://localhost:8080/api/v1/documents/punto-recarga-ve/{uuid}
     */
    @Operation(summary = "Memoria Técnica Punto de Recarga VE", description = "Genera la memoria técnica de diseño para puntos de recarga de vehículos eléctricos.")
    @GetMapping("/punto-recarga-ve/{id}")
    public ResponseEntity<byte[]> generateMemoriaTecnicaPuntoRecarga(@PathVariable UUID id) {
        return processDocumentResponse(id, "MemoriaTecnicaPuntoRecarga", "MTD_Punto_Recarga", formData -> {
            Map<String, String> images = new HashMap<>();
            mapDynamicImage(images, formData, "h_esquemaUnifilar", "esquemaUnifilarBase64");
            mapDynamicImage(images, formData, "otros_imagenPlanoEmplazamiento", "planoEmplazamientoBase64");
            return images;
        });
    }

    /**
     * CERTIFICADO 13: Planos de Situación, Emplazamiento y Cubierta.
     * URL: http://localhost:8080/api/v1/documents/planos/{uuid}
     */
    @Operation(summary = "Planos de Situación y Cubierta", description = "Genera el documento de planos de situación, emplazamiento y cubierta.")
    @GetMapping("/planos/{id}")
    public ResponseEntity<byte[]> generatePlanos(@PathVariable UUID id) {
        return processDocumentResponse(id, "PlanosSituacionEmplazamientoCubierta", "Planos", formData -> {
            Map<String, String> images = new HashMap<>();

            // Logos de certificación (Renovables)
            for (int i = 1; i <= 5; i++) {
                String base64 = jsonUtils.getResourceAsBase64("static/logos/Icono renovables " + i + ".png");
                if (!base64.isEmpty()) {
                    images.put("logoRenovables" + i + "Base64", "data:image/png;base64," + base64);
                }
            }

            // Imágenes dinámicas del JSON
            mapDynamicImage(images, formData, "otros_imagenPlanoSituacion",    "imagenSituacionBase64");
            mapDynamicImage(images, formData, "otros_imagenSituacion",          "imagenSituacionBase64");
            mapDynamicImage(images, formData, "otros_foto1",                    "imagenSituacionBase64");
            mapDynamicImage(images, formData, "otros_imagenPlanoEmplazamiento", "imagenEmplazamientoBase64");
            mapDynamicImage(images, formData, "otros_imagenPlanoCubierta",      "imagenCubiertaBase64");
            mapDynamicImage(images, formData, "otros_imagenCubierta",           "imagenCubiertaBase64");
            mapDynamicImage(images, formData, "otros_foto2",                    "imagenCubiertaBase64");

            return images;
        });
    }

    @Operation(summary = "Estudio Básico de Seguridad y Salud", description = "Genera el documento completo fusionando inicio dinámico, núcleo estático y final dinámico.")
    @GetMapping("/estudio-seguridad/{id}")
    public ResponseEntity<byte[]> generateEstudioSeguridad(@PathVariable UUID id) {
        // 1. Consultar la base de datos y preparar datos (una única vez)
        DocumentEntity doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("¡Error! No existe ningún cliente con el ID: " + id));
        Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
        Map<String, Object> enrichedFormData = documentConfigService.enrich("EstudioSeguridadSalud", formData);

        Map<String, Object> data = new HashMap<>();
        data.put("form", enrichedFormData);
        data.put("logoBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/logo-solay.png"));
        data.put("firmaBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/firma-solay.png"));

        // Logos corporativos (para la portada de inicio)
        for (int i = 1; i <= 5; i++) {
            String base64 = jsonUtils.getResourceAsBase64("static/logos/Icono renovables " + i + ".png");
            if (!base64.isEmpty()) {
                data.put("logoRenovables" + i + "Base64", "data:image/png;base64," + base64);
            }
        }

        // Imágenes dinámicas (opcionales para el inicio) — corregido: se mapean en 'data' directamente
        mapDynamicImage(data, formData, "h_esquemaUnifilar",             "esquemaUnifilarBase64");
        mapDynamicImage(data, formData, "otros_imagenPlanoEmplazamiento", "planoEmplazamientoBase64");

        // 2. Generar las partes dinámicas
        byte[] pdfInicio = documentService.generatePdf("EstudioSeguridadSalud_Inicio", data);
        byte[] pdfFinal  = documentService.generatePdf("EstudioSeguridadSalud_Final",  data);

        // 3. Cargar el núcleo estático (PDF de ~80 páginas)
        byte[] pdfNucleo = jsonUtils.getResourceAsBytes("static/pdf/11.- Estudio Básico de SYS Nucleo.pdf");

        // 4. Fusionar los 3 bloques en orden
        List<byte[]> pdfsToMerge = new ArrayList<>();
        pdfsToMerge.add(pdfInicio);
        if (pdfNucleo != null && pdfNucleo.length > 0) {
            pdfsToMerge.add(pdfNucleo);
        } else {
            System.err.println("¡ADVERTENCIA! No se encontró el archivo: static/pdf/11.- Estudio Básico de SYS Nucleo.pdf");
        }
        pdfsToMerge.add(pdfFinal);

        byte[] combinedPdf = documentService.mergePdfs(pdfsToMerge);

        // 5. Preparar respuesta
        String safeName = (doc.getNombre() != null) ? doc.getNombre().replace(" ", "_") : "Documento";
        String fileName = "Estudio_Seguridad_Salud_" + safeName + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());

        return new ResponseEntity<>(combinedPdf, headers, HttpStatus.OK);
    }

    /**
     * Auxiliar para mapear imágenesBase64 que vienen directamente en el JSON de la BD.
     * Funciona tanto con Map<String, String> como con Map<String, Object>.
     */
    @SuppressWarnings("unchecked")
    private <T> void mapDynamicImage(Map<String, T> target, Map<String, Object> formData, String jsonKey, String templateKey) {
        Object img = formData.get(jsonKey);
        if (img != null && !img.toString().isEmpty()) {
            String base64 = img.toString();
            if (!base64.startsWith("data:image")) {
                base64 = "data:image/png;base64," + base64;
            }
            target.put(templateKey, (T) base64);
        }
    }

    // =========================================================================
    // LÓGICA INTERNA COMÚN
    // =========================================================================

    private ResponseEntity<byte[]> processDocumentResponse(UUID id, String templateName, String filePrefix, Function<Map<String, Object>, Map<String, String>> extraImagesProvider) {
        // 1. Consultar la base de datos (una única vez)
        DocumentEntity doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("¡Error! No existe ningún cliente con el ID: " + id));

        // 2. Extraer y parsear datos del JSON (una única vez)
        Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());

        // 3. Calcular imágenes adicionales con el formData ya disponible
        Map<String, String> extraImages = null;
        if (extraImagesProvider != null) {
            extraImages = extraImagesProvider.apply(formData);
        }

        // 4. Enriquecer con defaultData y fieldMapping
        Map<String, Object> enrichedFormData = documentConfigService.enrich(templateName, formData);

        // 5. Preparar los datos comunes
        Map<String, Object> data = new HashMap<>();
        data.put("form", enrichedFormData);
        data.put("name", doc.getNombre() != null ? doc.getNombre() : "Cliente");

        // Imágenes corporativas estándar con prefijo (para evitar errores de SpEL)
        data.put("logoBase64",  "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/logo-solay.png"));
        data.put("firmaBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/firma-solay.png"));

        if (extraImages != null) {
            data.putAll(extraImages);
        }

        // 6. Generar el PDF
        byte[] pdfBytes = documentService.generatePdf(templateName, data);

        // 7. Configurar respuesta
        String safeName = (doc.getNombre() != null) ? doc.getNombre().replace(" ", "_") : "Documento";
        String fileName = filePrefix + "_" + safeName + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
