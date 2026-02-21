# System Design Mastery Roadmap (Project-Driven)

Use this as the main roadmap while building this app step by step.

## How to use this roadmap

1. Pick one item from the highest-priority unfinished tier.
2. Implement one related feature in this project.
3. Add test evidence and demo proof.
4. Update:
   - [Feature Learning Backlog](../features/learning-backlog.md)
   - [System Design Learning Backlog](./learning-backlog.md)
   - [Commit Map](../commit-map.md)

---

## Tier 1 — MUST MASTER (Non-Negotiable)

### Distributed System Fundamentals
- CAP Theorem
- Consistent Hashing
- Replication (Leader-Follower, Multi-Leader)
- Sharding / Partitioning
- Caching Strategies
- Load Balancing (L4 vs L7)
- Horizontal Scaling
- Eventual Consistency
- Quorum
- Rate Limiting

### Databases
- SQL vs NoSQL tradeoffs
- Indexing
- Transactions & Isolation Levels
- Read Replicas
- ACID vs BASE

### Messaging / Async
- Message Queues
- Pub/Sub
- Kafka basics
- Exactly-once vs At-least-once

### Project-first exercises (Tier 1)
- Implement JWT auth + refresh token + token revocation strategy.
- Add rate limiting to login/register APIs.
- Add read replica discussion + query/index strategy note for user/feed endpoints.
- Add async event for "new post" and "new follower" using queue abstraction.

---

## Tier 2 — Very Important (Frequently Tested)

### Distributed Architecture
- Monolith vs Microservices
- API Gateway
- Service Discovery
- Circuit Breaker
- Retry + Exponential Backoff
- Idempotency
- Leader Election
- Consensus (Raft basics)

### Reliability
- Observability (Logs, Metrics, Tracing)
- Health Checks
- Failover strategies
- Backpressure

### Performance
- CDN
- Batching
- Pagination
- Autoscaling

### Project-first exercises (Tier 2)
- Add idempotency key support for write APIs.
- Add retries/backoff policy for external integrations.
- Add health/readiness endpoints + metrics/tracing basics.
- Implement cursor pagination for feed API.

---

## Tier 3 — Good to Know (Senior Depth)

- Two Phase Commit
- Saga Pattern
- CQRS
- Event Sourcing
- Service Mesh
- Gossip Protocol
- PACELC Theorem
- Bulkhead Pattern
- Data Lake vs Warehouse

### Project-first exercises (Tier 3)
- Design post publish as Saga (media processing + metadata updates).
- Draft CQRS split for feed reads vs write path.
- Add anti-corruption boundary for future recommendation service.

---

## Tier 4 — Advanced Scenarios (Staff-Level Direction)

### 1) Data & Storage Architecture
- Multi-region active-active replication
- Cross-region failover strategy
- Geo-partitioning
- Hot partition mitigation
- Adaptive sharding
- Online re-sharding
- Data rebalancing
- Write amplification
- LSM Trees
- B-Trees vs LSM tradeoffs
- Vector clocks
- CRDTs
- Conflict resolution strategies
- Distributed transactions
- Global secondary indexes

Scenarios:
- Design Instagram with multi-region writes
- Avoid hot shard in celebrity accounts
- Zero-downtime database migration

### 2) Consistency & Coordination
- Linearizability
- Sequential consistency
- Causal consistency
- Strong vs Eventual consistency
- Consensus internals (Raft deep dive)
- Log replication
- Split brain problem
- Clock synchronization (NTP, TrueTime)
- Lamport timestamps
- Distributed locking (ZooKeeper style)
- Fencing tokens

Scenarios:
- Prevent double payment in distributed system
- Design globally consistent inventory system
- Handle split-brain in multi-leader setup

### 3) High-Scale Caching
- Cache stampede
- Cache penetration
- Cache avalanche
- Read-through vs Write-through vs Write-behind
- Multi-layer caching (Edge + App + DB)
- Cache warming
- Cache invalidation at scale
- Distributed eviction policies

Scenarios:
- Flash sale system (millions of users in seconds)
- Prevent thundering herd problem

### 4) High Availability & Resilience
- Chaos engineering
- Self-healing systems
- Graceful degradation
- Circuit breaker tuning
- Brownout strategy
- Active-passive vs Active-active
- Rolling deployments
- Canary release
- Blue-green deployment
- Disaster recovery (RTO/RPO)

Scenarios:
- Entire AWS region failure
- Deploy without downtime
- Survive 10x traffic spike

### 5) Streaming & Real-Time Systems
- Event streaming architecture
- Log-based architecture
- Exactly-once processing
- Stream joins
- Windowing
- Backpressure in streams
- Event ordering guarantees
- Time-series databases
- Out-of-order event handling

Scenarios:
- Real-time activity feed
- Fraud detection system
- Live chat with ordering guarantees

### 6) Search & Indexing Systems
- Inverted index
- Full-text search
- Distributed search clusters
- Search ranking algorithms
- Autocomplete system design
- Search consistency tradeoffs
- Near real-time indexing

Scenarios:
- Design YouTube search
- Real-time trending search system

### 7) Graph & Recommendation Systems
- Graph databases
- Distributed graph traversal
- Feed ranking systems
- Collaborative filtering
- Content-based recommendation
- Real-time recommendation
- Cold start problem

Scenarios:
- Instagram feed ranking
- Friend suggestion system
- Location-based recommendations

### 8) Security at Scale
- Zero-trust architecture
- Rate limiting strategies
- Bot mitigation
- Abuse detection
- API gateway hardening
- Distributed DDoS protection
- Multi-factor authentication flow
- Token revocation at scale

Scenarios:
- Protect login system under attack
- Secure high-value payment APIs

### 9) Observability & Operability
- Distributed tracing internals
- Sampling strategies
- High-cardinality metrics
- Log aggregation at scale
- Alert fatigue mitigation
- SLO-based error budgeting

Scenarios:
- Debug latency spike across 50 microservices
- Root cause analysis in distributed system

### 10) Advanced Architecture Patterns
- CQRS deep dive
- Event sourcing tradeoffs
- Saga orchestration vs choreography
- API composition
- Backend for frontend
- Data mesh
- Domain-driven design (DDD)
- Anti-corruption layer

Scenarios:
- Design large e-commerce system
- Multi-team scalable architecture
- Enterprise SaaS platform

---

## Ultra-Advanced (Staff+)

- Global scale system (100M+ DAU)
- Multi-tenant isolation
- Cost optimization architecture
- Capacity planning
- Edge computing
- Serverless scaling patterns
- Hybrid cloud design
- Traffic shadowing
- Distributed ML inference systems
- Feature flag infrastructure

---

## Suggested order for THIS app (next steps)

1. Token lifecycle (access + refresh) with protected endpoint (`/api/me`)
2. Idempotency + retries/backoff for write APIs
3. Feed pagination + caching strategy
4. Observability baseline (logs/metrics/tracing)
5. Queue-driven activity feed prototype
