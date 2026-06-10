package com.proyecto.document_api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Controlador para documentos de proyecto.
 * Genera el Anexo II: Planos ensamblando tiles de OpenStreetMap para
 * producir dos imágenes: Plano de Situación (zoom lejano) y Plano de
 * Emplazamiento (zoom cercano), sin necesitar API key externa.
 */
@RestController
@RequestMapping("/api/v1/proyectos")
@Tag(name = "Proyectos", description = "Endpoints para documentos de proyectos y planos OSM")
public class ProyectoController {

    @Autowired private DocumentService documentService;
    @Autowired private DocumentConfigService documentConfigService;
    @Autowired private DocumentRepository documentRepository;
    @Autowired private JsonUtils jsonUtils;

    private static final int TILE_SIZE            = 256;
    private static final int GRID                 = 3;   // cuadrícula 3×3 tiles → imagen 768×768 px
    private static final int ZOOM_SITUACION       = 18;  // vista media (antiguo emplazamiento)
    private static final int ZOOM_EMPLAZAMIENTO   = 20;  // vista muy cercana (detalle cubierta)
    // ESRI World Imagery: satélite real sin API key. Nota: formato {z}/{y}/{x} (y antes que x)
    private static final String ESRI_URL = "https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/%d/%d/%d";

    // =====================================================================
    // ENDPOINT PRINCIPAL: Anexo II – Planos
    // =====================================================================

    @Operation(summary = "Genera el Anexo II: Planos con mapas OSM automáticos")
    @GetMapping("/anexo-planos/{id}")
    public ResponseEntity<byte[]> generateAnexoPlanos(@PathVariable UUID id) {

        DocumentEntity doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe el registro con ID: " + id));

        Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
        Map<String, String> extraImages = new HashMap<>();

        // --- 1. Intentar cargar planos subidos manualmente ---
        mapImageFallback(extraImages, formData, "situacionBase64", "otros_imagenPlanoSituacion", "situacionBase64");
        mapImageFallback(extraImages, formData, "emplazamientoBase64", "otros_imagenPlanoEmplazamiento", "emplazamientoBase64");

        // --- 2. Si faltan planos, generarlos automáticamente (si hay dirección) ---
        if (!extraImages.containsKey("situacionBase64") || !extraImages.containsKey("emplazamientoBase64")) {
            double[] coords = getCoordinates(formData);
            if (coords != null) {
                if (!extraImages.containsKey("situacionBase64")) {
                    String situacion = buildOsmMap(coords[0], coords[1], ZOOM_SITUACION);
                    if (situacion != null) extraImages.put("situacionBase64", situacion);
                }
                if (!extraImages.containsKey("emplazamientoBase64")) {
                    String emplazamiento = buildOsmMap(coords[0], coords[1], ZOOM_EMPLAZAMIENTO);
                    if (emplazamiento != null) extraImages.put("emplazamientoBase64", emplazamiento);
                }
            }
        }

        // --- Planos subidos manualmente (Layout, Strings y Esquema Unifilar) ---
        mapImageFallback(extraImages, formData, "layoutBase64",  "otros_imagenPlanoLayout",   "layout");
        mapImageFallback(extraImages, formData, "stringsBase64", "otros_imagenPlanoStrings",  "strings");
        mapImageFallback(extraImages, formData, "unifilarBase64", "h_esquemaUnifilar", "esquemaUnifilar", "otros_imagenPlanoUnifilar", "unifilar");

        // --- Ensamblar y generar PDF ---
        Map<String, Object> enriched = documentConfigService.enrich("planos", formData);
        Map<String, Object> data = new HashMap<>();
        data.put("form", enriched);
        data.put("name", doc.getNombre() != null ? doc.getNombre() : "Cliente");
        data.put("logoBase64",  "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/logo-solay.png"));
        data.put("firmaBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/firma-solay.png"));
        data.putAll(extraImages);

        byte[] pdf = documentService.generatePdf("proyectos/AnexoPlanos", data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("Anexo II - Planos_" + (doc.getNombre() != null ? doc.getNombre() : "Proyecto") + ".pdf")
                .build());
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @Operation(summary = "Genera el Anexo VII: Estudio de Gestión de Residuos")
    @GetMapping("/estudio-residuos/{id}")
    public ResponseEntity<byte[]> generateEstudioResiduos(@PathVariable UUID id) {
        DocumentEntity doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe el registro con ID: " + id));

        Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
        Map<String, Object> enriched = documentConfigService.enrich("EstudioGestionResiduos", formData);

        Map<String, Object> data = new HashMap<>();
        data.put("form", enriched);
        data.put("name", doc.getNombre() != null ? doc.getNombre() : "Cliente");
        data.put("logoBase64",  "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/logo-solay.png"));
        data.put("firmaBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/firma-solay.png"));

        byte[] pdf = documentService.generatePdf("proyectos/EstudioGestionResiduos", data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("Estudio de Gestion de Residuos_" + (doc.getNombre() != null ? doc.getNombre() : "Proyecto") + ".pdf")
                .build());
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @Operation(summary = "Genera el Anexo III: Mediciones y Presupuesto")
    @GetMapping("/mediciones-presupuesto/{id}")
    public ResponseEntity<byte[]> generateMedicionesPresupuesto(@PathVariable UUID id) {
        DocumentEntity doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe el registro con ID: " + id));

        Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
        Map<String, Object> enriched = documentConfigService.enrich("MedicionesPresupuesto", formData);

        Map<String, Object> data = new HashMap<>();
        data.put("form", enriched);
        data.put("name", doc.getNombre() != null ? doc.getNombre() : "Cliente");
        data.put("logoBase64",  "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/logo-solay.png"));
        data.put("firmaBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/firma-solay.png"));

        byte[] pdf = documentService.generatePdf("proyectos/MedicionesPresupuesto", data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("Anexo III - Mediciones y Presupuesto_" + (doc.getNombre() != null ? doc.getNombre() : "Proyecto") + ".pdf")
                .build());
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @Operation(summary = "Genera el documento 13: Cálculo PEM")
    @GetMapping("/calculo-pem/{id}")
    public ResponseEntity<byte[]> generateCalculoPem(@PathVariable UUID id) {
        DocumentEntity doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe el registro con ID: " + id));

        Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
        Map<String, Object> enriched = documentConfigService.enrich("CalculoPem", formData);

        Map<String, Object> data = new HashMap<>();
        data.put("form", enriched);
        data.put("name", doc.getNombre() != null ? doc.getNombre() : "Cliente");
        data.put("logoBase64",  "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/logo-solay.png"));
        data.put("firmaBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/firma-solay.png"));

        byte[] pdf = documentService.generatePdf("proyectos/CalculoPem", data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("Calculo PEM_" + (doc.getNombre() != null ? doc.getNombre() : "Proyecto") + ".pdf")
                .build());
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @Operation(summary = "Genera el Anexo V: Pliego de Prescripciones Técnicas Particulares (PPTP)")
    @GetMapping("/pptp/{id}")
    public ResponseEntity<byte[]> generatePPTP(@PathVariable UUID id) {
        DocumentEntity doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe el registro con ID: " + id));

        Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
        Map<String, Object> enriched = documentConfigService.enrich("PPTP", formData);

        Map<String, Object> data = new HashMap<>();
        data.put("form", enriched);
        data.put("name", doc.getNombre() != null ? doc.getNombre() : "Cliente");
        data.put("logoBase64",  "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/logo-solay.png"));
        data.put("firmaBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/firma-solay.png"));

        byte[] pdf = documentService.generatePdf("proyectos/PPTP", data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("Anexo V - Pliego de Condiciones PPT_" + (doc.getNombre() != null ? doc.getNombre() : "Proyecto") + ".pdf")
                .build());
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @Operation(summary = "Genera el Anexo I: Cálculos Eléctricos y Energéticos")
    @GetMapping("/anexo-calculos/{id}")
    public ResponseEntity<byte[]> generateAnexoCalculos(@PathVariable UUID id) {
        DocumentEntity doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe el registro con ID: " + id));

        Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
        Map<String, Object> enriched = documentConfigService.enrich("Calculos", formData);

        Map<String, Object> data = new HashMap<>();
        data.put("form", enriched);
        data.put("name", doc.getNombre() != null ? doc.getNombre() : "Cliente");
        data.put("logoBase64",  "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/logo-solay.png"));
        data.put("firmaBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/firma-solay.png"));

        byte[] pdf = documentService.generatePdf("proyectos/Calculos", data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("Anexo I - Calculos_" + (doc.getNombre() != null ? doc.getNombre() : "Proyecto") + ".pdf")
                .build());
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @Operation(summary = "Genera el Anexo IV: Estudio Básico de Seguridad y Salud (EBSS)")
    @GetMapping("/anexo-ebss/{id}")
    public ResponseEntity<byte[]> generateAnexoEbss(@PathVariable UUID id) {
        DocumentEntity doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe el registro con ID: " + id));

        Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
        Map<String, Object> enriched = documentConfigService.enrich("EBSS", formData);

        Map<String, Object> data = new HashMap<>();
        data.put("form", enriched);
        data.put("name", doc.getNombre() != null ? doc.getNombre() : "Cliente");
        data.put("logoBase64",  "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/logo-solay.png"));
        data.put("firmaBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/firma-solay.png"));

        byte[] pdf = documentService.generatePdf("proyectos/EBSS", data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("Anexo IV - Estudio Basico SYS_" + (doc.getNombre() != null ? doc.getNombre() : "Proyecto") + ".pdf")
                .build());
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @Operation(summary = "Genera el Anexo VI: Fichas Técnicas de Equipos")
    @GetMapping("/anexo-fichas-tecnicas/{id}")
    public ResponseEntity<byte[]> generateAnexoFichasTecnicas(@PathVariable UUID id) {
        DocumentEntity doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe el registro con ID: " + id));

        Map<String, Object> formData = jsonUtils.parseJsonToMap(doc.getFormulario());
        Map<String, Object> enriched = documentConfigService.enrich("FichasTecnicas", formData);

        Map<String, Object> data = new HashMap<>();
        data.put("form", enriched);
        data.put("name", doc.getNombre() != null ? doc.getNombre() : "Cliente");
        data.put("logoBase64",  "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/logo-solay.png"));
        data.put("firmaBase64", "data:image/png;base64," + jsonUtils.getResourceAsBase64("static/firma-solay.png"));

        byte[] pdf = documentService.generatePdf("proyectos/FichasTecnicas", data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("Anexo VI - Fichas Tecnicas_" + (doc.getNombre() != null ? doc.getNombre() : "Proyecto") + ".pdf")
                .build());
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    // =====================================================================
    // ENDPOINT AUXILIAR: previsualización de mapa
    // =====================================================================

    @Operation(summary = "Genera planos OSM (situación y emplazamiento) a partir de una dirección")
    @GetMapping("/generar-mapa")
    public ResponseEntity<Map<String, String>> generateMapa(
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) String localidad,
            @RequestParam(required = false) String provincia) {

        Map<String, String> response = new HashMap<>();
        double[] coords = geocodeAddress(buildQuery(direccion, localidad, provincia));
        if (coords != null) {
            String s = buildOsmMap(coords[0], coords[1], ZOOM_SITUACION);
            String e = buildOsmMap(coords[0], coords[1], ZOOM_EMPLAZAMIENTO);
            if (s != null) response.put("situacionBase64",    s);
            if (e != null) response.put("emplazamientoBase64", e);
            return ResponseEntity.ok(response);
        }
        response.put("error", "No se pudo geocodificar la dirección.");
        return ResponseEntity.badRequest().body(response);
    }

    // =====================================================================
    // OSM – ENSAMBLADO DE TILES
    // =====================================================================

    /**
     * Descarga una cuadrícula GRID×GRID de tiles OSM centrada en (lat,lon)
     * y los ensambla en una sola imagen PNG codificada en Base64.
     */
    private String buildOsmMap(double lat, double lon, int zoom) {
        try {
            int cx = tileX(lon, zoom);
            int cy = tileY(lat, zoom);
            int half = GRID / 2;
            int imgSize = TILE_SIZE * GRID;

            BufferedImage canvas = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = canvas.createGraphics();
            g.setColor(new Color(230, 230, 230));
            g.fillRect(0, 0, imgSize, imgSize);

            int maxTile = 1 << zoom;
            for (int dy = -half; dy <= half; dy++) {
                for (int dx = -half; dx <= half; dx++) {
                    int tx = cx + dx;
                    int ty = cy + dy;
                    if (tx < 0 || ty < 0 || tx >= maxTile || ty >= maxTile) continue;

                    byte[] tileBytes = downloadEsriTile(zoom, tx, ty);
                    if (tileBytes != null) {
                        BufferedImage tile = ImageIO.read(new ByteArrayInputStream(tileBytes));
                        if (tile != null) {
                            g.drawImage(tile, (dx + half) * TILE_SIZE, (dy + half) * TILE_SIZE, null);
                        }
                    }
                }
            }

            // Marcador en el centro
            int mid = imgSize / 2;
            g.setColor(Color.RED);
            g.setStroke(new BasicStroke(2.5f));
            g.drawLine(mid - 14, mid, mid + 14, mid);
            g.drawLine(mid, mid - 14, mid, mid + 14);
            g.fillOval(mid - 5, mid - 5, 10, 10);

            g.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(canvas, "PNG", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());

        } catch (Exception e) {
            System.err.println("[ProyectoController] Error OSM zoom=" + zoom + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Descarga un tile de ESRI World Imagery (satélite).
     * IMPORTANTE: ESRI usa formato {z}/{y}/{x} — y y x están invertidos respecto a OSM.
     */
    private byte[] downloadEsriTile(int zoom, int x, int y) {
        try {
            // ESRI: z / y / x  (invertido respecto a OSM z/x/y)
            String url = String.format(ESRI_URL, zoom, y, x);
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("User-Agent", "SolayIngenieros/1.0");
            conn.setRequestProperty("Referer", "https://www.arcgis.com");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            if (conn.getResponseCode() == 200) {
                try (InputStream in = conn.getInputStream();
                     ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    byte[] buf = new byte[4096];
                    int n;
                    while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
                    return out.toByteArray();
                }
            }
        } catch (Exception e) {
            System.err.println("[ProyectoController] ESRI tile z=" + zoom + " x=" + x + " y=" + y + ": " + e.getMessage());
        }
        return null;
    }

    // =====================================================================
    // FÓRMULAS MERCATOR → TILE
    // =====================================================================

    private int tileX(double lon, int zoom) {
        return (int) Math.floor((lon + 180.0) / 360.0 * (1 << zoom));
    }

    private int tileY(double lat, int zoom) {
        double r = Math.toRadians(lat);
        return (int) Math.floor((1.0 - Math.log(Math.tan(r) + 1.0 / Math.cos(r)) / Math.PI) / 2.0 * (1 << zoom));
    }

    // =====================================================================
    // GEOCODIFICACIÓN (CATASTRO -> NOMINATIM)
    // =====================================================================

    private double[] getCoordinates(Map<String, Object> form) {
        // 1. Intentar con Referencia Catastral (es lo más exacto)
        String ref = getString(form, "referenciaCatastral", "referencia_catastral", "rc");
        if (!ref.isEmpty()) {
            double[] catastroCoords = geocodeCatastro(ref);
            if (catastroCoords != null) return catastroCoords;
        }

        // 2. Si no hay ref. catastral o falla, intentar con dirección (Nominatim)
        String dir = getString(form, "direccion", "emplazamientoCalle", "direccionCompleta", "direccion_instalacion");
        String loc = getString(form, "localidad", "localidadEmplazamiento");
        String pro = getString(form, "provincia", "provinciaEmplazamiento");
        if (!dir.isEmpty() || !loc.isEmpty()) {
            return geocodeAddress(buildQuery(dir, loc, pro));
        }
        return null;
    }

    /**
     * Consulta la API pública del Catastro Español para obtener lat/lon a partir de la RC
     */
    private double[] geocodeCatastro(String refCatastral) {
        try {
            // Eliminar espacios de la referencia (a veces los copian con espacios)
            refCatastral = refCatastral.replaceAll("\\s", "");
            if (refCatastral.length() < 14) return null;
            
            // La API de Catastro para coordenadas SOLO acepta la referencia de la parcela (primeros 14 caracteres).
            // Si mandamos la de 20 (incluye inmueble/puerta), devuelve error 18.
            String rc14 = refCatastral.substring(0, 14);

            String url = "http://ovc.catastro.meh.es/ovcservweb/OVCSWLocalizacionRC/OVCCoordenadas.asmx/Consulta_CPMRC?Provincia=&Municipio=&SRS=EPSG:4326&RC=" + rc14;
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);
            if (conn.getResponseCode() == 200) {
                try (InputStream in = conn.getInputStream();
                     ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    byte[] buf = new byte[4096];
                    int n;
                    while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
                    String xml = out.toString(StandardCharsets.UTF_8.name());
                    
                    // Parseo rudimentario de XML para extraer <xcen> y <ycen> (EPSG:4326 -> lon, lat)
                    if (xml.contains("<xcen>") && xml.contains("<ycen>")) {
                        String xStr = xml.substring(xml.indexOf("<xcen>") + 6, xml.indexOf("</xcen>"));
                        String yStr = xml.substring(xml.indexOf("<ycen>") + 6, xml.indexOf("</ycen>"));
                        return new double[]{ Double.parseDouble(yStr), Double.parseDouble(xStr) }; // [lat, lon]
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[ProyectoController] Catastro error: " + e.getMessage());
        }
        return null;
    }

    private double[] geocodeAddress(String query) {
        try {
            String url = "https://nominatim.openstreetmap.org/search?q="
                    + URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
                    + "&format=json&limit=1&countrycodes=es";
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("User-Agent", "SolayIngenieros-PlanosTecnicos/1.0 (contact@solay.es)");
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);
            if (conn.getResponseCode() == 200) {
                try (InputStream in = conn.getInputStream();
                     ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    byte[] buf = new byte[4096];
                    int n;
                    while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
                    JsonNode root = new ObjectMapper().readTree(out.toString(StandardCharsets.UTF_8.name()));
                    if (root.isArray() && root.size() > 0) {
                        JsonNode f = root.get(0);
                        return new double[]{ f.get("lat").asDouble(), f.get("lon").asDouble() };
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[ProyectoController] Nominatim error: " + e.getMessage());
        }
        return null;
    }

    private String buildQuery(String dir, String loc, String pro) {
        StringBuilder sb = new StringBuilder();
        if (dir != null && !dir.isBlank()) sb.append(dir.trim());
        if (loc != null && !loc.isBlank()) { if (sb.length() > 0) sb.append(", "); sb.append(loc.trim()); }
        if (pro != null && !pro.isBlank()) { if (sb.length() > 0) sb.append(", "); sb.append(pro.trim()); }
        sb.append(", España");
        return sb.toString();
    }

    // =====================================================================
    // HELPERS
    // =====================================================================

    private String getString(Map<String, Object> form, String... keys) {
        for (String k : keys) {
            Object v = form.get(k);
            if (v != null && !v.toString().isBlank()) return v.toString().trim();
        }
        return "";
    }

    private String downloadUrlAsBase64(String urlString) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
            conn.setRequestProperty("User-Agent", "SolayIngenieros/1.0");
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);
            if (conn.getResponseCode() == 200) {
                try (InputStream in = conn.getInputStream();
                     ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    byte[] buf = new byte[4096];
                    int n;
                    while ((n = in.read(buf)) != -1) {
                        out.write(buf, 0, n);
                    }
                    byte[] bytes = out.toByteArray();
                    String contentType = conn.getContentType();
                    if (contentType == null) contentType = "image/png";
                    return "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(bytes);
                }
            }
        } catch (Exception e) {
            System.err.println("[ProyectoController] Error descargando URL de plano: " + urlString + " - " + e.getMessage());
        }
        return null;
    }

    private void mapImageFallback(Map<String, String> target, Map<String, Object> form,
                                  String key, String... sourceKeys) {
        for (String sk : sourceKeys) {
            Object v = form.get(sk);
            if (v != null && !v.toString().isEmpty()) {
                String val = v.toString().trim();
                // Si es una URL (por ejemplo de Catastro), la descargamos y convertimos a Base64 en caliente
                if (val.startsWith("http")) {
                    String base64 = downloadUrlAsBase64(val);
                    if (base64 != null) {
                        target.put(key, base64);
                        return;
                    }
                    continue;
                }
                
                if (!val.startsWith("data:image")) val = "data:image/png;base64," + val;
                target.put(key, val);
                return;
            }
        }
    }
}
