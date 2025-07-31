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
        game.setCurrentColor(firstCard.getColor());

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
            throw new IllegalStateException("Game is not in progress.");
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
            throw new IllegalStateException("Card " + cardToPlay + " cannot be played on top of " + topDiscardCard);
        }

        //5. Ejecutar la jugada
        player.getHand().remove(cardToPlay);
        game.getDiscardPile().add(cardToPlay);

        //6. Aplicar efecto de la carta y determinar el siguiente jugador
        Player nextPlayer = applyCardEffect(game, player, cardToPlay, request.chosenColor());
        game.setCurrentPlayer(nextPlayer);

        //7. Guardar y notificar el cambio
        Game updatedGame = gameRepository.save(game);
        notifyGameUpdate(updatedGame);
    }

    @Transactional
    public void drawCard(String gameCode, Long playerId) {
        //1. Encontrar la partida y el jugador
        Game game = gameRepository.findByGameCode(gameCode)
            .orElseThrow(() -> new RuntimeException("Game not found with code: " + gameCode));

        Player player = game.getPlayers().stream()
            .filter(p -> p.getId().equals(playerId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Player not found with id " + playerId + " in game " + gameCode));

        //2. Validaciones
        if (game.getStatus() != Game.GameStatus.IN_PROGRESS) {
            throw new IllegalStateException("Game is not in progress.");
        }
        if (!player.getId().equals(game.getCurrentPlayer().getId())) {
            throw new IllegalStateException("It's not your turn to draw a card.");
        }

        //3. Validar que el jugador realmente no puede jugar ninguna carta
        Card topDiscardCard = game.getDiscardPile().getLast();
        boolean hasPlayableCard = player.getHand().stream()
            .anyMatch(card -> isCardPlayable(card, topDiscardCard, game.getCurrentColor()));
        if (hasPlayableCard) {
            throw new IllegalStateException("You have playable cards in your hand. You must play a card instead of drawing.");
        }

        //4. Robar una carta del mazo
        drawCardsForPlayer(game, player, 1);

        //5. Determinar el siguiente jugador
        game.setCurrentPlayer(determineNextPlayer(game, player, 1));

        //6. Guardar el estado del juego y notificar a los jugadores
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
            deck.add(new Card(color, CardValue.ZERO));

            // Dos cartas del '1' al '9' por color
            for (CardValue value : new CardValue[]{CardValue.ONE, CardValue.TWO, CardValue.THREE, CardValue.FOUR, CardValue.FIVE, CardValue.SIX, CardValue.SEVEN, CardValue.EIGHT, CardValue.NINE}) {
                deck.add(new Card(color, value));
                deck.add(new Card(color, value));
            }

            // Dos cartas de acción por color
            for (CardValue value : new CardValue[]{CardValue.SKIP, CardValue.REVERSE, CardValue.DRAW_TWO}) {
                deck.add(new Card(color, value));
                deck.add(new Card(color, value));
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
        //1. Un comodín (negro) puede jugarse en cualquier momento
        if (cardToPlay.getColor() == Color.BLACK) return true;

        //2. La caarta coincide con el color activo
        //3. O la carta coincide en valor con la caarta suerior de la pila de descarte
        return cardToPlay.getColor() == currentColor || cardToPlay.getValue() == topDiscardCard.getValue();
    }

    private Player applyCardEffect(Game game, Player currentPlayer, Card playedCard, Color chosenColor) {
        //Primero, se actualiza el color del juego
        if (playedCard.getColor() == Color.BLACK) {
            if (chosenColor == null || chosenColor == Color.BLACK) {
                throw new IllegalStateException(
                    "A valid color (RED, GREEN, BLUE, YELLOW) must be chosen when playing a wild card");
            }
            game.setCurrentColor(chosenColor);
        } else {
            game.setCurrentColor(playedCard.getColor());
        }

        Player nextPlayer = determineNextPlayer(game,
            currentPlayer,
            1);  // Por defecto, el siguiente jugador es el que sigue en turno

        switch (playedCard.getValue()) {
            case SKIP:
                // El siguiente jugador pierde su turno
                return determineNextPlayer(game, currentPlayer, 2);
            case REVERSE:
                // Cambia el sentido del juego
                game.setReversed(!game.isReversed());
                // Si solo hay 2 jugadores, REVERSE actúa como SKIP
                if (game.getPlayers().size() == 2) {
                    return determineNextPlayer(game, currentPlayer, 2);
                }
                //Con más de 2 jugadores, el turno va al jugador anterior
                return  determineNextPlayer(game, currentPlayer, 1);
            case DRAW_TWO:
                // El siguiente jugador roba dos cartas y pierde su turno
                drawCardsForPlayer(game, nextPlayer, 2);
                return determineNextPlayer(game, currentPlayer, 2);
            case WILD_DRAW_FOUR:
                // El siguiente jugador roba cuatro cartas y pierde su turno
                drawCardsForPlayer(game, nextPlayer, 4);
                return determineNextPlayer(game, currentPlayer, 2);
            case WILD:
                // Un comodín no tiene efecto especial, solo cambia el color
                // El siguiente jugador es el que sigue en turno
                return nextPlayer;
            default:
                return nextPlayer; // Para cualquier otra carta, simplemente retorna el siguiente jugador
        }
    }

    private Player determineNextPlayer(Game game, Player currentPlayer, int positionsToAdvance) {
        List<Player> players = game.getPlayers();
        int currentPlayerIndex = players.indexOf(currentPlayer);
        int totalPlayers = players.size();


        int direction = game.isReversed() ? -1 : 1;
        int nextPlayerIndex = (currentPlayerIndex + direction * positionsToAdvance + totalPlayers) % totalPlayers;

        if (nextPlayerIndex < 0) {
            nextPlayerIndex += totalPlayers; // Asegura que el índice no sea negativo
        }
        return players.get(nextPlayerIndex);
    }

    private void drawCardsForPlayer(Game game, Player player, int numberOfCards) {
        List<Card> drawPile = game.getDrawPile();

        for (int i = 0; i < numberOfCards; i++) {
            if (drawPile.isEmpty()) {
                //si el mazo de robo está vacío, se baraja la pila de descarte y se convierte en el nuevo mazo de robo
                reshuffleDiscardPile(game);
                // Verificar si después de rebarajar tenemos cartas disponibles
                if (drawPile.isEmpty()) {
                    // No hay más cartas disponibles, terminar el bucle
                    break;
                }
            }
            player.getHand().add(drawPile.removeLast());
        }
    }

    private void reshuffleDiscardPile(Game game) {
        Card topCard = game.getDiscardPile().removeLast(); // Retirar la última carta para no incluirla en el nuevo mazo
        List<Card> newDrawPile = new ArrayList<>(game.getDiscardPile());
        Collections.shuffle(newDrawPile);
        game.setDrawPile(newDrawPile);
        game.getDiscardPile().clear();
        game.getDiscardPile().add(topCard); // Volver a añadir la última carta como la nueva superior de la pila de descarte
    }
}
