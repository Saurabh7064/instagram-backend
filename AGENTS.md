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

## Diary Rules

- Reuse `docs/diary/_feature-template.md` for new entries.
- Keep each entry practical:
  - what was built
  - concepts learned
  - implementation notes
  - pitfalls/fixes
  - next improvements
- For all code references, use clickable Markdown file links (not plain text paths).
