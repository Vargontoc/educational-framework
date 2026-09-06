# syntax=docker/dockerfile:1
# Multi-stage Dockerfile para My Friend Nubi Frontend
# Stage 1: Build con Node.js
# Stage 2: Serve con nginx

# ============================================
# Stage 1: Build
# ============================================
FROM node:22-alpine AS builder

WORKDIR /app

# Evitar la descarga del binario de Cypress (solo se usa en pruebas E2E, no en el build)
ENV CYPRESS_INSTALL_BINARY=0

# Copiar archivos de dependencias
COPY package*.json .npmrc ./

# Instalar dependencias. Cache mount persistente para la caché de npm: sobrevive
# a la invalidación de capas cuando cambia el código fuente (clave para iterar
# rápido en E2E, donde el build se repite en cada revisión). Id propio para no
# compartir bucket con la caché npm de otros proyectos en el mismo builder.
RUN --mount=type=cache,id=educational-framework-frontend-npm,target=/root/.npm npm ci

# Copiar código fuente
COPY . .

# Build de producción
RUN npm run build

# ============================================
# Stage 2: Production with nginx
# ============================================
FROM nginx:alpine AS production

# Copiar configuración de nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copiar artefactos de build
COPY --from=builder /app/dist /usr/share/nginx/html

# Exponer puerto
EXPOSE 80

# Comando por defecto
CMD ["nginx", "-g", "daemon off;"]
