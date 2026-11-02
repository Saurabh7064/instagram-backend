# Micro-Lesson 05 — Load Balancing and Stateless Services

Core study: 15 minutes. Prerequisite: [Scalability, Availability, and Reliability](../09-reliability-resilience-and-fault-tolerance/01-scalability-availability-reliability.md).

## Upload on A, fetch on B: where did the photo go? — Must understand

You upload a photo through backend A. A saves it to its local disk and returns a URL. Your next request goes to backend B, which looks for that file on its own disk. It is absent. Both backends are healthy; the request changed behavior because required state stayed on A.

Now we can state the condition for safely spreading requests: the next suitable instance needs access to the information the previous request produced. Adding instances first can expose this problem rather than solve it.

A **load balancer** supplies a stable public entry point and selects an appropriate backend instance for incoming traffic. It can spread work and route around an unavailable instance. It does not copy that instance's memory or files to its neighbors.

## “Stateless” means no required state is stranded on one instance

Posts, accounts, and uploaded media are still state. A **stateless service** keeps no required client or workflow state solely in one application instance between requests. Shared durable storage or a verifiable client token carries what later requests need.

For our upload, shared object storage would let B retrieve the file A stored. For authentication, a bearer token can let either backend validate the same caller when both have the required signing configuration. A login remembered only in A's memory would have the same portability problem as A's file.

Always sending the user back to A can temporarily hide the problem. It leaves uneven load and loses the user's path if A restarts. Think of a restaurant host directing you to any server: the arrangement works only if the order is in a shared system rather than one server's private notebook. The analogy stops at in-flight work and retries; a failed request can already have changed shared data before the next server sees it.

## Follow an authenticated request after making state portable

The diagram is a proposed layout with shared media storage, rather than the current local-file arrangement:

```text
Client → load balancer → instance A ─┐
                       instance B ─┼→ PostgreSQL
                       instance C ─┘→ shared object storage
```

A client requests `GET /api/feed` through one public address. Suppose B is selected. It validates the token, reads the shared post/user data from PostgreSQL, and returns the response. The next request can go to A or C because the required identity and durable data did not depend on B's earlier memory.

If B stops, the balancer can send new requests to A/C, provided they can serve the path and have enough capacity. That qualification matters: all three can still depend on the same overloaded or unavailable database.

## Is the process alive, and can this path serve traffic?

A **health check** probes a specific serving condition. **Liveness** asks whether a process should remain running; **readiness** asks whether it should receive the relevant new traffic. A basic alive check does not prove a feed request can get a database connection.

Use readiness to handle failures it can actually route around. If B alone is stuck while A/C have headroom, removing B from rotation can help. If every backend shares one overloaded database, ejecting all of them creates no healthy destination. A database-aware probe may also need a scarce connection. Pair readiness with bounded admission and dependency monitoring, rather than turning every overload signal into a global routing outage.

## Instagram-backend mapping

**Actual:** [TokenService.java](../../../../../src/main/java/com/instagram/backend/service/TokenService.java) validates bearer tokens from shared configuration, so instances using the same signing secret can authenticate them. [PostService.java](../../../../../src/main/java/com/instagram/backend/service/PostService.java) loads user and post state from repositories rather than an earlier request's memory.

**Current scaling blocker:** [MediaStorageService.java](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) stores uploads on the instance's local filesystem. A later media request routed to another instance may not find that file.

**Proposed:** put multiple instances behind a load balancer and move media to shared object storage. Review the database-aware readiness probes in [application.properties](../../../../../src/main/resources/application.properties) alongside dependency and admission policy. They can expose inability to serve, but removing all instances from routing during shared database overload does not restore capacity. This infrastructure is not implemented here.

## When do the extra instances earn their complexity?

| Choose | Choose it when | Avoid or delay it when | Cost or new failure mode |
|---|---|---|---|
| One instance | Local learning or low traffic tolerates downtime | Capacity or instance failure matters | Simple, but one process is the capacity and failure limit |
| Balanced stateless replicas | Requests can run on any instance and traffic or availability requires redundancy | Required files or sessions remain instance-local | More infrastructure, health-check design, and downstream load |
| Keep one user routed to one instance temporarily | A legacy session cannot yet be externalized | Treating it as the final design | Uneven load and user failure when that instance disappears |

## B's pool fills while its process stays alive — paper exercise

Predict what an alive-only probe reports when B cannot obtain a database connection. Then ask whether this is B's isolated problem or a limit shared by all instances.

Requests may wait until timeout while the basic probe remains successful. Connection-wait time and active-request counts reveal that difference. Bound waits and new work. If suitable A/C capacity exists, temporarily taking B out of rotation lets it drain; if the database is saturated everywhere, reduce offered work instead of cycling every backend through readiness failure.

Recover by verifying the constrained dependency and a successful request path, then restore admission carefully. Test instance loss separately from shared dependency failure. Keep health probes cheap enough that they do not consume the same scarce resources they are meant to diagnose.

## Explain why B can serve the next request

In 60–90 seconds, explain why adding three instances is insufficient by itself. Trace one authenticated feed request, identify where state lives, and name the current media-storage obstacle.

## Before moving on

Artifact: `READY`; comprehension not yet demonstrated.

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
