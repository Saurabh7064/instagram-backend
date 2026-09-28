# 01E — Container networking and configuration

**Time:** 10–15 minutes

**Goal:** Predict how the backend reaches PostgreSQL and how the same JAR receives different settings in different environments.

## Where this micro-lesson fits

In 01D, the final image declared the Java command that will run when a container starts. This part explains how traffic reaches that process after it starts and how the process reaches the PostgreSQL container defined in [docker-compose.yml](/Users/saurabh/Documents/Learning/instagram-backend/docker-compose.yml).

## Assumptions

This lesson assumes that you already know:

- an image creates a container;
- the backend and PostgreSQL run as two separate container processes;
- the backend reads Spring settings from [application.properties](/Users/saurabh/Documents/Learning/instagram-backend/src/main/resources/application.properties).

You do **not** need prior networking knowledge. We will follow only two connections: laptop to backend, and backend to PostgreSQL.

## The problem we are solving

On the laptop, PostgreSQL is reachable at `localhost:55432`. If the backend runs inside its own container and uses that same address, it looks for PostgreSQL inside the **backend container itself**. Nothing is listening there, so the connection fails.

Containers also need different values in different places. A developer may run the JAR directly against `localhost:55432`; Compose should run the same JAR against `postgres:5432`; Kubernetes will eventually provide another address. Rebuilding the code for every address would be slow and error-prone.

The solution has two parts:

1. Compose gives containers a shared network and names they can use to find one another.
2. Environment values select the correct address when the container starts.

## Five new terms

1. **Network boundary** — the edge of one networking environment; the laptop and each container have their own view.
2. **`localhost`** — the current networking environment of the process making the request.
3. **Service name** — a Compose name, such as `postgres`, that other services can use as an address.
4. **Port mapping** — a rule connecting a port on the laptop to a port inside a container.
5. **Environment variable** — a named runtime value supplied to a process without changing its application code.

## Must understand

### 1. `localhost` means “here,” not “my laptop”

The meaning of `localhost` changes with the location of the process:

```text
Browser or curl on laptop
  localhost:8080
       |
       | Compose mapping 8080:8080
       v
Backend container
  application listens on 8080
       |
       | shared Compose network
       v
PostgreSQL container
  postgres:5432
```

From your terminal, `localhost` means the laptop. From the Java process inside the backend container, `localhost` means the backend container. It does not jump across to the laptop or database container.

### 2. Read a port mapping from left to right

The PostgreSQL service in [docker-compose.yml](/Users/saurabh/Documents/Learning/instagram-backend/docker-compose.yml) contains:

```yaml
ports:
  - "55432:5432"
```

Read this as:

```text
laptop port : container port
55432       : 5432
```

A database tool running on the laptop can connect to `localhost:55432`. Another container on the Compose network goes directly to PostgreSQL's own port and does not need the laptop-side mapping.

The backend service has:

```yaml
ports:
  - "8080:8080"
```

Therefore `curl http://localhost:8080/...` on the laptop reaches port `8080` inside the backend container.

The `EXPOSE 8080` line in the [Dockerfile](/Users/saurabh/Documents/Learning/instagram-backend/Dockerfile) only documents the intended container port. The Compose `ports` entry creates the actual mapping.

### 3. Containers find services by Compose name

The two top-level service names are `postgres` and `backend`. Compose places them on a shared default network and makes `postgres` usable as an address from the backend.

That is why the backend receives:

```yaml
DB_URL: jdbc:postgresql://postgres:5432/instagram
```

Break it into pieces:

- `postgres` — the Compose service to find;
- `5432` — the database port inside that service's container;
- `instagram` — the database name.

It must not use `localhost:55432` from inside the backend container.

### 4. Spring chooses environment values at startup

The project's [application.properties](/Users/saurabh/Documents/Learning/instagram-backend/src/main/resources/application.properties) says:

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:55432/instagram}
spring.datasource.username=${DB_USERNAME:instagram}
spring.datasource.password=${DB_PASSWORD:instagram}
```

For the first line, Spring follows this rule:

```text
Is DB_URL present?
  yes -> use its value
  no  -> use jdbc:postgresql://localhost:55432/instagram
```

Running the application directly on the laptop can use the fallback. Running it with Compose supplies `DB_URL`, so the same JAR uses `postgres:5432` instead.

Compose also supplies `APP_CORS_ALLOWED_ORIGINS` and `APP_JWT_SECRET`. The corresponding placeholders in [application.properties](/Users/saurabh/Documents/Learning/instagram-backend/src/main/resources/application.properties) let those runtime values override local-development defaults.

If you remember only one sentence, remember this:

> The connection address must be correct from the point of view of the process making the connection.

## Small exercise — trace the effective Compose configuration

### Prediction

Before running the command, predict these three values:

1. the database address passed to the backend;
2. the laptop port mapped to the backend;
3. the laptop port mapped to PostgreSQL.

Write your prediction down before checking it.

### Exact command

From the repository root, run:

```bash
docker compose config
```

This renders the configuration Docker Compose will use without starting the application.

### Expected result

In the rendered `backend` service, expect `DB_URL` to contain `postgres:5432`, and expect a published backend port of `8080` targeting container port `8080`. In the rendered `postgres` service, expect published port `55432` targeting container port `5432`.

Formatting may be more verbose than the source YAML, but the values should mean the same thing. If `docker compose config` reports a parsing error, fix the YAML structure before debugging Java or networking.

## Useful later

`depends_on` in [docker-compose.yml](/Users/saurabh/Documents/Learning/instagram-backend/docker-compose.yml) starts PostgreSQL before the backend is started, but it does not prove that PostgreSQL is ready to accept connections. “The container process has started” is weaker than “the database is ready.” Kubernetes later expresses this distinction with probes.

External configuration also makes images reusable. Kubernetes can provide values for the same `DB_URL`, `DB_USERNAME`, and other names without changing [application.properties](/Users/saurabh/Documents/Learning/instagram-backend/src/main/resources/application.properties) or rebuilding the image.

<details>
<summary>Optional deep dive</summary>

Compose provides name lookup on its network, so the address `postgres` is resolved to the current PostgreSQL container. Code should depend on that stable service name rather than a container's changing internal IP address.

Publishing PostgreSQL on `55432` is useful for local tools, but backend-to-database traffic does not need to leave the Compose network and return through the laptop. Direct service-to-service communication uses `postgres:5432`.

</details>

## Check your understanding

Answer first, then expand.

<details>
<summary>1. Why is `localhost:55432` correct for a database tool on the laptop but wrong for the backend container?</summary>

For the laptop tool, `localhost` is the laptop and port `55432` is mapped to PostgreSQL. For the backend process, `localhost` is the backend container itself, where PostgreSQL is not running. The backend should use the Compose service address `postgres:5432`.

</details>

<details>
<summary>2. In `55432:5432`, which port belongs to the laptop and which belongs to PostgreSQL's container?</summary>

The left side, `55432`, is the published laptop port. The right side, `5432`, is the port inside the PostgreSQL container. Reading the mapping in that order prevents a common connection mistake.

</details>

<details>
<summary>3. What value does Spring use when `DB_URL` is present?</summary>

It uses the value of `DB_URL` and ignores the fallback after the colon. Under Compose, that means `jdbc:postgresql://postgres:5432/instagram` replaces the local fallback in `application.properties`.

</details>

<details>
<summary>4. Does `EXPOSE 8080` alone make `http://localhost:8080` work?</summary>

No. `EXPOSE` documents the image's intended port. A runtime mapping, such as Compose's `8080:8080`, connects the laptop port to the container port.

</details>

<details>
<summary>5. If the backend logs show a connection attempt to `localhost:55432` under Compose, what should you inspect first?</summary>

Inspect whether the backend actually received `DB_URL` from `docker-compose.yml`. The log suggests Spring used its laptop-oriented fallback, which usually means the environment value is missing, misspelled, or not applied to the current container.

</details>

## Stop/go check

**GO to 01F** if you can draw the two paths `laptop:8080 -> backend:8080` and `backend -> postgres:5432`, and explain why Spring uses `DB_URL` under Compose.

**STOP and repeat the exercise** if you still treat `localhost` as a universal name for your laptop or cannot identify the laptop side of `55432:5432`.
