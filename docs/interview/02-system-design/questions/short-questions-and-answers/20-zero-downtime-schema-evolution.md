# Zero-Downtime Schema Evolution

## Question

You need to add post visibility to millions of rows while old and new application versions run together during a rolling deployment.

**“How would you change a large database schema without downtime?”**


## The old application is still writing

We want a `visibility` field on millions of posts. A rolling deployment briefly runs old and new application versions together. The old version does not know the new field exists, so it may continue inserting rows without a value while the new version is already reading it.

This coexistence is the main problem. Adding a column, rewriting every row and immediately requiring a value couples schema locks, data movement and application rollout into one risky step.

## Make the new shape safe to encounter

First add a compatible shape, such as a nullable `visibility` column, after reviewing the database operation and lock behavior. Old code can ignore it. Deploy new code that understands both old and new rows and writes an explicit value for new posts.

For this example only, assume the product has reviewed that every existing post is public, so new code interprets a missing value as `PUBLIC`. A migration must not silently choose this default for a system that previously had private posts. Also avoid exposing new private states while old readers would ignore them.

Next update existing rows in small, restartable batches. This is a **backfill**. Record progress by stable keys, make repeating a batch safe, and slow or pause when database latency rises. The goal is complete, correct conversion while ordinary traffic continues.

## Tighten the rule only after compatibility is proven

Drain old writers and account for jobs as well as web instances. Run a final verification or catch-up pass so rows written during the backfill are not missed. Then add the required default and non-null rule using a reviewed database-specific migration. Switch fully to the new representation once its correctness is established.

This order is called **expand–migrate–contract**: add a compatible shape, move data and callers, then remove the old dependency later. The **compatibility window** is the period when both versions must work.

Replacing a bridge while keeping traffic flowing is a useful analogy. Software's limit is that returning to the old application may be unsafe after new data states are written or old fields removed. Review that rollback boundary before contraction.

Use versioned production migrations and mixed-version tests. Automatic schema updates during application startup do not provide the same reviewed deployment sequence.

## Where this appears in our backend

The current [Post](../../../../../src/main/java/com/instagram/backend/domain/Post.java) entity has caption, media URL, location, count, and creation time but no visibility field. [PostService](../../../../../src/main/java/com/instagram/backend/service/PostService.java) reads and writes that entity directly. [application.properties](../../../../../src/main/resources/application.properties) currently uses Hibernate `ddl-auto=update`, which is convenient locally but does not provide reviewed, versioned production migrations.

**Proposed, not implemented:** Flyway or Liquibase migrations, compatibility tests across application versions, a checkpointed backfill job, deployment gates, and separate expand and contract releases.

## Choosing the approach and paying its costs

- Choose an additive nullable change for mixed-version safety. Do not add an expensive non-null default in one blocking operation without database-specific proof; temporary null handling is the cost.
- Choose application-level dual read/write when an old and new representation must coexist. Keep the window short because duplicated logic can drift.
- Choose a shadow table for major remodels or online copy tools when in-place changes are unsafe. They reduce lock risk but add synchronization, cutover, and rollback complexity.


## Paper drill: an old instance reads a removed column

Predict which instances fail if contraction removes a field before all old readers are gone. Errors correlate with the old application version, even while the new version works.

Stop the incompatible rollout and route away from affected readers where possible. Recover with a compatible schema/view when safe or a deliberate roll-forward; returning to an old binary cannot repair data it no longer understands.

Prevent recurrence with mixed-version tests, a verified list of remaining readers and separate expansion and contraction releases. Check the data as well as deployment inventory: a completed rollout does not prove the backfill caught every row.

## Two-to-three-minute interview answer

I would use expand–migrate–contract. First I would make an additive schema change that old code can ignore, such as a nullable `visibility` column. Then I would deploy code that can read both old and new rows and writes the new field for all new traffic. Existing rows would be backfilled in small, restartable batches with checkpoints, throttling, and database latency monitoring.

Only after verifying data coverage, query plans, constraints, and the absence of old application versions or jobs would I switch reads fully, add stricter constraints, and later remove the old shape. Expand and contract should normally be separate releases so rollback remains possible during the compatibility window. For this project I would replace production use of automatic Hibernate schema updates with versioned reviewed migrations and compatibility tests. The key principle is that deployment order, backfill load, and rollback semantics are part of the schema design.


## Before moving on

Artifact: `READY`; comprehension: `TODO`. Explain this scenario in your own words, predict the paper drill, and attempt the questions before opening their answers. Record learning only from your own explanation and predictions.

## Questions and explained answers

<details>
<summary>Why not add the new column, backfill it, enforce `NOT NULL`, and deploy code in one step?</summary>

That combines lock risk, data movement, and application incompatibility into one failure window. Separating stages keeps old code working and makes each change observable and reversible where possible.
</details>

<details>
<summary>What makes a backfill safe to resume?</summary>

It processes bounded ranges or stable keys, records progress, and is idempotent so repeating a completed batch does not corrupt data. It must also throttle against production load.
</details>

<details>
<summary>When is application rollback no longer automatically safe?</summary>

Rollback is unsafe once the schema or stored data no longer matches what the old binary expects—for example after dropping a column or writing values the old version cannot interpret.
</details>
