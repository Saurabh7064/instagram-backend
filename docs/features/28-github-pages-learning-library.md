# Feature 28: GitHub Pages Learning Library

## What was built

The complete committed `docs/` tree is published as a searchable static site at:

`https://saurabh7064.github.io/instagram-backend/`

The site uses hash-based routes so deeply nested lessons work on GitHub Pages without server rewrites. A generated explorer links every Markdown document, while the search box indexes lesson headings and text. Existing expandable disclosure blocks remain native clickable questions: selecting the question reveals its explanation without repeating an “Answer” label.

GitHub Pages hosts static files only. It publishes the learning library and its images/proof artifacts; it does not run the Spring Boot application, PostgreSQL, Docker, or Kubernetes workloads.

## Implementation notes

- [Pages deployment workflow](../../.github/workflows/deploy-learning-pages.yml) regenerates navigation, verifies the site, uploads `docs/`, and deploys the artifact after a `master` push that changes site content.
- [Static site entry point](../index.html) configures a repository-aware base path, hash routing, full-text search, generated navigation, and copyable code blocks.
- [Pages stylesheet](../assets/pages.css) supplies responsive reading styles and clear keyboard-accessible question expanders.
- [Navigation generator](../../tools/docs/generate-pages-navigation.mjs) discovers documents recursively, derives titles from their first heading, and creates `docs/_sidebar.md`.
- [Site verification](../../tools/docs/verify-pages-site.mjs) requires every Markdown document in the navigation and checks that every expandable block is balanced and has a clickable summary.

## Verification

Run from the backend repository:

```bash
node tools/docs/generate-pages-navigation.mjs
node tools/docs/verify-pages-site.mjs
python3 -m http.server 4174 --directory docs
curl --fail http://localhost:4174/
```

Browser verification covers desktop rendering, navigation to a Kubernetes lesson, and expanding a checkpoint by clicking its question. See the [verification record](../demo-proofs/2026-11-11/github-pages-verification.md) and [reviewed screenshot](../demo-proofs/2026-11-11/github-pages-expandable-kubernetes-question.png).

## Important deployment setting

The repository’s Pages source is **GitHub Actions**. The workflow creates and deploys the `github-pages` environment; no `gh-pages` branch is required. The successful deployment is recorded in [workflow run #2](https://github.com/Saurabh7064/instagram-backend/actions/runs/43163645475).
