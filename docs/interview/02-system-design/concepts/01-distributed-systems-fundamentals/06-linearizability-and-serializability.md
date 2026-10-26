# Micro-lesson 06 — Linearizability and serializability answer different questions

[Module](./README.md) · [Previous: session guarantees](./05-session-and-causal-consistency.md) · [Next: quorums](./07-quorums.md)

- Artifact: `READY`; comprehension: `TODO`.
- Core study: 10–15 minutes. Primary idea: distinguish real-time operation order from a valid order of multi-step transactions.
- Prerequisite: reads, writes, completed versus overlapping operations; consistency describes allowed observations.
- Core terms: linearizability, serializability, strict serializability.

## Why “the database is consistent” is too vague — Must understand

Suppose a profile must keep at least one featured post. Two concurrent editors each remove a different featured post after checking that another remains. Separately, after one removal succeeds, a reader still sees the removed flag. The first problem concerns interacting decisions across records; the second concerns when a completed change becomes observable.

A **transaction** groups reads and writes into one unit with a commit or rollback outcome. Its **isolation** controls how concurrent transactions interact. These are prerequisite ideas, not a claim that every transaction automatically prevents every race.

## Define the three promises

**Linearizability** requires each covered operation to appear to take effect at one instant between its start and response. If operation A finishes before B starts, B must come after A in that logical order. Overlapping operations can be ordered either way when their results fit. “One instant” is an observational model, not a demand that the implementation execute in zero time. See the [original Herlihy–Wing paper](https://www.cs.columbia.edu/~wing/publications/HerlihyWing90.pdf).

**Serializability** requires the combined results of committed transactions to match some execution of those transactions one at a time. “Serial” means one finishes before the next begins in that explanatory order. Implementations may run work concurrently, then reject an attempt that cannot fit a valid order. The abstract guarantee alone does not require that order to match real-time completion.

**Strict serializability** combines serializability with real-time order of transactions. It answers both questions together at the specified transaction scope. Naming the scope matters: a guarantee on one key does not automatically cover an operation spread across several records or systems.

## Work through one fixed dataset

Initial state: post 42 has `featured=true`; post 43 has `featured=true`. The business rule requires at least one to remain featured. Two proposed transactions are:

- T42 reads 43's flag and clears 42 only if 43 is still featured.
- T43 reads 42's flag and clears 43 only if 42 is still featured.

Each transaction's check and change belong to one transaction. Trace a concurrent execution:

1. T42 reads 43=true and decides clearing 42 is permitted.
2. Before T42 commits, T43 reads 42=true and decides clearing 43 is permitted.
3. T42 writes 42=false; T43 writes 43=false.
4. If both commit, the final state is `[42=false, 43=false]`. Each individually checked a true flag, but the combined business rule is broken.

Why is that outcome not serializable? Enumerate both possible serial orders:

| Order | First transaction's result | Second transaction's check | Final flags [42,43] |
|---|---|---|---|
| T42 then T43 | 42 becomes false | T43 reads 42=false and skips clearing 43 | [false,true] |
| T43 then T42 | 43 becomes false | T42 reads 43=false and skips clearing 42 | [true,false] |

Neither order yields `[false,false]`. Therefore a serializable execution must prevent both commits with those observations. It can block conflicting work or abort an attempt so the application retries with fresh reads.

Now consider the real-time observation question on this same dataset: T42 completes successfully, making 42=false. With no later restoration, a subsequent read of 42 returns true. A linearizable read/write contract forbids that stale result. The serializability definition by itself does not add real-time order between transactions; strict serializability does. Do not infer arbitrary stale behavior from PostgreSQL specifically; this is a comparison of abstract guarantees.

## Why retry is part of correct usage

If T43 is aborted, rerunning only its final “set false” statement would reuse a decision based on an obsolete read. Retry the whole transaction: reread 42=false, re-evaluate the condition, and skip removal. Bound retries and report exhaustion rather than looping forever.

[PostgreSQL's serializable isolation documentation](https://www.postgresql.org/docs/current/transaction-iso.html) explains that concurrent work can receive serialization failures and that applications must retry. It does not make an external cache or file write part of the database transaction.

A scheduling analogy helps: decisions must be explainable as if one editor finished before the other. The analogy stops where a database automatically detects dependencies and may abort work rather than physically scheduling every statement sequentially.

## Choice and project mapping

Use serializable transactions, explicit row locking, or an appropriate atomic data model when a cross-record invariant requires it. Compare contention, retry cost, and implementation complexity. Linearizable single-record reads are useful for freshness, but individually linearizable reads/writes do not make a multi-step check-and-change atomic.

[PostService.like and unlike](../../../../../src/main/java/com/instagram/backend/service/PostService.java) contain read/check/write sequences in transactions, but do not explicitly request serializable isolation. A transaction annotation alone is not a proof against races. The featured-post dataset is proposed to isolate the reasoning; no featured flags are implemented by this lesson.

## Failure drill — paper exercise

- **Predict:** both checks read true. Can both removals commit under a correct serializable contract?
- **Detect:** inspect the invariant and transaction outcomes, not only successful individual statements.
- **Contain:** prevent a second removal based on stale decisions; avoid external side effects before an attempt can safely complete.
- **Recover:** retry the aborted transaction from its initial read; verify one flag remains true.
- **Prevent:** test concurrency and retry exhaustion. Choose a contract covering the entire invariant, including all relevant rows.

## Recap and stop/go

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
