# Micro-lesson 05 — Session guarantees: causality and your own writes

[Module](./README.md)

- Artifact: `READY`; comprehension: `TODO`.
- Time: 10–15 minutes. Primary idea: session guarantees: causality and your own writes.
- Prerequisite: Complete the stop/go check for [Consistency models: strong and eventual](./04-consistency-models.md).
- Original roadmap topics: 7–8.

## Why does this exist? — Must understand

Causal consistency preserves dependencies: if a reply was created after reading a post, observers must not see that reply without its prerequisite post. Read-your-writes consistency ensures a client’s later reads include its own completed writes. It does not require all other clients to see them immediately.

## Analogy

A reply letter presupposes the original letter; the author also expects to see their own sent mail. Unlike letters, a service must carry dependencies across routing and failover explicitly.

## Predict before reading

Does read-your-writes guarantee everyone sees post 42? Explain your prediction before expanding the checkpoints below.

## Concrete flow

1. Author creates post 42.
2. immediately opens profile.
3. a lagging replica lacks 42. Routing this session to a caught-up source can preserve read-your-writes. Later a friend reads 42 and replies: causal delivery must expose 42 before that dependent reply.

## Instagram-backend mapping

[PostService.create, feed, and transactional methods](../../../../../src/main/java/com/instagram/backend/service/PostService.java) are the real database boundary. Replication, consensus, distributed locks, durable request-key handling, and multi-service workflows in this lesson are **proposed**, not implemented by these annotations.

## Decision and trade-off

Use session version tracking or authoritative reads for immediate author feedback. Dependency tracking costs metadata and may delay reads; simple sticky routing alone fails when the selected node dies or is stale.

## Failure drill — prediction, symptom, recovery, prevention

Predict the profile after failover to a lagging replica. In a proposed test, record a session’s required version, then reroute. Wait for catch-up or use the authoritative source. Verify the guarantee survives failover rather than just a normal refresh.

These are design/paper drills, not claims that a replica cluster or fault-injection lab was executed.

## Questions and explained answers

<details>
<summary>1. Does read-your-writes guarantee everyone sees post 42?</summary>

No. It is a guarantee for the writing client/session.

</details>

<details>
<summary>2. Can causal consistency show a dependent reply first?</summary>

No. The observed dependency must be respected.

</details>

<details>
<summary>3. Is sticky routing enough?</summary>

Only with additional assumptions about durability and freshness; rerouting exposes the weakness.

</details>

## Teach-back and stop/go

Explain the problem, trace the example, defend the choice, and diagnose the failure in 60–90 seconds without notes. Proceed only after a correct prediction, explanation, and failure diagnosis; reading this artifact does not complete comprehension.
