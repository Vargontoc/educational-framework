# ai-educational-child — Validation

Tests all 5 personalities against all 9 event types (65 combinations) against local Ollama.

## Requirements

- Python 3.10+
- `pip install requests`
- Ollama running locally with the base model pulled:
  ```
  ollama pull qwen2.5:7b-instruct-q5_K_M
  ```

## Run

```bash
# From this directory
python validate.py

# Custom host or run ID
python validate.py --host http://localhost:11434 --run-id run-002
```

## What it checks

| Check | Rule |
|---|---|
| Word count | Response text ≤ 35 words |
| JSON format | Response is valid JSON with text, emotion, tts_speed |
| Emotion enum | One of: happy, encouraging, calm, curious, playful, proud |
| tts_speed enum | One of: normal, slow, fast |
| Banned words | No: wrong, failed, bad, mistake, incorrect |

## Output

Results are saved to `tests/results/{run-id}.json` with per-test detail:
- status (PASS / FAIL / ERROR)
- word_count
- json_valid
- violations list
- full response text

## Note on Modelfile variables

The Modelfile uses `{placeholder}` syntax that Ollama does not resolve natively.
`validate.py` assembles the final system prompt by substituting all variables
before each API call — this is the same responsibility the backend will have in production.
