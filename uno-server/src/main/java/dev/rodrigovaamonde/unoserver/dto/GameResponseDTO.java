package dev.rodrigovaamonde.unoserver.dto;

import dev.rodrigovaamonde.unoserver.model.Game;
import lombok.Data;

@Data
public class GameResponseDTO {

    private String gameCode;
    private Game.GameStatus status;

    public static GameResponseDTO fromEntity(Game game) {
        GameResponseDTO dto = new GameResponseDTO();
        dto.setGameCode(game.getGameCode());
        dto.setStatus(game.getStatus());
        return dto;
    }
}
