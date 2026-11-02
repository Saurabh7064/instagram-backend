# Micro-Lesson 01 — Scalability, Availability, and Reliability

Core study: 15 minutes. Prerequisite: none.

## Three backend instances still share one database — Must understand

The feed works on one Spring Boot instance. Traffic grows, so we place three instances, A/B/C, behind one public address. Requests can now be spread across them. If B stops, perhaps A and C can continue. Have we solved “scalability,” “availability,” and “reliability” all at once?

Not yet. Every feed request still needs the same PostgreSQL database. If that database is unavailable, adding another healthy backend does not give it the missing posts. And if retrying a successful publish creates a duplicate, all three backends may be reachable while the user's intended outcome is wrong.

Those are separate questions about the same request path. Naming them helps us choose a change that addresses the actual problem.

## More work, continued service, and correct outcomes

**Scalability** is the ability to handle more load by adding or enlarging resources without unacceptable performance loss. Adding backends can provide more application capacity when their work can run independently and the database has headroom. It is not proof that the entire path can handle more requests.

**Availability** asks whether users can obtain the required service when they need it, measured over a defined period and success criteria. A redundant backend can keep the feed serving through one instance failure. It cannot compensate for every instance depending on an unavailable database. A process responding to a basic probe is weaker evidence than a user completing a feed request.

**Reliability** concerns correct, durable behavior over time. A successful response should correspond to the intended saved result. Transactions, safe retries, and durable storage address different parts of that requirement. Extra serving instances alone do not prevent a duplicated or lost effect.

Think of a grocery store. Extra checkout lanes address capacity; keeping a lane open through a breakdown addresses continued service; recording purchases and charging correctly addresses outcomes. The analogy separates the goals, but software adds a complication: partial communication failures can leave the caller uncertain whether a purchase-like operation completed. A human cashier's judgment is not a recovery protocol.

## Predict what the extra instances actually bought us

Before following the path below, answer two questions: if B alone stops, which requests can still finish? If PostgreSQL stops, do A and C provide an alternative source for the feed?

The first failure can be routed around if the remaining instances have enough capacity and no required state is stranded on B. The second breaks the shared dependency used by every branch. End-to-end availability depends on the whole path, while capacity depends on its constrained resources.

## Follow the dependency path before claiming the goal is met

```text
Client
  ↓
Load balancer
  ├─ Backend instance A ─┐
  ├─ Backend instance B ─┼─ PostgreSQL
  └─ Backend instance C ─┘
```

1. The load balancer sends each request to a healthy backend instance.
2. Stateless instances can be added when request volume rises.
3. If instance B fails, A and C continue serving requests.
4. Every feed request still depends on PostgreSQL.
5. If PostgreSQL is unavailable, healthy application instances cannot complete the feed request.

This is why architecture quality must be evaluated across the full dependency path, not component by component.

## Project mapping

The current application exposes HTTP endpoints through [PostController.java](../../../../../src/main/java/com/instagram/backend/controller/PostController.java) and executes feed logic in [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java).

The token design in [TokenService.java](../../../../../src/main/java/com/instagram/backend/service/TokenService.java) can support stateless authentication because any instance with the signing configuration can validate a token. Local media storage in [MediaStorageService.java](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java), however, becomes instance-local state and would need shared object storage before multiple instances can serve the same media reliably.

## Match the change to the quality you need

| Goal | Typical move | Cost or new risk |
|---|---|---|
| More request capacity | Add stateless instances | Load balancing, coordination, and more downstream load |
| Survive instance failure | Redundant instances across failure zones | Higher cost and health-check complexity |
| Preserve correct outcomes | Transactions, idempotency, durable storage | Latency and implementation complexity |
| Reduce latency | Cache or move data closer | Staleness and invalidation complexity |

Do not say “microservices make the system scalable.” A well-structured monolith can scale horizontally, and poorly bounded microservices can amplify failures.

## One pool is full. Should every instance disappear from routing? — paper exercise

Suppose B's process is alive, but its database connections are all occupied. Predict what a probe checking only whether the process runs can tell us about a new feed request.

That probe may pass while the feed waits for a connection and times out. Inspect latency, connection waits, and active work. If B alone is stuck and A/C can safely absorb traffic, taking B out of rotation can help while its work drains. Bound waits and accepted work so the instance can recover.

If the database is overloaded for *every* instance, marking every database-dependent backend unready does not create a healthy destination. Repeated probes may themselves need scarce connections. Separate process liveness from readiness for a particular serving path, and design readiness alongside admission limits rather than using it as a universal overload switch. Verify the dependency and successful request path during recovery, then rehearse both an isolated-instance failure and shared-database saturation.

## Explain it using the feed path

In 60–90 seconds, explain the grocery-store analogy, then replace it with the Instagram request path and identify one choice for each quality.

## Before moving on

Artifact: `READY`; comprehension not yet demonstrated.

Proceed only when you can:

- define all three qualities without using them as synonyms;
- explain what adding backend instances improves and what it does not;
- diagnose the exhausted-connection-pool scenario;
- name one trade-off introduced by redundancy.

## Questions and explained answers

<details>
<summary>1. Does horizontal scaling automatically make a system highly available?</summary>

No. It helps only if instances do not share an unprotected failure dependency, are placed across independent failure zones, and traffic is routed away from unhealthy instances. Three instances in one failed zone or behind one failed database do not provide end-to-end availability.

</details>

<details>
<summary>2. Can an available system be unreliable?</summary>

Yes. It may accept every request but lose writes, return stale authorization, or duplicate payments. A reachable HTTP endpoint alone does not establish useful availability. Define its success criteria, then assess the correctness and durability of the resulting business behavior as well.

</details>

<details>
<summary>3. Which metric would you ask for before “scaling” checkout?</summary>

Ask what is saturated or violating a target: request rate, CPU, database connections, lock contention, queue depth, or latency percentile. The answer determines whether to add instances, optimize a query, partition work, or protect a constrained dependency.

</details>

<details>
<summary>4. What is the difference between latency and throughput?</summary>

Latency is the time one operation takes. Throughput is the amount of work completed per unit time. Batching may improve throughput while increasing the waiting time of an individual item.

</details>
