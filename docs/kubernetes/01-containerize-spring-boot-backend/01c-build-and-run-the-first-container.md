# 01C - Build and Run the First Container

**Time:** 10-15 minutes

**Starting point:** Complete 01A and 01B. Docker Desktop or another Docker-compatible runtime must be running. You should know that a Dockerfile builds an image and an image starts a container.

## The problem

Reading a Dockerfile only tells us what should be built. We still need to prove that Docker can read this project, create the image, and start a process from it.

This part deliberately performs a small runtime check rather than starting the full Instagram API. The API also needs PostgreSQL and network configuration, which 01E explains without hiding those ideas inside this first build.

## The three terms for this lesson

| Term | Plain meaning |
|---|---|
| **Build context** | The directory of files Docker is allowed to use for a build |
| **Tag** | A readable name attached to an image, such as `instagram-backend:local` |
| **Build cache** | Reusable results from unchanged build steps |

## Must understand

This command builds the project image:

```bash
docker build -t instagram-backend:local .
```

Read it from right to left:

- `.` chooses the current directory as the **build context**.
- `instagram-backend:local` is the image **tag**.
- `-t` attaches that tag to the result.
- `docker build` follows the [Dockerfile](/Users/saurabh/Documents/Learning/instagram-backend/Dockerfile).

The build context matters because Dockerfile `COPY` instructions can only copy files supplied in that context. This project's [.dockerignore](/Users/saurabh/Documents/Learning/instagram-backend/.dockerignore) excludes build output, Git metadata, IDE settings, logs, and proof screenshots that the image build does not need.

After the build, this command lists the image:

```bash
docker image ls instagram-backend
```

An image existing still does not mean a container is running. We will start a very short-lived container that runs the image's Java runtime and prints its version. This isolates the question "Can a process start from this image?" from the later question "Can the full API connect to PostgreSQL?"

## Small exercise

### Predict

Write down what you expect:

1. Will `docker build` leave a long-running backend container?
2. What should a second unchanged build reuse?
3. After a `--rm` container exits, should it remain in `docker ps -a`?

### Build

From the repository root:

```bash
docker build -t instagram-backend:local .
docker image ls instagram-backend
```

Run the image's Java executable instead of the normal backend startup command:

```bash
docker run --rm --entrypoint java instagram-backend:local -version
```

Then build the unchanged project again:

```bash
docker build -t instagram-backend:local .
```

### Expected result

- The first build completes and the image list shows `instagram-backend` with tag `local`.
- The short-lived container prints Java version information and exits successfully.
- `--rm` removes that container after it exits.
- The second build should report cached results for unchanged steps and usually finish faster.

The exercise proves that the image can be built and can start a process. It does **not** yet prove that the Spring Boot API or PostgreSQL connection works.

## Useful later

- A tag is convenient but can be moved to newer image content. Production deployments often use controlled version tags or immutable digests.
- A clean build can reveal files that exist only on a developer's machine and were never committed.
- Changing a source file should invalidate later source-related layers without necessarily repeating every earlier dependency step.

<details>
<summary>Optional deep dive: why the dot matters</summary>

In `docker build ... .`, the final dot is not decoration. It identifies the current directory as the build context.

If you select the wrong directory, a `COPY` instruction may not find `gradlew`, `build.gradle`, or `src`. If you select an unnecessarily broad directory, Docker may process unrelated files. `.dockerignore` narrows the chosen context before it is sent to the builder.

</details>

## Check your understanding

<details>
<summary>1. What does the final dot mean in <code>docker build -t instagram-backend:local .</code>?</summary>

It chooses the current directory as the build context: the set of files available to Docker for this build after `.dockerignore` exclusions are applied.

</details>

<details>
<summary>2. What does <code>instagram-backend:local</code> identify?</summary>

It is a readable image tag. `instagram-backend` is the repository/name portion and `local` is the tag portion used here to identify the locally built variant.

</details>

<details>
<summary>3. Does <code>docker build</code> start the Spring Boot API?</summary>

No. It creates an image. Starting a container from that image is a separate runtime action.

</details>

<details>
<summary>4. Why did the exercise override the normal entry point with <code>java -version</code>?</summary>

It performs one controlled check: the final image can start its Java runtime. Starting the real backend would also test database networking and runtime configuration, concepts intentionally deferred to 01E.

</details>

<details>
<summary>5. Why is the second unchanged build usually faster?</summary>

Docker can reuse cached results for build steps whose instruction and inputs have not changed. It does not need to repeat identical work simply to produce the same layers.

</details>

<details>
<summary>6. Does a successful Java version command prove the API works?</summary>

No. It proves a process can start and the Java runtime exists. The API still needs its JAR startup, configuration, database connection, port access, and a real HTTP request to be verified.

</details>

## Stop/go check

Continue when you can explain all three observations separately:

1. `docker build` created an image.
2. `docker image ls` found the stored image.
3. `docker run` created a temporary container and process.

If you are still treating those as one action, repeat the exercise and say aloud what exists after each command.
