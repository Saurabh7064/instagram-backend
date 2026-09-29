# D00 — Readiness and Safety

## Start here

This module does not assume that the earlier container or Kubernetes labs were understood. It checks the foundation gently and teaches the first Terraform/AWS safety ideas from zero.

Do one row at a time. Do not read the whole module in one sitting.

## Problem this module solves

Kubernetes, Terraform, and AWS use words that depend on earlier ideas. If “process,” “image,” “desired state,” “identity,” or “state” is unclear, later commands may work while the system still feels confusing.

Cloud commands can also create real resources and charges. We need a safe preview-and-evidence habit before using them.

## Micro-lesson path

| Part | One idea | Time | Assumes | Ready when |
|---|---|---:|---|---|
| D00A | The one-system map: who does what? | 10–15 min | Nothing | Explain the separate jobs of Kubernetes, Terraform, AWS, CI, and GitOps |
| D00B | From source code to a running Java process | 10–15 min | D00A | Put source, JAR, JVM, and process in order |
| D00C | From image to container, Pod, and node | 10–15 min | D00B | Explain which thing stores instructions and which things are running |
| D00D | Terraform as previewable infrastructure instructions | 10–15 min | D00A | Explain configuration, provider, plan, apply, and state without running AWS changes |
| D00E | AWS account, human identity, region, and cost guardrail | 10–15 min | D00A | Identify the account/role/region and explain why a budget is an alert, not a hard cap |
| D00F | Baseline checkpoint and personal route | 10–15 min | D00B–D00E | Predict three outcomes and identify only the prerequisite parts that need review |

Every part will contain 3–6 questions. Each question is an expandable block whose explained answer stays inside it, so answers are never omitted and are not visible before you choose to reveal them.

## Existing material used only when needed

- If source/JAR/JVM/process is unclear: [Kubernetes 01A](../kubernetes/01-containerize-spring-boot-backend/01a-from-code-to-running-program.md).
- If image/container/Dockerfile is unclear: [Kubernetes 01B](../kubernetes/01-containerize-spring-boot-backend/01b-image-container-and-dockerfile.md).
- If cluster/node is unclear: [Kubernetes 02A](../kubernetes/02-local-cluster-kubectl-pods-labels/02a-why-kubernetes-cluster-node.md).
- If desired state is unclear: [Kubernetes 02D](../kubernetes/02-local-cluster-kubectl-pods-labels/02d-desired-state-objects-manifests.md).

These are targeted resets, not a request to reread both modules.

## What this module intentionally postpones

- Terraform syntax beyond a tiny local example;
- creation of AWS resources;
- EKS, VPC subnet design, and Kubernetes deployment YAML;
- CI/CD configuration;
- advanced or optional tools.

## Stop/go rule

Continue to D01 only after the learner has attempted the D00 checkpoints. A vague or partly correct answer changes the relevant part to `LEARNING`; it does not fail the learner and it does not mark the whole module understood.

Say `teach D00A` to begin with only the one-system map.
