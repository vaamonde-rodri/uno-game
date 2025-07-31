package dev.rodrigovaamonde.unoserver.controller;

import dev.rodrigovaamonde.unoserver.dto.GameResponseDTO;
import dev.rodrigovaamonde.unoserver.dto.JoinGameRequestDTO;
import dev.rodrigovaamonde.unoserver.model.Game;
import dev.rodrigovaamonde.unoserver.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games")
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<GameResponseDTO> createGame() {
        Game newGame = gameService.createGame();
        GameResponseDTO response = GameResponseDTO.fromEntity(newGame);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{gameId}/join")
    public ResponseEntity<GameResponseDTO> joinGame(
        @PathVariable Long gameId,
        @RequestBody JoinGameRequestDTO request
    ) {
        Game updatedGame = gameService.joinGame(gameId, request.playerName());

        GameResponseDTO response = GameResponseDTO.fromEntity(updatedGame);

        return ResponseEntity.ok(response);
    }
}
