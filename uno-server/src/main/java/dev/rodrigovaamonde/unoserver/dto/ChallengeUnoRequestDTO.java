package dev.rodrigovaamonde.unoserver.dto;

/**
 * DTO para la solicitud de desafiar a un jugador por no cantar "UNO".
 * @param challengerId El ID del jugador que realiza el desafío.
 * @param challengedId El ID del jugador que está siendo desafiado.
 */
public record ChallengeUnoRequestDTO(
    Long challengerId,
    Long challengedId
) {}