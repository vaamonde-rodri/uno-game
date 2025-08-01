import axios from 'axios';
import type { GameResponseDTO, JoinGameRequestDTO, PlayCardRequestDTO, DrawCardRequestDTO, DrawnCardDTO } from '../types';

const API_BASE_URL = 'http://localhost:8080/api';

class GameService {
  private api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  /**
   * Crear una nueva partida
   */
  async createGame(): Promise<GameResponseDTO> {
    const response = await this.api.post<GameResponseDTO>('/games');
    return response.data;
  }

  /**
   * Unirse a una partida existente
   */
  async joinGame(request: JoinGameRequestDTO): Promise<GameResponseDTO> {
    const response = await this.api.post<GameResponseDTO>('/games/join', request);
    return response.data;
  }

  /**
   * Obtener el estado actual de una partida
   */
  async getGame(gameCode: string): Promise<GameResponseDTO> {
    const response = await this.api.get<GameResponseDTO>(`/games/${gameCode}`);
    return response.data;
  }

  /**
   * Jugar una carta
   */
  async playCard(request: PlayCardRequestDTO): Promise<GameResponseDTO> {
    const response = await this.api.post<GameResponseDTO>('/games/play-card', request);
    return response.data;
  }

  /**
   * Robar una carta
   */
  async drawCard(request: DrawCardRequestDTO): Promise<DrawnCardDTO> {
    const response = await this.api.post<DrawnCardDTO>('/games/draw-card', request);
    return response.data;
  }

  /**
   * Iniciar una partida
   */
  async startGame(gameCode: string): Promise<GameResponseDTO> {
    const response = await this.api.post<GameResponseDTO>(`/games/${gameCode}/start`);
    return response.data;
  }
}

export const gameService = new GameService();
