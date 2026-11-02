# 18. API aggregation and Backend for Frontend

## Question

When should a screen use an aggregated API instead of calling many fine-grained endpoints?

[Module sequence](README.md) · Artifact: READY · Learning: TODO · Suggested study: 20–30 minutes plus design exercise

## What you will learn

Decide who should combine a profile screen's data, calculate the cost of that choice, and represent a failed section without inventing a false value.

Builds on [permissions](06-identity-and-ownership.md) and [waiting budgets](14-timeouts-and-async-work.md). The mobile adapter is proposed; the existing profile composition is linked below.

## One profile screen, several pieces of data

The profile screen needs the user's identity, counts, and a preview of recent posts. The phone could call three endpoints and combine the results. That works, but now every client must arrange those calls, decide which failure blocks the screen, and avoid displaying missing information as a real value.

We can move that composition to the backend and return one deliberate view. **Aggregation** means combining several pieces of data into one response. It is useful when the same caller flow keeps repeating that work; it is not a requirement that every response contain everything a user could ever need.

The current Spring application can do this in one in-process read service. If mobile and web eventually need substantially different experiences, a separate **Backend for Frontend (BFF)** can provide an API tailored to each. The name describes the audience of the API. It does not by itself justify a new service or remote network calls.

When an aggregate does call several downstream services, one incoming request causes several outgoing requests. That is **fan-out**. It can reduce phone-to-server round trips while increasing simultaneous backend work. We must count that work rather than assume one client request means one cheap query.

A concierge gathering answers from several desks is a helpful comparison. Its limit is consistency and failure: the answers can arrive at different times, one desk can be unavailable, and the combined answer is not automatically a single snapshot.

Keep identity, counts, and recent-post preview as our fixed screen. The choices below differ in who does the composition and which new responsibilities that creates.

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

## Slow the counts section while the other sections work

For a future fault test, keep identity at 40 ms and post preview at 120 ms, but delay counts beyond its allotted budget. Predict the naive behavior: profile requests keep waiting for counts, accumulating work even though the other sections are available.

Detect dependency latency and exhausted thread/connection capacity. Contain it with bounded waits and the agreed required-versus-optional section policy; if counts is optional, return explicitly unavailable rather than 0. Once counts recovers, verify waiting work drains and complete profiles return. Prevent recurrence with overall deadlines, bounded fan-out, and a slow-section test. Merely parallelizing the calls does not make a stalled dependency stop consuming resources.

## Project mapping and practice

[ProfileController](../../../../src/main/java/com/instagram/backend/controller/ProfileController.java) exposes /api/profile/me and returns [ProfilePageResponse](../../../../src/main/java/com/instagram/backend/dto/ProfilePageResponse.java) through [ProfileService](../../../../src/main/java/com/instagram/backend/service/ProfileService.java). That is a real composed-view starting point, not proof of a separate BFF deployment or remote fan-out. For F-009/F-016, specify a bounded profile preview, identify required versus optional sections, count SQL queries, and decide whether one shared endpoint suffices.

## Interview-ready answer

I aggregate when the same screen repeatedly composes several pieces and network latency or duplicated client logic justifies it. I first consider an in-process read service; a separate BFF is useful only when client needs and ownership justify another boundary. I bound fan-out, set an overall deadline, preserve authorization, and decide whether each section is required. I do not replace missing data with false zero values. I measure latency, query count, resource saturation, and partial failures, and explain that a combined response is not automatically a single consistent snapshot.

## Before moving on

Calculate 260 ms sequentially and the ideal 140 ms concurrently. Continue when you can justify one composition boundary and an explicit result when only counts is unavailable.

Attempt the questions before opening their answers and record the explanation in [learner progress](learner-progress.md). This revision leaves learning status unchanged; a finished document is not evidence that you have demonstrated the idea.

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
