package dev.rodrigovaamonde.unoserver.service;

import dev.rodrigovaamonde.unoserver.model.*;
import dev.rodrigovaamonde.unoserver.repository.GameRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class GameService {
    private final GameRepository gameRepository;
    private static final SecureRandom random = new SecureRandom();
    private static final String ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Transactional
    public Game createGame() {
        String gameCode = generateUniqueGameCode();
        Game game = new Game(gameCode);

        List<Card> deck = initializeDeck();
        Collections.shuffle(deck);

        game.setDrawPile(deck);
        return gameRepository.save(game);
    }

    @Transactional
    public Game joinGame(Long gameId, String playerName) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new RuntimeException("Game not found with id: " + gameId));

        if (game.getStatus() != Game.GameStatus.WAITING_FOR_PLAYERS) {
            throw new IllegalStateException("Cannot join a game that is already in progress or finished.");
        }

        boolean playerExists = game.getPlayers().stream().anyMatch(p -> p.getName().equalsIgnoreCase(playerName));
        if (playerExists) {
            throw new IllegalStateException("A player with the name '" + playerName + "' is already in this game");
        }

        Player newPlayer = new Player(playerName);

        game.addPlayer(newPlayer);

        return gameRepository.save(game);
    }

    private String generateUniqueGameCode() {
        String code;
        do {
            code = generateRandomCode(6);
        } while (gameRepository.findByGameCode(code).isPresent());
        return code;
    }

    private String generateRandomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC_CHARS.charAt(random.nextInt(ALPHANUMERIC_CHARS.length())));
        }
        return sb.toString();
    }

    private List<Card> initializeDeck() {
        List<Card> deck = new ArrayList<>();

        // Cartas de colores (Rojo, Verde, Azul, Amarillo)
        for (Color color : new Color[]{Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW}) {
            // Una carta de '0' por color
            deck.add(new Card(null, color, CardValue.ZERO));

            // Dos cartas del '1' al '9' por color
            for (CardValue value : new CardValue[]{CardValue.ONE, CardValue.TWO, CardValue.THREE, CardValue.FOUR, CardValue.FIVE, CardValue.SIX, CardValue.SEVEN, CardValue.EIGHT, CardValue.NINE}) {
                deck.add(new Card(null, color, value));
                deck.add(new Card(null, color, value));
            }

            // Dos cartas de acción por color
            for (CardValue value : new CardValue[]{CardValue.SKIP, CardValue.REVERSE, CardValue.DRAW_TWO}) {
                deck.add(new Card(null, color, value));
                deck.add(new Card(null, color, value));
            }
        }

        // Cartas comodín (negras)
        IntStream.range(0, 4).forEach(i -> {
            deck.add(new Card(null, Color.BLACK, CardValue.WILD));
            deck.add(new Card(null, Color.BLACK, CardValue.WILD_DRAW_FOUR));
        });

        return deck;
    }
}
