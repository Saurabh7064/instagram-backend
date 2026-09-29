# DevOps Toolchain: Core, Later, and Deferred

“Latest DevOps” does not mean installing every new tool. It means learning a coherent current workflow, checking official support before each lab, and adding a tool only when its problem is visible.

This document was reviewed against official documentation on 2026-09-28. Versions must still be rechecked at lesson time.

## Core path

| Tool/service | Simple job | Problem it solves | Introduce after |
|---|---|---|---|
| Git and GitHub | Record and review change | Untracked/manual change | From the start |
| Docker/BuildKit | Build a runnable image | “Works on my machine” packaging differences | Process/JAR basics |
| kind and `kubectl` | Run and inspect Kubernetes locally | Safe, fast Kubernetes practice | Container basics |
| Terraform + AWS provider | Describe and preview AWS infrastructure | Manual, unrepeatable cloud setup | Local Terraform workflow |
| AWS CLI + IAM Identity Center/roles | Use temporary human credentials | Long-lived access keys | AWS safety lesson |
| ECR | Store immutable container images | Moving an image into AWS/EKS | Image lifecycle |
| EKS | Managed Kubernetes control plane | Operating Kubernetes on AWS | Strong local Kubernetes basics |
| Kustomize | Patch a base manifest per environment | Duplicated environment YAML | Native manifests |
| Helm | Package/install related Kubernetes resources | Reusable and third-party installation complexity | Kustomize and Kubernetes objects |
| GitHub Actions + AWS OIDC | Test/build/publish with short-lived AWS access | Manual CI and stored AWS keys | Git + image + IAM basics |
| Argo CD | Reconcile application state from Git | Drift and direct CI-to-cluster deployment | Packaging and rollout basics |
| Trivy | Scan images, files, secrets, and IaC; create SBOMs | Invisible known risk and misconfiguration | Image/config basics |
| Prometheus + Grafana | Store/query metrics, alert, and visualize | No operational feedback | Probes/resources |
| OpenTelemetry | Generate, collect, and export telemetry | Vendor-specific instrumentation and broken request context | Metrics/logging basics |
| External Secrets + Secrets Manager | Deliver external secret values to workloads | Secrets copied into Git/manifests | IAM and Kubernetes Secrets |

## Advanced capstone tools

| Tool | Why later |
|---|---|
| Gateway API + AWS Load Balancer Controller | First understand Service, DNS, TLS, controller behavior, and load-balancer cost. CRDs alone do not route traffic. |
| Cosign/Sigstore | First understand image digests and scanning. A signature proves origin/integrity, not that an image is safe. |
| Kyverno | First understand admission and policy consequences. Begin in audit, then enforce. Use current CEL-based policy APIs rather than deprecated examples. |
| Karpenter or EKS Auto Mode | First understand requests, scheduling, HPA, nodes, disruption budgets, and cost. Do not run self-managed Karpenter alongside Auto Mode for a beginner lab. |
| OpenCost | It becomes meaningful only after Prometheus and real workload/cloud cost data exist. |

## Intentionally deferred

These may be explored after the capstone, not learned simultaneously:

- Jenkins, because GitHub Actions already supplies the course CI problem;
- Flux, because Argo CD already supplies the GitOps controller role;
- Vault, because AWS Secrets Manager plus External Secrets is enough for this project path;
- Pulumi/Crossplane/OpenTofu as implementation alternatives, because Terraform is the requested infrastructure language;
- service mesh, Cilium/eBPF deep dives, Backstage, and Argo Rollouts, until a concrete project problem requires them;
- a second observability stack, because duplicate tools blur the mental model.

## Current-practice rules

- Use IAM Identity Center or assumed roles with temporary credentials for people, and OIDC federation for CI. Never teach committed/static AWS keys as the normal path.
- Use EKS Access Entries for new-cluster human access. Treat hand-editing legacy `aws-auth` as migration knowledge, not the beginner default.
- Prefer EKS Pod Identity for supported workloads; teach IRSA as a compatibility alternative.
- Use S3 state locking with `use_lockfile = true` and bucket versioning. DynamoDB-based Terraform state locking is deprecated.
- Deploy immutable Git-SHA/version tags and preferably image digests. Do not use `latest` as deployment identity.
- Keep Terraform platform state separate from application delivery state. Argo CD, not Terraform, owns GitOps-managed app resources.
- Choose an EKS Kubernetes version from current standard support at lab time; never freeze a course-wide version.
- Pin Terraform/provider compatibility, commit `.terraform.lock.hcl`, and pin reusable module versions.
- Pin third-party GitHub Actions to reviewed full commit SHAs for production-style workflows.

## Version freshness checklist

At the start of every tool-specific milestone:

1. Open the official documentation and supported-version page.
2. Record the check date and selected version in the lesson evidence.
3. Check compatibility with Kubernetes, EKS, Terraform, Java, and dependent charts/controllers.
4. Pin the version or digest in configuration.
5. Read breaking-change and deprecation notes.
6. Upgrade in a separate reviewed change, not accidentally during an unrelated lesson.

Useful official references:

- [Terraform S3 backend](https://developer.hashicorp.com/terraform/language/backend/s3)
- [EKS Access Entries](https://docs.aws.amazon.com/eks/latest/userguide/access-entries.html)
- [EKS Pod Identity](https://docs.aws.amazon.com/eks/latest/userguide/pod-identities.html)
- [GitHub Actions OIDC for AWS](https://docs.github.com/en/actions/how-tos/secure-your-work/security-harden-deployments/oidc-in-aws)
- [Kustomize](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/)
- [Helm documentation](https://helm.sh/docs/)
- [Argo CD](https://argo-cd.readthedocs.io/en/stable/)
- [Trivy](https://trivy.dev/dev/getting-started/)
- [OpenTelemetry](https://opentelemetry.io/docs/what-is-opentelemetry/)
- [Kyverno policy types](https://kyverno.io/docs/policy-types/overview/)
- [OpenCost](https://opencost.io/docs/)
