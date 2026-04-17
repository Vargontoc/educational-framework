#!/bin/bash
set -e

ollama serve &
OLLAMA_PID=$!

echo "Waiting for Ollama to be ready..."
until curl -sf http://localhost:11434/api/tags > /dev/null 2>&1; do
  sleep 1
done
echo "Ollama ready."

echo "Creating model ai-educational-child..."
ollama create ai-educational-child -f /agents/ai-educational-child/Modelfile
echo "Model ready."

wait $OLLAMA_PID
