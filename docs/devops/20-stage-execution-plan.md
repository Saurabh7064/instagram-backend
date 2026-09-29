# 20-Stage Integrated DevOps Execution Plan

This is a sequence, not a deadline. Each stage becomes several 10–15 minute micro-lessons plus a separate lab. Read only the current stage; the full table is a map.

The daily sandbox is the local kind cluster. AWS/EKS is used for short, explicitly approved milestone labs and then removed.

## Phase 0 — Establish the floor

| Stage | Primary lesson | Terraform and AWS connection | DevOps practice | Evidence to finish | Cost |
|---|---|---|---|---|---|
| D00 | Diagnose container/Kubernetes prerequisites without assuming mastery | AWS account/region/identity model; Terraform's purpose | Learning evidence versus command completion | Explain source → process → image → container → Pod; baseline checkpoints recorded | Local only |
| D01 | Refresh only the weak parts of Kubernetes Modules 01–02 | Install/check Terraform and AWS CLI; local HCL exercise | Git hygiene; never store credentials/state | `fmt`, `validate`, `plan`, local apply/observe/destroy; no unresolved prerequisite | Local only |
| D02 | Image lifecycle and registries | Terraform provider/resource/data/state; ECR design | Immutable tag/digest and basic scanning | Build image, preview ECR plan, explain image tag versus digest | Plan-only by default |
| D03 | Cluster, control plane, node, Pod, desired state, and `kubectl` | Region/AZ, managed control plane, EC2 worker mapping; VPC mental model | Evidence-first troubleshooting | Predict and diagnose one local Pod failure; draw local-to-EKS mapping | Local only |

## Phase 1 — Build a reliable local platform

| Stage | Primary lesson | Terraform and AWS connection | DevOps practice | Evidence to finish | Cost |
|---|---|---|---|---|---|
| D04 | Deployment, ReplicaSet, and Service | Terraform dependency graph; what EKS manages versus what Kubernetes manages | Declarative desired state | Multiple backend Pods available through one Service; deletion self-recovers | Local only |
| D05 | ConfigMaps, Secrets, and runtime configuration | Variables, outputs, sensitive values, IAM roles/policies/temp credentials | Secret boundaries and least privilege | Rotate dev config; explain why a Kubernetes Secret is not a secret vault | Local only |
| D06 | PostgreSQL persistence and storage choices | Local/remote Terraform state; S3, EBS, and RDS roles | Backup versus persistence | Data survives Pod replacement; storage decision explained | Local; AWS plan only |
| D07 | Startup/readiness/liveness probes and CPU/memory requests/limits | S3 remote-state design with versioning and native lockfile; tagging | Metrics and failure recovery basics | Readiness blocks traffic; failed process recovers; resource behavior observed | Local; state backend only if approved |

## Phase 2 — Make delivery repeatable

| Stage | Primary lesson | Terraform and AWS connection | DevOps practice | Evidence to finish | Cost |
|---|---|---|---|---|---|
| D08 | UI/API traffic, Service types, DNS, and TLS | ALB, Route 53, ACM, public IPv4, and cost mapping | Gateway API as a later implementation, not magic routing | Full local browser flow; explain every network hop | Local only |
| D09 | Service accounts, RBAC, security contexts, and NetworkPolicies | Human IAM versus workload IAM; Pod Identity model | Trivy scan starts in report mode | Verify one allowed and one denied action; triage scan findings | Local only |
| D10 | Environment packaging | Terraform modules and separate state/lifecycle boundaries | Kustomize first; Helm for third-party/reusable packages | Render two environments; explain values/patch differences | Local only |
| D11 | Safe continuous integration | GitHub Actions assumes an AWS role through OIDC; ECR publication | Test → build → Trivy/SBOM → push by immutable digest | Pipeline passes and cannot use a stored AWS access key | Low AWS cost; approval required |

## Phase 3 — Use AWS deliberately

| Stage | Primary lesson | Terraform and AWS connection | DevOps practice | Evidence to finish | Cost |
|---|---|---|---|---|---|
| D12 | EKS architecture and shared responsibility | VPC/subnets/routes/security groups, ECR, EKS, managed nodes, Access Entries | Cost estimate and teardown rehearsal | Reviewed plan, billable-resource list, access recovery path, no apply yet | Plan-only |
| D13 | First short-lived EKS cluster | Terraform creates network/EKS; current standard-support Kubernetes selected at lesson time | Native manifests first; success/failure/cleanup evidence | Backend runs, failure diagnosed, cluster destroyed same session, leftovers checked | Paid; explicit approval |
| D14 | Application access to AWS | Pod Identity role per service account; S3 exercise | Temporary credentials and audit trail | Pod accesses only the allowed resource; denied action proved | Paid/low; explicit approval |
| D15 | Production dependency boundaries | RDS, S3 media, Secrets Manager, KMS, External Secrets, load balancing | Plan-first architecture; provision only the minimum deliberate lab | No credentials in Git; data/storage/secret ownership explained; cleanup verified | Potentially high; explicit approval |

## Phase 4 — Operate the platform

| Stage | Primary lesson | Terraform and AWS connection | DevOps practice | Evidence to finish | Cost |
|---|---|---|---|---|---|
| D16 | GitOps and drift | Terraform owns platform; Argo CD owns app objects | Argo CD sync, drift detection, rollback boundaries | Manual drift is detected/corrected on kind; EKS repeat optional | Local first |
| D17 | Observability | CloudWatch boundary and data/cost decisions | Prometheus/Grafana first; OpenTelemetry traces afterward | Diagnose one slow/failing request using metrics, logs, and a trace | Local first |
| D18 | Supply-chain and admission safety | IAM/KMS trust boundary | Trivy, SBOM, Cosign, provenance, Kyverno CEL policy | Scan/sign/verify; audit then reject one noncompliant artifact | Local first |
| D19 | Scaling, cost, resilience, and capstone | HPA versus node scaling; managed nodes versus Auto Mode/Karpenter; OpenCost | PR → CI → image → GitOps → observe → fail → recover → rollback | Rebuild from Git/Terraform, run incident, prove recovery, verify cloud teardown | Paid capstone; explicit approval |

## Tool order inside the plan

- Kustomize before Helm: first understand overlays, then package reusable/third-party software.
- Prometheus/Grafana before OpenTelemetry: first learn the signals and questions, then the vendor-neutral instrumentation pipeline.
- Trivy before Cosign/Kyverno: first find and understand risk, then prove origin and enforce policy.
- HPA before Karpenter/Auto Mode: first scale Pods, then learn when nodes must scale.
- kind before EKS: first learn Kubernetes behavior, then add AWS networking, IAM, cost, and managed components.

## The capstone ownership contract

```text
Terraform       -> AWS infrastructure
GitHub Actions  -> test, build, scan, publish
Argo CD         -> application delivery from Git
Kubernetes      -> running workload reconciliation
OpenTelemetry   -> telemetry generation and transport
Prometheus      -> metric storage/query/alerts
Grafana         -> dashboards and exploration
```

Terraform and Argo CD must not manage the same application object.
