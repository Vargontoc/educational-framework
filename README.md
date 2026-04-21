# AI Education Platform

This is a project for an AI education app web monousuario that provides an funny experiencie for childs 3-8 years old. The app includes a variety of educational games, interactive activities, and quizzes that help children learn while having fun. The project contains two domain agents, one for childs and other for adults. The app also includes a progress tracking system that allows parents to monitor their child's progress in the app.

App web is not a videogame, it's a web app that runs on any device with web browser and invites childs explore outside the app, and make questions to the parents. It is not a sustitutive education but a complement for their parents, they have all controlls. The app is designed to be used in conjunction with traditional education, not as a replacement.

## Stack

Java 21 + Spring Boot 3 + SpringAI · Vue 3 + TypeScript · PostgreSQL · Docker

## Quick start

{Empty}


# Expected:

{empty}

# Data persists in named volume postgres_data
# Use docker compose down -v to also remove the volume

## Production


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
AGENT.md                    → global context and git workflow
{layer}/AGENT.md            → layer-specific context and rules
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
