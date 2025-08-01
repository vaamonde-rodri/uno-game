package dev.rodrigovaamonde.unoserver.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para documentar endpoints de WebSocket de manera similar a @Operation de OpenAPI
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface WebSocketOperation {

    /**
     * Resumen breve de la operación
     */
    String summary() default "";

    /**
     * Descripción detallada de la operación
     */
    String description() default "";

    /**
     * Canal de destino donde se envía el mensaje
     */
    String destination() default "";

    /**
     * Canales donde se publican las respuestas
     */
    String[] responseChannels() default {};

    /**
     * Etiquetas para agrupar operaciones
     */
    String[] tags() default {};
}
