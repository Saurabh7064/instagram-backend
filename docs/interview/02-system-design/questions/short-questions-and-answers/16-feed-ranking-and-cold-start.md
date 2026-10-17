# Feed Ranking and Cold Start

## Question

Your chronological feed is technically correct, but engagement is falling because relevant posts are buried. New users have almost no activity history.

**“How would you rank a personalized feed, and how would you handle cold-start users?”**

## Why this problem exists

A user may follow hundreds of accounts, so showing every available post in time order gives equal weight to very different content. Ranking can improve relevance, but it must fit a strict latency budget and must not bypass privacy, blocking, or safety rules. A new user has little behavioral data, so a system that depends only on history has nothing reliable to rank.

## Analogy

A bookseller first chooses books the customer is allowed to see, then orders that smaller set using known interests and current popularity. The analogy stops because a distributed feed must make this decision for millions of users, from changing data, within milliseconds.

## Terms to establish

- **Candidate generation** selects a manageable set of possible posts before expensive scoring.
- **Ranking** orders candidates using signals such as recency, relationship, and predicted interest.
- **Cold start** means there is insufficient history for a new user, creator, or post.
- A **fallback** is a simpler safe path used when personalization data or models are unavailable.

## What I would do

1. Define the goal and guardrails: feed latency, freshness, privacy, content safety, diversity, and measurable user value.
2. Generate candidates from followed accounts, recent interactions, and a small set of eligible popular content.
3. Apply hard filters before ranking, then score the bounded set with fresh, available features.
4. Use onboarding choices, locale-safe popularity, recency, and exploration for cold-start users.
5. Keep a chronological or rules-based fallback and release ranking changes through offline evaluation, canaries, and controlled experiments.

## Concrete walkthrough

1. A user requests the first feed page.
2. The service gathers 500 eligible post IDs from followed accounts and a bounded discovery source.
3. Privacy and block rules remove forbidden posts; the ranker scores the remaining candidates.
4. A diversity rule prevents one creator from filling the page, and the top 20 posts are hydrated.
5. If the feature store times out, the service returns a filtered recency-based page. The observable result is reduced personalization, not a failed feed.

## Instagram-backend mapping

The current [PostService](../../../../../src/main/java/com/instagram/backend/service/PostService.java) loads every post and returns it in repository order. [PostRepository](../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) exposes only a global newest-first query, and [FeedPostResponse](../../../../../src/main/java/com/instagram/backend/dto/FeedPostResponse.java) is the current hydrated response shape.

**Proposed, not implemented:** follow-based candidate sources, privacy filtering, a bounded feature service, a ranker, experiment assignment, and a recency fallback. The transactional post store should remain authoritative; ranking data is a derived view.

## Choices and trade-offs

- Choose chronological ranking when simplicity, transparency, and freshness matter most. Do not expect it to personalize a large candidate set; the cost is lower relevance.
- Choose rules-based scoring when signals are limited or explainability is important. Too many rules become hard to tune and can interact unexpectedly.
- Choose a learned model when enough trustworthy data and operational maturity exist. Do not make it the only path; models add feature freshness, bias, rollout, and fallback risks.

## Failure drill

**Prediction:** a feature-service release causes ranking calls to exceed the feed deadline. **Symptom and detection:** feed p99 and ranker timeout rate rise while the post database remains healthy. **Containment:** open the ranking kill switch and use the filtered recency fallback. **Recovery:** roll back the feature release and verify latency and relevance metrics. **Prevention:** canary model and feature changes, enforce a ranking deadline, and continuously test the fallback.

## Two-to-three-minute interview answer

I would separate feed generation into candidate selection, mandatory filtering, ranking, and hydration. Candidate generation keeps the expensive decision bounded by selecting recent posts from followed accounts plus a small eligible discovery set. Privacy, blocking, and safety rules run before ranking because a high score must never make forbidden content visible. I would then score candidates using available signals such as recency, relationship strength, and prior interest, apply diversity rules, and return only a bounded page.

For cold start, I would use explicit onboarding interests, locale-safe popular content, recency, and controlled exploration rather than pretending we know the user. I would keep a deterministic recency fallback for feature-store or model failures. Ranking changes would be evaluated offline, canaried, and measured with latency, freshness, safety, and user-value guardrails—not engagement alone. In this project, the current global newest-first feed is a good fallback baseline, while candidate generation and ranking are future components.

## Practice status

The artifact is ready; comprehension remains unproven until the learner explains the pipeline, predicts a ranking dependency failure, and defends one cold-start choice without notes.

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
