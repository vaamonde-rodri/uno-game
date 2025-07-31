package dev.rodrigovaamonde.unoserver.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id")
    private List<Player> players = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_game_id")
    private List<Card> deck = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "discard_pile_game_id")
    private List<Card> discard = new ArrayList<>();

    @ManyToOne
    private Player currentPlayer;

    private boolean isReversed = false;

    @Enumerated(EnumType.STRING)
    private GameStatus status = GameStatus.WAITING_FOR_PLAYERS;

    public enum GameStatus {
        WAITING_FOR_PLAYERS,
        IN_PROGRESS,
        FINISHED
    }

    public Game(String gameCode) {
        this.gameCode = gameCode;
    }

}
