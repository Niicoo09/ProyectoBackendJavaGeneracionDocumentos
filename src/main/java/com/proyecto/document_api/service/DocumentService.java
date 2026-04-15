package com.proyecto.document_api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.document_api.model.DocumentEntity;
import com.proyecto.document_api.repository.DocumentRepository;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * El cerebro detrás de la generación de PDFs y la persistencia de datos.
 * Utiliza Thymeleaf para rellenar los datos, Playwright para "imprimir" e JPA para guardar cambios.
 * 
 * @author Nicolas Navarro Contreras
 */
@Service
public class DocumentService {

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private DocumentRepository documentRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Genera un PDF a partir de una plantilla HTML y un mapa de datos dinámicos.
     * 
     * @param templateName Nombre del archivo HTML en /resources/templates/ (sin .html)
     * @param data Mapa de datos que contiene la información del cliente para rellenar
     * @return Arreglo de bytes que representa el archivo PDF final
     */
    public byte[] generatePdf(String templateName, Map<String, Object> data) {
        
        // PASO 1: Preparación del contexto para Thymeleaf
        // El 'Context' es como una maleta donde metemos todos los datos (nombre, NIF, etc.)
        // que queremos que aparezcan en el documento final.
        Context context = new Context();
        context.setVariables(data);
        
        // PASO 2: Renderizado del HTML
        // El TemplateEngine toma nuestro archivo HTML físico y sustituye las etiquetas 
        // especiales (th:text) por los datos reales de la base de datos.
        String htmlContent = templateEngine.process(templateName, context);

        // PASO 3: Conversión de HTML a PDF usando Playwright
        // Abrimos un "Playwright", que es como un motor de navegador invisible (Headless)
        try (Playwright playwright = Playwright.create()) {
            
            // Lanzamos un navegador Chromium (el mismo motor que Google Chrome)
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            
            // Creamos una nueva página en el navegador
            BrowserContext browserContext = browser.newContext();
            Page page = browserContext.newPage();
            
            // PASO 4: Carga del contenido
            // Inyectamos el HTML que generamos en el PASO 2 directamente en la página del navegador
            page.setContent(htmlContent);
            
            // Generar el PDF
            byte[] pdfBytes = page.pdf(new Page.PdfOptions()
                    .setFormat("A4")
                    .setPrintBackground(true));
            
            browser.close();
            return pdfBytes;
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF con Playwright: " + e.getMessage(), e);
        }
    }

    /**
     * Fusiona varios archivos PDF (en formato byte[]) en un único PDF.
     * 
     * @param pdfs Lista de arreglos de bytes de los PDFs a fusionar, en orden.
     * @return Arreglo de bytes del PDF resultante de la fusión.
     */
    public byte[] mergePdfs(List<byte[]> pdfs) {
        PDFMergerUtility ut = new PDFMergerUtility();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Añadir cada PDF a la utilidad de fusión
            for (byte[] pdf : pdfs) {
                if (pdf != null && pdf.length > 0) {
                    ut.addSource(new RandomAccessReadBuffer(pdf));
                }
            }
            // Definir el destino y realizar la fusión
            ut.setDestinationStream(out);
            ut.mergeDocuments(null);
            
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al fusionar documentos PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Recupera un documento por ID y parsea su JSON interno a un Mapa.
     * Útil para rellenar el MasterForm en modo edición.
     */
    public Map<String, Object> getDocumentForm(UUID id) {
        DocumentEntity entity = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
        
        try {
            return objectMapper.readValue(entity.getFormulario(), new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al parsear el JSON del cliente: " + e.getMessage());
        }
    }

    /**
     * Guarda o actualiza un cliente.
     * Extrae automáticamente el nombre del JSON para mantener sincronizado el Dashboard.
     */
    public DocumentEntity saveDocument(UUID id, Map<String, Object> formData) {
        DocumentEntity entity;
        
        if (id == null) {
            // Caso: Creación sin ID previo
            entity = new DocumentEntity();
            entity.setId(UUID.randomUUID());
            entity.setCreatedAt(LocalDateTime.now());
        } else {
            // Caso: Intentamos buscarlo. Si no existe, es una creación con un ID proporcionado
            entity = documentRepository.findById(id).orElse(new DocumentEntity());
            if (entity.getId() == null) {
                entity.setId(id);
                entity.setCreatedAt(LocalDateTime.now());
            }
        }
        
        entity.setUpdatedAt(LocalDateTime.now());
        
        // Sincronización automática de nombre
        if (formData.containsKey("apellidosNombre") && formData.get("apellidosNombre") != null) {
            entity.setNombre(formData.get("apellidosNombre").toString());
        }

        try {
            // Guardar el JSON compacto en la base de datos
            entity.setFormulario(objectMapper.writeValueAsString(formData));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al serializar el formulario: " + e.getMessage());
        }

        return documentRepository.save(entity);
    }
}
