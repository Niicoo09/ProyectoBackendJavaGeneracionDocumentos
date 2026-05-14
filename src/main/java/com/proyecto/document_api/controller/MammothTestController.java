package com.proyecto.document_api.controller;

import com.proyecto.document_api.service.MammothService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/test/mammoth")
@RequiredArgsConstructor
@Tag(name = "Mammoth Test", description = "Endpoints para probar la conversión de Word a HTML")
public class MammothTestController {

    private final MammothService mammothService;

    @Operation(summary = "Convierte un archivo DOCX local a HTML")
    @GetMapping(value = "/convert", produces = MediaType.TEXT_HTML_VALUE)
    public String convertFile(@RequestParam String filePath) throws IOException {
        return mammothService.convertToHtml(filePath);
    }
}
