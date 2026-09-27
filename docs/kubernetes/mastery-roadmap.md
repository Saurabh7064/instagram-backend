# Kubernetes Mastery Roadmap (Project-Driven)

The goal is operational understanding, not memorizing YAML. Every topic should answer three questions: what Kubernetes is reconciling, how to observe it, and how the Instagram application behaves when it fails.

## Tier 0 — Prerequisites

- Linux processes, ports, signals, filesystems, and environment variables
- Container images, registries, tags, layers, and multi-stage builds
- Container networking and persistent volumes
- Basic YAML and HTTP health checks

Project exercise: build a small, non-root backend image and run it locally against PostgreSQL.

## Tier 1 — Core workload model

- Cluster, control plane, and worker nodes
- Declarative desired state and reconciliation loops
- Pods and container lifecycle
- Deployments, ReplicaSets, rolling updates, and rollbacks
- Services, selectors, endpoints, and cluster DNS
- Namespaces, labels, annotations, and owner references
- `kubectl get`, `describe`, `logs`, `exec`, `apply`, and `delete`

Project outcome: multiple backend replicas are reachable through one stable Service.

## Tier 2 — Application configuration and state

- ConfigMaps and Secrets
- Environment variables versus mounted files
- PersistentVolumes, PersistentVolumeClaims, and StorageClasses
- StatefulSets and stable identity
- Jobs and CronJobs
- Database migration strategies
- Why production databases are commonly managed outside the application cluster

Project outcome: local PostgreSQL data survives Pod replacement, while the production design recommends managed PostgreSQL.

## Tier 3 — Reliability and traffic

- Startup, readiness, and liveness probes
- CPU/memory requests and limits
- Quality of Service classes and eviction
- Ingress and ingress controllers
- Graceful shutdown and termination periods
- PodDisruptionBudgets
- Horizontal Pod Autoscaling
- Scheduling basics: affinity, anti-affinity, taints, and tolerations

Project outcome: the application starts safely, receives traffic only when ready, rolls out without obvious downtime, and scales within defined bounds.

## Tier 4 — Security and multi-environment delivery

- Service accounts and RBAC
- Security contexts and non-root containers
- NetworkPolicies
- Secret-management boundaries and external secret stores
- Image provenance, vulnerability scanning, and immutable image tags/digests
- Kustomize overlays and Helm charts
- Admission policies and namespace guardrails

Project outcome: least-privilege workloads and reproducible local/production-like configuration without committed credentials.

## Tier 5 — Operations and production judgment

- Events, logs, metrics, and distributed tracing
- Debugging CrashLoopBackOff, ImagePullBackOff, Pending, OOMKilled, and failing probes
- Capacity planning and cluster autoscaling
- Backup/restore and disaster recovery
- High availability, topology spread, and failure domains
- GitOps and progressive delivery concepts
- Upgrade planning, API deprecations, and cost controls

Project outcome: a production architecture and runbook explain monitoring, deployment, rollback, backup, restore, RTO/RPO, and failure response.

## Important design distinctions for this project

- Kubernetes can restart PostgreSQL, but restart automation is not the same as a highly available database.
- The current temp-backed media storage cannot safely serve multiple backend replicas; production media belongs in shared object storage.
- A Deployment provides replicas, but availability also depends on probes, disruption policy, scheduling, dependencies, and capacity.
- A Secret object improves configuration separation but is not automatically a complete secret-management solution.
- Horizontal scaling requires stateless application behavior. The JWT design helps, while local uploaded files work against it.
