# GitHub Pages Learning Library Verification — 2026-11-11

## Automated structure check

Commands:

```bash
node tools/docs/generate-pages-navigation.mjs
node tools/docs/verify-pages-site.mjs
```

Result:

```text
Generated docs/_sidebar.md with 353 documents
Verified GitHub Pages site: 353 documents, 755 expandable blocks across 181 documents
```

The verifier requires every Markdown document to appear exactly once in generated navigation. It also requires matching opening/closing disclosure tags and one clickable summary for every expandable block.

## HTTP check

A temporary server exposed `docs/` on `http://127.0.0.1:4174`. Successful HTTP requests verified:

- `/` returns the Docsify entry page.
- `/_sidebar.md` returns the generated 353-document navigation.
- `/learn/kubernetes/01-containerize-spring-boot-backend/01a-from-code-to-running-program.md` returns the Kubernetes micro-lesson source.

## Browser check

Opened the Kubernetes micro-lesson through its hash route and clicked this question:

> 1. Is a Java source file a running backend?

Observed:

- The question changed from collapsed to expanded.
- The explanation beginning “No. A source file contains instructions…” became visible.
- Other questions remained collapsed.
- The generated navigation and search field remained available beside the lesson.

The [desktop screenshot](./github-pages-expandable-kubernetes-question.png) was visually inspected after capture. It shows the expanded first question, visible explanation, collapsed neighboring questions, responsive lesson column, search box, and generated explorer.

## Scope boundary

This verifies the static Pages experience. GitHub Pages does not run the Spring Boot API, PostgreSQL, Docker, or Kubernetes workloads.
