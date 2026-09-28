# 01A - From Code to a Running Program

**Time:** 10-15 minutes

**Starting point:** You do not need to know Docker or Kubernetes. You only need to know that this repository contains Java files and that a terminal command can run a program.

## Why this comes first

Before putting the backend in a container, we need to be clear about what the backend actually becomes when it runs. Source code, a packaged application, and a running application are related, but they are not the same thing.

If those ideas are mixed together, later phrases such as "build an image" and "restart the container" sound mysterious. This micro-lesson builds the foundation beneath those phrases.

## The four terms for this lesson

| Term | Plain meaning | Is it running? |
|---|---|---|
| **Source code** | The Java instructions that developers read and edit | No |
| **JAR** | A packaged Java application containing compiled code and libraries | No |
| **JVM** | The Java Virtual Machine that knows how to execute Java bytecode | The JVM is part of a running Java process |
| **Process** | A program that the operating system is executing now | Yes |

Do not try to learn container vocabulary yet. These four ideas are enough for this part.

## Must understand

Open the project's [Spring Boot entry-point source file](/Users/saurabh/Documents/Learning/instagram-backend/src/main/java/com/instagram/backend/InstagramBackendApplication.java). The project also contains Java controllers, services, and repositories. A `.java` file does not listen on port `8080` by itself. It is an input to the build.

This project uses the Gradle wrapper to compile and package those instructions:

```bash
./gradlew bootJar
```

The result is a Spring Boot JAR under `build/libs/`. The JAR is a file on disk. It can be copied, stored, or deleted, but it is not alive.

The operating system starts the application when Java executes that file:

```bash
java -jar build/libs/<jar-file-name>.jar
```

At that point there is a Java process. The process can consume CPU and memory, connect to PostgreSQL, and listen for HTTP requests. If that process exits, the API stops even though the source code and JAR still remain on disk.

The flow is:

```text
Java source --Gradle compiles/packages--> JAR --JVM executes--> running process
```

The project's [Gradle build file](/Users/saurabh/Documents/Learning/instagram-backend/build.gradle) describes how to compile and package the application. The [application configuration](/Users/saurabh/Documents/Learning/instagram-backend/src/main/resources/application.properties) supplies settings the running process reads, such as its database connection.

## A useful mental model

Think of source code as a recipe, the JAR as a prepared meal in a sealed box, the JVM as the equipment that can serve that kind of meal, and the process as the meal actually being served.

The analogy is limited: a JAR contains executable bytecode rather than food, and a JVM is software rather than kitchen equipment. The important point is that a stored artifact is different from active work.

## Small exercise

### Predict

Before running anything, write down your prediction:

- Which directory will contain the packaged application?
- Will building the JAR start the backend server?

### Do

From the repository root, run:

```bash
./gradlew bootJar
ls -lh build/libs
```

Then inspect whether the build command left a backend Java process running:

```bash
pgrep -fl 'instagram-backend|java'
```

Other Java programs may already be running, so read the command output rather than assuming every Java process is this backend.

### Expected result

You should see a `.jar` file in `build/libs/`. The `bootJar` command should finish and return control to the terminal. It packages the application; it does not intentionally leave the Instagram backend running.

That is the key observation: **building creates an artifact, while running creates a process**.

## Useful later

- A JAR still needs a compatible Java runtime wherever it is executed.
- A process has temporary runtime state, such as memory and open network connections.
- Kubernetes eventually manages running processes indirectly, but it does not replace the Java build step.

<details>
<summary>Optional deep dive: what is inside a JAR?</summary>

A JAR is based on the ZIP file format. It can contain compiled `.class` files, metadata, and application resources. A Spring Boot executable JAR also includes the libraries and launcher structure needed to start the application with `java -jar`.

You can list its entries without extracting it:

```bash
jar tf build/libs/*.jar | head
```

You do not need to memorize the internal directories for this Kubernetes course.

</details>

## Check your understanding

Try to answer before expanding each explanation.

<details>
<summary>1. Is a Java source file a running backend?</summary>

No. A source file contains instructions for people and build tools. It must be compiled and then executed before an operating-system process can serve API requests.

</details>

<details>
<summary>2. What does <code>./gradlew bootJar</code> create?</summary>

It compiles the project and creates an executable Spring Boot JAR under `build/libs/`. It creates a stored artifact; it does not by itself mean that the API is running.

</details>

<details>
<summary>3. Why does a JAR need a JVM?</summary>

The JAR contains Java bytecode and resources. The JVM supplies the runtime that loads and executes that bytecode on the current operating system.

</details>

<details>
<summary>4. If the Java process stops, which things can still remain?</summary>

The source files and JAR can remain because they are stored files. The live API disappears because the process that accepted requests is no longer executing.

</details>

<details>
<summary>5. A JAR exists in <code>build/libs</code>, but <code>curl localhost:8080</code> fails. Is that contradictory?</summary>

No. The JAR proves that packaging succeeded. It does not prove that a JVM is currently executing it, that port `8080` is open, or that dependencies such as PostgreSQL are available.

</details>

## Stop/go check

Continue to 01B only if you can explain this sentence without looking back:

> Gradle turns source code into a JAR, and a JVM executes that JAR as a running process.

Also make sure you can answer: **Which one consumes CPU while the API is serving requests?** The answer is the running process.

If that distinction is still fuzzy, repeat the small exercise and point at the source directory, JAR file, and process as three separate things.
