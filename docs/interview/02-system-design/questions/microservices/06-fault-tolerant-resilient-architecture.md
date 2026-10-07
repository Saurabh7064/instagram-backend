# Design a fault-tolerant and resilient microservices architecture

> **Question:** Designing a fault-tolerant and resilient microservices architecture involves building ways for services to fail gracefully and recover quickly. How would you design a fault-tolerant and resilient microservices architecture?

## Why is this question important?

Microservices replace some in-process calls with networks, separate deployments, queues, and independently owned databases. That creates partial failure: the caller can be healthy while one dependency is slow, unreachable, restarting, or returning an ambiguous result.

A strong answer cannot be a list of “circuit breaker, retry, cache.” It must connect requirements to failure boundaries and trace what happens when a real component fails.

The goal is not a system in which nothing fails. The goal is a system that:

- continues the most important work when a component fails;
- contains the failure instead of amplifying it;
- produces correct outcomes despite retries and duplicate delivery;
- degrades explicitly when full functionality is impossible;
- detects, recovers, and learns from the incident.

## Analogy: a hospital during a power outage

A hospital does not rely on one perfect power source. It identifies critical loads, provides backup generators, separates electrical circuits, tests failover, and accepts that non-critical areas may temporarily lose power so operating rooms can continue.

In a microservice system:

- critical loads are business journeys such as checkout and payment status;
- separate circuits are bulkheads and independent failure domains;
- backup generators are redundant instances, replicas, and alternate paths;
- load priority is graceful degradation and load shedding;
- generator drills are game days and recovery tests.

The analogy stops at data consistency. Starting a backup generator does not duplicate a payment, deliver messages out of order, or create split-brain database writers. Software also needs idempotency, ownership, consistency, and reconciliation.

## Plain-language model

- A **fault** is an abnormal condition, such as one instance crashing or one network path dropping packets.
- A **failure** is the visible inability to provide the required behavior, such as checkout not accepting orders.
- **Fault tolerance** is the ability to continue required service while certain faults are occurring.
- **Resilience** is the broader ability to absorb disruption, degrade safely, recover, and return to normal operation.
- A **failure domain** is the set of components that can fail together, such as one process, node, availability zone, region, database, or shared credential.
- A **service-level objective (SLO)** is a measurable target, such as 99.95% successful checkout requests in 30 days.
- **Recovery time objective (RTO)** is the maximum targeted recovery time. **Recovery point objective (RPO)** is the maximum targeted data-loss window.

Fault tolerance is always stated against an assumed fault. “Survives one application-instance crash” is testable. “Highly available” without a failure assumption or target is not.

## Step 1: define critical journeys and measurable promises

Start with product behavior, not infrastructure.

For an e-commerce system, an example priority is:

| Journey | Correct degraded behavior | Example target |
|---|---|---|
| Browse catalog | Serve slightly stale cached data | High availability, moderate freshness |
| Recommendations | Omit section or serve cached popular items | Best effort |
| Place order | Accept once, reject clearly, or remain pending | No duplicate order; strict latency target |
| Authorize payment | Return success, decline, or explicit unknown/pending | Never double-charge |
| Send confirmation email | Delay and retry asynchronously | Eventual delivery |

Also define:

- peak and burst request rate;
- latency objectives such as p95 and p99;
- legal or durability requirements;
- RTO and RPO for each data class;
- which features may degrade and which must fail closed;
- cost constraints and expected regional failures.

For example, 99.95% availability permits roughly 21.6 minutes of unavailability in a 30-day month. That budget forces more concrete decisions than “five nines sounds good.”

## Step 2: draw failure domains, not only service boxes

An initial single-region design could be:

```text
Users
  |
DNS / CDN / WAF
  |
Regional load balancer
  |
  +---------------------+----------------------+
  |                     |                      |
AZ-A                  AZ-B                   AZ-C
API instances         API instances          API instances
  |                     |                      |
  +---------------------+----------------------+
                        |
          +-------------+------------------+
          |             |                  |
      Catalog path   Checkout path      Best-effort path
          |             |                  |
       cache/search  Saga orchestrator   Recommendations
                        |
               +--------+---------+
               |                  |
          Order Service      Inventory Service
          Order DB + outbox  Inventory DB + outbox
               |                  |
               +---- replicated message broker ----+
                                                     |
                                             Payment worker
                                                     |
                                             Payment provider

Cross-cutting: metrics, logs, traces, alerts, audit trail, configuration,
secrets, deployment controls, backups, and tested recovery procedures.
```

Now label shared dependencies. Three application instances are not truly independent if all are in one zone, use one exhausted connection pool, depend on one database with no failover, or require one unavailable identity provider on every request.

## Step 3: remove single points of failure deliberately

### Stateless application instances

Run multiple instances across independent availability zones behind health-aware load balancing. Keep request state outside one instance so another instance can continue.

Required details:

- **liveness** answers whether the process should be restarted;
- **readiness** answers whether it should receive new traffic;
- graceful shutdown stops new traffic and allows bounded in-flight work to finish;
- startup probes prevent traffic before initialization completes;
- session or authentication data must be portable across instances.

Redundancy is useful only when routing detects failure and moves traffic to healthy capacity.

### Data stores

Use a topology appropriate to the data contract:

- synchronous replicas across zones when low RPO inside the region is required;
- controlled primary failover with fencing so two primaries do not accept conflicting writes;
- read replicas for read scale only when stale reads are acceptable;
- automated backups and point-in-time recovery;
- recurring restore tests, because an untested backup is only a hope.

Replication is not a backup. A bad delete, corrupted write, or application bug can replicate immediately.

### Message broker

Use replicated durable storage, acknowledgements, and consumer offsets suitable for the delivery requirement. Producers use an outbox when database state and event publication must not split. Consumers handle duplicate delivery and poison messages. A dead-letter queue is a quarantine and investigation tool, not a place to forget failed business operations.

## Step 4: bound every remote call

An unbounded wait converts one slow dependency into resource exhaustion.

For each call, define:

1. an end-to-end deadline;
2. a shorter per-attempt timeout;
3. whether retry is safe;
4. the maximum attempts and retry budget;
5. the fallback or explicit failure response;
6. the concurrency limit;
7. the metric and trace evidence emitted.

Example timeout budget:

```text
Client deadline                         2.0 s
  Gateway budget                        1.8 s
    Checkout service budget             1.5 s
      Payment attempt timeout           0.9 s
      Remaining time for state update,
      response, and one policy decision 0.6 s
```

The exact values require measurement. The important point is that a downstream timeout leaves time for the upstream service to make and persist a safe decision before its own deadline expires.

## Step 5: use retries carefully

Retry only when:

- the failure is likely transient;
- the operation is idempotent or carries a durable idempotency key;
- enough deadline remains;
- the retry occurs at one chosen layer;
- a retry budget prevents a storm.

Use exponential backoff and jitter so thousands of clients do not retry simultaneously. Do not retry validation failures, authorization failures, deterministic business declines, or overload responses without respecting their retry guidance.

A timeout is an **unknown outcome**, not proof of failure. If a payment response is lost after authorization succeeded, a retry with a new payment ID can double-charge. Query or retry with the original idempotency key.

## Step 6: contain cascading failures

### Bulkheads

Give payment, recommendations, background work, and other risk classes bounded, independently controlled concurrency or worker pools. A stuck recommendation call must not consume all checkout capacity. See [Explain bulkhead isolation](./05-bulkhead-isolation.md).

### Circuit breakers

A circuit breaker temporarily stops calls when recent failures cross a threshold:

```text
CLOSED -- failures exceed threshold --> OPEN
  ^                                      |
  |                                      | cooldown
  +---- successful probe <---------- HALF_OPEN
```

It fails fast while the dependency recovers and admits a small number of probes later. It must not turn a correctness-sensitive unknown payment outcome into a fake decline.

### Rate limiting, backpressure, and load shedding

- rate limits control admitted request rate by caller, tenant, or operation;
- backpressure signals producers to slow down when consumers cannot keep up;
- load shedding rejects low-priority work before critical capacity is exhausted;
- bounded queues absorb short bursts, not endless overload;
- admission control uses current capacity rather than allowing every request to begin and fail late.

Autoscaling helps sustained load, but it is not immediate and can amplify pressure on a fixed-size database. Keep enough baseline capacity for expected failures and protect the downstream before adding callers.

## Step 7: preserve correctness across services

For each business entity, prefer one authoritative writer. Use:

- local database transactions within one service;
- Saga orchestration or choreography for multi-service workflows;
- transactional outbox for reliable event publication;
- inbox/deduplication and idempotent handlers for redelivery;
- reservations and expirations for temporary resource holds;
- semantic compensation for completed steps;
- reconciliation for stuck, missed, and ambiguous operations.

See [Distributed transactions and data consistency](./04-distributed-transactions-and-data-consistency.md) for the complete checkout flow.

Correctness rules must be encoded as states and constraints, not left in a diagram. Examples include unique idempotency keys, allowed state transitions, version checks, non-negative inventory constraints, and a manual-review state for facts the system cannot safely infer.

## Step 8: degrade by business priority

Graceful degradation must remain honest:

| Failure | Safe behavior | Unsafe behavior |
|---|---|---|
| Recommendation service down | Omit it or serve labeled cached results | Fail the entire product page |
| Catalog cache stale | Serve within an approved freshness window | Show stale price at checkout without validation |
| Email provider down | Queue confirmation and retry | Roll back a valid paid order |
| Payment response lost | Keep order pending; query by idempotency key | Report decline and charge again with a new key |
| Authorization system uncertain | Fail closed for protected write | Guess that the user is authorized |

Not every feature deserves a fallback. Security checks and correctness-sensitive writes often need to fail closed. The product owner should approve degraded behavior before an incident.

## Step 9: design asynchronous work for failure

Queues decouple arrival from processing and can preserve work during a short outage, but they add their own failure modes.

For each queue define:

- producer durability and acknowledgement behavior;
- consumer idempotency;
- partitioning and ordering needs;
- maximum queue age and backlog alert;
- retry delay and maximum attempts;
- poison-message handling;
- dead-letter ownership and replay procedure;
- backpressure when consumers remain slower than producers.

Track the **age of the oldest message**, not only queue length. A small queue containing one business-critical message stuck for two hours can be worse than a large queue draining normally.

## Step 10: make failures observable and diagnosable

Collect three connected forms of evidence:

- **metrics:** request rate, error rate, latency percentiles, saturation, bulkhead rejection, circuit state, queue age, connection-pool use, replica lag, and business invariants;
- **structured logs:** timestamp, service, instance, severity, trace ID, operation ID, order ID, event ID, outcome, and safe error context;
- **distributed traces:** the end-to-end path, time spent in each service, retries, messaging spans, and failure location.

Alert on customer symptoms and SLO burn, not every isolated technical error. Dashboards should answer:

1. Which journey is failing?
2. When did it start?
3. Which change or dependency correlates with it?
4. Is the system saturated, unavailable, or returning incorrect outcomes?
5. Is recovery progressing?

Never place passwords, tokens, payment details, or sensitive personal data in logs or traces.

## Step 11: make deployments reversible

Many outages come from change, not hardware.

- use backward-compatible API, event, and database changes;
- deploy additive schema changes before code that needs them;
- canary a small percentage of traffic;
- compare technical and business metrics;
- stop or roll back automatically on a violated threshold;
- use feature flags for risky behavior, with ownership and expiry;
- preserve old consumers during an event-schema transition;
- avoid a coordinated all-services release.

A rollback is only safe when the old version can understand the current schema and messages.

## Step 12: prove recovery with controlled failure

Run tests at several levels:

- unit tests for retry, timeout, state-transition, and idempotency policies;
- integration tests with real databases and brokers;
- load tests at normal load, expected peak, and dependency slowdown;
- fault injection for latency, dropped responses, process death, and duplicate messages;
- availability-zone and database failover exercises;
- backup restore and point-in-time recovery tests;
- game days using runbooks, alerts, and incident roles.

Measure whether the critical journey met its SLO and whether RTO/RPO were achieved. A failover mechanism that has never been exercised is an architectural claim, not evidence.

## Concrete failure walkthroughs

### Failure A: one application instance crashes

1. The load balancer stops routing new traffic after readiness fails.
2. Requests already completed remain durable outside the instance.
3. Idempotent clients retry safe operations against another instance.
4. Capacity remains sufficient because replicas span zones and baseline capacity assumed one failure.
5. Orchestration recreates the instance; alerts fire only if user impact or redundancy risk crosses a threshold.

### Failure B: Payment Provider becomes slow

1. Payment calls reach a short timeout within the checkout deadline.
2. The payment bulkhead caps in-flight calls.
3. The circuit opens after sufficient evidence and fails new attempts fast.
4. The order remains `PAYMENT_PENDING`; it is not falsely marked failed.
5. Status lookup and retry reuse the original idempotency key.
6. Reconciliation resolves long-running unknown outcomes or sends them to review.
7. Browse and unrelated services retain their capacity.

### Failure C: a traffic spike exceeds capacity

1. CDN and cache absorb eligible reads.
2. Per-client and global admission limits reject excess traffic early.
3. Non-critical recommendations and background work shed load first.
4. Small bounded queues absorb a short burst.
5. Autoscaling adds stateless capacity, but concurrency limits continue protecting the database.
6. Operators monitor SLO burn, saturation, and recovery rather than only CPU.

### Failure D: broker is unavailable

1. Service-local business transactions and their outbox records can still commit within an approved storage limit.
2. The relay retries with backoff; unpublished-outbox age triggers an alert.
3. Consumers do not receive new work, so the UI shows explicit pending state where necessary.
4. On recovery, the relay publishes the backlog at a controlled rate.
5. Consumers deduplicate messages and do not overwhelm their databases during catch-up.

## Multi-region is a business decision, not a checkbox

A multi-region design adds protection from regional failure, but also routing, data consistency, cost, and operational complexity.

First choose the **traffic topology**:

- In **active/passive**, one region normally serves production traffic and another region is prepared to take over. The passive region's readiness determines recovery speed and cost.
- In **active/active**, two or more regions serve production traffic at the same time. This can reduce failover time and user latency, but requires global routing plus explicit write ownership, consistency, and conflict handling.

For an active/passive topology, choose a **readiness level** that satisfies the RTO/RPO:

| Active/passive readiness | What remains ready in the passive region | Recovery and cost |
|---|---|---|
| Pilot light | Replicated critical data and only the minimum core services | More components must start and scale during failover; lower steady cost |
| Warm standby | A complete working stack at reduced capacity | Scale up and redirect traffic; moderate recovery time and cost |
| Hot standby | A full or near-full-capacity stack kept synchronized and continuously tested | Fastest active/passive failover; highest steady cost |

Backup and restore is a disaster-recovery baseline rather than a live traffic topology: infrastructure and data are rebuilt from backups after failure, so it generally has the longest RTO. It can support an active/passive plan, but there may be no continuously ready passive application stack.

Asynchronous cross-region replication may lose the most recent writes during sudden failover, so its lag must fit the RPO. Synchronous cross-region writes reduce that window but increase latency and can reduce availability during a partition. Test DNS/traffic failover, credentials, quotas, data promotion, and failback—not just infrastructure creation.

## Project mapping: current state and sensible next steps

The current repository is a learning-scale modular monolith, so resilience should be added according to real failure needs rather than splitting it prematurely.

- [`TokenService`](../../../../../src/main/java/com/instagram/backend/service/TokenService.java) validates signed tokens without storing per-request server session state. Multiple instances can validate tokens if they receive the same protected signing configuration.
- [`PostService.create`](../../../../../src/main/java/com/instagram/backend/service/PostService.java) correctly benefits from one local Spring transaction while post data remains in one service boundary.
- [`MediaStorageService`](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) stores uploads on local disk. With multiple instances, one instance may not see another instance's files, and instance loss can lose local data. Shared durable object storage is needed before claiming resilient horizontal scaling.
- [`PostService.feed`](../../../../../src/main/java/com/instagram/backend/service/PostService.java) loads all posts and performs per-post enrichment. Before large scale, add bounded pagination and remove repeated dependency work; replicas alone do not fix an unbounded request.
- [`application.properties`](../../../../../src/main/resources/application.properties) points to one PostgreSQL endpoint and currently has no documented health, metrics, failover, or backup behavior. Those are future exercises, not existing guarantees.

A practical evolution order is:

1. add health endpoints, metrics, structured correlation IDs, and latency/error/saturation dashboards;
2. add pagination and resource bounds;
3. run multiple stateless application instances and move media to durable shared object storage;
4. add tested database backup/restore and high-availability configuration;
5. introduce a queue only for a real asynchronous need such as media processing;
6. add outbox, idempotent consumers, and reconciliation with that workflow;
7. inject failures and record whether the defined journey targets are met.

## Decision and trade-off table

| Decision | Protects against | Cost or limitation |
|---|---|---|
| Multi-zone replicas | Instance or zone loss | More infrastructure; shared dependencies remain |
| Timeout | Indefinite wait | May produce an ambiguous result |
| Idempotent bounded retry | Transient failure | Extra load and latency; not all operations are safe |
| Circuit breaker | Repeated calls to unhealthy dependency | Tuning and false opens; no data repair |
| Bulkhead | Resource-exhaustion cascade | Reserved idle capacity and more pools |
| Cache/fallback | Read dependency outage or latency | Staleness and invalidation rules |
| Queue | Short producer/consumer mismatch | Backlog, ordering, duplicate delivery |
| Saga + outbox/inbox | Cross-service partial failure | Intermediate states and compensation complexity |
| Backup + restore | Corruption, deletion, disaster | Recovery time and ongoing verification |
| Multi-region | Regional failure | Large consistency, cost, and operating burden |

## Failure drill

**Scenario:** a new release causes Inventory Service latency to rise from 50 ms to 8 seconds. Checkout retries three times at the gateway and three times in Order Service. All services share a large connection pool to one database cluster.

1. **Predict:** one customer request can create up to nine inventory attempts. Threads and connections accumulate, database latency rises, timeouts spread, and healthy services begin failing.
2. **Observe:** trace data shows nested retries; inventory p99, active requests, connection-pool saturation, and checkout error rate rise together.
3. **Contain:** stop the rollout or route back, disable duplicate retry layers, open the circuit, shed non-critical work, and preserve capacity for status/recovery calls.
4. **Recover:** allow controlled probes, drain only unexpired work, reconcile orders and reservations with ambiguous outcomes, and restore traffic gradually.
5. **Prevent:** one retry layer with a budget, shorter downstream timeout, inventory bulkhead, independent connection budgets where justified, canary thresholds, capacity tests under 8-second latency, and a runbook for uncertain orders.

## Interview-ready answer

I would start with critical user journeys and measurable SLOs, latency budgets, RTO, and RPO. Then I would map failure domains—process, node, availability zone, database, broker, external provider, credential, and region—because “three replicas” helps only when those replicas do not share the failed dependency. Stateless services would run across zones behind health-aware load balancing, with graceful shutdown and enough remaining capacity after one planned failure. Stateful components need controlled failover, replication matched to the consistency requirement, and tested backups because replication does not protect against a replicated bad delete.

Every remote call gets an end-to-end deadline, a shorter attempt timeout, bounded concurrency, and an explicit outcome policy. I retry only transient, idempotent work at one layer, with backoff, jitter, and a retry budget. Bulkheads keep a slow dependency from taking every thread or connection; circuit breakers stop repeated calls; rate limits, backpressure, bounded queues, and load shedding preserve critical work. Fallbacks must be honest: cached recommendations may be acceptable, but unknown payment cannot be reported as declined.

For example, if Payment Provider authorizes an order but its response is lost, checkout keeps the order `PAYMENT_PENDING`, reuses the same idempotency key for status or retry, and prevents fulfilment. The payment bulkhead contains waiting calls, the circuit opens after evidence of failure, and reconciliation later confirms, compensates, or sends the case to review. Browsing remains available because it does not share that capacity.

Cross-service state changes use local transactions, Saga, outbox/inbox, idempotent consumers, compensation, and reconciliation. Metrics, structured logs, traces, queue age, saturation, and business-invariant alerts make failures diagnosable. Backward-compatible canaries make releases reversible, and fault injection, failover, duplicate-message, and restore drills prove recovery.

The trade-off is cost and complexity: redundancy, reserved capacity, and stronger consistency reduce efficiency or increase latency. I would add multi-region only when RTO/RPO justify it, explicitly choosing active/passive readiness or active/active traffic with its conflict-management burden.

## Questions and explained answers

<details>
<summary>1. Does running three instances make a service fault tolerant?</summary>

Only against faults that the instances do not share. Three instances can survive one process crash if traffic is rerouted and remaining capacity is sufficient. They do not protect against one failed zone containing all three, one unavailable database, one bad deployment, one expired shared credential, or corrupted data. State the assumed fault and inspect the complete request path.

</details>

<details>
<summary>2. Why can retries make an outage worse?</summary>

Retries multiply load while a dependency is already slow. If both gateway and service retry three times, one request can create nine downstream attempts. Synchronized retries also arrive in waves. Use one chosen retry layer, safe/idempotent operations, exponential backoff with jitter, a remaining deadline, and a global retry budget. Overload responses often need load shedding, not immediate retries.

</details>

<details>
<summary>3. What is the difference between replication and backup?</summary>

Replication keeps another copy current so service can continue or fail over after hardware loss. It can also copy an accidental delete or corrupt write immediately. A backup preserves recoverable historical state and must be restored to prove it works. A resilient data design commonly needs both, with RTO/RPO determining their topology and frequency.

</details>

<details>
<summary>4. How do you choose a graceful fallback?</summary>

Start from business correctness. A stale recommendation or temporarily missing like count may be acceptable within a defined window. Stale authorization, invented inventory, or a guessed payment decline may be unsafe. The fallback should be explicit, observable, product-approved, and tested. When no honest fallback exists, fail closed or return a pending/unknown state.

</details>

<details>
<summary>5. How would you prove this architecture is resilient?</summary>

Define the expected behavior first, then inject the named fault under realistic load. Kill an instance, delay a dependency, duplicate an event, exhaust a bulkhead, fail over the database, and restore from backup. Verify customer SLOs, correctness invariants, RTO/RPO, alert quality, containment, and recovery. Diagrams and configured replicas are not proof until the failure and recovery paths have been exercised.

</details>
