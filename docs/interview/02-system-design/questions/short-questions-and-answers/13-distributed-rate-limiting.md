# Distributed Rate Limiting

## Question

A bot attack targets login while a legitimate creator’s viral post causes a large feed spike. You need protection without blocking normal users unfairly.

**“How would you rate-limit login, uploads, and viral feed traffic fairly?”**

## Why this problem exists

One global limit treats cheap reads and risky authentication attempts as equivalent. An IP-only limit can punish many users behind one corporate or mobile network, while a per-instance counter becomes inaccurate when traffic moves between replicas. Limits must reflect route cost, identity confidence, abuse risk, and dependency capacity.

## Analogy: venue entry and baggage rules

A venue limits total occupancy, checks each ticket, and applies separate rules to oversized baggage. The analogy stops because distributed requests reach different gateways and identities can be forged, shared, or unavailable before login.

## Terms to establish

- A **token bucket** refills permits over time and allows a bounded burst.
- A **rate limit key** identifies the subject counted, such as account, IP, device, or API token.
- A **concurrency limit** bounds work currently in flight rather than requests per second.
- **Load shedding** rejects lower-priority work to protect critical capacity.

## What I would do

1. Define separate limits for login, upload, feed reads, and mutations based on risk and resource cost.
2. Apply layered keys: account or authenticated user where known, plus IP/device/network signals for anonymous abuse.
3. Enforce coarse limits at the edge and precise shared limits at the gateway or service using an atomic distributed counter.
4. Return `429 Too Many Requests` with `Retry-After`, and distinguish rate limiting from bounded concurrency and overload shedding.
5. Monitor blocked cohorts, false positives, limiter latency, and downstream saturation; provide trusted-user or support recovery paths.

## Concrete walkthrough

1. One IP sends 500 login attempts per second across several pods.
2. The edge blocks an obvious flood; the shared limiter counts by IP, account identifier, and device signal.
3. Failed attempts consume a stricter token bucket, while successful authenticated feed traffic uses a different policy.
4. A whole office behind the same NAT is not blocked solely by the IP because account-level buckets remain independent.
5. During database overload, a concurrency guard rejects optional feed refreshes while preserving logout and security operations.

## Instagram-backend mapping

[AuthController](../../../../../src/main/java/com/instagram/backend/controller/AuthController.java) exposes registration, login, and refresh endpoints, while [MediaController](../../../../../src/main/java/com/instagram/backend/controller/MediaController.java) accepts authenticated uploads. [RequestCorrelationFilter](../../../../../src/main/java/com/instagram/backend/observability/RequestCorrelationFilter.java) records normalized route outcomes that could support bounded limiter metrics.

**Proposed, not implemented:** edge and application rate limiters, distributed counters, `Retry-After` responses, trusted exemptions, per-route concurrency limits, and abuse dashboards.

## Choices and trade-offs

- Use token buckets when short legitimate bursts should pass. A strict fixed window is simpler but creates boundary spikes and unfair cutoff behavior.
- Fail closed for security-sensitive operations when the limiter state is unavailable; do not automatically fail closed for every read or an internal limiter outage can become a full outage.
- Use shared counters for global accuracy, but keep small local emergency limits because the shared limiter itself can be slow or unavailable.

## Failure drill

**Prediction:** an IP-only policy blocks an office or carrier network behind one NAT. **Symptom and detection:** many unrelated accounts from one network receive `429` while their individual behavior is normal. **Containment:** relax the IP dimension and rely on layered account/device limits with a safe global cap. **Recovery:** expire the offending bucket and review affected cohorts. **Prevention:** test shared-network scenarios, measure false positives, and require multiple signals before severe blocking.

## Two-to-three-minute interview answer

I would not use one universal requests-per-second number. Login, uploads, reads, and writes have different abuse risk and backend cost, so each gets its own token bucket and concurrency policy. I would key authenticated traffic primarily by user or API identity and combine IP, device, and account identifier for anonymous login attempts. Layered keys prevent both easy evasion and unfair dependence on one shared IP.

Coarse protection belongs at the edge, while a shared atomic limiter provides cross-replica enforcement. The API returns `429` and `Retry-After`; overload protection may additionally shed low-priority work based on dependency saturation. I would choose fail-open or fail-closed per operation: sensitive writes should stay protected, while harmless reads may use conservative local fallback limits. Monitoring must include blocked requests, false-positive cohorts, limiter latency, and the dependency being protected.

## Practice status

The artifact is `READY`; comprehension remains `TODO` until the learner can choose keys for three routes, explain a fail-open decision, and diagnose a fairness failure.

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
