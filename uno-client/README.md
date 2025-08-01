# 🎮 UNO Client - Frontend

Aplicación frontend React con TypeScript para el juego UNO multijugador en tiempo real.

## 🚀 Características

- ✅ **Interfaz moderna** con React 19 y TypeScript
- ✅ **Comunicación en tiempo real** via WebSockets
- ✅ **Responsive design** para móviles y desktop
- ✅ **Internacionalización (i18n)** con soporte multiidioma
- ✅ **Gestión de estado** optimizada para juegos en tiempo real
- ✅ **Hot reload** con Vite para desarrollo rápido
- ✅ **Validación de tipos** con TypeScript estricto
- ✅ **Linting** automático con ESLint

## 🛠️ Stack Tecnológico

- **TypeScript** - Lenguaje base con tipado estático
- **React 19.1.0** - Framework de UI
- **Vite 7.0.4** - Build tool y servidor de desarrollo
- **SockJS Client** - Cliente WebSocket para comunicación en tiempo real
- **STOMP** - Protocolo de mensajería sobre WebSockets
- **React i18next** - Internacionalización
- **CSS Modules / Tailwind** - Estilos
- **ESLint** - Linting de código

## 📦 Configuración y Desarrollo

### Prerrequisitos
- Node.js 18+
- npm o yarn

### Setup Rápido

1. **Desde la raíz del proyecto:**
```bash
# Esto configura tanto backend como frontend
./setup.sh
```

2. **Solo frontend (desde esta carpeta):**
```bash
# Instalar dependencias
npm install

# Iniciar servidor de desarrollo
npm run dev
```

### Scripts Disponibles

```bash
# Desarrollo con hot reload
npm run dev

# Build para producción
npm run build

# Vista previa del build
npm run preview

# Linting
npm run lint

# Formateo de código
npm run format
```

## 🏗️ Arquitectura del Frontend

### Estructura del Código

```
src/
├── components/        # Componentes reutilizables
│   ├── Game/         # Componentes específicos del juego
│   ├── UI/           # Componentes de interfaz general
│   └── Layout/       # Componentes de layout
├── pages/            # Páginas/vistas principales
│   ├── Home/         # Página de inicio
│   ├── Game/         # Vista del juego
│   └── Lobby/        # Sala de espera
├── hooks/            # Hooks personalizados
│   ├── useWebSocket/ # Hook para WebSocket
│   ├── useGame/      # Hook para estado del juego
│   └── usePlayer/    # Hook para datos del jugador
├── services/         # Servicios de comunicación
│   ├── api.ts        # Cliente API REST
│   ├── websocket.ts  # Cliente WebSocket
│   └── gameService.ts # Lógica de comunicación del juego
├── types/            # Definiciones de tipos TypeScript
│   ├── game.ts       # Tipos del juego
│   ├── player.ts     # Tipos del jugador
│   └── websocket.ts  # Tipos de WebSocket
├── locales/          # Archivos de traducción
│   ├── en/           # Inglés
│   └── es/           # Español
├── utils/            # Utilidades y helpers
└── styles/           # Estilos globales
```

### Comunicación con el Backend

El frontend se comunica con el backend de dos formas:

1. **API REST** - Para operaciones iniciales:
   - Crear partidas
   - Unirse a partidas
   - Obtener estado inicial

2. **WebSockets** - Para tiempo real:
   - Movimientos de juego
   - Actualizaciones de estado
   - Notificaciones a jugadores

## 🔧 Desarrollo

### Configuración del Entorno

El frontend se conecta automáticamente al backend en:
- **Desarrollo**: `http://localhost:8080`
- **Producción**: Variable de entorno `VITE_API_URL`

### Variables de Entorno

Crear archivo `.env.local`:

```bash
# URL del backend
VITE_API_URL=http://localhost:8080

# WebSocket URL (opcional, se deriva de API_URL)
VITE_WS_URL=ws://localhost:8080

# Idioma por defecto
VITE_DEFAULT_LOCALE=es
```

### Desarrollo con Hot Reload

```bash
npm run dev
```

El servidor de desarrollo estará disponible en `http://localhost:5173`

### Linting y Formateo

```bash
# Verificar linting
npm run lint

# Arreglar problemas automáticamente
npm run lint:fix

# Formatear código
npm run format
```

## 🎨 Componentes Principales

### Componentes del Juego
- **GameBoard** - Tablero principal del juego
- **PlayerHand** - Cartas en mano del jugador
- **GameCard** - Componente individual de carta
- **GameControls** - Controles de acción (UNO, robar carta, etc.)

### Componentes de UI
- **Button** - Botones reutilizables
- **Modal** - Modales para diálogos
- **Loader** - Indicadores de carga
- **Notification** - Sistema de notificaciones

## 🌐 Internacionalización

El proyecto soporta múltiples idiomas:

```typescript
// Usar en componentes
import { useTranslation } from 'react-i18next';

const MyComponent = () => {
  const { t } = useTranslation();
  return <h1>{t('game.title')}</h1>;
};
```

Archivos de traducción en `src/locales/[locale]/`.

## 📱 Responsive Design

La aplicación está optimizada para:
- **Desktop** (1024px+)
- **Tablet** (768px - 1023px)
- **Mobile** (< 768px)

## 🔍 Testing

```bash
# Ejecutar tests
npm run test

# Tests en modo watch
npm run test:watch

# Cobertura de tests
npm run test:coverage
```

## 🚀 Build y Despliegue

```bash
# Build para producción
npm run build

# Los archivos se generan en dist/
```

Para instrucciones de despliegue, consulta [`../DEPLOYMENT.md`](../DEPLOYMENT.md).

## 🐛 Debugging

### DevTools
- React DevTools
- Redux DevTools (si se usa)
- WebSocket debugging en Network tab

### Logs
```typescript
// Habilitar logs de desarrollo
localStorage.setItem('debug', 'uno:*');
```
