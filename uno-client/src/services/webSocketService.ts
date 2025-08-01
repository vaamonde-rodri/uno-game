import { Client, Frame, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type { GameResponseDTO, PlayerActionDTO } from '../types';

export type GameEventHandler = (gameState: GameResponseDTO) => void;
export type PlayerActionHandler = (action: PlayerActionDTO) => void;

class WebSocketService {
  private client: Client | null = null;
  private connected = false;
  private gameCode: string | null = null;

  /**
   * Conectar al servidor WebSocket
   */
  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      this.client = new Client({
        webSocketFactory: () => new SockJS('http://localhost:8080/api/ws'),
        connectHeaders: {},
        debug: (str) => {
          console.log('[WebSocket Debug]:', str);
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      });

      this.client.onConnect = (frame: Frame) => {
        console.log('Conectado al WebSocket:', frame);
        this.connected = true;
        resolve();
      };

      this.client.onStompError = (frame: Frame) => {
        console.error('Error de STOMP:', frame.headers['message']);
        console.error('Detalles:', frame.body);
        reject(new Error(frame.headers['message'] || 'Error de conexión WebSocket'));
      };

      this.client.onDisconnect = () => {
        console.log('Desconectado del WebSocket');
        this.connected = false;
      };

      this.client.activate();
    });
  }

  /**
   * Desconectar del servidor WebSocket
   */
  disconnect(): void {
    if (this.client) {
      this.client.deactivate();
      this.connected = false;
      this.gameCode = null;
    }
  }

  /**
   * Suscribirse a las actualizaciones de una partida específica
   */
  subscribeToGame(gameCode: string, onGameUpdate: GameEventHandler): void {
    if (!this.client || !this.connected) {
      throw new Error('WebSocket no está conectado');
    }

    this.gameCode = gameCode;

    // Suscribirse al estado del juego
    this.client.subscribe(`/topic/game/${gameCode}`, (message: IMessage) => {
      try {
        const gameState: GameResponseDTO = JSON.parse(message.body);
        onGameUpdate(gameState);
      } catch (error) {
        console.error('Error al parsear el estado del juego:', error);
      }
    });

    console.log(`Suscrito a las actualizaciones del juego: ${gameCode}`);
  }

  /**
   * Suscribirse a las acciones de los jugadores
   */
  subscribeToPlayerActions(gameCode: string, onPlayerAction: PlayerActionHandler): void {
    if (!this.client || !this.connected) {
      throw new Error('WebSocket no está conectado');
    }

    this.client.subscribe(`/topic/game/${gameCode}/actions`, (message: IMessage) => {
      try {
        const action: PlayerActionDTO = JSON.parse(message.body);
        onPlayerAction(action);
      } catch (error) {
        console.error('Error al parsear la acción del jugador:', error);
      }
    });

    console.log(`Suscrito a las acciones del juego: ${gameCode}`);
  }

  /**
   * Enviar una acción de juego través del WebSocket
   */
  sendPlayerAction(action: PlayerActionDTO): void {
    if (!this.client || !this.connected) {
      throw new Error('WebSocket no está conectado');
    }

    this.client.publish({
      destination: '/app/game.action',
      body: JSON.stringify(action)
    });
  }

  /**
   * Verificar si está conectado
   */
  isConnected(): boolean {
    return this.connected;
  }

  /**
   * Obtener el código del juego actual
   */
  getCurrentGameCode(): string | null {
    return this.gameCode;
  }
}

export const webSocketService = new WebSocketService();
