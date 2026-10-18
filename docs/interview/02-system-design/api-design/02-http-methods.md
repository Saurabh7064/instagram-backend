# 02. HTTP methods: GET, HEAD, POST, PUT, PATCH, DELETE, and OPTIONS

## Question

How do you choose an HTTP method, especially POST versus PUT versus PATCH, and what should retries do?

[Module sequence](README.md) · Artifact: READY · Learning: TODO

## What you will learn

Choose a method from the requested effect, explain every common method, distinguish replacement from partial modification, derive missing/null behavior for two patch formats, and identify which retries need protection.

Prerequisite: [resource identity and URLs](01-resources-and-urls.md). Study this chapter in three passes: method semantics; POST/PUT/PATCH examples; patch formats and implementation. Each pass is about 15–20 minutes. The protocol examples are proposed contracts unless explicitly marked current behavior.

## Must understand: why methods exist

A link preview, browser prefetch, proxy retry, and human submit button can all send requests. If reading a URL could like a post, merely previewing it could change data. If retrying an uncertain write always created a new item, a network failure could produce duplicates. Methods describe what the caller asks the server to do, so callers and intermediaries can reason about requests.

An HTTP method is like an instruction on an addressed envelope: read, replace, or remove the thing at that address. The analogy stops because network delivery and responses can fail independently, and intermediaries may retry under specific rules.

**Safe** means the caller does not request a state change. Logging or accounting can still occur. **Idempotent** means several identical requests have the same intended server effect as one request. It does not mean identical status codes, zero side effects, or automatic protection against competing different requests. **Cacheable** concerns whether a response can be retained and reused under cache rules; it is a separate property.

The method definitions follow [HTTP Semantics, section 9](https://www.rfc-editor.org/rfc/rfc9110.html#section-9); the worked requests below apply those semantics to this backend.

## Method comparison

| Method | Requested meaning | Safe? | Idempotent by semantics? | Typical success | Example |
|---|---|---|---|---|---|
| GET | Retrieve a selected representation | Yes | Yes | 200, or 304 for a conditional read | GET /posts/42 |
| HEAD | GET metadata without response content | Yes | Yes | 200, or conditional outcome | HEAD /uploads/photo.png |
| POST | Ask target to process submitted content | No | No guarantee | 200, 201, 202, or 204 by contract | POST /posts |
| PUT | Create/replace state at the target URI | No | Yes | 201 if created; 200/204 if replaced | PUT /posts/42/editable-content |
| PATCH | Apply a patch document to target | No | No guarantee; particular operations can be | Usually 200/204 | PATCH /posts/42 |
| DELETE | Remove the target resource association | No | Yes | 200/204, or 202 if pending | DELETE /posts/42 |
| OPTIONS | Describe communication options | Yes | Yes | Often 200/204 | OPTIONS /posts/42 |

These statuses are possibilities, not mandatory universal responses. Resource state, preconditions, permissions, and the documented contract still decide outcomes. TRACE is a diagnostic loopback method and is usually disabled on application APIs; CONNECT establishes a tunnel and is normally a proxy concern. Neither is a normal choice for post CRUD.

## GET and HEAD: retrieving without requested mutation

Use GET for a post, feed page, or search result with selection parameters. Do not increment a business counter, publish content, or create a like merely because GET was called. Operational access logs do not turn GET into an unsafe application command.

A GET request body has no generally defined semantics and is poorly interoperable; use query parameters for bounded ordinary search. If a complex search document is necessary, POST a clearly documented search operation or search resource. That is a design decision with different caching/retry assumptions.

HEAD requests the corresponding GET metadata without a response body. If Content-Length is present, it describes the GET representation, not a zero-length representation. HEAD is useful for metadata/existence checks, but must enforce the same access policy and can still incur substantial work. It is not a permission bypass or a guaranteed cheap database query.

Spring MVC supports HEAD for GET mappings. That framework behavior does not prove your service avoids loading the full representation; measure it if using HEAD for a high-volume probe. See [Spring request mapping](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-requestmapping.html).

## POST: processing, often server-assigned creation

When the client knows the collection but not the new item identity, POST /posts lets the server allocate it. POST is broader than create: POST /exports might start a job; POST /sessions might authenticate; POST /posts/42/publications might create a publishing attempt. Its meaning comes from the target contract.

```http
POST /api/posts HTTP/1.1
Content-Type: application/json
Authorization: Bearer <access-token>

{"caption":"Sunset","imageUrl":"/uploads/sunset.png","locationLabel":"Beach"}
```

Current create returns 201 and a FeedPostResponse. If its response is lost, repeating this request can create a second post. A proposed Idempotency-Key protocol can make one logical attempt replayable; POST itself does not promise that. Do not select POST because the body is “large” or “contains JSON”: PUT and PATCH also carry bodies.

## POST versus PUT versus PATCH: choose by intent

| Decision | POST | PUT | PATCH |
|---|---|---|---|
| What does the body mean? | Input to the target’s documented processing | Complete declared replacement state | Changes under a declared patch format |
| Who chooses new item identity? | Commonly server; contract decides | Client already addresses target URI | Usually an existing target; creation is format/policy dependent |
| What if a writable field is omitted? | Create/default/operation schema decides | Reject or reset/default under replacement schema | Patch format decides; object merge omission preserves it |
| What does an identical repeat promise? | No method-level deduplication guarantee | Same intended target state | Depends on patch operations and guards |
| Typical use here | Create comment or processing attempt | Replace editable content or ensure like exists | Change selected caption/location fields |

Choose the intention first: new item in a server-identified collection -> POST; known target with complete desired writable state -> PUT; selected modifications with explicit missing/null rules -> PATCH. A workflow can still be POST even when it modifies an existing post. Body size, number of fields changed, and whether a controller calls save() do not determine the method.

## PUT: set the complete defined writable state at a known URI

For a **proposed** editable-content resource, define its complete writable representation as caption and locationLabel. Its server-managed ID, author, and counters are not fields the client replaces.

```http
PUT /api/posts/42/editable-content HTTP/1.1
Content-Type: application/json
Authorization: Bearer <access-token>

{"caption":"Dusk","locationLabel":"Pier"}
```

Starting state caption=Sunset, locationLabel=Beach -> after first PUT: Dusk/Pier -> after identical PUT: Dusk/Pier. The caller chooses the target URI; the server does not allocate a different item for every repeat. PUT may create an absent target if the contract permits it, or the API may require the target already exist and return 404 otherwise. “POST creates, PUT updates” is therefore incomplete.

Omitted fields must follow replacement semantics for the declared representation: reject an incomplete representation, or reset/default omitted optional fields according to a documented schema. Silently preserving every omitted writable field makes the operation behave like a partial update. Define the writable view explicitly rather than interpreting “full” as every database column.

### Current backend nuance

[PostController](../../../../src/main/java/com/instagram/backend/controller/PostController.java) exposes PUT /api/posts/{postId}. [UpdatePostRequest](../../../../src/main/java/com/instagram/backend/dto/UpdatePostRequest.java) requires caption and locationLabel; [PostService](../../../../src/main/java/com/instagram/backend/service/PostService.java) changes those two and leaves imageUrl unchanged. Omitting locationLabel is invalid; it does not preserve its old value. That is the actual behavior. To clarify the public model, document the two-field editable view, introduce an explicit subresource, or evolve to a partial-edit contract deliberately. Do not silently rename the route and break clients.

When PUT transforms the submitted representation before storage, do not assume a success-response ETag is allowed: HTTP constrains validators on PUT responses. A subsequent GET can supply the validator for the actual selected representation. In this backend, trimming makes that distinction relevant.

Idempotency does not prevent lost updates: two different complete replacements can still overwrite each other. A proposed If-Match requirement handles stale writes; see [conditional updates](11-conditional-updates.md).

## PATCH: apply specified modifications, with a defined format

PATCH answers “change these parts according to this patch document.” The content type defines what the document means; `@PatchMapping` alone does not implement merge, operations, missing/null semantics, field authorization, or atomicity.

Start with the same existing state in each following example:

```json
{"caption":"Sunset","locationLabel":"Beach","tags":["sea","sky"]}
```

### JSON Merge Patch: object-shaped desired changes

```http
PATCH /api/posts/42 HTTP/1.1
Content-Type: application/merge-patch+json
If-Match: "post42-v7"

{"caption":"Dusk","locationLabel":null}
```

With this format, omitted properties remain unchanged; null removes a member. Derivation: caption is supplied -> set Dusk; locationLabel is null -> remove it; tags is omitted -> retain [sea,sky]. If the resource requires a nonblank location, the resulting state is invalid and the entire operation must fail rather than storing a half-update. A merge patch cannot naturally distinguish “store literal JSON null” from “remove this property.” Arrays are replaced as a whole: `{"tags":["night"]}` produces [night], not [sea,sky,night]. See [JSON Merge Patch](https://www.rfc-editor.org/rfc/rfc7396.html).

### JSON Patch: explicit operations and paths

```http
PATCH /api/posts/42 HTTP/1.1
Content-Type: application/json-patch+json
If-Match: "post42-v7"

[
  {"op":"test","path":"/caption","value":"Sunset"},
  {"op":"replace","path":"/caption","value":"Dusk"},
  {"op":"add","path":"/tags/-","value":"night"}
]
```

Operations execute in order: test checks Sunset -> passes; replace changes caption to Dusk; add at `-` appends night -> [sea,sky,night]. A failing test prevents the whole patch. Other operations include remove, move, and copy. The paths use JSON Pointer syntax; nested and escaped paths require proper parsing, not String splitting. See [JSON Patch](https://www.rfc-editor.org/rfc/rfc6902.html).

Repeating a field replacement to the same value can have an idempotent intended effect. Repeating an array append adds night twice; PATCH therefore has no blanket idempotency guarantee. If-Match can reject replay against a changed revision, but a lost response still leaves the client needing to determine whether its first attempt succeeded.

| Decision | PUT | Merge Patch | JSON Patch |
|---|---|---|---|
| Client sends | Complete writable representation | Changed object members | Ordered operations |
| Omitted writable member | Invalid/reset/default per replacement schema | Unchanged | Unchanged unless targeted |
| Null | Literal value if allowed by schema | Remove member | Literal null can be an operation value |
| Array edit | Replace array | Replace array | Target individual indices or append |
| Complexity | Simple full-state validation | Presence and null handling | Path/op validation and ordering |

## Spring Boot PATCH boundary and atomic application

Proposed outline, not a complete implementation:

```java
@PatchMapping(path = "/posts/{id}",
        consumes = "application/merge-patch+json")
FeedPostResponse patch(
        @PathVariable Long id,
        @RequestHeader("Authorization") String authorization,
        @RequestHeader("If-Match") String expected,
        @RequestBody Map<String, Object> document) {
    return patchPosts.mergeOwned(authorization, id, expected, document);
}
```

`consumes` admits this patch format, not every JSON operation list. A Map preserves the difference between absent `caption` and present `caption:null` through containsKey, but does not validate types, nesting, duplicate JSON keys, or the schema automatically. A presence-aware DTO or JSON tree is another option. The proposed collaborator must:

1. Authenticate and authorize the target, and reject fields outside the editable allowlist, including authorId and likeCount.
2. Validate patch syntax/types; apply it to a candidate writable representation, not directly onto the managed entity.
3. Validate the complete resulting state. Missing required location must fail even if the patch’s other member is valid.
4. Evaluate the supplied revision and persist all changes atomically with concurrency protection.
5. Return the documented representation/validator or no-body success. No partial update may remain after failure.

For example, caption=Dusk passes but removing required location fails: rollback must preserve both Sunset and Beach. A transaction annotation helps with database writes but does not make external notifications rollback; defer those through an outgoing event strategy. PATCH atomicity and support advertisement are specified in [HTTP PATCH](https://www.rfc-editor.org/rfc/rfc5789.html).

## DELETE and OPTIONS

DELETE requests removal of the resource association. It does not promise physical byte erasure from backups, synchronous cascading deletion, or immediate deletion from every cache. Define soft deletion, retention, and related-object effects. Current post delete removes likes and the post in a transaction and returns 204. A repeat finds no post and returns 404: still idempotent because the post remains absent.

OPTIONS can advertise supported methods in Allow. Accept-Patch can advertise admitted patch media types. A CORS preflight also uses OPTIONS with Origin and Access-Control-Request-Method; its browser permission response is not the same thing as domain authorization. A caller can still be forbidden from PUT even when PUT is listed as supported. Spring has automatic OPTIONS handling for mappings; explicit CORS configuration and advertised patch formats need deliberate review.

## Controlled failure drill

Prediction: a patch first changes caption then removes a required location; the second step fails. Symptom of a broken implementation: response is an error but a follow-up GET shows the changed caption. Containment: disable the new patch route or keep the old full-edit contract while fixing it. Recovery: restore the original state in disposable lab data, then re-run the failing request. Prevention: apply to a candidate, validate final state, persist atomically, and assert unchanged stored values after failure. Separately test unknown protected fields, unsupported media types, and concurrent stale requests.

## Interview-ready answer

I choose the method from the requested effect on the addressed resource. GET and HEAD retrieve without requesting mutation; OPTIONS describes supported interaction. POST submits content for processing, often creating a server-identified item or starting an operation. PUT sets the complete declared writable state at a known address and can create there if allowed. PATCH applies a document describing partial changes; its media type defines missing, null, arrays, and operations. DELETE removes the target association.

I distinguish safety, idempotency, caching, and concurrency. Identical PUTs or DELETEs have the same intended effect, though statuses can differ. POST needs an attempt-key contract for safe create replay. PATCH may be idempotent for setting a value but not appending an array item. None of those choices prevents two different edits from clobbering each other, so I add atomic revision preconditions where necessary. In Spring I keep DTOs, field permissions, final-state validation, and transaction/concurrency rules in the design rather than assuming an annotation supplies them. I test repeats, stale writes, and partial-failure rollback.

## Stop/go and learner evidence

Explain all seven common methods, derive each worked PUT and patch result, predict a failed multi-change patch, and justify a method for creating a comment, liking a post, and starting an export. Record an attempt in [learner progress](learner-progress.md); do not mark comprehension from reading alone.

## Questions and explained answers

<details>
<summary>1. Why is “POST creates, PUT updates, PATCH updates one field” incomplete?</summary>

POST requests target-specific processing and can do more than creation. PUT can create at a known URI if allowed and otherwise replaces a complete defined writable representation. PATCH can change many fields or perform several operations; it needs a format and all-or-nothing application, not a one-field restriction.

</details>

<details>
<summary>2. What do omitted locationLabel and locationLabel:null mean for PUT and Merge Patch?</summary>

For a complete PUT schema requiring locationLabel, omission or null is invalid. If a different replacement schema makes it optional, omitted state needs documented reset/default semantics. In Merge Patch, omission preserves its current member; null removes it. If removal violates a required-field invariant, reject the whole patch and preserve original state.

</details>

<details>
<summary>3. Can PUT lose a concurrent edit, and can PATCH be idempotent?</summary>

Yes to both. Two different full replacements are not identical repeated requests and can overwrite one another without a version precondition. A patch setting caption to Dusk can be idempotent; one appending night to an array repeats its effect. Method semantics and concurrency control are distinct.

</details>

<details>
<summary>4. Should HEAD return a JSON body, and does OPTIONS authorize a write?</summary>

HEAD has no response content, though its metadata describes the corresponding GET. OPTIONS can describe supported methods and CORS-related browser permissions; it does not establish that this caller may perform a domain mutation. Authentication and object permissions still apply.

</details>

<details>
<summary>5. What exactly happens today if PUT /api/posts/42 omits locationLabel?</summary>

The current UpdatePostRequest requires a nonblank locationLabel. Missing binds to null and fails validation, normally 400 before the service edit. The current route does not implement PATCH, Merge Patch, JSON Patch, or If-Match. Those examples are proposals, not claims about deployed behavior.

</details>
