#!/bin/bash

# Script de configuración para el proyecto UNO Game con Docker/Podman
# Automatiza la configuración de PostgreSQL, migraciones y dependencias

echo "🎯 === Configuración del Proyecto UNO Game ==="

# Función para detectar el motor de contenedores disponible
detect_container_engine() {
    if command -v podman &> /dev/null; then
        if podman --version &> /dev/null; then
            echo "podman"
            return 0
        fi
    fi

    if command -v docker &> /dev/null; then
        if docker --version &> /dev/null; then
            echo "docker"
            return 0
        fi
    fi

    echo "none"
    return 1
}

# Detectar motor de contenedores
CONTAINER_ENGINE=$(detect_container_engine)

if [ "$CONTAINER_ENGINE" = "none" ]; then
    echo "❌ Ni Docker ni Podman están instalados o disponibles."
    echo ""
    echo "Opciones de instalación:"
    echo "📦 Docker: https://docs.docker.com/get-docker/"
    echo "📦 Podman: https://podman.io/getting-started/installation"
    echo ""
    echo "Para macOS:"
    echo "  brew install docker"
    echo "  brew install podman"
    exit 1
fi

echo "✅ Motor de contenedores detectado: $CONTAINER_ENGINE"

# Configurar comando de compose según el motor
if [ "$CONTAINER_ENGINE" = "podman" ]; then
    # Verificar si podman-compose está disponible
    if command -v podman-compose &> /dev/null; then
        COMPOSE_CMD="podman-compose"
        echo "✅ Usando podman-compose"
    elif podman compose version &> /dev/null 2>&1; then
        COMPOSE_CMD="podman compose"
        echo "✅ Usando podman compose (integrado)"
    else
        echo "❌ Podman Compose no está disponible."
        echo "Instala con: pip3 install podman-compose"
        echo "O actualiza Podman a una versión que incluya 'podman compose'"
        exit 1
    fi
else
    # Docker
    if docker compose version &> /dev/null 2>&1; then
        COMPOSE_CMD="docker compose"
        echo "✅ Usando docker compose (v2)"
    elif command -v docker-compose &> /dev/null; then
        COMPOSE_CMD="docker-compose"
        echo "✅ Usando docker-compose (v1)"
    else
        echo "❌ Docker Compose no está disponible."
        exit 1
    fi
fi

# Función para verificar si los servicios están corriendo
check_services() {
    echo "🔍 Verificando estado de los servicios..."

    # Verificar que los contenedores estén corriendo
    if ! $COMPOSE_CMD ps | grep -q "postgres.*Up"; then
        echo "❌ El contenedor de PostgreSQL no está corriendo"
        return 1
    fi

    # Intentar conectar a PostgreSQL
    max_attempts=30
    attempt=0

    while [ $attempt -lt $max_attempts ]; do
        if $COMPOSE_CMD exec postgres pg_isready -U uno_user -d uno_game_db &> /dev/null; then
            echo "✅ PostgreSQL está listo"

            # Verificar que podemos conectarnos desde fuera del contenedor
            if command -v psql &> /dev/null; then
                if PGPASSWORD=uno_password psql -h localhost -p 5432 -U uno_user -d uno_game_db -c "SELECT 1;" &> /dev/null; then
                    echo "✅ Conexión externa a PostgreSQL verificada"
                    return 0
                fi
            fi

            # Si no tenemos psql local, asumir que está bien si pg_isready funciona
            echo "✅ PostgreSQL responde correctamente"
            return 0
        fi

        echo "⏳ Esperando PostgreSQL... ($attempt/$max_attempts)"
        sleep 2
        attempt=$((attempt + 1))
    done

    echo "❌ Timeout: PostgreSQL no está respondiendo después de $max_attempts intentos"
    echo "💡 Verifica los logs con: $COMPOSE_CMD logs postgres"
    return 1
}

# Función para verificar las migraciones antes de ejecutarlas
check_migrations() {
    echo "🔍 Verificando archivos de migración..."

    if [ ! -d "src/main/resources/db/migration" ]; then
        echo "❌ Directorio de migraciones no encontrado"
        return 1
    fi

    migration_files=$(find src/main/resources/db/migration -name "*.sql" | wc -l)
    if [ $migration_files -eq 0 ]; then
        echo "❌ No se encontraron archivos de migración"
        return 1
    fi

    echo "✅ Encontrados $migration_files archivos de migración"
    return 0
}

# Detener contenedores existentes si están corriendo
echo "🔧 Deteniendo contenedores existentes..."
$COMPOSE_CMD down 2>/dev/null || true

# Levantar PostgreSQL y PgAdmin
echo "🐘 Iniciando PostgreSQL y PgAdmin con $CONTAINER_ENGINE..."
$COMPOSE_CMD up -d

if [ $? -ne 0 ]; then
    echo "❌ Error al iniciar los servicios"
    echo "💡 Verifica los logs con: $COMPOSE_CMD logs"
    exit 1
fi

# Esperar a que PostgreSQL esté listo
if ! check_services; then
    echo "❌ Los servicios no se iniciaron correctamente"
    echo "💡 Comandos de diagnóstico:"
    echo "   - Ver logs: $COMPOSE_CMD logs"
    echo "   - Estado: $COMPOSE_CMD ps"
    echo "   - Reiniciar: $COMPOSE_CMD restart"
    exit 1
fi

# Configurar dependencias del servidor
echo "🔧 Instalando dependencias del servidor..."
cd uno-server

# Verificar que Gradle esté disponible
if [ ! -f "./gradlew" ]; then
    echo "❌ Gradle wrapper no encontrado"
    exit 1
fi

# Configurar variables de entorno para Flyway (desarrollo local)
export FLYWAY_URL="jdbc:postgresql://localhost:5432/uno_game_db"
export FLYWAY_USER="uno_user"
export FLYWAY_PASSWORD="uno_password"

echo "🔐 Variables de entorno configuradas para desarrollo local"

# Limpiar warnings de Gradle deprecado
echo "🧹 Compilando proyecto (ignorando warnings de deprecación)..."
./gradlew clean build -x test --warning-mode=none

if [ $? -eq 0 ]; then
    echo "✅ Dependencias del servidor instaladas"
else
    echo "❌ Error al instalar dependencias del servidor"
    echo "💡 Intenta ejecutar: ./gradlew clean build --warning-mode all"
    exit 1
fi

# Verificar migraciones antes de ejecutarlas
if ! check_migrations; then
    echo "❌ Problema con las migraciones"
    exit 1
fi

# Ejecutar migraciones de Flyway
echo "🔄 Ejecutando migraciones de base de datos..."

# Primero verificar el estado de Flyway
echo "📊 Verificando estado de Flyway..."
./gradlew flywayInfo --warning-mode=none

# Ejecutar las migraciones con variables de entorno
echo "🔐 Usando credenciales de desarrollo local"
./gradlew flywayMigrate --warning-mode=none

if [ $? -eq 0 ]; then
    echo "✅ Migraciones ejecutadas correctamente"

    # Mostrar el estado final
    echo "📊 Estado final de las migraciones:"
    ./gradlew flywayInfo --warning-mode=none
else
    echo "❌ Error al ejecutar migraciones"
    echo ""
    echo "🔧 Posibles soluciones:"
    echo "1. Verificar que PostgreSQL esté corriendo: $COMPOSE_CMD ps"
    echo "2. Ver logs de PostgreSQL: $COMPOSE_CMD logs postgres"
    echo "3. Verificar conectividad: $COMPOSE_CMD exec postgres pg_isready -U uno_user -d uno_game_db"
    echo "4. Verificar variables de entorno:"
    echo "   FLYWAY_URL=$FLYWAY_URL"
    echo "   FLYWAY_USER=$FLYWAY_USER"
    echo "5. Reparar migraciones: ./gradlew flywayRepair --warning-mode=none"
    echo "6. Ver información de Flyway: ./gradlew flywayInfo --warning-mode=none"
    echo ""
    echo "Si el problema persiste, puedes ejecutar el servidor en modo desarrollo:"
    echo "./gradlew bootRun --args='--spring.profiles.active=dev' --warning-mode=none"
    exit 1
fi

cd ..

# Configurar dependencias del cliente
if [ -d "uno-client" ]; then
    echo "🔧 Instalando dependencias del cliente..."
    cd uno-client

    # Verificar que Node.js esté disponible
    if ! command -v npm &> /dev/null; then
        echo "⚠️  npm no está disponible, saltando instalación del cliente"
        echo "💡 Instala Node.js desde: https://nodejs.org/"
    else
        npm install

        if [ $? -eq 0 ]; then
            echo "✅ Dependencias del cliente instaladas"
        else
            echo "❌ Error al instalar dependencias del cliente"
            exit 1
        fi
    fi
    cd ..
fi

echo ""
echo "🎉 ¡Configuración completada exitosamente!"
echo ""
echo "📋 Información importante:"
echo "┌─────────────────────────────────────────────────────────────┐"
echo "│ SERVICIOS DISPONIBLES                                       │"
echo "├─────────────────────────────────────────────────────────────┤"
echo "│ 🐘 PostgreSQL:     localhost:5432                          │"
echo "│ 🛠️  PgAdmin:        http://localhost:5050                   │"
echo "│ 🚀 Servidor:       http://localhost:8080                   │"
echo "│ 🌐 Cliente:        http://localhost:5173                   │"
echo "│ 📖 API Docs:       http://localhost:8080/api/swagger-ui.html│"
echo "└──────────────��──────────────────────────────────────────────┘"
echo ""
echo "🔑 Credenciales de PgAdmin:"
echo "   Email: admin@uno-game.com"
echo "   Password: admin123"
echo ""
echo "🔑 Credenciales de PostgreSQL:"
echo "   Host: postgres (dentro de contenedor) o localhost (desde host)"
echo "   Database: uno_game_db"
echo "   Username: uno_user"
echo "   Password: uno_password"
echo ""
echo "🚀 Comandos para ejecutar el proyecto:"
echo ""
echo "1️⃣ Servidor (modo producción con PostgreSQL):"
echo "   cd uno-server && ./gradlew bootRun"
echo ""
echo "2️⃣ Servidor (modo desarrollo con H2):"
echo "   cd uno-server && ./gradlew bootRun --args='--spring.profiles.active=dev'"
echo ""
echo "3️⃣ Cliente (si existe):"
echo "   cd uno-client && npm run dev"
echo ""
echo "🔄 Comandos útiles de base de datos:"
echo "   Motor de contenedores: $CONTAINER_ENGINE"
echo "   Ver logs de PostgreSQL: $COMPOSE_CMD logs postgres"
echo "   Acceder a PostgreSQL:   $COMPOSE_CMD exec postgres psql -U uno_user -d uno_game_db"
echo "   Estado de servicios:    $COMPOSE_CMD ps"
echo "   Detener servicios:      $COMPOSE_CMD down"
echo "   Ejecutar migraciones:   cd uno-server && ./gradlew flywayMigrate"
echo "   Info de migraciones:    cd uno-server && ./gradlew flywayInfo"
echo ""
echo "🐛 Solución de problemas:"
echo "   - Reiniciar servicios:  $COMPOSE_CMD restart"
echo "   - Ver logs completos:   $COMPOSE_CMD logs"
echo "   - Limpiar volúmenes:    $COMPOSE_CMD down -v"
