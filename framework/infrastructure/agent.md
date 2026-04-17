# Layer Context

layer: infrastructure
purpose: Manage all environment orchestration, service configuration and container lifecycle for local and production environments
does_not_own: application code, business logic, API contracts

# Environment Strategy

local: docker-compose.yml
production: docker-compose.yml + docker-compose.prod.yml (override)
env_files: infrastructure/envs/{service}.env.example -> copy to .env (gitignored)

# Services

ollama-educational:
    image: build from framework/agents/Dockerfile
    port: 11434

# Network

name: educational-network
driver: bridge
rule: All services must be an educational-network, No service exposes ports directly to host in production except frontend (80) and backend (8080)

# NVidia Configuratiin

Active capabilities for ollama-educational service use nvidia

# Skills Available

coding: add or modify service in docker-compose files
refactor: restructure environment configuration
testing: validare containers start correctly and network is reachable
design: propose new infrastructure components or enviroment strategies

# Sprint Context

current_sprint: see infrastrutucre/sprints/current.md

# Workflow

1. Read this file and the active sprint in framework/infrastruture/sprints/current.md
2. Load the relevant skill from framework/infrastructure/skills/
3. Never modify backend/ or frontend/ source files from this layer
4. Never commit real .env files - only .env.example files are committed
5. After any change to docker-compose files, validate with: docker compose config
6. Commit folowing: /infra/type/short-description

# Agent Compatibility

This file is written in plain structured natural language.
It is compatible with: Claude Code, Gemini, ChatGPT, and local models