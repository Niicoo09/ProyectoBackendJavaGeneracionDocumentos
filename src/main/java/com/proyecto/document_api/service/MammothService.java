package com.proyecto.document_api.service;

import org.springframework.stereotype.Service;
import org.zwobble.mammoth.DocumentConverter;
import org.zwobble.mammoth.Result;

import java.io.File;
import java.io.IOException;

@Service
public class MammothService {

    public String convertToHtml(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File not found: " + filePath);
        }

        DocumentConverter converter = new DocumentConverter()
            .addStyleMap("p[style-name='Heading 1'] => h1:fresh")
            .addStyleMap("p[style-name='Heading 2'] => h2:fresh")
            .addStyleMap("p[style-name='Heading 3'] => h3:fresh")
            .addStyleMap("table => table.table-bordered"); // Basic bootstrap table class

        Result<String> result = converter.convertToHtml(file);
        String html = result.getValue();
        
        // Basic wrapping to see it clearly in the browser
        return "<html><head><style>body{font-family: sans-serif; padding: 20px;} table{border-collapse: collapse; width: 100%;} th, td{border: 1px solid #ddd; padding: 8px;}</style></head><body>" + html + "</body></html>";
    }
}
