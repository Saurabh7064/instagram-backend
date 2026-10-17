# Zero-Downtime Schema Evolution

## Question

You need to add post visibility to millions of rows while old and new application versions run together during a rolling deployment.

**“How would you change a large database schema without downtime?”**

## Why this problem exists

Application and database changes do not become active atomically. Old instances may still read the previous shape, a large backfill can lock rows or exhaust I/O, and an immediate constraint can reject old-version writes. A safe change keeps both versions compatible until data migration and rollout are proven.

## Analogy

To replace a bridge without closing the road, engineers build the new path, redirect traffic gradually, and remove the old path only after verification. The analogy stops because database clients may cache schemas, transactions overlap, and rollback cannot automatically undo already transformed data.

## Terms to establish

- **Expand–migrate–contract** means add a compatible shape, move data and traffic, then remove the old shape later.
- A **backfill** updates existing rows to the new representation.
- **Dual read/write** temporarily supports both old and new representations during migration.
- A **compatibility window** is the period when multiple application versions and schema shapes must coexist.

## What I would do

1. Expand with an additive nullable column or new table that old code can ignore.
2. Deploy code that understands both shapes and writes the new value safely, using a default in application logic where needed.
3. Backfill in small, restartable, rate-limited batches with checkpoints and production monitoring.
4. Verify coverage, correctness, query plans, and rollback behavior before adding a constraint or switching all reads.
5. Contract only after every old version, job, and consumer is gone; remove compatibility code in a later release.

## Concrete walkthrough

1. Add nullable `visibility` to `posts`; old instances continue writing rows without it.
2. New instances treat `null` as `PUBLIC` and write explicit values for new posts.
3. A worker updates old rows in small ID ranges, recording progress and pausing when database latency rises.
4. After verification shows no nulls and all old instances are drained, add the default and `NOT NULL` constraint using a safe migration method.
5. The observable result is mixed-version compatibility throughout the rollout, followed by a verified single representation.

## Instagram-backend mapping

The current [Post](../../../../../src/main/java/com/instagram/backend/domain/Post.java) entity has caption, media URL, location, count, and creation time but no visibility field. [PostService](../../../../../src/main/java/com/instagram/backend/service/PostService.java) reads and writes that entity directly. [application.properties](../../../../../src/main/resources/application.properties) currently uses Hibernate `ddl-auto=update`, which is convenient locally but does not provide reviewed, versioned production migrations.

**Proposed, not implemented:** Flyway or Liquibase migrations, compatibility tests across application versions, a checkpointed backfill job, deployment gates, and separate expand and contract releases.

## Choices and trade-offs

- Choose an additive nullable change for mixed-version safety. Do not add an expensive non-null default in one blocking operation without database-specific proof; temporary null handling is the cost.
- Choose application-level dual read/write when an old and new representation must coexist. Keep the window short because duplicated logic can drift.
- Choose a shadow table for major remodels or online copy tools when in-place changes are unsafe. They reduce lock risk but add synchronization, cutover, and rollback complexity.

## Failure drill

**Prediction:** the old column is dropped while old pods still read it. **Symptom and detection:** only old-version instances emit SQL column errors immediately after migration. **Containment:** stop the rollout and route traffic away from incompatible instances. **Recovery:** restore a compatible view/column when safe or roll forward every instance; do not blindly roll back transformed data. **Prevention:** enforce expand–migrate–contract stages, version compatibility tests, and a deployment gate proving old consumers are gone.

## Two-to-three-minute interview answer

I would use expand–migrate–contract. First I would make an additive schema change that old code can ignore, such as a nullable `visibility` column. Then I would deploy code that can read both old and new rows and writes the new field for all new traffic. Existing rows would be backfilled in small, restartable batches with checkpoints, throttling, and database latency monitoring.

Only after verifying data coverage, query plans, constraints, and the absence of old application versions or jobs would I switch reads fully, add stricter constraints, and later remove the old shape. Expand and contract should normally be separate releases so rollback remains possible during the compatibility window. For this project I would replace production use of automatic Hibernate schema updates with versioned reviewed migrations and compatibility tests. The key principle is that deployment order, backfill load, and rollback semantics are part of the schema design.

## Practice status

The artifact is ready; comprehension remains unproven until the learner can order the migration stages, predict a mixed-version failure, and explain when rollback becomes unsafe.

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
