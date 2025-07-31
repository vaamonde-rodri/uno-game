package dev.rodrigovaamonde.unoserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class GameWebSocketController {

    @MessageMapping("/play-card")
    public void playCard(@Payload String message) {
        log.info("Received message into playCard: " + message);
    }
}
