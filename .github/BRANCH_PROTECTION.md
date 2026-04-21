# Branch Protection Rules — develop
# ─────────────────────────────────────────────
# Apply these settings in:
# GitHub → Settings → Branches → Add rule → develop

## Required settings

Require a pull request before merging:         ON
Require approvals:                              OFF (single developer)
Require status checks to pass before merging:   ON
Require branches to be up to date before merging: ON
Do not allow bypassing the above settings:      ON

## Required status checks
# Add the checks that correspond to the layer being merged:

infrastructure PRs: CI — Infrastructure / validate


## Branch naming enforcement
# GitHub does not enforce branch naming natively.
# The agent must follow this convention without enforcement:
#   framework/{layer}/type/short-description
#   e.g. backend/feature/user-auth
#        frontend/fix/login-redirect
#        infrastructure/chore/add-healthcheck

## Direct push to develop
Block direct pushes: ON
# No agent or human should push directly to develop.
# All changes arrive via PR from a layer branch.