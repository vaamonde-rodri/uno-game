package dev.rodrigovaamonde.unoserver.dto;

import dev.rodrigovaamonde.unoserver.model.Card;
import dev.rodrigovaamonde.unoserver.model.CardValue;
import dev.rodrigovaamonde.unoserver.model.Color;

public record CardDTO (
    Color color,
    CardValue value
) {

    public static CardDTO fromEntity(Card card) {
        if (card == null) {
            return null;
        }
        return new CardDTO(card.getColor(), card.getValue());
    }
}
