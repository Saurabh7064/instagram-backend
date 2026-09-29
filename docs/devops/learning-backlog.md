# Integrated DevOps Learning Backlog

This backlog tracks project artifacts. It does not claim that the learner understands the material; use [Learner Progress](./learner-progress.md) for that.

## Status legend

- `TODO` — not started in the integrated path
- `IN_PROGRESS` — current project milestone
- `DONE` — implemented, verified, documented, cleaned up where required, and committed

## Learning sequence

| ID | Milestone and project exercise | Status | Completion evidence |
|---|---|---|---|
| DEVOPS-000 | Baseline container/Kubernetes understanding and AWS/Terraform safety | TODO | Prerequisite checkpoint, identity/region/budget checklist, no paid resources |
| DEVOPS-001 | Local Terraform workflow and targeted refresh of weak Kubernetes prerequisites | TODO | `fmt`/`validate`/`plan` plus local apply/observe/destroy; prerequisite gaps recorded |
| DEVOPS-002 | Image lifecycle, Terraform mental model, and ECR design | TODO | Image digest explained; ECR plan and cost/cleanup behavior reviewed |
| DEVOPS-003 | Kubernetes architecture mapped to AWS infrastructure | TODO | Local failure diagnosed; cluster/node/region/AZ/VPC mapping taught back |
| DEVOPS-004 | Backend Deployment and stable Service | TODO | Multiple replicas route through one Service and recover after deletion |
| DEVOPS-005 | Configuration, Secrets, Terraform inputs, and IAM foundations | TODO | Runtime config rotates; secret and least-privilege boundaries explained |
| DEVOPS-006 | PostgreSQL persistence and S3/EBS/RDS decision | TODO | Data survives Pod replacement; state/storage choice justified |
| DEVOPS-007 | Health, resources, failure recovery, and remote-state design | TODO | Probes/resources verified; versioned S3 lockfile design reviewed |
| DEVOPS-008 | Full-stack traffic, DNS, TLS, and AWS load-balancing map | TODO | Local browser flow verified; request path explained; cloud plan remains cost-aware |
| DEVOPS-009 | Kubernetes security plus Trivy report-mode scanning | TODO | Least-privilege allow/deny evidence and triaged scan report |
| DEVOPS-010 | Kustomize, Helm, and Terraform module boundaries | TODO | Two environments render; ownership/state boundaries explained |
| DEVOPS-011 | GitHub Actions CI, AWS OIDC, ECR digest, and SBOM | TODO | Tests/scans pass; immutable image published without stored AWS keys |
| DEVOPS-012 | EKS architecture, Access Entries, cost, and teardown plan | TODO | Reviewed Terraform plan, access recovery path, billable list, no apply |
| DEVOPS-013 | Short-lived EKS managed-node-group lab | TODO | App success/failure evidence plus verified same-session teardown |
| DEVOPS-014 | EKS Pod Identity and least-privilege S3 access | TODO | Allowed and denied AWS actions observed from the workload |
| DEVOPS-015 | RDS/S3/Secrets Manager/External Secrets production boundary | TODO | Plan or approved lab proves no credentials in Git and correct ownership/cleanup |
| DEVOPS-016 | Argo CD GitOps and drift correction | TODO | Git reconciliation and controlled drift/rollback demonstrated on kind first |
| DEVOPS-017 | Prometheus/Grafana/OpenTelemetry observability | TODO | One failing/slow request diagnosed with metrics, logs, and a trace |
| DEVOPS-018 | SBOM, Cosign, provenance, and Kyverno admission policy | TODO | Artifact verified; audit then denial of one noncompliant case |
| DEVOPS-019 | Scaling, cost, resilience, and end-to-end capstone | TODO | PR-to-production-like flow, failure/recovery/rollback, architecture teach-back, cloud teardown proof |

## Next recommended

1. `DEVOPS-000` — establish the real starting point and cloud safety.
2. `DEVOPS-001` — learn Terraform locally while refreshing only demonstrated gaps.
3. `DEVOPS-002` — connect the existing backend image to Terraform and ECR without rushing into EKS.
