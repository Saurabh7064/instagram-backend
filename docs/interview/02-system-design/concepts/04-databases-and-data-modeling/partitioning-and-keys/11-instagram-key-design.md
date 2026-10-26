# Lesson 11 — Instagram has query-specific partition keys, not one universal key

[Module](./README.md) · [Previous: query routing](./10-cassandra-query-routing.md)

- Artifact: `READY`; comprehension: `TODO`.
- Core study: 10–15 minutes. Primary idea: choose a key by the exact read and its worst-case load, then account for the other queries.
- Prerequisites: SQL key roles, Cassandra grouping/order, hot-key remedies, full-key routing, and read fan-out.
- New core term: query-driven model.

## “What is the partition key for Instagram?” needs a query — Must understand

An author wants their activity, a reader wants a home feed, and an analyst wants interactions with a post. Those are different grouping requirements. Naming the application does not identify one correct key for every table.

A **query-driven model** designs stored groups around the reads that must be efficient. This can duplicate event facts in several tables, with each table serving a distinct lookup. Duplication introduces extra writes and recovery obligations; it is not a free substitute for relational queries. [Cassandra's modeling introduction](https://cassandra.apache.org/doc/latest/cassandra/developing/data-modeling/intro.html) explains this approach.

## Derive a key for the fixed activity query

Fixed requirement: return user 42's activities on October 25, recent first, including both equal-time VIEW_POST events. Keep the five-row dataset and UTC day convention from this module.

1. The client knows user 42 and date October 25, so `(user_id,activity_date)` can name the exact group.
2. Grouping only by date would mix all users into today's partition and concentrate global writes. Grouping only by user would collect unbounded history.
3. Choose `(user_id,activity_date)` as the initial bounded grouping. It yields three logical partitions for the five sample rows.
4. Choose `(occurred_at,activity_id)` as clustering columns with descending time and ascending ID. This yields 1002,1003,1001 within the selected day and preserves the tied events.
5. Estimate the worst user's per-day size and active write rate before approving the design. A small sample proves key semantics, not capacity.

A cross-midnight request now queries `(42,Oct25)` and `(42,Oct26)`. The second partition has event 1004 at October 26 00:00, later than all October 25 events. A newest-first merge therefore returns 1004 before 1002,1003,1001. The application needs both buckets because day is part of the key.

## Work through scale rather than assuming buckets are sufficient

Use lesson 4's explicit workload assumption on this same user/day model: 9,000 events/second, 3,000/second available per independent destination group. A day bucket still puts all current writes in one group. If measurement justifies four deterministic sub-groups, `(user_id,activity_date,shard_number)` can produce a modeled 2,250 writes/second each under even distribution and adequate placement.

Reading two days now requires 2×4=8 partition queries rather than 2. Pagination must preserve each group's progress and merge equal-time rows with a stable identity. The capacity benefit trades against eight opportunities for delay/failure and extra merge work. Do not add shards before identifying this need or claim that four partitions ensure four independent servers.

## Compare neighboring Instagram queries

These are **proposed** layouts, not claims about Instagram's actual internal database:

| Exact query | Candidate partition grouping | Ordering inside it | Main concern |
|---|---|---|---|
| One user's day of activity | (user_id,day) | event time, event ID | Worst-case active user |
| One author's posts in a time bucket | (author_id,bucket) | creation time, post ID | Celebrity write/read concentration |
| One reader's precomputed feed bucket | (viewer_id,bucket) | feed ordering value, unique entry ID | Extra delivery writes and refresh rules |
| Interactions on one post in a time bucket | (post_id,bucket[,shard]) | event time, interaction ID | Viral post, shard fan-out |

The names alone are not final schemas. Define how buckets are derived, how ordering ties are broken, what retention applies, and how a query locates its required buckets. A ranked feed's ordering changes introduce additional product and pagination choices; do not assume chronological activity is already a ranked home feed.

## SQL and Cassandra decisions side by side

The SQL activity exercise uses date child partitions for retention plus an index for user/time reads. The Cassandra exercise uses exact user/day groups for one query. Both begin with access patterns and size/load requirements; neither “SQL” nor “NoSQL” universally chooses a key.

Keep the current relational source of truth when its transactions, indexes, and capacity fit. Introduce a distributed query model only when measured needs justify new tables, duplicate propagation, repair, operational expertise, and cross-partition read handling. A query-specific store can coexist with relational metadata; it need not own the same consistency boundary.

The analogy is an archive with separate catalogs by author, recipient, and subject. One catalog cannot make every lookup direct. The analogy stops where keeping duplicated machine views current requires durable update/recovery rules.

## Real project mapping and concrete follow-up

[Post](../../../../../../src/main/java/com/instagram/backend/domain/Post.java) contains author, creation time, and identity. [PostRepository.findAllByOrderByCreatedAtDesc](../../../../../../src/main/java/com/instagram/backend/repository/PostRepository.java) currently reads a global chronological feed; it does not query a Cassandra viewer bucket or implement personalization.

Proposed task tied to F-016 feed expansion and SD-007 query planning: record expected query shapes and worst-case author/day volume, measure the existing relational query, then compare an indexed relational design with a bounded query-specific read model. For F-021 personalized ranking, define ordering/pagination separately. No Cassandra migration or product-feature completion is claimed by this lesson.

## Failure drill — paper exercise

- **Predict:** select author_id as the only key and then process a celebrity's prolonged history and traffic. Which assumptions fail?
- **Symptom:** one group's size and request rate dominate despite many quiet users; a cluster-wide average hides it.
- **Contain:** bound traffic and recovery queues while preserving event identities; avoid infinite retries.
- **Recover:** design/version a new bucket/shard layout, catch up from durable data, compare exact event IDs and merged order, then move reads deliberately.
- **Prevent:** evaluate worst-case users/posts and cross-bucket pagination before adopting the schema. A key change requires migration, not just an edited CREATE TABLE statement.

## Recap and stop/go

Explain why `(user_id,day)` fits the activity query, derive the cross-day order, and calculate two versus eight read groups. State why another Instagram query may need another table/key.

## Questions and explained answers

<details>
<summary>1. Why is user_id alone not automatically the best key?</summary>

It gives a direct user lookup but groups all historical activity and current traffic together. Day bucketing bounds time history; extreme current traffic may still need a measured write split.

</details>

<details>
<summary>2. How does four-way write sharding change a two-day read?</summary>

Each day now has four exact partition keys. Two days require 2×4=8 group reads and an ordered merge, with state for partial failure/pagination. The original layout required two groups.

</details>

<details>
<summary>3. Does the proposed viewer-feed key describe the current backend?</summary>

No. The real repository loads a global createdAt-desc list. A viewer-bucket feed and its delivery/ranking rules are proposed designs requiring separate implementation and verification.

</details>
