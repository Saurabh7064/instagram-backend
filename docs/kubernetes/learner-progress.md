# Kubernetes Learner Progress

This tracker records demonstrated understanding, not whether project code was already implemented.

## Status legend

- `NOT_ASSESSED` — the material exists, but understanding has not been checked in conversation.
- `LEARNING` — the learner has started the part and still has an unresolved checkpoint.
- `UNDERSTOOD` — the learner explained the problem in plain language, predicted one behavior, and answered the part's checkpoint.

Never mark a part `UNDERSTOOD` merely because commands succeeded or the module backlog says `DONE`.

## Module 01 — Containers

| Part | Topic | Status | Evidence of understanding |
|---|---|---|---|
| [01A](./01-containerize-spring-boot-backend/01a-from-code-to-running-program.md) | Source, JAR, JVM, and process | NOT_ASSESSED |  |
| [01B](./01-containerize-spring-boot-backend/01b-image-container-and-dockerfile.md) | Image, container, and Dockerfile | NOT_ASSESSED |  |
| [01C](./01-containerize-spring-boot-backend/01c-build-and-run-the-first-container.md) | Build and run an image | NOT_ASSESSED |  |
| [01D](./01-containerize-spring-boot-backend/01d-read-the-multi-stage-dockerfile.md) | Multi-stage Dockerfile | NOT_ASSESSED |  |
| [01E](./01-containerize-spring-boot-backend/01e-container-networking-and-configuration.md) | Networking and runtime configuration | NOT_ASSESSED |  |
| [01F](./01-containerize-spring-boot-backend/01f-security-storage-and-recovery.md) | Security, storage, and recovery | NOT_ASSESSED |  |

## Module 02 — Kubernetes primitives

| Part | Topic | Status | Evidence of understanding |
|---|---|---|---|
| [02A](./02-local-cluster-kubectl-pods-labels/02a-why-kubernetes-cluster-node.md) | Why Kubernetes; cluster and node | LEARNING | 2026-09-30: learner started the part and correctly identified that node inspection assumed an uncreated cluster. Lesson setup order was corrected; conceptual checkpoint not attempted yet. |
| [02B](./02-local-cluster-kubectl-pods-labels/02b-control-plane-request-journey.md) | Control-plane request journey | NOT_ASSESSED |  |
| [02C](./02-local-cluster-kubectl-pods-labels/02c-kubectl-kubeconfig-context.md) | kubectl, kubeconfig, and context | NOT_ASSESSED |  |
| [02D](./02-local-cluster-kubectl-pods-labels/02d-desired-state-objects-manifests.md) | Desired state, objects, and manifests | NOT_ASSESSED |  |
| [02E](./02-local-cluster-kubectl-pods-labels/02e-pods-lifecycle.md) | Pods and lifecycle | NOT_ASSESSED |  |
| [02F](./02-local-cluster-kubectl-pods-labels/02f-namespaces-labels-selectors.md) | Namespaces, labels, and selectors | NOT_ASSESSED |  |
| [02G](./02-local-cluster-kubectl-pods-labels/02g-observe-break-recover.md) | Observe, diagnose, and recover | NOT_ASSESSED |  |

## Current recommended starting point

Start with [01A](./01-containerize-spring-boot-backend/01a-from-code-to-running-program.md). Even if the container lab has already been completed, confirm the source → JAR → JVM → process distinction before moving into image and container terminology.
