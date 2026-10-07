# Micro-Lesson 01 — Scalability, Availability, and Reliability

- Status: `READY`; comprehension not yet demonstrated
- Time: 15 minutes
- Primary idea: these are different system qualities, so improving one does not automatically improve the others
- New terms: scalability, availability, reliability
- Prerequisite: none

## Why does this exist?

“Make it scalable” is not a complete requirement. A system can serve more users but fail frequently. Another system can stay reachable while returning incorrect results. An interviewer needs to know which problem you are solving before evaluating your architecture.

## Analogy: a grocery store

- **Scalability:** Can the store add checkout lanes when more shoppers arrive?
- **Availability:** Is at least one checkout lane open when a shopper needs it?
- **Reliability:** Does the checkout charge the correct amount and record the purchase every time?

The analogy is useful for separating the goals. It stops working when software introduces replication lag, partial network failure, retries, and requests duplicated across machines.

## Plain-language model

### Scalability

The ability to handle more load by adding or enlarging resources without unacceptable performance loss.

Example: add Spring Boot instances behind a load balancer when feed traffic increases.

### Availability

The proportion of time the system can accept and serve requests. Redundant instances across failure zones improve availability because one failure does not remove every serving path.

### Reliability

The ability to produce correct, durable outcomes over time. A payment endpoint that returns `200 OK` but occasionally charges twice may be available, but it is not reliable.

## Predict before reading

One backend instance becomes three instances behind a load balancer, but all three use one database. What improved, and what major risk remains?

<details>
<summary>Reveal the prediction</summary>

Application-tier capacity and availability improved: traffic can be spread and one application instance can fail. The single database remains a shared bottleneck and single failure domain, so the whole request path is not highly available.

</details>

## Concrete Instagram feed flow

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

The current application exposes HTTP endpoints through [PostController.java](../../../../src/main/java/com/instagram/backend/controller/PostController.java) and executes feed logic in [PostService.java](../../../../src/main/java/com/instagram/backend/service/PostService.java).

The token design in [TokenService.java](../../../../src/main/java/com/instagram/backend/service/TokenService.java) can support stateless authentication because any instance with the signing configuration can validate a token. Local media storage in [MediaStorageService.java](../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java), however, becomes instance-local state and would need shared object storage before multiple instances can serve the same media reliably.

## Decision and trade-off

| Goal | Typical move | Cost or new risk |
|---|---|---|
| More request capacity | Add stateless instances | Load balancing, coordination, and more downstream load |
| Survive instance failure | Redundant instances across failure zones | Higher cost and health-check complexity |
| Preserve correct outcomes | Transactions, idempotency, durable storage | Latency and implementation complexity |
| Reduce latency | Cache or move data closer | Staleness and invalidation complexity |

Do not say “microservices make the system scalable.” A well-structured monolith can scale horizontally, and poorly bounded microservices can amplify failures.

## Failure drill

Scenario: the load balancer continues routing to an instance whose process is alive but whose database connections are exhausted.

1. **Prediction:** some requests hang or fail even though the process responds to a basic liveness check.
2. **Observable symptom:** elevated latency, timeouts, and connection-pool saturation on one or more instances.
3. **Recovery:** stop routing new traffic to an unready instance, bound request time, and allow capacity to recover.
4. **Mitigation:** distinguish liveness from readiness, use bounded pools and queues, alert on saturation, and prevent retry storms.

## Interview questions

<details>
<summary>1. Does horizontal scaling automatically make a system highly available?</summary>

No. It helps only if instances do not share an unprotected failure dependency, are placed across independent failure zones, and traffic is routed away from unhealthy instances. Three instances in one failed zone or behind one failed database do not provide end-to-end availability.

</details>

<details>
<summary>2. Can an available system be unreliable?</summary>

Yes. It may accept every request but lose writes, return stale authorization, or duplicate payments. Availability describes reachability; reliability includes correct and durable behavior.

</details>

<details>
<summary>3. Which metric would you ask for before “scaling” checkout?</summary>

Ask what is saturated or violating a target: request rate, CPU, database connections, lock contention, queue depth, or latency percentile. The answer determines whether to add instances, optimize a query, partition work, or protect a constrained dependency.

</details>

<details>
<summary>4. What is the difference between latency and throughput?</summary>

Latency is the time one operation takes. Throughput is the amount of work completed per unit time. Batching may improve throughput while increasing the waiting time of an individual item.

</details>

## Teach-back

In 60–90 seconds, explain the grocery-store analogy, then replace it with the Instagram request path and identify one choice for each quality.

## Stop/go

Proceed only when you can:

- define all three qualities without using them as synonyms;
- explain what adding backend instances improves and what it does not;
- diagnose the exhausted-connection-pool scenario;
- name one trade-off introduced by redundancy.
