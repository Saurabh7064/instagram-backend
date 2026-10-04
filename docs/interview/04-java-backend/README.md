# Java and Backend Questions

Prioritize explanation and production examples over trivia.

## Java/JVM priority list

- Object contracts: equality, hashing, immutability, records
- Collections and their performance trade-offs
- Generics and type erasure
- Exceptions and resource management
- Streams, lambdas, and when not to use them
- Threads, executors, futures, locks, and concurrent collections
- Java Memory Model, visibility, atomicity, and `volatile`
- JVM memory, garbage collection, profiling, and common failure symptoms
- Modern Java features used in the target company’s version
- Testing with JUnit, mocking boundaries, and integration tests

## Spring/backend priority list

- Dependency injection and bean lifecycle
- Spring Boot configuration and profiles
- Request lifecycle, validation, and exception handling
- Transactions, isolation, propagation, and rollback surprises
- JPA/Hibernate fetching, N+1 queries, locking, and batching
- Authentication, authorization, sessions, JWT, and common API risks
- REST semantics, pagination, versioning, idempotency, and retries
- Test strategy: unit, integration, contract, and end-to-end
- Diagnosing latency, memory, database, and thread-pool problems

## Answer template

1. Define the idea simply.
2. Explain the problem it solves.
3. Give a concrete Java/Spring example.
4. State the important trade-off or failure mode.
5. Connect it to production experience when truthful.
