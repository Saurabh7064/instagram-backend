# Micro-Lesson 05 — Load Balancing and Stateless Services

- Status: `READY`; comprehension not yet demonstrated
- Time: 15 minutes
- Primary idea: a load balancer can safely spread requests only when any healthy instance can serve the next request
- New terms: load balancer, stateless service, health check
- Prerequisite: [Scalability, Availability, and Reliability](./01-scalability-availability-reliability.md)

## Why does this exist?

One instance has finite capacity and can fail. Multiple instances need one stable entry point that distributes traffic.

Distribution is safe only if B can serve after A. Instance-local login state or files make later requests fail randomly, so state must be portable.

## Analogy: a restaurant host

A host sends each party to an open table. Any server can continue the order because a shared system holds it, not one server's memory.

The analogy stops at partial network failures, in-flight requests, retries, and shared dependency bottlenecks.

## Plain-language model

- A **load balancer** is the stable entry point that selects a healthy backend instance for each request.
- A **stateless service** keeps no required client or workflow state only in one application instance between requests. Durable state lives in a shared database, object store, or client token.
- A **health check** is a probe that decides whether an instance is alive and ready for traffic. An alive process may still be unable to reach a dependency.

Stateless does not mean “the system has no state.” Posts, users, and likes are state. It means the application instance is not the only owner of state needed by a later request.

## Predict before reading

A user logs in through instance A. A stores the login only in its memory. The next request goes through the load balancer to instance B. What happens?

<details>
<summary>Reveal the prediction</summary>

B cannot find A's memory, so it may reject the user. Always routing that user to A hides the problem until A restarts and creates uneven load. Shared storage or a verifiable token removes the dependency.

</details>

## Concrete request flow

```text
Client → load balancer → instance A ─┐
                       instance B ─┼→ PostgreSQL
                       instance C ─┘→ shared object storage
```

1. The client sends `GET /api/feed` to one public address.
2. The load balancer selects instance B from the ready instances.
3. B validates the bearer token without needing an earlier request on B.
4. B reads shared data from PostgreSQL and returns the response.
5. A or C can serve the next request from the same token and data.
6. If B becomes unready, the balancer sends new traffic to A and C.

The balancer improves the application tier, not the whole path automatically. If PostgreSQL is unavailable or overloaded, every application instance can still fail.

## Instagram-backend mapping

**Actual:** [TokenService.java](../../../../src/main/java/com/instagram/backend/service/TokenService.java) validates bearer tokens from shared configuration, so instances using the same signing secret can authenticate them. [PostService.java](../../../../src/main/java/com/instagram/backend/service/PostService.java) loads user and post state from repositories rather than an earlier request's memory.

**Current scaling blocker:** [MediaStorageService.java](../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) stores uploads on the instance's local filesystem. A later media request routed to another instance may not find that file.

**Proposed:** put multiple instances behind a load balancer and move media to shared object storage. Use the readiness probes in [application.properties](../../../../src/main/resources/application.properties) to stop routing when the database is unavailable. This infrastructure is not implemented here.

## Decision and trade-off

| Choose | Choose it when | Avoid or delay it when | Cost or new failure mode |
|---|---|---|---|
| One instance | Local learning or low traffic tolerates downtime | Capacity or instance failure matters | Simple, but one process is the capacity and failure limit |
| Balanced stateless replicas | Requests can run on any instance and traffic or availability requires redundancy | Required files or sessions remain instance-local | More infrastructure, health-check design, and downstream load |
| Keep one user routed to one instance temporarily | A legacy session cannot yet be externalized | Treating it as the final design | Uneven load and user failure when that instance disappears |

## Failure drill

Scenario: an instance's process is running, but its database connection pool is exhausted.

1. **Prediction:** an alive-only probe passes, so traffic keeps reaching an instance that cannot complete requests.
2. **Failure:** requests wait for connections until they time out.
3. **Observable symptom:** active requests and connection waits rise while basic health stays successful.
4. **Containment and recovery:** mark the instance unready, stop new traffic, bound waits, and let work drain.
5. **Prevention:** separate alive from ready checks, bound pools and queues, and test dependency failure.

## Teach-back

In 60–90 seconds, explain why adding three instances is insufficient by itself. Trace one authenticated feed request, identify where state lives, and name the current media-storage obstacle.

## Stop/go

Proceed only when you can explain without notes why stateless does not mean “no data,” predict the in-memory-login failure, trace routing around one failed instance, and state why the database can remain a shared bottleneck.

## Questions and explained answers

<details>
<summary>1. Why must a horizontally scaled application usually be stateless?</summary>

Successive requests can reach different instances, and failed instances are replaced. Process-local required state makes routing change behavior. Shared durable storage or verifiable tokens let any healthy instance serve the request.

</details>

<details>
<summary>2. Does a load balancer make the service highly available?</summary>

No. It routes around failed instances only while healthy replicas and required dependencies remain available. One database, zone, or bad shared configuration can still take down the service.

</details>

<details>
<summary>3. Why is routing one user repeatedly to the same instance only a temporary workaround?</summary>

It preserves instance-local state while that instance lives, but creates uneven distribution and loses the user's path when the instance restarts. It also complicates scaling and deployment. Moving required state outside the instance removes the underlying coupling.

</details>
