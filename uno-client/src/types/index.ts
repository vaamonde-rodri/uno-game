// Tipos que coinciden con los DTOs del backend

export enum Color {
  RED = 'RED',
  YELLOW = 'YELLOW',
  GREEN = 'GREEN',
  BLUE = 'BLUE',
  WILD = 'WILD'
}

export enum CardValue {
  ZERO = 'ZERO',
  ONE = 'ONE',
  TWO = 'TWO',
  THREE = 'THREE',
  FOUR = 'FOUR',
  FIVE = 'FIVE',
  SIX = 'SIX',
  SEVEN = 'SEVEN',
  EIGHT = 'EIGHT',
  NINE = 'NINE',
  SKIP = 'SKIP',
  REVERSE = 'REVERSE',
  DRAW_TWO = 'DRAW_TWO',
  WILD = 'WILD',
  WILD_DRAW_FOUR = 'WILD_DRAW_FOUR'
}

export enum GameStatus {
  WAITING_FOR_PLAYERS = 'WAITING_FOR_PLAYERS',
  IN_PROGRESS = 'IN_PROGRESS',
  FINISHED = 'FINISHED'
}

export interface CardDTO {
  color: Color;
  value: CardValue;
}

export interface PlayerDTO {
  id: number;
  name: string;
  handSize: number;
  isCurrentPlayer: boolean;
}

export interface GameResponseDTO {
  id: number;
  gameCode: string;
  status: GameStatus;
  players: PlayerDTO[];
  topDiscardCard: CardDTO | null;
  currentPlayerId: number | null;
  createdById: number | null;
}

export interface PlayerActionDTO {
  playerId: number;
  gameCode: string;
  actionType: 'PLAY_CARD' | 'DRAW_CARD' | 'CHALLENGE_UNO';
  cardPlayed?: CardDTO;
  colorChosen?: Color;
}

export interface JoinGameRequestDTO {
  gameCode: string;
  playerName: string;
}

export interface PlayCardRequestDTO {
  gameCode: string;
  playerId: number;
  card: CardDTO;
  colorChosen?: Color;
}

export interface DrawCardRequestDTO {
  gameCode: string;
  playerId: number;
}

export interface DrawnCardDTO {
  card: CardDTO;
  canPlay: boolean;
}
