# 16. Content negotiation and representation design

## Question

How do Content-Type, Accept, and representation selection affect an API contract?

[Module sequence](README.md) · Artifact: READY · Learning: TODO · Suggested study: 20–30 minutes plus design exercise

## What you will learn

Distinguish request parsing from response selection; derive 400, 406, and 415 outcomes; design media types and cache variation.

Prerequisites: the foundational contract, method, authorization, and compatibility lessons. These are proposed teaching examples; routes, collaborators, DTOs, and policies shown here are not installed by this documentation change. Imports and application wiring are omitted. Use the existing authentication rules when implementing any extension.

## Must understand: two directions, two decisions

A server needs to know how to parse submitted bytes and which response format the caller can consume. A media type names the representation format. Content-Type describes the body actually being sent; Accept lists response formats the caller is willing to receive. Negotiation is the process of selecting an available representation that fits the request.

Think of submitting a form in one language while requesting a receipt in another. The analogy stops because media types specify machine syntax and semantics, not only human language, and selection also affects caches and errors.

| Request situation | Server decision | Expected result under this proposed JSON-only contract |
|---|---|---|
| Content-Type application/json, valid JSON, Accept application/json | Parse and produce JSON | Normal domain outcome |
| Content-Type text/plain with JSON-looking text | Unsupported input format | 415 |
| Content-Type application/json but malformed JSON | Parser cannot bind | 400 |
| Valid JSON, Accept application/xml, no XML representation | Cannot produce acceptable representation | 406 |
| Accept */* | Any offered representation acceptable | JSON can be selected |

A vendor media type can distinguish a documented representation version, but naming one does not configure a converter automatically. A `+json` suffix signals JSON syntax conventions; the application still needs a supported serializer/converter path and explicit contract.

## Spring Boot example and walkthrough

```java
@PostMapping(path = "/api/posts",
        consumes = "application/json",
        produces = "application/json")
ResponseEntity<FeedPostResponse> create(
        @RequestHeader("Authorization") String authorization,
        @Valid @RequestBody CreatePostRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(postService.create(authorization, request));
}
```

Proposed explicit-media variant of current create: consumes narrows input selection; produces narrows output selection; @RequestBody invokes conversion; @Valid checks the bound DTO; the service authenticates and performs creation. Changing produces from JSON to a custom media type requires integration tests with the actual converters. An error body can have application/problem+json rather than the success body’s type; document both.

## Request trace: metadata and bytes must agree

1. Caller sends Content-Type=text/plain with `{"caption":"hi"}`. Under this explicit route it is not an admitted JSON request; do not infer format by sniffing text.
2. Caller corrects Content-Type to application/json but sends a trailing broken brace. Now the converter is selected, parsing fails, and the request is 400.
3. Caller fixes JSON and sends Accept=application/xml. The route offers only JSON, so negotiation can fail with 406 rather than creating an XML contract by accident. If a write must never execute when response negotiation fails, verify that guarantee with a no-persisted-write assertion in the configured stack.
4. Caller accepts JSON and sends all required valid fields; ownership/media policy passes -> 201 with authoritative representation.

## Representation patterns and trade-offs

Use separate create/update/read DTOs because they have different trust and required-field boundaries. A database entity exposes persistence relationships and server-managed fields; it can also trigger lazy queries during serialization. A projection is a selected view, such as PostSummary versus PostDetail. It reduces overfetching but adds DTO and query maintenance.

Sparse field selection, such as fields=id,caption, can reduce bytes but must allowlist fields, enforce field-level authorization, and define how fields interact with required response properties. It may complicate validators, generated clients, and cache keys. Prefer a few stable named views until arbitrary projection is justified.

If the same URL can return JSON or another representation selected by Accept, set appropriate Vary: Accept and validate CDN behavior. For query-based selection, preserve relevant query parameters in cache keys. Neither Vary nor a new media type protects private data without authorization and cache policy.

## Controlled failure drill

Prediction: a cache stores a v2 media-type response by URL alone. An old client requesting v1 receives v2 and fails despite a successful status. Detect with two Accept values through the cache, contain by bypassing/purging affected entries, recover correct variants, prevent with Vary/key tests and explicit representation contracts.

## Project mapping and practice

[PostController](../../../../src/main/java/com/instagram/backend/controller/PostController.java) uses request/response DTOs; [MediaController](../../../../src/main/java/com/instagram/backend/controller/MediaController.java) handles multipart upload and binary resource responses. Compare JSON creation versus multipart media upload: request body formats and limits differ. For F-030, specify accepted upload formats, success/error media types, size policy, and two negative requests. See [Spring mapping and media types](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-requestmapping.html).

## Interview-ready answer

I separate the format the client sends from the formats it accepts back. I use consumes and produces to state the supported contract, DTOs to isolate writable and readable state, and tests for unsupported media, malformed content, and unacceptable output. I prefer a small set of explicit views over arbitrary field selection until overfetching warrants that complexity. When selection varies by headers, I make cache variation explicit and test old/new clients through intermediaries. Negotiation does not replace domain validation or access control.

## Stop/go and learner evidence

Explain the worked request without notes, justify a competing option, predict the failure, and describe a test that checks the final state or response. Record an attempt in [learner progress](learner-progress.md). A completed artifact does not establish understanding or an implemented feature.

## Questions and explained answers

<details>
<summary>1. Is Content-Type the same as Accept?</summary>

No. Request Content-Type describes submitted bytes; Accept expresses acceptable response representations. A request can send JSON while accepting a different output type if the server offers it.

</details>

<details>
<summary>2. Why distinguish 415 from 400?</summary>

415 means the submitted representation type is unsupported. 400 can mean the admitted format could not be parsed or validated. Correcting syntax does not fix a wrong declared media type.

</details>

<details>
<summary>3. Does a +json suffix install a serializer?</summary>

No. It identifies JSON-related syntax conventions, but actual converter support and domain schema still need implementation and tests.

</details>

<details>
<summary>4. Why can sparse fields create a security issue?</summary>

A caller might request an internal or privileged field. Field selection must be constrained by both an allowlist and caller permissions; it cannot expose arbitrary entity properties.

</details>

<details>
<summary>5. Why test cache behavior for Accept?</summary>

Two representations can share a URL while differing by Accept. Without correct cache selection, a client can receive a different contract or format from the one requested.

</details>
