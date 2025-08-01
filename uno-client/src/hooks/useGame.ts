import { useState, useEffect, useCallback } from 'react';
import { GameResponseDTO, PlayerDTO, CardDTO } from '../types';
import { gameService } from '../services/gameService';
import { webSocketService } from '../services/webSocketService';

interface UseGameReturn {
  // Estado del juego
  game: GameResponseDTO | null;
  currentPlayer: PlayerDTO | null;
  isMyTurn: boolean;
  loading: boolean;
  error: string | null;

  // Acciones del juego
  createGame: (playerName: string) => Promise<GameResponseDTO>;
  joinGame: (gameCode: string, playerName: string) => Promise<GameResponseDTO>;
  playCard: (card: CardDTO, colorChosen?: string) => Promise<void>;
  drawCard: () => Promise<void>;
  startGame: () => Promise<void>;

  // Estado de conexión
  connected: boolean;
}

export function useGame(playerId?: number): UseGameReturn {
  const [game, setGame] = useState<GameResponseDTO | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [connected, setConnected] = useState(false);

  // Conectar al WebSocket cuando se monta el componente
  useEffect(() => {
    const connectWebSocket = async () => {
      try {
        await webSocketService.connect();
        setConnected(true);
      } catch (err) {
        console.error('Error conectando WebSocket:', err);
        setError('Error de conexión en tiempo real');
      }
    };

    connectWebSocket();

    return () => {
      webSocketService.disconnect();
      setConnected(false);
    };
  }, []);

  // Manejar actualizaciones del juego vía WebSocket
  const handleGameUpdate = useCallback((updatedGame: GameResponseDTO) => {
    console.log('Actualización del juego recibida:', updatedGame);
    setGame(updatedGame);
    setError(null);
  }, []);

  // Suscribirse a actualizaciones cuando tenemos un código de juego
  useEffect(() => {
    if (game?.gameCode && connected) {
      webSocketService.subscribeToGame(game.gameCode, handleGameUpdate);
    }
  }, [game?.gameCode, connected, handleGameUpdate]);

  // Calcular jugador actual y si es mi turno
  const currentPlayer = game?.players.find(p => p.id === playerId) || null;
  const isMyTurn = Boolean(playerId && game?.currentPlayerId === playerId);

  // Funciones para manejar errores
  const handleError = (err: any) => {
    console.error('Error en operación:', err);
    setError(err.response?.data?.message || err.message || 'Error desconocido');
    setLoading(false);
  };

  // Crear nueva partida
  const createGame = useCallback(async (playerName: string) => {
    setLoading(true);
    setError(null);
    try {
      const newGame = await gameService.createGame(playerName);
      setGame(newGame);
      return newGame; // Devolver la partida actualizada
    } catch (err) {
      handleError(err);
      throw err; // Re-lanzar el error para que el componente pueda manejarlo
    } finally {
      setLoading(false);
    }
  }, []);

  // Unirse a una partida
  const joinGame = useCallback(async (gameCode: string, playerName: string) => {
    setLoading(true);
    setError(null);
    try {
      const joinedGame = await gameService.joinGame({ gameCode, playerName });
      setGame(joinedGame);
      return joinedGame; // Devolver la partida actualizada
    } catch (err) {
      handleError(err);
      throw err; // Re-lanzar el error para que el componente pueda manejarlo
    } finally {
      setLoading(false);
    }
  }, []);

  // Jugar una carta
  const playCard = useCallback(async (card: CardDTO, colorChosen?: string) => {
    if (!game || !playerId) return;

    setLoading(true);
    setError(null);
    try {
      await gameService.playCard({
        gameCode: game.gameCode,
        playerId,
        card,
        colorChosen: colorChosen as any
      });
      // El estado se actualizará vía WebSocket
    } catch (err) {
      handleError(err);
    } finally {
      setLoading(false);
    }
  }, [game, playerId]);

  // Robar una carta
  const drawCard = useCallback(async () => {
    if (!game || !playerId) return;

    setLoading(true);
    setError(null);
    try {
      await gameService.drawCard({
        gameCode: game.gameCode,
        playerId
      });
      // El estado se actualizará vía WebSocket
    } catch (err) {
      handleError(err);
    } finally {
      setLoading(false);
    }
  }, [game, playerId]);

  // Iniciar partida
  const startGame = useCallback(async () => {
    if (!game || !playerId) return;

    setLoading(true);
    setError(null);
    try {
      await gameService.startGame(game.gameCode, playerId);
      // El estado se actualizará vía WebSocket
    } catch (err) {
      handleError(err);
    } finally {
      setLoading(false);
    }
  }, [game, playerId]);

  return {
    game,
    currentPlayer,
    isMyTurn,
    loading,
    error,
    createGame,
    joinGame,
    playCard,
    drawCard,
    startGame,
    connected
  };
}
