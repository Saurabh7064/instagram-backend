# API and Event Contract Evolution — Interview Revision Card

> **Question:** How would you evolve APIs and event schemas without breaking older consumers during independent deployments?
>
> [Detailed source](../../questions/microservices/09-api-event-contract-evolution.md)

## Key requirements

- Old and new producers/consumers coexist safely.
- Queued historical events remain readable.
- Removal occurs only after measured adoption.

## Scale assumptions

- Mobile clients may remain old for months, and events may be replayed long after publication.

## Components

- **Backward compatibility** lets a new producer serve old consumers; **tolerant reading** ignores unknown optional fields; a **contract test** verifies assumptions.
- Schema registry, consumer inventory, version telemetry, and deprecation policy.

## Request flow

1. Add the new optional field or representation without removing the old.
2. Deploy consumers that understand both shapes.
3. Deploy producers that write both or prefer the new shape.
4. Measure old-field/version use and replay compatibility.
5. Remove legacy shape only after the support window and rollback need end.

## Data model

- `Event(eventId, type, schemaVersion, occurredAt, payload)`; preserve meaning of existing fields.

## Three trade-offs

1. Additive evolution is simple but grows payload and temporary complexity.
2. Versioned endpoints isolate breaking change but duplicate support paths.
3. Translating adapters protect consumers but add latency and ownership.

## Three failures and mitigations

1. **Unknown enum breaks consumer:** handle unknown values and contract-test them.
2. **Old event replay fails:** keep historical schemas/readers in replay tests.
3. **Early field removal:** measure adoption and enforce deprecation gates.

## Two-minute spoken answer

I use expand-migrate-contract. First I inventory consumers and compatibility promises. I add a new optional field or representation while preserving the old meaning, then deploy consumers that read both shapes before producers begin emitting the new one. Contract tests verify real consumer expectations, including unknown enum values and old event fixtures.

During migration, producers may write both forms and telemetry shows remaining old-version use. Event envelopes carry stable identity, type, and schema version, but versioning does not excuse semantic changes. I remove the old form only after consumers migrated, queued history remains replayable, the support window ended, and rollback no longer needs it. This costs temporary duplication, but permits independent deployment without coordinated outages.

## Recall questions

1. What is the safe order for a field rename?
2. Why can an added enum value break a consumer?
3. What evidence permits removal?
