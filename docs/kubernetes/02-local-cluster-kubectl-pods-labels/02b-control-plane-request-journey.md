# 02B — The Control-Plane Request Journey

**Time:** 10–15 minutes
**Prerequisite:** [02A — Why Kubernetes? Cluster and Node](./02a-why-kubernetes-cluster-node.md)

## One thing you will learn

You will be able to tell the story of how a request moves from Kubernetes' front door to a running container on a node.

## Assumptions

You already know that a cluster is the complete Kubernetes environment and that a node is a machine-like worker inside it. You do not need to know the Kubernetes command line or YAML yet.

## Vocabulary budget

This micro-lesson introduces five terms:

| Term | Plain meaning |
|---|---|
| control plane | The cluster's decision-making and state-management side |
| API server | The front door for Kubernetes requests |
| scheduler | The component that chooses a node for new work |
| kubelet | The node-side agent that makes assigned work run |
| container runtime | The node software that pulls images and starts containers |

## The problem first

A request such as “run this application” needs several different decisions:

1. Is the request valid?
2. Where should the application run?
3. Who starts its container on that machine?
4. Who reports what actually happened?

If one program tried to do all of this invisibly, failures would be hard to locate. Kubernetes divides the journey into clear responsibilities.

## Must understand

### The control plane decides and records

The **control plane** is the decision-making and state-management side of the cluster. It accepts requests, keeps the cluster's object data, and coordinates what should happen next.

The **API server** is its front door. Tools and internal components read or change Kubernetes data through this API. A client does not normally SSH into a node and start a process itself.

The **scheduler** handles placement. When new work needs a node, it chooses a suitable one. Choosing a node is not the same as starting a container.

### The node carries out the assignment

Each node runs a **kubelet**. It watches for work assigned to that node and makes sure the requested containers are running.

The kubelet asks the **container runtime** to pull the requested image and start or stop containers. The runtime does the low-level container work; the kubelet connects that work to Kubernetes.

### The complete request journey

For now, imagine a user submits “run NGINX.” The next micro-lessons explain the command and the object in detail.

```text
request
  -> API server accepts and records it
  -> scheduler chooses a node
  -> kubelet on that node sees the assignment
  -> container runtime pulls the image and starts the container
  -> kubelet reports what happened
  -> API server makes that observation available to clients
```

The most important distinction is:

- the scheduler **chooses**;
- the kubelet **ensures**;
- the container runtime **starts**.

### Mental model: order desk, dispatcher, kitchen lead

The API server resembles an order desk, the scheduler resembles a dispatcher choosing a kitchen, and the kubelet resembles the kitchen lead ensuring that the assigned order gets made.

The analogy is incomplete. Kubernetes components continuously observe shared cluster state; they are not passing a paper ticket once and forgetting it.

## Instagram-project example

Later, a request will refer to the image built from the project [Dockerfile](/Users/saurabh/Documents/Learning/instagram-backend/Dockerfile). The scheduler will choose the local kind node. The node's kubelet will ask its runtime to start the backend container.

The backend will still need its PostgreSQL settings from [application.properties](/Users/saurabh/Documents/Learning/instagram-backend/src/main/resources/application.properties). Kubernetes can start a container, but it cannot guess missing application configuration.

## Useful later

The control plane contains more components than the two introduced here. You will later encounter storage for cluster data and controllers that repeatedly compare desired and actual state. They are intentionally deferred so this request journey stays small.

<details>
<summary>Optional deep dive</summary>

In the kind learning setup, one node hosts both control-plane and node-side components. Their responsibilities remain conceptually different even though they happen on the same Docker container.

</details>

## Tiny exercise

This exercise only observes the control plane. The explicit connection file is explained in 02C; for now, copy the command exactly.

### Predict

If the cluster has a working API server, should the command report a control-plane address or directly name an application container?

### Try it

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig cluster-info
```

### Expected result

The output should include a line beginning with something like:

```text
Kubernetes control plane is running at https://...
```

The exact address can differ. If the temporary file is missing, do not switch to an unknown default cluster; 02C explains how to restore and verify it safely.

### Why this result occurs

The command contacts the API server and asks for cluster information. It is observing Kubernetes' front door, not reaching into an application container.

## Pause and check

<details>
<summary>1. What is the API server's job in the request journey?</summary>

It is the front door for Kubernetes API requests. It accepts and validates requests and exposes stored cluster object information. Other components work from that shared state rather than a client directly controlling each node.

</details>

<details>
<summary>2. What is the difference between the scheduler and the kubelet?</summary>

The scheduler selects a node for new work. The kubelet runs on a node and ensures the work assigned to that node is carried out. One chooses placement; the other acts on the assignment.

</details>

<details>
<summary>3. Which component actually performs low-level image pulls and container starts?</summary>

The container runtime does that low-level work after the kubelet asks it to. The kubelet manages the Kubernetes responsibility; the runtime manages containers on the node.

</details>

<details>
<summary>4. Does the user's command normally SSH into the chosen node?</summary>

No. A Kubernetes client talks to the API server. The scheduler, kubelet, and container runtime cooperate through cluster state to place and start the work.

</details>

<details>
<summary>5. If an image cannot be pulled, has the scheduler necessarily failed?</summary>

No. The scheduler may have successfully chosen a node. The later image-pull step can fail at the node. This is why knowing the journey helps: evidence about placement and evidence about image pulling answer different questions.

</details>

## Stop/go check

Continue when you can narrate the five-step request journey and correctly finish these sentences:

- “The API server is ...”
- “The scheduler chooses ...”
- “The kubelet asks ...”

If you cannot, trace one imaginary Instagram backend container through the diagram before moving on.

Next: [02C — kubectl, Kubeconfig, and Context](./02c-kubectl-kubeconfig-context.md).
