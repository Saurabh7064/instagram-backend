# System Design Interview Learning Plan

This plan turns the November–December 2024 questions into a structured path for Senior Java/Backend interviews. It complements the project-driven [System Design Mastery Roadmap](../../system-design/mastery-roadmap.md); this folder focuses on explaining decisions aloud in a 45–60 minute interview.

## Target outcome

You are interview-ready when you can:

1. turn an ambiguous prompt into explicit requirements;
2. choose components because of concrete scale, reliability, security, or consistency needs;
3. trace one request end to end;
4. predict failures and explain recovery;
5. compare at least one alternative without claiming one universal “best” design;
6. complete three 45–60 minute mocks without losing the requirements-to-trade-offs structure.

## Your learning format

Every micro-lesson follows the same sequence:

1. **Why:** What real problem forces this concept to exist?
2. **Analogy:** Build one accurate mental picture, then state where the analogy stops working.
3. **Plain-language model:** Define no more than three new terms.
4. **Concrete flow:** Trace a request, event, or failure step by step.
5. **Instagram-backend mapping:** Connect the idea to this repository.
6. **Decision:** State when to use it, when not to use it, and the trade-off.
7. **Failure prediction:** Predict what breaks before revealing the answer.
8. **Checkpoint questions:** Answer 3–5 questions aloud before opening the explanations.
9. **Teach-back:** Explain the concept in 60–90 seconds without notes.
10. **Spaced review:** Revisit after 1 day, 1 week, and 1 month.

Reading a lesson or drawing a diagram does not mean it is understood. Progress is tracked separately in [Learner Progress](./learner-progress.md).

## The interview answer loop

Use this loop for every design prompt:

```text
Clarify → Estimate → Contract/data → High-level flow
        → Deep dive → Failures/security → Trade-offs → Evolution
```

### 1. Clarify

Ask who uses the system, the most important actions, what is explicitly out of scope, and which quality matters most: latency, availability, correctness, privacy, or cost.

### 2. Estimate only what changes a decision

Estimate requests per second, stored data, object size, read/write ratio, and traffic spikes. Do not calculate numbers merely to perform arithmetic.

### 3. Define contracts and data

Name the important APIs or events, identifiers, ownership boundaries, and access patterns before selecting databases.

### 4. Draw the smallest complete flow

Start with client, entry point, service, and storage. Add a cache, queue, search engine, replica, or shard only after naming the problem it solves.

### 5. Deep-dive into two risks

Examples: payment correctness, inventory races, cache invalidation, hot partitions, privacy, or cross-region failover.

### 6. Close with failures and trade-offs

Explain timeouts, retries, idempotency, degradation, observability, recovery, and what you deliberately did not optimize yet.

## Eight-week sequence

Study one micro-lesson at a time. Later rows remain `TODO` until the current stop/go check is completed.

| Week | Module | Questions from the 2024 notes | Deliverable |
|---:|---|---|---|
| 1 | Interview frame and quality attributes | “Scalable architecture,” high availability, high throughput | One 15-minute Instagram read-path design |
| 2 | Data distribution and storage | Cassandra partitions/nodes, SQL vs NoSQL keys, hot keys | Partition-key worksheet plus failure diagnosis |
| 3 | Caching and product search | Caching strategies/consistency, “how do you search a product?” | Cache decision table and catalog search flow |
| 4 | Async workflows and correctness | Saga, checkout/payment availability | Order workflow with idempotency and compensation |
| 5 | Monolith-to-microservices migration | Decomposition, risk, coexistence, backward compatibility | Strangler migration plan for this backend |
| 6 | Privacy and multi-region reliability | Healthcare sharing, anonymization, geographic failure | Data classification and regional failover plan |
| 7 | Full e-commerce design | Dynamically scaling high-throughput e-commerce | 45-minute recorded design and review |
| 8 | Mixed mocks and repair | Three broad design prompts | Three mocks plus targeted re-study |

## Module details

### Module 1 — Foundations

1. [Scalability, Availability, and Reliability](./concepts/01-scalability-availability-reliability.md) — `READY`
2. [Load Balancing and Stateless Services](./concepts/05-load-balancing-and-stateless-services.md) — `READY`
3. [Latency, Throughput, and Saturation](./concepts/06-latency-throughput-and-saturation.md) — `READY`
4. [Tail Latency, Percentiles, and Queueing](./concepts/07-tail-latency-percentiles-and-queueing.md) — `READY`
5. [Backpressure and Load Shedding](./concepts/08-backpressure-and-load-shedding.md) — `READY`
6. Consistency and durability — `TODO`

Stop/go: explain why “highly scalable” and “highly available” are different claims, then identify which one a proposed change improves.

Focused diagnosis practice: [Production Troubleshooting Scenarios](./questions/production-scenarios/README.md). Use the [revision cards](./revision-cards/README.md) for retrieval only after attempting the detailed answer.

### Module 2 — Data placement

1. [Partitioning, Replication, and Hot Keys](./concepts/03-partitioning-replication-hot-keys.md) — `READY`
2. SQL versus NoSQL from access patterns — `TODO`
3. Cassandra partition and clustering keys — `TODO`
4. Read replicas, quorum, and replication lag — `TODO`

Stop/go: choose a key for a concrete query, predict which node receives it, and diagnose one hot-partition design.

### Module 3 — Fast reads and search

1. [Caching and Consistency](./concepts/02-caching-consistency.md) — `READY`
2. Cache-aside, write-through, and invalidation — `TODO`
3. Stampede, penetration, and eviction — `TODO`
4. Product search, inverted indexes, and indexing delay — `TODO`

Stop/go: trace a cache hit, miss, update, and failure; then explain why product search should not be implemented as repeated SQL wildcard scans at scale.

### Module 4 — Stateful workflows

1. Idempotency for checkout and payment — `TODO`
2. Saga orchestration versus choreography — `TODO`
3. Compensation and irreversibility — `TODO`
4. Queues, retries, dead letters, and reconciliation — `TODO`

Stop/go: explain how a retried checkout avoids duplicate payment and how the system recovers when inventory reservation succeeds but payment fails.

### Module 5 — Architecture evolution

1. [Monolith-to-Microservices Migration](./concepts/04-monolith-to-microservices-migration.md) — `READY`
2. Service boundaries and data ownership — `TODO`
3. Backward compatibility and contract testing — `TODO`
4. Observability, canaries, rollback, and cutover — `TODO`

Interview-answer practice: [Microservices Interview Questions](./questions/microservices/README.md).

Stop/go: propose the first service to extract, justify the boundary, describe coexistence, and give a rollback path.

### Module 6 — Privacy and global operation

1. Data classification, consent, and least privilege — `TODO`
2. Tokenization, pseudonymization, anonymization — `TODO`
3. Active-passive versus active-active regions — `TODO`
4. RTO, RPO, replication lag, and conflict resolution — `TODO`

Stop/go: separate billing identity from analytics data and explain what happens when an entire region becomes unreachable.

## Three anchor designs

These three prompts cover most of the supplied scenarios without memorizing dozens of unrelated diagrams.

### 1. Design a scalable e-commerce platform

Covers catalog, search, cart, inventory, checkout, payment, cache, autoscaling, distributed workflows, idempotency, events, and hot products.

### 2. Design Instagram/news feed

Covers high-read fan-out, media storage/CDN, caching, pagination, celebrity hot keys, eventual consistency, ranking, and abuse controls. It maps directly to this repository.

### 3. Design a healthcare data-sharing platform

Covers privacy classification, consent, encryption, audit, de-identification, tenant boundaries, data residency, regional failure, and secure analytics.

Use “migrate a legacy monolith” as an evolution follow-up to any of these designs.

## Weekly rhythm

### Session A — Learn one concept, 45 minutes

- Predict the problem before reading.
- Read one micro-lesson only.
- Draw one flow from memory.
- Answer checkpoints aloud.

### Session B — Apply it, 45 minutes

- Add the concept to one anchor design.
- State the benefit, cost, new failure mode, and alternative.
- Record a five-minute explanation.

### Session C — Retrieve and interview, 60 minutes

- Spend 10 minutes reviewing without notes.
- Run a 35–45 minute design.
- Spend 10 minutes recording misses and the next drill.

## Evidence and scoring

Score each mock from `0` to `2` on each dimension:

| Dimension | 0 | 1 | 2 |
|---|---|---|---|
| Requirements | Assumed | Some clarification | Prioritized and bounded |
| Scale | Missing | Numbers without decisions | Estimates drive decisions |
| Data/contracts | Vague | Partial | Clear ownership and access paths |
| Architecture | Components only | Main flow works | Flow and boundaries are justified |
| Reliability | Ignored | Names patterns | Traces failure and recovery |
| Trade-offs | “Best practice” claims | One alternative | Explicit benefit/cost/evolution |
| Communication | Unstructured | Mostly followable | Leads a coherent discussion |

Target: at least `11/14` three times, including no zero in reliability or trade-offs.

## What not to mix into this track

The source notes also contain Java syntax, Spring MVC, coding, low-level design, and behavioral questions. They are retained and routed in the [November–December 2024 Question Bank](./question-bank-nov-dec-2024.md), but they do not become system-design lessons.
