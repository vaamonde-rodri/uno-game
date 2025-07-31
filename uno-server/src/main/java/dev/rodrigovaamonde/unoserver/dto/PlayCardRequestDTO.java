package dev.rodrigovaamonde.unoserver.dto;

import dev.rodrigovaamonde.unoserver.model.Color;

/**
 * DTO para la solicitud de jugar una carta.
 *
 * @param playerId    El ID del jugador que realiza la jugada.
 * @param cardId      El ID de la carta que se juega.
 * @param chosenColor El color elegido si la carta es un comodín (WILD), si no, es null.
 */
public record PlayCardRequestDTO(
    Long playerId,
    Long cardId,
    Color chosenColor
) {
}
