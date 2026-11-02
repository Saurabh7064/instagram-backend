# Design a fault-tolerant and resilient microservices architecture

> **Question:** Designing a fault-tolerant and resilient microservices architecture involves building ways for services to fail gracefully and recover quickly. How would you design a fault-tolerant and resilient microservices architecture?

> [One-page revision card](../../revision-cards/microservices/06-fault-tolerant-resilient-architecture.md)

## Which part of the product must keep working when payment slows?

Imagine a shop where browsing, checkout, recommendations, and payment all work normally. Then the payment provider stops replying promptly. Checkout waits, customers retry, and shared workers fill up. Soon browsing is slow too, even though reading the catalog never needed a payment result.

A useful resilient design starts by explaining that sequence and deciding where to stop it. Which operations should continue? Which must return a clear pending or unavailable result? How will we prevent a retry from charging twice, and how will an operator resolve the uncertain orders?

Those questions connect the architecture to an observable result. A collection of replicas, queues, and circuit breakers is useful only when we can show which failure each one contains and what the user sees while it does.

## A hospital chooses what the backup must keep alive

A hospital does not rely on one perfect power source. It identifies critical loads, provides backup generators, separates electrical circuits, tests failover, and accepts that non-critical areas may temporarily lose power so operating rooms can continue.

In a microservice system:

- critical loads are business journeys such as checkout and payment status;
- separate circuits are bulkheads and independent failure domains;
- backup generators are redundant instances, replicas, and alternate paths;
- load priority is graceful degradation and load shedding;
- generator drills are game days and recovery tests.

The analogy stops at data consistency. Starting a backup generator does not duplicate a payment, deliver messages out of order, or create split-brain database writers. Software also needs idempotency, ownership, consistency, and reconciliation.

## State the failure we are promising to survive

One application instance crashing is a **fault**, an abnormal condition inside the system. If another instance serves the request successfully, the user may see no failure. A **failure** is the visible inability to provide the promised behavior. This distinction matters: we cannot prevent every fault, but we can keep selected faults from becoming user failures.

**Fault tolerance** means continuing the required service while a specified fault occurs. **Resilience** also includes absorbing disruption, degrading safely, and recovering. Neither is an unlimited promise. “Survives one application-instance crash at peak traffic” is testable; “never goes down” is not.

A **failure domain** is a group that may fail together. Two instances on one machine share the machine's failure domain. Instances in different places may still share one database, credential, or bad release. Follow those shared dependencies before counting replicas as independent protection.

## What are we trying to preserve?

For the shop example, payment correctness matters more than recommendations. If recommendations disappear, the customer can still buy. If an uncertain payment is reported as a decline and retried as a new charge, the system can hurt the customer while appearing available.

| Journey | Acceptable reduced behavior | What must remain true |
|---|---|---|
| Browse catalog | Serve approved slightly stale data | Do not present stale checkout facts as guaranteed current |
| Recommendations | Omit the section or use cached popular items | Do not take checkout capacity |
| Place order | Accept once, reject clearly, or show pending | Do not duplicate the order |
| Authorize payment | Show success, decline, or explicit unknown/pending | Do not double-charge |
| Confirmation email | Delay delivery | Keep the valid order and durable delivery intent |

Turn those promises into measurements. A **service-level objective (SLO)** is a target for an explicitly measured outcome, such as 99.95% successful checkout requests over 30 days. If this were a time-based availability objective with every minute eligible, the allowed downtime would be `30 × 24 × 60 × 0.0005 = 21.6 minutes`. A request-based objective instead allows 0.05% of eligible requests to fail; it does not translate directly into minutes under uneven traffic.

Recovery needs two separate targets. The **recovery time objective (RTO)** is how long restoration may take. The **recovery point objective (RPO)** is the maximum targeted data-loss window. Restoring quickly from yesterday's backup might meet neither the order-loss requirement nor the required recovery point.

Also state expected traffic and burst size, acceptable response time, permitted staleness, data durability, and budget. These assumptions decide whether we need spare local capacity, another region, or simply an honest rejection during overload.

## Trace one request through what can fail together

An **availability zone** is a provider-defined location intended to isolate some infrastructure failures from other zones. Put application instances in different zones only if the rest of the request path can survive losing one too.

```text
User
  |
Regional load balancer
  |-- application instance in zone A --|
  |-- application instance in zone B --|--> required database
  |-- application instance in zone C --|--> payment provider for checkout
```

If zone A fails, the load balancer must stop sending it work, and B plus C must have enough capacity. But if all three instances need the same unavailable database, the three application copies do not save that journey. If every request calls a shared identity provider, that provider also belongs on the failure map.

At the edge, the Domain Name System (DNS) maps service names to destinations. A content delivery network (CDN) can serve eligible cached content near users, and a web application firewall (WAF) can filter some unwanted traffic. Each can be useful; each is another dependency with its own configuration and failure behavior. Include it because the request uses it, rather than adding it as decoration to an architecture diagram.

The complete shop may have separate catalog, order, inventory, notification, and payment capabilities. We will choose their waiting, storage, and recovery policies below. First, identify what they still share: database capacity, message storage, credentials, deployment machinery, and observability. A service box is not automatically a separate failure domain.

## What happens when one instance or storage node disappears?

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

## Leave time to return a truthful answer

Suppose checkout has a two-second user deadline. The gateway reserves time for routing and response handling, leaving 1.8 seconds; checkout reserves another 300 ms and works within 1.5 seconds. A payment attempt allowed 900 ms leaves `1.5 - 0.9 = 0.6 seconds` to persist the observed state and return a response.

These are illustrative budgets. Healthy and tail latency determine the real values, and every attempt plus its waiting time must fit inside the remaining deadline. A fresh two-second timeout at each hop would allow a downstream operation to continue after the user has already given up.

For each call, also decide how much concurrent capacity it may occupy and what happens when the response is absent. For payment, “unknown/pending” may be the only truthful result; for recommendations, omission may be acceptable. Record timeouts, active calls, and the operation identity so the recovery path can find the same work.

Cancel work that is no longer useful where supported, but do not confuse cancellation with undoing a commit. A provider can finish the authorization even if the caller stopped waiting.

## Retry the same intention, within the same budget

A second attempt can hide a temporary connection fault. It cannot repair invalid input or create capacity in an overloaded provider. Retry only when the fault may clear, the operation is safe to repeat, and enough deadline remains.

An **idempotency key** identifies one logical payment attempt. Store it before calling the provider and reuse it when querying or retrying that attempt. If the first authorization succeeded but its response disappeared, using a fresh key can create a second charge.

Choose one layer to retry and cap total attempts. Increase delays between attempts, called exponential backoff, and randomize them with jitter so callers do not all return together. Respect overload retry guidance. A small local retry setting becomes a large multiplication when the gateway and service client each repeat the other's attempts.

## Keep payment’s slowdown inside its capacity allowance

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

## A successful response is not enough if the records disagree

Checkout spans order, reservation, and payment owners. Keep changes within each owner's database in one local transaction. For the cross-owner workflow, persist the steps and valid intermediate states. This is a Saga; it can direct the next step or send a repair action after a definite failure.

When saving a business change must also announce an event, save an **outbox** record in that same database transaction. A publisher can retry later. On the receiving side, an **inbox** or processed-event record lets the consumer recognize duplicates; commit it with the local business effect.

If payment definitely declines, releasing inventory is a **compensation**, a new business action repairing earlier progress. If the result is unknown, do not invent a decline. Keep the order pending and reconcile it against provider and reservation facts. Reconciliation is the comparison and repair process for missed or ambiguous work.

The [distributed-transactions lesson](./04-distributed-transactions-and-data-consistency.md) follows that checkout in detail. Here, the architecture requirement is that each temporary state has a durable owner, permitted next transitions, a deadline, and a safe recovery path. A diagram alone does not enforce any of those rules.

## Tell the truth when full functionality is unavailable

Graceful degradation must remain honest:

| Failure | Safe behavior | Unsafe behavior |
|---|---|---|
| Recommendation service down | Omit it or serve labeled cached results | Fail the entire product page |
| Catalog cache stale | Serve within an approved freshness window | Show stale price at checkout without validation |
| Email provider down | Queue confirmation and retry | Roll back a valid paid order |
| Payment response lost | Keep order pending; query by idempotency key | Report decline and charge again with a new key |
| Authorization system uncertain | Fail closed for protected write | Guess that the user is authorized |

Not every feature deserves a fallback. Security checks and correctness-sensitive writes often need to fail closed. The product owner should approve degraded behavior before an incident.

## Deferred work still needs a deadline and an owner

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

## How would the operator tell whether containment worked?

Start with checkout's outcome and latency. Then check whether payment calls are trapped, retries are multiplying, or pending orders are aging. An alert about user harm should lead to evidence explaining that harm.

**Metrics** summarize the population: request success, latency, active work, rejection, queue age, and unresolved business states. **Structured logs** record individual events with named fields such as operation ID, outcome, version, and safe error context. **Distributed traces** join timed operations for one request so we can see where it waited and which calls repeated.

Keep the same logical operation identity through retries and messages. It lets an operator distinguish three attempts at one payment from three legitimate payments. Avoid credentials, tokens, payment details, and unnecessary personal data in telemetry.

Use the journey's SLO to set urgency, then use resource and dependency signals to diagnose and predict risk. “The circuit opened” is not automatically a new outage: if it protected browsing and pending checkout remains correct, it may be evidence the containment policy worked.

## Could our own release defeat the recovery plan?

A bad release can reach every replica, so redundancy alone does not protect against it. Start a candidate with a small traffic cohort, a canary, and compare user outcomes with the baseline before expanding.

Keep API, event, and database changes compatible while versions coexist. Add the new schema before deploying code that requires it, migrate data in stages, and remove the old form only after its readers are gone. A previous application version is a usable rollback only if it understands the data and messages that now exist.

A feature flag can separate enabling behavior from deploying code, provided the disabled path remains valid. Give the flag an owner and removal date. Prepare stop thresholds for both immediate request failures and delayed consumer failures; one can stay green while the other degrades.

## Which failures have we actually demonstrated?

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
- [`application.properties`](../../../../../src/main/resources/application.properties) points to one PostgreSQL endpoint. It already configures health/readiness, HTTP latency histograms, bounded metric tags, and tracing settings. [RequestCorrelationFilter.java](../../../../../src/main/java/com/instagram/backend/observability/RequestCorrelationFilter.java) supplies structured request completion and correlation. Database failover, restore proof, and centralized monitoring remain future exercises; see the [observability lesson](./02-production-observability-and-debugging.md) for the precise implemented boundary.

A practical evolution order is:

1. build retained latency/error/saturation dashboards on the existing health, metric, and correlation baseline;
2. add pagination and resource bounds;
3. run multiple stateless application instances and move media to durable shared object storage;
4. add tested database backup/restore and high-availability configuration;
5. introduce a queue only for a real asynchronous need such as media processing;
6. add outbox, idempotent consumers, and reconciliation with that workflow;
7. inject failures and record whether the defined journey targets are met.

## Which protection earns its operational cost?

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

**Scenario:** a new release causes Inventory Service latency to rise from 50 ms to 8 seconds. Checkout makes three total attempts at the gateway and three total attempts in Order Service. All services share a large connection pool to one database cluster.

1. **Predict:** one customer request can create up to nine inventory attempts. Threads and connections accumulate, database latency rises, timeouts spread, and healthy services begin failing.
2. **Observe:** trace data shows nested retries; inventory p99, active requests, connection-pool saturation, and checkout error rate rise together.
3. **Contain:** stop the rollout or route back, disable duplicate retry layers, open the circuit, shed non-critical work, and preserve capacity for status/recovery calls.
4. **Recover:** allow controlled probes, drain only unexpired work, reconcile orders and reservations with ambiguous outcomes, and restore traffic gradually.
5. **Prevent:** one retry layer with a budget, shorter downstream timeout, inventory bulkhead, independent connection budgets where justified, canary thresholds, capacity tests under 8-second latency, and a runbook for uncertain orders.

## A 2–3 minute interview answer

I would start with critical user journeys and measurable SLOs, latency budgets, RTO, and RPO. Then I would map failure domains—process, node, availability zone, database, broker, external provider, credential, and region—because “three replicas” helps only when those replicas do not share the failed dependency. Stateless services would run across zones behind health-aware load balancing, with graceful shutdown and enough remaining capacity after one planned failure. Stateful components need controlled failover, replication matched to the consistency requirement, and tested backups because replication does not protect against a replicated bad delete.

Every remote call gets an end-to-end deadline, a shorter attempt timeout, bounded concurrency, and an explicit outcome policy. I retry only transient, idempotent work at one layer, with backoff, jitter, and a retry budget. Bulkheads keep a slow dependency from taking every thread or connection; circuit breakers stop repeated calls; rate limits, backpressure, bounded queues, and load shedding preserve critical work. Fallbacks must be honest: cached recommendations may be acceptable, but unknown payment cannot be reported as declined.

For example, if Payment Provider authorizes an order but its response is lost, checkout keeps the order `PAYMENT_PENDING`, reuses the same idempotency key for status or retry, and prevents fulfilment. The payment bulkhead contains waiting calls, the circuit opens after evidence of failure, and reconciliation later confirms, compensates, or sends the case to review. Browsing remains available because it does not share that capacity.

Cross-service state changes use local transactions, Saga, outbox/inbox, idempotent consumers, compensation, and reconciliation. Metrics, structured logs, traces, queue age, saturation, and business-invariant alerts make failures diagnosable. Backward-compatible canaries make releases reversible, and fault injection, failover, duplicate-message, and restore drills prove recovery.

The trade-off is cost and complexity: redundancy, reserved capacity, and stronger consistency reduce efficiency or increase latency. I would add multi-region only when RTO/RPO justify it, explicitly choosing active/passive readiness or active/active traffic with its conflict-management burden.

Before marking this answer practiced, trace the payment slowdown without notes, state what remains available and why, and predict the unresolved order state. Answer the questions below before opening them. The proposed architecture and drills are not claims of executed recovery tests.

## Questions and explained answers

<details>
<summary>1. Does running three instances make a service fault tolerant?</summary>

Only against faults that the instances do not share. Three instances can survive one process crash if traffic is rerouted and remaining capacity is sufficient. They do not protect against one failed zone containing all three, one unavailable database, one bad deployment, one expired shared credential, or corrupted data. State the assumed fault and inspect the complete request path.

</details>

<details>
<summary>2. Why can retries make an outage worse?</summary>

Retries multiply load while a dependency is already slow. If both gateway and service make three total attempts, one request can create `3 × 3 = 9` downstream attempts. Synchronized retries also arrive in waves. Use one chosen retry layer, safe/idempotent operations, exponential backoff with jitter, a remaining deadline, and a global retry budget. Overload responses often need load shedding, not immediate retries.

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
