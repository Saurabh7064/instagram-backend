# AWS and Terraform Safety Rules

AWS labs can create real charges. Terraform makes changes repeatable, but it does not make every change safe or free.

## Before the first cloud resource

- Protect the root user with phishing-resistant MFA and do not create root access keys.
- Use IAM Identity Center or an assumed role with temporary credentials for daily work.
- Select one learning account and one region.
- Create a small AWS Budget and Cost Anomaly Detection alert.
- Remember: budget/anomaly notifications can be delayed and are not a universal real-time spending cutoff.
- Decide standard tags: `Project`, `Environment`, `Owner`, `ManagedBy=Terraform`, and `ExpiresAt`.
- Never commit AWS credentials, kubeconfig, Terraform state, saved plans, rendered secrets, or `.terraform/`.

## Before every AWS-changing command

1. Verify the caller with `aws sts get-caller-identity`.
2. Verify the intended region.
3. Run `terraform fmt -check`, `terraform validate`, and relevant lint/security scans.
4. Review `terraform plan` and explain every create/update/destroy action.
5. List potentially billable resources and the expected lab duration.
6. State the teardown order and independent post-destroy checks.
7. Obtain explicit user confirmation in the current turn before creating paid resources.

Never run `apply` merely because a plan exists. Never run `destroy` without explicit confirmation and a data/backup review.

## Cost classes

Cost varies by region, usage, and date. Recheck official pricing at lesson time.

| Class | Typical examples | Course behavior |
|---|---|---|
| Local | kind, Docker, local Terraform exercises | Default daily learning environment |
| Low/variable | ECR storage/scanning, small S3 state bucket, Secrets Manager, CloudWatch data | Create only for a named lab; keep cleanup/lifecycle rules |
| Noticeable | EKS control plane, EC2 nodes, EBS, public IPv4, load balancer | Short milestone lab; apply and destroy in the same session |
| High-risk for accidental spend | NAT Gateway, RDS, multi-AZ resources, extended-support/long-lived EKS, high-volume logs/data transfer | Plan-only by default; require a specific cost review and explicit approval |

A VPC or IAM object may have no direct hourly price, but components attached to it can. “Free resource” never means “the architecture is free.”

## Terraform safety

- Begin with local state only to learn what state does.
- For shared/cloud work, bootstrap an S3 backend separately with bucket versioning, encryption, public-access blocking, least privilege, and `use_lockfile = true`.
- Do not create a new DynamoDB lock table for Terraform state; that locking approach is deprecated.
- Treat state and plan files as sensitive because they can contain values marked sensitive in normal output.
- Commit `.terraform.lock.hcl`; do not commit `.tfstate`, `.terraform/`, saved plans, or variable files containing secrets.
- Separate foundational platform state (network/EKS/IAM) from application configuration and managed data with different lifecycles.
- Review resource replacement and deletion before apply. A syntactically valid plan can still be operationally wrong.

## EKS lab rules

- Use kind for daily Kubernetes practice.
- Start with a managed node group so the learner sees control plane versus nodes and capacity.
- Select a currently supported EKS/Kubernetes version at lab time.
- Use Access Entries for human cluster access and keep a tested recovery/admin path.
- Use Pod Identity for supported application access; do not grant app permissions through the node role.
- Use immutable image tags/digests.
- Delete application load balancers/Services/Ingress or Gateway resources before deleting the cluster.
- Destroy short-lived EKS labs the same session unless the user explicitly chooses otherwise after a cost review.

## Independent teardown proof

`terraform destroy` succeeding is useful evidence, not the only evidence. Check that the intended environment no longer has:

- EKS clusters or managed node groups;
- load balancers and target groups;
- NAT gateways, unattached Elastic IPs, and unexpected ENIs;
- EC2 instances and unexpected EBS volumes/snapshots;
- RDS instances/clusters and retained snapshots;
- temporary Secrets Manager secrets, CloudWatch log groups, or other intentionally short-lived resources;
- Terraform-managed resources still listed in the relevant state.

Retain shared bootstrap resources such as the state bucket only when the lesson explicitly says they are persistent and their continuing cost is understood.

Official references:

- [AWS IAM security best practices](https://docs.aws.amazon.com/IAM/latest/UserGuide/best-practices.html)
- [AWS CLI IAM Identity Center configuration](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-sso.html)
- [AWS Budgets best practices](https://docs.aws.amazon.com/cost-management/latest/userguide/budgets-best-practices.html)
- [Cost Anomaly Detection](https://docs.aws.amazon.com/cost-management/latest/userguide/getting-started-ad.html)
- [Amazon EKS pricing](https://aws.amazon.com/eks/pricing/)
- [Terraform S3 backend](https://developer.hashicorp.com/terraform/language/backend/s3)
- [Terraform sensitive data](https://developer.hashicorp.com/terraform/language/manage-sensitive-data)
- [Delete an EKS cluster](https://docs.aws.amazon.com/eks/latest/userguide/delete-cluster.html)
