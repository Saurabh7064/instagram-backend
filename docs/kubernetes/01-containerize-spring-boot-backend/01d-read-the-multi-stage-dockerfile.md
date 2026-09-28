# 01D — Read the multi-stage Dockerfile

**Time:** 10–15 minutes

**Goal:** Read this project's Dockerfile from top to bottom and explain why it builds the application in one stage but runs it in another.

## Where this micro-lesson fits

The earlier parts introduced the Java source, the executable JAR, images, and containers. This part connects those ideas to the actual [Dockerfile](/Users/saurabh/Documents/Learning/instagram-backend/Dockerfile).

## Assumptions

This lesson assumes that you already know:

- Java source must be compiled before it can run;
- `./gradlew bootJar` creates a Spring Boot JAR;
- an image is a reusable package, while a container is a running instance of that package.

You do **not** need to know Dockerfile syntax yet. This lesson teaches the lines used by this project.

## The problem we are solving

Building the backend needs development tools: the Java compiler, the Gradle wrapper, source code, and downloaded dependencies. Running the finished backend needs much less: a Java runtime and the JAR.

If every development tool is shipped in production, the final image is larger and contains files that the running application does not need. If none of the build tools are available, however, there is no JAR to run.

The solution is to use one temporary workspace to **build** the JAR and a second, cleaner workspace to **run** it.

Think of a restaurant:

```text
kitchen with ingredients and tools        serving tray
              build the meal        ->    carry only the finished meal
              build stage                  runtime stage
```

The customer does not need the oven, sacks of flour, or dirty mixing bowls. The running container does not need the compiler, source tree, or Gradle cache.

## Five terms for this lesson

1. **Base image** — the existing image used as the starting point for a new stage.
2. **JDK/JRE** — the JDK includes Java development tools; the JRE contains what is needed to run Java.
3. **Build stage** — the temporary part that compiles and packages the application.
4. **Runtime stage** — the final part that contains what is needed to run the application.
5. **Layer** — a reusable result produced by an image-building instruction.

## Must understand

### 1. The first half builds the JAR

Open the real [Dockerfile](/Users/saurabh/Documents/Learning/instagram-backend/Dockerfile). Its first line is:

```dockerfile
FROM eclipse-temurin:24-jdk AS build
```

This says:

- start from an image that contains the Java 24 JDK;
- call this part `build` so that a later part can copy from it.

The next lines create a predictable directory and copy the Gradle build files:

```dockerfile
WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN ./gradlew dependencies --no-daemon
```

`WORKDIR /workspace` means that later relative paths start inside `/workspace`. The two `COPY` instructions place the wrapper and build definitions there. `RUN` executes while Docker is creating the image, so Gradle downloads the declared dependencies at **build time**.

Only after that does the source arrive:

```dockerfile
COPY src ./src
RUN ./gradlew bootJar --no-daemon
```

Gradle compiles the source and creates a JAR under `/workspace/build/libs/`. The build stage has now done its job.

### 2. The second half starts fresh and runs the JAR

A second `FROM` begins a new stage:

```dockerfile
FROM eclipse-temurin:24-jre
```

This starting image contains a Java runtime rather than the full development kit. Files from the build stage do not automatically appear here.

The runtime stage creates its working directory and a dedicated operating-system user:

```dockerfile
WORKDIR /app

RUN groupadd --system instagram && useradd --system --gid instagram instagram
```

Then one line crosses the boundary between the two stages:

```dockerfile
COPY --from=build /workspace/build/libs/*.jar app.jar
```

Read it as: “From the stage named `build`, take the generated JAR and put it here as `/app/app.jar`.” The Java source, compiler, and Gradle cache are left behind.

Finally:

```dockerfile
USER instagram
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

- `USER` makes the application run as the dedicated `instagram` user.
- `EXPOSE 8080` documents the port the application intends to use. It does not create a laptop-to-container port mapping.
- `ENTRYPOINT` is the command started when a container is created from the image.

The Java process is the container's main process. If that process exits, the container stops.

### 3. Keep build time and run time separate

```text
docker build
  -> execute Dockerfile instructions
  -> run Gradle
  -> create app.jar
  -> produce the final image

docker run / docker compose up
  -> create a container from that image
  -> execute java -jar /app/app.jar
```

The Gradle commands do not run every time the container starts. They already ran while the image was built.

If you remember only one sentence, remember this:

> The first stage creates the application artifact; the second stage becomes the application environment.

## Small exercise — build what you just read

### Prediction

Before running the command, predict which stage will take longer on the first build and whether the final image will start Gradle or Java.

A good prediction is: the build stage takes longer because it downloads dependencies and compiles code; a container made from the final image starts Java directly.

### Exact command

Run this from the repository root:

```bash
docker build -t instagram-backend:lesson-01d .
```

The final `.` is the build context. Docker can use the project files that are not removed by [.dockerignore](/Users/saurabh/Documents/Learning/instagram-backend/.dockerignore).

### Expected result

The output should show two different base images, a successful `./gradlew bootJar`, a `COPY --from=build` step, and a final image tagged `instagram-backend:lesson-01d`. A second unchanged build will usually reuse several cached layers and finish faster.

If the build fails, read the **first meaningful error**, not only the final “build failed” line. A dependency-download error and a Java-compilation error have different causes.

## Useful later

The copy order is intentional. The [Gradle build file](/Users/saurabh/Documents/Learning/instagram-backend/build.gradle), wrapper, and dependency definitions are copied before source files such as [InstagramBackendApplication.java](/Users/saurabh/Documents/Learning/instagram-backend/src/main/java/com/instagram/backend/InstagramBackendApplication.java). When only application source changes, Docker may reuse the earlier dependency layer rather than downloading everything again.

The [.dockerignore](/Users/saurabh/Documents/Learning/instagram-backend/.dockerignore) excludes `.gradle`, `build`, Git data, IDE files, logs, and demo screenshots. That gives Docker a smaller, cleaner build context and reduces accidental copying of irrelevant local files.

<details>
<summary>Optional deep dive</summary>

The JSON-array form of `ENTRYPOINT` starts Java directly instead of asking a shell to interpret a command string. Direct startup generally makes operating-system signals easier for Java to receive. That matters when Docker or Kubernetes asks a container to stop gracefully.

Image tags such as `lesson-01d` are convenient names, but a tag can be reused for different image content. Production systems often record an immutable image digest so that “deploy this version” points to one exact image.

</details>

## Check your understanding

Try to answer each question aloud before expanding it.

<details>
<summary>1. Why does this project use a JDK in the first stage and a JRE in the second?</summary>

The first stage must compile Java and run the Gradle build, so it needs development tools supplied by the JDK. The second stage only executes the finished JAR, so a JRE is sufficient. Separating them prevents unnecessary build tools from entering the final runtime image.

</details>

<details>
<summary>2. What exactly crosses from the build stage into the runtime stage?</summary>

Only the JAR matched by `/workspace/build/libs/*.jar` is copied across, and it is renamed `app.jar` in `/app`. The source tree, Gradle cache, compiler, and other build-stage files are not copied.

</details>

<details>
<summary>3. Does `RUN ./gradlew bootJar --no-daemon` execute whenever a container starts?</summary>

No. `RUN` executes while Docker builds the image. Starting a container executes the final `ENTRYPOINT`, which is `java -jar /app/app.jar` in this project.

</details>

<details>
<summary>4. What does the final dot in `docker build -t instagram-backend:lesson-01d .` mean?</summary>

It selects the current directory as the build context. Dockerfile `COPY` instructions can access included files from that context, after exclusions in `.dockerignore` are applied.

</details>

<details>
<summary>5. Would removing `COPY --from=build ...` still leave a runnable final image?</summary>

No. The fresh runtime stage would contain Java, but not this project's application JAR. Its entrypoint would try to open `/app/app.jar` and fail because the file was never transferred from the build stage.

</details>

## Stop/go check

**GO to 01E** if you can explain, without reading, (1) why there are two `FROM` lines, (2) which stage runs Gradle, and (3) which single project artifact reaches the final image.

**STOP and reread “Must understand”** if “build stage” still sounds like a running backend container or if you think `EXPOSE 8080` publishes port `8080` on your laptop. That port distinction is the starting point of the next lesson.
