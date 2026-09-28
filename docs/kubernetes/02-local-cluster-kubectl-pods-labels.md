# 02 - Local Cluster, kubectl, Pods, Namespaces, Labels, and Selectors

## Goal

Lesson 01 produced a container image. This lesson creates the place where Kubernetes can run containers and teaches the vocabulary used to observe and organize them.

By the end, you should be able to:

- explain what a cluster, control plane, node, API server, scheduler, kubelet, Pod, and namespace are;
- explain how `kubectl`, kubeconfig files, and contexts work together;
- distinguish desired state in `spec` from observed state in `status`;
- read the essential fields in a Kubernetes YAML manifest;
- create and inspect a namespaced Pod;
- add and query labels with selectors;
- use `get`, `describe`, `logs`, `exec`, events, `apply`, and `delete` for different purposes;
- diagnose `ErrImagePull` and `ImagePullBackOff` from evidence;
- explain why a bare Pod is not recreated after deletion;
- connect these primitives to the Instagram backend that will be deployed next.

This lesson is intentionally detailed. Kubernetes becomes much easier once the object model and observation workflow are clear.

## In simple terms

Kubernetes is a system for declaring how containerized applications should run and continuously comparing that request with reality.

You do not normally tell it, “start this exact process on this exact machine and forget about it.” You create Kubernetes objects that describe intent. Kubernetes stores those objects and its control loops keep checking whether the cluster matches them.

For example, a Pod object can say:

> Run one NGINX container from this image, with these labels, in this namespace.

The cluster then schedules the Pod onto a node. The node pulls the image and starts the container. Kubernetes records what actually happened in the object's status and events.

In this lesson, you create a single-node local cluster using **kind**, which means “Kubernetes IN Docker.” The Kubernetes node itself runs as a Docker container on the laptop. Inside that node, Kubernetes runs the lesson Pod.

```text
Mac laptop
|
+-- Docker Desktop
    |
    +-- kind node container
        |
        +-- Kubernetes control plane
        +-- Kubernetes worker components
        +-- Pod: hello-kubernetes
            |
            +-- Container: NGINX
```

The kind node container is infrastructure for the local cluster. The NGINX container is the workload managed by Kubernetes. They are different levels of the stack.

## What problem does this solve?

Docker can start containers, but operating many application instances across machines raises additional questions:

- Which machine should run each container?
- What happens when a container or machine fails?
- How do clients find moving application instances?
- How do we group development, staging, and production resources?
- How do we update the desired configuration consistently?
- How do we inspect why the actual state differs from the desired state?

Kubernetes provides an API and controllers for answering those questions through declared objects and reconciliation.

This lesson does not solve all of them yet. A bare Pod gives us the smallest useful object for learning. Deployments, Services, probes, storage, and other resources build on the same object model in later lessons.

## Mental model: a thermostat, not a one-time script

A thermostat has:

- a desired temperature;
- a measured temperature;
- a control loop that acts on the difference.

Kubernetes has:

- a desired state, usually declared in an object's `spec`;
- an observed state, reported through `status` and events;
- controllers and node components that act to reduce the difference.

The analogy is useful because Kubernetes repeatedly observes and acts. It is not exact: there are many independent controllers, not one controller; some objects such as bare Pods have limited self-healing; and applications have more states than a single temperature.

## From image to orchestration

Lesson 01 separated these concepts:

```text
Dockerfile -> image -> running container
```

Kubernetes adds another layer:

```text
manifest -> Kubernetes object -> scheduler chooses node
         -> kubelet asks runtime to start container from image
         -> status and events report the outcome
```

Kubernetes does not normally build the Dockerfile. It consumes an image that already exists in a registry or has been loaded into a local cluster node.

## Cluster architecture in plain language

### Cluster

A **cluster** is the complete Kubernetes environment: control-plane components plus one or more nodes that can run workloads.

A cluster is not the same thing as a node. A production cluster often has several control-plane and worker nodes. This local kind cluster has one node that performs both roles for learning.

### Control plane

The **control plane** is the decision-making and state-management part of Kubernetes. Its important components include:

- **API server:** the front door for Kubernetes API requests;
- **etcd:** the durable key-value store holding cluster object data;
- **scheduler:** chooses a suitable node for newly created, unscheduled Pods;
- **controller manager:** runs control loops that reconcile many resource types.

You normally interact through the API server rather than talking directly to these components.

### Node

A **node** is a machine—physical or virtual—that runs Pods. In kind, a Docker container behaves as the Kubernetes node.

Important node-side components include:

- **kubelet:** watches for Pods assigned to its node and ensures their containers run;
- **container runtime:** pulls images and starts or stops containers;
- **networking components:** make Pod and cluster networking function.

### A request's journey

When you apply the Pod manifest:

```text
kubectl
  -> reads kubeconfig and selected context
  -> sends HTTPS request to API server
  -> API server validates and stores Pod object
  -> scheduler chooses a node
  -> node's kubelet notices the assignment
  -> container runtime pulls NGINX image
  -> runtime starts container
  -> kubelet reports Pod/container status
  -> kubectl reads status and events through API server
```

`kubectl` does not SSH into the node and start the container itself.

## kubectl, kubeconfig, and contexts

### kubectl

`kubectl` is the primary command-line client for the Kubernetes API. It translates commands such as `get`, `apply`, and `delete` into API requests.

### Kubeconfig

A **kubeconfig** file tells a Kubernetes client:

- which API servers are known;
- which credentials or users can authenticate;
- which cluster and user combinations are named contexts;
- which context is currently selected;
- optionally, which namespace should be the default.

Kubeconfig contains connection and authentication data. Do not commit it to this repository.

### Context

A **context** chooses a cluster, user, and optional default namespace. The same `kubectl delete pod ...` command can affect completely different infrastructure depending on the selected context.

This machine already had an old GKE context selected. To avoid touching a remote cluster, the lesson created kind with a separate kubeconfig:

```bash
kind create cluster \
  --name instagram-learning \
  --config k8s/learning/02-primitives/kind-cluster.yaml \
  --kubeconfig /tmp/instagram-learning-kubeconfig \
  --wait 120s
```

Every lab command uses that explicit file:

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig ...
```

This is deliberately repetitive. Safe targeting matters more than saving a few keystrokes.

Before a mutating command, verify:

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig config current-context
```

The expected value is `kind-instagram-learning`.

If the temporary file is removed while the kind cluster still exists, export it again:

```bash
kind export kubeconfig \
  --name instagram-learning \
  --kubeconfig /tmp/instagram-learning-kubeconfig
```

## Kubernetes objects and manifests

Kubernetes represents desired and observed state with API objects. A YAML **manifest** is a human-readable way to send the desired object to the API.

Most manifests contain four essential top-level fields:

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: hello-kubernetes
  namespace: instagram-learning
spec:
  containers:
    - name: web
      image: nginx:1.27-alpine
```

### `apiVersion`

This identifies the API group and version used for the resource schema. Core resources such as Pods and Namespaces use `v1`.

### `kind`

This identifies the object type, such as `Pod`, `Namespace`, `Service`, or `Deployment`.

### `metadata`

Metadata identifies and organizes the object. It can include name, namespace, labels, annotations, owner references, and a server-generated UID.

### `spec`

The spec describes the desired state supplied by the user. A Pod spec includes containers and their images.

### `status`

Status is normally written by Kubernetes after observing the object. It can include phase, conditions, assigned node, Pod IP, and container states. You do not normally declare status in the manifest.

```text
Manifest/spec: "I want nginx:1.27-alpine"
Status:        "The Pod is Running, Ready, and has IP 10.244.0.6"
Events:        "Scheduled, pulled image, created container, started container"
```

## Pod fundamentals

A **Pod** is Kubernetes' smallest deployable compute object. Kubernetes schedules Pods, not individual containers.

A Pod often contains one application container. It can contain multiple tightly coupled containers that need to share:

- one network namespace and Pod IP;
- `localhost` connectivity;
- declared volumes;
- one scheduling and lifecycle boundary.

A Pod is not exactly a container. It is the wrapper and shared execution context around one or more containers.

A Pod is also not a miniature virtual machine. Its containers still share the node kernel, and the Pod is intended to be replaceable.

### Pod phase versus readiness

Common Pod phases include `Pending`, `Running`, `Succeeded`, `Failed`, and `Unknown`.

`Running` means the Pod has been bound to a node and at least one container is running, starting, or restarting. It does not prove that the application is correct or ready to serve useful traffic.

The `READY` column reports ready containers, such as `1/1`. Later lessons add application-aware readiness probes. This lesson's NGINX container has no custom readiness probe, so its basic container readiness is a weaker signal than a product-level API check.

### Restart versus replacement

If a container process exits and the Pod's restart policy permits it, kubelet may restart a container inside the same Pod. The Pod name and UID stay the same, while the restart count increases.

If a Pod is deleted and a higher-level controller owns it, the controller can create a replacement Pod with a new UID. But this lesson creates a **bare Pod** with no Deployment or ReplicaSet. Deleting it removes the desired object, so nothing recreates it.

This distinction is central:

- kubelet can restart containers within an existing Pod;
- a controller can replace missing Pods that it owns;
- a bare Pod has no higher-level owner to recreate it after deletion.

## Namespaces

A **namespace** scopes and groups many Kubernetes resources inside one cluster. This lesson uses `instagram-learning` instead of putting work in `default`.

Names usually need to be unique only for the same resource kind inside one namespace. A Pod named `hello-kubernetes` can exist in two different namespaces.

Some objects are cluster-scoped rather than namespaced. Nodes, PersistentVolumes, and Namespace objects themselves are examples.

Discover the difference with:

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  api-resources --namespaced=true

kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  api-resources --namespaced=false
```

A namespace is not another cluster and is not automatically a complete security boundary. RBAC, network policies, quotas, and admission policies are separate mechanisms.

## Labels, annotations, and selectors

### Labels

Labels are small key/value attributes attached to objects. They describe dimensions used for organization and selection, such as application, tier, environment, or release track.

This Pod uses:

```yaml
labels:
  app.kubernetes.io/name: hello-kubernetes
  app.kubernetes.io/part-of: instagram-learning
  tier: learning
  track: stable
```

Labels do not need to be unique. Many Pods should share an application label when they belong to the same logical application.

### Selectors

A selector is a query over labels:

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  get pods -n instagram-learning \
  -l 'app.kubernetes.io/name=hello-kubernetes,tier=learning'
```

Comma-separated requirements are combined with logical AND: both must match.

Selectors are a foundational grouping mechanism. Later, a Service will use a selector to choose the Pods that receive traffic, and a Deployment will connect its desired replicas to a Pod template through labels and selectors.

A selector itself does not route traffic. It only identifies a set. A Service is the resource that uses a selector as part of network routing.

### Annotations

Annotations also store metadata, but they are intended for non-identifying information that should not be used for efficient selection—for example, a description, build URL, or tool-specific configuration.

Use labels when a system needs to group or select objects. Use annotations for supporting information.

## The observation toolbox

Different commands answer different questions.

| Command | Main question it answers |
|---|---|
| `kubectl get` | What objects exist and what is their summarized state? |
| `kubectl describe` | What detailed state, conditions, and recent events explain this object? |
| `kubectl logs` | What did the container process write to standard output/error? |
| `kubectl exec` | What happens when a command runs inside a current container? |
| `kubectl get events` | What chronological cluster actions and failures were recorded? |
| `kubectl apply` | Make the live object match the declared manifest fields. |
| `kubectl delete` | Remove an object and its declared desired state. |
| `kubectl wait` | Block until a specific status condition becomes true or times out. |

Do not substitute one signal for another. Logs can be empty during an image-pull failure because no container started. Events are then more useful. A Pod can be `Running` while its application returns errors, so a real request may be stronger evidence than `get` output.

## Mapping to the Instagram project

This lesson intentionally uses an NGINX Pod rather than the backend image.

The backend in [Dockerfile](/Users/saurabh/Documents/Learning/instagram-backend/Dockerfile) needs PostgreSQL and runtime configuration from [application.properties](/Users/saurabh/Documents/Learning/instagram-backend/src/main/resources/application.properties). Deploying it correctly introduces Deployments, Services, ConfigMaps, Secrets, and database networking. Mixing all of that into the first Pod exercise would hide the primitives being learned.

The mapping is still direct:

- the NGINX `image` field corresponds to the Instagram backend image in the next lesson;
- the Pod container port `80` will become backend port `8080`;
- the `instagram-learning` namespace will hold project resources;
- labels will connect future Deployments and Services to Pods;
- Compose DNS name `postgres` does not automatically exist in Kubernetes;
- the backend image exists on the laptop but must be loaded into kind or pushed to a registry before a kind node can use it;
- local uploaded media remains ephemeral and replica-local, as explained in Lesson 01.

## Hands-on exercise

### 0. Install the local tools and create the cluster

On macOS with Homebrew:

```bash
brew install kind kubernetes-cli
```

Docker Desktop must be running because kind uses Docker containers as local nodes.

Create the named cluster while keeping its credentials separate from the global kubeconfig:

```bash
kind create cluster \
  --name instagram-learning \
  --config k8s/learning/02-primitives/kind-cluster.yaml \
  --kubeconfig /tmp/instagram-learning-kubeconfig \
  --wait 120s
```

If kind reports that the cluster already exists, do not create a duplicate. Inspect it with `kind get clusters` and export its kubeconfig again if necessary.

### 1. Verify the target before changing anything

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  config current-context
```

Expected:

```text
kind-instagram-learning
```

Also inspect tool versions:

```bash
kind version
kubectl version --client -o yaml
```

The verified environment used kind `0.33.0`, Kubernetes `1.37.0`, and kubectl `1.37.1`.

Keep kubectl close to the server version. Kubernetes supports kubectl within one minor version older or newer than the API server. The machine's previous kubectl `1.25.4` was far too old for the new `1.37.0` cluster, so the compatible Homebrew client was installed before the lab.

### 2. Inspect the cluster and node

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig cluster-info
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig get nodes -o wide
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig get pods --all-namespaces
```

Predict first: how many nodes will a single-node kind cluster show, and why are there Pods before the lesson Pod exists?

The system Pods run DNS, networking, storage, the API server, scheduler, controller manager, and other cluster functions.

### 3. Apply the namespace and Pod

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  apply -f k8s/learning/02-primitives/namespace.yaml

kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  apply -f k8s/learning/02-primitives/pod.yaml

kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  wait --for=condition=Ready \
  pod/hello-kubernetes \
  -n instagram-learning \
  --timeout=120s
```

`apply` sends the declared resources to the API. `wait` watches status until the `Ready` condition becomes true or the timeout expires.

### 4. Observe from several angles

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  get pod hello-kubernetes \
  -n instagram-learning \
  -o wide \
  --show-labels

kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  describe pod hello-kubernetes \
  -n instagram-learning

kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  get events \
  -n instagram-learning \
  --sort-by=.metadata.creationTimestamp
```

Find the assigned node, Pod IP, image, phase, readiness, labels, and the Scheduled/Pulling/Pulled/Created/Started event sequence.

### 5. Prove the container serves content

Run a request inside the Pod's network namespace:

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  exec -n instagram-learning hello-kubernetes -- \
  wget -qO- http://127.0.0.1
```

Then inspect the NGINX access log:

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  logs -n instagram-learning hello-kubernetes --tail=10
```

The successful HTML response proves more than `Running`: a process is listening and serving HTTP inside the Pod.

### 6. Exercise labels and selectors

First query the declared labels:

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  get pods -n instagram-learning \
  -l 'app.kubernetes.io/name=hello-kubernetes,tier=learning'
```

Change one label imperatively:

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  label pod hello-kubernetes \
  -n instagram-learning \
  track=canary --overwrite
```

Compare these queries:

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  get pods -n instagram-learning -l track=stable

kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  get pods -n instagram-learning -l track=canary --show-labels
```

The stable selector returns nothing; the canary selector finds the Pod. Reapply the file:

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  apply -f k8s/learning/02-primitives/pod.yaml
```

Because the manifest declares `track: stable`, apply restores that declared label.

### 7. Break image resolution safely

Change the image to a tag that does not exist:

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  set image pod/hello-kubernetes \
  -n instagram-learning \
  web=nginx:this-tag-does-not-exist
```

Observe:

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  get pod hello-kubernetes -n instagram-learning -w
```

The observed transition was:

```text
Running -> ErrImagePull -> ImagePullBackOff
```

Press `Ctrl-C` to stop watching. Diagnose before fixing:

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  describe pod hello-kubernetes -n instagram-learning

kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  get events -n instagram-learning \
  --sort-by=.metadata.creationTimestamp
```

The recorded event explained that `nginx:this-tag-does-not-exist` could not be found. `logs` cannot explain a container that never started; status and events can.

Recover by reapplying the valid manifest:

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  apply -f k8s/learning/02-primitives/pod.yaml

kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  wait --for=condition=Ready \
  pod/hello-kubernetes \
  -n instagram-learning \
  --timeout=120s
```

### 8. Prove that a bare Pod is not recreated

Delete the Pod:

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  delete pod hello-kubernetes \
  -n instagram-learning \
  --wait=true

kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  get pods -n instagram-learning
```

After waiting five seconds, the verified result was:

```text
No resources found in instagram-learning namespace.
```

Nothing recreated it because it had no controller owner. Restore it for later exploration:

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  apply -f k8s/learning/02-primitives/pod.yaml

kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  wait --for=condition=Ready \
  pod/hello-kubernetes \
  -n instagram-learning \
  --timeout=120s
```

## Common pitfalls

### Wrong context

Symptom: resources are missing, authentication fails, or a command targets an unexpected cluster.

Check `kubectl config current-context` using the exact kubeconfig intended for the command. Never infer the target from the terminal directory.

### Wrong namespace

Symptom: `No resources found` even though the Pod exists.

The default namespace is usually `default`; this Pod is in `instagram-learning`. Add `-n instagram-learning` or inspect all namespaces with `-A`.

### Treating a Pod as a container

A Pod may contain multiple containers and owns the shared networking and volume context. Commands such as `logs` or `exec` may require `-c <container>` when several containers exist.

### Treating `Running` as proof of health

`Running` is a lifecycle phase, not a business-level guarantee. Check readiness, logs, events, and an actual request.

### Looking for logs during `ImagePullBackOff`

There may be no new container logs because the container never started. Use `describe` and events to see pull failures.

### Expecting a bare Pod to self-heal after deletion

Kubelet can restart a failed container within an existing Pod, but no Deployment or ReplicaSet recreates this bare Pod after its object is deleted.

### Assuming a laptop-local image exists in kind

The kind node has its own container runtime image store. A local Docker image must be loaded with `kind load docker-image ... --name instagram-learning` or pulled from a registry.

### Believing namespaces provide complete isolation

Namespaces scope names and many policies, but security and network isolation require explicit RBAC, NetworkPolicies, quotas, and other controls.

### Believing selectors route traffic

Selectors only identify matching objects. A Service later uses a selector and network machinery to route traffic.

### Editing live state and forgetting the manifest

An imperative label or image change can make live state drift from Git. Reapplying the manifest may restore declared fields. Version-controlled manifests are the durable learning artifact.

## Check your understanding

Try to answer each question in your own words before expanding it. Select a question to reveal its explanation.

### Part A — foundations

<details>
<summary>1. What is a Kubernetes cluster?</summary>

A cluster is the complete Kubernetes environment: its control plane plus one or more nodes and their networking and runtime components. It is the system within which Kubernetes objects and workloads exist.

</details>

<details>
<summary>2. What is the difference between a cluster and a node?</summary>

A cluster contains nodes. A node is one machine or machine-like worker that can run Pods; the cluster coordinates one or many nodes. This kind setup has one Docker container acting as both control-plane and worker node.

</details>

<details>
<summary>3. What is the control plane responsible for?</summary>

The control plane accepts desired state, stores cluster objects, schedules work, and runs control loops that reconcile actual state toward desired state.

</details>

<details>
<summary>4. What role does the API server play?</summary>

The API server is Kubernetes' front door. Clients and internal components read or change objects through its HTTP API; it validates requests and coordinates persistence of object state.

</details>

<details>
<summary>5. What does the scheduler decide?</summary>

The scheduler chooses a suitable node for a Pod that has not yet been assigned, considering constraints and available cluster information. It chooses placement; it does not itself start the container.

</details>

<details>
<summary>6. What does kubelet do?</summary>

Kubelet runs on a node. It watches Pods assigned to that node, asks the container runtime to start or stop containers, and reports observed Pod and container status.

</details>

<details>
<summary>7. Does `kubectl` normally start a container directly on a node?</summary>

No. `kubectl` sends an API request to the API server. The scheduler and node components perform placement and runtime work.

</details>

<details>
<summary>8. What is desired state?</summary>

Desired state is what the user or controller says should exist, commonly expressed in an object's `spec`—for example, a Pod using `nginx:1.27-alpine`.

</details>

<details>
<summary>9. What is observed state?</summary>

Observed state is what Kubernetes currently sees: assignment, phase, container state, IP, readiness, and other status information. Events add a history of notable actions and failures.

</details>

<details>
<summary>10. What does reconciliation mean?</summary>

Reconciliation is the repeated comparison of desired and observed state followed by action to reduce their difference. It is continuous control, not merely a one-time script.

</details>

<details>
<summary>11. What is a Kubernetes object?</summary>

A Kubernetes object is an API representation of intent or cluster state, such as a Pod, Namespace, Service, or Deployment. It has identity and structured fields stored through the API.

</details>

<details>
<summary>12. What is a manifest?</summary>

A manifest is a YAML or JSON document describing the object you want to submit. It is a version-controllable representation of desired configuration, not the running process itself.

</details>

### Part B — manifests, Pods, and lifecycle

<details>
<summary>13. What do `apiVersion`, `kind`, `metadata`, and `spec` mean?</summary>

`apiVersion` selects an API group/version; `kind` selects the resource type; `metadata` identifies and organizes it; `spec` describes user-requested desired state specific to that type.

</details>

<details>
<summary>14. Who normally writes an object's `status`?</summary>

Kubernetes components normally populate and update `status` after observing the resource. Users declare `spec`; the platform reports status.

</details>

<details>
<summary>15. What is a Pod?</summary>

A Pod is Kubernetes' smallest schedulable workload object. It wraps one or more tightly coupled containers in a shared execution context.

</details>

<details>
<summary>16. Why is a Pod not exactly the same as a container?</summary>

A Pod may have several containers and owns their shared network identity, lifecycle placement, and declared volumes. A container is one process-isolation unit inside that Pod.

</details>

<details>
<summary>17. Why is a Pod not a miniature virtual machine?</summary>

A Pod does not include a complete guest operating system or kernel. Its containers share the node kernel and are designed to be replaceable.

</details>

<details>
<summary>18. What can containers in the same Pod share?</summary>

Containers in one Pod can share the Pod IP and network namespace, communicate through `localhost`, and mount declared shared volumes. They are also scheduled together.

</details>

<details>
<summary>19. Does Pod phase `Running` prove that an application is healthy?</summary>

No. `Running` is a broad lifecycle phase. The process may be starting, restarting, misconfigured, or returning failures. Readiness signals and useful application requests provide stronger evidence.

</details>

<details>
<summary>20. What is the difference between restarting a container and replacing a Pod?</summary>

A container restart occurs inside the same Pod; the Pod name and UID remain and restart count rises. Pod replacement creates a new Pod object/runtime identity, usually with a new UID and often a different IP.

</details>

<details>
<summary>21. Why did the lesson Pod stay deleted instead of returning automatically?</summary>

It was a bare Pod with no owning controller. Deleting the API object removed the desired state, so there was no Deployment or ReplicaSet asking Kubernetes to create a replacement.

</details>

<details>
<summary>22. What changes when the deleted Pod is recreated from the same manifest?</summary>

It receives a new server-generated UID and may receive a different Pod IP, even if its human-readable name and spec are identical. It is a new object instance.

</details>

### Part C — kubeconfig, namespaces, labels, and selectors

<details>
<summary>23. What information does kubeconfig contain?</summary>

Kubeconfig stores known clusters/API endpoints, user authentication information, contexts pairing users with clusters, the selected context, and optionally a default namespace.

</details>

<details>
<summary>24. What is a context?</summary>

A context selects one cluster, one user identity, and optionally one namespace. It determines the target and credentials used by a kubectl request.

</details>

<details>
<summary>25. Why did this lesson use a separate kubeconfig file?</summary>

The machine's default context pointed at an old remote GKE cluster. A separate file guarantees that lesson commands target only the local kind cluster without changing or relying on the user's global selection.

</details>

<details>
<summary>26. Does changing directories in the terminal change the kubectl context?</summary>

No. The current working directory does not select a Kubernetes cluster. Kubeconfig, context, command flags, and environment configuration do.

</details>

<details>
<summary>27. What problem does a namespace solve?</summary>

A namespace groups and scopes many resources within one cluster, allowing repeated names and attaching quotas, access rules, and policies to logical groups.

</details>

<details>
<summary>28. Can two namespaces each contain a Pod named `hello-kubernetes`?</summary>

Yes. Names must be unique for a given resource kind inside a namespace, not usually across the whole cluster.

</details>

<details>
<summary>29. Are Nodes namespaced resources?</summary>

No. Nodes are cluster-scoped. `kubectl api-resources --namespaced=false` reveals other cluster-scoped types.

</details>

<details>
<summary>30. Is a namespace automatically a complete security boundary?</summary>

No. Namespaces establish scope and grouping. Effective isolation also requires mechanisms such as RBAC, NetworkPolicies, quotas, admission rules, and careful credential handling.

</details>

<details>
<summary>31. What is a label?</summary>

A label is a key/value attribute intended to identify an object's meaningful dimensions so clients and Kubernetes resources can group and select it.

</details>

<details>
<summary>32. Must labels be unique?</summary>

No. Many objects commonly share a label such as `app=instagram-backend`. Labels are deliberately non-unique grouping metadata.

</details>

<details>
<summary>33. What does a label selector do?</summary>

A selector filters objects by their labels. It may match zero, one, or many objects.

</details>

<details>
<summary>34. In `app=demo,tier=web`, do both conditions need to match?</summary>

Both must match. Comma-separated selector requirements are combined with logical AND.

</details>

<details>
<summary>35. What is the difference between a label and an annotation?</summary>

Labels are designed for grouping and selection. Annotations store non-identifying supporting metadata that is not intended for selector queries.

</details>

<details>
<summary>36. Does a selector itself send network traffic to Pods?</summary>

No. It only identifies a set of objects. A Service can use a selector as part of its networking behavior, but the selector alone is not a router.

</details>

### Part D — commands and evidence

<details>
<summary>37. What question does `kubectl get` answer best?</summary>

`get` best answers which objects exist and what their summarized current state is. Output formats can reveal more fields, but its default view is an inventory/status overview.

</details>

<details>
<summary>38. When is `kubectl describe` more useful than `get`?</summary>

`describe` is useful when you need details such as node assignment, container state/reason, conditions, mounts, and recent events explaining why an object is not behaving as expected.

</details>

<details>
<summary>39. When are container logs useful?</summary>

Logs are useful after a container has started and its process writes diagnostics or request records to standard output/error. They help explain application-level startup and runtime behavior.

</details>

<details>
<summary>40. Why might logs be unavailable during `ImagePullBackOff`?</summary>

If the image cannot be pulled, the new container never starts, so it produces no logs. Pod status, `describe`, and events show the registry/image resolution failure instead.

</details>

<details>
<summary>41. What does `kubectl exec` do?</summary>

`exec` asks Kubernetes to run a command inside an existing container. It is useful for targeted inspection, but changes made interactively are not a durable substitute for manifests or rebuilt images.

</details>

<details>
<summary>42. What does `kubectl apply` do conceptually?</summary>

`apply` submits desired fields from a manifest and reconciles the live object configuration toward them. Reapplying the same intended configuration is repeatable.

</details>

<details>
<summary>43. Why use `kubectl wait` after applying a Pod?</summary>

API acceptance does not mean the container is ready. `wait` observes a specific condition until success or timeout, making the expected state explicit.

</details>

<details>
<summary>44. What does `kubectl delete` remove: only a running process, or the API object representing desired state?</summary>

`delete` removes the Kubernetes API object representing desired state. Kubernetes then stops associated runtime containers. Whether a replacement appears depends on an owning controller.

</details>

### Part E — predict and troubleshoot

<details>
<summary>45. You run `kubectl get pods` and see nothing, but the Pod exists in `instagram-learning`. What is the likely mistake?</summary>

The command likely queried the `default` namespace. Add `-n instagram-learning` or use `-A` to inspect all namespaces.

</details>

<details>
<summary>46. A Pod reports `ErrImagePull`, followed by `ImagePullBackOff`. What should you inspect first, and what does the backoff mean?</summary>

Inspect `describe pod` and namespace events first. `ErrImagePull` records a pull failure; `ImagePullBackOff` means Kubernetes is delaying repeated pull attempts rather than retrying continuously. The event message usually gives the concrete registry, tag, authentication, or network error.

</details>

<details>
<summary>47. A Pod is `Running` and `1/1`, but HTTP requests fail. What evidence should you gather next?</summary>

Inspect `describe`, events, and logs, then execute or port-forward an application-level request. `Running 1/1` only proves basic container state without a meaningful readiness probe.

</details>

<details>
<summary>48. You change `track=stable` to `track=canary`. What will selectors for each value return?</summary>

The stable selector returns no objects while the canary selector finds the Pod. Labels drive query membership immediately; the Pod name and image do not need to change.

</details>

<details>
<summary>49. The Instagram backend image exists in Docker on the laptop, but kind reports that it cannot pull it. Why?</summary>

The kind node has its own container runtime image store. The host Docker image is not automatically visible inside that store. Load it with `kind load docker-image ... --name instagram-learning` or push/pull it through a registry.

</details>

<details>
<summary>50. A bare Pod's container process crashes. How can that differ from deleting the entire Pod object?</summary>

With `restartPolicy: Always`, kubelet can restart the failed container inside the same existing Pod, increasing the restart count. Deleting the bare Pod removes the object itself, and no higher-level controller recreates it.

</details>

<details>
<summary>51. You accidentally omit `--kubeconfig` and the default context names a remote GKE cluster. What should you do before any mutation?</summary>

Stop and inspect the target. Use the explicit lesson kubeconfig and verify `kind-instagram-learning` before issuing any create, update, or delete command. Never test a destructive command to discover where it goes.

</details>

<details>
<summary>52. Why does Compose hostname `postgres` not automatically resolve in this Kubernetes cluster?</summary>

Compose and Kubernetes create different networks and service-discovery systems. A Compose service name has no meaning in Kubernetes unless a Kubernetes Service or matching DNS configuration is created there.

</details>

<details>
<summary>53. Why was NGINX chosen for this lesson instead of immediately running the Instagram backend?</summary>

The backend requires PostgreSQL, credentials/configuration, port exposure, and project image loading. NGINX isolates Pod, namespace, label, selector, and diagnostic learning so those dependencies do not obscure the fundamentals.

</details>

<details>
<summary>54. In one paragraph, describe the complete path from `kubectl apply` to a running NGINX container.</summary>

`kubectl` reads the explicit kubeconfig and sends the Pod manifest to the API server. The API server validates and stores the object. The scheduler assigns it to the kind node. Kubelet on that node observes the assignment and asks containerd to pull `nginx:1.27-alpine` and start it. Kubelet then reports Pod and container status, while events record scheduling, pulling, creation, and startup actions.

</details>

### Teach it back

Explain in two or three minutes, without reading commands:

1. how `kubectl` knows which cluster to contact;
2. what happens after a Pod manifest reaches the API server;
3. how a namespace and labels organize the Pod;
4. how you would diagnose a Pod that cannot start;
5. why deleting this bare Pod does not demonstrate Kubernetes self-healing.

<details>
<summary>Teach-it-back checklist</summary>

A strong explanation should:

- identify the API server rather than saying kubectl controls containers directly;
- distinguish a cluster, node, Pod, and container;
- connect `spec`, `status`, and reconciliation;
- explain that contexts determine command targets;
- explain namespaces as scope and labels/selectors as grouping;
- choose `describe` and events for image-pull failures;
- explain why a controller, not merely Kubernetes in general, is needed to replace a deleted Pod.

</details>

## Evidence

- Tooling: kind `0.33.0`, Kubernetes server `1.37.0`, and kubectl `1.37.1` on macOS arm64.
- Context safety: the global context remained the old GKE context; every lesson command used `/tmp/instagram-learning-kubeconfig`, whose context was `kind-instagram-learning`.
- Cluster: `instagram-learning-control-plane` reached node status `Ready` with containerd `2.3.4`.
- Namespace: `instagram-learning` was created from the committed manifest.
- Pod: `hello-kubernetes` reached `Running`, `1/1 Ready`, on the kind node with labels visible.
- Application check: `kubectl exec ... wget http://127.0.0.1` returned the NGINX welcome HTML, and NGINX logs recorded HTTP `200`.
- Selector check: `track=stable` found nothing after relabeling to canary; `track=canary` found the Pod; reapplying the manifest restored stable.
- Failure: the invalid image moved through `ErrImagePull` to `ImagePullBackOff`; events explicitly reported that the tag was not found.
- Recovery: reapplying the valid manifest restored `Running` and `Ready`.
- Bare-Pod behavior: after deletion and a five-second check, no Pods existed in the namespace; manually reapplying recreated it with a new UID/IP.
- Backend regression suite: `./gradlew test` completed successfully on 2026-09-28.
- Browser proof: not applicable; this lesson changes local Kubernetes learning infrastructure, not UI behavior.

## Code and configuration pointers

- [kind-cluster.yaml](/Users/saurabh/Documents/Learning/instagram-backend/k8s/learning/02-primitives/kind-cluster.yaml)
- [namespace.yaml](/Users/saurabh/Documents/Learning/instagram-backend/k8s/learning/02-primitives/namespace.yaml)
- [pod.yaml](/Users/saurabh/Documents/Learning/instagram-backend/k8s/learning/02-primitives/pod.yaml)
- [Dockerfile](/Users/saurabh/Documents/Learning/instagram-backend/Dockerfile)
- [application.properties](/Users/saurabh/Documents/Learning/instagram-backend/src/main/resources/application.properties)
- [Lesson 01](/Users/saurabh/Documents/Learning/instagram-backend/docs/kubernetes/01-containerize-spring-boot-backend.md)

## Official references

- [kind Quick Start](https://kind.sigs.k8s.io/docs/user/quick-start/)
- [Kubernetes objects](https://kubernetes.io/docs/concepts/overview/working-with-objects/)
- [Pods](https://kubernetes.io/docs/concepts/workloads/pods/)
- [Namespaces](https://kubernetes.io/docs/concepts/overview/working-with-objects/namespaces/)
- [Labels and selectors](https://kubernetes.io/docs/concepts/overview/working-with-objects/labels/)
- [kubectl overview](https://kubernetes.io/docs/concepts/overview/kubectl/)

## Commit pointers

- `fa12b65` added the kind cluster configuration, namespace and Pod manifests, verified primitives lesson, teaching-standard alignment, and completed backlog entry.

## What comes next

Lesson `K8S-003` replaces the fragile bare Pod with a Deployment and exposes backend replicas through a Service.

That next step answers two limitations demonstrated here:

1. A Deployment controller can recreate a missing Pod and manage a replica count.
2. A Service can use labels to provide stable discovery and route traffic to changing Pods.

It will also load the existing Instagram backend image into kind, configure its runtime dependencies, and make the API reachable for verification.
