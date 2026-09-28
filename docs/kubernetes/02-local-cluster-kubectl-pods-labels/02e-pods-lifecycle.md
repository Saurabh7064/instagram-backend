# 02E — Pods and Their Lifecycle

**Time:** 10–15 minutes
**Prerequisite:** [02D — Desired State, Objects, and Manifests](./02d-desired-state-objects-manifests.md)

## One thing you will learn

You will understand what a Pod adds around a container and why `Running` is not enough evidence that an application works.

## Assumptions

You know what a container is, can identify `spec` as desired state, and have applied the lesson manifest. You are not expected to know Deployments or Services.

## Vocabulary budget

This micro-lesson introduces five terms:

| Term | Plain meaning |
|---|---|
| Pod | Kubernetes' smallest schedulable workload object |
| phase | A broad lifecycle category such as `Pending` or `Running` |
| readiness | Whether a container is considered able to receive useful work |
| restart | Starting a container again inside the same Pod |
| replacement | Creating a new Pod object after an old one is removed |

## The problem first

Kubernetes needs a unit it can place on a node and observe as one lifecycle. A raw container is too narrow because some applications need tightly coupled helper containers, shared networking, or shared declared storage.

Kubernetes therefore schedules **Pods**, not individual containers.

## Must understand

### A Pod wraps one or more containers

A **Pod** is the smallest workload object that Kubernetes schedules. Most application Pods contain one main container, which is the easiest case to picture.

Containers in one Pod are placed together and share one network identity. They can communicate through `localhost` and can share declared volumes.

```text
Pod: hello-kubernetes
├── one Pod IP
└── container: web (NGINX)
```

A Pod is not another word for container, because a Pod can contain more than one container and owns the shared context around them. It is also not a small virtual machine; its containers still share the node's operating-system kernel.

### Phase is a broad lifecycle summary

A Pod **phase** gives a high-level category:

- `Pending`: accepted, but not all containers are running yet;
- `Running`: assigned to a node, with at least one container running, starting, or restarting;
- `Succeeded` or `Failed`: completed work ended successfully or unsuccessfully;
- `Unknown`: Kubernetes could not obtain the state.

`Running` does not mean “the Instagram product works.” A process can be running while returning errors.

### Readiness is a separate question

The `READY` column might show `1/1`: one ready container out of one declared container. Later you will add an application-aware readiness check. Without that check, basic container readiness is weaker evidence than a real HTTP request.

### Restart is not replacement

If a process exits and the Pod configuration permits it, the kubelet can **restart** the container inside the same Pod. The Pod identity remains and the restart count increases.

A **replacement** is a new Pod object, commonly created by a higher-level resource introduced in the next module. A replacement can have a new identity and network address.

### Mental model: a reusable envelope

Think of a Pod as an addressed envelope holding one or more closely related containers. Kubernetes places the whole envelope on one node.

The analogy stops when discussing restarts: Kubernetes can restart a container inside the existing Pod environment, while a physical envelope cannot restart its contents.

## Instagram-project example

The current [pod.yaml](/Users/saurabh/Documents/Learning/instagram-backend/k8s/learning/02-primitives/pod.yaml) uses NGINX so the Pod lifecycle remains visible without PostgreSQL configuration.

The actual backend listens on port `8080` and depends on settings in [application.properties](/Users/saurabh/Documents/Learning/instagram-backend/src/main/resources/application.properties). Putting that backend in a Pod will not automatically create its database or correct configuration. Those concerns are deliberately separated into later lessons.

## Useful later

Pods are intended to be replaceable. Application code should not assume that a Pod name, Pod IP, or files written only inside a container will remain forever. The project's current local media behavior will need shared object storage before safe multi-replica production use.

<details>
<summary>Optional deep dive</summary>

Multi-container Pods are appropriate when containers are tightly coupled and genuinely need the same network and lifecycle. They are not a general replacement for running separate application services.

</details>

## Tiny exercise

### Predict

If the Pod shows `Running` and `1/1`, will that alone prove NGINX returns an HTTP page?

### Try it

First inspect the summary:

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  get pod hello-kubernetes \
  -n instagram-learning
```

Then make an HTTP request from inside the Pod:

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  exec -n instagram-learning hello-kubernetes -- \
  wget -qO- http://127.0.0.1
```

### Expected result

The summary should eventually show `Running` and `1/1`. The second command should return NGINX welcome-page HTML.

### Why this result occurs

The first command reports lifecycle information. The second command asks the running container to perform a real HTTP request through the Pod's own network. That request provides stronger evidence that a web process is listening and responding.

## Pause and check

<details>
<summary>1. Why is a Pod not exactly the same thing as a container?</summary>

A Pod is Kubernetes' schedulable wrapper and shared execution context. It can contain one or more containers that share networking and declared volumes. A container is one runtime unit inside that Pod.

</details>

<details>
<summary>2. Why is a Pod not a miniature virtual machine?</summary>

Its containers do not have an independent guest kernel like a typical virtual machine. They use operating-system isolation while sharing the node's kernel, and Pods are designed to be replaceable.

</details>

<details>
<summary>3. Does phase `Running` prove the application is healthy?</summary>

No. It is a broad lifecycle phase. The process can be starting, restarting, misconfigured, or returning errors. Readiness information and a useful application request provide stronger evidence.

</details>

<details>
<summary>4. What is the difference between restarting a container and replacing a Pod?</summary>

A restart occurs within the same Pod identity and increases the container restart count. Replacement creates a new Pod object, usually with a new generated identity and possibly a new network address.

</details>

<details>
<summary>5. Why does this first Pod use NGINX instead of the Instagram backend?</summary>

The backend also needs PostgreSQL, runtime configuration, project-image loading, and network access. NGINX keeps this exercise focused on the Pod itself so missing dependencies do not hide the lifecycle lesson.

</details>

## Stop/go check

Continue when you can:

- draw a Pod containing one container;
- explain why `Running` is not a product-level health guarantee;
- distinguish a container restart from a Pod replacement.

Next: [02F — Namespaces, Labels, and Selectors](./02f-namespaces-labels-selectors.md).
