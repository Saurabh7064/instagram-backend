# Micro-lesson 06 — Linearizability and serializability answer different questions

[Module](./README.md) · [Previous: session guarantees](./05-session-and-causal-consistency.md) · [Next: quorums](./07-quorums.md)

Core study: 10–15 minutes. Prerequisite: reads, writes, completed versus overlapping operations; consistency describes allowed observations.

## Both editors checked. How did both posts disappear? — Must understand

A profile has two featured posts, 42 and 43. The rule is simple: at least one must remain featured. One editor wants to remove 42; another wants to remove 43. Each first checks that the other post is still featured. Can both checks pass and still leave the profile with none?

Yes, if the checks and changes interact in the wrong way. This is a different question from “after a successful edit, can someone still read the old value?” The first asks whether several decisions fit together; the second asks when a completed change becomes visible. We will use the same two-post setup to separate them.

A **transaction** groups reads and writes into a unit that commits or rolls back. Its **isolation** controls how it interacts with concurrent transactions. Putting each check and change inside a transaction is a starting point, but the chosen isolation promise determines which combined outcomes are allowed.

## Try both one-at-a-time orders to test the result

Initial state: post 42 has `featured=true`; post 43 has `featured=true`. The business rule requires at least one to remain featured. Two proposed transactions are:

- T42 reads 43's flag and clears 42 only if 43 is still featured.
- T43 reads 42's flag and clears 43 only if 42 is still featured.

Each transaction's check and change belong to one transaction. Trace a concurrent execution:

1. T42 reads 43=true and decides clearing 42 is permitted.
2. Before T42 commits, T43 reads 42=true and decides clearing 43 is permitted.
3. T42 writes 42=false; T43 writes 43=false.
4. If both commit, the final state is `[42=false, 43=false]`. Each individually checked a true flag, but the combined business rule is broken.

Could these results be explained by running one whole transaction before the other? There are only two possible orders:

| Order | First transaction's result | Second transaction's check | Final flags [42,43] |
|---|---|---|---|
| T42 then T43 | 42 becomes false | T43 reads 42=false and skips clearing 43 | [false,true] |
| T43 then T42 | 43 becomes false | T42 reads 43=false and skips clearing 42 | [true,false] |

Neither order yields `[false,false]`. A contract requiring a valid one-at-a-time explanation must therefore prevent both commits with those observations. It can block conflicting work or abort an attempt so the application retries with fresh reads.

Now consider the real-time observation question on this same dataset: T42 completes successfully, making 42=false. With no later restoration, a subsequent read of 42 returns true. That result breaks a promise that a later read includes a completed change. Now we can name that promise and distinguish it from the rule that prevents both removals. These are abstract contracts, rather than a claim that PostgreSQL permits every behavior described here.

## Two questions need two different guarantees

**Linearizability** requires each covered operation to appear to take effect at one instant between its start and response. If operation A finishes before B starts, B must come after A in that logical order. Overlapping operations can be ordered either way when their results fit. “One instant” is an observational model, not a demand that the implementation execute in zero time. See the [original Herlihy–Wing paper](https://www.cs.columbia.edu/~wing/publications/HerlihyWing90.pdf).

**Serializability** requires the combined results of committed transactions to match some execution of those transactions one at a time. “Serial” means one finishes before the next begins in that explanatory order. Implementations may run work concurrently, then reject an attempt that cannot fit a valid order. The abstract guarantee alone does not require that order to match real-time completion.

**Strict serializability** combines serializability with real-time order of transactions. It answers both questions together at the specified transaction scope. Naming the scope matters: a guarantee on one key does not automatically cover an operation spread across several records or systems.

## An aborted decision must be made again

If T43 is aborted, rerunning only its final “set false” statement would reuse a decision based on an obsolete read. Retry the whole transaction: reread 42=false, re-evaluate the condition, and skip removal. Bound retries and report exhaustion rather than looping forever.

[PostgreSQL's serializable isolation documentation](https://www.postgresql.org/docs/current/transaction-iso.html) explains that concurrent work can receive serialization failures and that applications must retry. It does not make an external cache or file write part of the database transaction.

A scheduling analogy helps: decisions must be explainable as if one editor finished before the other. The analogy stops where a database automatically detects dependencies and may abort work rather than physically scheduling every statement sequentially.

## Choice and project mapping

Use serializable transactions, explicit row locking, or an appropriate atomic data model when a cross-record invariant requires it. Compare contention, retry cost, and implementation complexity. Linearizable single-record reads are useful for freshness, but individually linearizable reads/writes do not make a multi-step check-and-change atomic.

[PostService.like and unlike](../../../../../src/main/java/com/instagram/backend/service/PostService.java) contain read/check/write sequences in transactions, but do not explicitly request serializable isolation. A transaction annotation alone is not a proof against races. The featured-post dataset is proposed to isolate the reasoning; no featured flags are implemented by this lesson.

## Let both editors read true — paper exercise

Pause each transaction after it reads the other post's true flag. Predict whether a serializable contract can allow both removals to commit with those observations.

Check the final business rule and transaction outcomes together. Two successful statements are insufficient evidence when their combination leaves no featured post. Prevent the second removal from proceeding on an obsolete decision, and keep external effects out of an attempt that may still abort.

If one transaction aborts, restart it from its initial read. After T42 commits, T43 must reread 42=false and skip its removal; retrying only the final write would repeat the bad decision. Test contention and retry exhaustion as well as the normal case. The protection must cover all rows involved in the rule, with bounded retries and an explicit failure result if they cannot finish.

## Before moving on

Artifact: `READY`; comprehension: `TODO`.

Derive both serial orders, then explain the separate stale-read failure. Say why whole-transaction retry is necessary and when strict serializability adds a guarantee.

## Questions and explained answers

<details>
<summary>1. Why is [false,false] forbidden for the two committed transactions?</summary>

There are only two serial orders. In either one the second transaction sees the first cleared flag and leaves its own flag true. The concurrent outcome matches neither order, so it cannot satisfy serializability.

</details>

<details>
<summary>2. Does linearizability require one physical thread to execute every operation?</summary>

No. Operations may overlap internally. Their observations must admit a valid order with an effective point inside each operation's interval, preserving completed-before-started order.

</details>

<details>
<summary>3. What does strict serializability add, and why is retrying only the final write wrong?</summary>

It adds real-time transaction order to a valid serial explanation. Retrying just the final write retains a decision made from stale reads; the complete transaction must recompute that decision on fresh state.

</details>
