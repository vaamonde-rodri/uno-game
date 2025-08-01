# 🚀 Guía de Despliegue - UNO Game

Esta guía explica cómo desplegar el proyecto UNO Game en diferentes entornos usando variables de entorno para las credenciales.

## 🔒 Configuración de Seguridad

### Variables de Entorno Requeridas

Para **Flyway** (migraciones):
```bash
FLYWAY_URL=jdbc:postgresql://host:port/database
FLYWAY_USER=username
FLYWAY_PASSWORD=password
```

Para **Spring Boot** (aplicación):
```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://host:port/database
SPRING_DATASOURCE_USERNAME=username
SPRING_DATASOURCE_PASSWORD=password
SPRING_PROFILES_ACTIVE=prod
```

## 🏠 Desarrollo Local

### Opción 1: Usando el script automático
```bash
./setup.sh
```

### Opción 2: Configuración manual
```bash
# 1. Configurar variables de entorno
export FLYWAY_URL="jdbc:postgresql://localhost:5432/uno_game_db"
export FLYWAY_USER="uno_user"
export FLYWAY_PASSWORD="uno_password"

# 2. Iniciar servicios
docker compose up -d

# 3. Ejecutar migraciones
cd uno-server
./gradlew flywayMigrate

# 4. Iniciar aplicación
./gradlew bootRun
```

## ☁️ Despliegue en Heroku

### 1. Configurar variables de entorno en Heroku
```bash
heroku config:set SPRING_PROFILES_ACTIVE=prod
heroku config:set SPRING_DATASOURCE_URL="jdbc:postgresql://your-heroku-postgres-url"
heroku config:set SPRING_DATASOURCE_USERNAME="your-user"
heroku config:set SPRING_DATASOURCE_PASSWORD="your-password"

# Para migraciones
heroku config:set FLYWAY_URL="jdbc:postgresql://your-heroku-postgres-url"
heroku config:set FLYWAY_USER="your-user"
heroku config:set FLYWAY_PASSWORD="your-password"
```

### 2. Ejecutar migraciones en Heroku
```bash
heroku run ./gradlew flywayMigrate --app your-app-name
```

## 🐳 Despliegue con Docker

### 1. Dockerfile para producción
```dockerfile
FROM openjdk:21-jdk-slim

WORKDIR /app
COPY build/libs/*.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
```

### 2. Docker Compose para producción
```yaml
version: '3.8'
services:
  app:
    build: .
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=${DATABASE_URL}
      - SPRING_DATASOURCE_USERNAME=${DB_USER}
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
    depends_on:
      - postgres
  
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: ${DB_NAME}
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
```

## ☸️ Despliegue en Kubernetes

### 1. Secret para credenciales
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: uno-game-secrets
type: Opaque
stringData:
  database-url: "jdbc:postgresql://postgres:5432/uno_game_db"
  database-user: "uno_user"
  database-password: "your-secure-password"
```

### 2. Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: uno-game
spec:
  replicas: 3
  selector:
    matchLabels:
      app: uno-game
  template:
    metadata:
      labels:
        app: uno-game
    spec:
      containers:
      - name: uno-game
        image: uno-game:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: uno-game-secrets
              key: database-url
        - name: SPRING_DATASOURCE_USERNAME
          valueFrom:
            secretKeyRef:
              name: uno-game-secrets
              key: database-user
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: uno-game-secrets
              key: database-password
```

## 🚀 CI/CD con GitHub Actions

### Ejemplo de workflow
```yaml
name: Deploy UNO Game

on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'
    
    - name: Run migrations
      env:
        FLYWAY_URL: ${{ secrets.DATABASE_URL }}
        FLYWAY_USER: ${{ secrets.DB_USER }}
        FLYWAY_PASSWORD: ${{ secrets.DB_PASSWORD }}
      run: |
        cd uno-server
        ./gradlew flywayMigrate
    
    - name: Build application
      run: |
        cd uno-server
        ./gradlew build
    
    - name: Deploy to production
      env:
        SPRING_PROFILES_ACTIVE: prod
        SPRING_DATASOURCE_URL: ${{ secrets.DATABASE_URL }}
        SPRING_DATASOURCE_USERNAME: ${{ secrets.DB_USER }}
        SPRING_DATASOURCE_PASSWORD: ${{ secrets.DB_PASSWORD }}
      run: |
        # Comandos específicos de tu plataforma de despliegue
```

## 🔧 Comandos Útiles

### Verificar configuración
```bash
# Ver variables de entorno actuales
env | grep -E "(FLYWAY|SPRING)"

# Verificar estado de migraciones
./gradlew flywayInfo

# Reparar migraciones si hay problemas
./gradlew flywayRepair
```

### Migraciones específicas
```bash
# Migrar a una versión específica
./gradlew flywayMigrate -Pflyway.target=2.1

# Limpiar base de datos (¡CUIDADO EN PRODUCCIÓN!)
./gradlew flywayClean

# Baseline para bases de datos existentes
./gradlew flywayBaseline
```

## 🛡️ Mejores Prácticas de Seguridad

1. **Nunca hardcodear credenciales** en el código
2. **Usar secretos** específicos de cada entorno
3. **Rotar credenciales** regularmente
4. **Usar HTTPS** en producción
5. **Limitar permisos** de base de datos
6. **Monitorear accesos** y conexiones
7. **Backup regular** de la base de datos

## 🚨 Solución de Problemas

### Error de conexión a base de datos
1. Verificar que las variables de entorno estén configuradas
2. Comprobar conectividad de red
3. Validar credenciales
4. Revisar logs de la aplicación

### Error en migraciones
1. Verificar estado: `./gradlew flywayInfo`
2. Reparar si es necesario: `./gradlew flywayRepair`
3. Revisar archivos de migración
4. Verificar permisos de base de datos

### Problemas de rendimiento
1. Configurar pool de conexiones
2. Optimizar consultas
3. Añadir índices necesarios
4. Monitorear métricas de base de datos
