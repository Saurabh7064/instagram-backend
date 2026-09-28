# 02D — Desired State, Objects, and Manifests

**Time:** 10–15 minutes  
**Prerequisite:** [02C — kubectl, Kubeconfig, and Context](./02c-kubectl-kubeconfig-context.md)

## One thing you will learn

You will understand how a YAML file expresses what you want and how Kubernetes reports what actually happened.

## Assumptions

You can identify the local context and know that kubectl sends requests to the API server. You do not need to know the details of Pods or namespaces yet.

## Vocabulary budget

This micro-lesson introduces five terms:

| Term | Plain meaning |
|---|---|
| object | A structured record stored through the Kubernetes API |
| manifest | A YAML or JSON file describing an object you want |
| desired state | What should be true |
| observed state | What Kubernetes currently sees |
| reconciliation | Repeated work to move reality toward the desired state |

## The problem first

A one-time command can start something, but it does not clearly record the intended result. If the environment changes, another person may not know what to restore.

Kubernetes uses durable **objects** to represent intent and observations. A version-controlled **manifest** lets the team review and repeat the intended configuration.

## Must understand

### Desired state says what should be true

The desired state is the request. For an application, it might say “use this image and expose this container port.” It does not claim that the request has already succeeded.

### Observed state says what is true now

Kubernetes reports what it currently sees: whether work was assigned, whether a container started, its address, and conditions such as readiness.

The difference matters:

```text
desired: use image nginx:1.27-alpine
observed: container is running and ready
```

Or during a failure:

```text
desired: use image nginx:tag-that-does-not-exist
observed: image cannot be pulled
```

### Reconciliation is a repeated comparison

**Reconciliation** means repeatedly comparing desired and observed state and taking action to reduce the difference. It is closer to a thermostat checking room temperature than to a script that runs once and forgets.

The analogy has a limit: Kubernetes has many independent components and many kinds of state, not one temperature and one switch.

### The four fields to recognize first

Open [pod.yaml](/Users/saurabh/Documents/Learning/instagram-backend/k8s/learning/02-primitives/pod.yaml). You do not need to understand a Pod yet. First recognize this shape:

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: hello-kubernetes
spec:
  containers:
    - name: web
      image: nginx:1.27-alpine
```

- `apiVersion` selects the API schema version.
- `kind` names the object type.
- `metadata` identifies and organizes the object.
- `spec` contains the requested desired state for this type.

Kubernetes normally writes `status` after observing the object. A committed manifest usually declares the request, not a copied status.

## Instagram-project example

The project keeps its learning intent in version-controlled files:

- [kind-cluster.yaml](/Users/saurabh/Documents/Learning/instagram-backend/k8s/learning/02-primitives/kind-cluster.yaml) describes the local kind cluster;
- [namespace.yaml](/Users/saurabh/Documents/Learning/instagram-backend/k8s/learning/02-primitives/namespace.yaml) describes the project grouping introduced in 02F;
- [pod.yaml](/Users/saurabh/Documents/Learning/instagram-backend/k8s/learning/02-primitives/pod.yaml) describes the small NGINX workload introduced in 02E.

Later manifests will refer to the Instagram image built from the [Dockerfile](/Users/saurabh/Documents/Learning/instagram-backend/Dockerfile).

## Useful later

`kubectl apply -f file.yaml` asks the API server to create the object or move its declared fields toward the file. Reapplying a stable manifest is safer and more repeatable than remembering a chain of manual edits.

<details>
<summary>Optional deep dive</summary>

Not every object has the same `spec` or `status` shape. The API version and kind determine the schema. Learn the common top-level pattern first; use documentation or API discovery for type-specific fields later.

</details>

## Tiny exercise

### Predict

In [pod.yaml](/Users/saurabh/Documents/Learning/instagram-backend/k8s/learning/02-primitives/pod.yaml), which field expresses the requested image: `metadata`, `spec`, or `status`?

Then predict whether applying a file proves the application is already working.

### Try it

First ensure the grouping object exists, then submit the workload manifest:

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  apply -f k8s/learning/02-primitives/namespace.yaml

kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  apply -f k8s/learning/02-primitives/pod.yaml
```

### Expected result

Each command should report `created`, `configured`, or `unchanged`. All three mean the API accepted the requested object state.

### Why this result occurs

The image appears under `spec`, so it is desired state. `apply` confirms the API operation, not application health. You must inspect observed state and make a useful request before claiming that the workload works.

## Pause and check

<details>
<summary>1. What is the difference between an object and a manifest?</summary>

An object is the structured API record in the cluster. A manifest is a file used to describe desired object data to the API. The file is not the running process and is not itself the live cluster object.

</details>

<details>
<summary>2. What is the difference between desired and observed state?</summary>

Desired state says what should be true, usually through `spec`. Observed state reports what Kubernetes currently sees, often through `status`, conditions, and events. They can differ while work is starting or failing.

</details>

<details>
<summary>3. What does reconciliation mean?</summary>

It is the repeated comparison of desired and observed state followed by action to reduce their difference. The repetition is important: Kubernetes does not merely issue one start command and forget the request.

</details>

<details>
<summary>4. What do `apiVersion`, `kind`, `metadata`, and `spec` tell you?</summary>

They identify the schema version, object type, object identity/organization, and type-specific desired state. Reading these four fields provides a reliable first pass through an unfamiliar manifest.

</details>

<details>
<summary>5. `kubectl apply` reports `configured`. Does that prove the application serves HTTP correctly?</summary>

No. It proves the API accepted a configuration change. The runtime work may still be pending or failing. Inspect status and events, then perform an application-level request for stronger evidence.

</details>

## Stop/go check

Continue when you can:

- point to desired state in the manifest;
- explain why API acceptance is not the same as application success;
- describe reconciliation without using the word “magic.”

Next: [02E — Pods and Their Lifecycle](./02e-pods-lifecycle.md).
