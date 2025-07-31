package dev.rodrigovaamonde.unoserver.controller;

import dev.rodrigovaamonde.unoserver.dto.GameResponseDTO;
import dev.rodrigovaamonde.unoserver.model.Game;
import dev.rodrigovaamonde.unoserver.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

//TODO: TErminamos haciendo este controller. El siguiente paso lógico sería implementar la funcionalidad para que un jugador se una a una partida existente.
