# Distributed Rate Limiting

## Question

A bot attack targets login while a legitimate creator’s viral post causes a large feed spike. You need protection without blocking normal users unfairly.

**“How would you rate-limit login, uploads, and viral feed traffic fairly?”**


## A login attempt and a feed read are different kinds of work

An address sends 500 login attempts per second across several application copies. At the same time, real users open a viral post. A single “requests per second” limit cannot express both problems: login needs abuse protection, while the feed needs fair access within its storage capacity.

First choose what a limit counts. A **rate-limit key** identifies a subject such as an authenticated user, a claimed account name or a network address. An address alone is weak evidence of identity: many people may share it, and an attacker may use many addresses. Combine signals and define which layer can reject work; independent account buckets do not magically undo an overly strict shared-address cap.

## Let a legitimate burst fit

A **token bucket** holds a limited number of permits and refills them at a chosen rate. Each admitted request consumes a permit. A short burst can use saved permits, while sustained requests eventually follow the refill rate. Set different policies for login attempts, uploads and feed reads because their costs and risks differ.

If every application copy keeps its own full allowance, a caller can multiply the intended global allowance by reaching several copies. A shared atomic decision can enforce a cross-copy limit, at the cost of another dependency and its latency. Keep a conservative local fallback for the cases where that shared service is unavailable, with an explicit per-operation policy.

A venue's occupancy rules and ticket checks make a useful analogy: one controls total pressure, another checks an individual's permission. Software has to combine imperfect identities and distributed counters, which the ticket-counter picture does not solve.

## Rate and concurrent work protect different limits

A **concurrency limit** caps work currently in progress. If database calls suddenly become much slower, the same arrival rate keeps many more calls active. A requests-per-second rule alone may therefore leave the database overloaded.

Use rate limits for arrival fairness and abuse, and concurrency limits for finite resources. On a policy rejection, document a `429 Too Many Requests` response and suitable retry guidance. General capacity overload may need a different response contract. Avoid telling every client to retry at precisely the same instant.

Finally, watch whom the limiter blocks. A lower database load is not a successful outcome if the policy excludes a whole office of ordinary users. Record bounded rejection categories and review shared-network cases as well as attack traffic.

## Where this appears in our backend

[AuthController](../../../../../src/main/java/com/instagram/backend/controller/AuthController.java) exposes registration, login, and refresh endpoints, while [MediaController](../../../../../src/main/java/com/instagram/backend/controller/MediaController.java) accepts authenticated uploads. [RequestCorrelationFilter](../../../../../src/main/java/com/instagram/backend/observability/RequestCorrelationFilter.java) records normalized route outcomes that could support bounded limiter metrics.

**Proposed, not implemented:** edge and application rate limiters, distributed counters, `Retry-After` responses, trusted exemptions, per-route concurrency limits, and abuse dashboards.

## Choosing the approach and paying its costs

- Use token buckets when short legitimate bursts should pass. A strict fixed window is simpler but creates boundary spikes and unfair cutoff behavior.
- Fail closed for security-sensitive operations when the limiter state is unavailable; do not automatically fail closed for every read or an internal limiter outage can become a full outage.
- Use shared counters for global accuracy, but keep small local emergency limits because the shared limiter itself can be slow or unavailable.


## Paper drill: the office cannot log in

Predict what an IP-only policy does when many unrelated employees share one public address. Their combined attempts may exhaust the same bucket, even if each person's behavior is normal.

Detect the pattern by comparing rejection cohorts and account-level behavior without exposing account existence to callers. Contain the incident by adjusting the shared-address policy while preserving an independent safe overall cap and appropriate account protections.

Verify that legitimate users recover as buckets refill or expire, then review whether the rule still constrains the original abuse. Prevent recurrence with shared-network tests, conservative treatment of device signals and a clear fallback policy for limiter failure. Every extra rule should have a reason and an observable fairness check.

## Two-to-three-minute interview answer

I would not use one universal requests-per-second number. Login, uploads, reads, and writes have different abuse risk and backend cost, so each gets its own token bucket and concurrency policy. I would key authenticated traffic primarily by user or API identity and combine IP, device, and account identifier for anonymous login attempts. Layered keys can reduce evasion and dependence on one shared IP, provided a shared-address rule is not still set so strictly that it blocks legitimate users before the other rules help.

Coarse protection belongs at the edge, while a shared atomic limiter provides cross-replica enforcement. The API returns `429` and `Retry-After`; overload protection may additionally shed low-priority work based on dependency saturation. I would choose fail-open or fail-closed per operation: sensitive writes should stay protected, while harmless reads may use conservative local fallback limits. Monitoring must include blocked requests, false-positive cohorts, limiter latency, and the dependency being protected.


## Before moving on

Artifact: `READY`; comprehension: `TODO`. Explain this scenario in your own words, predict the paper drill, and attempt the questions before opening their answers. Record learning only from your own explanation and predictions.

## Questions and explained answers

<details>
<summary>Why is IP address alone an unfair rate limit key?</summary>

Many legitimate users may share one NAT or carrier address, while an attacker can rotate addresses. Layered account, device, IP, and global policies reduce both false positives and evasion.
</details>

<details>
<summary>What is the difference between rate limiting and concurrency limiting?</summary>

Rate limiting bounds arrivals over time. Concurrency limiting bounds work currently consuming a finite resource, which directly protects threads, connections, or downstream capacity.
</details>

<details>
<summary>Why keep a local emergency limit when counters are shared?</summary>

The shared limiter can fail or become slow. A conservative local cap prevents one instance from sending unlimited traffic downstream while the global decision service is unavailable.
</details>
