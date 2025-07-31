package dev.rodrigovaamonde.unoserver.dto;

import dev.rodrigovaamonde.unoserver.model.Game;
import lombok.Data;

import java.util.List;

@Data
public class GameResponseDTO {

    private Long id;
    private String gameCode;
    private Game.GameStatus status;
    private List<PlayerDTO> players;
    private CardDTO topDiscardCard;
    private Long currentPlayerId;

    public static GameResponseDTO fromEntity(Game game) {
        GameResponseDTO dto = new GameResponseDTO();
        dto.setGameCode(game.getGameCode());
        dto.setStatus(game.getStatus());
        dto.setPlayers(
            game.getPlayers().stream()
                .map(PlayerDTO::fromEntity)
                .toList());

        if (game.getCurrentPlayer() != null) {
            dto.setCurrentPlayerId(game.getCurrentPlayer().getId());
        }

        if (game.getDiscardPile() != null && !game.getDiscardPile().isEmpty()) {
            dto.setTopDiscardCard(
                CardDTO.fromEntity(game.getDiscardPile().get(game.getDiscardPile().size() -1))
            );
        }

        return dto;
    }
}
