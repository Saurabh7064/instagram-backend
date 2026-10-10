# Production Troubleshooting Scenarios

These drills turn broad system-design concepts into the short diagnosis-and-decision questions commonly asked in technical meetings. Read one detailed answer while learning; use its one-page card for no-notes revision.

| Order | Scenario | Primary concept | Detailed answer | Revision card | Comprehension |
|---:|---|---|---|---|---|
| 1 | Peak-hour latency rises from 200 ms to 3–4 seconds | Demand, capacity, saturation | [Answer](./01-peak-hour-latency-spike.md) | [Card](../../revision-cards/production-scenarios/01-peak-hour-latency-spike.md) | `TODO` |
| 2 | Latency rises 20 minutes after deployment | Causality, mitigation, rollback | [Answer](./02-post-deployment-latency-spike.md) | [Card](../../revision-cards/production-scenarios/02-post-deployment-latency-spike.md) | `TODO` |
| 3 | p99 is high while average latency looks normal | Tail latency and cohorts | [Answer](./03-high-p99-with-normal-average.md) | [Card](../../revision-cards/production-scenarios/03-high-p99-with-normal-average.md) | `TODO` |
| 4 | Database pool is full while application CPU is low | Connection waiting and database capacity | [Answer](./04-database-connection-pool-saturation.md) | [Card](../../revision-cards/production-scenarios/04-database-connection-pool-saturation.md) | `TODO` |
| 5 | Slow dependency triggers a retry storm | Deadlines, retries, idempotency | [Answer](./05-downstream-timeout-and-retry-storm.md) | [Card](../../revision-cards/production-scenarios/05-downstream-timeout-and-retry-storm.md) | `TODO` |
| 6 | Queue lag makes results stale | Consumer throughput and safe replay | [Answer](./06-queue-backlog-and-stale-results.md) | [Card](../../revision-cards/production-scenarios/06-queue-backlog-and-stale-results.md) | `TODO` |
| 7 | Cache expiry overloads the database | Stampede prevention and stale fallback | [Answer](./07-cache-stampede-on-hot-key.md) | [Card](../../revision-cards/production-scenarios/07-cache-stampede-on-hot-key.md) | `TODO` |
| 8 | One region becomes slow | Fault isolation and traffic shifting | [Answer](./08-single-region-latency-spike.md) | [Card](../../revision-cards/production-scenarios/08-single-region-latency-spike.md) | `TODO` |

## Practice rule

For one scenario at a time: answer before reading, compare with the detailed source, close it, deliver the two-minute version from the card, then answer its three unrevealed recall questions. Record comprehension only after a correct explanation, failure prediction, and trade-off.
