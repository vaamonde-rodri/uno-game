# UNO Web Multijugador 🎮

Una implementación moderna del clásico juego de cartas UNO para navegadores web, diseñada para partidas multijugador en tiempo real.

## 🎯 Descripción del Proyecto

Este proyecto permite a los usuarios crear y unirse a partidas de UNO online, jugando contra otros usuarios en tiempo real a través de sus navegadores. La aplicación mantiene toda la lógica del juego en el servidor para garantizar la integridad y prevenir trampas.

## 🏗️ Arquitectura

El proyecto está organizado como un **monorepo** con dos módulos principales:

- **Backend (`uno-server`)**: Servidor Spring Boot que actúa como árbitro del juego
- **Frontend (`uno-client`)**: Aplicación React que proporciona la interfaz de usuario

### Arquitectura de Comunicación

- **API REST**: Para operaciones básicas (crear partida, unirse, obtener estado)
- **WebSockets**: Para comunicación en tiempo real durante las partidas
- **Backend como única fuente de verdad**: Toda la lógica del juego se ejecuta en el servidor

## 🛠️ Stack Tecnológico

### Backend (uno-server)
- **Java 21** - Lenguaje de programación
- **Spring Boot 3.5.4** - Framework principal
- **Spring Data JPA** - Persistencia de datos
- **Spring WebSocket** - Comunicación en tiempo real
- **PostgreSQL** - Base de datos
- **Lombok** - Reducción de código boilerplate
- **Gradle** - Gestión de dependencias

### Frontend (uno-client)
- **TypeScript** - Lenguaje de programación tipado
- **React 19.1.0** - Framework de UI
- **Vite 7.0.4** - Herramienta de desarrollo y build
- **ESLint** - Linter de código

## 📁 Estructura del Proyecto

```
uno-game/
├── README.md
├── AGENT.md                    # Documentación técnica detallada
├── uno-server/                 # Backend Spring Boot
│   ├── src/main/java/dev/rodrigovaamonde/unoserver/
│   │   ├── config/             # Configuración (WebSocket, Seguridad)
│   │   ├── controller/         # Controladores REST y WebSocket
│   │   ├── model/              # Entidades JPA (Game, Player, Card)
│   │   ├── repository/         # Repositorios Spring Data JPA
│   │   ├── service/            # Lógica de negocio (reglas del juego)
│   │   └── UnoServerApplication.java
│   ├── build.gradle.kts
│   └── src/main/resources/
│       └── application.properties
└── uno-client/                 # Frontend React
    ├── src/
    │   ├── components/         # Componentes reutilizables
    │   ├── pages/              # Vistas principales
    │   ├── hooks/              # Hooks personalizados
    │   ├── services/           # Comunicación con backend
    │   ├── locales/            # Archivos de traducción
    │   └── App.tsx             # Componente raíz
    ├── package.json
    └── vite.config.ts
```

## 🚀 Instalación y Configuración

### Prerrequisitos

- **Java 21** o superior
- **Node.js** (versión LTS recomendada)
- **PostgreSQL** (para la base de datos)
- **Git**

### Configuración del Backend

1. **Navegar al directorio del servidor:**
   ```bash
   cd uno-server
   ```

2. **Configurar la base de datos:**
   - Crear una base de datos PostgreSQL
   - Configurar las credenciales en `src/main/resources/application.properties`

3. **Ejecutar el servidor:**
   ```bash
   ./gradlew bootRun
   ```

   El servidor estará disponible en `http://localhost:8080`

### Configuración del Frontend

1. **Navegar al directorio del cliente:**
   ```bash
   cd uno-client
   ```

2. **Instalar dependencias:**
   ```bash
   npm install
   ```

3. **Ejecutar en modo desarrollo:**
   ```bash
   npm run dev
   ```

   La aplicación estará disponible en `http://localhost:5173`

## 🎮 Funcionalidades Implementadas

- ✅ Estructura base del proyecto (monorepo)
- ✅ Configuración de Spring Boot con WebSockets
- ✅ Frontend React con TypeScript
- ✅ Arquitectura preparada para tiempo real

### Funcionalidades Planificadas

- 🚧 Sistema de autenticación de usuarios
- 🚧 Creación y gestión de salas de juego
- 🚧 Lógica completa del juego UNO
- 🚧 Chat en tiempo real durante las partidas
- 🚧 Sistema de puntuación y estadísticas
- 🚧 Internacionalización (i18n)
- 🚧 Interfaz responsive para móviles

## 🎯 Reglas del Juego

Las reglas completas y detalladas del juego se encuentran en el documento [RULES.md](RULES.md).

A continuación, un resumen rápido:
- **Objetivo:** Ser el primer jugador en quedarse sin cartas.
- **Mecánica básica:** Jugar una carta que coincida en color, número o símbolo.
- **Cartas especiales:** Reversa, Salta turno, +2, Comodín y Comodín +4.
- **Regla "UNO":** Debes anunciar "UNO" cuando te quede una sola carta.

## 🧪 Testing

### Backend
```bash
cd uno-server
./gradlew test
```

### Frontend
```bash
cd uno-client
npm run test
```

## 📦 Build de Producción

### Backend
```bash
cd uno-server
./gradlew build
```

### Frontend
```bash
cd uno-client
npm run build
```

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Por favor:

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -am 'Añadir nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crea un Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## 👨‍💻 Autor

**Rodrigo Vaamonde** - [GitHub](https://github.com/rodrigovaamonde)

## 📚 Documentación Adicional

Para más detalles técnicos sobre la arquitectura y decisiones de diseño, consulta el archivo `AGENT.md`.

---

⭐ Si te gusta este proyecto, ¡no olvides darle una estrella!
