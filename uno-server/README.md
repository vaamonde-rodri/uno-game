# 🎴 UNO Server - Backend

Servidor backend Spring Boot para el juego UNO multijugador en tiempo real.

## 🚀 Características

- ✅ **API REST** completa para gestión de juegos
- ✅ **WebSockets STOMP** para comunicación en tiempo real
- ✅ **Lógica completa del juego UNO** implementada en el servidor
- ✅ **Base de datos PostgreSQL** con migraciones automáticas
- ✅ **Documentación automática** con OpenAPI/Swagger y AsyncAPI
- ✅ **Perfiles de configuración** (dev, prod, test)
- ✅ **Cobertura de código** con JaCoCo
- ✅ **Seguridad** con validación de movimientos en servidor

## 🛠️ Stack Tecnológico

- **Java 21** - Lenguaje base
- **Spring Boot 3.5.4** - Framework principal
- **Spring Data JPA** - Persistencia de datos
- **Spring WebSocket (STOMP)** - Comunicación en tiempo real
- **PostgreSQL 16** - Base de datos en producción
- **H2 Database** - Base de datos para desarrollo y testing
- **Flyway** - Gestión de migraciones de BD
- **OpenAPI/Swagger** - Documentación API REST
- **AsyncAPI** - Documentación API WebSocket
- **Lombok** - Reducción de código boilerplate
- **Gradle** - Gestión de dependencias y build
- **JaCoCo** - Análisis de cobertura de código

## 📦 Configuración y Desarrollo

### Prerrequisitos
- Java 21+
- Docker y Docker Compose (para PostgreSQL)

### Setup Rápido

1. **Desde la raíz del proyecto:**
```bash
# Esto configura tanto backend como frontend
./setup.sh
```

2. **Solo backend (desde esta carpeta):**
```bash
# Iniciar base de datos
docker compose -f ../docker-compose.yml up -d

# Ejecutar migraciones
./gradlew flywayMigrate

# Iniciar servidor
./gradlew bootRun
```

### Variables de Entorno

Para desarrollo local, las configuraciones están en `application-dev.yml`. Para producción:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://host:port/database
export SPRING_DATASOURCE_USERNAME=username
export SPRING_DATASOURCE_PASSWORD=password
export SPRING_PROFILES_ACTIVE=prod
```

## 🏗️ Arquitectura del Backend

### Estructura del Código

```
src/main/java/dev/rodrigovaamonde/unoserver/
├── annotation/        # Anotaciones personalizadas para WebSocket
├── config/            # Configuración (WebSocket, Seguridad, Documentación)
├── controller/        # Controladores REST y WebSocket
├── model/             # Entidades JPA (Game, Player, Card)
├── repository/        # Repositorios Spring Data JPA
├── service/           # Lógica de negocio y reglas del juego
└── UnoServerApplication.java
```

### Endpoints Principales

#### API REST
- `POST /api/games` - Crear nueva partida
- `POST /api/games/{gameId}/join` - Unirse a partida
- `GET /api/games/{gameId}` - Estado de la partida

#### WebSocket STOMP
- `/app/game/{gameId}/play-card` - Jugar carta
- `/app/game/{gameId}/draw-card` - Robar carta
- `/topic/game/{gameId}` - Eventos de juego en tiempo real

### Base de Datos

El servidor usa PostgreSQL en producción y H2 para desarrollo. Las migraciones se gestionan con Flyway:

```bash
# Ejecutar migraciones
./gradlew flywayMigrate

# Limpiar base de datos
./gradlew flywayClean
```

## 🔧 Desarrollo

### Comandos Útiles

```bash
# Compilar
./gradlew build

# Ejecutar tests
./gradlew test

# Generar reporte de cobertura
./gradlew jacocoTestReport

# Ejecutar con hot reload
./gradlew bootRun

# Generar JAR
./gradlew bootJar
```

### Perfiles de Configuración

- **dev** - Desarrollo local con H2
- **prod** - Producción con PostgreSQL
- **test** - Testing con H2 en memoria

### Testing

```bash
# Ejecutar todos los tests
./gradlew test

# Ejecutar tests específicos
./gradlew test --tests "*GameServiceTest*"

# Ver reporte de cobertura
open build/reports/jacoco/test/html/index.html
```

## 📋 Documentación de APIs

Cuando el servidor esté ejecutándose:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **AsyncAPI Spec**: http://localhost:8080/api/docs/asyncapi
- **WebSocket Docs**: http://localhost:8080/api/docs/websocket

También consulta [`../WEBSOCKET_API.md`](../WEBSOCKET_API.md) para ejemplos detallados de uso.

## 🐛 Logs y Debugging

Los logs se configuran en `application.yml`. Para desarrollo:

```yaml
logging:
  level:
    dev.rodrigovaamonde.unoserver: DEBUG
    org.springframework.web.socket: DEBUG
```

## 🚀 Despliegue

Para instrucciones de despliegue en producción, consulta [`../DEPLOYMENT.md`](../DEPLOYMENT.md).
