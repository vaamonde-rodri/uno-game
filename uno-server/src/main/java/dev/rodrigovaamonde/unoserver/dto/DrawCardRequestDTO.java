package dev.rodrigovaamonde.unoserver.dto;

/**
 * DTO para la solicitud de robar una carta del mazo.
 * @param playerId El ID del jugador que realiza la acción.
 */
public record DrawCardRequestDTO(
    Long playerId
) {}