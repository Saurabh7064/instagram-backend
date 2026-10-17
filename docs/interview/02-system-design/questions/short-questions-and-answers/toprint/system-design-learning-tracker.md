# System Design Learning Tracker

This tracker follows `index.md` exactly. Use a date or checkmark only after an honest attempt. `Explain` requires the core idea, trade-off, and one failure without notes.

| ID | Question / concept | Learned | Review +1 day | Review +7 days | Explain without notes |
|---|---|---|---|---|---|

## 01 Distributed Systems Fundamentals

| 1 | Distributed systems |  |  |  |  |
| 2 | CAP theorem |  |  |  |  |
| 3 | PACELC theorem |  |  |  |  |
| 4 | Consistency models |  |  |  |  |
| 5 | Strong consistency |  |  |  |  |
| 6 | Eventual consistency |  |  |  |  |
| 7 | Causal consistency |  |  |  |  |
| 8 | Read-your-writes consistency |  |  |  |  |
| 9 | Linearizability |  |  |  |  |
| 10 | Serializability |  |  |  |  |
| 11 | Consensus algorithms |  |  |  |  |
| 12 | Raft consensus |  |  |  |  |
| 13 | Paxos |  |  |  |  |
| 14 | Leader election |  |  |  |  |
| 15 | Split-brain problem |  |  |  |  |
| 16 | Quorum reads and writes |  |  |  |  |
| 17 | Distributed locks |  |  |  |  |
| 18 | Distributed transactions |  |  |  |  |
| 19 | Two-phase commit (2PC) |  |  |  |  |
| 20 | Saga pattern |  |  |  |  |
| 21 | Idempotency keys |  |  |  |  |
| 22 | Exactly-once vs at-least-once vs at-most-once processing |  |  |  |  |
| 23 | Logical clocks |  |  |  |  |
| 24 | Vector clocks |  |  |  |  |
| 25 | Lamport timestamps |  |  |  |  |

## 02 Scalability & Load Balancing

| 26 | Horizontal scaling |  |  |  |  |
| 27 | Vertical scaling |  |  |  |  |
| 28 | Stateless vs stateful services |  |  |  |  |
| 29 | Load balancing |  |  |  |  |
| 30 | Layer 4 vs Layer 7 load balancers |  |  |  |  |
| 31 | Round-robin load balancing |  |  |  |  |
| 32 | Least-connections routing |  |  |  |  |
| 33 | Consistent hashing |  |  |  |  |
| 34 | Sticky sessions |  |  |  |  |
| 35 | Reverse proxies |  |  |  |  |
| 36 | Forward proxies |  |  |  |  |
| 37 | API gateways |  |  |  |  |
| 38 | Service discovery |  |  |  |  |
| 39 | Health checks |  |  |  |  |
| 40 | Connection pooling |  |  |  |  |
| 41 | Rate limiting |  |  |  |  |
| 42 | Token bucket algorithm |  |  |  |  |
| 43 | Leaky bucket algorithm |  |  |  |  |
| 44 | Fixed-window rate limiting |  |  |  |  |
| 45 | Sliding-window rate limiting |  |  |  |  |
| 46 | Backpressure |  |  |  |  |
| 47 | Load shedding |  |  |  |  |
| 48 | Autoscaling |  |  |  |  |
| 49 | Hotspots and hot partitions |  |  |  |  |
| 50 | Thundering herd problem |  |  |  |  |

## 03 Caching & Content Delivery

| 51 | Cache-aside pattern |  |  |  |  |
| 52 | Read-through caching |  |  |  |  |
| 53 | Write-through caching |  |  |  |  |
| 54 | Write-behind caching |  |  |  |  |
| 55 | Cache invalidation |  |  |  |  |
| 56 | Cache eviction policies (LRU, LFU, FIFO) |  |  |  |  |
| 57 | Cache TTL |  |  |  |  |
| 58 | Cache stampede |  |  |  |  |
| 59 | Cache penetration |  |  |  |  |
| 60 | Cache avalanche |  |  |  |  |
| 61 | Edge caching |  |  |  |  |
| 62 | Content Delivery Networks (CDNs) |  |  |  |  |
| 63 | Browser caching |  |  |  |  |
| 64 | HTTP caching headers |  |  |  |  |
| 65 | ETag and conditional requests |  |  |  |  |
| 66 | Redis caching |  |  |  |  |
| 67 | Distributed caching |  |  |  |  |
| 68 | Cache consistency |  |  |  |  |
| 69 | Negative caching |  |  |  |  |
| 70 | Cache warming |  |  |  |  |

## 04 Databases & Data Modeling

| 71 | SQL vs NoSQL |  |  |  |  |
| 72 | ACID properties |  |  |  |  |
| 73 | BASE properties |  |  |  |  |
| 74 | Database normalization |  |  |  |  |
| 75 | Database denormalization |  |  |  |  |
| 76 | Primary keys vs foreign keys |  |  |  |  |
| 77 | Indexes |  |  |  |  |
| 78 | Composite indexes |  |  |  |  |
| 79 | Covering indexes |  |  |  |  |
| 80 | B-tree vs B+ tree |  |  |  |  |
| 81 | Hash indexes |  |  |  |  |
| 82 | Query execution plans |  |  |  |  |
| 83 | N+1 query problem |  |  |  |  |
| 84 | Database sharding |  |  |  |  |
| 85 | Database partitioning |  |  |  |  |
| 86 | Database replication |  |  |  |  |
| 87 | Leader-follower replication |  |  |  |  |
| 88 | Multi-leader replication |  |  |  |  |
| 89 | Read replicas |  |  |  |  |
| 90 | Replication lag |  |  |  |  |
| 91 | Optimistic locking |  |  |  |  |
| 92 | Pessimistic locking |  |  |  |  |
| 93 | MVCC (Multi-Version Concurrency Control) |  |  |  |  |
| 94 | Transaction isolation levels |  |  |  |  |
| 95 | Deadlocks |  |  |  |  |
| 96 | Database connection pools |  |  |  |  |
| 97 | Materialized views |  |  |  |  |
| 98 | Change Data Capture (CDC) |  |  |  |  |
| 99 | CQRS |  |  |  |  |
| 100 | Event sourcing |  |  |  |  |

## 05 Messaging, Queues & Event-Driven Architecture

| 101 | Message queues |  |  |  |  |
| 102 | Publish-subscribe pattern |  |  |  |  |
| 103 | Kafka topics and partitions |  |  |  |  |
| 104 | Consumer groups |  |  |  |  |
| 105 | Consumer offsets |  |  |  |  |
| 106 | Message acknowledgments |  |  |  |  |
| 107 | Dead-letter queues (DLQs) |  |  |  |  |
| 108 | Retry queues |  |  |  |  |
| 109 | Exponential backoff |  |  |  |  |
| 110 | Jitter |  |  |  |  |
| 111 | Poison messages |  |  |  |  |
| 112 | Message deduplication |  |  |  |  |
| 113 | Ordering guarantees |  |  |  |  |
| 114 | Outbox pattern |  |  |  |  |
| 115 | Inbox pattern |  |  |  |  |
| 116 | Event-driven architecture |  |  |  |  |
| 117 | Eventual consistency in messaging |  |  |  |  |
| 118 | Event replay |  |  |  |  |
| 119 | Stream processing |  |  |  |  |
| 120 | Queue visibility timeout |  |  |  |  |

## 06 Networking, DNS & HTTP

| 121 | OSI model |  |  |  |  |
| 122 | TCP/IP model |  |  |  |  |
| 123 | TCP three-way handshake |  |  |  |  |
| 124 | TCP vs UDP |  |  |  |  |
| 125 | DNS resolution |  |  |  |  |
| 126 | DNS caching |  |  |  |  |
| 127 | DNS TTL |  |  |  |  |
| 128 | DNS load balancing |  |  |  |  |
| 129 | HTTP/1.1 vs HTTP/2 vs HTTP/3 |  |  |  |  |
| 130 | HTTPS and TLS |  |  |  |  |
| 131 | TLS handshake |  |  |  |  |
| 132 | SSL termination / TLS termination |  |  |  |  |
| 133 | HTTP keep-alive |  |  |  |  |
| 134 | Connection reuse |  |  |  |  |
| 135 | WebSockets |  |  |  |  |
| 136 | Server-Sent Events (SSE) |  |  |  |  |
| 137 | Long polling |  |  |  |  |
| 138 | gRPC |  |  |  |  |
| 139 | REST APIs |  |  |  |  |
| 140 | GraphQL |  |  |  |  |
| 141 | HTTP status codes |  |  |  |  |
| 142 | HTTP request methods |  |  |  |  |
| 143 | CORS |  |  |  |  |
| 144 | NAT |  |  |  |  |
| 145 | Anycast routing |  |  |  |  |

## 07 Microservices & API Design

| 146 | Monolith vs microservices |  |  |  |  |
| 147 | Modular monolith |  |  |  |  |
| 148 | Service-oriented architecture (SOA) |  |  |  |  |
| 149 | API versioning |  |  |  |  |
| 150 | API pagination |  |  |  |  |
| 151 | Cursor-based pagination |  |  |  |  |
| 152 | Offset-based pagination |  |  |  |  |
| 153 | API idempotency |  |  |  |  |
| 154 | API request validation |  |  |  |  |
| 155 | API aggregation |  |  |  |  |
| 156 | Backend for Frontend (BFF) |  |  |  |  |
| 157 | Circuit breaker pattern |  |  |  |  |
| 158 | Bulkhead pattern |  |  |  |  |
| 159 | Timeout management |  |  |  |  |
| 160 | Retry policies |  |  |  |  |
| 161 | Service mesh |  |  |  |  |
| 162 | Sidecar pattern |  |  |  |  |
| 163 | Distributed tracing |  |  |  |  |
| 164 | Correlation IDs |  |  |  |  |
| 165 | Contract testing |  |  |  |  |
| 166 | Consumer-driven contracts |  |  |  |  |
| 167 | API schema evolution |  |  |  |  |
| 168 | Feature flags |  |  |  |  |
| 169 | Strangler fig pattern |  |  |  |  |
| 170 | Anti-corruption layer |  |  |  |  |

## 08 Kubernetes, Containers & Deployment

| 171 | Containers vs virtual machines |  |  |  |  |
| 172 | Docker images |  |  |  |  |
| 173 | Multi-stage Docker builds |  |  |  |  |
| 174 | Container registries |  |  |  |  |
| 175 | Kubernetes Pods |  |  |  |  |
| 176 | Kubernetes Deployments |  |  |  |  |
| 177 | Kubernetes ReplicaSets |  |  |  |  |
| 178 | Kubernetes StatefulSets |  |  |  |  |
| 179 | Kubernetes DaemonSets |  |  |  |  |
| 180 | Kubernetes Services |  |  |  |  |
| 181 | ClusterIP, NodePort, LoadBalancer |  |  |  |  |
| 182 | Kubernetes Ingress |  |  |  |  |
| 183 | Gateway API |  |  |  |  |
| 184 | ConfigMaps |  |  |  |  |
| 185 | Kubernetes Secrets |  |  |  |  |
| 186 | Liveness probes |  |  |  |  |
| 187 | Readiness probes |  |  |  |  |
| 188 | Startup probes |  |  |  |  |
| 189 | Horizontal Pod Autoscaler (HPA) |  |  |  |  |
| 190 | Vertical Pod Autoscaler (VPA) |  |  |  |  |
| 191 | Kubernetes resource requests and limits |  |  |  |  |
| 192 | PersistentVolumes (PV) |  |  |  |  |
| 193 | PersistentVolumeClaims (PVC) |  |  |  |  |
| 194 | Helm charts |  |  |  |  |
| 195 | Helm values and templates |  |  |  |  |
| 196 | Rolling deployments |  |  |  |  |
| 197 | Blue-green deployments |  |  |  |  |
| 198 | Canary deployments |  |  |  |  |
| 199 | Kubernetes Jobs and CronJobs |  |  |  |  |
| 200 | PodDisruptionBudgets (PDB) |  |  |  |  |

## 09 Reliability, Resilience & Fault Tolerance

| 201 | Fault tolerance |  |  |  |  |
| 202 | High availability |  |  |  |  |
| 203 | Redundancy |  |  |  |  |
| 204 | Single point of failure (SPOF) |  |  |  |  |
| 205 | Graceful degradation |  |  |  |  |
| 206 | Fail-fast principle |  |  |  |  |
| 207 | Failover and failback |  |  |  |  |
| 208 | Active-active architecture |  |  |  |  |
| 209 | Active-passive architecture |  |  |  |  |
| 210 | Disaster recovery |  |  |  |  |
| 211 | Recovery Time Objective (RTO) |  |  |  |  |
| 212 | Recovery Point Objective (RPO) |  |  |  |  |
| 213 | Multi-region architecture |  |  |  |  |
| 214 | Multi-availability-zone architecture |  |  |  |  |
| 215 | Chaos engineering |  |  |  |  |
| 216 | Fault injection |  |  |  |  |
| 217 | Retry storms |  |  |  |  |
| 218 | Cascading failures |  |  |  |  |
| 219 | Heartbeats |  |  |  |  |
| 220 | Failure detection |  |  |  |  |
| 221 | Timeout propagation |  |  |  |  |
| 222 | Request hedging |  |  |  |  |
| 223 | Retry budgets |  |  |  |  |
| 224 | Graceful shutdown |  |  |  |  |
| 225 | Zero-downtime deployments |  |  |  |  |

## 10 Observability & Production Monitoring

| 226 | Logging vs metrics vs tracing |  |  |  |  |
| 227 | Structured logging |  |  |  |  |
| 228 | Centralized logging |  |  |  |  |
| 229 | Distributed tracing |  |  |  |  |
| 230 | Trace IDs and span IDs |  |  |  |  |
| 231 | OpenTelemetry |  |  |  |  |
| 232 | Prometheus |  |  |  |  |
| 233 | Grafana |  |  |  |  |
| 234 | Splunk |  |  |  |  |
| 235 | CloudWatch |  |  |  |  |
| 236 | Application Performance Monitoring (APM) |  |  |  |  |
| 237 | Service Level Indicators (SLIs) |  |  |  |  |
| 238 | Service Level Objectives (SLOs) |  |  |  |  |
| 239 | Service Level Agreements (SLAs) |  |  |  |  |
| 240 | Error budgets |  |  |  |  |
| 241 | Golden signals (latency, traffic, errors, saturation) |  |  |  |  |
| 242 | RED method (Rate, Errors, Duration) |  |  |  |  |
| 243 | USE method (Utilization, Saturation, Errors) |  |  |  |  |
| 244 | Alert fatigue |  |  |  |  |
| 245 | Alert thresholds |  |  |  |  |
| 246 | Percentile latency (P50, P95, P99) |  |  |  |  |
| 247 | Incident management |  |  |  |  |
| 248 | Root cause analysis (RCA) |  |  |  |  |
| 249 | Postmortems |  |  |  |  |
| 250 | Synthetic monitoring |  |  |  |  |

## 11 Security, Authentication & Authorization

| 251 | Authentication vs authorization |  |  |  |  |
| 252 | OAuth 2.0 |  |  |  |  |
| 253 | OpenID Connect (OIDC) |  |  |  |  |
| 254 | JWT |  |  |  |  |
| 255 | Session-based authentication |  |  |  |  |
| 256 | API keys |  |  |  |  |
| 257 | Role-Based Access Control (RBAC) |  |  |  |  |
| 258 | Attribute-Based Access Control (ABAC) |  |  |  |  |
| 259 | Least privilege principle |  |  |  |  |
| 260 | Zero Trust architecture |  |  |  |  |
| 261 | Mutual TLS (mTLS) |  |  |  |  |
| 262 | Secrets management |  |  |  |  |
| 263 | Key rotation |  |  |  |  |
| 264 | Encryption at rest |  |  |  |  |
| 265 | Encryption in transit |  |  |  |  |
| 266 | Symmetric vs asymmetric encryption |  |  |  |  |
| 267 | Hashing vs encryption |  |  |  |  |
| 268 | Salting and password hashing |  |  |  |  |
| 269 | SQL injection |  |  |  |  |
| 270 | Cross-Site Scripting (XSS) |  |  |  |  |
| 271 | Cross-Site Request Forgery (CSRF) |  |  |  |  |
| 272 | Server-Side Request Forgery (SSRF) |  |  |  |  |
| 273 | Distributed Denial of Service (DDoS) |  |  |  |  |
| 274 | Web Application Firewalls (WAF) |  |  |  |  |
| 275 | Certificate management |  |  |  |  |

## 12 Cloud Infrastructure & DevOps

| 276 | Infrastructure as Code (IaC) |  |  |  |  |
| 277 | Terraform |  |  |  |  |
| 278 | AWS CloudFormation |  |  |  |  |
| 279 | CI/CD pipelines |  |  |  |  |
| 280 | GitHub Actions |  |  |  |  |
| 281 | Jenkins |  |  |  |  |
| 282 | GitOps |  |  |  |  |
| 283 | Argo CD |  |  |  |  |
| 284 | Immutable infrastructure |  |  |  |  |
| 285 | Infrastructure drift |  |  |  |  |
| 286 | Environment promotion |  |  |  |  |
| 287 | Artifact versioning |  |  |  |  |
| 288 | Deployment rollback |  |  |  |  |
| 289 | AWS IAM |  |  |  |  |
| 290 | AWS VPC |  |  |  |  |
| 291 | Public vs private subnets |  |  |  |  |
| 292 | Security groups |  |  |  |  |
| 293 | Route tables |  |  |  |  |
| 294 | Internet gateways vs NAT gateways |  |  |  |  |
| 295 | AWS Lambda |  |  |  |  |
| 296 | AWS S3 |  |  |  |  |
| 297 | AWS RDS |  |  |  |  |
| 298 | AWS SQS and SNS |  |  |  |  |
| 299 | AWS EventBridge |  |  |  |  |
| 300 | AWS Step Functions |  |  |  |  |

## 13 Concurrency & Multithreading

| 301 | Processes vs threads |  |  |  |  |
| 302 | Concurrency vs parallelism |  |  |  |  |
| 303 | Race conditions |  |  |  |  |
| 304 | Critical sections |  |  |  |  |
| 305 | Mutex locks |  |  |  |  |
| 306 | Semaphores |  |  |  |  |
| 307 | Read-write locks |  |  |  |  |
| 308 | Reentrant locks |  |  |  |  |
| 309 | Thread pools |  |  |  |  |
| 310 | Thread starvation |  |  |  |  |
| 311 | Deadlock prevention |  |  |  |  |
| 312 | Livelock |  |  |  |  |
| 313 | Atomic operations |  |  |  |  |
| 314 | Compare-and-swap (CAS) |  |  |  |  |
| 315 | Lock-free programming |  |  |  |  |
| 316 | Memory visibility |  |  |  |  |
| 317 | Happens-before relationship |  |  |  |  |
| 318 | Context switching |  |  |  |  |
| 319 | Producer-consumer problem |  |  |  |  |
| 320 | Thread-safe data structures |  |  |  |  |

## 14 Storage, Files & Data Processing

| 321 | Object vs block vs file storage |  |  |  |  |
| 322 | Blob storage |  |  |  |  |
| 323 | Object versioning |  |  |  |  |
| 324 | Multipart uploads |  |  |  |  |
| 325 | Presigned URLs |  |  |  |  |
| 326 | Data compression |  |  |  |  |
| 327 | Data deduplication |  |  |  |  |
| 328 | Checksums |  |  |  |  |
| 329 | Data integrity |  |  |  |  |
| 330 | Write-Ahead Logging (WAL) |  |  |  |  |
| 331 | Log-Structured Merge Trees (LSM Trees) |  |  |  |  |
| 332 | Bloom filters |  |  |  |  |
| 333 | Merkle trees |  |  |  |  |
| 334 | Consistent snapshots |  |  |  |  |
| 335 | Incremental backups |  |  |  |  |
| 336 | Batch processing |  |  |  |  |
| 337 | Stream processing |  |  |  |  |
| 338 | ETL vs ELT |  |  |  |  |
| 339 | Data lakes |  |  |  |  |
| 340 | Data warehouses |  |  |  |  |

## 15 Advanced Architecture & Performance

| 341 | Domain-Driven Design (DDD) |  |  |  |  |
| 342 | Bounded contexts |  |  |  |  |
| 343 | Hexagonal architecture |  |  |  |  |
| 344 | Clean architecture |  |  |  |  |
| 345 | Dependency inversion |  |  |  |  |
| 346 | Twelve-Factor App |  |  |  |  |
| 347 | Command Query Separation (CQS) |  |  |  |  |
| 348 | Fan-out / fan-in |  |  |  |  |
| 349 | Scatter-gather pattern |  |  |  |  |
| 350 | MapReduce |  |  |  |  |
| 351 | Work stealing |  |  |  |  |
| 352 | Async vs sync communication |  |  |  |  |
| 353 | Request coalescing |  |  |  |  |
| 354 | Batching |  |  |  |  |
| 355 | Micro-batching |  |  |  |  |
| 356 | Little's Law |  |  |  |  |
| 357 | Queueing theory |  |  |  |  |
| 358 | Amdahl's Law |  |  |  |  |
| 359 | Latency vs throughput |  |  |  |  |
| 360 | Capacity planning |  |  |  |  |

## 16 Testing, Release Engineering & Compatibility

| 361 | Unit testing |  |  |  |  |
| 362 | Integration testing |  |  |  |  |
| 363 | End-to-end testing |  |  |  |  |
| 364 | Load testing |  |  |  |  |
| 365 | Stress testing |  |  |  |  |
| 366 | Soak testing |  |  |  |  |
| 367 | Spike testing |  |  |  |  |
| 368 | Contract testing |  |  |  |  |
| 369 | Chaos testing |  |  |  |  |
| 370 | Shadow traffic |  |  |  |  |
| 371 | Traffic mirroring |  |  |  |  |
| 372 | Dark launches |  |  |  |  |
| 373 | A/B testing |  |  |  |  |
| 374 | Backward compatibility |  |  |  |  |
| 375 | Forward compatibility |  |  |  |  |
| 376 | Expand-and-contract database migrations |  |  |  |  |
| 377 | Schema migration strategies |  |  |  |  |
| 378 | Rolling upgrades |  |  |  |  |
| 379 | Deployment health gates |  |  |  |  |
| 380 | Automated rollback |  |  |  |  |
