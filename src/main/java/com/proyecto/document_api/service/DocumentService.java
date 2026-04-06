package com.proyecto.document_api.service;

import com.microsoft.playwright.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

/**
 * El cerebro detrás de la generación de PDFs.
 * Utiliza Thymeleaf para rellenar los datos y Playwright para "imprimir" el documento.
 * 
 * @author Nicolas Navarro Contreras
 */
@Service
public class DocumentService {

    @Autowired
    private TemplateEngine templateEngine;

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
}
