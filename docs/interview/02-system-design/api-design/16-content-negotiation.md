# 16. Content negotiation and representation design

## Question

How do Content-Type, Accept, and representation selection affect an API contract?

[Module sequence](README.md) · Artifact: READY · Learning: TODO · Suggested study: 20–30 minutes plus design exercise

## What you will learn

Distinguish the format being sent from the format requested back. Use the same create request to explain why 415, 400, and 406 call for different corrections.

Builds on [response contracts](03-status-and-response-contracts.md) and the [method examples](02-http-methods.md). The explicit media-type route below is proposed; imports and wiring are omitted.

## What are you sending, and what can you read?

A client submits a new post as JSON. It also asks for the response as XML, a different structured text format. Those are two independent statements: “these are the bytes I am sending” and “these are the formats I can read back.” Confusing them makes failures difficult to diagnose.

A **media type** names a representation format. In a request, `Content-Type: application/json` describes the submitted body. `Accept: application/json` says the caller is willing to receive JSON in the response. **Content negotiation** is the server choosing an available representation that the caller accepts.

Use one fixed create request with caption `hi`, a valid image reference, and location `Home`. If the client labels the body `text/plain`, our proposed JSON-only route rejects the input format with 415. If it labels malformed bytes as JSON, the server selects the JSON parser but cannot read them, so the result is 400. If the body is valid but the caller accepts only XML, the server cannot offer that response and can return 406. The correction is different in each case.

Submitting a form in one language and asking for a receipt in another captures the two directions. The comparison stops at media-type meaning: the format can define operations, not just spelling. For example, the two [PATCH formats](02-http-methods.md) both use JSON syntax but interpret their documents differently.

The table below is a reference for that one JSON-only contract. It is not evidence that new routes or converters have been installed.

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

## Correct the same request one decision at a time

Keep the proposed create body fixed in meaning: caption `hi`, an authorized image reference, and location `Home`. First send its JSON bytes with `Content-Type: text/plain`. The route admits JSON, so the result is 415; rewriting the JSON values cannot fix the declared format.

Next use `Content-Type: application/json` but break a brace in that body. The input type is now admitted, but parsing fails, so the result is 400. Fixing the brace solves a parsing problem, not a permission or business-rule problem.

Now send the valid body with `Accept: application/xml`. The route offers JSON only, so response selection can fail with 406. If the write contract promises no creation when negotiation fails, assert that there is no persisted post in this case in the configured Spring stack. A status check alone cannot prove it.

Finally accept JSON, keep the valid body, and pass authentication and domain checks. The create can complete with 201 and the authoritative JSON post. There is no reason to infer a format by sniffing text when the agreed metadata already states it.

## Representation patterns and trade-offs

Use separate create/update/read DTOs because they have different trust and required-field boundaries. A database entity exposes persistence relationships and server-managed fields; it can also trigger lazy queries during serialization. A projection is a selected view, such as PostSummary versus PostDetail. It reduces overfetching but adds DTO and query maintenance.

Sparse field selection, such as fields=id,caption, can reduce bytes but must allowlist fields, enforce field-level authorization, and define how fields interact with required response properties. It may complicate validators, generated clients, and cache keys. Prefer a few stable named views until arbitrary projection is justified.

If the same URL can return JSON or another representation selected by Accept, set appropriate Vary: Accept and validate CDN behavior. For query-based selection, preserve relevant query parameters in cache keys. Neither Vary nor a new media type protects private data without authorization and cache policy.

## Make the cache ignore the requested representation

For a future cache test, let the same post URL offer a v1 and v2 representation selected by Accept. Store the v2 response using only the URL as key, then request v1. Predict the symptom: the old client receives the wrong contract despite a successful status.

Detect it by sending both Accept values through the intermediary, not just directly to Spring. Contain reuse and purge incorrect variants where supported. Recover correct selection with the required Vary/key behavior and verify both responses again. Prevent recurrence by including representation headers in cache-contract tests. This protects format selection; personal data still needs its own authorization and storage policy.

## Project mapping and practice

[PostController](../../../../src/main/java/com/instagram/backend/controller/PostController.java) uses request/response DTOs; [MediaController](../../../../src/main/java/com/instagram/backend/controller/MediaController.java) handles multipart upload and binary resource responses. Compare JSON creation versus multipart media upload: request body formats and limits differ. For F-030, specify accepted upload formats, success/error media types, size policy, and two negative requests. See [Spring mapping and media types](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-requestmapping.html).

## Interview-ready answer

I separate the format the client sends from the formats it accepts back. I use consumes and produces to state the supported contract, DTOs to isolate writable and readable state, and tests for unsupported media, malformed content, and unacceptable output. I prefer a small set of explicit views over arbitrary field selection until overfetching warrants that complexity. When selection varies by headers, I make cache variation explicit and test old/new clients through intermediaries. Negotiation does not replace domain validation or access control.

## Before moving on

Correct the fixed request through 415, 400, and 406, explaining the decision at each stage. Continue when you can state what consumes, produces, and converter support actually provide.

Attempt the questions before opening their answers and record the explanation in [learner progress](learner-progress.md). This revision leaves learning status unchanged; a finished document is not evidence that you have demonstrated the idea.

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
