package dev.rodrigovaamonde.unoserver.dto;

import dev.rodrigovaamonde.unoserver.model.Player;
import lombok.Data;

@Data
public class PlayerDTO {
    private Long id;
    private String name;
    private int cardCount;

    public static PlayerDTO fromEntity(Player player) {
        PlayerDTO dto = new PlayerDTO();
        dto.setId(player.getId());
        dto.setName(player.getName());
        dto.setCardCount(player.getHand().size());
        return dto;
    }
}
