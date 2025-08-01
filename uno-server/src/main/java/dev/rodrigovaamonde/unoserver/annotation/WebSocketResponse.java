package dev.rodrigovaamonde.unoserver.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para documentar respuestas de WebSocket
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface WebSocketResponse {

    /**
     * Canal donde se envía la respuesta
     */
    String channel();

    /**
     * Descripción de la respuesta
     */
    String description() default "";

    /**
     * Tipo de contenido de la respuesta
     */
    Class<?> content() default Object.class;

    /**
     * Si la respuesta es broadcast (a todos) o unicast (a uno)
     */
    boolean broadcast() default true;
}
