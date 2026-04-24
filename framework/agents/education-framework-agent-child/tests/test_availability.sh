#!/usr/bin/env bash
# test_availability.sh
# Tier 1 — CI-safe. No model call required.
# Asserts: container is running AND the model is loaded in Ollama.

set -euo pipefail

CONTAINER="ollama-educational"
MODEL="education-framework-agent-child"
PASS=0
FAIL=1

echo "=== [availability] education-framework-agent-child ==="

# 1. Container running
echo -n "[1/2] Container '$CONTAINER' is running ... "
if ! docker ps --filter "name=^${CONTAINER}$" --filter "status=running" --format "{{.Names}}" | grep -q "^${CONTAINER}$"; then
  echo "FAIL"
  echo "    Container '$CONTAINER' is not running."
  echo "    Run: docker compose -f framework/infrastructure/docker-compose.yml up -d"
  exit $FAIL
fi
echo "OK"

# 2. Model loaded
echo -n "[2/2] Model '$MODEL' is loaded in Ollama ... "
if ! docker exec "$CONTAINER" ollama list 2>/dev/null | grep -q "$MODEL"; then
  echo "FAIL"
  echo "    Model '$MODEL' not found in ollama list."
  echo "    Run: docker cp framework/agents/education-framework-agent-child/Modelfile $CONTAINER:/tmp/Modelfile"
  echo "    Then: docker exec $CONTAINER ollama create $MODEL -f /tmp/Modelfile"
  exit $FAIL
fi
echo "OK"

echo ""
echo "PASSED: availability checks complete."
exit $PASS
