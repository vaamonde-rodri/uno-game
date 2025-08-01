package dev.rodrigovaamonde.unoserver.controller;

import dev.rodrigovaamonde.unoserver.annotation.WebSocketOperation;
import dev.rodrigovaamonde.unoserver.annotation.WebSocketParam;
import dev.rodrigovaamonde.unoserver.annotation.WebSocketResponse;
import dev.rodrigovaamonde.unoserver.dto.*;
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

    @WebSocketOperation(
        summary = "Jugar una carta",
        description = "Permite a un jugador jugar una carta de su mano. La carta debe ser válida según las reglas del UNO.",
        destination = "/app/game/{gameCode}/play-card",
        responseChannels = {"/topic/game/{gameCode}/state"},
        tags = {"Gameplay", "Cards"}
    )
    @WebSocketResponse(
        channel = "/topic/game/{gameCode}/state",
        description = "Estado actualizado del juego enviado a todos los jugadores",
        content = GameResponseDTO.class,
        broadcast = true
    )
    @MessageMapping("/game/{gameCode}/play-card")
    public void playCard(
        @WebSocketParam(
            name = "gameCode",
            description = "Código único de 6 caracteres que identifica la partida",
            example = "ABC123"
        )
        @DestinationVariable String gameCode,

        @WebSocketParam(
            name = "request",
            description = "Datos de la carta a jugar incluyendo ID del jugador y carta"
        )
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

    @WebSocketOperation(
        summary = "Robar una carta",
        description = "Permite a un jugador robar una carta del mazo cuando no puede o no quiere jugar.",
        destination = "/app/game/{gameCode}/draw-card",
        responseChannels = {"/queue/game/{gameCode}/drawn-card", "/topic/game/{gameCode}/state"},
        tags = {"Gameplay", "Cards"}
    )
    @WebSocketResponse(
        channel = "/queue/game/{gameCode}/drawn-card",
        description = "Carta robada enviada privadamente al jugador",
        content = DrawnCardDTO.class,
        broadcast = false
    )
    @MessageMapping("/game/{gameCode}/draw-card")
    public void drawCard(
        @WebSocketParam(
            name = "gameCode",
            description = "Código único de 6 caracteres que identifica la partida",
            example = "ABC123"
        )
        @DestinationVariable String gameCode,

        @WebSocketParam(
            name = "request",
            description = "Solicitud que contiene el ID del jugador que quiere robar"
        )
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

    @WebSocketOperation(
        summary = "Pasar turno",
        description = "Permite a un jugador pasar su turno sin jugar una carta.",
        destination = "/app/game/{gameCode}/pass-turn",
        responseChannels = {"/topic/game/{gameCode}/state"},
        tags = {"Gameplay", "Turn Management"}
    )
    @WebSocketResponse(
        channel = "/topic/game/{gameCode}/state",
        description = "Estado actualizado del juego con el turno pasado al siguiente jugador",
        content = GameResponseDTO.class,
        broadcast = true
    )
    @MessageMapping("/game/{gameCode}/pass-turn")
    public void passTurn(
        @WebSocketParam(
            name = "gameCode",
            description = "Código único de 6 caracteres que identifica la partida",
            example = "ABC123"
        )
        @DestinationVariable String gameCode,

        @WebSocketParam(
            name = "request",
            description = "Acción del jugador que contiene su ID"
        )
        @Payload PlayerActionDTO request
    ) {
        try {
            gameService.passTurn(gameCode, request.playerId());
        } catch (Exception e) {
            log.error("Error processing pass turn request for game {}: {}", gameCode, e.getMessage(), e);
        }
    }

    @WebSocketOperation(
        summary = "Declarar UNO",
        description = "Permite a un jugador declarar UNO cuando le queda una sola carta.",
        destination = "/app/game/{gameCode}/declare-uno",
        responseChannels = {"/topic/game/{gameCode}/state"},
        tags = {"Gameplay", "UNO Rules"}
    )
    @WebSocketResponse(
        channel = "/topic/game/{gameCode}/state",
        description = "Estado actualizado del juego mostrando que el jugador declaró UNO",
        content = GameResponseDTO.class,
        broadcast = true
    )
    @MessageMapping("/game/{gameCode}/declare-uno")
    public void declareUno(
        @WebSocketParam(
            name = "gameCode",
            description = "Código único de 6 caracteres que identifica la partida",
            example = "ABC123"
        )
        @DestinationVariable String gameCode,

        @WebSocketParam(
            name = "request",
            description = "Acción del jugador que contiene su ID"
        )
        @Payload PlayerActionDTO request
    ) {
        try {
            gameService.declareUno(gameCode, request.playerId());
        } catch (Exception e) {
            log.error("Error processing declare UNO request for game {}: {}", gameCode, e.getMessage(), e);
            //TODO: Enviar un mensaje de error específico al jugador que intentó declarar UNO.
        }
    }

    @WebSocketOperation(
        summary = "Desafiar UNO",
        description = "Permite a un jugador desafiar a otro jugador que declaró UNO, si considera que no tenía una sola carta.",
        destination = "/app/game/{gameCode}/challenge-uno",
        responseChannels = {"/topic/game/{gameCode}/state"},
        tags = {"Gameplay", "UNO Rules"}
    )
    @WebSocketResponse(
        channel = "/topic/game/{gameCode}/state",
        description = "Estado actualizado del juego mostrando el resultado del desafío de UNO",
        content = GameResponseDTO.class,
        broadcast = true
    )
    @MessageMapping("/game/{gameCode}/challenge-uno")
    public void challengeUno(
        @WebSocketParam(
            name = "gameCode",
            description = "Código único de 6 caracteres que identifica la partida",
            example = "ABC123"
        )
        @DestinationVariable String gameCode,

        @WebSocketParam(
            name = "request",
            description = "Datos del desafío incluyendo ID del jugador que desafía y el jugador desafiado"
        )
        @Payload ChallengeUnoRequestDTO request
    ) {
        try {
            gameService.challengeUno(gameCode, request);
        } catch (Exception e) {
            log.error("Error processing challenge UNO request for game {}: {}", gameCode, e.getMessage(), e);
            //TODO: ENviar un mensaje de error específico al jugador que intentó desafiar UNO.
        }
    }
}
