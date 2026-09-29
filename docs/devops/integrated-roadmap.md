# Integrated Kubernetes, Terraform, AWS, and DevOps Roadmap

## Goal

Learn to take one Spring Boot application from source code to a secure, observable, recoverable Kubernetes deployment on AWS—and understand why every layer exists.

This roadmap uses a spiral. The same Instagram backend is revisited at increasing depth:

1. run it as a process;
2. package it as an image;
3. run and repair it with local Kubernetes;
4. describe its cloud infrastructure with Terraform;
5. run it briefly on EKS;
6. automate delivery, observation, security, recovery, and cleanup.

## The four learning lanes

| Lane | Question it answers | Project examples |
|---|---|---|
| Kubernetes | How should the application run and recover? | Pods, Deployments, Services, probes, scaling, configuration |
| Terraform | How do we describe and repeat infrastructure changes safely? | ECR, VPC, IAM, EKS, S3, RDS |
| AWS | What real cloud services and responsibility boundaries are involved? | Regions, networking, identity, registry, cluster, database, storage |
| DevOps practices | How do changes move safely and how do we know the system works? | CI, GitOps, scanning, observability, policy, rollback, cost |

Only one lane provides the primary idea in a micro-lesson. The other lanes provide a small connection or wait until a later part.

## Phase 0 — Readiness and safety

### Problems solved

- The learner may have completed commands without a stable mental model.
- A cloud experiment can create real cost or expose credentials.
- Terraform can change many resources, so its preview/state model must be understood first.

### Outcomes

- Diagnose gaps in container and Kubernetes basics without rereading every lesson.
- Distinguish source, process, image, container, Pod, node, and cluster.
- Understand Terraform configuration, provider, plan, apply, and state locally.
- Set up AWS human access with temporary credentials, a chosen region, a budget, anomaly detection, and tagging rules.
- Create no paid AWS infrastructure.

## Phase 1 — Reliable local platform

### Problems solved

- One manually started container is not self-healing or safely reachable.
- Application configuration, database state, and health need different handling.
- Cloud vocabulary is hard to learn without mapping it to something already visible locally.

### Outcomes

- Run the backend through a Deployment and stable Service.
- Externalize configuration; distinguish Kubernetes Secrets from a complete secret-management solution.
- Practice PostgreSQL persistence and decide when EBS, S3, or RDS fits.
- Add probes, requests, limits, and failure recovery.
- Learn Terraform dependency graphs, variables, outputs, modules, state, and the AWS provider in small steps.

## Phase 2 — Repeatable delivery

### Problems solved

- Manually edited YAML drifts between environments.
- A build is not trustworthy merely because it compiled once.
- Static cloud credentials in CI become long-lived secrets.
- A deployment needs a safe, observable path from Git to the cluster.

### Outcomes

- Learn traffic, DNS, and TLS locally before paying for a load balancer.
- Use Kustomize for environment overlays, then Helm for third-party packages and reusable packaging.
- Use GitHub Actions for test, build, scan, and publication.
- Use AWS OIDC for short-lived CI credentials.
- Publish an immutable ECR image tag and record its digest.
- Scan source, configuration, and images with Trivy and produce an SBOM.

## Phase 3 — Short-lived AWS and EKS labs

### Problems solved

- Local Kubernetes hides AWS networking, identity, cost, and managed-service boundaries.
- A first EKS cluster can be expensive and easy to leave running.
- Human and workload access to AWS need separate least-privilege identities.

### Outcomes

- Build ECR, the network, IAM roles, and a first EKS cluster with Terraform.
- Use a managed node group first so control plane, nodes, EC2 capacity, and scheduling remain visible.
- Use EKS Access Entries for human access and EKS Pod Identity for supported workload access.
- Deploy native Kubernetes resources before adding GitOps abstraction.
- Use RDS, S3, and Secrets Manager as production-boundary exercises, plan-only unless a paid lab is explicitly approved.
- Destroy the EKS environment the same session and verify no billable leftovers.

## Phase 4 — Modern operations and capstone

### Problems solved

- CI pushing directly into a cluster gives the pipeline broad access and weak drift control.
- Logs alone cannot explain every distributed failure.
- Security, scaling, and cost decisions need enforceable evidence.

### Outcomes

- Use Argo CD to reconcile application state from Git.
- Use Prometheus/Grafana for metrics and alerts, then OpenTelemetry for vendor-neutral telemetry generation and transport.
- Add image signing/attestation and policy enforcement only after scanning and admission concepts are clear.
- Compare managed node groups with EKS Auto Mode/Karpenter after scheduling and HPA are understood.
- Use OpenCost after real metrics and AWS runtime data exist.
- Complete a capstone: change, test, scan, publish, deploy, observe, break, recover, roll back, and tear down.

## Understanding gates

Project status and learner status are independent.

For a concept to reach `DEMONSTRATED`, the learner must:

1. explain the problem and mechanism in simple words;
2. predict one result before executing it;
3. diagnose one controlled failure from status, events, logs, metrics, a Terraform plan, or AWS evidence.

Recheck the key idea in a later session before changing it to `RETAINED`.
