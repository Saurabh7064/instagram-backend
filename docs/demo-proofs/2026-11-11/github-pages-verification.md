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
- Clicking the generated `01B - Image, Container, and Dockerfile` sidebar link changed the route and rendered the 01B lesson heading; sidebar routes are resolved from the site root rather than beneath the current lesson.

The [desktop screenshot](./github-pages-expandable-kubernetes-question.png) was visually inspected after capture. It shows the expanded first question, visible explanation, collapsed neighboring questions, responsive lesson column, search box, and generated explorer.

## Scope boundary

This verifies the static Pages experience. GitHub Pages does not run the Spring Boot API, PostgreSQL, Docker, or Kubernetes workloads.

## Live deployment

GitHub Pages was enabled with **GitHub Actions** as its source. [Workflow run #2](https://github.com/Saurabh7064/instagram-backend/actions/runs/43163645475) completed successfully: the build and deploy jobs both passed and published one Pages artifact.

Live HTTP verification succeeded for:

- `https://saurabh7064.github.io/instagram-backend/`
- `https://saurabh7064.github.io/instagram-backend/kubernetes/01-containerize-spring-boot-backend/01a-from-code-to-running-program.md`

The public browser check opened the same Kubernetes lesson and expanded “1. Is a Java source file a running backend?”. Its explanation became visible while neighboring questions remained collapsed.
