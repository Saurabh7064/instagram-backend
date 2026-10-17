# API Design with Spring Boot

Learn to design the contract first, then connect it to a Spring Boot implementation. This module contains **15 focused lessons** in prerequisite order, using posts, likes, media, and feeds from this Instagram backend.

## How to study

Start with lesson 01. Predict each request result, read the explanation and code walkthrough, attempt the design task, and answer the checkpoints before opening the explained answers. Study one lesson at a time. No lesson is marked learned merely because its file exists.

These are documentation artifacts. Proposed routes, services, tests, database changes, and DTOs are explicitly illustrative; they have not been installed into the application or verified as working endpoints. Current behavior is linked to actual source. Each lesson takes about 15–20 minutes; split practice into another session if needed.

## Lesson sequence

| Lesson | Primary idea | Artifact | Learning |
|---|---|---|---|
| [01. Resources and URL design](01-resources-and-urls.md) | Resources and URL design | READY | TODO |
| [02. HTTP methods and repeatable effects](02-http-methods.md) | HTTP methods and repeatable effects | READY | TODO |
| [03. Status codes and response contracts](03-status-and-response-contracts.md) | Status codes and response contracts | READY | TODO |
| [04. Request validation and write boundaries](04-request-validation.md) | Request validation and write boundaries | READY | TODO |
| [05. Consistent error bodies](05-error-contracts.md) | Consistent error bodies | READY | TODO |
| [06. Identity and ownership checks](06-identity-and-ownership.md) | Identity and ownership checks | READY | TODO |
| [07. Offset pagination and bounded reads](07-offset-pagination.md) | Offset pagination and bounded reads | READY | TODO |
| [08. Cursor pagination for a live feed](08-cursor-pagination.md) | Cursor pagination for a live feed | READY | TODO |
| [09. Filtering and sorting contracts](09-filtering-and-sorting.md) | Filtering and sorting contracts | READY | TODO |
| [10. Idempotency keys for create requests](10-idempotency-keys.md) | Idempotency keys for create requests | READY | TODO |
| [11. Conditional updates and lost writes](11-conditional-updates.md) | Conditional updates and lost writes | READY | TODO |
| [12. Caching and private representations](12-caching-and-privacy.md) | Caching and private representations | READY | TODO |
| [13. API evolution and compatibility](13-compatibility-and-versioning.md) | API evolution and compatibility | READY | TODO |
| [14. Timeouts, rate limits, and asynchronous work](14-timeouts-and-async-work.md) | Timeouts, rate limits, and asynchronous work | READY | TODO |
| [15. Contract tests and observable API behavior](15-contract-tests-and-observability.md) | Contract tests and observable API behavior | READY | TODO |

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

Use [HTTP Semantics, RFC 9110](https://www.rfc-editor.org/rfc/rfc9110.html) for method, status, and conditional-request definitions. Spring-specific error examples follow the official [Spring MVC error response documentation](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html). For independent details, consult [Spring MVC request validation](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-validation.html), [Spring Data scrolling](https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html#jpa.query-methods.scroll), and [HTTP caching, RFC 9111](https://www.rfc-editor.org/rfc/rfc9111.html). The project’s actual dependency versions remain authoritative for adapting snippets.

[Learning evidence](learner-progress.md) · [System design interview home](../README.md)
