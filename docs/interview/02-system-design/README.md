# System Design Interviews

Use the project’s [system-design mastery roadmap](../../system-design/mastery-roadmap.md) for concept depth. This folder is for interview practice and design transcripts, not duplicate theory notes.

## Start here

1. Follow the [System Design Interview Learning Plan](./learning-plan.md).
2. Study only the first selected [Concept Micro-Lesson](./concepts/README.md).
3. Practice one answer at a time from the [Microservices Interview Questions](./questions/microservices/README.md).
4. Use the [November–December 2024 Question Bank](./question-bank-nov-dec-2024.md) for historical prompts and answer frameworks.
5. Record explanations, predictions, failure diagnoses, and spaced reviews in [Learner Progress](./learner-progress.md).

The lesson format is: **why → analogy → plain-language model → concrete flow → project mapping → decision/trade-off → failure drill → questions → teach-back**. A lesson file being complete does not mean the learner understands it.

## Interview structure

1. Clarify functional and non-functional requirements.
2. Estimate scale only where it changes a decision.
3. Define APIs/events and the data model.
4. Draw the high-level request and data flow.
5. Deep-dive into two important components.
6. Address bottlenecks, failures, security, and observability.
7. Summarize trade-offs and evolution paths.

## Design practice order

- Anchor design 1: scalable e-commerce—catalog, search, cache, inventory, checkout, payment, and Saga.
- Anchor design 2: Instagram/news feed—fan-out, media/CDN, pagination, ranking, hot keys, and eventual consistency.
- Anchor design 3: healthcare data sharing—privacy, audit, anonymized analytics, regional operation, and high availability.
- Focused drills: URL shortener, rate limiter, notification service, chat, distributed scheduler, and autocomplete when a weakness or target-company pattern requires them.

## Design note template

- Prompt and time limit:
- Clarifying questions:
- Functional requirements:
- Quality attributes and scale:
- APIs/events:
- Data model:
- Architecture diagram:
- Request/data flow:
- Deep dives:
- Failure modes and recovery:
- Security and abuse cases:
- Observability:
- Trade-offs and alternatives:
- Mock feedback and next drill:

For new concept lessons, use the fuller [Concept Template](./_concept-template.md).
