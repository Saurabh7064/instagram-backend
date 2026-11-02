# Micro-lesson 15 — Logical clocks: order dependencies without trusting wall time

[Module](./README.md) · [Previous: safe retries](./14-idempotency-and-delivery.md) · [Next: vector clocks](./16-vector-clocks.md)

Core study: 10–15 minutes. Prerequisites: independent processes send messages; a reply can depend on a received request.

## A reply cannot cause the request that came before it — Must understand

A sends a request to B, and B sends a reply. You inspect their logs and find the reply's displayed time is earlier than the request's. The machine clocks disagree. Sorting those times has made a real dependency look backwards.

Start with what we know without trusting either clock. A must send before B can receive. B receives before it sends its reply. Those relationships form a chain from A's request to B's reply. An **event** is one such action: sending, receiving, or doing local work.

**Happened-before** names this dependency relation. Earlier actions in one process precede later ones; sending precedes receiving; and chains preserve that order. If no chain connects two events either way, the model calls them concurrent. That word does not require them to occur at the same physical instant.

In our fixed exchange, A and B send one request and reply. C does two local events and sends no messages. We want numbers that preserve the A/B dependency chain without pretending C's unrelated work caused anything in it. A counter can preserve the first requirement, but we will see its limit on the second.

## Make a receive number larger than both known histories

A **logical clock** tracks ordering information through counters or metadata rather than estimating physical time. A **Lamport timestamp** is a logical counter assigned according to these rules. Lamport is a person's name, not an acronym.

For this lesson's convention, each process starts at 0:

- Before a local event, including sending a message, increment the local counter by 1.
- Put the send event's counter on the message.
- At a receive event, set the receiver's counter to `max(localCounter, receivedCounter)+1`. That assignment is the receive event's increment; do not increment again for the same event.

Why this receive formula? The receipt must follow the receiver's own previous event *and* the remote send. Taking the maximum preserves whichever known count is larger. Adding one makes the receipt strictly later than both. Simply copying the received count can move a busy receiver backward; simply incrementing the local count can leave it below the remote send.

The [original Lamport paper](https://lamport.azurewebsites.net/pubs/time-clocks.pdf) formalizes happened-before and the clock condition: dependency order implies increasing timestamps. The formulas below are a concrete instance of those rules.

## Calculate the request and reply counters

Processes A/B exchange one request and reply. C performs independent local work and sends no messages. Initial counters are A=0, B=0, C=0.

1. A sends the request: A changes 0→1 and the request carries 1.
2. B receives it: local=0, received=1; max(0,1)+1=1+1=2. B changes 0→2.
3. B sends its reply: B changes 2→3 and the reply carries 3.
4. A receives the reply: local=1, received=3; max(1,3)+1=3+1=4. A changes 1→4.
5. Independently, C performs two local events: 0→1→2. There is no communication connecting them to the A/B exchange.

| Event | Local value before | Message value | Calculation | Event timestamp |
|---|---:|---:|---|---:|
| A sends | 0 | none | 0+1 | 1 |
| B receives | 0 | 1 | max(0,1)+1 | 2 |
| B replies | 2 | none | 2+1 | 3 |
| A receives | 1 | 3 | max(1,3)+1 | 4 |
| C first local event | 0 | none | 0+1 | 1 |
| C second local event | 1 | none | 1+1 | 2 |

A's request-send timestamp 1 is less than B's receive 2, which is less than reply-send 3, which is less than A's receive 4. The dependency chain is preserved without comparing wall clocks.

But C's second event also has timestamp 2. Its number being below A's final 4 does not prove C influenced A. Numbers from unrelated counters can compare even when events are independent.

## A smaller counter does not prove a dependency

Write L(e) for event e’s Lamport timestamp. If event e happened before f, then L(e)<L(f). The converse is false: L(e)<L(f) does not establish a dependency. The C event demonstrates that missing inference using the same fixed exchange.

To sort all events deterministically, an application can compare `(counter, processID)` pairs, using process identity to break equal counters. That gives a chosen total order, meaning every pair can be ranked. It does not make the tie-break order a real causal relationship or a measure of elapsed time.

Numbering letters so replies use larger numbers is an analogy. The analogy stops where unrelated letter writers can choose similar numbers; a larger number alone does not prove one writer read another's letter.

## Choice and project mapping

Use logical ordering when dependency order matters and physical clock agreement is unsuitable. Use physical timing tools for duration measurements; counter 4 does not mean four milliseconds or seconds. Use vector clocks when distinguishing independent histories is necessary, as the next lesson explains. Counter persistence, restart identity, and message metadata need a defined lifecycle in a real implementation.

[PostRepository.findAllByOrderByCreatedAtDesc](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) orders posts by a stored creation time, not by Lamport metadata. [RequestCorrelationFilter](../../../../../src/main/java/com/instagram/backend/observability/RequestCorrelationFilter.java) helps correlate a request; an identifier alone is not a logical clock. Cross-service logical timestamps are proposed work here.

## Let B's wall clock run behind A's — paper exercise

Predict whether sorting displayed times can place B's reply before A's request. Then compare that order with the known send-before-receive chain.

The contradiction tells us that wall-time sorting is insufficient for dependency reconstruction. A larger scalar counter alone is also insufficient proof that unrelated events influenced each other, so keep message relationships alongside the carried logical metadata.

Reconstruct the exchange from those relationships and logical timestamps. Physical time remains useful for diagnostics, with its uncertainty understood. Define counter persistence and process identity across restart, and propagate the metadata with messages. No clock can reconstruct a relationship that was never recorded or turn incomplete logs into a complete history.

## Before moving on

Artifact: `READY`; comprehension: `TODO`.

Derive 1,2,3,4 with the receive formula and explain C's two independent events. State what increasing counters prove and what they cannot prove before advancing.

## Questions and explained answers

<details>
<summary>1. Why does B receive at 2 rather than 1?</summary>

The received message carries 1 and B's local counter is 0. The receipt must be after both histories, so max(0,1)+1=2. Copying 1 would fail the strict send-before-receive increase.

</details>

<details>
<summary>2. Does C's timestamp 2 being below A's timestamp 4 prove C caused A's event?</summary>

No. C sent no messages and has no dependency chain to A. Lamport order preserves known causality in one direction but does not detect all concurrency.

</details>

<details>
<summary>3. What is gained and lost by sorting (counter, process ID)?</summary>

The pair gives a stable deterministic order even for equal counters. The tie-break order is chosen, not evidence of causality or elapsed duration; unrelated events remain unrelated.

</details>
