package dev.rodrigovaamonde.unoserver.service;

import dev.rodrigovaamonde.unoserver.dto.GameResponseDTO;
import dev.rodrigovaamonde.unoserver.dto.PlayCardRequestDTO;
import dev.rodrigovaamonde.unoserver.model.*;
import dev.rodrigovaamonde.unoserver.repository.GameRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final SimpMessagingTemplate messagingTemplate;
    private static final SecureRandom random = new SecureRandom();
    private static final String ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public GameService(GameRepository gameRepository, SimpMessagingTemplate messagingTemplate) {
        this.gameRepository = gameRepository;
        this.messagingTemplate = messagingTemplate;
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

        Game updatedGame = gameRepository.save(game);
        notifyGameUpdate(updatedGame);

        return updatedGame;
    }

    @Transactional
    public Game startGame(Long gameId) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new RuntimeException("Game not found with id: " + gameId));

        //1. Validaciones
        if (game.getStatus() != Game.GameStatus.WAITING_FOR_PLAYERS) {
            throw new IllegalStateException("Game has already started or is finished");
        }

        if (game.getPlayers().size() < 2) {
            throw new IllegalStateException("Cannot start the game with fewer than 2 players");
        }

        //2. Cambiar estado de la partida
        game.setStatus(Game.GameStatus.IN_PROGRESS);

        //3. Repartir 7 cartas a cada jugador
        List<Card> drawPile = game.getDrawPile();
        for (Player player : game.getPlayers()) {
            for (int i = 0; i < 7; i++) {
                if (drawPile.isEmpty()) {
                    throw new IllegalStateException("The deck ran out of cards during initial deal.");
                }
                player.getHand().add(drawPile.removeLast());
            }
        }

        //4. Poner la primera carta en la pila de decarte
        Card firstCard;
        do {
            firstCard = drawPile.removeLast();
            //Regla oficial: Si la primera carta es un Comodón +4, se devuelve al mazo y se baraja
            if (firstCard.getValue() == CardValue.WILD_DRAW_FOUR) {
                drawPile.add(firstCard);
                Collections.shuffle(drawPile);
            }
        } while (firstCard.getValue() == CardValue.WILD_DRAW_FOUR);

        game.getDiscardPile().add(firstCard);

        //TODO: Aplicaar el efecto de la primera carta si es de acción (Saltar, Reversa, +2)

        //5. Establecer el primer jugador
        game.setCurrentPlayer(game.getPlayers().getFirst());

        Game startedGame = gameRepository.save(game);
        notifyGameUpdate(startedGame);

        return startedGame;
    }

    @Transactional
    public void playCard(String gameCode, PlayCardRequestDTO request) {
        //1. Buscar la partida por el código
        Game game = gameRepository.findByGameCode(gameCode)
            .orElseThrow(() -> new RuntimeException("Game not found with code: " + gameCode));

        //2. Validar el estado de la partida
        if (game.getStatus() != Game.GameStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot play a card in a game that is not in progress.");
        }

        //3. Encontrar al jugador y la carta en su mano
        Player player = game.getPlayers().stream()
            .filter(p -> p.getId().equals(request.playerId()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Player not found with id " + request.playerId() + " in game " + gameCode));

        Card cardToPlay = player.getHand().stream()
            .filter(card -> card.getId() != null && card.getId().equals(request.cardId()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Card not found with id " + request.cardId() + " in player's hand"));

        //4. Validar la jugada
        if (!player.getId().equals(game.getCurrentPlayer().getId())) {
            throw new IllegalStateException("It's not your turn to play.");
        }

        Card topDiscardCard = game.getDiscardPile().getLast();
        if (!isCardPlayable(cardToPlay, topDiscardCard, game.getCurrentColor())) {
            throw new IllegalStateException("Card cannot be played on top of " + topDiscardCard);
        }

        //5. Ejecutar la jugada
        player.getHand().remove(cardToPlay);
        game.getDiscardPile().add(cardToPlay);

        //TODO: Implentar la lógica completa de los efectos de las cartas jugadas

        //5.1. Manejar efectos especiales de las cartas
        if (cardToPlay.getValue() == CardValue.WILD || cardToPlay.getValue() == CardValue.WILD_DRAW_FOUR) {
            // Para cartas comodín, cambiar el color del juego al especificado en el request
            if (request.chosenColor() != null) {
                game.setCurrentColor(request.chosenColor());
            }
        } else {
            // Para cartas normales, el color actual es el color de la carta jugada
            game.setCurrentColor(cardToPlay.getColor());
        }

        //6. Determinar el siguiente jugador (lógica simple por ahora)
        int currentPlayerIndex = game.getPlayers().indexOf(game.getCurrentPlayer());
        int nextPlayerIndex = (currentPlayerIndex + 1) % game.getPlayers().size();
        game.setCurrentPlayer(game.getPlayers().get(nextPlayerIndex));

        //7. Guardar y notificar el cambio
        Game updatedGame = gameRepository.save(game);
        notifyGameUpdate(updatedGame);
    }

    private void notifyGameUpdate(Game game) {
        if (game == null) {
            return;
        }
        String destination = "/topic/" + game.getGameCode();
        GameResponseDTO gameResponse = GameResponseDTO.fromEntity(game);
        messagingTemplate.convertAndSend(destination, gameResponse);
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

    private boolean isCardPlayable(Card cardToPlay, Card topDiscardCard, Color currentColor) {
        //Un comodín simple se puede jugar
        if (cardToPlay.getValue() == CardValue.WILD) {
            return true;
        }

        //Si el color activo coincide (por un comodín previo)
        if (cardToPlay.getColor() == currentColor) {
            return true;
        }

        //La carta coincide en color o valor con la carta física de la pila
        return cardToPlay.getColor() == topDiscardCard.getColor() || cardToPlay.getValue() == topDiscardCard.getValue();
    }
}
