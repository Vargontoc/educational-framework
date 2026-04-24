#!/usr/bin/env bash
# test_schema.sh
# Tier 2 — requires running container and loaded model.
# Sends event_activity_completed fixture and validates the response structure.
# Dependencies: docker, jq

set -euo pipefail

CONTAINER="ollama-educational"
MODEL="education-framework-agent-child"
FIXTURE_DIR="$(cd "$(dirname "$0")/fixtures" && pwd)"
FIXTURE="$FIXTURE_DIR/event_activity_completed.json"
PASS=0
FAIL=1
ERRORS=0

echo "=== [schema] education-framework-agent-child ==="

# Pre-check: jq available
if ! command -v jq &>/dev/null; then
  echo "FAIL: 'jq' is required but not found in PATH."
  exit $FAIL
fi

# Pre-check: fixture exists
if [ ! -f "$FIXTURE" ]; then
  echo "FAIL: fixture not found at $FIXTURE"
  exit $FAIL
fi

# Send payload to model
echo "Sending fixture: event_activity_completed ..."
PAYLOAD=$(cat "$FIXTURE")
PROMPT="Respond to this event: ${PAYLOAD}"

RAW=$(docker exec -i "$CONTAINER" ollama run "$MODEL" "$PROMPT" 2>/dev/null) || {
  echo "FAIL: ollama run returned non-zero exit code."
  exit $FAIL
}

# Strip any accidental markdown fences the model may emit
RESPONSE=$(echo "$RAW" | sed '/^```/d' | tr -d '\000-\010\013\014\016-\037')

echo ""
echo "--- Raw response ---"
echo "$RESPONSE"
echo "--------------------"
echo ""

assert_pass() {
  echo "  [OK] $1"
}
assert_fail() {
  echo "  [FAIL] $1"
  ERRORS=$((ERRORS + 1))
}

# 1. Valid JSON
echo "[1] Response is valid JSON"
if echo "$RESPONSE" | jq . &>/dev/null; then
  assert_pass "valid JSON"
else
  assert_fail "response is not valid JSON"
  exit $FAIL
fi

# 2. Required fields present
for field in version response_type content_text content_type suggested_actions safety_flags tool_calls; do
  echo -n "[2] Field '$field' present ... "
  if echo "$RESPONSE" | jq -e "has(\"$field\")" &>/dev/null; then
    assert_pass "$field present"
  else
    assert_fail "$field missing"
  fi
done

# 3. version == "v1"
echo -n "[3] version == 'v1' ... "
VERSION=$(echo "$RESPONSE" | jq -r '.version')
if [ "$VERSION" = "v1" ]; then
  assert_pass "version=v1"
else
  assert_fail "version='$VERSION' (expected v1)"
fi

# 4. response_type is a valid enum value
echo -n "[4] response_type is valid ... "
RT=$(echo "$RESPONSE" | jq -r '.response_type')
case "$RT" in
  narration|prompt|action|tool_call|refusal) assert_pass "response_type=$RT" ;;
  *) assert_fail "response_type='$RT' not in allowed enum" ;;
esac

# 5. content_text length <= 300
echo -n "[5] content_text length <= 300 chars ... "
CT_LEN=$(echo "$RESPONSE" | jq -r '.content_text | length')
if [ "$CT_LEN" -le 300 ]; then
  assert_pass "content_text length=$CT_LEN"
else
  assert_fail "content_text length=$CT_LEN (max 300)"
fi

# 6. content_type is a valid enum value
echo -n "[6] content_type is valid ... "
CTYPE=$(echo "$RESPONSE" | jq -r '.content_type')
case "$CTYPE" in
  plain_text|tts_snippet|structured_activity) assert_pass "content_type=$CTYPE" ;;
  *) assert_fail "content_type='$CTYPE' not in allowed enum" ;;
esac

# 7. suggested_actions is array with <= 5 items
echo -n "[7] suggested_actions is array <= 5 items ... "
SA_COUNT=$(echo "$RESPONSE" | jq '.suggested_actions | length')
if echo "$RESPONSE" | jq -e '.suggested_actions | type == "array"' &>/dev/null && [ "$SA_COUNT" -le 5 ]; then
  assert_pass "suggested_actions count=$SA_COUNT"
else
  assert_fail "suggested_actions invalid (count=$SA_COUNT or not array)"
fi

# 8. safety_flags is array
echo -n "[8] safety_flags is array ... "
if echo "$RESPONSE" | jq -e '.safety_flags | type == "array"' &>/dev/null; then
  assert_pass "safety_flags is array"
else
  assert_fail "safety_flags is not array"
fi

# 9. tool_calls is array with <= 3 items
echo -n "[9] tool_calls is array <= 3 items ... "
TC_COUNT=$(echo "$RESPONSE" | jq '.tool_calls | length')
if echo "$RESPONSE" | jq -e '.tool_calls | type == "array"' &>/dev/null && [ "$TC_COUNT" -le 3 ]; then
  assert_pass "tool_calls count=$TC_COUNT"
else
  assert_fail "tool_calls invalid (count=$TC_COUNT or not array)"
fi

# 10. No unexpected top-level keys
echo -n "[10] No unexpected top-level keys ... "
ALLOWED_KEYS='["version","response_type","content_text","content_type","suggested_actions","safety_flags","tool_calls","confidence_score"]'
UNEXPECTED=$(echo "$RESPONSE" | jq --argjson allowed "$ALLOWED_KEYS" '[keys[] | select(. as $k | $allowed | index($k) == null)] | length')
if [ "$UNEXPECTED" -eq 0 ]; then
  assert_pass "no unexpected keys"
else
  EXTRA=$(echo "$RESPONSE" | jq --argjson allowed "$ALLOWED_KEYS" '[keys[] | select(. as $k | $allowed | index($k) == null)]')
  assert_fail "unexpected keys found: $EXTRA"
fi

echo ""
if [ "$ERRORS" -eq 0 ]; then
  echo "PASSED: all schema checks passed."
  exit $PASS
else
  echo "FAILED: $ERRORS assertion(s) failed."
  exit $FAIL
fi
