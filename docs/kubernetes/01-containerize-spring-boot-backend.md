# 01 - Containerize the Spring Boot Backend

## Goal

Understand what a container image is, why Kubernetes needs one, and how to build a production-style Spring Boot backend image for this project.

By the end, the backend has a multi-stage Docker image that can run as a non-root user and connect to PostgreSQL through environment variables.

## In simple terms

A container image is a packaged filesystem plus startup command. It contains the application jar, the Java runtime, and enough operating-system files to start the process the same way every time.

Kubernetes does not build the application from source when it creates Pods. It pulls an already-built image, starts containers from it, and restarts or replaces them when actual state drifts from desired state.

### Start one level earlier: what is actually running?

This repository contains Java source code. Source code is written for people and build tools; it is not yet the running backend.

Gradle compiles that source and packages it into a **JAR** (Java Archive). The Spring Boot JAR contains compiled application code and the libraries the application needs. You create it with:

```bash
./gradlew bootJar
```

The JAR still needs a **JVM** (Java Virtual Machine) to execute it. This project uses Java 24. When you run `java -jar ...`, the operating system starts a Java **process**. A process is a program that is currently executing: it consumes CPU and memory, opens port `8080`, and connects to PostgreSQL.

This gives us four different things:

| Thing | Meaning | Running? |
|---|---|---|
| Java source | Instructions developers edit | No |
| Spring Boot JAR | Compiled application package | No |
| Container image | JAR, Java runtime, filesystem, and startup metadata packaged together | No |
| Container | An isolated running process created from an image | Yes |

If the Java process stops, the API stops. The source, JAR, and image can still exist because they are stored artifacts, not running programs.

### Five words that must not be mixed up

**Image:** a read-only, layered template containing files and runtime metadata. An image is not alive and does not consume application CPU while sitting unused.

**Container:** a running instance created from an image. Several containers can be created from the same image. Each has its own process, environment values, network identity, and temporary writable layer.

**Dockerfile:** the text recipe Docker follows to build an image. It is neither the image nor the running container.

**Registry:** a service that stores and distributes images. A Kubernetes node can pull an image from a registry before starting it.

**Container runtime:** the software responsible for creating and managing containers from images. Docker provides this experience locally; Kubernetes nodes commonly use a runtime such as containerd.

### The complete path

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
    | Docker/container runtime starts it
    v
Running container and Java process
    |
    | later, Kubernetes manages its lifecycle
    v
Container running inside a Pod
```

Gradle understands how to build the Java application. Docker understands how to package and start it. Kubernetes will understand how many instances should exist, where to run them, and how to replace failed instances.

## What problem does this solve?

Before this lesson, the backend could run from Gradle on a developer machine, but Kubernetes would have nothing stable to deploy. A cluster needs an image tag like `instagram-backend:local`, not a local IDE run configuration.

A common failure without containerization is "works in IntelliJ, fails in cluster" because the app secretly depends on the developer's local Java version, working directory, or environment variables.

More specifically, containerization addresses these problems:

1. **Different Java versions:** the image selects Java 24 rather than hoping the destination machine has it.
2. **Forgotten startup knowledge:** the image records the startup command instead of relying on an operator to remember it.
3. **Hidden laptop dependencies:** a clean image build reveals dependencies on uncommitted files, IDE settings, or local build output.
4. **Repeatable replacement:** Kubernetes can start another instance from the same artifact after a failure.
5. **Distribution:** an image can be stored in a registry and pulled by different machines.

Containerization does **not** automatically provide high availability, backups, monitoring, secure secrets, database replication, or correct application behavior. It provides a consistent runnable unit. Later Kubernetes lessons add orchestration around that unit.

## Mental model

Think of a container image as a sealed lunchbox for the application process: jar, runtime, startup instruction, and defaults travel together.

Where the analogy stops: containers share the host kernel, and images are layered filesystems, not full virtual machines.

Another useful analogy is a Java class and object: the image resembles a reusable definition, while a container resembles one runtime instance created from it. The analogy is imperfect, but it reinforces that one image can create many separate containers.

### Container versus virtual machine

A virtual machine normally includes a complete guest operating system and its own kernel. A container shares the host kernel while isolating its process, filesystem view, network namespace, and other resources. Containers are therefore usually smaller and faster to start, but they do not provide automatic or perfect security.

## How it works

The new [Dockerfile](/Users/saurabh/Documents/Learning/instagram-backend/Dockerfile) has two stages:

1. `build` uses a JDK image, copies the Gradle wrapper and source, and runs `./gradlew bootJar`.
2. The final stage uses a smaller JRE image, copies only the built jar, creates a non-root `instagram` user, exposes port `8080`, and starts `java -jar /app/app.jar`.

The new [.dockerignore](/Users/saurabh/Documents/Learning/instagram-backend/.dockerignore) keeps local build output, Git metadata, IDE files, and proof screenshots out of the build context.

The updated [docker-compose.yml](/Users/saurabh/Documents/Learning/instagram-backend/docker-compose.yml) now includes a `backend` service. It builds this image and connects it to the existing `postgres` service using `DB_URL=jdbc:postgresql://postgres:5432/instagram`.

## Mapping to this project

The backend already externalizes runtime settings in [application.properties](/Users/saurabh/Documents/Learning/instagram-backend/src/main/resources/application.properties):

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `APP_CORS_ALLOWED_ORIGINS`
- `APP_JWT_SECRET`

That is why the same jar can run from Gradle, Docker Compose, and later Kubernetes. The code does not need to know whether PostgreSQL is on `localhost`, a Compose service name, or a Kubernetes Service DNS name.

The API verification uses [AuthController](/Users/saurabh/Documents/Learning/instagram-backend/src/main/java/com/instagram/backend/controller/AuthController.java) for login and [PostController](/Users/saurabh/Documents/Learning/instagram-backend/src/main/java/com/instagram/backend/controller/PostController.java) for feed reads.

## Hands-on exercise

### Predict

Before running commands, predict:

1. The Docker build should compile the jar in the builder stage.
2. The final image should not contain the Gradle project as the runtime working tree.
3. The backend container should connect to PostgreSQL using the Compose service name `postgres`.
4. Login should return an access token, and feed should return seeded posts when called with that token.

### Build

Start Docker Desktop, then run:

```bash
docker compose up --build -d postgres backend
```

Or build only the image:

```bash
docker build -t instagram-backend:local .
```

Run the automated integration tests:

```bash
./gradlew test
```

### Observe

Inspect the running services:

```bash
docker compose ps
docker compose logs backend --tail=80
```

Verify login:

```bash
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"identifier":"a","password":"a"}')

echo "$LOGIN_RESPONSE"
```

Extract the access token:

```bash
ACCESS_TOKEN=$(printf '%s' "$LOGIN_RESPONSE" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
```

Verify the feed:

```bash
curl -s http://localhost:8080/api/feed \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### Break and recover

Break the database connection safely:

```bash
docker compose stop postgres
docker compose logs backend --tail=80
```

Expected result: the backend logs database connection failures because it cannot reach PostgreSQL.

Recover:

```bash
docker compose start postgres
docker compose restart backend
docker compose ps
```

The recovery works because Compose restores the `postgres` service, and restarting the backend gives Spring Boot a fresh chance to create a working database connection pool.

## Common pitfalls

- Using `localhost` for the database inside a container: inside the backend container, `localhost` means the backend container itself, not PostgreSQL.
- Putting secrets directly into the image: image layers can be inspected later, so runtime secrets belong in environment variables or secret stores.
- Running as root by default: a non-root user limits damage if the app process is compromised.
- Copying the entire repo into the runtime image: this makes the image larger and leaks files that are not needed to run the service.
- Forgetting `.dockerignore`: Docker may send build outputs, Git data, or screenshots into the build context, making builds slower and less predictable.

## Check your understanding

Answer these before opening the answer key.

1. Recall question: what is the difference between a Docker image and a running container?
2. Application question: why does `DB_URL` use `postgres:5432` in Compose instead of `localhost:55432`?
3. Troubleshooting question: the backend container starts, but login returns a database connection error. Which two commands would you run first?

### Teach it back

Explain why Kubernetes needs a container image before it can run the Spring Boot backend.

<details>
<summary>Answer key and explanations</summary>

1. An image is the packaged template: filesystem layers plus metadata and startup command. A container is a running process created from that image.
2. Compose puts services on a shared network and gives each service a DNS name. From the backend container, `postgres:5432` reaches the PostgreSQL container directly. `localhost:55432` is the host-machine mapping, not the container-to-container address.
3. Start with `docker compose ps` to see service state and `docker compose logs backend --tail=80` to inspect the application error. If PostgreSQL looks suspicious, follow with `docker compose logs postgres --tail=80`.

Teach-it-back checklist:

- Names the problem being solved: Kubernetes needs a portable runnable artifact.
- Describes image versus container.
- Connects environment variables to runtime configuration.
- Mentions this project's backend jar and PostgreSQL dependency.

</details>

## Evidence

- Tests: `./gradlew test` passed on 2026-09-27.
- Build: `./gradlew bootJar` passed on 2026-09-27.
- Image build: `docker compose build backend` built the `instagram-backend-backend` image on 2026-09-27.
- Container state: `docker compose ps` showed `instagram-backend` running with `0.0.0.0:8080->8080/tcp` and `instagram-postgres` running with `0.0.0.0:55432->5432/tcp`.
- Startup logs: backend started as user `instagram`, used Java 24, started Tomcat on port `8080`, and connected to `jdbc:postgresql://postgres:5432/instagram`.
- Kubernetes resource state: not applicable yet; this lesson prepares the image for later Pod and Deployment lessons.
- API verification: `POST /api/auth/login` returned `200` for demo user `a`; authenticated `GET /api/feed` returned seeded posts by `a` and `mira.frames`.
- Browser proof, if applicable: not applicable; backend-only container lesson.
- Failure/recovery result: `docker compose stop postgres`, `docker compose start postgres`, and `docker compose restart backend` recovered successfully; login returned `200` after recovery.

## Code and configuration pointers

- [Dockerfile](/Users/saurabh/Documents/Learning/instagram-backend/Dockerfile)
- [.dockerignore](/Users/saurabh/Documents/Learning/instagram-backend/.dockerignore)
- [docker-compose.yml](/Users/saurabh/Documents/Learning/instagram-backend/docker-compose.yml)
- [application.properties](/Users/saurabh/Documents/Learning/instagram-backend/src/main/resources/application.properties)
- [AuthController.java](/Users/saurabh/Documents/Learning/instagram-backend/src/main/java/com/instagram/backend/controller/AuthController.java)
- [PostController.java](/Users/saurabh/Documents/Learning/instagram-backend/src/main/java/com/instagram/backend/controller/PostController.java)

## Commit pointers

- Add commit hash after committing this lesson.

## What comes next

This prepares `K8S-002`: creating a local cluster and learning Kubernetes primitives. Once the backend has an image, a Pod can run it, labels can select it, and later a Deployment can manage multiple replicas.
