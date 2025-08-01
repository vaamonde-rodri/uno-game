# 🎴 UNO Game - Servidor Backend

Un servidor backend completo para el juego UNO desarrollado con Spring Boot, PostgreSQL y WebSockets.

## 🚀 Características

- ✅ **API REST** completa para gestión de juegos
- ✅ **WebSockets** para comunicación en tiempo real
- ✅ **Base de datos PostgreSQL** con Docker
- ✅ **Sistema de migraciones** con Flyway
- ✅ **Documentación automática** con OpenAPI/Swagger
- ✅ **Perfiles de configuración** (dev, prod, test)
- ✅ **Cobertura de código** con JaCoCo

## 🛠️ Tecnologías

- **Java 21** con Spring Boot 3.5.4
- **PostgreSQL 16** (producción)
- **H2 Database** (desarrollo y testing)
- **Flyway** para migraciones
- **Docker & Docker Compose** para infraestructura
- **Gradle** como build tool
- **Lombok** para reducir boilerplate
- **JaCoCo** para cobertura de código

## 📦 Configuración Rápida

### Prerrequisitos
- Java 21+
- Docker y Docker Compose
- Git

### Instalación Automática
```bash
# Clonar el repositorio (si aplica)
git clone <repository-url>
cd uno-game

# Ejecutar script de configuración automática
./setup.sh
```

### Instalación Manual

1. **Iniciar servicios de base de datos:**
```bash
docker compose up -d
```

2. **Compilar y ejecutar migraciones:**
```bash
cd uno-server
./gradlew clean build
./gradlew flywayMigrate
```

3. **Ejecutar el servidor:**
```bash
# Modo producción (PostgreSQL)
./gradlew bootRun

# Modo desarrollo (H2)
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## 🔧 Configuración

### Perfiles Disponibles

#### Producción (por defecto)
- Base de datos: PostgreSQL
- Puerto: 8080
- Migraciones: Habilitadas
- Logging: INFO

#### Desarrollo (`dev`)
- Base de datos: H2 (en memoria)
- Puerto: 8080
- Consola H2: http://localhost:8080/h2-console
- Logging: DEBUG
- DDL: create-drop

#### Testing (`test`)
- Base de datos: H2 (en memoria)
- Logging: WARN
- Perfiles de datos de prueba

### Variables de Entorno

Para personalizar la configuración, puedes usar estas variables de entorno:

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=uno_game_db
export DB_USERNAME=uno_user
export DB_PASSWORD=uno_password
export SERVER_PORT=8080
```

## 🗄️ Base de Datos

### Estructura de Tablas

- **games**: Información de partidas
- **players**: Jugadores registrados
- **cards**: Cartas del juego

### Migraciones

Las migraciones se ejecutan automáticamente con Flyway:

```bash
# Ver estado de migraciones
./gradlew flywayInfo

# Ejecutar migraciones pendientes
./gradlew flywayMigrate

# Reparar migraciones (si hay errores)
./gradlew flywayRepair
```

## 🌐 API Endpoints

### Documentación Interactiva
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api/api-docs

### Principales Endpoints

```
GET    /api/games              # Listar juegos
POST   /api/games              # Crear nuevo juego
GET    /api/games/{id}          # Obtener juego específico
POST   /api/games/{id}/join     # Unirse a juego
POST   /api/games/{id}/start    # Iniciar juego
POST   /api/games/{id}/play     # Jugar carta
```

### WebSocket
- **Endpoint**: `/ws`
- **Destinos**:
  - `/topic/game/{gameId}` - Actualizaciones del juego
  - `/app/game/{gameId}/action` - Enviar acciones

## 🧪 Testing

```bash
# Ejecutar todos los tests
./gradlew test

# Ejecutar tests con cobertura
./gradlew jacocoTestReport

# Ver reporte de cobertura
open build/reports/jacoco/test/html/index.html
```

## 🐳 Docker

### Servicios Disponibles

- **PostgreSQL**: Puerto 5432
- **PgAdmin**: http://localhost:5050

### Comandos Útiles

```bash
# Iniciar servicios
docker compose up -d

# Ver logs
docker compose logs postgres

# Acceder a PostgreSQL
docker compose exec postgres psql -U uno_user -d uno_game_db

# Detener servicios
docker compose down

# Limpiar volúmenes
docker compose down -v
```

## 📊 Monitoreo

### Actuator Endpoints
- `/api/actuator/health` - Estado de la aplicación
- `/api/actuator/info` - Información de la aplicación
- `/api/actuator/metrics` - Métricas de rendimiento

### Logging
Los logs se escriben en la consola con formato personalizado:
```
2024-01-01 12:00:00 - [INFO] - Aplicación iniciada correctamente
```

## 🔒 Seguridad

### Configuraciones de Seguridad
- CORS configurado para desarrollo
- Validación de entrada en todos los endpoints
- Sanitización de datos de usuario
- Rate limiting en endpoints críticos

## 🚀 Despliegue

### Variables de Entorno para Producción
```bash
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-host:5432/uno_game_db
SPRING_DATASOURCE_USERNAME=your-username
SPRING_DATASOURCE_PASSWORD=your-password
```

### Docker en Producción
```bash
# Construir imagen
docker build -t uno-server .

# Ejecutar contenedor
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/uno_game_db \
  uno-server
```

## 🤝 Contribución

1. Fork el proyecto
2. Crea una branch para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la branch (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📝 Notas de Desarrollo

### Reglas de Negocio
- Máximo 4 jugadores por partida
- Mínimo 2 jugadores para iniciar
- Cartas especiales tienen efectos específicos
- Sistema de penalizaciones por no declarar UNO

### Patrones Utilizados
- **Repository Pattern** para acceso a datos
- **DTO Pattern** para transferencia de datos
- **Service Layer** para lógica de negocio
- **WebSocket** para comunicación en tiempo real

## 📞 Soporte

Si encuentras algún problema:

1. Revisa los logs: `docker compose logs`
2. Verifica la base de datos: http://localhost:5050
3. Consulta la documentación API: http://localhost:8080/api/swagger-ui.html
4. Ejecuta los tests: `./gradlew test`

---

**¡Disfruta jugando UNO! 🎉**
