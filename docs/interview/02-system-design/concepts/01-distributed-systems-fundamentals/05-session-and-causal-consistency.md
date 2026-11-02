# Micro-lesson 05 — See your own writes and preserve dependencies

[Module](./README.md) · [Previous: consistency models](./04-consistency-models.md) · [Next: linearizability and serializability](./06-linearizability-and-serializability.md)

Core study: 10–15 minutes. Prerequisite: an old replica can return a stale observation after a write succeeds.

## Lea wants to see her post; Noor wants the reply to make sense — Must understand

Lea publishes post 42, gets a success message, then opens her profile. The post is missing. Later, someone sees Noor's reply, “Great photo!”, without seeing the photo it answers. These experiences look related, but fixing one does not automatically fix the other.

Keep one setup in mind: A and B initially have neither post 42 nor reply 9. Lea writes through A. Noor reads through A. B receives updates later. Nobody deletes anything in this example.

First consider Lea. She does not necessarily need every stranger's newest post immediately. She needs her next profile read to include the post the service already told *her* it saved. **Read-your-writes consistency** names this promise for a client's related operations, grouped into a **session**. The service must connect that session to sufficiently current data; logging in alone does not do it.

Now consider Noor. Noor can write a meaningful reply only after reading the photo. A viewer should not receive that dependent reply before the history it relies on. **Causal consistency** preserves such dependencies. Independent edits need not have one global order.

These promises describe included history, not content that must remain forever. A legitimate later deletion can supersede Lea's post. We exclude deletions from this example so we can focus on delayed delivery.

## Follow the author first, then the dependent reply

Watch where the two promises make different demands on B:

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

## How would B know what a reader needs?

A future service might return a required version with the write result and carry it on later reads. The read path then checks whether its selected copy has reached that version. Dependency-aware delivery similarly needs information about what earlier history a reply relies on. Without carrying such information, a load balancer merely choosing a healthy server cannot know the read is sufficiently current.

Routing a user repeatedly to the same server is **sticky routing**. It can help under specific conditions, but failover can send the session to an older copy. An application server's identity is also different from the database replica it queries. Test rerouting, not only refreshes that happen to hit one node.

The analogy is outgoing mail and replies: you expect your own sent letter to appear in your history, and a reply presupposes the original. It stops matching because distributed storage must encode dependencies explicitly; it cannot rely on a reader recognizing missing context.

For background, Vogels describes [client-centric and causal observation guarantees](https://www.allthingsdistributed.com/2008/12/eventually_consistent.html). The routing/version mechanism above is a proposed design for this example, not a statement about a particular product.

## Project mapping and trade-offs

[PostService.create and feed](../../../../../src/main/java/com/instagram/backend/service/PostService.java) do not carry replica-version metadata. Replies in this example are proposed; this method set does not implement the future comments feature. Read-your-writes routing could improve author feedback, while dependent-delivery tracking fits later comments or notifications. Both add metadata and can delay reads when required history is unavailable. Prefer an authoritative read if its simplicity and latency fit the application.

## Send Lea to B after A becomes unreachable — paper exercise

A disappears after Lea's successful publish. Predict what happens if her next profile request fails over to B before B has post 42. Has keeping her on A earlier made this next read safe?

A successful publish followed by a missing profile post reveals the problem. Compare the session's required history with B's version. Do not label B's old result as satisfying Lea's own-write promise merely because B is healthy.

Catch B up or choose another sufficiently current source, then verify that Lea sees 42. When reply 9 arrives, verify its prerequisite history is available before exposing it too. Test both rerouting and reordered dependency messages. These promises still permit unrelated events to appear in different orders; the recovery check should test the promised relationships rather than demand a global newest-everything view.

## Before moving on

Artifact: `READY`; comprehension: `TODO`.

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
