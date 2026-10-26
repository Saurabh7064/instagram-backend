# Micro-lesson 05 — See your own writes and preserve dependencies

[Module](./README.md) · [Previous: consistency models](./04-consistency-models.md) · [Next: linearizability and serializability](./06-linearizability-and-serializability.md)

- Artifact: `READY`; comprehension: `TODO`.
- Core study: 10–15 minutes. Primary idea: useful consistency promises can follow a particular client or a dependency, without making all readers globally current.
- Prerequisite: an old replica can return a stale observation after a write succeeds.
- Core terms: session, read-your-writes consistency, causal consistency.

## A product-level problem — Must understand

An author publishes a post successfully, opens their profile, and cannot see it. Another user sees a reply saying “Great photo!” before seeing the photo it answers. Both experiences are confusing, but they require different guarantees to prevent them.

A **session** groups a client's related operations, with whatever identity or metadata the service uses to carry its guarantees. Logging in does not automatically create a storage-consistency promise. The implementation must connect that identity to read routing or version tracking.

**Read-your-writes consistency** promises that a session's later reads include its own completed writes, or a later state incorporating or superseding them. It does not promise that every other client sees those writes immediately. “Include” is about the state history: a legitimate later deletion can mean the post is gone, rather than requiring old content to remain forever.

**Causal consistency** preserves dependencies. If one operation used another operation's result, observers must not see the dependent effect without the prerequisite history. A reply written after reading a post depends on that post. Independent edits need not have one shared global order.

## Fixed example: author, friend, post 42

Initial state: replicas A and B have no post 42 or reply 9. Author Lea writes through A. Friend Noor also reads through A. B receives updates later. No one deletes anything in this example.

1. Lea creates post 42 at A and receives success. A has 42; B does not yet have it.
2. Lea opens her profile. If the request goes directly to old B, the post is absent. This violates read-your-writes for Lea.
3. A compliant path must find a source at least as current as Lea's completed write, wait for B to catch up, or report that the promised read is unavailable. Remembering “Lea needs version containing 42” is one proposed mechanism.
4. Noor reads 42 at A, then creates reply 9 about it. Reply 9 now has a dependency on post 42's creation.
5. Suppose messages deliver reply 9 to B before 42. Exposing reply 9 immediately would violate the dependency promise. B must wait for 42's history or fetch it before showing the dependent reply.
6. Once B has both, it can show 42 and reply 9 together. Lea's own-write requirement and Noor's dependency requirement are both satisfied in this run.

| Promise | Who is protected? | Forbidden observation here |
|---|---|---|
| Read-your-writes | Lea's later session reads | Lea sees no 42 after successful creation |
| Causal consistency | Observers of the dependent reply | Reply 9 appears without the prerequisite 42 |
| Global real-time current reads | Every covered reader | Any later reader missing a completed write |

The third row is stronger in a different dimension. The first two do not independently force every unrelated reader to observe the newest unrelated post.

## How to carry the necessary knowledge

A future service might return a required version with the write result and carry it on later reads. The read path then checks whether its selected copy has reached that version. Dependency-aware delivery similarly needs information about what earlier history a reply relies on. Without carrying such information, a load balancer merely choosing a healthy server cannot know the read is sufficiently current.

Routing a user repeatedly to the same server is **sticky routing**. It can help under specific conditions, but failover can send the session to an older copy. An application server's identity is also different from the database replica it queries. Test rerouting, not only refreshes that happen to hit one node.

The analogy is outgoing mail and replies: you expect your own sent letter to appear in your history, and a reply presupposes the original. It stops matching because distributed storage must encode dependencies explicitly; it cannot rely on a reader recognizing missing context.

For background, Vogels describes [client-centric and causal observation guarantees](https://www.allthingsdistributed.com/2008/12/eventually_consistent.html). The routing/version mechanism above is a proposed design for this example, not a statement about a particular product.

## Project mapping and trade-offs

[PostService.create and feed](../../../../../src/main/java/com/instagram/backend/service/PostService.java) do not carry replica-version metadata. Replies in this example are proposed; this method set does not implement the future comments feature. Read-your-writes routing could improve author feedback, while dependent-delivery tracking fits later comments or notifications. Both add metadata and can delay reads when required history is unavailable. Prefer an authoritative read if its simplicity and latency fit the application.

## Failure drill — paper exercise

- **Predict:** A becomes unreachable after step 1; Lea's next read goes to old B. Does sticky routing preserve the promise?
- **Symptom:** a successful publish followed by a profile missing 42; session/version evidence reveals the inconsistency.
- **Contain:** do not return B's old result as a read satisfying Lea's guarantee.
- **Recover:** catch B up or use another sufficiently current source, then verify 42. Once reply 9 arrives, verify its prerequisite history is exposed first.
- **Prevent:** test failover with carried requirements and delayed dependent messages. Independent events may still appear in different orders, which these promises allow.

## Recap and stop/go

Explain which client owns the read-your-writes promise and why reply 9 requires post 42. Compare these with a global latest-read requirement; predict the failover behavior without notes.

## Questions and explained answers

<details>
<summary>1. Must an unrelated reader see 42 immediately because Lea has read-your-writes?</summary>

No. That promise covers Lea's session. Guaranteeing all later readers the newest completed write requires a broader observation contract.

</details>

<details>
<summary>2. Why is reply 9 arriving before 42 different from two independent posts arriving in different orders?</summary>

Noor created reply 9 using post 42's content, so the reply carries a dependency. Independent posts have no such relationship. Causal consistency preserves dependency order rather than inventing one order for everything.

</details>

<details>
<summary>3. What must a failover read path know to preserve Lea's promise?</summary>

It needs evidence of Lea's completed write and whether the selected source includes that history. A server being alive or selected by session affinity is insufficient evidence of freshness.

</details>
