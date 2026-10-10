# Database Connection-Pool Saturation — Interview Revision Card

> **Question:** API CPU is 40%, requests time out, and every database connection is busy. What is likely happening, and what do you check before increasing the pool?
>
> [Detailed answer](../../questions/production-scenarios/04-database-connection-pool-saturation.md)

## Key requirements

- Separate pool waiting from database execution time.
- Protect the database from amplified concurrency.
- Recover without losing or duplicating writes.

## Scale assumptions

- Total possible connections equal pool size times application instances and must stay within tested database capacity.

## Components

- A **connection pool** reuses bounded database sessions; a **lock wait** pauses until another transaction releases data.
- Pool metrics, query timings, database saturation, traces, and slow-query logs.

## Request flow

1. Measure acquisition wait, active/idle connections, and timeouts.
2. Trace time before versus inside queries.
3. Inspect plans, locks, transaction length, and query count.
4. Limit costly traffic or stop a blocker safely.
5. Fix the cause; load-test pool changes against database capacity.

## Data model

- `DbSpan(traceId, queryName, acquireMs, executeMs, rows, outcome)`

## Three trade-offs

1. A larger pool reduces waiting only if the database has spare capacity.
2. Shorter timeouts release resources faster but can reject legitimate work.
3. Read replicas add read capacity but introduce lag and routing complexity.

## Three failures and mitigations

1. **Slow query:** add the right index or reshape access; verify the plan.
2. **Long transaction/lock:** terminate safely, shorten scope, enforce deadlines.
3. **N+1 calls:** batch or join reads; add query-count tests.

## Two-minute spoken answer

Forty-percent API CPU may mean threads are waiting for connections. I separate connection-acquisition from query time, inspect active, idle, and pending counts per instance, and calculate fleet maximum connections. On the database I check CPU, I/O, locks, long transactions, plans, and limits. Traces reveal excess queries.

I do not enlarge the pool first: if the database is saturated, extra sessions add contention. I limit the costly route, cancel safe long work, stop a blocker, or shed optional reads. Then I index or rewrite queries, shorten transactions, batch N+1 calls, or add a replica for suitable reads. I load-test any pool increase with instance scaling. Recovery means acquisition wait and tail latency fall while throughput and database saturation remain safe.

## Recall questions

1. Why can a larger pool worsen the incident?
2. Which measurements separate pool starvation from a slow query?
3. How does instance autoscaling change connection capacity?
