-- Migración inicial: Crear tablas básicas para el juego UNO
-- V1__Create_initial_tables.sql

-- Crear tabla de juegos
CREATE TABLE games (
    id BIGSERIAL PRIMARY KEY,
    game_code VARCHAR(255) NOT NULL UNIQUE,
    is_reversed BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(50) NOT NULL DEFAULT 'WAITING_FOR_PLAYERS',
    current_color VARCHAR(20),
    current_player_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear tabla de jugadores
CREATE TABLE players (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    has_declared_uno BOOLEAN NOT NULL DEFAULT FALSE,
    game_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_player_game FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE
);

-- Crear tabla de cartas
CREATE TABLE cards (
    id BIGSERIAL PRIMARY KEY,
    color VARCHAR(20) NOT NULL,
    value VARCHAR(50) NOT NULL,
    player_id BIGINT,
    deck_game_id BIGINT,
    discard_pile_game_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_card_player FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
    CONSTRAINT fk_card_deck_game FOREIGN KEY (deck_game_id) REFERENCES games(id) ON DELETE CASCADE,
    CONSTRAINT fk_card_discard_game FOREIGN KEY (discard_pile_game_id) REFERENCES games(id) ON DELETE CASCADE
);

-- Agregar foreign key para current_player en games (después de crear players)
ALTER TABLE games
ADD CONSTRAINT fk_game_current_player
FOREIGN KEY (current_player_id) REFERENCES players(id) ON DELETE SET NULL;

-- Crear índices para mejorar el rendimiento
CREATE INDEX idx_games_game_code ON games(game_code);
CREATE INDEX idx_games_status ON games(status);
CREATE INDEX idx_players_name ON players(name);
CREATE INDEX idx_players_game_id ON players(game_id);
CREATE INDEX idx_cards_player_id ON cards(player_id);
CREATE INDEX idx_cards_deck_game_id ON cards(deck_game_id);
CREATE INDEX idx_cards_discard_pile_game_id ON cards(discard_pile_game_id);
