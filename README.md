# UNO Web Multijugador 🎮

Una implementación moderna del clásico juego de cartas UNO para navegadores web, diseñada para partidas multijugador en tiempo real.

## 🎯 Descripción del Proyecto

Este proyecto permite a los usuarios crear y unirse a partidas de UNO online, jugando contra otros usuarios en tiempo real a través de sus navegadores. La aplicación mantiene toda la lógica del juego en el servidor para garantizar la integridad y prevenir trampas.

## 🏗️ Arquitectura

El proyecto está organizado como un **monorepo** con dos módulos principales:

- **[Backend (`uno-server`)](./uno-server/README.md)**: Servidor Spring Boot que actúa como árbitro del juego
- **[Frontend (`uno-client`)](./uno-client/README.md)**: Aplicación React que proporciona la interfaz de usuario

### Comunicación
- **API REST**: Para operaciones básicas (crear partida, unirse, obtener estado)
- **WebSockets**: Para comunicación en tiempo real durante las partidas
- **Backend como única fuente de verdad**: Toda la lógica del juego se ejecuta en el servidor

## 🚀 Inicio Rápido

### Prerrequisitos
- Java 21+
- Node.js 18+
- Docker y Docker Compose

### Configuración Automática
```bash
git clone <repository-url>
cd uno-game
./setup.sh
```

El script configurará automáticamente ambos módulos y iniciará todos los servicios.

### Configuración Manual
1. **Backend**: Sigue las instrucciones en [`uno-server/README.md`](./uno-server/README.md)
2. **Frontend**: Sigue las instrucciones en [`uno-client/README.md`](./uno-client/README.md)

## 📁 Estructura del Proyecto

```
uno-game/
├── README.md                   # Documentación general del proyecto
├── setup.sh                   # Script de configuración automática
├── docker-compose.yml         # Servicios de infraestructura
├── WEBSOCKET_API.md           # Documentación API WebSocket
├── RULES.md                   # Reglas del juego UNO
├── AGENT.md                   # Documentación técnica para IA
├── DEPLOYMENT.md              # Guía de despliegue
├── uno-server/                # Backend Spring Boot
│   ├── README.md              # Documentación específica del backend
│   └── ...                    # Código fuente del servidor
└── uno-client/                # Frontend React
    ├── README.md              # Documentación específica del frontend
    └── ...                    # Código fuente del cliente
```

## 📚 Documentación

### 🎮 Para Jugadores
- **[RULES.md](RULES.md)** - Reglas completas del UNO

### 🔧 Para Desarrolladores
- **[uno-server/README.md](./uno-server/README.md)** - Setup y desarrollo del backend
- **[uno-client/README.md](./uno-client/README.md)** - Setup y desarrollo del frontend
- **[WEBSOCKET_API.md](WEBSOCKET_API.md)** - API WebSocket completa
- **[AGENT.md](AGENT.md)** - Documentación técnica detallada

### 🚀 Para DevOps
- **[DEPLOYMENT.md](DEPLOYMENT.md)** - Guía de despliegue en producción

## 🛠️ Stack Tecnológico

| Componente | Tecnologías |
|------------|-------------|
| **Backend** | Java 21, Spring Boot 3, PostgreSQL, WebSockets |
| **Frontend** | TypeScript, React 19, Vite |
| **Infraestructura** | Docker, Docker Compose |

Para más detalles técnicos, consulta la documentación específica de cada módulo.

## 🤝 Contribución

1. Fork el proyecto
2. Crea tu rama de funcionalidad (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para detalles.
