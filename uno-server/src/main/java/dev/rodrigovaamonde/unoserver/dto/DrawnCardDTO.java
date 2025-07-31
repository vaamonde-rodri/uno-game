package dev.rodrigovaamonde.unoserver.dto;

import dev.rodrigovaamonde.unoserver.model.Card;

/**
 * DTO para enviar la información de una carta recién robada a un jugador específico.
 * @param card La carta que ha sido robada.
 * @param isPlayable Indica si la carta puede ser jugada inmediatamente.
 */
public record DrawnCardDTO(
    Card card,
    boolean isPlayable
) {}