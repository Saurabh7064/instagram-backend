# 02A — Why Kubernetes? Cluster and Node

**Time:** 10–15 minutes
**Learning state:** Start here

## One thing you will learn

You will understand why a team might add Kubernetes after it has already learned to run containers, and you will be able to distinguish a **cluster** from a **node**.

## Assumptions

This micro-lesson assumes only that:

- an application runs as a process;
- a container packages that process and what it needs;
- the Instagram backend already has a container image.

You do **not** need to know Kubernetes commands, YAML, networking, or cloud infrastructure yet.

## Vocabulary budget

This micro-lesson introduces four terms:

| Term | Plain meaning |
|---|---|
| Kubernetes | A system that coordinates containerized applications |
| workload | An application or task that Kubernetes runs |
| cluster | The complete Kubernetes environment |
| node | One machine-like worker inside a cluster |

## The problem first

Imagine that the Instagram backend has one container and you start it by hand. That is manageable.

Now imagine that it has ten copies spread across several machines. One machine fails at 2 a.m. A new version must replace the old version gradually. The team must know which machines still have capacity. Starting containers one at a time is no longer the hard part; **coordinating them reliably** is the hard part.

Kubernetes exists to perform that coordination. It gives the team one system in which to describe, place, observe, and later replace containerized applications.

## Must understand

### Kubernetes is a coordinator

Kubernetes does not turn Java source code into a JAR, and it does not normally build the Docker image. Those steps happen before Kubernetes receives the application.

Kubernetes starts with an already-built image and coordinates where and how the resulting containers run.

For this project, the path is:

```text
Java source -> JAR -> container image -> Kubernetes workload
```

### A cluster is the whole environment

A **cluster** is the complete Kubernetes environment. It includes the parts that make decisions and the nodes that can run application workloads.

A **node** is one machine-like worker in that environment. A production cluster often has several nodes. The local learning cluster has only one.

```text
cluster
└── node
    └── application workloads will run here
```

The words are not interchangeable: a node belongs to a cluster.

### Mental model: a restaurant and its kitchens

Think of a cluster as the whole restaurant operation and a node as one kitchen that can prepare orders. The restaurant coordinates the work; a kitchen provides a place where the work happens.

The analogy stops here: a Kubernetes cluster is software coordinating machines, not people moving physical meals. Later lessons will name the software components that make and carry out decisions.

## Instagram-project example

The backend image is defined by the project [Dockerfile](/Users/saurabh/Documents/Learning/instagram-backend/Dockerfile). In Lesson 01, Docker ran that image directly. In this module, a local Kubernetes cluster becomes the environment that will eventually run it.

For now, the cluster configuration intentionally describes one node:

```yaml
nodes:
  - role: control-plane
```

See [kind-cluster.yaml](/Users/saurabh/Documents/Learning/instagram-backend/k8s/learning/02-primitives/kind-cluster.yaml).

## Useful later

The local cluster is created with **kind**, a learning tool whose name means “Kubernetes IN Docker.” Its node is itself a Docker container on the laptop. That is convenient for learning, but production nodes are usually virtual or physical machines.

<details>
<summary>Optional deep dive</summary>

This one-node setup performs both decision-making and workload-running roles. Production clusters often separate or replicate those responsibilities so that one machine failure does not remove the whole environment.

You do not need that architecture detail to continue.

</details>

## Tiny exercise

### Predict

Read [kind-cluster.yaml](/Users/saurabh/Documents/Learning/instagram-backend/k8s/learning/02-primitives/kind-cluster.yaml). How many nodes should the `instagram-learning` cluster contain?

### Try it

```bash
kind get nodes --name instagram-learning
```

### Expected result

You should see one node named similarly to:

```text
instagram-learning-control-plane
```

If kind reports that the cluster does not exist, stop here rather than creating something blindly. The complete cluster-creation command appears later in the guided lab.

### Why this result occurs

The configuration contains one entry under `nodes`, so kind creates one node container. The name identifies that node; it is not the name of the whole cluster.

## Pause and check

Try each answer aloud before expanding it.

<details>
<summary>1. Why might a team need Kubernetes if Docker can already start a container?</summary>

Docker can start an individual container, but a larger system must coordinate many containers across machines, observe failures, place work, and roll out changes. Kubernetes addresses that coordination problem. It uses container images; it does not replace the image-building step.

</details>

<details>
<summary>2. Is a cluster the same thing as a node?</summary>

No. The cluster is the complete Kubernetes environment. A node is one machine-like worker inside it. A cluster can contain one node for learning or many nodes in a production environment.

</details>

<details>
<summary>3. Does Kubernetes normally compile the Instagram backend or build its image?</summary>

No. Gradle compiles and packages the Java application, and an image builder uses the [Dockerfile](/Users/saurabh/Documents/Learning/instagram-backend/Dockerfile) to create the image. Kubernetes consumes that completed image when it runs the workload.

</details>

<details>
<summary>4. The local cluster has one node. Does that mean every Kubernetes cluster has one node?</summary>

No. One node is a deliberate simplification for local learning. Kubernetes supports multiple nodes, which lets it spread workloads and continue operating when individual machines have problems.

</details>

## Stop/go check

Continue to 02B only when you can say, without rereading:

- the operational problem Kubernetes begins to solve;
- the difference between a cluster and a node;
- why the existing container image comes before Kubernetes.

If those still blur together, redraw the three-line cluster diagram and explain it using the Instagram backend as the workload.

Next: [02B — The Control-Plane Request Journey](./02b-control-plane-request-journey.md).
