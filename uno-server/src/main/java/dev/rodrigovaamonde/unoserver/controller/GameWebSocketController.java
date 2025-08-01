package dev.rodrigovaamonde.unoserver.controller;

import dev.rodrigovaamonde.unoserver.dto.DrawCardRequestDTO;
import dev.rodrigovaamonde.unoserver.dto.DrawnCardDTO;
import dev.rodrigovaamonde.unoserver.dto.PlayCardRequestDTO;
import dev.rodrigovaamonde.unoserver.dto.PlayerActionDTO;
import dev.rodrigovaamonde.unoserver.model.Card;
import dev.rodrigovaamonde.unoserver.model.Game;
import dev.rodrigovaamonde.unoserver.service.GameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@Slf4j
public class GameWebSocketController {

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    public GameWebSocketController(GameService gameService, SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Se activa cuando un cliente envía un mensaje a "/app/game/{gameCode}/play-card".
     *
     * @param gameCode El código de la partida, extraído de la URL del destino.
     * @param request  El payload con los detalles de la jugada.
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

    /**
     * Se activa cuando un cliente envía un mensaje a "/app/game/{gameCode}/draw-card".
     *
     * @param gameCode El código de la partida, extraído de la URL del destino.
     * @param request  El payload con los detalles de la solicitud de robar una carta.
     */
    @MessageMapping("/game/{gameCode}/draw-card")
    public void drawCard(
        @DestinationVariable String gameCode,
        @Payload DrawCardRequestDTO request,
        Principal principal
    ) {
        try {
            Card drawnCard = gameService.drawCard(gameCode, request.playerId());
            Game game = gameService.getGame(gameCode);

            boolean isPlayable = gameService.isCardPlayable(drawnCard,
                game.getDiscardPile().getLast(),
                game.getCurrentColor());
            DrawnCardDTO response = new DrawnCardDTO(drawnCard, isPlayable);

            messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/game/" + gameCode + "/drawn-card",
                response
            );

        } catch (Exception e) {
            log.error("Error processing draw card request for game {}: {}", gameCode, e.getMessage(), e);
            //TODO: Enviar un mensaje de error específico al jugador que intentó robar una carta.
        }
    }

    @MessageMapping("/game/{gameCode}/pass-turn")
    public void passTurn(
        @DestinationVariable String gameCode,
        @Payload PlayerActionDTO request
    ) {
        try {
            gameService.passTurn(gameCode, request.playerId());
        } catch (Exception e) {
            log.error("Error processing pass turn request for game {}: {}", gameCode, e.getMessage(), e);
        }
    }

    @MessageMapping("/game/{gameCode}/declare-uno")
    public void declareUno(
        @DestinationVariable String gameCode,
        @Payload PlayerActionDTO request
    ) {
        try {
            gameService.declareUno(gameCode, request.playerId());
        } catch (Exception e) {
            log.error("Error processing declare UNO request for game {}: {}", gameCode, e.getMessage(), e);
            //TODO: Enviar un mensaje de error específico al jugador que intentó declarar UNO.
        }
    }
}
