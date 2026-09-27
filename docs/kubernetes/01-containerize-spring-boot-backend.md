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

### The build stage, line by line

```dockerfile
FROM eclipse-temurin:24-jdk AS build
```

`FROM` chooses the starting image. A **JDK** includes the Java compiler and other build tools. `AS build` names this stage so a later stage can copy its output.

```dockerfile
WORKDIR /workspace
```

Later build commands run relative to `/workspace`. Docker creates the directory when necessary.

```dockerfile
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN ./gradlew dependencies --no-daemon
```

These lines copy the Gradle wrapper and build definitions, then resolve dependencies. They appear before the application source because Docker caches image layers. Changing one Java file should not force every dependency to be downloaded again when the build definitions are unchanged.

`RUN` happens while the image is being built. It is not repeated each time a container starts.

```dockerfile
COPY src ./src
RUN ./gradlew bootJar --no-daemon
```

The source is copied only after dependency setup. Gradle then compiles it and creates the executable JAR under `/workspace/build/libs/`.

### The runtime stage, line by line

```dockerfile
FROM eclipse-temurin:24-jre
```

The second `FROM` starts a fresh stage. A **JRE** provides what is needed to execute Java but excludes many development tools found in the JDK.

```dockerfile
WORKDIR /app
RUN groupadd --system instagram && useradd --system --gid instagram instagram
```

The runtime working directory is `/app`. A dedicated operating-system group and user are created so the backend does not run as the powerful root user.

```dockerfile
COPY --from=build /workspace/build/libs/*.jar app.jar
```

Only the built JAR is copied from the builder stage. The source tree, Gradle cache, and compiler do not enter the final image.

```dockerfile
USER instagram
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

- `USER` selects the non-root runtime identity.
- `EXPOSE 8080` documents the intended container port. It does **not** publish the port to the host.
- `ENTRYPOINT` supplies the default process. The JSON-array form starts Java directly and provides clearer operating-system signal handling than wrapping it in a shell.

When this Java process exits, the container exits. A container is not a background machine that remains alive independently of its main process.

### Why two stages?

| Build stage | Runtime stage |
|---|---|
| Compiles the project | Runs the finished project |
| Needs the JDK and Gradle | Needs a JRE |
| Contains source and build caches | Contains the application JAR |
| Temporary input to the build | Becomes the final deployable image |

This makes the final image smaller, reduces unnecessary tools and files, and creates a clearer security boundary between building and running.

### Build context, layers, and cache

In `docker build -t instagram-backend:local .`, the final `.` is the **build context**. Docker may send files from that directory to the builder, and `COPY` can only access included context files.

The [.dockerignore](/Users/saurabh/Documents/Learning/instagram-backend/.dockerignore) removes items such as Git history, IDE metadata, previous build output, logs, and demo screenshots from the context. This reduces transfer size, avoids accidental disclosure, and prevents irrelevant files from invalidating cached layers.

Most Dockerfile instructions create reusable layers. If only a file under `src/` changes, the earlier Gradle and dependency layers can often be reused. This explains why a second unchanged build is usually faster.

### Image tag versus image identity

This command creates the human-readable tag `instagram-backend:local`:

```bash
docker build -t instagram-backend:local .
```

A tag can later be moved to a rebuilt image. Production systems often use controlled version tags or immutable digests so operators know exactly which content is deployed.

### Networking: why `localhost` is confusing

`localhost` means “the network environment of the process making the request.” Inside the backend container, `localhost` points to the backend container—not the laptop and not the PostgreSQL container.

Compose gives services DNS names on a shared network. The backend therefore reaches PostgreSQL at:

```text
postgres:5432
```

The Compose mapping `55432:5432` has two sides:

- `55432` is the port on the laptop;
- `5432` is the port inside the PostgreSQL container.

A database client running on the laptop uses `localhost:55432`. The backend container uses `postgres:5432` and does not need to travel through the host mapping.

Keep these port concepts separate:

- **listening:** a process has opened a port inside its network environment;
- **exposed:** an image documents its intended port with `EXPOSE`;
- **published:** the runtime maps a host port to a container port;
- **service discovery:** a DNS name such as `postgres` resolves to the desired service.

### Configuration at runtime

The image should hold the stable application and runtime. Environment-specific values should be supplied when a container starts. Spring syntax such as:

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:55432/instagram}
```

means “use `DB_URL` when present; otherwise use the value after the colon.” That is why the same image can work directly on a laptop, in Compose, and later in Kubernetes.

Real credentials should not be copied into an image. Images can be inspected and shared, and secrets baked into layers are difficult to rotate safely.

### What `depends_on` does not mean

Compose's `depends_on` provides basic startup ordering. It does not necessarily mean PostgreSQL is fully initialized and accepting connections before the backend begins connecting.

“The container process started” and “the application is ready for traffic” are different statements. Later Kubernetes lessons make this visible through startup and readiness probes.

### A project-specific scaling warning

JWT authentication helps multiple backend instances because any replica with the signing key can validate a request without server-local login-session memory.

Uploaded media, however, is currently stored on local temp-backed storage. If container A stores a file and a later request reaches container B, container B may not have it. Containerization does not remove this stateful limitation; production media should eventually move to shared object storage.

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

First build the application artifact without Docker:

```bash
./gradlew bootJar
ls -lh build/libs
```

This proves the application can be packaged. It does not prove that PostgreSQL is reachable or that the API works at runtime.

Start Docker Desktop, then build and start the two services:

```bash
docker compose up --build -d postgres backend
```

Or build only the image:

```bash
docker build -t instagram-backend:local .
```

Inspect the image and repeat the build to observe caching:

```bash
docker image ls instagram-backend
docker image inspect instagram-backend:local
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
docker compose logs postgres --tail=80
```

Verify the runtime user and main process:

```bash
docker compose exec backend id
docker compose exec backend ps
```

Expected: the application runs as the `instagram` user rather than UID `0` (root).

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

Repeat login and feed verification after recovery. Recovery is not proven merely because both containers say `Up`; useful API behavior must return.

When finished, stop and remove the Compose containers and network:

```bash
docker compose down
```

This keeps the named PostgreSQL volume. `docker compose down --volumes` would also delete that volume and can erase the local database, so do not add `--volumes` casually.

## Common pitfalls

- Using `localhost` for the database inside a container: inside the backend container, `localhost` means the backend container itself, not PostgreSQL.
- Putting secrets directly into the image: image layers can be inspected later, so runtime secrets belong in environment variables or secret stores.
- Running as root by default: a non-root user limits damage if the app process is compromised.
- Copying the entire repo into the runtime image: this makes the image larger and leaks files that are not needed to run the service.
- Forgetting `.dockerignore`: Docker may send build outputs, Git data, or screenshots into the build context, making builds slower and less predictable.
- Assuming `EXPOSE 8080` publishes a host port: it is documentation; Compose `ports` performs the mapping.
- Assuming `depends_on` means PostgreSQL is ready: startup ordering is weaker than a readiness guarantee.
- Assuming `Up` means healthy: the process can run while database-backed requests fail.
- Expecting local uploaded files to appear in every replica: container-local writable storage is not automatically shared.

## Check your understanding

Answer these in your own words before expanding the answers. The goal is to explain and predict, not memorize vocabulary.

### Part A — foundations

1. What is the difference between Java source code and a running process?
<details>
<summary>Answer</summary>

Source code is input for developers and build tools. A process is a program currently executing with CPU, memory, and operating-system resources.

</details>

2. What does `./gradlew bootJar` produce?
<details>
<summary>Answer</summary>

It produces an executable Spring Boot JAR under `build/libs/`.

</details>

3. Why does the JAR still need a JVM?
<details>
<summary>Answer</summary>

The JAR contains Java bytecode. The JVM loads and executes it and supplies the Java runtime libraries.

</details>

4. What is a container image?
<details>
<summary>Answer</summary>

An image is a read-only, layered application package containing files and runtime metadata such as the startup command.

</details>

5. What is a container?
<details>
<summary>Answer</summary>

A container is a running instance created from an image, with a main process and isolated runtime state.

</details>

6. Can one image create multiple containers? What is shared and what remains separate?
<details>
<summary>Answer</summary>

Yes. They share the packaged image contents. Each container has its own process, environment, network identity, and writable layer unless storage is intentionally shared.

</details>

7. What does a Dockerfile do?
<details>
<summary>Answer</summary>

It is the recipe Docker uses to build an image.

</details>

8. What does an image registry do?
<details>
<summary>Answer</summary>

It stores and distributes images so other machines or cluster nodes can pull them.

</details>

9. Why does Kubernetes normally need an image rather than only a Git repository?
<details>
<summary>Answer</summary>

Kubernetes schedules runnable artifacts. It should not recreate a development environment and compile the repository every time a Pod is replaced.

</details>

10. Give two differences between a container and a virtual machine.
<details>
<summary>Answer</summary>

A virtual machine usually includes a guest kernel and complete operating system. A container shares the host kernel and is generally smaller and faster to start.

</details>

### Part B — read the Dockerfile

11. Why does the build stage use a JDK while the runtime stage uses a JRE?
<details>
<summary>Answer</summary>

Compilation requires JDK development tools. Running an already-built JAR needs the smaller JRE.

</details>

12. What does `AS build` enable?
<details>
<summary>Answer</summary>

It names the first stage so the final stage can copy the built artifact from it.

</details>

13. Does `RUN ./gradlew bootJar` execute whenever a container starts? Why?
<details>
<summary>Answer</summary>

No. `RUN` executes during image construction. Container startup executes the `ENTRYPOINT`.

</details>

14. Why are the Gradle build files copied before `src`?
<details>
<summary>Answer</summary>

Stable build definitions allow Docker to reuse dependency layers when only source code changes.

</details>

15. What does `COPY --from=build` accomplish?
<details>
<summary>Answer</summary>

It copies the generated JAR out of the builder stage into the clean runtime stage.

</details>

16. Why does the final image not need source code or the Java compiler?
<details>
<summary>Answer</summary>

Java executes compiled bytecode in the JAR. Source and compiler tools are build inputs, not runtime requirements.

</details>

17. What security benefit does `USER instagram` provide?
<details>
<summary>Answer</summary>

A compromised application process generally has fewer privileges than it would as root, reducing potential damage.

</details>

18. What does `EXPOSE 8080` do, and what does it not do?
<details>
<summary>Answer</summary>

It documents the expected container port. It does not publish that port to the host or prove a process is listening.

</details>

19. What process does `ENTRYPOINT` start?
<details>
<summary>Answer</summary>

It starts `java -jar /app/app.jar` as the container's main process.

</details>

20. What happens to the container when that main process exits?
<details>
<summary>Answer</summary>

The container stops because its main process ended.

</details>

### Part C — configuration and networking

21. Why should real database credentials and JWT secrets not be built into the image?
<details>
<summary>Answer</summary>

Image layers and metadata can be inspected and images may be shared. Embedded secrets are hard to rotate and travel with every copy.

</details>

22. Explain the default-value behavior in `${DB_URL:jdbc:postgresql://localhost:55432/instagram}`.
<details>
<summary>Answer</summary>

Spring uses `DB_URL` when provided; otherwise it uses the value after the colon as a local default.

</details>

23. Why does the backend use `postgres:5432` inside Compose?
<details>
<summary>Answer</summary>

Compose gives each service an internal DNS name. `postgres` resolves to the database container, which listens internally on `5432`.

</details>

24. Why is `localhost:55432` wrong from inside the backend container?
<details>
<summary>Answer</summary>

`localhost` inside the backend container refers to the backend container. `55432` is the host-side published port, not the database's internal address.

</details>

25. Explain both numbers in `55432:5432`.
<details>
<summary>Answer</summary>

`55432` is the laptop's host port; `5432` is the PostgreSQL container port receiving the forwarded traffic.

</details>

26. Does backend-to-PostgreSQL traffic need the host port `55432`?
<details>
<summary>Answer</summary>

No. Containers communicate directly over the Compose network using `postgres:5432`.

</details>

27. What is the difference between listening, exposed, and published ports?
<details>
<summary>Answer</summary>

Listening means a process opened a port. `EXPOSE` documents an intended port. Publishing maps a host port to a container port.

</details>

28. Why can the same image run in Compose and later Kubernetes without recompiling Java?
<details>
<summary>Answer</summary>

Environment-specific addresses and secrets are supplied at runtime, while the compiled application and Java runtime remain unchanged in the image.

</details>

### Part D — layers, storage, and security

29. What is the Docker build context?
<details>
<summary>Answer</summary>

The build context is the directory tree Docker may send to the builder and use in `COPY` instructions. Here, `.` means the repository directory.

</details>

30. Name three things excluded by `.dockerignore` and explain why that helps.
<details>
<summary>Answer</summary>

Examples include `.git`, `build`, `.gradle`, logs, IDE files, and screenshots. Excluding them reduces transfer size, accidental disclosure, and unnecessary cache invalidation.

</details>

31. Why is a second unchanged image build often faster?
<details>
<summary>Answer</summary>

Docker can reuse unchanged image layers instead of executing every instruction again.

</details>

32. Why does changing only a Java source file usually preserve the cached dependency layer?
<details>
<summary>Answer</summary>

Build definitions and resolved dependencies appear in earlier layers. A later `COPY src` change invalidates that layer and following layers, not the unchanged earlier ones.

</details>

33. Does running as non-root guarantee security? Explain.
<details>
<summary>Answer</summary>

No. It reduces privilege but does not fix vulnerable code, leaked secrets, unsafe networks, or excessive external permissions.

</details>

34. What happens to files stored only in a deleted container's writable layer?
<details>
<summary>Answer</summary>

Those files disappear when the container is deleted unless they were written to a mounted volume or external storage.

</details>

35. Why does PostgreSQL use a named Compose volume?
<details>
<summary>Answer</summary>

It keeps database data separate from the disposable PostgreSQL container so data can survive container replacement.

</details>

36. Why is the current local media-upload storage unsafe across multiple backend replicas?
<details>
<summary>Answer</summary>

A file written by one backend container does not automatically exist in another container's local filesystem. Shared object storage is the eventual production solution.

</details>

### Part E — troubleshooting scenarios

37. Compose says the backend is `Up`, but login reports a database error. How can both be true?
<details>
<summary>Answer</summary>

`Up` only says the main process exists. The application can still be unable to use a required dependency.

</details>

38. Which three commands would you run first to inspect the backend and PostgreSQL?
<details>
<summary>Answer</summary>

Start with `docker compose ps`, `docker compose logs backend --tail=80`, and `docker compose logs postgres --tail=80`.

</details>

39. The backend logs show attempts to reach `localhost:55432`. What is likely wrong?
<details>
<summary>Answer</summary>

`DB_URL` is probably missing or overridden incorrectly, causing the host-oriented default to be used inside the container.

</details>

40. Docker says host port `8080` is already allocated. Must Spring's internal port change? What else can you do?
<details>
<summary>Answer</summary>

No. Stop the conflicting host process or publish a different host port such as `8081:8080`; the application may continue listening on container port `8080`.

</details>

41. You changed Java code, but the running API still behaves like the old version. List a sensible debugging order.
<details>
<summary>Answer</summary>

Confirm the source was saved, rebuild the image, inspect build output/cache, recreate the backend service, verify which image/container is running, and call the intended host and port.

</details>

42. PostgreSQL was recreated and its data disappeared. What configuration or command would you investigate?
<details>
<summary>Answer</summary>

Inspect the named-volume mount and whether a command such as `docker compose down --volumes` deleted it.

</details>

43. Why is a real login plus protected feed call stronger evidence than startup logs alone?
<details>
<summary>Answer</summary>

The calls verify host networking, HTTP routing, request handling, database access, token issuance, and authorization together. Logs may prove only partial startup.

</details>

44. What does `depends_on` guarantee, and what does it not guarantee?
<details>
<summary>Answer</summary>

It supplies basic startup ordering. It does not guarantee that PostgreSQL is healthy and ready to accept connections.

</details>

45. Why is `docker compose down --volumes` more destructive than `docker compose down`?
<details>
<summary>Answer</summary>

`down` preserves named volumes by default. Adding `--volumes` removes them and can erase local database data.

</details>

### Part F — connecting this lesson to Kubernetes

46. What artifact from this lesson will a Kubernetes Pod reference?
<details>
<summary>Answer</summary>

A Pod will reference the backend container image, eventually through a local image store or registry.

</details>

47. If Kubernetes creates three Pods from one image, how many running application instances exist?
<details>
<summary>Answer</summary>

Three running application instances exist, one in each Pod, even though they were created from the same packaged image.

</details>

48. Why does JWT authentication help those replicas behave interchangeably?
<details>
<summary>Answer</summary>

Any replica with the signing key can validate a JWT without depending on server-local session memory.

</details>

49. Which current feature still prevents the replicas from being completely interchangeable?
<details>
<summary>Answer</summary>

Temp-backed local media storage is replica-local, so a later request routed elsewhere may not find an uploaded file.

</details>

50. In one paragraph, explain the path from Java source to a Kubernetes-managed process.
<details>
<summary>Answer</summary>

Gradle compiles the Java source into a Spring Boot JAR. Docker's JDK stage builds it, the JRE stage packages only the runtime artifact and startup metadata, and the result becomes an image. Kubernetes later asks a node runtime to create and manage containers from that image.

</details>

### Teach it back

Explain to a developer who has never used containers:

1. what an image is;
2. what a container is;
3. why the Dockerfile has two stages;
4. how the backend finds PostgreSQL;
5. why Kubernetes needs this work first.

Teach-it-back checklist:

- Names the problem being solved: Kubernetes needs a portable runnable artifact.
- Describes image versus container.
- Connects environment variables to runtime configuration.
- Mentions this project's backend jar and PostgreSQL dependency.
- Distinguishes host ports from container ports.
- Identifies local media storage as a remaining horizontal-scaling limitation.

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

- `c9007bf` added the Dockerfile, `.dockerignore`, Compose backend service, lesson note, and backlog completion.

## What comes next

This prepares `K8S-002`: creating a local cluster and learning Kubernetes primitives. Once the backend has an image, a Pod can run it, labels can select it, and later a Deployment can manage multiple replicas.
