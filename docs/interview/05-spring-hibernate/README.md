# Spring and Hibernate Interview Questions

Focus on how the framework behaves at runtime, where abstractions leak, and how you diagnose production problems. Keep core language and JVM preparation in [Java](../04-java/README.md).

## Spring and Spring Boot priority list

- Dependency injection, bean scopes, lifecycle, and circular dependencies
- Auto-configuration, component scanning, configuration properties, and profiles
- Request lifecycle, filters, interceptors, validation, and exception handling
- Spring Security filter chain, authentication, authorization, sessions, and JWT
- AOP and proxy behavior, including self-invocation surprises
- Transactions, isolation, propagation, rollback rules, and transaction boundaries
- REST semantics, pagination, versioning, idempotency, and retries
- Testing slices, unit tests, integration tests, Testcontainers, and MockMvc
- Actuator, metrics, tracing, health checks, and production diagnostics

## JPA and Hibernate priority list

- Entity lifecycle and persistence context
- Owning side, mappings, cascades, and orphan removal
- Lazy versus eager loading and `LazyInitializationException`
- N+1 queries, fetch joins, entity graphs, projections, and batching
- Dirty checking, flush timing, and transaction interaction
- Optimistic and pessimistic locking
- First-level and second-level caching
- Query generation, pagination, indexes, and reading SQL logs
- Database migrations and schema ownership

## Answer template

1. Define the Spring or Hibernate behavior.
2. Explain what the abstraction does for the application.
3. Describe a concrete request, transaction, or query flow.
4. Name the common failure or performance trap.
5. Explain how you would test, observe, and fix it.
