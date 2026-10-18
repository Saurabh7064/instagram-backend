# API Design with Spring Boot

Learn to design the contract first, then connect it to a Spring Boot implementation. This module contains **20 lessons with worked design decisions** in prerequisite order, using posts, likes, media, and feeds from this Instagram backend.

## How to study

Start with lesson 01. Predict each request result, read the explanation and code walkthrough, attempt the design task, and answer the checkpoints before opening the explained answers. Study one lesson at a time. No lesson is marked learned merely because its file exists.

These are documentation artifacts. Proposed routes, services, tests, database changes, and DTOs are explicitly illustrative; they have not been installed into the application or verified as working endpoints. Current behavior is linked to actual source. Use the study passes indicated in each chapter. The methods and versioning chapters deliberately take several focused sessions; do not compress every design decision into one sitting.

## Lesson sequence

| Lesson | Main design decisions | Artifact | Learning |
|---|---|---|---|
| [01. Resources and URL design](01-resources-and-urls.md) | Identity vs selection; nesting; relationship resources; commands | READY | TODO |
| [02. HTTP methods: GET, HEAD, POST, PUT, PATCH, DELETE, and OPTIONS](02-http-methods.md) | Every common method; POST vs PUT vs PATCH; Merge Patch vs JSON Patch; atomicity | READY | TODO |
| [03. Status codes and response contracts](03-status-and-response-contracts.md) | Status decision table; response types; ID precision; no-body responses | READY | TODO |
| [04. Request validation and write boundaries](04-request-validation.md) | Validation pipeline; null/blank/normalization; mass assignment; durable invariants | READY | TODO |
| [05. Consistent error bodies](05-error-contracts.md) | Problem Details; field codes; exception responsibility; safe diagnostics | READY | TODO |
| [06. Identity and ownership checks](06-identity-and-ownership.md) | Object/collection/field/transition permissions; CORS vs CSRF; token model | READY | TODO |
| [07. Offset pagination and bounded reads](07-offset-pagination.md) | SQL bounds; Page vs Slice; insertion/deletion drift; N+1 query cost | READY | TODO |
| [08. Cursor pagination for a live feed](08-cursor-pagination.md) | Tie-breakers; size+1; cursor context; indexes; ranked-feed limits | READY | TODO |
| [09. Filtering and sorting contracts](09-filtering-and-sorting.md) | AND/OR semantics; repeated filters; allowlisted sort; query cost | READY | TODO |
| [10. Idempotency keys for create requests](10-idempotency-keys.md) | Scoped keys; fingerprints; crash/replay states; transactions; outbox | READY | TODO |
| [11. Conditional updates and lost writes](11-conditional-updates.md) | Strong validators; atomic update; 412/428; response loss; representation scope | READY | TODO |
| [12. Caching and private representations](12-caching-and-privacy.md) | no-store/private/no-cache; freshness; revalidation; 304; cache isolation | READY | TODO |
| [13. Compatibility, API versioning, and client migration](13-compatibility-and-versioning.md) | Path/query/header/media type; MVC version resolver; expand/migrate/retire/rollback | READY | TODO |
| [14. Timeouts, rate limits, and asynchronous work](14-timeouts-and-async-work.md) | Deadline arithmetic; rate limiting; retry budgets; durable jobs; cancellation | READY | TODO |
| [15. Contract tests and observable API behavior](15-contract-tests-and-observability.md) | Contract matrix; committed-state assertions; consumer/provider tests; diagnostics | READY | TODO |
| [16. Content negotiation and representation design](16-content-negotiation.md) | Content-Type vs Accept; consumes/produces; 400/406/415; views and cache variation | READY | TODO |
| [17. Relationship resources, commands, and invariants](17-relationships-and-commands.md) | Desired-state likes/follows; commands; pair uniqueness vs counter races | READY | TODO |
| [18. API aggregation and Backend for Frontend](18-aggregation-and-bff.md) | Client composition vs aggregate vs BFF; fan-out budgets; partial failure | READY | TODO |
| [19. Bulk operations and partial success](19-bulk-operations.md) | Atomic vs partial batches; item outcomes; per-item permissions; subset replay | READY | TODO |
| [20. Contract-first design and an API review rubric](20-contract-first-openapi.md) | OpenAPI sketch; schema/behavior gap; operation review and release rubric | READY | TODO |

## Study passes

- **Contract foundation (01–06):** identity, methods, responses, validation, errors, and permissions.
- **Collections and reliable writes (07–12):** paging, selection, replay, concurrency, and caches.
- **Evolution and operation (13–15):** version migration, capacity, and verification.
- **Applied patterns (16–20):** negotiation, relationship commands, aggregation/BFF, bulk work, and contract-first review.

A pattern is a choice with costs, not a universal best practice. Each chapter identifies when the simpler alternative is sufficient. All learning statuses remain TODO until a learner attempt.

## Integrated design exercise

After the individual stop/go checks, design F-017 comments: identify collection/item URLs; write create/read/delete request and response examples; define ownership and validation; choose bounded pagination; describe retry and compatibility policy; list errors and contract tests. Compare the proposal against current posts code and label every unimplemented component. Implementation remains a separate feature task.

## Vocabulary recap

| Term | Meaning |
|---|---|
| Contract | Promised request, response, and externally visible behavior |
| Resource | An identifiable thing exposed by the API |
| DTO | Selected request/response data, separated from persistence entities |
| Idempotent | Repetition has the same intended server effect as one request |
| Cursor | Continuation boundary in an ordered collection |
| Validator | Value used to check whether a representation is current |
| Compatibility | Existing supported clients retain their promised behavior |

## Primary references

The expanded methods and versioning examples also link [HTTP PATCH](https://www.rfc-editor.org/rfc/rfc5789.html), [Merge Patch](https://www.rfc-editor.org/rfc/rfc7396.html), [JSON Patch](https://www.rfc-editor.org/rfc/rfc6902.html), [Spring MVC versioning](https://docs.spring.io/spring-framework/reference/web/webmvc-versioning.html), [Problem Details](https://www.rfc-editor.org/rfc/rfc9457.html), [Deprecation](https://www.rfc-editor.org/rfc/rfc9745.html), and [OpenAPI 3.1.1](https://spec.openapis.org/oas/v3.1.1.html). Framework API names used in versioning/cache examples were inspected in the locally cached Spring 7.0.3 JARs; the teaching excerpts still require application wiring and feature tests.

Use [HTTP Semantics, RFC 9110](https://www.rfc-editor.org/rfc/rfc9110.html) for method, status, and conditional-request definitions. Spring-specific error examples follow the official [Spring MVC error response documentation](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html). For independent details, consult [Spring MVC request validation](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-validation.html), [Spring Data scrolling](https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html#jpa.query-methods.scroll), and [HTTP caching, RFC 9111](https://www.rfc-editor.org/rfc/rfc9111.html). The project’s actual dependency versions remain authoritative for adapting snippets.

[Review coverage and verification](review-notes.md) · [Learning evidence](learner-progress.md) · [System design interview home](../README.md)
