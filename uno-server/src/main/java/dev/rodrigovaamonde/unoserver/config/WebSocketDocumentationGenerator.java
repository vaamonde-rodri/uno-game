package dev.rodrigovaamonde.unoserver.config;

import dev.rodrigovaamonde.unoserver.annotation.WebSocketOperation;
import dev.rodrigovaamonde.unoserver.annotation.WebSocketParam;
import dev.rodrigovaamonde.unoserver.annotation.WebSocketResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * Generador de documentación para endpoints WebSocket
 * Similar a como Swagger genera documentación para REST APIs
 */
@Component
@Slf4j
public class WebSocketDocumentationGenerator implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private final Map<String, WebSocketEndpointDoc> endpoints = new HashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void generateDocumentation() {
        log.info("Generando documentación de WebSocket endpoints...");

        // Buscar solo los controllers (evita dependencias circulares)
        Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(Controller.class);

        for (Object controller : controllers.values()) {
            Class<?> clazz = controller.getClass();

            for (Method method : clazz.getDeclaredMethods()) {
                MessageMapping messageMapping = method.getAnnotation(MessageMapping.class);
                if (messageMapping != null) {
                    processWebSocketEndpoint(method, messageMapping);
                }
            }
        }

        logDocumentation();
    }

    private void processWebSocketEndpoint(Method method, MessageMapping messageMapping) {
        WebSocketOperation operation = method.getAnnotation(WebSocketOperation.class);
        WebSocketResponse[] responses = method.getAnnotationsByType(WebSocketResponse.class);

        String destination = messageMapping.value().length > 0 ? messageMapping.value()[0] : "";

        WebSocketEndpointDoc doc = new WebSocketEndpointDoc();
        doc.setDestination(destination);
        doc.setMethodName(method.getName());

        if (operation != null) {
            doc.setSummary(operation.summary());
            doc.setDescription(operation.description());
            doc.setTags(Arrays.asList(operation.tags()));
            doc.setResponseChannels(Arrays.asList(operation.responseChannels()));
        }

        // Procesar parámetros
        Parameter[] parameters = method.getParameters();
        for (Parameter param : parameters) {
            WebSocketParam wsParam = param.getAnnotation(WebSocketParam.class);
            if (wsParam != null) {
                ParameterDoc paramDoc = new ParameterDoc();
                paramDoc.setName(wsParam.name().isEmpty() ? param.getName() : wsParam.name());
                paramDoc.setDescription(wsParam.description());
                paramDoc.setRequired(wsParam.required());
                paramDoc.setExample(wsParam.example());
                paramDoc.setType(param.getType().getSimpleName());
                doc.getParameters().add(paramDoc);
            }
        }

        // Procesar respuestas
        for (WebSocketResponse response : responses) {
            ResponseDoc responseDoc = new ResponseDoc();
            responseDoc.setChannel(response.channel());
            responseDoc.setDescription(response.description());
            responseDoc.setContentType(response.content().getSimpleName());
            responseDoc.setBroadcast(response.broadcast());
            doc.getResponses().add(responseDoc);
        }

        endpoints.put(destination, doc);
    }

    private void logDocumentation() {
        log.info("=== DOCUMENTACIÓN DE WEBSOCKET ENDPOINTS ===");

        endpoints.forEach((destination, doc) -> {
            log.info("\n📡 ENDPOINT: {}", destination);
            log.info("   Método: {}", doc.getMethodName());
            log.info("   Resumen: {}", doc.getSummary());
            log.info("   Descripción: {}", doc.getDescription());
            log.info("   Tags: {}", doc.getTags());

            if (!doc.getParameters().isEmpty()) {
                log.info("   📥 PARÁMETROS:");
                doc.getParameters().forEach(param -> {
                    log.info("     • {} ({}): {} {}",
                        param.getName(),
                        param.getType(),
                        param.getDescription(),
                        param.isRequired() ? "[REQUERIDO]" : "[OPCIONAL]"
                    );
                    if (!param.getExample().isEmpty()) {
                        log.info("       Ejemplo: {}", param.getExample());
                    }
                });
            }

            if (!doc.getResponses().isEmpty()) {
                log.info("   📤 RESPUESTAS:");
                doc.getResponses().forEach(response -> {
                    log.info("     • Canal: {}", response.getChannel());
                    log.info("       Tipo: {} ({})",
                        response.getContentType(),
                        response.isBroadcast() ? "Broadcast" : "Unicast"
                    );
                    log.info("       Descripción: {}", response.getDescription());
                });
            }

            log.info("   📨 Canales de respuesta: {}", doc.getResponseChannels());
        });

        log.info("\n=== FIN DOCUMENTACIÓN WEBSOCKET ===");
    }

    public Map<String, WebSocketEndpointDoc> getEndpoints() {
        return endpoints;
    }

    // Clases internas para estructurar la documentación
    public static class WebSocketEndpointDoc {
        private String destination;
        private String methodName;
        private String summary;
        private String description;
        private List<String> tags = new ArrayList<>();
        private List<String> responseChannels = new ArrayList<>();
        private List<ParameterDoc> parameters = new ArrayList<>();
        private List<ResponseDoc> responses = new ArrayList<>();

        // Getters y setters
        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }
        public String getMethodName() { return methodName; }
        public void setMethodName(String methodName) { this.methodName = methodName; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }
        public List<String> getResponseChannels() { return responseChannels; }
        public void setResponseChannels(List<String> responseChannels) { this.responseChannels = responseChannels; }
        public List<ParameterDoc> getParameters() { return parameters; }
        public void setParameters(List<ParameterDoc> parameters) { this.parameters = parameters; }
        public List<ResponseDoc> getResponses() { return responses; }
        public void setResponses(List<ResponseDoc> responses) { this.responses = responses; }
    }

    public static class ParameterDoc {
        private String name;
        private String description;
        private String type;
        private boolean required;
        private String example;

        // Getters y setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }
        public String getExample() { return example; }
        public void setExample(String example) { this.example = example; }
    }

    public static class ResponseDoc {
        private String channel;
        private String description;
        private String contentType;
        private boolean broadcast;

        // Getters y setters
        public String getChannel() { return channel; }
        public void setChannel(String channel) { this.channel = channel; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public boolean isBroadcast() { return broadcast; }
        public void setBroadcast(boolean broadcast) { this.broadcast = broadcast; }
    }
}
