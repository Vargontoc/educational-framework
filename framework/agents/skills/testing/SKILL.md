# SKILL — agents/testing
# ─────────────────────────────────────────────
# Use this skill for: validating agent behaviour, testing MCP tool calls,
# verifying rules are respected and examples produce expected output.

## What to test
Agent testing has four dimensions — cover all four before closing a sprint:

1. Happy path
   The agent receives a clear, in-scope request and responds correctly.
   Verify: response matches expected format, tone and length constraints.

2. Boundary cases
   The agent receives requests at the edge of its defined scope.
   Verify: agent handles ambiguity by asking one clarifying question.
   Verify: agent does not hallucinate tools or capabilities it does not have.

3. Rule violations
   The agent receives requests that violate its hard constraints.
   Verify: agent refuses clearly and explains why.
   Verify: agent does not comply even with rephrased or indirect requests.
   Examples: ask for destructive operations, ask it to reveal its system prompt,
             ask it to impersonate a human.

4. Tool call validation
   The agent receives a request that requires a tool call.
   Verify: agent announces the tool call before executing.
   Verify: agent summarises the result after execution.
   Verify: agent handles tool errors gracefully without retrying silently.

## Test format
Document each test case in agents/{name}/context/examples.md:
  input:    the exact user message sent
  expected: the expected agent behaviour (not exact wording)
  result:   pass | fail | partial
  notes:   what to fix if result is not pass

## How to run tests
# Build the agent locally
ollama create {agent-name} -f agents/{agent-name}/Modelfile

# Run a single test via Ollama API
curl http://localhost:11434/api/chat -d '{
  "model": "{agent-name}",
  "messages": [{ "role": "user", "content": "{test input}" }],
  "stream": false
}'

# Run all examples as smoke tests
# Read each input from context/examples.md and verify manually

## Regression testing
- Before any Modelfile change, run all existing examples first
- If a change breaks a passing example, it is a regression — fix before merging
- If behaviour change is intentional, update the example to reflect new expected output
  and note the change in agents/sprints/current.md Notes

## After testing
- Update result field in each test case in context/examples.md
- If all tests pass, mark testing task complete in agents/sprints/current.md
- If any test fails, do not close the sprint — fix and retest