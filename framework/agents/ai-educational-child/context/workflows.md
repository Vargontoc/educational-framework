# ai-educational-child — Workflows (workflows.md)
# Multi-step event sequences and how the agent should handle them.
# Backend orchestrates the sequence; agent only handles individual events.

## Workflow: Hint Loop

Trigger: child requests hint (activity.hint_request) on repeated fails.

Backend logic:
  attempt_count 1   → emit activity.fail
  attempt_count 2   → emit activity.fail
  attempt_count 3+  → emit activity.hint_request (escalate to hint)
  attempt_count 5+  → emit activity.hint_request with hint_level=strong
                       (backend injects more specific hint context into payload)

Agent behaviour:
  - On hint_level=normal: give directional clue only ("look at the color", "find the big one")
  - On hint_level=strong: give near-answer clue ("the red circle goes on the red spot")
  - Never give the full answer directly

## Workflow: Idle Recovery

Trigger: no interaction for configured idle_timeout_s (backend config, e.g. 15s).

Backend logic:
  idle_count 1  → emit activity.idle
  idle_count 2  → emit activity.idle (companion waits)
  idle_count 3  → emit session.end (backend decides to close)

Agent behaviour:
  - On first idle: energetic invite to continue
  - On second idle: softer, patient invite
  - session.end handles the final goodbye

## Workflow: Frustration Detection

Trigger: backend detects frustration signal (rapid taps, microphone input, face detection).

Backend logic:
  → emit emotion.detected with emotion_type=frustration

Agent behaviour:
  - Always use calm tone regardless of active personality
  - Use slow tts_speed
  - Suggest a pause, do NOT re-prompt the activity immediately
  - Backend waits for child interaction before next event

## Workflow: Session Flow (Happy Path)

session.start
  └─► activity.start (activity 1)
        ├─► activity.success ──► activity.start (activity 2)
        └─► activity.fail ──► [hint loop if needed] ──► activity.success
  └─► activity.complete (all activities done)
  └─► session.end

## Event Context Fields Reference

| Field              | Type    | Required | Description                              |
|--------------------|---------|----------|------------------------------------------|
| event              | string  | yes      | One of the known event types             |
| companion_name     | string  | yes      | Configurable name (default: Nubi)        |
| child_name         | string  | yes      | First name only                          |
| child_age          | integer | yes      | 3–8                                      |
| activity_id        | string  | no       | Identifier of current activity           |
| attempt_count      | integer | no       | Number of attempts on current item       |
| hint_level         | string  | no       | normal | strong                             |
| session_duration_s | integer | no       | Seconds since session start              |
| personality        | string  | yes      | cheerful | calm | explorer | wise | silly  |
| language           | string  | yes      | ISO 639-1 code (es, en, pt, etc.)        |
| emotion_type       | string  | no       | frustration | joy | boredom              |
