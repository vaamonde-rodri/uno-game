# AGENT.md

Este documento proporciona el contexto necesario para que Gemini Code Assist entienda la estructura, tecnología y objetivos de este proyecto.

---

## 1. Visión General del Proyecto (Project Overview)

El objetivo es desarrollar una versión web multijugador en tiempo real del clásico juego de cartas UNO. Los usuarios podrán crear partidas, unirse a ellas y jugar unos contra otros a través de sus navegadores.

La arquitectura se basa en un **monorepo** con un backend que gestiona toda la lógica del juego (actuando como el árbitro) y un frontend que se encarga de la presentación visual y la interacción del usuario.

---

## 2. Pila Tecnológica (Tech Stack)

**Backend (`uno-server`):**
* **Lenguaje:** Java 21
* **Framework:** Spring Boot 3
* **Gestor de Dependencias:** Gradle (con Kotlin)
* **Comunicación:** Spring Web (para API REST) y Spring WebSocket (para tiempo real).
* **Base de Datos:** PostgreSQL
* **Librerías Clave:** Spring Data JPA, Lombok

**Frontend (`uno-client`):**
* **Lenguaje:** TypeScript
* **Framework:** React 19
* **Tooling:** Vite
* **Gestor de Paquetes:** npm
* **Estilos:** CSS Modules o Tailwind CSS.
* **Comunicación:** Se usará una librería de cliente para WebSockets (como `StompJS` y `SockJS-Client`) para conectar con el backend de Spring.
* **Internacionalización (i18n):** `react-i18next` para gestionar las traducciones.

---

## 3. Estructura del Proyecto (Project Structure)

El proyecto está organizado en dos módulos principales dentro de la raíz.

````text
uno-game/
├── uno-server/                 # Módulo del Backend (Spring Boot)
│   ├── src/main/java/com/rodrigovaamonde/unoserver/
│   │   ├── config/             # Configuración (WebSocket, Seguridad)
│   │   ├── controller/         # Controladores REST y WebSocket
│   │   ├── model/              # Entidades JPA (Game, Player, Card)
│   │   ├── repository/         # Repositorios Spring Data JPA
│   │   ├── service/            # Lógica de negocio (reglas del juego)
│   │   └── UnoServerApplication.java
│   └── pom.xml
│
├── uno-client/                 # Módulo del Frontend (React)
│   ├── src/
│   │   ├── components/         # Componentes reutilizables (Card, Button, PlayerAvatar)
│   │   ├── pages/              # Vistas principales (Lobby, GameBoard)
│   │   ├── hooks/              # Hooks personalizados (ej. useWebSocket)
│   │   ├── services/           # Lógica de comunicación con el backend
│   │   ├── locales/            # Archivos de traducción (en.json, es.json)
│   │   └── App.jsx             # Componente raíz
│   └── package.json
│
└── AGENT.md                    # Este archivo
````

---

## 4. Arquitectura y Decisiones Clave

* **Backend como Única Fuente de Verdad:** El `uno-server` es el único responsable de la lógica del juego. El cliente nunca toma decisiones sobre las reglas para evitar trampas.
* **Comunicación Dual:**
    * **API REST (`/api/**`):** Se usará para acciones que no son en tiempo real, como la autenticación de usuarios, la creación de partidas o la consulta de estadísticas.
    * **WebSockets (`/ws/**`):** Es la vía principal para la comunicación durante la partida. El servidor enviará el estado del juego actualizado a todos los clientes suscritos a una partida después de cada movimiento.
* **Frontend Reactivo:** El `uno-client` es una aplicación de página única (SPA) que reacciona a los mensajes recibidos por WebSocket para actualizar la interfaz de usuario, sin necesidad de recargar la página.
* **Soporte Multi-idioma (i18n):** La aplicación debe ser diseñada desde el principio para soportar múltiples idiomas. La interfaz de usuario y los mensajes del juego deben poder mostrarse en español e inglés inicialmente, con la posibilidad de añadir más idiomas en el futuro.

---

## 5. Estilo de Código y Convenciones

* **Idioma:** Escribe los comentarios en el código en **español**. El código (nombres de variables, funciones, clases) debe estar en **inglés**.
* **Backend:** Sigue las convenciones estándar de Java y Spring Boot. Usa Lombok (`@Data`, `@Builder`, etc.) para reducir el código repetitivo.
* **Frontend:** Utiliza **componentes funcionales y hooks** de React. Evita los componentes de clase.
* **Commits:** Usa el estándar de [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) (ej: `feat:`, `fix:`, `docs:`, `chore:`).

---

## 6. Comandos Importantes

* **Arrancar Backend:**  
  Desde la raíz:  
  ```bash
  cd uno-server && ./mvnw spring-boot:run
  ```

* **Arrancar Frontend:**  
  Desde la raíz:  
  ```bash
  cd uno-client && npm install && npm run dev
  ```

---

## 7. Pruebas (Testing)

### 🔹 Backend

El backend debe estar bien cubierto por tests unitarios y de integración, especialmente en la lógica del juego.

**Herramientas utilizadas:**
* **JUnit 5** – Para test unitarios.
* **Mockito** – Para mocks de servicios y repositorios.
* **Spring Boot Test** – Para pruebas de integración con contexto completo.

**Áreas clave a testear:**
- Reglas del juego (servicios).
- Controladores REST y WebSocket.
- Validaciones y flujo del juego (turnos, penalizaciones, fin de partida).
- Persistencia y recuperación de partidas y jugadores.

**Ejemplo de test unitario básico:**
```java
@Test
void givenPlayerDrawsCard_whenNoPlayableCard_thenPlayerDraws() {
    // Arrange
    Player player = new Player("Rodrigo");
    Game game = gameService.createGame();
    game.addPlayer(player);

    // Act
    gameService.playTurn(player.getId(), /* carta no jugable */ null);

    // Assert
    assertEquals(8, player.getHand().size()); // Roba una carta
}
```

### 🔹 Frontend (opcional por ahora)

Aunque el enfoque principal está en el backend, se recomienda en el futuro:
- **React Testing Library** + **Jest** para tests de componentes.
- Mocks de WebSocket para probar flujos en tiempo real.

---

## 8. Objetivos Actuales / Siguientes Pasos

1. Establecer la conexión básica de WebSocket entre el `uno-client` y el `uno-server`.
2. Definir las entidades JPA iniciales: `Player` y `Game`.
3. Crear un componente de "Lobby" en React donde los jugadores puedan esperar antes de que empiece la partida.
4. Comenzar a implementar y testear la lógica básica del flujo de turnos y jugadas en el backend.