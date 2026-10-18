# API design curriculum depth review — 2026-10-17

The original 15-lesson set was too narrow for practical API design. All original lessons were re-reviewed and expanded; methods and versioning were rewritten. Five applied-pattern chapters were added. The [module](README.md) now contains 20 lessons and 100 explained checkpoints, with [comprehension](learner-progress.md) still TODO.

## Coverage decisions

| Lesson | Gap addressed | Worked decision or failure |
|---|---|---|
| [01](01-resources-and-urls.md) | Noun-only URL guidance | Identity vs selection; nested membership; relationship vs command |
| [02](02-http-methods.md) | DELETE-only example did not teach the methods | Every common method; POST/PUT/PATCH comparison; null/missing/arrays; atomic patch failure |
| [03](03-status-and-response-contracts.md) | Incomplete status selection | Status/failure-boundary table; 406 vs 415; ID precision and no-body outcomes |
| [04](04-request-validation.md) | Annotations without validation order | Parse/field/domain/database pipeline; normalization boundary; mass assignment |
| [05](05-error-contracts.md) | One exception handler without taxonomy | Problem body; framework/domain/persistence responsibility; filter errors |
| [06](06-identity-and-ownership.md) | Ownership without wider permission model | Object/collection/field/transition policy; current auth wiring; CORS/CSRF |
| [07](07-offset-pagination.md) | One insert drift example | Delete drift; Page/Slice; database-side bounds; per-post query cost |
| [08](08-cursor-pagination.md) | Predicate without full cursor protocol | size+1; tie boundary; context validation; ranked snapshot limits |
| [09](09-filtering-and-sorting.md) | Enum without filter contract | AND/OR and repeated values; wildcard vs injection; pre-pagination filtering |
| [10](10-idempotency-keys.md) | Handler without durable state decisions | Key state table; canonical fingerprint; commit/response-loss and crash boundaries |
| [11](11-conditional-updates.md) | ETag without atomic storage algorithm | Conditional SQL; strong validator scope; lost response; transformed PUT caveat |
| [12](12-caching-and-privacy.md) | no-store without freshness model | Directive comparison; fresh reuse and 304 trace; access before validation |
| [13](13-compatibility-and-versioning.md) | Additive DTO without version strategy | Four selection mechanisms; Spring configuration; expand/migrate/retire/rollback |
| [14](14-timeouts-and-async-work.md) | 202 without capacity/lifecycle model | Deadline arithmetic; rate windows; job states, leases, cancellation |
| [15](15-contract-tests-and-observability.md) | Happy-path assertions | Security/state/concurrency matrix; actual Boot 4 fixture; consumer/provider evidence |
| [16](16-content-negotiation.md) | Missing negotiation chapter | Content-Type vs Accept; 400/406/415; converter, view, and cache contracts |
| [17](17-relationships-and-commands.md) | Missing desired-state design | Toggle replay; pair uniqueness vs distinct-user count race; legal transitions |
| [18](18-aggregation-and-bff.md) | Missing screen composition trade-offs | Aggregate/BFF alternatives; 260 vs 140 ms budget; unavailable vs zero |
| [19](19-bulk-operations.md) | Missing multi-item transaction contract | Atomic vs partial effects; per-item authorization; subset replay |
| [20](20-contract-first-openapi.md) | Missing design/release review method | OpenAPI excerpt; blank vs minLength; schema vs business guarantee |

## Verification scope

- Checks passed: 20 ordered lesson/index entries, 100 explained checkpoints, 158 resolving local module links, five parseable JSON blocks, and 26 Java excerpts; required sections, answered final questions, and balanced code fences were verified. `git diff --check` passed.
- Checked the documented current PUT required fields, absent PATCH/If-Match support, feed array shape, absent entity version, like-pair uniqueness, and current authentication dependency setup against repository source.
- Inspected cached Spring 7.0.3 class signatures with `javap`: MVC version configuration, versioned GetMapping, and CacheControl methods used in examples are present. This is API-name verification, not full application compilation.
- Reviewed protocol decisions against linked primary HTTP, patch-format, Spring MVC, and OpenAPI references. Each lesson keeps proposed design separate from implemented behavior.
- Documentation-only change: no endpoint, entity, integration test, dependency, or UI behavior was changed. The teaching excerpts are not a runnable feature and were not presented as tested endpoints. Feature implementation would still need the repository’s integration/API/browser evidence workflow.

## Remaining implementation and learning work

Choose one exercise only after its prerequisite checks. Actual PATCH support, feed paging/version migration, atomic counters, durable attempt keys/jobs, cache policy, aggregation, and bulk endpoints remain proposals. No learner checkpoint attempt was supplied by this curriculum review, so no comprehension or mastery status advanced.
