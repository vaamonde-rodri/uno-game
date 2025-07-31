package dev.rodrigovaamonde.unoserver.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String gameCode;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Player> players = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_game_id")
    private List<Card> drawPile = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "discard_pile_game_id")
    private List<Card> discardPile = new ArrayList<>();

    @ManyToOne
    @ToString.Exclude
    private Player currentPlayer;

    private boolean isReversed = false;

    @Enumerated(EnumType.STRING)
    private GameStatus status = GameStatus.WAITING_FOR_PLAYERS;

    @Enumerated(EnumType.STRING)
    private Color currentColor;

    public enum GameStatus {
        WAITING_FOR_PLAYERS,
        IN_PROGRESS,
        FINISHED
    }

    public Game(String gameCode) {
        this.gameCode = gameCode;
    }

    public void addPlayer(Player player) {
        players.add(player);
        player.setGame(this);
    }
}
