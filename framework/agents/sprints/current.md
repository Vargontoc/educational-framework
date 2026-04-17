# Sprint 3 - framework/agents
# -----------------------------------------------

## Goal
Implement the three remaining personalities for ai-educational-child: explorer, wise and silly.

## Status
status:     completed
started_at: 2026-04-17 00:00:00
closed_at:  2026-04-17 00:00:00
blocked_by:
waiting_for:

## Tasks
- [x] Write personalities/explorer.md
- [x] Write personalities/wise.md
- [x] Write personalities/silly.md
- [x] Add few-shot examples for each new personality to context/examples.md (min 2 per event type)
- [x] Update docs/contracts/agents/ai-educational-child.json — personalities_available.v1 extended

## Risks
- explorer and silly may push vocabulary complexity up — must enforce age-appropriate limits in the personality prompt itself
- wise uses a Socratic style (questions) but rules.md bans open-ended questions — personality must use guided yes/no questions only
- silly uses humor and onomatopoeia — must stay within child-safe content and avoid repetitive gimmicks

## Dependencies
- personalities/cheerful.md and personalities/calm.md (Sprint 1) — confirmed present, used as reference
- context/rules.md hard constraints apply to all personalities without exception

## Agent Instruction
- Personality files go in framework/agents/ai-educational-child/personalities/
- Each file follows the same structure as cheerful.md and calm.md: name, target_age, energy, tone, Prompt Injection, Tone Examples, tts_speed Bias, Coletillas
- Coletillas (basketball + nursing) apply to all three personalities with appropriate tone adaptation
- examples.md additions go at the bottom under a new personality section header
- Contract bump: 1.1.0 → 1.2.0 (non-breaking: personalities_available.v1 list extended)

## Notes
- explorer: curiosity-driven, every activity is an adventure or discovery
- wise: calm and reflective, age 6-8 focused, guides with simple yes/no questions
- silly: playful humor, onomatopoeia, light silliness — must not overpower the activity feedback

## Review

completed_tasks:
  - personalities/explorer.md: curiosity-driven, adventure framing, discovery vocabulary, coletillas scouting/diagnóstico
  - personalities/wise.md: Socratic guided yes/no questions only, age 6-8, understated celebration, coletillas calm variants
  - personalities/silly.md: onomatopoeia (Boing/Zap/Whoosh), one gag per response rule, full silliness suppressed on emotion.detected
  - context/examples.md: 2 examples × 9 events × 3 personalities = 54 new examples + 1 negative silly/emotion.detected
  - docs/contracts/agents/ai-educational-child.json bumped 1.1.0 → 1.2.0, personalities_available.v1 now complete

incomplete_tasks:
  none

contract_changes:
  - docs/contracts/agents/ai-educational-child.json bumped to v1.2.0
  - personalities_available.v1: ["cheerful","calm","explorer","wise","silly"] — planned list now empty
  - Non-breaking change: no input/output/event schema altered

learnings:
  - wise personality required explicit clarification that Socratic questions must be yes/no or forced-choice — open-ended questions are forbidden by rules.md even in wise mode
  - silly personality needs a hard rule: drop ALL silliness on emotion.detected — added negative example to reinforce this
  - All 5 personalities now share the same coletilla frequency rules; only tone and variants differ

next_sprint_suggestions:
  - Sprint 4: local Ollama validation — test all 5 personalities against all 9 events, measure word count, verify JSON output
