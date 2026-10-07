# November–December 2024 System Design Question Bank

This file extracts, normalizes, de-duplicates, explains, and routes the supplied historical questions. It preserves the original month/date context while separating system design from Java, Spring, coding, low-level design, and behavioral preparation.

## Priority legend

- `P0`: must answer confidently in a senior backend interview.
- `P1`: common deep-dive or follow-up.
- `P2`: useful specialization or role-dependent depth.
- `ROUTE`: valuable question, but belongs in another interview track.

For full, standalone answers to the microservices prompts below, use the [Microservices Interview Questions](./questions/microservices/README.md) module. It keeps one primary question per file and ends every answer with explained practice questions.

## November 2024 — extracted system-design questions

| ID | Date | Normalized question | Priority | Primary lesson |
|---|---|---|---|---|
| MIG-01 | Nov 6 | How would you decompose a tightly coupled legacy Java monolith into microservices? | `P0` | [Detailed answer](./questions/microservices/01-decomposing-a-monolith.md) |
| MIG-02 | Nov 6 | How would you minimize risk and preserve functionality during migration? | `P0` | [Detailed answer](./questions/microservices/01-decomposing-a-monolith.md) |
| MIG-03 | Nov 6 | How do you maintain backward compatibility while both architectures coexist? | `P0` | [Contract evolution](./questions/microservices/09-api-event-contract-evolution.md) |
| SCALE-01 | Nov 6 | How would you build dynamically scaling, high-throughput e-commerce services? | `P0` | [Foundations](./concepts/01-scalability-availability-reliability.md) |
| PAY-01 | Nov 6 | How would you make checkout and payment highly available without sacrificing correctness? | `P0` | [Resilient architecture](./questions/microservices/06-fault-tolerant-resilient-architecture.md) |
| CACHE-01 | Nov 6 | Which caching strategy would you choose, and how would you manage consistency? | `P0` | [Caching](./concepts/02-caching-consistency.md) |
| TXN-01 | Nov 6 | Where would you use a Saga, and how would failures be compensated? | `P0` | [Distributed transactions](./questions/microservices/04-distributed-transactions-and-data-consistency.md) |
| PRIV-01 | Nov 6 | How can healthcare services share data efficiently while meeting privacy obligations? | `P1` | Privacy/global operation |
| PRIV-02 | Nov 6 | How can analytics data be anonymized while original identity remains available for billing? | `P1` | Privacy/global operation |
| GEO-01 | Nov 6 | How do you manage latency and network failure across geographic regions? | `P1` | Privacy/global operation |
| GEO-02 | Nov 6 | How do cross-region fault tolerance, availability, and data synchronization work together? | `P1` | Privacy/global operation |
| CAP-01 | Nov 12 | Why can an oversized thread pool cause resource thrashing and context-switch overhead? | `P1` | [Bulkhead isolation](./questions/microservices/05-bulkhead-isolation.md) |
| MIG-04 | Nov 28 | How would you modernize a pre-Java-6 Spring/Struts/JSP monolith with one database? | `P0` | [Detailed answer](./questions/microservices/01-decomposing-a-monolith.md) |

## November answer frameworks

### MIG-01/MIG-02/MIG-03/MIG-04 — Legacy migration

A strong answer should cover this sequence:

1. **Understand before changing:** inventory business journeys, database writes, integrations, batch jobs, and hidden behavior.
2. **Create safety:** characterization tests, observability, deployment repeatability, and rollback.
3. **Choose a boundary:** select one business capability with clear contracts and limited transactional coupling.
4. **Strangle gradually:** place a facade/gateway in front and route a small path to the new service.
5. **Preserve compatibility:** additive API changes, adapters for legacy shapes, versioned events, and contract tests.
6. **Own data:** one writer per entity; use outbox/CDC for new read models rather than permanent shared writes.
7. **Prove and cut over:** shadow reads, canary traffic, reconciliation, gradual ramp, rollback, then deletion of the old path.

Senior signal: explicitly say when a modular monolith is enough. Microservices are justified by different scaling, isolation, deployment, or team-ownership needs—not by fashion.

### SCALE-01 — High-throughput e-commerce

Start with user journeys and traffic shape:

```text
Browse/search → product detail → cart → checkout
                               → inventory reservation
                               → payment authorization
                               → order confirmation
```

A defensible first architecture:

- CDN and cache for static assets and popular catalog reads;
- search index for text, facets, and ranking;
- stateless application instances behind a load balancer;
- authoritative catalog, inventory, order, and payment boundaries;
- queue/event log for asynchronous updates and slow side effects;
- autoscaling based on saturation signals, not CPU alone;
- per-dependency timeouts, bounded concurrency, backpressure, and load shedding.

Deep dives: hot products, inventory correctness, duplicate checkout, search-index delay, cache stampede, and database bottlenecks.

### PAY-01/TXN-01 — Checkout, payment, and Saga

Availability must not mean “charge twice but return quickly.” Include:

- client-generated or server-issued idempotency key;
- durable order state machine;
- inventory reservation with expiration;
- payment-provider idempotency and status lookup;
- Saga steps with explicit compensation;
- transactional outbox for reliable event publication;
- retry only for safe/idempotent operations with backoff and jitter;
- reconciliation jobs for ambiguous timeouts;
- multi-zone deployment and a clear degraded mode.

Example Saga:

```text
Create pending order
  → reserve inventory
  → authorize payment
  → confirm order

Failure after reservation:
  → release reservation
  → mark order failed
```

Some actions are not truly reversible. A captured card payment may require a refund, which is a new business transaction rather than time travel.

### CACHE-01 — Caching and consistency

Name the data and required freshness before naming a strategy.

| Data | Likely strategy | Consistency decision |
|---|---|---|
| Product description | Cache-aside | Invalidate on update plus TTL |
| Search result | Query cache/edge cache | Short TTL; tolerate indexing delay |
| Inventory browse count | Short cache | Checkout revalidates authority |
| Payment/order status | Durable lookup | Cache cannot be the source of truth |

Cover stale reads, invalidation races, stampedes, TTL jitter, negative caching, eviction, cache loss, and observability.

### PRIV-01/PRIV-02 — Healthcare privacy and analytics

Start with data classification and purpose limitation:

- separate identity/contact data from clinical or analytical facts;
- store the re-identification mapping in a tightly controlled token vault;
- use pseudonymous identifiers in analytics pipelines;
- remove or generalize quasi-identifiers that could re-identify a person;
- encrypt in transit and at rest, use least privilege, and audit every sensitive access;
- record consent and enforce purpose/retention policies;
- isolate billing access from analytics access;
- apply regional legal requirements with qualified compliance/legal review.

Important distinction: pseudonymized data can be re-linked through a protected mapping; truly anonymized data should not be reasonably re-identifiable. If billing must recover identity, the analytical record is normally pseudonymized, not absolutely anonymous.

### GEO-01/GEO-02 — Multi-region operation

Clarify business targets first:

- required latency by user region;
- recovery time objective (RTO);
- recovery point objective (RPO);
- whether writes may be stale, rejected, or conflicted during partition;
- data-residency restrictions.

Then compare:

| Model | Benefit | Cost |
|---|---|---|
| Active-passive | Simpler write ownership and conflicts | Slower failover; idle capacity |
| Active-active reads, single write region | Fast reads, simpler writes | Write latency and replication lag |
| Active-active writes | Local writes and regional independence | Conflict resolution and global invariants |

Include health-based routing, replicated durable data, failover testing, split-brain prevention, conflict policy, reconciliation, and observability by region.

### CAP-01 — Resource thrashing and context switching

A thread waiting for I/O can allow another thread to use the CPU, so some concurrency helps. Too many runnable threads create overhead:

1. the scheduler pauses one thread;
2. saves registers and execution state;
3. restores another thread's state;
4. loses useful CPU-cache locality;
5. repeats this instead of completing application work.

An oversized pool can also multiply memory use, database connections, lock contention, queueing, and downstream pressure. Use bounded pools/queues, separate CPU-bound from blocking work, apply backpressure, and tune from measurements rather than a magic thread count.

## December 2024 — extracted system-design questions

| ID | Date | Normalized question | Priority | Primary lesson |
|---|---|---|---|---|
| DATA-01 | Dec 1 | What are Cassandra partition keys, clustering keys, partitions, replicas, and nodes? | `P0` | [Partitioning](./concepts/03-partitioning-replication-hot-keys.md) |
| DATA-02 | Dec 1 | How do you choose partition keys in SQL and NoSQL systems? | `P0` | Partitioning |
| DATA-03 | Dec 1 | How would you model a Cassandra `UserActivity` table? | `P1` | Partitioning |
| DATA-04 | Dec 1 | How does Cassandra route a query to a partition and replica nodes? | `P1` | Partitioning |
| DATA-05 | Dec 1 | Do partitions or nodes store the data? | `P0` | Partitioning |
| DATA-06 | Dec 1 | How can one partition contain multiple rows? | `P0` | Partitioning |
| DATA-07 | Dec 1 | Do one million unique user IDs mean one million partitions? | `P1` | Partitioning |
| DATA-08 | Dec 1 | What causes hot partitions, and how are they repaired? | `P0` | Partitioning |
| DATA-09 | Dec 1 | Can you visualize partition keys, logical partitions, and storage nodes? | `P1` | Partitioning |
| ECOM-01 | Dec 3 | Design a scalable e-commerce platform in detail. | `P0` | Anchor design |
| SEARCH-01 | Dec 3 | How would product search work? | `P0` | Search/indexing |
| META-01 | Nov 7 / Dec notes | Which three system-design prompts cover the broadest interview scenarios? | `P1` | [Learning plan](./learning-plan.md) |

## December answer frameworks

### DATA-01 through DATA-08 — Cassandra and partitioning

Use this model:

```sql
PRIMARY KEY ((user_id, activity_day), occurred_at)
```

- `(user_id, activity_day)` routes to one logical partition.
- `occurred_at` distinguishes and orders rows inside it.
- hashing the complete partition key selects a token range.
- nodes own token ranges and store many partitions plus replica copies.
- one million distinct keys can create roughly one million logical partitions distributed over far fewer nodes.

A good key satisfies a required query, bounds worst-case partition size, and distributes request rate—not merely byte count. Add time buckets or controlled write shards for hot keys, acknowledging that sharding adds read fan-out and merge cost.

### SEARCH-01 — Product search

Do not make every search request scan transactional product rows with `%term%` matching at scale.

```text
Catalog DB → change event/outbox → indexer → search cluster
User query → search API → query parser → search cluster → ranked product IDs
                                          ↓
                                hydrate product details/cache
```

Explain:

- inverted index for terms to product documents;
- filters/facets for category, brand, price, and availability;
- analyzers for case, language, stemming, and synonyms;
- ranking signals such as text relevance, popularity, and business constraints;
- asynchronous indexing and acceptable delay;
- cache for repeated safe queries;
- fallback/degradation if search is unavailable.

Transactional catalog storage remains authoritative; the search index is a query-optimized projection.

### META-01 — Three broad interview prompts

1. **E-commerce:** caching, search, inventory, payments, Saga, hot products, and autoscaling.
2. **Instagram/news feed:** fan-out, media/CDN, feed ranking, pagination, celebrity keys, and eventual consistency.
3. **Healthcare data sharing:** privacy, audit, anonymized analytics, regional operation, and high availability.

No three prompts literally cover every scenario. These provide broad transfer; use focused drills for chat ordering, rate limiting, and schedulers when target-company evidence requires them.

## Questions routed to other tracks

| Source question | Correct track | Reason |
|---|---|---|
| SRP and OCP | [Low-level design](../06-low-level-design/README.md) | Object/module design principles |
| Interfaces versus abstract classes | [Java](../04-java/README.md) | Language and OO modeling |
| StringBuffer versus StringBuilder | Java | Language/concurrency API |
| Spring page/controller with two fields | [Spring/Hibernate](../05-spring-hibernate/README.md) | Framework implementation |
| PathVariable versus request/path parameters | Spring/Hibernate | HTTP/Spring MVC API usage |
| Avoiding SQL injection with Spring JDBC | Spring/Hibernate/security | Parameterized query implementation |
| final versus finally versus finalize | Java | Language/runtime knowledge |
| JPA versus JDBC | Spring/Hibernate | Persistence abstraction trade-off |
| Java design patterns | Low-level design/Java | Code-level design |
| OCR tools | Role-specific/product research | Not enough context for system design |
| Giant duck versus 100 dwarf horses | Behavioral/culture | Conversation prompt, not architecture |
| Streams: `groupingBy`, `counting`, `mapToInt`, `max`, `partitioningBy`, downstream collectors, `collectingAndThen` | Java | Library practice |
| Explain `partitioningBy(e -> e.getAge() > 30)` and print both groups | Java | Library practice |
| Explain `groupingBy(Employee::getGender, maxBy(...salary...))` | Java | Library practice |
| What is a downstream collector? | Java | Library concept |
| How does `collectingAndThen` find the highest-salary employee by department? | Java | Library practice |
| Find the largest island area in a 2D grid | [Coding](../01-coding/README.md) | Graph/grid traversal |
| Write the largest-island code | Coding | Implementation exercise |
| Compare BFS and DFS time/space for largest island | Coding | Algorithm analysis |
| Return the largest connected-component sum for arbitrary numbers | Coding | Graph/grid variation |
| Give an example of a critical decision under pressure | [Behavioral](../03-behavioral/README.md) | Experience evidence |
| How do you handle tough feedback? | Behavioral | Communication and growth |
| How do you manage disagreement or conflict? | Behavioral | Collaboration evidence |
| How do you prioritize work across three projects? | Behavioral | Prioritization and scope |
| Explain a complex technical issue to a nontechnical person | Behavioral | Audience-aware communication |
| Describe a failed project and what you learned | Behavioral | Ownership and learning |
| Produce a de-duplicated master list of system-design concepts | This learning plan and the broader [mastery roadmap](../../system-design/mastery-roadmap.md) | Curriculum artifact rather than an interview question |

Routing prevents random topic switching while preserving every question for later study.

## How to practice this bank

For each `P0` question:

1. answer aloud for two minutes without notes;
2. draw one flow;
3. name one failure and recovery;
4. compare one alternative;
5. add the miss to [Learner Progress](./learner-progress.md);
6. retry after one day, one week, and one month.
