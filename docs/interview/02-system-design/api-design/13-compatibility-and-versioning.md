# 13. Compatibility, API versioning, and client migration

## Question

How do you evolve an API, select a versioning strategy, and retire an old contract without breaking supported clients?

[Module sequence](README.md) · Artifact: READY · Learning: TODO

## What you will learn

Classify structural and semantic breaking changes, compare four version-selection mechanisms, implement proposed Spring MVC version boundaries, and plan expansion, migration, retirement, and rollback.

Prerequisites: [response contracts](03-status-and-response-contracts.md), [pagination](07-offset-pagination.md), and [caching](12-caching-and-privacy.md). Study in two passes: compatibility and version selection; implementation and rollout. Allow 15–20 minutes per pass plus the migration exercise.

## Must understand: why a new deployment is not a new client

An old mobile build may stay installed for months; two server releases can run during a rollout; a generated client may reject a field an ordinary JavaScript client ignores. Backward compatibility means the supported existing caller’s promised behavior still works. A breaking change invalidates one of those promises. Version selection identifies which contract the caller requests, not which Git commit or database schema is running.

The analogy is replacing a power socket while old plugs remain in homes. The analogy stops because API compatibility also includes business meanings, error behavior, authorization, ordering, and performance budgets rather than only shape.

## Classify the change before inventing v2

| Change | Compatibility risk | Why |
|---|---|---|
| Add optional response field | Often compatible, not guaranteed | Strict readers/signatures can reject extras |
| Add optional request field with old default | Often compatible | Omitted request must keep old meaning |
| Require a formerly optional request field | Breaking | Old callers do not send it |
| Rename/remove imageUrl | Breaking | Old callers lose their lookup key |
| Add an enum value | Potentially breaking | Exhaustive switches can fail |
| Change number to string | Breaking for many consumers | Type assumptions and generated clients differ |
| Change milliseconds to seconds | Semantically breaking | Shape stays numeric but meaning changes |
| Change feed array to page envelope | Breaking | array.map no longer works |
| Change default sorting or visibility | Behavioral risk | Cursor/UX and permissions change |
| Make validation stricter | Potentially breaking | Previously accepted requests fail |
| Change error code/status | Potentially breaking | Client retry and UI branches change |

“Additive” is a hypothesis to test against supported consumers, not a universal safety guarantee. Response tolerance is useful; accepting undocumented malformed request data forever creates a security and maintenance burden. Be deliberate at each boundary.

## Compare version-selection patterns

| Mechanism | Request example | Choose when | Cost/new failure mode |
|---|---|---|---|
| URI path | /api/v2/feed | Public contracts; easy routing/debugging | Multiple addresses for related views; route duplication |
| Query | /api/feed?api-version=2 | Simple explicit selection on one path | Version parameter must be retained by links and caches |
| Custom header | API-Version: 2 | Controlled callers/gateways | Less visible in links; forward header and vary caches |
| Media type | Accept: application/vnd.instagram.feed.v2+json | Version belongs to selected representation | Negotiation, 406 handling, tooling and Vary complexity |

Pick one primary mechanism rather than letting conflicting path, header, and query versions compete. If a caller supplies conflicting selectors, reject or follow a documented precedence; silently choosing one makes incidents difficult to diagnose. An unversioned endpoint must have an explicit stable default policy. Defaulting to “latest” can break clients on a deployment without their request changing.

A version usually represents a supported public contract, not every internal field addition. Too many versions multiply test matrices, security fixes, documentation, and operations. Sometimes a new resource or optional field is clearer than a whole API version; sometimes a genuinely different response needs a separate boundary.

## Spring Boot example: explicit path versions and adapters

Proposed route excerpts; `feedViews`, `V2FeedPage`, and these routes do not exist today. Reuse the current authorization/service rules rather than duplicating persistence logic in each controller.

```java
@GetMapping("/api/v1/feed")
List<FeedPostResponse> v1(
        @RequestHeader("Authorization") String authorization) {
    return feedViews.legacyArray(authorization);
}

@GetMapping("/api/v2/feed")
V2FeedPage v2(
        @RequestHeader("Authorization") String authorization,
        @RequestParam(defaultValue = "20") int limit,
        @RequestParam(required = false) String cursor) {
    return feedViews.cursorPage(authorization, limit, cursor);
}
```

Each adapter implements its own public request/response shape and delegates common business rules. V2 validates limit/cursor. V1 cannot silently change from array to envelope. Keeping its old unbounded behavior forever has cost; measure remaining traffic, define support limits, and plan retirement rather than pretending it is free. The current unversioned `/api/feed` also needs a migration policy: adding `/v1` does not automatically move existing callers there.

### Spring MVC 7 version resolution

This project uses Boot 4.0.2. The API names below were checked against locally cached Spring 7.0.3 classes; framework APIs should still be checked against the resolved build when implementing. Spring MVC 7 supports a configured API version resolver and version conditions. One proposed header strategy is:

```java
@Configuration
class ApiVersions implements WebMvcConfigurer {
    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer.useRequestHeader("API-Version")
                .setVersionRequired(true)
                .addSupportedVersions("2");
    }
}

// Alternative to path versioning, not combined with it here:
@GetMapping(path = "/api/feed", version = "2")
V2FeedPage selectedV2(
        @RequestHeader("Authorization") String authorization,
        @RequestParam(defaultValue = "20") int limit,
        @RequestParam(required = false) String cursor) {
    return feedViews.cursorPage(authorization, limit, cursor);
}
```

Check the configured resolver’s effect on existing unversioned auth/profile routes before enabling it application-wide; include those requests in regression tests. This excerpt is an isolated alternative, not a drop-in global migration.

The final method is an alternative handler excerpt with proposed types; the delegated service must validate limit and cursor and enforce authorization. `ApiVersionConfigurer` is in `org.springframework.web.servlet.config.annotation`. This alternative enables only version 2 and requires a selector; missing/unsupported selectors need tested 400 handling in this configuration. Test behavior behind the gateway as well. Exact `version="2"` and baseline forms such as `"2+"` differ: a baseline can cover later compatible versions, so careless use can route a future incompatible request to an old handler. Prefer explicit supported contracts until that resolution rule is understood. See the official [Spring MVC versioning reference](https://docs.spring.io/spring-framework/reference/web/webmvc-versioning.html).

## Worked migration: current array to bounded feed envelope

Current [PostController](../../../../src/main/java/com/instagram/backend/controller/PostController.java) returns `List<FeedPostResponse>`. A proposed next contract returns:

```json
{"items":[{"id":42}],"nextCursor":"opaque-token","hasNext":true}
```

1. **Expand:** add the new route/adapter and bounded query while retaining the old route and array. Test both contracts and authorization rules.
2. **Deploy providers first:** make the new API available before shipping a client that requires it. During mixed server rollout, route appropriately or gate client use until all serving instances support it.
3. **Migrate consumers:** release a client reading items and nextCursor; record contract-version usage without logging tokens. A fallback is only safe if deliberately designed and tested, not an automatic retry of every error against another endpoint.
4. **Verify:** compare errors, latency, item visibility, and pagination boundary behavior; keep old-client fixtures. A healthy v2 does not prove v1 still works.
5. **Deprecate:** announce dates, documentation, and a support policy; expose useful migration metadata if clients/tooling use it. Deprecation means discouraged, not already unavailable.
6. **Retire:** remove support only under the stated client-support policy with observed usage and a reviewed removal plan. “No traffic yesterday” is not sufficient for infrequent users.
7. **Rollback:** retain old adapters and compatible storage long enough to restore service. Dropping fields or data required by v1 can make a controller rollback ineffective.

The standardized Deprecation header reports deprecation timing; Sunset can communicate anticipated unavailability timing. They are different signals and do not migrate a caller by themselves. Avoid copying an obsolete `Deprecation: true` example as current syntax; use [RFC 9745](https://www.rfc-editor.org/rfc/rfc9745.html) when implementing it.

Database schema expansion/contraction is a related but separate migration. New application contracts may share one model through adapters. Keeping two APIs does not require two databases; keeping two adapters does require tests so their meanings do not drift.

## Controlled failure drill

Prediction: deploy an envelope at the existing array route. Symptom: an old consumer throws when it calls `.map` on an object, despite a 200 response. Detect with a saved old-client fixture before deployment and consumer error telemetry after. Containment: stop rollout and route users to the old contract. Recovery: restore the old response shape and confirm rendering with the old client. Prevention: parallel contract migration, consumer verification, stable default version, and a removal gate. Separately test lost version headers at a gateway and cache mixing between versions selected by headers.

## Interview-ready answer

I first identify the supported consumers and their promises: request shape, response shape, meaning, errors, ordering, and permissions. I prefer compatible additions where actual consumers tolerate them, but a feed array becoming an envelope or milliseconds becoming seconds is breaking. I select one version mechanism based on operational needs: path versions are visible and easy to route; headers and media types need explicit forwarding, negotiation, and cache variation.

In this backend I would keep the unversioned feed’s contract while adding a bounded new contract through a shared service and separate DTO adapters. I would deploy provider support first, migrate clients, measure usage, and retain old-consumer tests. Deprecation and retirement have separate dates and gates. Rollback must preserve compatible database state as well as routes. A version number is a support commitment, not a substitute for compatibility tests or a reason to create a version for every internal release.

## Stop/go and learner evidence

Classify five changes, justify a version mechanism, trace an old and new client through mixed rollout, and diagnose the envelope failure without notes. Draft the F-016 migration and record evidence in [learner progress](learner-progress.md). SD-004 remains unmastered until demonstrated.

## Questions and explained answers

<details>
<summary>1. Why can adding an enum value break an API that kept every existing field?</summary>

A generated client or exhaustive switch may only accept old enum values. The new value violates a value-domain assumption even though the structure is unchanged. Test actual consumers and define unknown-value behavior where appropriate.

</details>

<details>
<summary>2. When would you choose path versioning over media-type versioning?</summary>

Choose paths when visible links, gateway routing, simple diagnostics, and documentation matter more than keeping one resource address. Media types suit representation negotiation but require clients, caches, and tools to preserve the selection and handle 406. Neither approach automatically ensures semantic compatibility.

</details>

<details>
<summary>3. What can go wrong if the gateway drops API-Version or the cache ignores it?</summary>

The server may reject the request or choose an unintended default; a shared cache can return another version’s representation. Test forwarding, missing/unsupported behavior, and appropriate Vary/key configuration. Do not silently default to latest.

</details>

<details>
<summary>4. How do you migrate /api/feed from array to envelope safely?</summary>

Keep the existing contract while deploying a new version/route with a bounded query and explicit DTO. Migrate clients, verify both contracts and visibility, measure old usage, announce deprecation, then retire under a support policy. Keep rollback-compatible adapters and data through the migration.

</details>

<details>
<summary>5. Is a v2 route enough to make a breaking database migration safe?</summary>

No. V1 and old application instances may still require columns or meanings removed by the migration. Expand storage compatibly, move readers/writers in stages, verify old versions are retired, and only then contract storage under a reviewed rollback policy.

</details>
