# 01B - Image, Container, and Dockerfile

**Time:** 10-15 minutes

**Starting point:** Complete 01A first. You should already be able to distinguish source code, a JAR, and a running Java process. No Docker or Kubernetes knowledge is assumed.

## The problem

The backend may work on your laptop because your laptop happens to have the right Java version, files, command, and environment. Another machine may have none of those things.

We need a repeatable package that says, "Here are the application files and the way to start them." That package is what a container system can use locally and what Kubernetes can later use on a cluster node.

## The five terms for this lesson

| Term | Plain meaning |
|---|---|
| **Dockerfile** | A text recipe for building an application package |
| **Image** | The built, read-only package containing files and startup information |
| **Container** | A running instance created from an image |
| **Container runtime** | Software that creates and manages containers from images |
| **Registry** | A service that stores images so other machines can obtain them |

## Must understand

The project's [Dockerfile](/Users/saurabh/Documents/Learning/instagram-backend/Dockerfile) is a recipe. It contains steps such as choosing a Java base, building the JAR, copying the result, selecting a runtime user, and declaring the startup command.

Running a Docker build follows that recipe and produces an **image**. An image is stored data. It does not serve requests or consume application CPU merely because it exists.

Starting the image creates a **container**. The container has a running main process. For this project, that main process is Java executing `/app/app.jar`:

```dockerfile
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

The relationship is:

```text
Dockerfile --build--> image --start with runtime--> container
                                                \-> Java process
```

One image can create several containers. They share the same packaged starting files, but each container gets its own process, environment values, network identity, and temporary writable area.

Kubernetes normally receives an image reference. It does not open this Git repository and invent the build steps. Later, Kubernetes will ask a node's container runtime to create a container from the chosen image.

## Mental model

Think of a Dockerfile as a cake recipe, an image as a finished reusable cake mould, and a container as one cake produced from it.

A more precise software analogy is a class and its objects: the image is reusable input, and several independent running containers can be created from it. The analogy stops at implementation details; an image is a layered filesystem and metadata, not Java source code.

## Small exercise

### Predict

Open the [Dockerfile](/Users/saurabh/Documents/Learning/instagram-backend/Dockerfile) but do not study every instruction yet. Predict:

1. Which line describes the process that starts when a container starts?
2. Does the file itself prove that an image has already been built?

### Do

Run these read-only commands:

```bash
sed -n '1,120p' Dockerfile
docker image ls instagram-backend
docker ps --filter name=instagram-backend
```

Match each command to one question:

- What recipe exists?
- What matching images exist?
- What matching containers are running now?

### Expected result

The Dockerfile output ends with the Java `ENTRYPOINT`. The image and container lists may be empty if nothing has been built or started yet. That is valid and demonstrates that a recipe, an image, and a running container are separate things.

## Useful later

- A registry lets a cluster node download an image built elsewhere.
- An image tag is a human-readable reference to an image; the next micro-lesson introduces it.
- A container shares the host's operating-system kernel. It is not a complete virtual machine.

<details>
<summary>Optional deep dive: container versus virtual machine</summary>

A virtual machine normally contains a complete guest operating system and its own kernel. A container normally shares the host kernel while receiving isolated views of processes, networking, and filesystems.

That usually makes containers smaller and quicker to start. It does not make them perfectly isolated or automatically secure.

</details>

## Check your understanding

<details>
<summary>1. Is a Dockerfile an image?</summary>

No. A Dockerfile is the recipe used during a build. The image is the result produced by following that recipe.

</details>

<details>
<summary>2. Is an image a running application?</summary>

No. An image is stored, read-only package data and metadata. A container created from it contains the running application process.

</details>

<details>
<summary>3. Can one image create three containers?</summary>

Yes. Each container starts from the same packaged image but has a separate running process and runtime state. This is how Kubernetes can later create several backend replicas from one image.

</details>

<details>
<summary>4. What happens to a container when its main Java process exits?</summary>

The container stops. A container is not a small background machine that stays alive independently of its main process.

</details>

<details>
<summary>5. Why does Kubernetes need an image rather than only this Git repository?</summary>

Kubernetes is designed to schedule runnable packages. The image records the runtime files and startup information, so replacement instances can start consistently without reconstructing a developer workstation and compiling source every time.

</details>

<details>
<summary>6. What problem does a registry solve?</summary>

It stores and distributes images. A cluster node can obtain the same built image even when that image was created on another machine.

</details>

## Stop/go check

Continue only if you can complete this chain from memory:

```text
__________ --build--> __________ --start--> __________
```

The answer is `Dockerfile -> image -> container`.

You should also be able to point to the container's main process in the real Dockerfile. If not, reread **Must understand** before going on.
