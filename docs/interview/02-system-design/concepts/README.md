# System Design Concept Micro-Lessons

Browse the [16-category concept index](./index.md) for the full topic roadmap. The sequence below preserves the existing lesson order across category folders.

Study one micro-lesson at a time. The files being present means only that the learning artifact is ready; it does not mean the learner understands it.

| Order | Micro-lesson | Primary idea | Artifact status |
|---:|---|---|---|
| 1 | [Scalability, Availability, and Reliability](./09-reliability-resilience-and-fault-tolerance/01-scalability-availability-reliability.md) | Different quality goals require different design choices | `READY` |
| 2 | [Caching and Consistency](./03-caching-and-content-delivery/02-caching-consistency.md) | A faster copy creates freshness decisions | `READY` |
| 3 | [Partitioning, Replication, and Hot Keys](./04-databases-and-data-modeling/03-partitioning-replication-hot-keys.md) | Split load and copy data for different reasons | `READY` |
| 4 | [Monolith-to-Microservices Migration](./07-microservices-and-api-design/04-monolith-to-microservices-migration.md) | Migrate behavior incrementally behind stable contracts | `READY` |
| 5 | [Load Balancing and Stateless Services](./02-scalability-and-load-balancing/05-load-balancing-and-stateless-services.md) | Any healthy instance can serve the next request | `READY` |
| 6 | [Latency, Throughput, and Saturation](./15-advanced-architecture-and-performance/06-latency-throughput-and-saturation.md) | Waiting grows when demand reaches constrained capacity | `READY` |
| 7 | [Tail Latency, Percentiles, and Queueing](./10-observability-and-production-monitoring/07-tail-latency-percentiles-and-queueing.md) | Slow cohorts require percentiles and stage timing | `READY` |
| 8 | [Backpressure and Load Shedding](./02-scalability-and-load-balancing/08-backpressure-and-load-shedding.md) | Bound accepted work to preserve useful service | `READY` |

Use the reusable [Concept Template](../_concept-template.md) for future lessons and record actual understanding in [Learner Progress](../learner-progress.md).
