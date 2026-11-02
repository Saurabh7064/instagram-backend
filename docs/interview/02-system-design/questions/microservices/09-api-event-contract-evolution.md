# 09 — Evolving API and Event Contracts Safely

> **Interview question:** How would you evolve APIs and event schemas without breaking older consumers while microservices deploy independently?

> [One-page revision card](../../revision-cards/microservices/09-api-event-contract-evolution.md)

## The backend deployed. Why did the old app break?

Imagine the post API starts returning `text` instead of `caption`. The new backend passes its tests and returns valid JSON. An older phone app still asks the response for `caption`, so the post appears to have no text.

Within one application, we could rename the field and every caller together. Once callers deploy independently, that assumption disappears. A phone may run an old app for months. During a server rollout, old and new instances coexist. A queued event can outlive both, and an emergency rollback can bring an older reader back into use.

The design problem is to let the producer change while those readers continue working. Compatibility is therefore about what supported consumers can still do, rather than whether the latest producer builds successfully.

## Keep the old plug working while devices move

If a building has old plugs, replacing every wall outlet overnight breaks existing devices. A safer transition supports the old plug while an adapter or new outlet is introduced, moves devices gradually, verifies usage, and removes the old form only after it is unused.

The analogy has limits. Software contracts include meaning, validation, ordering, security, and side effects—not only shape. Two schemas may look compatible while a changed meaning silently corrupts behavior.

## A field carries a promise, not just a type

The consumer relies on more than the spelling of `caption`. It may rely on the field being present, containing text, and having the same meaning as before. It also relies on error responses and authorization behavior. Together, those expectations form the **contract** between producer and consumer.

An **additive change** introduces something a supported old reader can safely ignore, such as an optional `location` object alongside the existing `locationLabel`. A **breaking change** invalidates an expectation: removing `caption`, turning its string into an object, or requiring a new input old clients never send.

Even an addition needs a little thought. A strict parser may reject unknown fields. A new enum value can break a reader that assumes only the existing values exist. A larger response can exceed a client's practical size or latency limit. “Additive” describes the intended migration; supported-reader tests establish whether it is safe.

A consumer contract test records an expectation from an actual consumer and checks that the producer still meets it. It gives the old app a voice in the producer's build. It cannot prove every production interaction, but it catches disagreements that tests written only for the new producer might miss.

## Predict before reading

The post API renames response field `caption` to `text` and deploys before the web client. The JSON is otherwise valid. What will the old client render?

<details>
<summary>Reveal the prediction</summary>

The old client still looks for `caption`, so it may render blank text or fail. JSON validity does not make the change backward-compatible. During migration the provider should usually return both fields with the same meaning, migrate consumers, observe old-field use, and remove it only under a declared version/deprecation policy.

</details>

## Let both clients work during the location change

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

Now readers can move at different times. Update their code and contract tests, deploy them, and check that they actually use the new representation. A release announcement does not mean every phone upgraded.

For a response field, the server usually cannot see which property the client read. Adoption evidence may need supported client-version telemetry, consumer confirmation, or a separately versioned endpoint. For an old request field, the server can count its continued use directly. Agree on the deprecation window and keep enough compatibility for both long-lived clients and a rollback to an older reader.

### 3. Contract

Only after usage reaches the agreed threshold should the producer stop accepting or returning the old field. If old clients cannot be forced to upgrade, keep the compatibility layer or expose a separately versioned API.

For database changes behind the API, use the same shape: add nullable columns or new tables, make code handle both schemas, backfill, switch reads, verify, and remove the old schema in a later deployment. Never deploy code that requires a column before the database change exists everywhere.

## What changes when the reader is a message consumer?

The same compatibility problem lasts longer for events. A `PostPublished` event is a record of something that happened. A consumer rebuilding its feed next year may read that old record, long after today's producer has stopped emitting it. Renaming fields in the current code does not rewrite those stored facts safely.

Give the record an event ID, type, occurrence time, and schema version so readers can identify what they received. Add optional information with explicit defaults where possible. Readers should ignore unknown fields and know what a missing optional field means. If the business fact changes, give it a new name or version: “publication was scheduled” is not the same fact as “post was published.”

During a transition, either teach readers both supported forms or translate them through a maintained adapter. Publishing both versions can also work, but subscribers must not mistake them for two independent business events and apply the effect twice. Include that duplicate risk in the migration design.

A schema registry can reject incompatible field types before release. It cannot know that a number formerly measured in dollars now means cents. Human review must protect meaning, and replay tests using old stored examples must protect the versions we still promise to read.

## Check the consumer’s expectation before changing a field

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

For example, replacing `CreatePostRequest.locationLabel` with a required nested object in one release would break a browser that still sends only the text field. A safe path is to add an optional `location` representation while continuing to accept `locationLabel`, update clients, record old-field usage, and remove it only through a deliberate major-version policy.

[AuthIntegrationTests.java](../../../../../src/test/java/com/instagram/backend/AuthIntegrationTests.java) already asserts selected JSON fields. Those tests protect the provider's current response, but a future extracted service would also need consumer-focused contract tests and fixtures for old/new event versions.

## When coexistence needs a new version

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

Before marking this answer practiced, explain how an old phone and a new server can coexist, then predict what an old event reader will do. Try the questions below before revealing the answers; documentation readiness does not establish comprehension.

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
