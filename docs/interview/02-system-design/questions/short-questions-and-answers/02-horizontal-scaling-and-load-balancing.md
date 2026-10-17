# Horizontal Scaling and Load Balancing

## Question

Your team adds a second Spring Boot pod. An image uploaded through one pod intermittently returns `404 Not Found` when a later request reaches the other pod.

**“How would you scale a stateless Spring Boot API horizontally and route traffic safely?”**

## Why this problem exists

Horizontal scaling works safely only when any healthy instance can handle the next request. Required state kept in one process or on one pod’s disk makes load balancing expose inconsistent behavior instead of resilience.

## Analogy: interchangeable service counters

A customer can use any bank counter because every teller shares the account system. More counters increase capacity. The analogy stops because application replicas can overload shared dependencies and require automated health checks and traffic draining.

## Terms to establish

- **Horizontal scaling** adds instances rather than making one instance larger.
- A **load balancer** selects a healthy instance for each request.
- A **stateless service** keeps no required request-to-request state only inside one instance.
- **Readiness** indicates whether an instance should receive traffic now.

## What I would do

1. Put replicas behind an L7 load balancer and route only to ready instances.
2. Keep business data in shared PostgreSQL, media in object storage, and identity in verifiable tokens or a shared session store.
3. Drain before shutdown and scale from concurrency or latency only when dependencies have headroom.

## Concrete walkthrough

1. An upload reaches pod A, which writes `/uploads/abc.jpg` locally.
2. A later image request reaches pod B, which lacks the file and returns `404`.
3. Move media to object storage and persist its object key. Either pod can now serve a stable URL.
4. Terminate pod A; readiness removes it, requests drain, and pod B continues serving traffic.

## Instagram-backend mapping

[TokenService.java](../../../../../src/main/java/com/instagram/backend/service/TokenService.java) lets replicas authenticate independently when they share a signing key. JPA data is in PostgreSQL. [MediaStorageService.java](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) writes instance-local files, the concrete blocker. [application.properties](../../../../../src/main/resources/application.properties) enables liveness and readiness probes. Replicas, load balancing, shared object storage, graceful draining, and autoscaling are proposed, not implemented.

## Choices and trade-offs

- Add replicas for application concurrency or availability, not when PostgreSQL is already saturated.
- Shared storage permits failover but adds network dependency and cost.
- Sticky sessions can contain an incident, but create imbalance and weak failover.

## Failure drill

**Prediction:** local media makes failures correlate with the selected pod. **Symptom and detection:** the same URL alternates between `200` and `404`, grouped logs show failures only on instances without the file. **Containment:** drain affected replicas or temporarily pin traffic while preserving availability. **Recovery:** copy media to shared durable storage, update references safely, and re-enable balanced routing. **Prevention:** prohibit required instance-local state and test pod rescheduling, rolling deployment, and scale-out behavior.

## Two-to-three-minute interview answer

I would make every replica interchangeable. An L7 load balancer should route only to ready instances, while liveness replaces stuck processes. JWT authentication works across replicas when they share compatible keys. Users, posts, and likes belong in PostgreSQL, and media belongs in shared object storage, not on pod disks.

I would keep replica configuration compatible, gracefully drain terminating pods, and autoscale from concurrency, latency, and saturation signals tied to the bottleneck. More replicas help only when shared dependencies have headroom; otherwise they worsen database pressure. Sticky sessions may contain an incident but do not replace stateless design.

I would verify failure behavior directly: upload media, kill the receiving pod, reschedule traffic, and confirm another replica can authenticate the request and retrieve the same file. I would also run rolling deployments and ensure readiness prevents startup or draining instances from receiving traffic.

## Practice status

The artifact is ready; comprehension is not implied. Practice by tracing one upload across two pods and explaining why sticky sessions only hide the underlying problem.

## Questions and explained answers

<details>
<summary>Why can adding application replicas make database latency worse?</summary>

Each replica may open its own connection pool and issue more concurrent queries. If the database is already constrained, extra callers increase contention and waiting rather than adding useful capacity.
</details>

<details>
<summary>What is the difference between liveness and readiness?</summary>

Liveness asks whether the process should be restarted. Readiness asks whether it should receive traffic now; an instance may be alive but temporarily unready during startup, draining, or dependency loss.
</details>

<details>
<summary>Why are sticky sessions a weak fix for instance-local state?</summary>

They create uneven load and fail when the selected instance is restarted or unavailable. Shared durable state lets any healthy replica continue the workflow.
</details>
