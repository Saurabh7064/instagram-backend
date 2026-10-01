# 02 - Local Cluster, kubectl, Pods, Namespaces, Labels, and Selectors

## Start here

This module assumes only one container idea from Module 01: an image is a stored template and a container is a running instance. If that distinction is unclear, read [01B](./01-containerize-spring-boot-backend/01b-image-container-and-dockerfile.md) first.

No Kubernetes, YAML, cloud, or cluster knowledge is assumed. YAML structure, cluster vocabulary, safe local-cluster creation, command targeting, Pods, namespaces, labels, and troubleshooting are introduced in separate parts. The 02A exercise checks that Docker and kind are available, then creates or safely reuses the lesson cluster before inspecting it.

This module does **not** deploy the Instagram backend yet. It uses one simple NGINX Pod so database configuration cannot hide the Kubernetes fundamentals. The backend Deployment and Service belong to Module 03.

Complete one 10–15 minute part and stop at its readiness check. Do not read all seven parts in one sitting unless you are reviewing material you already understand.

## Why this module exists

Starting one container by hand is manageable. Coordinating many containers across machines—placing them, observing failures, organizing them, and restoring declared state—requires a system around the container runtime.

Kubernetes provides that system. Before using higher-level resources such as Deployments and Services, you need a small mental model of the cluster and its most basic workload object: the Pod.

## Core micro-lesson path

| Part | Learn one thing | Time | Start when | Ready when |
|---|---|---:|---|---|
| [02A](./02-local-cluster-kubectl-pods-labels/02a-why-kubernetes-cluster-node.md) | Why Kubernetes exists; cluster versus node; create or reuse the local cluster | 10–15 min plus cluster startup | Image/container distinction is clear | You can explain the two terms and inspect one node only after its cluster exists |
| [02B](./02-local-cluster-kubectl-pods-labels/02b-control-plane-request-journey.md) | Who accepts, schedules, and starts work | 10–15 min | 02A is clear | You can retell the request path without saying kubectl starts containers |
| [02C](./02-local-cluster-kubectl-pods-labels/02c-kubectl-kubeconfig-context.md) | Safe kubectl targeting | 10–15 min | 02B is clear | You verify the local context before mutation |
| [02D](./02-local-cluster-kubectl-pods-labels/02d-desired-state-objects-manifests.md) | Object, manifest, spec, status, reconciliation | 10–15 min | 02B and 02C are clear | You can separate what you request from what Kubernetes observes |
| [02E](./02-local-cluster-kubectl-pods-labels/02e-pods-lifecycle.md) | Pod and container lifecycle | 10–15 min | 02D is clear | You can distinguish Running, Ready, restart, and replacement |
| [02F](./02-local-cluster-kubectl-pods-labels/02f-namespaces-labels-selectors.md) | Scope and grouping | 10–15 min | 02D and 02E are clear | You can predict which Pods a selector matches |
| [02G](./02-local-cluster-kubectl-pods-labels/02g-observe-break-recover.md) | Observe, break, diagnose, and recover | 10–15 min | 02E and 02F are clear | You choose the right evidence for an image-pull failure |

## Core path versus optional reference

- **Core path:** the seven micro-lessons above.
- **Optional full reference:** [original detailed Lesson 02](./reference/02-local-cluster-kubectl-pods-labels-full-reference.md). Use it for lookup or cumulative review after the micro-lessons.
- **Project artifacts:** [kind cluster configuration](../../k8s/learning/02-primitives/kind-cluster.yaml), [namespace manifest](../../k8s/learning/02-primitives/namespace.yaml), and [Pod manifest](../../k8s/learning/02-primitives/pod.yaml).

## One-page recap

```text
kubectl
  |
  | reads kubeconfig/context and calls HTTPS API
  v
API server stores the requested object
  |
  v
scheduler chooses a node
  |
  v
kubelet asks the container runtime to start the image
  |
  v
Pod status and events report what actually happened
```

Keep these distinctions:

- A cluster is the whole Kubernetes environment; a node is one machine-like member.
- kubectl is an API client; it does not directly start containers on a node.
- A manifest expresses desired object fields; `status` reports observed state.
- A Pod wraps one or more containers in one scheduling and network context.
- `Running` does not guarantee useful application behavior.
- A namespace scopes names and grouping; it is not automatically a security boundary.
- Labels describe objects; selectors find matching objects; selectors alone do not route traffic.
- A bare Pod has no higher-level controller to recreate it after deletion.

## Vocabulary cheat sheet

| Term | Plain meaning | Project example | Do not confuse it with |
|---|---|---|---|
| Cluster | Whole Kubernetes environment | `instagram-learning` kind cluster | A single node |
| Node | Machine-like worker inside a cluster | `instagram-learning-control-plane` | The cluster or a Pod |
| API server | Front door for Kubernetes object requests | Target contacted by kubectl | The container runtime |
| Context | Selected cluster, user, and optional namespace | `kind-instagram-learning` | Terminal directory |
| Manifest | YAML/JSON declaration sent to the API | [pod.yaml](../../k8s/learning/02-primitives/pod.yaml) | Live status |
| Pod | Smallest schedulable workload wrapper | `hello-kubernetes` | Exactly one container or a VM |
| Namespace | Name and policy scope inside one cluster | `instagram-learning` | A separate cluster |
| Label | Selectable key/value metadata | `track: stable` | A unique object identity |
| Selector | Query that matches labels | `track=stable` | A network router |
| Event | Recorded cluster action or failure clue | Failed image pull message | Application log output |

## Integrated module practice

The practical work is distributed so every command has a concept behind it:

1. Check for, create or reuse, and then inspect the cluster and node in [02A](./02-local-cluster-kubectl-pods-labels/02a-why-kubernetes-cluster-node.md).
2. Verify the isolated kubeconfig and context in [02C](./02-local-cluster-kubectl-pods-labels/02c-kubectl-kubeconfig-context.md).
3. Read and apply the manifests in [02D](./02-local-cluster-kubectl-pods-labels/02d-desired-state-objects-manifests.md).
4. Verify Pod readiness and HTTP behavior in [02E](./02-local-cluster-kubectl-pods-labels/02e-pods-lifecycle.md).
5. Change and query labels in [02F](./02-local-cluster-kubectl-pods-labels/02f-namespaces-labels-selectors.md).
6. Create `ImagePullBackOff`, diagnose it, recover, and prove bare-Pod deletion behavior in [02G](./02-local-cluster-kubectl-pods-labels/02g-observe-break-recover.md).

Verified command evidence remains in the [full reference](./reference/02-local-cluster-kubectl-pods-labels-full-reference.md#evidence).

## Teach it back

After 02G, explain:

> How does kubectl target the correct cluster, what happens after a Pod manifest reaches the API server, and which evidence would you inspect if its container never starts?

If you say kubectl starts the container directly, revisit 02B. If context targeting is uncertain, revisit 02C before running any mutating command.

## Evidence and commit pointers

- `fa12b65` added the isolated kind cluster, namespace and Pod manifests, original detailed lesson, selector exercise, controlled image failure, recovery, and bare-Pod evidence.
- `b83c4d3` added the original commit mapping.
- `7f1ace1` split the module into seven beginner micro-lessons and preserved the original chapter as optional reference material.
- The local `instagram-learning` cluster and lesson Pod were verified before this readability refactor.

## What comes next

Module `K8S-003` replaces the deliberately fragile bare Pod with a Deployment and exposes changing backend Pods through a stable Service. Begin it only when Pod/container and label/selector distinctions are clear.
