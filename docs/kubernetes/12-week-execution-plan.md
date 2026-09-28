# 12-Week Kubernetes Execution Plan

This schedule assumes roughly four to six focused hours per week. Slow down when a failure exercise is unclear; debugging is part of the curriculum.

| Week | Learning goal | Project deliverable | Required verification |
|---|---|---|---|
| 1 | Containers and image lifecycle | Multi-stage, non-root backend image plus `.dockerignore` | Build image, run container, execute tests and API smoke calls |
| 2 | Cluster model and `kubectl` | Local cluster setup note, namespace, labels, and disposable Pod exercises | Inspect desired/actual state, logs, events, and Pod lifecycle |
| 3 | Deployments and Services | Backend Deployment and ClusterIP/locally reachable Service | Scale replicas; show Service routing and rollout status |
| 4 | Configuration and Secrets | ConfigMap/Secret-based Spring configuration | Rotate a development credential and restart safely without manifest secrets |
| 5 | Stateful workloads | PostgreSQL StatefulSet, Service, and PVC for local learning | Create data, replace Pod, and prove persistence |
| 6 | Health and resources | Spring health endpoints, three probe types, requests, and limits | Demonstrate readiness gating and recovery from a failed process |
| 7 | Full-stack traffic | UI deployment and Ingress routing for browser and `/api` traffic | Complete browser login/feed flow and save verified screenshots |
| 8 | Safe releases | Versioned image, rolling update, deliberately bad release, and rollback | Record rollout history, failure symptoms, and successful rollback |
| 9 | Scaling and scheduling | Metrics setup, HPA, load exercise, and replica placement rules | Capture CPU/load and replica changes; explain observed bottleneck |
| 10 | Security and isolation | Non-root security context, service account/RBAC, and NetworkPolicy | Verify permitted access and at least one intentional denial |
| 11 | Packaging and automation | Kustomize overlays or Helm chart plus CI lint/render validation | Render both environments and validate all generated manifests |
| 12 | Production operations | Production architecture and disaster-recovery runbook | Review managed Postgres/object storage, HA, backups, RTO/RPO, observability, cost, and rollback |

## Weekly learning loop

1. Read the simple explanation and identify the problem being solved.
2. Explain the concept in your own words before implementation.
3. Predict the resource state and failure behavior.
4. Apply the smallest working change.
5. Observe the controller status, events, logs, and application behavior.
6. Introduce one controlled failure.
7. Recover it and record why the recovery worked.
8. Try each lesson question before expanding its explained answer.
9. Complete the teach-it-back prompt.
10. Update the backlog, learning note, and commit map.

## Suggested local tooling

- A local Kubernetes implementation such as kind, minikube, or Docker Desktop Kubernetes
- `kubectl`
- Docker-compatible image tooling
- An ingress controller when Week 7 begins
- Metrics Server when Week 9 begins
- Optional later tools: Helm, Kustomize, kubeconform, and a container-image scanner

Choose one local cluster implementation and keep it through the core weeks so tooling differences do not distract from Kubernetes concepts.
