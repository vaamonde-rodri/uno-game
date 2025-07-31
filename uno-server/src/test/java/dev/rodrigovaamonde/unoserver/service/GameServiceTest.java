package dev.rodrigovaamonde.unoserver.service;

import dev.rodrigovaamonde.unoserver.model.*; // Importar los modelos de cartas
import dev.rodrigovaamonde.unoserver.repository.GameRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList; // Importar ArrayList
import java.util.List;      // Importar List
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

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
}