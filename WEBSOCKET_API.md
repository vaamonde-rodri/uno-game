# Documentación WebSocket API - UNO Game

Esta documentación describe todos los endpoints WebSocket disponibles para el juego UNO en tiempo real.

## Conexión WebSocket

**URL de conexión:** `ws://localhost:8080/ws`

**Protocolo:** STOMP sobre WebSocket

## Autenticación

Los endpoints WebSocket requieren autenticación. El cliente debe enviar un token JWT válido durante la conexión.

## Endpoints Disponibles

### 🎮 Gameplay

#### Jugar una Carta
- **Destino:** `/app/game/{gameCode}/play-card`
- **Descripción:** Permite a un jugador jugar una carta de su mano
- **Respuesta:** Broadcast a `/topic/game/{gameCode}/state`

**Parámetros:**
- `gameCode` (string): Código único de 6 caracteres de la partida
- `request` (PlayCardRequestDTO): Datos de la carta a jugar

**Ejemplo de mensaje:**
```json
{
  "playerId": "player-123",
  "cardId": "card-456",
  "chosenColor": "RED"
}
```

#### Robar una Carta
- **Destino:** `/app/game/{gameCode}/draw-card`
- **Descripción:** Permite a un jugador robar una carta del mazo
- **Respuestas:** 
  - Unicast a `/queue/game/{gameCode}/drawn-card` (carta robada)
  - Broadcast a `/topic/game/{gameCode}/state` (estado actualizado)

**Ejemplo de mensaje:**
```json
{
  "playerId": "player-123"
}
```

#### Pasar Turno
- **Destino:** `/app/game/{gameCode}/pass-turn`
- **Descripción:** Permite a un jugador pasar su turno
- **Respuesta:** Broadcast a `/topic/game/{gameCode}/state`

#### Declarar UNO
- **Destino:** `/app/game/{gameCode}/declare-uno`
- **Descripción:** Permite a un jugador declarar UNO cuando le queda una carta
- **Respuesta:** Broadcast a `/topic/game/{gameCode}/state`

#### Desafiar UNO
- **Destino:** `/app/game/{gameCode}/challenge-uno`
- **Descripción:** Permite a un jugador desafiar a otro que declaró UNO incorrectamente
- **Respuesta:** Broadcast a `/topic/game/{gameCode}/state`

### 📡 Suscripciones (Recibir mensajes)

#### Estado del Juego
- **Canal:** `/topic/game/{gameCode}/state`
- **Descripción:** Recibe actualizaciones del estado del juego
- **Tipo:** Broadcast a todos los jugadores de la partida

#### Carta Robada
- **Canal:** `/queue/game/{gameCode}/drawn-card`
- **Descripción:** Recibe la carta robada privadamente
- **Tipo:** Unicast al jugador que robó

## Tipos de Datos

### PlayCardRequestDTO
```typescript
interface PlayCardRequestDTO {
  playerId: string;
  cardId: string;
  chosenColor?: "RED" | "BLUE" | "GREEN" | "YELLOW"; // Solo para cartas comodín
}
```

### DrawCardRequestDTO
```typescript
interface DrawCardRequestDTO {
  playerId: string;
}
```

### PlayerActionDTO
```typescript
interface PlayerActionDTO {
  playerId: string;
}
```

### ChallengeUnoRequestDTO
```typescript
interface ChallengeUnoRequestDTO {
  challengerId: string;
  challengedPlayerId: string;
}
```

### GameResponseDTO
```typescript
interface GameResponseDTO {
  gameCode: string;
  status: "WAITING_PLAYERS" | "IN_PROGRESS" | "FINISHED";
  players: PlayerDTO[];
  currentPlayerId: string;
  direction: "CLOCKWISE" | "COUNTERCLOCKWISE";
  lastPlayedCard: CardDTO;
  currentColor: "RED" | "BLUE" | "GREEN" | "YELLOW";
  deckSize: number;
}
```

### DrawnCardDTO
```typescript
interface DrawnCardDTO {
  card: CardDTO;
  isPlayable: boolean;
}
```

### PlayerDTO
```typescript
interface PlayerDTO {
  id: string;
  name: string;
  cardCount: number;
  hasCalledUno: boolean;
}
```

### CardDTO
```typescript
interface CardDTO {
  id: string;
  color: "RED" | "BLUE" | "GREEN" | "YELLOW" | "WILD";
  type: "NUMBER" | "SKIP" | "REVERSE" | "DRAW_TWO" | "WILD" | "WILD_DRAW_FOUR";
  value: string; // Valor numérico para cartas NUMBER (0-9)
}
```

## Códigos de Error

Los errores se manejan mediante logs del servidor. En futuras versiones se implementará un sistema de mensajes de error específicos enviados al cliente.

## Ejemplos de Uso

### Conectar y suscribirse (JavaScript)
```javascript
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    // Suscribirse al estado del juego
    stompClient.subscribe('/topic/game/ABC123/state', function(message) {
        const gameState = JSON.parse(message.body);
        console.log('Estado actualizado:', gameState);
    });
    
    // Suscribirse a cartas robadas (privado)
    stompClient.subscribe('/user/queue/game/ABC123/drawn-card', function(message) {
        const drawnCard = JSON.parse(message.body);
        console.log('Carta robada:', drawnCard);
    });
});
```

### Enviar mensajes
```javascript
// Jugar una carta
stompClient.send('/app/game/ABC123/play-card', {}, JSON.stringify({
    playerId: 'player-123',
    cardId: 'card-456',
    chosenColor: 'RED'
}));

// Robar una carta
stompClient.send('/app/game/ABC123/draw-card', {}, JSON.stringify({
    playerId: 'player-123'
}));

// Pasar turno
stompClient.send('/app/game/ABC123/pass-turn', {}, JSON.stringify({
    playerId: 'player-123'
}));

// Declarar UNO
stompClient.send('/app/game/ABC123/declare-uno', {}, JSON.stringify({
    playerId: 'player-123'
}));

// Desafiar UNO
stompClient.send('/app/game/ABC123/challenge-uno', {}, JSON.stringify({
    challengerId: 'player-123',
    challengedPlayerId: 'player-456'
}));
```

## Flujo de Juego Típico

1. El cliente se conecta al WebSocket
2. Se suscribe a `/topic/game/{gameCode}/state` para recibir actualizaciones
3. Se suscribe a `/user/queue/game/{gameCode}/drawn-card` para cartas robadas
4. Envía mensajes a los endpoints `/app/game/{gameCode}/*` para realizar acciones
5. Recibe actualizaciones automáticas del estado del juego

## Herramientas Recomendadas

- **Desarrollo:** Postman WebSocket Client, WebSocket King
- **Testing:** SockJS client, STOMP.js
- **Monitoreo:** Spring Boot Actuator endpoints
- **Documentación:** AsyncAPI Studio para visualizar la especificación AsyncAPI

## Enlaces Útiles

- **Documentación JSON:** `GET /api/docs/websocket`
- **Especificación AsyncAPI:** `GET /api/docs/asyncapi`
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **AsyncAPI Spec:** `/src/main/resources/asyncapi.yml`
