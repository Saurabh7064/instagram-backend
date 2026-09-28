# 02C — kubectl, Kubeconfig, and Context

**Time:** 10–15 minutes
**Prerequisite:** [02B — The Control-Plane Request Journey](./02b-control-plane-request-journey.md)

## One thing you will learn

You will understand how a Kubernetes command knows which cluster to contact, and you will perform a safe target check before changing anything.

## Assumptions

You know that the API server is the cluster's front door. You do not need prior command-line knowledge beyond copying a command and reading its output.

## Vocabulary budget

This micro-lesson introduces three terms:

| Term | Plain meaning |
|---|---|
| kubectl | The main command-line client for the Kubernetes API |
| kubeconfig | Connection and identity settings used by Kubernetes clients |
| context | A named choice of cluster, user, and optional default namespace |

## The problem first

A developer may have access to several clusters: local learning, testing, staging, and production. The command below is dangerous if its target is unknown:

```bash
kubectl delete pod example
```

The terminal's current directory does not choose the cluster. A command that looks identical can affect different infrastructure depending on its kubeconfig and selected context.

The safety rule is simple: **verify the target before a mutating command**.

## Must understand

### kubectl is a client, not the cluster

`kubectl` turns commands such as `get`, `apply`, and `delete` into requests to the Kubernetes API server. It does not contain the cluster and it does not normally start containers itself.

### Kubeconfig answers “where and as whom?”

A **kubeconfig** tells a client:

- which API server addresses it knows;
- which identities or credentials are available;
- which combinations have been given context names;
- which context is currently selected.

Because the file can contain connection and authentication information, do not commit it to this repository.

### A context selects the target

A **context** ties together a cluster, an identity, and optionally a default namespace. Selecting a context is like choosing the account and destination for the next API call.

For this course, the safe expected context is:

```text
kind-instagram-learning
```

The lesson uses a separate temporary kubeconfig on every command:

```text
/tmp/instagram-learning-kubeconfig
```

This repetition is intentional. It avoids relying on the user's global context, which may point somewhere else.

### Mental model: an address book entry

Think of kubeconfig as an address book containing destinations and identities. A context is the selected entry. `kubectl` is the caller using that entry to contact the API server.

The analogy stops at authentication: real kubeconfig data can contain certificates, tokens, or references to credential helpers, not merely an address and a name.

## Instagram-project example

All commands in this module target the one-node cluster configured by [kind-cluster.yaml](/Users/saurabh/Documents/Learning/instagram-backend/k8s/learning/02-primitives/kind-cluster.yaml). Keeping its kubeconfig separate protects any other clusters already configured on the computer.

The application files, such as the [Dockerfile](/Users/saurabh/Documents/Learning/instagram-backend/Dockerfile), do not select a cluster. Project directory and cluster target are separate concerns.

## Useful later

Many `kubectl` commands also accept `-n <namespace>`. That flag narrows a request inside the selected cluster. It does not change the cluster. Namespaces are explained in 02F.

<details>
<summary>Optional deep dive</summary>

Clients can merge multiple kubeconfig files, and environment variables can change which files are read. This course avoids that complexity by using the explicit `--kubeconfig` flag.

</details>

## Tiny exercise

### Predict

Will changing into the repository directory cause kubectl to choose the local cluster automatically?

No. Predict which context the explicit lesson kubeconfig should report.

### Try it

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  config current-context
```

### Expected result

```text
kind-instagram-learning
```

If the file was removed while the cluster still exists, restore it safely:

```bash
kind export kubeconfig \
  --name instagram-learning \
  --kubeconfig /tmp/instagram-learning-kubeconfig
```

Then repeat the target check.

### Why this result occurs

The `--kubeconfig` flag explicitly chooses the lesson connection file. That file's current context points to the named kind cluster. The working directory plays no role.

## Pause and check

<details>
<summary>1. What does kubectl talk to?</summary>

It sends requests to a Kubernetes API server. The API server then exposes or changes cluster object state; kubectl does not directly control container processes on nodes.

</details>

<details>
<summary>2. What information does kubeconfig provide?</summary>

It provides known cluster/API endpoints, available user identities or authentication settings, context definitions, and the selected context. It answers where a request goes and which identity it uses.

</details>

<details>
<summary>3. What does a context select?</summary>

A context selects a cluster and a user identity, plus an optional default namespace. It is the named target configuration used for requests.

</details>

<details>
<summary>4. Does opening a terminal inside this repository select the learning cluster?</summary>

No. The current directory does not select a Kubernetes cluster. Kubeconfig selection, its current context, and command flags determine the target.

</details>

<details>
<summary>5. Why is the explicit lesson kubeconfig safer than relying on a global default?</summary>

It makes the intended local target visible in every command and avoids accidentally using a previously selected remote cluster. The current context check then provides evidence before any mutation.

</details>

## Stop/go check

Do not continue until:

- you can explain `kubectl -> kubeconfig -> context -> API server`;
- the target check prints `kind-instagram-learning`;
- you know that the repository directory does not select the cluster.

Next: [02D — Desired State, Objects, and Manifests](./02d-desired-state-objects-manifests.md).
