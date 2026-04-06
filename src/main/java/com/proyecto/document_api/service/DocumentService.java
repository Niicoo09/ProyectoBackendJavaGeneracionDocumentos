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
     * Genera un PDF a partir de una plantilla HTML y un mapa de datos.
     * @param templateName Nombre de la plantilla en /resources/templates/ (sin .html)
     * @param data Mapa con las variables para Thymeleaf
     * @return El contenido del PDF en bytes
     */
    public byte[] generatePdf(String templateName, Map<String, Object> data) {
        // 1. Renderizar el HTML con Thymeleaf
        Context context = new Context();
        context.setVariables(data);
        String htmlContent = templateEngine.process(templateName, context);

        // 2. Convertir el HTML a PDF usando Playwright
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext browserContext = browser.newContext();
            Page page = browserContext.newPage();
            
            // Establecer el contenido HTML directamente
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
