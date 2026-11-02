# Horizontal Scaling and Load Balancing

## Question

Your team adds a second Spring Boot pod. An image uploaded through one pod intermittently returns `404 Not Found` when a later request reaches the other pod.

**“How would you scale a stateless Spring Boot API horizontally and route traffic safely?”**


## Follow the photo through two application copies

With one Spring Boot application, an upload can save `/uploads/abc.jpg` on its own disk and read it back later. Now run two copies, A and B, to share traffic. A receives the upload. The next image request reaches B. B looks on its own disk, finds no file, and returns `404`.

The routing worked: B was running and received the request. The problem is that the file belongs to A alone. Adding application copies is **horizontal scaling**. It helps only if those copies can handle the requests we send them.

A **load balancer** chooses an application copy for each incoming request. It has no reason to know that this particular photo exists only on A. We need to arrange the data so either healthy copy can finish the request.

## What must be shared, and what can stay local?

Keep durable post records in shared PostgreSQL and put photos in durable storage accessible to both copies, such as object storage. The database can store the object's key. A and B can then return or authorize access to the same object, regardless of which one received the upload.

This is the relevant meaning of a **stateless service**: a later request does not depend on information held only by the particular application copy that handled an earlier one. The application still uses state; that state has an owner outside one disposable copy. Temporary local caches are possible if losing them does not lose required data or correctness.

Think of bank counters whose tellers consult the same account records. Any teller can serve the next customer. The analogy has a limit: software copies also need automated traffic controls and can collectively overload the shared database.

## A running copy may still need to stop receiving requests

Before shutting A down, first stop routing new requests to it, let existing work finish within a limit, and only then terminate it. This is graceful draining. **Readiness** answers “should this copy receive traffic now?” Liveness answers whether its process needs restarting. They serve different purposes; a temporarily unavailable dependency should not automatically cause a restart loop.

Finally, check PostgreSQL before adding more copies. Every copy can add connections and queries. More callers cannot create capacity inside an already overloaded database.

## Where this appears in our backend

[TokenService.java](../../../../../src/main/java/com/instagram/backend/service/TokenService.java) lets replicas authenticate independently when they share a signing key. JPA data is in PostgreSQL. [MediaStorageService.java](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) writes instance-local files, the concrete blocker. [application.properties](../../../../../src/main/resources/application.properties) enables liveness and readiness probes. Replicas, load balancing, shared object storage, graceful draining, and autoscaling are proposed, not implemented.

## Choosing the approach and paying its costs

- Add replicas for application concurrency or availability, not when PostgreSQL is already saturated.
- Shared storage permits failover but adds network dependency and cost.
- Sticky sessions can contain an incident, but create imbalance and weak failover.


## Paper drill: the same URL alternates between success and failure

Predict which log field would distinguish a missing photo from a broken URL. Then compare requests for `/uploads/abc.jpg` by application instance. If A returns `200` and B returns `404`, inspect which disk contains the file.

Temporarily route affected requests to the copy that has the media, where that preserves availability, and protect its capacity. This contains the incident but leaves a dependence on that copy. Recover by copying the files to shared durable storage, verifying the copies, and changing references safely before restoring balanced traffic.

The follow-up test is concrete: upload a photo, remove the receiving copy from service, and fetch the same photo through another copy. Also check startup, draining and rolling replacement. A successful request before scale-out would not have exposed this failure.

## Two-to-three-minute interview answer

I would make every replica interchangeable. An L7 load balancer should route only to ready instances, while liveness replaces stuck processes. JWT authentication works across replicas when they share compatible keys. Users, posts, and likes belong in PostgreSQL, and media belongs in shared object storage, not on pod disks.

I would keep replica configuration compatible, gracefully drain terminating pods, and autoscale from concurrency, latency, and saturation signals tied to the bottleneck. More replicas help only when shared dependencies have headroom; otherwise they worsen database pressure. Sticky sessions may contain an incident but do not replace stateless design.

I would verify failure behavior directly: upload media, kill the receiving pod, reschedule traffic, and confirm another replica can authenticate the request and retrieve the same file. I would also run rolling deployments and ensure readiness prevents startup or draining instances from receiving traffic.


## Before moving on

Artifact: `READY`; comprehension: `TODO`. Explain this scenario in your own words, predict the paper drill, and attempt the questions before opening their answers. Record learning only from your own explanation and predictions.

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
