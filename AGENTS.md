# Project Workflow Rules

## Definition Of Done For Any Feature

For every new feature, refactor, or bug fix that changes behavior:

1. Update code and tests.
2. Update or create a diary entry in `docs/diary/`.
3. Add exact code pointers (file paths).
4. Add commit pointers (hashes) when available.
5. Create at least one git commit for that feature/learning change.
6. Append the commit in `docs/commit-map.md` with feature and concept mapping.
7. Update backlog trackers:
   - feature status in `docs/feature-backlog.md`
   - concept status in `docs/system-design/learning-backlog.md`

If a change does not affect behavior (for example formatting-only), diary update is optional.

## Command Protocol

Use these two commands as workflow triggers:

1. `next feature`
2. `next concept`

### `next feature` behavior

Before starting:
1. Verify no previous feature is left `IN_PROGRESS` in `docs/feature-backlog.md`.
2. Verify docs + commit map are updated for the last completed change.
3. Verify both repos have clean git working trees.

Then:
1. Pick the next `TODO` feature from `docs/feature-backlog.md` (top-down unless user overrides).
2. Mark it `IN_PROGRESS`.
3. Build it end-to-end.
4. Mark it `DONE`.
5. Update diary/docs and `docs/commit-map.md`.
6. Commit all related changes.

### `next concept` behavior

Before starting:
1. Verify no previous concept is left `IN_PROGRESS` in `docs/system-design/learning-backlog.md`.
2. Verify docs + commit map are updated for the last completed concept.
3. Verify both repos have clean git working trees.

Then:
1. Pick next `TODO` concept from `docs/system-design/learning-backlog.md` (top-down unless user overrides).
2. Mark it `IN_PROGRESS`.
3. Add/expand concept note with project mapping.
4. Add at least one concrete implementation task linked to a feature.
5. Mark concept `DONE` (or keep `IN_PROGRESS` if implementation is intentionally deferred).
6. Update `docs/commit-map.md`.
7. Commit all related changes.

## Diary Rules

- Reuse `docs/diary/_feature-template.md` for new entries.
- Keep each entry practical:
  - what was built
  - concepts learned
  - implementation notes
  - pitfalls/fixes
  - next improvements
- For all code references, use clickable Markdown file links (not plain text paths).
