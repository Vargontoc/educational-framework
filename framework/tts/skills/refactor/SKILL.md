# Name

TTS Refactor Skill

## When to Use

- When the API shell structure becomes unclear.
- When configuration loading spreads across multiple files.
- When future provider adapter boundaries need to be prepared without implementing providers.
- When Dockerfile or dependency layout can be simplified.

## Rules

- Prefer the smallest change that preserves the FEAT-001 scope.
- Do not introduce provider integration while refactoring the shell.
- Do not introduce backend, frontend, or infrastructure compose changes from this layer.
- Keep API routes thin and move reusable logic into service/config modules only when it is actually reused.
- Preserve placeholder behavior for synthesis until a later feature replaces it.

## After Refactoring

- Run the API shell tests.
- Rebuild the Docker image if Dockerfile or dependencies changed.
- Confirm `GET /health` and placeholder synthesis behavior remain unchanged.
