# SKILL — analysis/sprint-review
# ─────────────────────────────────────────────
# Use this skill to: generate the Review section of a completed sprint
# and prepare it for archiving in {layer}/sprints/history/.

## Trigger
This skill is activated when:
  - All tasks in {layer}/sprints/current.md are checked
  - Or the human explicitly requests a sprint close

## Before Reviewing
1. Read {layer}/sprints/current.md in full
2. Read git log for commits in {layer}/ since started_at date
3. Check docs/contracts/openapi.json for changes made during the sprint
4. Check if any other layer was unblocked as a result of this sprint

## Review Output
Produce the completed Review section following the sprint template:
  - completed_tasks:     list of checked tasks with brief note
  - incomplete_tasks:    list of unchecked tasks and reason why
  - contract_changes:    any changes made to docs/contracts/ during sprint
  - learnings:           decisions taken, blockers found and how resolved
  - next_sprint_suggestions: proposed tasks for the next sprint

## After Review
Set closed_at to today's date.
Set status to completed.
Instruct the human to:
  1. Review and approve the generated Review section
  2. Move current.md to {layer}/sprints/history/YYYY-MM-DD-sprint-{n}.md
  3. Create a new current.md for the next sprint if needed

## Output Format
Present the completed sprint file ready to copy.
Do not move or modify files directly — wait for human confirmation.