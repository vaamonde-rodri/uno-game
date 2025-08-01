-- Script de inicialización para PostgreSQL en Docker
-- Este archivo se ejecuta automáticamente cuando se crea el contenedor

-- Crear extensiones útiles
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Configurar timezone
SET timezone = 'UTC';

-- Mensaje de confirmación
SELECT 'Base de datos UNO Game inicializada correctamente' as mensaje;
