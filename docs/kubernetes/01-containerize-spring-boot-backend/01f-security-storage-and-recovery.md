# 01F — Security, storage, and recovery

**Time:** 10–15 minutes

**Goal:** Explain three safety choices in this project: a less-powerful runtime user, a persistent PostgreSQL volume, and a controlled way to recover after a container replacement.

## Where this micro-lesson fits

The image now builds correctly, and the backend can reach PostgreSQL. This final part asks what happens when something goes wrong: an application is compromised, a container is replaced, or a database process is restarted.

The relevant project files are the [Dockerfile](/Users/saurabh/Documents/Learning/instagram-backend/Dockerfile), [docker-compose.yml](/Users/saurabh/Documents/Learning/instagram-backend/docker-compose.yml), [.dockerignore](/Users/saurabh/Documents/Learning/instagram-backend/.dockerignore), and [application.properties](/Users/saurabh/Documents/Learning/instagram-backend/src/main/resources/application.properties).

## Assumptions

This lesson assumes that you already know:

- the final image runs `/app/app.jar`;
- a container has its own writable filesystem view;
- Compose starts separate backend and PostgreSQL containers.

You do **not** need prior security or storage administration knowledge. The goal is to build safe instincts, not to design a production database today.

## The problems we are solving

There are three different risks:

1. A Java process running with maximum operating-system power can cause more damage if exploited.
2. Database files stored only in a replaceable container can disappear with that container.
3. A container replacement can be mistaken for data loss unless we deliberately verify what survived.

Containerization does not solve these automatically. The image and Compose configuration must express safer choices.

## Five new terms

1. **Root user** — the operating-system identity with very broad power inside a container.
2. **Least privilege** — giving a process only the permissions it needs for its job.
3. **Named volume** — storage managed by Docker under a stable name and mounted into a container.
4. **Persistence** — data surviving beyond the lifetime of one container instance.
5. **Recovery check** — a deliberate test that proves the system can return with expected data after interruption.

## Must understand

### 1. Run the application with less power

The runtime half of the [Dockerfile](/Users/saurabh/Documents/Learning/instagram-backend/Dockerfile) contains:

```dockerfile
RUN groupadd --system instagram && useradd --system --gid instagram instagram
USER instagram
```

The first line creates a dedicated operating-system group and user. `USER instagram` tells Docker to run later commands, including the Java entrypoint, as that identity rather than as root.

Why it matters: if a bug lets an attacker influence the Java process, a less-powerful user limits some of what that process can change. This is an example of least privilege.

It is a safety layer, not a security guarantee. It does not repair vulnerable code, hide leaked credentials, or block every network attack.

### 2. Keep runtime secrets out of image layers

The image contains the stable program. Values that can change between environments are provided when a container starts. In [docker-compose.yml](/Users/saurabh/Documents/Learning/instagram-backend/docker-compose.yml), the backend receives database credentials and `APP_JWT_SECRET` through its environment.

That is better than writing secrets into the [Dockerfile](/Users/saurabh/Documents/Learning/instagram-backend/Dockerfile), because Dockerfile content and image layers can be inspected and shared.

However, the values currently written in Compose are development examples, not strong production secret management. Production Kubernetes lessons will separate ordinary configuration from sensitive values and discuss controlled delivery and rotation.

The [.dockerignore](/Users/saurabh/Documents/Learning/instagram-backend/.dockerignore) also prevents several irrelevant local paths—such as `.git`, build output, IDE metadata, logs, and demo screenshots—from entering the build context. This reduces accidental exposure, but it is not permission to keep real secrets in arbitrary project files. A secret that is not ignored can still be sent to the builder.

### 3. Put PostgreSQL data outside the replaceable container

The PostgreSQL service in [docker-compose.yml](/Users/saurabh/Documents/Learning/instagram-backend/docker-compose.yml) mounts:

```yaml
volumes:
  - instagram-postgres-data:/var/lib/postgresql/data
```

At the bottom of the file, the named volume is declared:

```yaml
volumes:
  instagram-postgres-data:
```

PostgreSQL writes its database files to `/var/lib/postgresql/data`. Docker connects that directory to storage named `instagram-postgres-data`, whose lifetime is separate from a single PostgreSQL container.

```text
PostgreSQL container A ----\
                           > instagram-postgres-data
PostgreSQL container B ----/        named volume
```

Container A can be removed and replaced by container B. If both mount the same surviving volume correctly, B sees the existing database files.

### 4. Know which shutdown command preserves data

```bash
docker compose down
```

removes the Compose containers and network but preserves named volumes by default.

```bash
docker compose down --volumes
```

also removes the named volume. For a database, that can intentionally erase local data. Do not add `--volumes` casually while practicing recovery.

If you remember only one sentence, remember this:

> Containers are replaceable; important state must live somewhere whose lifetime is intentionally managed.

## Small exercise — prove that container replacement is not data loss

This exercise creates a tiny marker table in the **local learning database**, replaces the PostgreSQL container while preserving its named volume, and checks that the marker remains. It does not change repository files.

### Prediction

Predict whether the row with `id = 1` will still exist after Compose force-recreates the PostgreSQL container. Explain your reason before running anything.

The expected prediction is “yes,” because the old container is replaceable while the separately managed named volume remains attached to the service.

### Exact commands

Run from the repository root:

```bash
docker compose up -d postgres
docker compose exec -T postgres psql -U instagram -d instagram -c \
  "CREATE TABLE IF NOT EXISTS lesson_storage_marker (id integer PRIMARY KEY); INSERT INTO lesson_storage_marker (id) VALUES (1) ON CONFLICT DO NOTHING;"
docker compose up -d --force-recreate postgres
docker compose exec -T postgres psql -U instagram -d instagram -c \
  "SELECT id FROM lesson_storage_marker;"
```

### Expected result

The final query should return one row containing `1`. That proves the database state survived replacement of the PostgreSQL container because the replacement mounted the same named volume.

If the query runs before PostgreSQL is ready after recreation, wait a few seconds and repeat only the final query. A temporary readiness delay is not the same as data loss.

When you no longer need the marker, remove only the practice table:

```bash
docker compose exec -T postgres psql -U instagram -d instagram -c \
  "DROP TABLE IF EXISTS lesson_storage_marker;"
```

Do not use `docker compose down --volumes` as a cleanup shortcut unless you intentionally want to erase the entire local PostgreSQL volume.

## Useful later

The backend container itself has no volume in [docker-compose.yml](/Users/saurabh/Documents/Learning/instagram-backend/docker-compose.yml). Files it writes only to its own writable container layer are not durable when that container is replaced.

This matters because the project's uploaded media is currently local-file based. Multiple backend replicas would not automatically share those files. A later production design should place shared media in an external object store rather than treating an application container as permanent storage.

The Compose setting `restart: unless-stopped` applies to PostgreSQL. It asks Docker to restart the process after certain exits, but restarting is not the same as proving correctness. Logs, readiness checks, and a recovery check are still needed.

<details>
<summary>Optional deep dive</summary>

A named volume protects data from ordinary container replacement, but it is not automatically a backup. Accidental deletion, corrupted data, or a failed disk can affect the volume itself. Real recovery planning needs backups stored separately and a tested restore procedure.

Likewise, a non-root user reduces privilege inside the container but the surrounding deployment still matters: mounted files, Linux capabilities, network access, and Kubernetes permissions can expand what a compromised process can reach.

</details>

## Check your understanding

Answer in your own words, then expand the explanation.

<details>
<summary>1. Why does the Dockerfile switch to `USER instagram` before starting Java?</summary>

It applies least privilege. The backend does not need root power to listen on port `8080` and run its JAR, so giving it a dedicated, less-powerful identity limits some damage if the process is compromised.

</details>

<details>
<summary>2. Does running as a non-root user make the backend completely secure?</summary>

No. It reduces one category of risk, but vulnerable code, exposed services, weak credentials, excessive external permissions, and leaked secrets can still cause harm. Security uses multiple reinforcing controls.

</details>

<details>
<summary>3. Why does PostgreSQL mount `instagram-postgres-data` at `/var/lib/postgresql/data`?</summary>

That is where PostgreSQL stores its database files. Mounting a named volume there gives those files a lifetime separate from one replaceable PostgreSQL container, allowing a replacement container to use the existing data.

</details>

<details>
<summary>4. What important difference exists between `docker compose down` and `docker compose down --volumes`?</summary>

Plain `down` removes the Compose containers and network while preserving named volumes by default. Adding `--volumes` also deletes the named volume, which can erase the local database. The second command is therefore intentionally destructive to stored data.

</details>

<details>
<summary>5. A marker survives a container replacement. Does that prove you have a complete backup strategy?</summary>

No. It proves only that this named volume survived that interruption. A backup strategy must also protect against volume loss or corruption and must include a tested way to restore data from a separate copy.

</details>

## Stop/go check

**GO onward** if you can explain (1) why Java runs as `instagram`, (2) why database files use a named volume, and (3) why a successful replacement check is useful but is not a backup.

**STOP and repeat the exercise** if you expect every file in every container to persist automatically, or if you cannot explain why `docker compose down --volumes` deserves extra caution.
