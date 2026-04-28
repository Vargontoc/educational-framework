# AI Education Platform

This is a project for an AI education app web monousuario that provides an funny experiencie for childs 3-8 years old. The app includes a variety of educational games, interactive activities, and quizzes that help children learn while having fun. The project contains two domain agents, one for childs and other for adults. The app also includes a progress tracking system that allows parents to monitor their child's progress in the app.

It's a web app that runs on any device with web browser and invites childs explore outside the app, and make questions to the parents. It is not a sustitutive education but a complement for their parents, they have all controlls. The app is designed to be used in conjunction with traditional education, not as a replacement.

## Stack

Java 21 + Spring Boot 3 + SpringAI · Vue 3 + TypeScript · PostgreSQL · Docker

## Quick start — Development

**Prerequisites:** Docker Desktop, Docker Compose, Git.

```bash
# 1. Clone and move into the project
git clone <repo-url> educational-framework
cd educational-framework

# 2. Create env files from examples (edit secrets before starting)
cp framework/infrastructure/envs/ollama.env.example framework/infrastructure/envs/ollama.env
cp framework/infrastructure/envs/postgres.env.example framework/infrastructure/envs/postgres.env

# 3. Start all services (Ollama + PostgreSQL)
docker compose -f framework/infrastructure/docker-compose.yml up -d

# 4. Verify services are healthy
docker compose -f framework/infrastructure/docker-compose.yml ps
```

**Load AI agents after the Ollama container is healthy:**

```powershell
# Run in PowerShell (Git Bash translates Linux paths incorrectly on Windows)
docker cp "framework/agents/education-framework-agent-child/Modelfile" `
    ollama-educational:/root/Modelfile

docker exec ollama-educational ollama create education-framework-agent-child `
    -f /root/Modelfile

# Verify the model is loaded
docker exec ollama-educational ollama list
```

**Data** persists in named volumes `pgdata` and `ollama_models`.
Use `docker compose down -v` to also remove the volumes.

## Quick start — Production

**Prerequisites:** Docker, Docker Compose, NVIDIA drivers (for GPU), access to secret manager or CI secrets.

```bash
# 1. Create and configure env files with real secrets (never commit these)
cp framework/infrastructure/envs/ollama.env.example framework/infrastructure/envs/ollama.env
cp framework/infrastructure/envs/postgres.env.example framework/infrastructure/envs/postgres.env
# Edit both files: replace all placeholder values with production secrets

# 2. Start with production overrides (stricter restart policy, no host port exposure)
docker compose \
  -f framework/infrastructure/docker-compose.yml \
  -f framework/infrastructure/docker-compose.prod.yml \
  up -d

# 3. Load AI agents (same as development, run in PowerShell)
docker cp framework/agents/education-framework-agent-child/Modelfile \
    ollama-educational:/root/Modelfile
docker exec ollama-educational ollama create education-framework-agent-child \
    -f /root/Modelfile
```

> Ollama is **not exposed to the host** in production — it is accessible only within `educational-network-dev`.


## Project structure
AGENT.md              → global agent context and workflow rules
analysis/             → read-only analysis agent (planning, reviews, ADRs)
docs/
  architecture/       → Architecture Decision Records (ADR-001 … ADR-006)
  contracts/          → inter-layer contracts (openapi.json, websocket.json, agent contracts, ddl)
  sprints/            → global sprint template and history
  product/            → vision, feature map and roadmap

 
framework/infrastructure/       → Docker Compose, environment configuration
framework/backend/              → Java 21 + Spring Boot 3 (hexagonal architecture)
framework/frontend/             → Vue 3 + TypeScript + Pinia + Axios
framework/agents/               → domain AI agents (Ollama Modelfiles and context)

## Working with agents
# Before starting any task, read the relevant AGENT.md:
agent.md                    → global context and git workflow
{layer}/agent.md            → layer-specific context and rules
{layer}/sprints/current.md  → active sprint status and tasks
{layer}/skills/{type}/SKILL.md → how to execute that type of task

# Agent tool: Continue.dev (VS Code / JetBrains)
# Analysis model: qwen3:14b via Ollama (http://localhost:11434)
# Domain agents: qwen2.5:7b-instruct-q5_K_M via Ollama

## Branch strategy
develop                          ← protected base branch
{layer}/type/short-description   ← working branches
# All changes arrive via PR — no direct push to develop
# See .github/BRANCH_PROTECTION.md for GitHub settings

## Key decisions
See docs/architecture/ for all Architecture Decision Records.
