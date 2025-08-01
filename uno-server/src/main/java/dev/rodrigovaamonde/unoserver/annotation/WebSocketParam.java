package dev.rodrigovaamonde.unoserver.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para documentar parámetros de WebSocket
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface WebSocketParam {

    /**
     * Nombre del parámetro
     */
    String name() default "";

    /**
     * Descripción del parámetro
     */
    String description() default "";

    /**
     * Si el parámetro es requerido
     */
    boolean required() default true;

    /**
     * Ejemplo de valor para el parámetro
     */
    String example() default "";
}
