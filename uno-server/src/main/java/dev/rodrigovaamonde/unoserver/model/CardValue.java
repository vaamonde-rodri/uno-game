package dev.rodrigovaamonde.unoserver.model;

import lombok.Getter;

@Getter
public enum CardValue {
    ZERO(0),
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),

    SKIP(20),
    REVERSE(20),
    DRAW_TWO(20),

    WILD(50),
    WILD_DRAW_FOUR(50);

    private final int points;

    CardValue(int points) {
        this.points = points;
    }
}
