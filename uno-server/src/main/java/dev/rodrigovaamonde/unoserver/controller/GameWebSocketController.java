package dev.rodrigovaamonde.unoserver.controller;

import dev.rodrigovaamonde.unoserver.dto.PlayCardRequestDTO;
import dev.rodrigovaamonde.unoserver.service.GameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class GameWebSocketController {

    private final GameService gameService;

    public GameWebSocketController(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Se activa cuando un cliente envía un mensaje a "/app/game/{gameCode}/play-card".
     * @param gameCode El código de la partida, extraído de la URL del destino.
     * @param request El payload con los detalles de la jugada.
     */
    @MessageMapping("/game/{gameCode}/play-card")
    public void playCard(
        @DestinationVariable String gameCode,
        @Payload PlayCardRequestDTO request
        ) {
        try {
            gameService.playCard(gameCode, request);
        } catch (Exception e) {
            // TODO: Enviar un mensaje de error específico al jugador que hizo la jugada.
            // Por ahora, lo registramos en el log del servidor.
            log.error("Error processing play card request for game {}: {}", gameCode, e.getMessage(), e);
        }
    }
}
