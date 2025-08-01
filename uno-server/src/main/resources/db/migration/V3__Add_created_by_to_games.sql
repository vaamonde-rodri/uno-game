-- Añadir columna para identificar al creador de la partida
ALTER TABLE games
ADD COLUMN created_by_player_id BIGINT;

-- Actualizar partidas existentes: asignar como creador al primer jugador de cada partida
UPDATE games
SET created_by_player_id = (
    SELECT p.id
    FROM players p
    WHERE p.game_id = games.id
    ORDER BY p.id ASC
    LIMIT 1
)
WHERE created_by_player_id IS NULL;

-- Añadir clave foránea hacia la tabla players
ALTER TABLE games
ADD CONSTRAINT fk_games_created_by
FOREIGN KEY (created_by_player_id) REFERENCES players(id);

-- Crear índice para mejorar el rendimiento de consultas
CREATE INDEX idx_games_created_by ON games(created_by_player_id);
