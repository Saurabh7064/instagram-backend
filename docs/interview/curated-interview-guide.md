# What to Learn for Senior Java Backend Interviews

Prepared 2026-10-25 for **10+ years of Java, Spring, and backend experience**, targeting senior engineer or hands-on technical lead roles. Years of experience alone do not determine level; staff/architect scope needs additional evidence of influence across teams.

**Use this page to choose what to study. Use the other folders only to answer the selected question.** This is a preparation shortlist, not a course to finish from beginning to end. Its priorities replace the older blanket targets of 120–150 coding problems and 15–20 designs in the [original plan](./interview-plan.md).

## 1. Your focus in one minute

1. Prepare **two real project deep-dives and eight reusable experience stories**. Your personal decisions, failures, and results are essential at this experience level.
2. Diagnose the **24 core questions below**. Study the ones you cannot explain, apply, and defend; skip rereading what you can already demonstrate.
3. Choose **one interview profile**. A Java-heavy process needs more Java/Spring/SQL; an algorithm-heavy process needs timed data structures and algorithms (DSA).
4. Practice **two designs first**, expanding to six for design-heavy processes. Include failure recovery and trade-offs.
5. Add a new topic only for a confirmed interview round, a job requirement, a resume claim, or a repeated mock weakness.

Start applying while preparing. Neither finishing this document nor completing every existing lesson is an application prerequisite.

## 2. Choose by interview format, not company stereotype

Banks, consultancies, startups, and large product companies can each use different formats across teams, locations, and levels. The profiles below are planning choices, not claims that an entire industry asks or skips LeetCode. A process may combine profiles; prepare each confirmed round.

| Profile | Evidence that selects it | Your preparation emphasis |
|---|---|---|
| **J — Java/Spring depth** | Recruiter confirms framework questions, Java exercises, SQL, project discussion; little or no algorithm assessment | Core questions, practical Java/SQL drills, two designs, project stories. No broad DSA expansion unless the format changes. |
| **A — Algorithms + design** | Online assessment or timed DSA rounds plus system design | Core questions, the 24-problem starting set, unseen timed variations, six design prompts, stories. |
| **P — Practical backend coding** | Pair programming, take-home, code review, API implementation, or machine coding | Core questions, a small tested service exercise, code review, two low-level designs, two distributed designs, stories. |
| **L — Lead/architect emphasis** | Architecture, migration, technical strategy, mentoring, and cross-team ownership rounds | Core questions, six designs, migration and failure drills, two project deep-dives, leadership stories; preserve coding practice for any coding round. |

There are published examples of these differences: [Amazon's senior SDE guide](https://www.amazon.jobs/content/en/how-we-hire/sde-iii-interview-prep) covers coding, system design, and behavioral evidence; [Atlassian's engineering handbook](https://www.atlassian.com/company/careers/resources/interviewing/engineering) separates data structures, code design, and systems design. [GitLab's technical interview guide](https://handbook.gitlab.com/handbook/hiring/interviewing/technical/) describes reviewing and improving a merge request, and [Monzo's published backend process](https://monzo.com/blog/demystifying-the-backend-engineering-interview-process) describes take-home/pair coding, design, and behavioral stages. These examples support tailoring the preparation; they do not establish a universal Java-only company list. Reconfirm the current process for your specific role.

Ask the recruiter before increasing study scope:

- What are the rounds, durations, and level expectations?
- Is coding algorithmic, practical Java/API work, code review, or object design? Is there an online assessment?
- Will I write SQL? Which Java version and framework topics matter?
- Is design about classes in one program, a distributed service, or both?
- Can I use an IDE, run tests, consult documentation, or use AI? Follow that process's stated rules.

Record the answers in [one company note](./11-company-specific/README.md). If the format is unknown, use the mixed allocation below until you have evidence.

### Suggested allocation per 10 preparation hours

These are starting budgets, not employer statistics. Move time toward the next confirmed round and your demonstrated weaknesses.

| Profile | Java/Spring/SQL and practical coding | Timed DSA | System/object design | Projects and behavioral |
|---|---:|---:|---:|---:|
| Unknown / mixed targets | 3 h | 3 h | 2 h | 2 h |
| J | 5 h | 0.5 h | 2 h | 2.5 h |
| A | 1.5 h | 4 h | 2.5 h | 2 h |
| P | 4.5 h | 0.5 h | 2.5 h | 2.5 h |
| L | 2 h | 1 h | 4 h | 3 h |

For J/P, the small DSA allowance maintains basic array/string/map fluency; it does not activate the full algorithm list. For L with confirmed algorithm rounds, use A's coding budget until that round is ready.

## 3. Must know: 24 core questions

**First diagnose, then learn.** Attempt each question aloud for 3–5 minutes. Mark it `GAP`, `PRACTICING`, or `READY` in your weekly evidence. READY means an accurate explanation, a concrete example, a trade-off, and a correct failure prediction without notes. Reading an answer or running supplied code does not establish readiness.

Each row is a question family to revise, not a single lesson to teach all at once. If foundations are missing, select one prerequisite micro-lesson before returning to the question.

### Java and JVM — J1–J8

| ID | Interview question | Depth to demonstrate |
|---|---|---|
| J1 | Why can a key become unfindable in a `HashMap`? | `equals`/`hashCode` contract, mutable keys, collisions, immutability; ordinary records are not automatically deeply immutable. |
| J2 | Which collection would you choose for lookup, ordering, deduplication, or top-k work? | `HashMap`, `HashSet`, `ArrayList`, `TreeMap`, `ArrayDeque`, `PriorityQueue`; explain operation costs and ordering guarantees. |
| J3 | How do you design a type-safe API that handles errors and closes resources? | Generic types and `extends`/`super` use cases, erasure limits, checked/unchecked exceptions, try-with-resources. |
| J4 | When would you use a stream instead of a loop? | Lambdas/interfaces, `map` vs `flatMap`, lazy vs terminal operations, `Optional`, grouping, duplicate keys, ordering, side effects, result ownership. |
| J5 | Why is `volatile int count` insufficient for concurrent `count++`? | Threads and shared objects first; visibility vs atomicity, lost updates, happens-before, safe publication. |
| J6 | How would you protect shared state and diagnose a deadlock? | `synchronized`, locks, atomic operations, concurrent collections, compound-action races, lock ordering, thread dumps. |
| J7 | How would you run independent I/O tasks without overloading a dependency? | Executors, bounded work, futures/`CompletableFuture`, deadlines, interruption, exception handling, cancellation limitations; task vs thread. |
| J8 | How would you investigate high CPU, slow garbage collection, or an out-of-memory error? | Heap vs stack, object retention, GC basics, thread/heap dumps and profiling; choose evidence before changing JVM flags. |

Use the [Java index](./04-java/README.md). For J4, pull the relevant lessons from [functional concepts](./04-java/01-functional-programming-coding/learning-backlog.md); do not complete all 28 automatically. For J5–J7, use [thread foundations and planned coverage](./04-java/02-multithreading-from-scratch/learning-backlog.md). Advanced concurrency lessons are still planned, so an index entry is not an available complete lesson.

### Spring, Hibernate, and service implementation — S1–S7

| ID | Interview question | Depth to demonstrate |
|---|---|---|
| S1 | How does Spring Boot create and configure your service? | Constructor injection, component scanning, bean scope/lifecycle, conditional auto-configuration, profiles/properties; mutable singleton state. |
| S2 | What happens from an HTTP request to the database and back? | Filter vs interceptor vs controller, validation, service/repository responsibilities, exception mapping, REST status/method semantics, pagination. |
| S3 | Why can `@Transactional` fail to do what the author expects? | Proxy interception, self-invocation, propagation, rollback defaults, transaction duration, isolation, thread boundaries; demonstrate a failure. |
| S4 | Why does a simple JPA endpoint execute many SQL queries? | Persistence context, dirty checking/flush, lazy loading, N+1, fetch joins/projections, pagination interactions; inspect generated SQL. |
| S5 | How do you secure a Spring API and enforce ownership? | Authentication vs authorization, security filter chain, password hashing, JWT validation/expiry/revocation, cookie vs bearer-token trade-offs, CORS vs CSRF. |
| S6 | What do unit, integration, and API tests each prove? | Real database behavior, JUnit/MockMvc/Testcontainers boundaries, mocking limits, concurrency/rollback checks, deterministic failure cases. |
| S7 | How would you release and operate a Spring service safely? | Health/readiness, metrics/logs/traces, safe configuration/secrets, deployment rollback, compatibility and database connection-pool limits. |

Use the [Spring/Hibernate shortlist](./05-spring-hibernate/README.md) and selected [API design lessons](./02-system-design/api-design/README.md). A topic index is a lookup aid, not proof that every detailed answer has been authored.

### SQL and database correctness — D1–D4

| ID | Interview question | Depth to demonstrate |
|---|---|---|
| D1 | Can you write SQL for real reporting and selection requirements? | Joins, grouping/HAVING, nulls, duplicate rows, top-N per group with a window function; explain the returned rows. |
| D2 | Why is a query slow even though a column has an index? | Composite index order, selectivity, query plans, sort/pagination cost, read/write trade-offs; measure before adding indexes. |
| D3 | How do two requests avoid double booking or losing an update? | Transactions, isolation anomalies, unique constraints, optimistic vs pessimistic locking, deadlocks, bounded retries. |
| D4 | How would you model data and change its schema during a rolling release? | Keys/relationships, normalization vs duplication, SQL vs NoSQL criteria, expand/migrate/contract, old/new application compatibility. |

Use [backend fundamentals](./07-core-fundamentals/README.md), [index/query questions](./02-system-design/questions/short-questions-and-answers/04-indexing-query-design-and-read-replicas.md), [concurrent writes](./02-system-design/questions/short-questions-and-answers/21-concurrent-writes-and-database-constraints.md), and [schema evolution](./02-system-design/questions/short-questions-and-answers/20-zero-downtime-schema-evolution.md).

### Distributed services — A1–A5

| ID | Interview question | Depth to demonstrate |
|---|---|---|
| A1 | How would you scale a service, and what becomes the next bottleneck? | Requirements and meaningful estimates, stateless instances, DNS/TLS/HTTP path, load balancing, replicas vs shards, consistency; justify a monolith or service boundary. |
| A2 | How would you add a cache without serving unsafe or stale data? | Source of truth, keys/expiry/invalidation, cache-aside flow, hot keys, stampede prevention, privacy and consistency trade-offs. |
| A3 | When should work move to a queue, and when would Kafka fit? | Synchronous vs asynchronous completion, partitions, consumer groups, offsets, per-partition ordering, lag, duplicates, failed-message recovery. |
| A4 | What happens if the database commits but sending the event fails? | Idempotency keys, transactional outbox, retry/deduplication, reconciliation, multi-step compensation; limits of exactly-once claims. |
| A5 | What happens when a downstream service slows down? | Deadline budgets, bounded retries/backoff/jitter, circuit breaker, bulkhead, backpressure, overload rejection, observability and recovery. |

Use selected [short design answers](./02-system-design/questions/short-questions-and-answers/README.md), [microservices questions](./02-system-design/questions/microservices/README.md), and [production failure drills](./02-system-design/questions/production-scenarios/README.md). A3 needs targeted Kafka knowledge; the [full Kafka roadmap](../kafka/README.md) is outside the default interview scope.

## 4. Coding: choose the right kind

### Practical Java and SQL — every profile

Practice these as focused tasks, using loops or streams where appropriate. Explain empty input, null policy, duplicate handling, ownership, complexity, and tests.

- Group transactions by customer and calculate totals; compare a loop with `groupingBy` and a downstream collector.
- Deduplicate records by ID with an explicit conflict rule, then sort deterministically; explain `toMap` duplicate-key behavior.
- Diagnose and fix a shared-counter or concurrent-cache race; explain why the fix works.
- Write SQL for customers with no orders, customers above a total-spend threshold, and the top two orders per customer. Check ties and join duplication.

These are practice prompts, not newly supplied runnable lessons. Select or author one focused exercise when activating it. Do not count all of them complete because related examples exist.

### Algorithms — profile A or a confirmed DSA round

Use **24 representative problems as a starting set**, then add unseen variations only where a timed mock exposes a gap. The number is a scope boundary, not a guarantee of interview coverage. Solve in Java and explain the invariant, complexity, and edge cases.

| Order | Focus | Two representative problems |
|---:|---|---|
| 1 | Hash lookup and grouping | Two Sum; Group Anagrams |
| 2 | Two pointers | Valid Palindrome; 3Sum |
| 3 | Sliding window | Longest Substring Without Repeating Characters; Longest Repeating Character Replacement |
| 4 | Accumulated state and ranges | Subarray Sum Equals K; Merge Intervals |
| 5 | Stack | Valid Parentheses; Daily Temperatures |
| 6 | Linked lists | Reverse Linked List; Linked List Cycle |
| 7 | Binary search | Find First and Last Position of Element in Sorted Array; Search in Rotated Sorted Array |
| 8 | Tree traversal | Maximum Depth of Binary Tree; Binary Tree Level Order Traversal |
| 9 | Heap/top-k | Kth Largest Element in an Array; Top K Frequent Elements |
| 10 | Graph traversal and dependencies | Number of Islands; Course Schedule |
| 11 | Backtracking | Subsets; Combination Sum |
| 12 | Basic dynamic programming | House Robber; Coin Change |

Find the official problem links and existing lesson IDs in the [coding index](./01-leetcode/lesson-index.md). Rows later in that index can be roadmap-only; do not assume they already have runnable lesson artifacts. Learn an easier prerequisite when needed, then return to the selected representative problem.

**Stop expanding** when you can solve three mixed, unseen medium problems across different patterns in about 35 minutes each, without hints, explain correctness/complexity, and test them. This is a local practice gate, not a company's pass criterion. Continue light timed practice. Add BSTs, harder graph/DP problems, tries, union-find, or other variations only for confirmed process requirements or observed weaknesses.

J/P without DSA rounds: keep basic arrays, strings, maps, sorting, and complexity fluent; spend the saved time on Java, SQL, testing, and implementation. No algorithm question is universally forbidden or guaranteed.

## 5. Design: two anchors first, six prompts at most initially

**High-level design** describes services, data stores, communication, and failure behavior. **Low-level design** describes responsibilities, interfaces, and state transitions inside a program. Check which one the interview tests.

| Order | Design prompt | What it tests |
|---:|---|---|
| 1 | Order/ticket booking with payment | Concurrent writes, reservations, idempotency, timeouts, compensation and reconciliation |
| 2 | Notification service | Queues, retries, duplicate handling, preferences, provider failures, delivery status |
| 3 | URL shortener | API/data model, identifiers, read scaling, caching, abuse prevention |
| 4 | Rate limiter | Algorithm choice, atomic updates, multiple instances, hot keys, fail-open/fail-closed trade-offs |
| 5 | Instagram/news feed | Fan-out, pagination, cache, media storage/CDN, privacy, consistency, celebrity load |
| 6 | Chat service | Connections, ordering, persistence, reconnect, duplicate delivery, offline users |

J/P: practice 1 and 2, then stop broad design expansion unless the interview requires it. A/L: extend through 6 and use new constraints to test transfer. For L, add migration sequencing, cost, team ownership, rollout, and measurable success to these same designs rather than collecting unrelated architectures.

For a confirmed object-design or machine-coding round, choose **two** from the [low-level design bank](./06-low-level-design/README.md): an in-memory cache with expiry/eviction, a notification-rule engine, or a meeting-room scheduler. Implement the core behavior and tests, then handle one changed requirement. Use design patterns only when they solve that requirement.

P: also rehearse one small API change with validation, a transactional database update, error handling, integration tests, and a concise README; review someone else's change for correctness and maintainability. Time-box it to the actual interview format.

For every distributed design: requirements → APIs/data → one concrete request/event → scale-sensitive decisions → failure/recovery → security/observability → alternatives. Use the [design structure and source material](./02-system-design/README.md). READY means leading a 45–60 minute mock, defending two trade-offs, and adapting to a changed constraint. Prepare two real project deep-dives independently of these hypothetical designs.

## 6. Your experience: mandatory at 10+ years

Prepare **two real projects** using the [project template](./09-resume-project-deep-dives/README.md). Know the business problem, measured scale, architecture, your exact contribution, alternatives, deployment, incidents, results, and what you would change. Use only facts you can defend; label estimates and distinguish team work from your decisions.

Reuse the existing [eight-story bank](./03-behavioral/story-bank.md): technical challenge, architecture decision, production incident, performance improvement, disagreement, ambiguous ownership, failure/learning, and mentoring/cross-team influence. Give each a two-minute version and a deeper five-minute version.

Expect follow-ups such as: “What did you personally decide?”, “What failed?”, “Why not the alternative?”, “How did you measure the result?”, and “How did you get others to adopt it?” For lead/architect roles, show influence beyond your own implementation. Do not infer staff readiness from tenure.

For production preparation, start with **three** [failure scenarios](./02-system-design/questions/production-scenarios/README.md): a post-deployment latency spike, database connection-pool saturation, and downstream timeouts/retry storms. Predict a symptom, select evidence, contain impact, recover, and prevent recurrence. Add queue lag or cache stampede when your role/design needs it.

## 7. Defer unless there is evidence you need it

| Defer from default interview study | Promote it only when… |
|---|---|
| Full Kubernetes, Terraform, AWS, or Kafka courses and certifications | The role owns that platform, a round explicitly tests it, or a resume claim requires depth. Keep the deployment, queue, and cloud services you actually used explainable. |
| Every functional-programming lesson | J4 reveals a specific gap. Custom collectors, gatherers, and advanced parallel-stream behavior are conditional topics. |
| Deep JVM tuning, bytecode internals, GC implementation details | A performance/JVM role or an incident you claim requires them. J8 diagnosis remains core. |
| Full Java-version feature catalog | The job uses the version/feature. Review records, switch/pattern matching, or virtual threads in that context; explain limitations and verify against that JDK's official documentation. |
| Every Spring annotation/module, reactive programming, WebFlux/Reactor | The job uses it or asks for reactive systems. Spring MVC and its runtime behavior remain core. |
| Advanced algorithms, competitive programming, dozens of hard problems | A confirmed assessment or repeated mock weakness requires them. |
| More hypothetical systems, exotic databases, deep consensus internals | The design role/domain needs them; first explain ordinary storage, consistency, failure, and recovery well. |
| New AI projects, frontend specialization, or unrelated tools | They are explicit role requirements or directly repair interview evidence. |

Defer means “outside this interview preparation scope,” not “unimportant engineering.” Keep broader learning in its own backlog without scheduling it as interview work.

## 8. Execution rules that keep the list small

1. **First block:** record your interview profile and attempt J1, S3, D3, and one project explanation. This is a diagnostic, not four simultaneous lessons. Pick the weakest relevant item afterward.
2. **Keep at most three weekly outcomes:** one Java/practical-coding gap, one design/data gap, and one story/mock outcome. Profile A can replace the first with a coding pattern. Teach only one micro-lesson at a time.
3. **Keep the existing active lesson:** [Two Sum](./01-leetcode/lessons/01-arrays-hash-maps/03-value-to-index/README.md) still needs its explanation/checkpoints in the [tracker](./progress-tracker.md). This guide activates no lessons and awards no mastery. Finish that check or explicitly defer it before opening another coding lesson.
4. **Use the archive as a lookup:** open the linked lesson for the selected question, learn the prerequisite if necessary, demonstrate it, then close the folder. Do not follow every link as a syllabus.
5. **Review weekly:** record evidence in the [progress tracker](./progress-tracker.md), remove completed gaps from the active queue, and adapt to real interview feedback. Put unrelated questions in the [inbox](./question-inbox.md).

| Area | Enough to move from study to maintenance practice |
|---|---|
| Core 24 | Can answer accurately without notes, apply the idea, discuss a trade-off, and predict a failure; revisit missed questions rather than restarting a course. |
| Practical coding/SQL | Can produce a correct solution, explain input/ownership/error choices, and check meaningful edge cases in the allotted round time. |
| Algorithms, when required | Three mixed unseen medium passes with explanation/tests; continue targeted practice for the actual assessment. |
| Design | Two anchor designs for J/P or six starting prompts for A/L, followed by a mock with an unfamiliar constraint; counts alone do not establish readiness. |
| Experience | Two defensible project deep-dives and eight rehearsed stories that survive skeptical follow-ups. |
| Full interview | Rehearse the actual round format, record feedback, and repair the largest recurring gap. Start applications before every gate is met. |

**When you are unsure what to learn next, choose the weakest question needed for your next interview round. Do not choose another course.**
