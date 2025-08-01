package dev.rodrigovaamonde.unoserver.controller;

import dev.rodrigovaamonde.unoserver.dto.GameResponseDTO;
import dev.rodrigovaamonde.unoserver.dto.JoinGameRequestDTO;
import dev.rodrigovaamonde.unoserver.dto.StartGameRequestDTO;
import dev.rodrigovaamonde.unoserver.model.Game;
import dev.rodrigovaamonde.unoserver.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/games")
@Tag(name = "Game Management", description = "API para crear, unirse y empezar partidas de UNO")
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @Operation(summary = "Crear una nueva partida", description = "Crea una nueva sala de juego y automáticamente añade al jugador creador.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Partida creada exitosamente",
            content = { @Content(mediaType = "application/json", schema = @Schema(implementation = GameResponseDTO.class)) })
    })
    @PostMapping
    public ResponseEntity<GameResponseDTO> createGame(@RequestBody JoinGameRequestDTO request) {
        Game newGame = gameService.createGameWithPlayer(request.playerName());
        GameResponseDTO response = GameResponseDTO.fromEntity(newGame);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Unirse a una partida por código", description = "Permite a un jugador unirse a una partida usando el código de la partida.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Jugador unido exitosamente",
            content = { @Content(mediaType = "application/json", schema = @Schema(implementation = GameResponseDTO.class)) }),
        @ApiResponse(responseCode = "400", description = "Petición inválida (ej. nombre de jugador ya existe o la partida ya empezó)"),
        @ApiResponse(responseCode = "404", description = "Partida no encontrada")
    })
    @PostMapping("/join")
    public ResponseEntity<GameResponseDTO> joinGameByCode(@RequestBody JoinGameRequestDTO request) {
        Game updatedGame = gameService.joinGameByCode(request.gameCode(), request.playerName());
        GameResponseDTO response = GameResponseDTO.fromEntity(updatedGame);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Unirse a una partida existente", description = "Permite a un jugador unirse a una partida que está esperando jugadores.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Jugador unido exitosamente",
            content = { @Content(mediaType = "application/json", schema = @Schema(implementation = GameResponseDTO.class)) }),
        @ApiResponse(responseCode = "400", description = "Petición inválida (ej. nombre de jugador ya existe o la partida ya empezó)"),
        @ApiResponse(responseCode = "404", description = "Partida no encontrada")
    })
    @PostMapping("/{gameId}/join")
    public ResponseEntity<GameResponseDTO> joinGame(
        @PathVariable Long gameId,
        @RequestBody JoinGameRequestDTO request
    ) {
        Game updatedGame = gameService.joinGame(gameId, request.playerName());

        GameResponseDTO response = GameResponseDTO.fromEntity(updatedGame);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Iniciar una partida por código", description = "Inicia una partida usando el código de la partida que tiene suficientes jugadores.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Partida iniciada exitosamente",
            content = { @Content(mediaType = "application/json", schema = @Schema(implementation = GameResponseDTO.class)) }),
        @ApiResponse(responseCode = "400", description = "No se puede iniciar la partida (ej. no hay suficientes jugadores)"),
        @ApiResponse(responseCode = "404", description = "Partida no encontrada"),
        @ApiResponse(responseCode = "403", description = "Solo el creador puede iniciar la partida")
    })
    @PostMapping("/{gameCode}/start")
    public ResponseEntity<GameResponseDTO> startGameByCode(
        @PathVariable String gameCode,
        @RequestBody StartGameRequestDTO request
    ) {
        Game startedGame = gameService.startGameByCode(gameCode, request.playerId());
        GameResponseDTO response = GameResponseDTO.fromEntity(startedGame);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Iniciar una partida", description = "Inicia una partida que tiene suficientes jugadores. Reparte las cartas y establece el primer turno.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Partida iniciada exitosamente",
            content = { @Content(mediaType = "application/json", schema = @Schema(implementation = GameResponseDTO.class)) }),
        @ApiResponse(responseCode = "400", description = "No se puede iniciar la partida (ej. no hay suficientes jugadores)"),
        @ApiResponse(responseCode = "404", description = "Partida no encontrada")
    })
    @PostMapping("/{gameId}/start")
    public ResponseEntity<GameResponseDTO> startGame(@PathVariable Long gameId) {
        Game startedGame = gameService.startGame(gameId);
        GameResponseDTO response = GameResponseDTO.fromEntity(startedGame);
        return ResponseEntity.ok(response);
    }
}
