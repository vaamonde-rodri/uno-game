-- Corregir la restricción de nombres de jugadores para que sea única solo por partida
-- V4__Fix_player_name_unique_constraint.sql

-- Eliminar la restricción UNIQUE global en el nombre
ALTER TABLE players DROP CONSTRAINT IF EXISTS players_name_key;

-- Crear una restricción UNIQUE compuesta (nombre + game_id)
-- Esto permite que el mismo nombre se use en diferentes partidas
ALTER TABLE players
ADD CONSTRAINT players_name_game_unique
UNIQUE (name, game_id);

-- Eliminar el índice anterior si existe
DROP INDEX IF EXISTS idx_players_name;

-- Crear un nuevo índice compuesto para mejorar el rendimiento
CREATE INDEX idx_players_name_game ON players(name, game_id);
