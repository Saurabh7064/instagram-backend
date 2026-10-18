# 18. API aggregation and Backend for Frontend

## Question

When should a screen use an aggregated API instead of calling many fine-grained endpoints?

[Module sequence](README.md) · Artifact: READY · Learning: TODO · Suggested study: 20–30 minutes plus design exercise

## What you will learn

Compare client composition, aggregation, and a Backend for Frontend; budget fan-out, partial failures, and authorization.

Prerequisites: the foundational contract, method, authorization, and compatibility lessons. These are proposed teaching examples; routes, collaborators, DTOs, and policies shown here are not installed by this documentation change. Imports and application wiring are omitted. Use the existing authentication rules when implementing any extension.

## Must understand: the screen needs more than one object

A profile screen needs identity, counts, and recent posts. Calling several endpoints adds network round trips and forces every client to compose errors. Aggregation combines results into one response. Fan-out means making several downstream requests for one incoming request. A Backend for Frontend (BFF) is an API tailored to a particular client experience, such as mobile, rather than a universal domain interface.

A concierge gathering several desk answers is a useful analogy. It stops matching because downstream calls can race, timeout, return different snapshots, and consume independent connection pools.

| Approach | Choose when | Cost/failure mode |
|---|---|---|
| Client composition | Few fast independent calls; reusable endpoints | Repeated client orchestration and round trips |
| General aggregate endpoint | Multiple clients need same view | Can grow into a bloated universal response |
| Separate BFF | Mobile/web requirements substantially differ | Extra deployable/contract and auth propagation |
| In-process read service | One modular backend owns data | Must prevent N+1 queries and poor coupling |

A BFF is not a reason to split a small Spring application into services. The current modular backend can compose a profile DTO in-process first.

## Spring Boot example and walkthrough

```java
@GetMapping("/api/mobile/profile/me")
MobileProfileView profile(
        @RequestHeader("Authorization") String authorization) {
    return mobileProfiles.load(authorization);
}
```

Proposed read adapter: the service resolves identity, checks each data scope, fetches required profile data and a bounded post preview, and maps an explicit DTO. Do not put network orchestration into a controller that has no deadline, bounds, or failure contract. Outgoing calls must preserve the appropriate actor/service authorization; a privileged backend token must not broaden what the caller sees.

## Request trace with budgets and partial failure

Assume illustrative component times: profile 40 ms, counts 80 ms, preview posts 120 ms, orchestration overhead 20 ms. Sequential dependency time is 40+80+120+20=260 ms. If independent calls can run concurrently, the ideal lower bound is max(40,80,120)+20=140 ms; real scheduling and pool limits can add time. Parallelism trades latency for simultaneous resource use, not free work.

If counts times out but identity is required, choose a documented policy: fail the screen, or return profile plus an explicitly unavailable counts section. Do not turn unknown count into 0: zero is a known value and can mislead users. For example a section could be `{"state":"unavailable","value":null}` under an agreed response schema. A 200 aggregate with section errors means successful delivery of that documented partial view, not universal success.

Set an overall deadline and allocate sub-budgets. Cancel/limit unnecessary work on failure where supported, cap outstanding calls, and avoid multiplying retries at the client, BFF, and every downstream layer. Independent calls are not necessarily consistent: a post can appear after the count was read. If exact consistency matters, design a common snapshot/read model; otherwise state the eventual-consistency policy.

## Controlled failure drill

Prediction: counts sleeps beyond its budget while profile and preview remain fast. Symptom in a naive implementation: every profile request occupies threads until the slow service times out. Detect dependency latency and pool saturation, contain with bounded waits and the chosen partial-response policy, recover after the dependency returns and verify resources drain, prevent with deadlines, load limits, and slow-dependency tests.

## Project mapping and practice

[ProfileController](../../../../src/main/java/com/instagram/backend/controller/ProfileController.java) exposes /api/profile/me and returns [ProfilePageResponse](../../../../src/main/java/com/instagram/backend/dto/ProfilePageResponse.java) through [ProfileService](../../../../src/main/java/com/instagram/backend/service/ProfileService.java). That is a real composed-view starting point, not proof of a separate BFF deployment or remote fan-out. For F-009/F-016, specify a bounded profile preview, identify required versus optional sections, count SQL queries, and decide whether one shared endpoint suffices.

## Interview-ready answer

I aggregate when the same screen repeatedly composes several pieces and network latency or duplicated client logic justifies it. I first consider an in-process read service; a separate BFF is useful only when client needs and ownership justify another boundary. I bound fan-out, set an overall deadline, preserve authorization, and decide whether each section is required. I do not replace missing data with false zero values. I measure latency, query count, resource saturation, and partial failures, and explain that a combined response is not automatically a single consistent snapshot.

## Stop/go and learner evidence

Explain the worked request without notes, justify a competing option, predict the failure, and describe a test that checks the final state or response. Record an attempt in [learner progress](learner-progress.md). A completed artifact does not establish understanding or an implemented feature.

## Questions and explained answers

<details>
<summary>1. Does aggregation require microservices?</summary>

No. A modular Spring backend can compose a DTO from local services/repositories. A remote BFF introduces new network and deployment failure modes.

</details>

<details>
<summary>2. Why is a missing count different from zero?</summary>

Missing means the system could not determine a value; zero states that the count is known to be zero. A partial-response schema should represent availability explicitly.

</details>

<details>
<summary>3. What is the ideal latency for 40, 80, and 120 ms independent calls plus 20 ms overhead?</summary>

Sequential is 260 ms. Fully parallel ideal is max(40,80,120)+20=140 ms, subject to scheduling, connection pools, and other overhead.

</details>

<details>
<summary>4. Does one aggregate response guarantee consistency?</summary>

No. Components may be observed at different times. Exact snapshot requirements need a coordinated read model/transaction strategy, not just one JSON envelope.

</details>

<details>
<summary>5. Where can authorization fail in a BFF?</summary>

If a privileged downstream credential retrieves data outside the caller’s scope, aggregation can leak it. Every section must preserve caller permissions and appropriate service identity.

</details>
