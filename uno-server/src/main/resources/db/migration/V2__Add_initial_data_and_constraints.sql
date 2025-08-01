-- Migración V2: Datos iniciales y configuraciones adicionales
-- V2__Add_initial_data_and_constraints.sql

-- Agregar trigger para actualizar updated_at automáticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Aplicar trigger a la tabla games
CREATE TRIGGER update_games_updated_at
    BEFORE UPDATE ON games
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Agregar restricciones adicionales
ALTER TABLE games
ADD CONSTRAINT chk_game_status
CHECK (status IN ('WAITING_FOR_PLAYERS', 'IN_PROGRESS', 'FINISHED', 'CANCELLED'));

ALTER TABLE cards
ADD CONSTRAINT chk_card_color
CHECK (color IN ('RED', 'BLUE', 'GREEN', 'YELLOW', 'WILD'));

ALTER TABLE cards
ADD CONSTRAINT chk_card_value
CHECK (value IN ('ZERO', 'ONE', 'TWO', 'THREE', 'FOUR', 'FIVE', 'SIX', 'SEVEN', 'EIGHT', 'NINE',
                 'SKIP', 'REVERSE', 'DRAW_TWO', 'WILD', 'WILD_DRAW_FOUR'));

-- Agregar restricción para que una carta no pueda estar en múltiples lugares a la vez
ALTER TABLE cards
ADD CONSTRAINT chk_card_single_location
CHECK (
    (player_id IS NOT NULL AND deck_game_id IS NULL AND discard_pile_game_id IS NULL) OR
    (player_id IS NULL AND deck_game_id IS NOT NULL AND discard_pile_game_id IS NULL) OR
    (player_id IS NULL AND deck_game_id IS NULL AND discard_pile_game_id IS NOT NULL)
);

-- Crear vista para estadísticas de juegos
CREATE VIEW game_statistics AS
SELECT
    g.id,
    g.game_code,
    g.status,
    COUNT(p.id) as player_count,
    g.created_at,
    g.updated_at
FROM games g
LEFT JOIN players p ON g.id = p.game_id
GROUP BY g.id, g.game_code, g.status, g.created_at, g.updated_at;
