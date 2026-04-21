# SKILL — analysis/product-discovery (v2)
# ─────────────────────────────────────────────
# Use this skill to: convert a raw idea into a structured feature map,
# prioritised roadmap and first sprint proposals — before any code is written.

## When to use this skill
- At the start of a new project — before creating any layer or sprint
- When a new major feature is proposed and its scope is unclear
- When the human says "I have an idea but I don't know where to start"
- When the backlog has grown without clear prioritisation

## Step 0 — Identify the product type
Before anything else, determine which type of product this is.
Ask the human: "Is this solving a pain someone has, or creating
an experience someone wants?"

Type A — pain product
  The user has a problem. The product eliminates or reduces it.
  Examples: task manager, invoicing tool, monitoring dashboard.
  Entry point: Step 1A

Type B — experience product
  The user has a desire. The product fulfils it.
  Examples: videogame, creative tool, interactive story, social app.
  Entry point: Step 1B

If the human is unsure, ask: "Would someone use this because
they have to solve something, or because they want to feel something?"
That answer determines the type.

## Step 1A — Pain product: extract the core problem
Ask:
  - What specific problem does this solve?
  - Who has this problem? (one specific type of person, not everyone)
  - How do they solve it today? What is painful about that?
  - What does success look like for that person?
  - What is explicitly out of scope?

Do not proceed until all five questions are answered.
If the human cannot name a specific type of person with this problem,
the idea needs more narrowing — help them find it.

## Step 1B — Experience product: extract the core experience
Ask:
  - What do you want the user to feel? (one primary emotion or sensation)
  - What is the moment in your product that must be unforgettable?
  - What other products create a similar experience?
  - What does yours do differently or better?
  - What experience is explicitly out of scope?

Do not proceed until all five questions are answered.
The core experience is the north star — every feature either
serves it or does not belong in the product.

## Step 2 — Generate the feature map
For Type A: features solve specific user problems.
For Type B: features create or sustain the core experience.

Organise into three tiers:
  core:      without these the product does not work / the experience does not exist
  important: these make the product significantly better
  future:    valuable but not needed to validate the idea

Rules for all features regardless of type:
  - Each feature must be testable: "this works" or "this doesn't"
  - Features are user-facing behaviours, not technical tasks
    WRONG: "implement pathfinding algorithm"
    RIGHT: "player can navigate the map without getting stuck"
  - Maximum 5 features per tier — more means too granular
  - For Type B: every feature must connect back to the core experience.
    If you cannot explain how it serves the feeling, cut it.

## Step 3 — Detect dependencies between features
For each feature identify:
  - Which features must exist first (hard dependency)
  - Which features make it significantly better (soft dependency)
  - Which layer owns the majority of the work

## Step 4 — Define the MVP
For Type A: smallest set that lets a real user solve their problem end-to-end.
For Type B: smallest set that already transmits the core experience,
            even if rough and incomplete.

Rules for both types:
  - MVP must be usable / playable by a real person, not just a demo
  - MVP must be achievable in 2-4 sprints maximum
  - If MVP requires more than 4 sprints, cut further
  - Nice-to-have features are never part of MVP

## Step 5 — Propose the roadmap
  phase 0 — foundation: infrastructure and auth (always first)
  phase 1 — MVP:        core features in dependency order
  phase 2 — growth:     important features after MVP is validated
  phase 3 — future:     everything else

## Step 6 — Propose first sprints
Generate sprint proposals for each layer following
docs/sprints/sprint_template.md exactly.
Flag inter-layer dependencies and blocked sprints explicitly.

## Output format
Produce three files as proposals — do not write directly:
  docs/product/vision.md
  docs/product/features.md
  docs/product/roadmap.md

Present each and wait for human review before writing.

## The most important rule
The goal is not a perfect plan.
The goal is to make implicit assumptions explicit so the human
can make informed decisions before investing time in code.

A feature map revised 50% by the human is a success.
A feature map accepted without revision is suspicious.