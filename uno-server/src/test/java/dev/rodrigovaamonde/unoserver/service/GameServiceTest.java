package dev.rodrigovaamonde.unoserver.service;

import dev.rodrigovaamonde.unoserver.dto.GameResponseDTO;
import dev.rodrigovaamonde.unoserver.dto.PlayCardRequestDTO;
import dev.rodrigovaamonde.unoserver.model.*; // Importar los modelos de cartas
import dev.rodrigovaamonde.unoserver.repository.GameRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList; // Importar ArrayList
import java.util.List;      // Importar List
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private GameService gameService;

    /**
     * Crea un mazo de prueba con un número específico de cartas simples.
     * Esto evita la necesidad de llamar al método privado initializeDeck() del servicio.
     * @param cardCount El número de cartas a crear.
     * @return Una lista de cartas para usar en los tests.
     */
    private List<Card> createTestDeck(int cardCount) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < cardCount; i++) {
            // Las cartas no necesitan ser variadas, solo existir para la lógica de repartir.
            deck.add(new Card(Color.BLUE, CardValue.ONE));
        }
        return deck;
    }

    @Test
    void joinGame_shouldAddPlayerToGame() {
        // Arrange
        Long gameId = 1L;
        Game game = new Game("ABCDEF");
        game.setId(gameId);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Game updatedGame = gameService.joinGame(gameId, "Rodrigo");

        // Assert
        assertEquals(1, updatedGame.getPlayers().size());
        assertEquals("Rodrigo", updatedGame.getPlayers().getFirst().getName());
        verify(gameRepository, times(1)).findById(gameId);
        verify(gameRepository, times(1)).save(game);
    }

    @Test
    void joinGame_shouldNotifyClientesViaWebStock() {
        Long gameId = 1L;
        String gameCode ="ABCDEF";
        Game game = new Game(gameCode);
        game.setId(gameId);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        gameService.joinGame(gameId, "Rodrigo");

        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<GameResponseDTO> payloadCaptor = ArgumentCaptor.forClass(GameResponseDTO.class);
        verify(messagingTemplate, times(1)).convertAndSend(destinationCaptor.capture(), payloadCaptor.capture());
        assertEquals("/topic/" + gameCode, destinationCaptor.getValue());
        assertEquals(1, payloadCaptor.getValue().getPlayers().size());
    }

    @Test
    void joinGame_shouldThrowException_whenGameIsInProgress() {
        // Arrange
        Long gameId = 1L;
        Game game = new Game("ABCDEF");
        game.setId(gameId);
        game.setStatus(Game.GameStatus.IN_PROGRESS);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            gameService.joinGame(gameId, "Ana");
        });

        assertEquals("Cannot join a game that is already in progress or finished.", exception.getMessage());
        verify(gameRepository, never()).save(any(Game.class));
    }

    @Test
    void joinGame_shouldThrowException_whenPlayerNameExists() {
        // Arrange
        Long gameId = 1L;
        Game game = new Game("ABCDEF");
        game.setId(gameId);
        game.addPlayer(new Player("Rodrigo"));

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            gameService.joinGame(gameId, "Rodrigo");
        });

        assertEquals("A player with the name 'Rodrigo' is already in this game", exception.getMessage());
    }

    @Test
    void startGame_shouldStartGameSuccessfully() {
        // Arrange
        Long gameId = 1L;
        Game game = new Game("XYZ123");
        game.setId(gameId);

        // Para 2 jugadores, necesitamos al menos 2*7 + 1 = 15 cartas. Creamos 30 por seguridad.
        game.setDrawPile(createTestDeck(30));

        game.addPlayer(new Player("Player 1"));
        game.addPlayer(new Player("Player 2"));

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Game startedGame = gameService.startGame(gameId);

        // Assert
        assertEquals(Game.GameStatus.IN_PROGRESS, startedGame.getStatus());
        assertNotNull(startedGame.getCurrentPlayer());
        assertEquals("Player 1", startedGame.getCurrentPlayer().getName());

        // Cada jugador debe tener 7 cartas
        assertEquals(7, startedGame.getPlayers().get(0).getHand().size());
        assertEquals(7, startedGame.getPlayers().get(1).getHand().size());

        // El mazo de robo ahora tendrá 30 - 14 (repartidas) - 1 (descarte) = 15 cartas
        assertEquals(15, startedGame.getDrawPile().size());

        // La pila de descarte debe tener 1 carta
        assertEquals(1, startedGame.getDiscardPile().size());

        verify(gameRepository, times(1)).save(game);
    }

    @Test
    void startGame_shouldNotifyClientesViaWebSocket() {
        Long gameId = 1L;
        String gameCode = "XYZ123";
        Game game = new Game(gameCode);
        game.setId(gameId);
        game.setDrawPile(createTestDeck(30));
        game.addPlayer(new Player("Player 1"));
        game.addPlayer(new Player("Player 2"));

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        gameService.startGame(gameId);

        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        verify(messagingTemplate, times(1)).convertAndSend(destinationCaptor.capture(), any(GameResponseDTO.class));
        assertEquals("/topic/" + gameCode, destinationCaptor.getValue());
    }

    @Test
    void startGame_shouldThrowException_whenNotEnoughPlayers() {
        // Arrange
        Long gameId = 1L;
        Game game = new Game("XYZ123");
        game.setId(gameId);
        game.addPlayer(new Player("Player 1"));

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            gameService.startGame(gameId);
        });

        assertEquals("Cannot start the game with fewer than 2 players", exception.getMessage());
    }

    @Test
    void playCard_shouldSucceed_whenCardIsValid() {
        // Arrange
        Game game = setupInProgressGame();
        Player currentPlayer = game.getCurrentPlayer();
        // Le damos al jugador una carta jugable (ROJO_UNO sobre un ROJO_CINCO)
        Card cardToPlay = new Card(Color.RED, CardValue.ONE);
        cardToPlay.setId(100L);
        currentPlayer.getHand().add(cardToPlay);

        PlayCardRequestDTO request = new PlayCardRequestDTO(currentPlayer.getId(), cardToPlay.getId(), null);

        // Act
        gameService.playCard(game.getGameCode(), request);

        // Assert
        // La carta jugada ahora está en la pila de descarte
        assertEquals(cardToPlay.getId(), game.getDiscardPile().getLast().getId());
        // El jugador tiene una carta menos
        assertEquals(7, currentPlayer.getHand().size());
        // El turno ha pasado al siguiente jugador
        assertNotEquals(currentPlayer.getId(), game.getCurrentPlayer().getId());
        // El color actual del juego es el de la carta jugada
        assertEquals(Color.RED, game.getCurrentColor());
        // Se ha notificado a los clientes
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(GameResponseDTO.class));
    }

    @Test
    void playCard_shouldThrowException_whenNotPlayerTurn() {
        // Arrange
        Game game = setupInProgressGame();
        Player notCurrentPlayer = game.getPlayers().get(1); // El segundo jugador
        Card cardToPlay = new Card(Color.RED, CardValue.ONE);
        cardToPlay.setId(100L);
        notCurrentPlayer.getHand().add(cardToPlay);

        PlayCardRequestDTO request = new PlayCardRequestDTO(notCurrentPlayer.getId(), cardToPlay.getId(), null);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> gameService.playCard(game.getGameCode(), request));
        assertEquals("It's not your turn to play.", exception.getMessage());
    }

    @Test
    void playCard_shouldThrowException_whenCardIsNotPlayable() {
        // Arrange
        Game game = setupInProgressGame(); // La carta superior es ROJO_CINCO
        Player currentPlayer = game.getCurrentPlayer();
        // Le damos al jugador una carta no jugable (AZUL_UNO)
        Card cardToPlay = new Card(Color.BLUE, CardValue.ONE);
        cardToPlay.setId(100L);
        currentPlayer.getHand().add(cardToPlay);

        PlayCardRequestDTO request = new PlayCardRequestDTO(currentPlayer.getId(), cardToPlay.getId(), null);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> gameService.playCard(game.getGameCode(), request));
        assertTrue(exception.getMessage().startsWith("Card cannot be played on top of"));
    }

    @Test
    void playCard_shouldSucceed_withWildCardAndChangeColor() {
        // Arrange
        Game game = setupInProgressGame();
        Player currentPlayer = game.getCurrentPlayer();
        Card wildCard = new Card(Color.BLACK, CardValue.WILD);
        wildCard.setId(200L);
        currentPlayer.getHand().add(wildCard);

        PlayCardRequestDTO request = new PlayCardRequestDTO(currentPlayer.getId(), wildCard.getId(), Color.GREEN);

        // Act
        gameService.playCard(game.getGameCode(), request);

        // Assert
        assertEquals(wildCard.getId(), game.getDiscardPile().getLast().getId());
        // El color del juego ahora es el elegido (VERDE)
        assertEquals(Color.GREEN, game.getCurrentColor());
        assertNotEquals(currentPlayer.getId(), game.getCurrentPlayer().getId());
    }

    /**
     * Método de ayuda para configurar un juego en progreso para los tests.
     */
    private Game setupInProgressGame() {
        String gameCode = "TEST123";
        Game game = new Game(gameCode);
        game.setStatus(Game.GameStatus.IN_PROGRESS);

        // Añadir jugadores con 7 cartas cada uno
        for (int i = 1; i <= 2; i++) {
            Player player = new Player("Player " + i);
            player.setId((long) i);
            player.setHand(createTestDeck(7));
            game.addPlayer(player);
        }

        // Poner una carta inicial en la pila de descarte
        Card topCard = new Card(Color.RED, CardValue.FIVE);
        topCard.setId(99L);
        game.getDiscardPile().add(topCard);
        game.setCurrentColor(Color.RED);
        game.setCurrentPlayer(game.getPlayers().getFirst());

        when(gameRepository.findByGameCode(gameCode)).thenReturn(Optional.of(game));
        lenient().when(gameRepository.save(any(Game.class))).thenReturn(game);

        return game;
    }
}
