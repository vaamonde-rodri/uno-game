package dev.rodrigovaamonde.unoserver.dto;

/**
 * DTO genérico para acciones de un jugador que solo requieren su ID.
 * Se usará para "pasar turno" y "cantar UNO".
 * @param playerId El ID del jugador que realiza la acción.
 */
public record PlayerActionDTO(
    Long playerId
) {}