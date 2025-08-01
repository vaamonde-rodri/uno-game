package dev.rodrigovaamonde.unoserver.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Color color;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardValue value;

    // Relación con el jugador que tiene la carta en su mano
    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;

    // Relación con el juego cuando la carta está en el mazo de robo
    @ManyToOne
    @JoinColumn(name = "deck_game_id")
    private Game deckGame;

    // Relación con el juego cuando la carta está en la pila de descarte
    @ManyToOne
    @JoinColumn(name = "discard_pile_game_id")
    private Game discardPileGame;

    public Card(Color color, CardValue value) {
        this.color = color;
        this.value = value;
    }
}
