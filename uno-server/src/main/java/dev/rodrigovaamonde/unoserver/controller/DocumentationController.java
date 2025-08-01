package dev.rodrigovaamonde.unoserver.controller;

import dev.rodrigovaamonde.unoserver.config.WebSocketDocumentationGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/docs")
@Tag(name = "Documentation", description = "Endpoints para acceder a la documentación de APIs")
public class DocumentationController {

    private final WebSocketDocumentationGenerator wsDocGenerator;

    public DocumentationController(WebSocketDocumentationGenerator wsDocGenerator) {
        this.wsDocGenerator = wsDocGenerator;
    }

    @Operation(
        summary = "Obtener documentación de WebSocket",
        description = "Devuelve la documentación completa de todos los endpoints WebSocket disponibles"
    )
    @GetMapping("/websocket")
    public ResponseEntity<Map<String, WebSocketDocumentationGenerator.WebSocketEndpointDoc>> getWebSocketDocumentation() {
        return ResponseEntity.ok(wsDocGenerator.getEndpoints());
    }

    @Operation(
        summary = "Obtener AsyncAPI spec",
        description = "Devuelve la especificación AsyncAPI en formato JSON"
    )
    @GetMapping("/asyncapi")
    public ResponseEntity<String> getAsyncApiSpec() {
        // En un escenario real, esto podría cargar y devolver el archivo asyncapi.yml
        return ResponseEntity.ok("{\n  \"info\": {\n    \"title\": \"Ver /resources/asyncapi.yml para especificación completa\"\n  }\n}");
    }
}
