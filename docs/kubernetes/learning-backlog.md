# Kubernetes Learning Backlog

Use this file to track hands-on Kubernetes learning for the Instagram project.

## Status legend

- `TODO` not started
- `IN_PROGRESS` currently learning and applying
- `DONE` implemented, verified, documented, and committed

## Learning sequence

| ID | Topic and project exercise | Status | Completion evidence |
|---|---|---|---|
| K8S-001 | Containerize the Spring Boot backend with a production-style multi-stage image | DONE | Image builds; Compose backend starts; login and feed APIs pass via `curl`; recovery after PostgreSQL restart verified in [Lesson 01](./01-containerize-spring-boot-backend.md) |
| K8S-002 | Create a local cluster and learn `kubectl`, Pods, namespaces, labels, and selectors | TODO | Cluster inspection commands and a disposable Pod exercise are documented |
| K8S-003 | Deploy the backend with a Deployment and expose it with a Service | TODO | Deployment becomes available; Service routes to multiple healthy Pods |
| K8S-004 | Externalize application settings with ConfigMaps and Secrets | TODO | No runtime credentials in manifests; backend receives configuration correctly |
| K8S-005 | Run PostgreSQL with persistent storage and understand StatefulSets | TODO | Data survives PostgreSQL Pod deletion and recreation |
| K8S-006 | Add startup, readiness, and liveness probes plus resource requests/limits | TODO | Traffic waits for readiness; failed health check recovery is demonstrated |
| K8S-007 | Containerize and deploy the UI; route UI and API traffic through Ingress | TODO | Browser completes login and feed flow through one local entry point |
| K8S-008 | Practice rolling updates, rollout status, rollback, and deployment history | TODO | A bad release is detected and successfully rolled back |
| K8S-009 | Add horizontal Pod autoscaling and run a small load exercise | TODO | Metrics and replica changes are captured; scaling limits are explained |
| K8S-010 | Debug with logs, events, exec, port-forward, and ephemeral troubleshooting tools | TODO | A deliberately broken deployment is diagnosed from cluster evidence |
| K8S-011 | Apply service accounts, RBAC, security contexts, and Secret-handling rules | TODO | Least-privilege access and non-root container execution are verified |
| K8S-012 | Package environments with Kustomize or Helm | TODO | Reproducible local and production-like configurations render successfully |
| K8S-013 | Add network policies and document cluster DNS/service discovery | TODO | Intended connections work and a denied connection is demonstrated |
| K8S-014 | Build CI validation and a safe deployment workflow | TODO | Manifests are validated automatically; image tags and rollback path are documented |
| K8S-015 | Design a production topology and disaster-recovery runbook | TODO | Architecture covers managed database, media storage, availability, backups, RTO/RPO, and cost |

## Next 3 recommended

1. `K8S-001` Containerize the backend
2. `K8S-002` Local cluster and Kubernetes primitives
3. `K8S-003` Backend Deployment and Service
