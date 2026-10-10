# 09 — Evolving API and Event Contracts Safely

> **Interview question:** How would you evolve APIs and event schemas without breaking older consumers while microservices deploy independently?

> [One-page revision card](../../revision-cards/microservices/09-api-event-contract-evolution.md)

- Status: `READY`; comprehension not yet demonstrated
- Time: 20–25 minutes
- Primary idea: expand a contract compatibly, migrate consumers, observe adoption, and only then remove the old shape

## Why does this matter?

Independent deployment works only if a producer and every consumer do not need to release at the same instant. In production, old mobile apps may remain for months, services roll out gradually, queued events may be replayed years later, and an emergency rollback may reintroduce an older binary.

A field rename that is trivial inside one process can therefore break clients, poison a message queue, or make old events unreadable.

## Analogy: changing an electrical outlet

If a building has old plugs, replacing every wall outlet overnight breaks existing devices. A safer transition supports the old plug while an adapter or new outlet is introduced, moves devices gradually, verifies usage, and removes the old form only after it is unused.

The analogy has limits. Software contracts include meaning, validation, ordering, security, and side effects—not only shape. Two schemas may look compatible while a changed meaning silently corrupts behavior.

## Plain-language model

### Contract

A **contract** is everything a consumer relies on:

- request and response fields;
- types, required/optional rules, and error codes;
- authentication and authorization behavior;
- event field meanings and delivery guarantees;
- ordering, idempotency, and latency expectations when promised.

OpenAPI or a schema file captures structure, but documentation and tests must also protect behavior.

### Additive and breaking changes

An **additive change** adds something old consumers can ignore, such as a new optional response field. A **breaking change** invalidates an existing expectation, such as removing a field, changing its type, making an optional request field required, or changing “amount in dollars” to “amount in cents” under the same name.

“Additive” is not automatically safe. Adding an enum value can break a consumer with an exhaustive switch. Making responses much larger can violate latency or payload assumptions.

### Consumer contract test

A **consumer contract test** records an important request/response or event expectation from a real consumer and verifies the provider still satisfies it. It complements provider tests; it does not prove every runtime behavior or replace integration testing.

## Predict before reading

The post API renames response field `caption` to `text` and deploys before the web client. The JSON is otherwise valid. What will the old client render?

<details>
<summary>Reveal the prediction</summary>

The old client still looks for `caption`, so it may render blank text or fail. JSON validity does not make the change backward-compatible. During migration the provider should usually return both fields with the same meaning, migrate consumers, observe old-field use, and remove it only under a declared version/deprecation policy.

</details>

## Safe API flow: expand, migrate, contract

Suppose `locationLabel` will eventually become an object containing a display name and place ID.

### 1. Expand

Keep the old field and add an optional new field:

```json
{
  "locationLabel": "Los Angeles",
  "location": {
    "placeId": "place-17",
    "displayName": "Los Angeles"
  }
}
```

The producer populates both. Old clients read `locationLabel`; new clients prefer `location` and fall back to the old field.

### 2. Migrate

- update consumer code and contract tests;
- deploy consumers independently;
- measure which client/service versions still rely on the old field;
- communicate an explicit deprecation date;
- retain compatibility long enough for rollback and long-lived clients.

### 3. Contract

Only after usage reaches the agreed threshold should the producer stop accepting or returning the old field. If old clients cannot be forced to upgrade, keep the compatibility layer or expose a separately versioned API.

For database changes behind the API, use the same shape: add nullable columns or new tables, make code handle both schemas, backfill, switch reads, verify, and remove the old schema in a later deployment. Never deploy code that requires a column before the database change exists everywhere.

## Safe event evolution

Events are historical facts and may be retained or replayed. Treat their published fields as long-lived contracts.

For a `PostPublished` event:

1. Give each event an event ID, event type, occurrence time, and schema version.
2. Prefer adding optional fields with clear defaults.
3. Make consumers ignore unknown fields and handle missing optional fields.
4. Never silently reuse a field with a new meaning.
5. If meaning changes fundamentally, publish a new event type or major schema version, such as `PostPublicationScheduled`, rather than pretending it is the old fact.
6. During transition, translate at a controlled adapter or publish both versions when the operational cost is justified.
7. Test replay using older stored events and test new events against supported consumer versions.

A schema registry can reject structurally incompatible changes, but it cannot determine whether `price` silently changed from dollars to cents. Semantic review is still required.

## Handling common changes

| Proposed change | Compatibility risk | Safer evolution |
|---|---|---|
| Add optional response field | Usually low | Add it; consumers ignore unknown fields |
| Add required request field | Old callers cannot send it | Add optional field with server default, migrate, then require only in a new version if needed |
| Rename field | Removal plus addition | Return/accept both during migration or create a new major version |
| Change field type | Old parser may fail | Add a new field or major version; do not mutate in place |
| Add enum value | Exhaustive consumers may fail | Require unknown-value handling and verify consumer contracts |
| Remove event field | Replays and lagging consumers fail | Keep it, version the event, or translate old/new schemas |
| Change field meaning | Silent data corruption | Introduce a clearly named new field/event and document units/semantics |

## Instagram-backend mapping

The current API contract is visible in [CreatePostRequest.java](../../../../../src/main/java/com/instagram/backend/dto/CreatePostRequest.java), [FeedPostResponse.java](../../../../../src/main/java/com/instagram/backend/dto/FeedPostResponse.java), and [PostController.java](../../../../../src/main/java/com/instagram/backend/controller/PostController.java).

For example, changing `CreatePostRequest.locationLabel` from required text to a required nested object in one release would break the existing browser. A safe path is to add an optional `location` representation while continuing to accept `locationLabel`, update clients, record old-field usage, and remove it only through a deliberate major-version policy.

[AuthIntegrationTests.java](../../../../../src/test/java/com/instagram/backend/AuthIntegrationTests.java) already asserts selected JSON fields. Those tests protect the provider's current response, but a future extracted service would also need consumer-focused contract tests and fixtures for old/new event versions.

## Decision and trade-off

| Choice | Prefer it when | Cost or limitation |
|---|---|---|
| Additive evolution in one version | Change can be optional and old semantics remain valid | Temporary duplicate fields and cleanup work |
| New endpoint/API major version | The required behavior or shape is fundamentally incompatible | Multiple versions to operate and deprecate |
| New event type/version | The business fact or meaning changes | Consumers migrate explicitly; possible dual publishing |
| Translation adapter | A legacy consumer cannot change quickly | Adapter becomes another maintained component |
| Coordinated deployment | Small controlled system during a narrow migration | Couples release schedules and is unsafe for long-lived/external clients |

Versioning is not permission to break consumers casually. Even `/v2` needs discovery, migration, monitoring, and retirement plans.

## Failure drill

**Scenario:** a producer changes event field `authorId` from a number to an object and deploys successfully. An older feed consumer cannot deserialize the first new event.

1. **Prediction:** the consumer repeatedly fails on the same message, and later messages may stop behind it.
2. **Observable symptom:** consumer lag increases, dead-letter volume appears, and fresh posts do not enter feeds.
3. **Immediate recovery:** pause or roll back the producer, quarantine incompatible events, deploy a consumer that accepts the supported forms, and replay from the last safe position.
4. **Data check:** compare published post IDs with feed-projection IDs after replay; “consumer is running” is not enough.
5. **Prevention:** compatibility checks in CI, representative old-event replay tests, consumer contracts, and an expand-before-remove release order.

## Two-to-three-minute interview-ready answer

“I treat an API or event as a contract that includes structure and meaning: fields, required rules, error behavior, units, and delivery expectations. I first classify a change as compatible or breaking from the consumer's perspective, not merely by asking whether the new JSON parses. Removing a field, changing its type, requiring previously optional input, or silently changing dollars to cents is breaking.

For compatible evolution I use expand-migrate-contract. I add an optional new field while preserving the old field, make the provider populate both, and deploy consumers that can read the new form while falling back to the old one. I measure adoption and deprecated-field use, retain enough time for rollback and long-lived clients, and remove the old form only after the agreed support window. A truly incompatible change needs a new field, major endpoint version, or event type rather than hidden semantic reuse.

Events require extra care because retained messages may be replayed years later. Consumers should ignore unknown fields, supply documented defaults for missing optional fields, and understand every version the replay policy supports. I put machine-readable schemas and real consumer contracts in CI, verify the provider against supported consumers, and replay old event fixtures. A schema registry catches structural incompatibility, but human review must still catch changed meaning.

For example, renaming this project's `caption` response field to `text` in one deployment would make the old web client render blank content. I would return both temporarily and migrate the client first. If a producer instead sends an incompatible event and the feed consumer stops, I would pause the producer, quarantine the bad messages, deploy a compatible reader, replay, and reconcile source post IDs with feed IDs. I monitor deserialization failures, dead letters, lag, client versions, and deprecated use. The trade-off is temporary duplicate fields, versions, and adapters, but that buys independent deployment, replayability, and safe rollback.”

## Questions and explained answers

<details>
<summary>1. Is adding a response field always backward-compatible?</summary>

No. Well-behaved JSON consumers usually ignore unknown fields, but strict deserializers can reject them, payload growth can matter, and a new enum member may break exhaustive logic. Verify the supported consumers rather than assuming “additive” means harmless.

</details>

<details>
<summary>2. When should you create a new API version instead of adding a field?</summary>

Use a new major version when the old and new meanings cannot honestly coexist—for example, removing required behavior, changing a field's type or units, or adopting a workflow incompatible with old clients. Simple optional additions usually do not need a new major version.

</details>

<details>
<summary>3. Why are event contracts often harder to retire than synchronous APIs?</summary>

Events may remain in durable logs, backups, or replay pipelines long after current producers change. A new consumer may need to rebuild state from years of versions. The organization must either preserve readers for those versions, translate them, or deliberately bound replay support.

</details>

<details>
<summary>4. What does a consumer contract test catch that a provider unit test may miss?</summary>

A provider unit test proves behavior the provider author expected. A consumer contract captures a dependency a real consumer relies on, such as a field, status code, or event shape. It can reveal that a “safe” provider refactor violates an actual consumer expectation, though runtime and end-to-end tests are still needed.

</details>
