# Feed Ranking and Cold Start

## Question

Your chronological feed is technically correct, but engagement is falling because relevant posts are buried. New users have almost no activity history.

**“How would you rank a personalized feed, and how would you handle cold-start users?”**


## Choose a small set before deciding its order

A viewer follows hundreds of creators. Fetching every historical post and running an expensive scoring model on all of them would make opening the app progressively slower. Start by selecting a bounded set worth considering. This is **candidate generation**.

For our proposed example, gather up to 500 candidate post IDs from followed accounts and an eligible discovery source. Check mandatory visibility, blocking and safety rules. Only eligible candidates proceed; a score cannot grant permission.

**Ranking** then orders that smaller set using available signals such as recency, relationship and interest. Apply a diversity rule so one creator does not fill the page, and load the display fields for the top 20. Those illustrative counts are budgets to test, not universal settings.

A bookseller similarly chooses relevant books before arranging a recommendation shelf. Software must additionally apply policy and compute the result under a deadline, using data that may be delayed or missing.

## What do we know about a brand-new viewer?

Very little. This lack of history is the **cold-start** problem. Asking the viewer for interests provides explicit information; recent eligible content and a bounded amount of popularity-based discovery provide a defensible starting point. Reserve room to learn from new choices rather than repeatedly reinforcing the first guess.

Begin with rules whose inputs and behavior you can explain. A learned model can become useful when trustworthy examples and operational support justify it. Evaluate user value alongside latency, freshness, safety and diversity. Increasing clicks alone can conceal a worse experience.

## Keep a simpler route through the same safety checks

Suppose the feature service, which supplies scoring inputs, becomes slow. Give ranking a deadline within the feed's overall budget. If it cannot finish, a filtered recency-based page can preserve useful service with less personalization.

That fallback must run the same required eligibility checks. The existing global newest-first query is a starting point for recency ordering, not evidence that a future follow/privacy-aware fallback already exists.

Test model changes gradually and test the fallback continuously. A ranking feature is an added dependency in a user journey, so its failure behavior belongs in the design from the start.

## Where this appears in our backend

The current [PostService](../../../../../src/main/java/com/instagram/backend/service/PostService.java) loads every post and returns it in repository order. [PostRepository](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) exposes only a global newest-first query, and [FeedPostResponse](../../../../../src/main/java/com/instagram/backend/dto/FeedPostResponse.java) is the current hydrated response shape.

**Proposed, not implemented:** follow-based candidate sources, privacy filtering, a bounded feature service, a ranker, experiment assignment, and a recency fallback. The transactional post store should remain authoritative; ranking data is a derived view.

## Choosing the approach and paying its costs

- Choose chronological ranking when simplicity, transparency, and freshness matter most. Do not expect it to personalize a large candidate set; the cost is lower relevance.
- Choose rules-based scoring when signals are limited or explainability is important. Too many rules become hard to tune and can interact unexpectedly.
- Choose a learned model when enough trustworthy data and operational maturity exist. Do not make it the only path; models add feature freshness, bias, rollout, and fallback risks.


## Paper drill: scoring misses the deadline

Predict what users experience when feature reads slow down while the post database stays healthy. Without a ranking deadline, feed requests can wait on optional personalization until the entire request fails.

Detect increasing ranking timeouts and feed tail latency together. Contain the problem by selecting the already-tested filtered recency path. Roll back the faulty feature release, then verify latency and result eligibility before restoring normal ranking gradually.

Prevent recurrence with bounded feature calls, staged releases and failure tests proving that the fallback remains usable. Confirm its database capacity too: moving all traffic to a simpler path can still overload it.

## Two-to-three-minute interview answer

I would separate feed generation into candidate selection, mandatory filtering, ranking, and hydration. Candidate generation keeps the expensive decision bounded by selecting recent posts from followed accounts plus a small eligible discovery set. Privacy, blocking, and safety rules run before ranking because a high score must never make forbidden content visible. I would then score candidates using available signals such as recency, relationship strength, and prior interest, apply diversity rules, and return only a bounded page.

For cold start, I would use explicit onboarding interests, locale-safe popular content, recency, and controlled exploration rather than pretending we know the user. I would keep a deterministic recency fallback for feature-store or model failures. Ranking changes would be evaluated offline, canaried, and measured with latency, freshness, safety, and user-value guardrails—not engagement alone. In this project, the current global newest-first feed is a good fallback baseline, while candidate generation and ranking are future components.


## Before moving on

Artifact: `READY`; comprehension: `TODO`. Explain this scenario in your own words, predict the paper drill, and attempt the questions before opening their answers. Record learning only from your own explanation and predictions.

## Questions and explained answers

<details>
<summary>Why should privacy and blocking filters run before ranking?</summary>

Ranking decides order, not permission. Filtering first prevents a scoring bug, stale feature, or high predicted relevance from exposing content the viewer is not allowed to see.
</details>

<details>
<summary>What is a safe cold-start baseline?</summary>

A bounded mix of recent followed content, explicit onboarding interests, and eligible popular content is defensible. It provides value without claiming personalized knowledge that the system does not yet have.
</details>

<details>
<summary>Why keep a chronological fallback after adding a sophisticated ranker?</summary>

The ranker depends on models, features, and remote services that can fail or become slow. A filtered chronological path preserves feed availability and gives operators a known baseline during incidents.
</details>
