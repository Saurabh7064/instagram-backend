# 02F — Namespaces, Labels, and Selectors

**Time:** 10–15 minutes  
**Prerequisite:** [02E — Pods and Their Lifecycle](./02e-pods-lifecycle.md)

## One thing you will learn

You will understand how Kubernetes scopes names and how it groups objects without relying on their names.

## Assumptions

You can identify the lesson Pod and know that it is an object stored through the Kubernetes API. You do not need to know Services, access control, or network policy.

## Vocabulary budget

This micro-lesson introduces four terms:

| Term | Plain meaning |
|---|---|
| namespace | A scope and grouping boundary inside one cluster |
| label | A selectable key/value description attached to an object |
| selector | A query that finds objects by their labels |
| annotation | Supporting metadata not intended for selection |

## The problem first

A cluster can contain hundreds or thousands of objects. Names alone cannot answer questions such as:

- Which objects belong to the Instagram learning project?
- Which Pods are backend Pods?
- Which Pods are on the stable release track?
- Can two teams use the same simple name without a collision?

Namespaces provide scope. Labels and selectors provide flexible grouping.

## Must understand

### A namespace scopes many object names

The lesson uses the **namespace** `instagram-learning` rather than placing work in `default`.

Within one cluster, two namespaces can each contain a Pod named `hello-kubernetes`. Their full locations differ:

```text
instagram-learning / hello-kubernetes
another-namespace  / hello-kubernetes
```

Not every object belongs to a namespace. Nodes and Namespace objects themselves apply to the whole cluster.

A namespace is also not a separate cluster. Most importantly, it is not automatically a complete security boundary. Access control, network rules, and resource limits require additional configuration.

### Labels describe selectable dimensions

A **label** is a key/value description meant for grouping and selection. The Pod manifest includes:

```yaml
labels:
  app.kubernetes.io/name: hello-kubernetes
  app.kubernetes.io/part-of: instagram-learning
  tier: learning
  track: stable
```

Labels are not unique identifiers. Many future Pods can share `app.kubernetes.io/part-of: instagram-learning`.

### Selectors ask “which objects match?”

A **selector** filters objects by labels:

```text
track=stable
```

It can match zero, one, or many objects. In a comma-separated selector, all requirements must match:

```text
app.kubernetes.io/name=hello-kubernetes,tier=learning
```

A selector identifies a set. It does not send network traffic by itself. A later Service will use a selector as part of routing.

### Annotations are notes, not grouping keys

An **annotation** stores supporting information such as a description, build URL, or tool-specific setting. Use a label when a system must select or group objects; use an annotation for non-identifying metadata.

### Mental model: folders and colored tags

A namespace is like a folder that scopes names. Labels are like colored tags attached to files. A selector is a search for matching tags.

The analogy stops at security. A computer folder may have its own permissions, while a Kubernetes namespace does not become a full isolation boundary merely because it exists.

## Instagram-project example

The project grouping is declared in [namespace.yaml](/Users/saurabh/Documents/Learning/instagram-backend/k8s/learning/02-primitives/namespace.yaml). The Pod's grouping information is declared in [pod.yaml](/Users/saurabh/Documents/Learning/instagram-backend/k8s/learning/02-primitives/pod.yaml).

Later, backend replicas will share an application label. A Service will select that label instead of depending on one replaceable Pod name.

## Useful later

Good labels describe stable dimensions such as application, component, environment, and release track. Avoid putting frequently changing or large values into labels.

<details>
<summary>Optional deep dive</summary>

Kubernetes supports equality-based and set-based selector expressions. This course starts with equality because it is enough to understand how future Deployments and Services connect to Pods.

</details>

## Tiny exercise

### Predict

If `track` changes from `stable` to `canary`, which selector should find the Pod? What should happen after the committed manifest is reapplied?

### Try it

Change only the live label:

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  label pod hello-kubernetes \
  -n instagram-learning \
  track=canary --overwrite
```

Compare the two groups:

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  get pods -n instagram-learning -l track=stable

kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  get pods -n instagram-learning -l track=canary --show-labels
```

Restore the committed intent:

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  apply -f k8s/learning/02-primitives/pod.yaml
```

### Expected result

While the live label is `canary`, the stable selector returns no Pod and the canary selector returns `hello-kubernetes`. Reapplying [pod.yaml](/Users/saurabh/Documents/Learning/instagram-backend/k8s/learning/02-primitives/pod.yaml) restores `track=stable` because that is the declared value.

### Why this result occurs

Selectors evaluate current labels; they do not remember earlier values. Reapplying the manifest moves the managed label back to the committed desired state.

## Pause and check

<details>
<summary>1. What problem does a namespace solve?</summary>

It groups and scopes many resources inside one cluster. It allows the same kind and name to exist in different scopes and gives policies or quotas a logical target later.

</details>

<details>
<summary>2. Is a namespace another cluster or a complete security boundary?</summary>

No. It exists inside one cluster and primarily provides scope and grouping. Effective security and network isolation need explicit access rules, network policies, quotas, and other controls.

</details>

<details>
<summary>3. Must a label be unique?</summary>

No. Labels are intentionally reusable. Many Pods may share the same application label so one selector can find the whole group.

</details>

<details>
<summary>4. What can a selector return?</summary>

It can return zero, one, or many matching objects. A selector describes membership conditions; it is not a unique-name lookup.

</details>

<details>
<summary>5. Does a selector itself route traffic?</summary>

No. It only identifies matching objects. A Service introduced later uses a selector plus Kubernetes networking to direct traffic.

</details>

<details>
<summary>6. When should you use an annotation instead of a label?</summary>

Use an annotation for supporting information that does not need to participate in grouping or selection, such as a description or build URL. Use labels for compact, meaningful dimensions that clients need to query.

</details>

## Stop/go check

Continue when you can:

- explain namespace scope without calling it a separate cluster;
- predict which Pods a two-condition selector matches;
- explain labels versus annotations;
- state why a selector alone does not route traffic.

Next: [02G — Observe, Break, and Recover](./02g-observe-break-recover.md).
