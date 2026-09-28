# 02G — Observe, Break, and Recover

**Time:** 10–15 minutes
**Prerequisite:** [02F — Namespaces, Labels, and Selectors](./02f-namespaces-labels-selectors.md)

## One thing you will learn

You will choose the right evidence for an image-startup failure, explain the evidence, and recover by restoring the committed desired state.

## Assumptions

You can verify the `kind-instagram-learning` context, find the Pod in the `instagram-learning` namespace, and distinguish desired state from observed state. The Pod should be `Running` before this exercise begins.

## Vocabulary budget

This micro-lesson introduces five diagnostic terms:

| Term | Plain meaning |
|---|---|
| summary view | `kubectl get`: what exists and its high-level state |
| diagnosis view | `kubectl describe`: detailed state, reasons, and recent events |
| event | A timestamped record of an important cluster action or failure |
| container log | Text written by a started process to standard output or error |
| ImagePullBackOff | Kubernetes is delaying retries after image pulls failed |

## The problem first

When a workload fails, random commands create noise. The useful question is:

> Which stage of the request journey failed, and which evidence can describe that stage?

An application log can explain a process that started and then failed. It cannot explain a new container whose image was never downloaded and whose process never began.

## Must understand

### Start broad, then narrow

Use a small evidence ladder:

1. `kubectl get` — identify the object and high-level symptom.
2. `kubectl describe` — inspect assignment, container state, reason, and recent events.
3. namespace events — view important actions and failures in time order.
4. `kubectl logs` — inspect process output only if a container started.
5. an application request — prove useful behavior after recovery.

No one command proves everything.

### ImagePullBackOff describes retry behavior

If the requested image tag does not exist, the node runtime cannot pull it. You may first see `ErrImagePull`, then `ImagePullBackOff`.

The “backoff” does not mean Kubernetes permanently gave up. It means retries are being delayed so the node does not hammer the registry continuously. The event message usually contains the concrete reason, such as “tag not found.”

### Recovery restores intent, then verifies behavior

The durable source of desired state is [pod.yaml](/Users/saurabh/Documents/Learning/instagram-backend/k8s/learning/02-primitives/pod.yaml). Reapplying it restores the valid image request. Waiting for readiness confirms lifecycle recovery; an HTTP request confirms useful behavior.

### Mental model: triage before treatment

A doctor first observes symptoms, gathers evidence, identifies the likely cause, and then treats it. Similarly, do not “fix” a Pod before capturing the status and event that explain the failure.

The analogy stops at certainty: one Kubernetes symptom can have several causes, so the actual event message matters more than memorizing a diagnosis.

## Instagram-project example

The NGINX image in [pod.yaml](/Users/saurabh/Documents/Learning/instagram-backend/k8s/learning/02-primitives/pod.yaml) is intentionally simple. The same evidence order applies when the image built from the project's [Dockerfile](/Users/saurabh/Documents/Learning/instagram-backend/Dockerfile) fails later.

For the real backend, a pulled and started image can still fail because PostgreSQL settings from [application.properties](/Users/saurabh/Documents/Learning/instagram-backend/src/main/resources/application.properties) are wrong. That would be a different stage, where container logs become more useful.

## Useful later

The current Pod was applied directly and has no higher-level owner. If you delete the Pod object, nothing recreates it automatically. The next Kubernetes module introduces a Deployment, which keeps a requested number of Pods present.

You can prove the current behavior after the main recovery:

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  delete pod hello-kubernetes -n instagram-learning --wait=true

kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  get pods -n instagram-learning
```

Expected: no Pod returns. Restore it by reapplying [pod.yaml](/Users/saurabh/Documents/Learning/instagram-backend/k8s/learning/02-primitives/pod.yaml).

<details>
<summary>Optional deep dive</summary>

An old container instance may have logs even while a newly requested image cannot start. Always connect logs to the correct container instance and timestamp. Detailed flags for earlier instances are deferred until crash-loop troubleshooting.

</details>

## Tiny exercise

This intentionally causes a safe failure in the local lesson cluster. Verify the context first. Do not run the mutation if the output is anything other than `kind-instagram-learning`.

### Predict

Before running the commands, write down:

1. Will an invalid image tag produce application logs from a new NGINX process?
2. Which two evidence sources should explain the failure?
3. What should reapplying the committed manifest restore?

### Observe the healthy starting point

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  config current-context

kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  get pod hello-kubernetes -n instagram-learning
```

Expected: context `kind-instagram-learning`, then a `Running` Pod.

### Break image resolution

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  set image pod/hello-kubernetes \
  -n instagram-learning \
  web=nginx:this-tag-does-not-exist
```

Observe and diagnose:

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  get pod hello-kubernetes -n instagram-learning

kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  describe pod hello-kubernetes -n instagram-learning

kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  get events -n instagram-learning \
  --sort-by=.metadata.creationTimestamp
```

### Expected failure result

The high-level state should move through `ErrImagePull` and/or `ImagePullBackOff`. The diagnosis view or events should say that `nginx:this-tag-does-not-exist` cannot be found or pulled.

New process logs are not the right primary evidence because the requested container never started.

### Recover

```bash
kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  apply -f k8s/learning/02-primitives/pod.yaml

kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  wait --for=condition=Ready \
  pod/hello-kubernetes \
  -n instagram-learning \
  --timeout=120s

kubectl --kubeconfig /tmp/instagram-learning-kubeconfig \
  exec -n instagram-learning hello-kubernetes -- \
  wget -qO- http://127.0.0.1
```

### Expected recovery result

The Pod returns to ready state and the final request returns NGINX HTML. Recovery works because the manifest restores the valid `nginx:1.27-alpine` desired image.

## Pause and check

<details>
<summary>1. What question does `kubectl get` answer best?</summary>

It answers which objects exist and what their summarized current state is. It is the fast first view, but it usually does not contain enough detail to explain a failure.

</details>

<details>
<summary>2. When should you use `kubectl describe`?</summary>

Use it when you need detailed state, reasons, conditions, assignment information, and recent events for one object. It connects a high-level symptom to more specific evidence.

</details>

<details>
<summary>3. Why can logs be unhelpful for a newly requested image in ImagePullBackOff?</summary>

The image was not pulled, so the new container process never started and produced no new output. The Pod's state, diagnosis view, and events describe the image-pull stage instead.

</details>

<details>
<summary>4. What does the “backoff” part of ImagePullBackOff mean?</summary>

Kubernetes is spacing out repeated pull attempts after failures. It reduces continuous retry load; it does not by itself identify the root cause. Read the event message for the concrete error.

</details>

<details>
<summary>5. Why does reapplying pod.yaml recover this specific failure?</summary>

The committed manifest requests the valid `nginx:1.27-alpine` image. Applying it replaces the invalid live image request, allowing the node runtime to use the valid image and start the container.

</details>

<details>
<summary>6. If this directly created Pod is deleted, why does it stay absent?</summary>

Deleting it removes the desired Pod object, and no higher-level owner is asking for a replacement. Kubernetes only reconciles declared desired state; it does not assume that every deleted Pod should return.

</details>

## Stop/go check

This module is complete when you can:

- choose `get`, `describe`, events, or logs for the question each answers;
- explain `ImagePullBackOff` without saying only “Kubernetes is broken”;
- recover from the invalid image by restoring the manifest;
- verify both readiness and an actual HTTP response;
- explain why the directly created Pod is not recreated after deletion.

Next module: Deployments will add an owner that maintains Pods, and Services will provide stable discovery for replaceable Pods.
