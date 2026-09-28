# 01 - Containerize the Spring Boot Backend

## Start here

This module assumes only that you can open a terminal and recognize that the repository contains Java files. It does **not** assume Docker, Kubernetes, Linux-process, networking, or JVM knowledge.

Do not read this module as one long chapter. Complete one 10–15 minute part, do its small exercise, and stop at its readiness check. Continue only when the current distinction feels explainable in your own words.

This module teaches how the backend moves from Java source code to a repeatable container. It postpones Kubernetes itself until Module 02.

## Why this module exists

The backend may work on one laptop because that laptop already has the correct Java version, local configuration, files, and startup knowledge. Another machine may not.

Before Kubernetes can manage the backend, the project needs a portable image that contains the application runtime and a clear startup command. Containerization creates that runnable unit.

## Core micro-lesson path

| Part | Learn one thing | Time | Start when | Ready when |
|---|---|---:|---|---|
| [01A](./01-containerize-spring-boot-backend/01a-from-code-to-running-program.md) | Source → JAR → JVM → process | 10–15 min | You can run a terminal command | You can identify what is stored and what is running |
| [01B](./01-containerize-spring-boot-backend/01b-image-container-and-dockerfile.md) | Image versus container | 10–15 min | 01A is clear | You can distinguish recipe, template, and running instance |
| [01C](./01-containerize-spring-boot-backend/01c-build-and-run-the-first-container.md) | Build, tag, and run | 10–15 min | Docker is running | You can explain build context and host/container ports |
| [01D](./01-containerize-spring-boot-backend/01d-read-the-multi-stage-dockerfile.md) | Why the Dockerfile has two stages | 10–15 min | 01C is clear | You can narrate build stage versus runtime stage |
| [01E](./01-containerize-spring-boot-backend/01e-container-networking-and-configuration.md) | Container networking and runtime configuration | 10–15 min | 01C is clear | You can explain `postgres:5432` versus `localhost:55432` |
| [01F](./01-containerize-spring-boot-backend/01f-security-storage-and-recovery.md) | Security, persistent data, and recovery | 10–15 min | 01D and 01E are clear | You can distinguish process state from useful application health |

Recommended pace: one or two parts per sitting. There is no reward for finishing all six at once.

## Core path versus optional reference

- **Core path:** the six micro-lessons above.
- **Optional full reference:** [original detailed Lesson 01](./reference/01-containerize-spring-boot-backend-full-reference.md). Use it for review or deeper lookup, not as the first reading path.
- **Project artifacts:** [Dockerfile](../../Dockerfile), [.dockerignore](../../.dockerignore), [docker-compose.yml](../../docker-compose.yml), and [application.properties](../../src/main/resources/application.properties).

## One-page recap

```text
Java source
    |
    | Gradle compiles and packages
    v
Spring Boot JAR
    |
    | Docker follows the Dockerfile
    v
Container image
    |
    | a container runtime starts it
    v
Container with a running Java process
```

Keep these distinctions:

- Source code, JARs, and images are stored artifacts; a process is running work.
- A Dockerfile is the recipe, an image is the built template, and a container is a running instance.
- The build stage needs compiler tools; the runtime stage needs only what runs the JAR.
- `localhost` inside a container means that container, not another container.
- Runtime environment variables let the same image work in different environments.
- A container can be running while the application is unusable because PostgreSQL or another dependency is unavailable.
- Database data belongs in persistent storage; the container itself should be replaceable.

## Vocabulary cheat sheet

| Term | Plain meaning | Project example | Do not confuse it with |
|---|---|---|---|
| JAR | Packaged Java application | File produced under `build/libs/` | A running Java process |
| JVM | Runtime that executes Java bytecode | Java 24 runtime | The application source |
| Process | Program executing now | Spring Boot listening on `8080` | A stored JAR or image |
| Dockerfile | Instructions for building an image | [Dockerfile](../../Dockerfile) | The built image |
| Image | Read-only runnable template | Backend image built from the Dockerfile | A running container |
| Container | Running instance of an image | Compose `backend` service instance | A virtual machine or image |
| Port mapping | Host port forwarded to container port | `8080:8080`, `55432:5432` | `EXPOSE`, which is documentation |
| Volume | Storage kept outside a disposable container | `instagram-postgres-data` | A container writable layer |

## Integrated module practice

The exercises are distributed deliberately:

1. Build the JAR in [01A](./01-containerize-spring-boot-backend/01a-from-code-to-running-program.md).
2. Build and start the first image in [01C](./01-containerize-spring-boot-backend/01c-build-and-run-the-first-container.md).
3. Verify backend-to-PostgreSQL networking in [01E](./01-containerize-spring-boot-backend/01e-container-networking-and-configuration.md).
4. Break PostgreSQL availability, observe the failure, recover the API, and inspect persistence in [01F](./01-containerize-spring-boot-backend/01f-security-storage-and-recovery.md).

Completion evidence from the project run is retained in the [full reference](./reference/01-containerize-spring-boot-backend-full-reference.md#evidence).

## Teach it back

After 01F, explain this without reading the diagram:

> How does this repository become a running backend container, how does that container reach PostgreSQL, and which data must survive container replacement?

If the explanation mixes up image/container or host/container `localhost`, revisit 01B or 01E rather than moving forward.

## Evidence and commit pointers

- `c9007bf` added the Dockerfile, `.dockerignore`, Compose backend service, original lesson, and verified container/API evidence.
- `877ca3a` expanded the original beginner explanations.
- `7f1ace1` split the module into six beginner micro-lessons and preserved the original chapter as optional reference material.
- The complete command evidence remains in the optional full reference.

## What comes next

[Module 02](./02-local-cluster-kubectl-pods-labels.md) begins only after you can distinguish an image from a running container. It explains why Kubernetes exists and then introduces the cluster one small concept at a time.
