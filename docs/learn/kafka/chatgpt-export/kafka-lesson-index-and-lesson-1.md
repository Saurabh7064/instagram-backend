# Kafka Lesson Index

## You said:

kafka lesson index

## ChatGPT said:

# Apache Kafka — Complete Lesson Index

A structured learning roadmap from **beginner to advanced**, designed for a Senior Software Engineer preparing for system design interviews and real-world distributed systems.

Learning progress

0 / 45 lessons

0 of 40 lessons completed

0%

### Module 1 — Kafka Fundamentals

Beginner

- [ ] 01. What is Apache Kafka? Why do we need it?
- [ ] 02. Kafka vs traditional message queues (RabbitMQ, SQS)
- [ ] 03. Kafka architecture: brokers, clusters, producers, consumers
- [ ] 04. Topics, partitions, and offsets
- [ ] 05. How Kafka messages flow end to end
- [ ] 06. Kafka installation and CLI basics

### Module 2 — Producers

Beginner–Intermediate

- [ ] 07. Producer architecture and message serialization
- [ ] 08. Partitioning strategies and message keys
- [ ] 09. Acknowledgments: acks=0, 1, all
- [ ] 10. Retries, idempotent producers, and delivery guarantees
- [ ] 11. Batching, linger.ms, compression, and throughput

### Module 3 — Consumers

Intermediate

- [ ] 12. Consumer architecture and poll loop
- [ ] 13. Consumer groups and partition assignment
- [ ] 14. Consumer offsets and commit strategies
- [ ] 15. Auto commit vs manual commit
- [ ] 16. Consumer group rebalancing
- [ ] 17. Consumer lag, backpressure, and slow consumers

### Module 4 — Kafka Internals

Advanced

- [ ] 18. Replication factor, leaders, and followers
- [ ] 19. ISR (In-Sync Replicas) and high availability
- [ ] 20. Leader election and broker failures
- [ ] 21. KRaft metadata management vs ZooKeeper
- [ ] 22. Kafka storage: segments, retention, and compaction
- [ ] 23. Kafka ordering guarantees and partition scaling

### Module 5 — Reliability & Transactions

Advanced

- [ ] 24. At-most-once, at-least-once, exactly-once delivery
- [ ] 25. Kafka transactions and exactly-once semantics
- [ ] 26. Handling duplicate messages and idempotent consumers
- [ ] 27. Poison messages, retries, and dead-letter topics
- [ ] 28. Disaster recovery and multi-region Kafka

### Module 6 — Spring Boot + Kafka

Hands-on

- [ ] 29. Spring Kafka configuration and dependencies
- [ ] 30. Building a REST API that publishes Kafka events
- [ ] 31. Building consumers with @KafkaListener
- [ ] 32. Error handling, retries, and DLT in Spring Kafka
- [ ] 33. Integration testing with Testcontainers
- [ ] 34. Schema evolution with Avro and Schema Registry

### Module 7 — Advanced Kafka & System Design

Senior/Staff

- [ ] 35. Kafka Streams, state stores, and windowing
- [ ] 36. Kafka Connect and CDC with Debezium
- [ ] 37. Event-driven architecture vs event sourcing
- [ ] 38. Outbox pattern and database consistency
- [ ] 39. Design a scalable order processing system
- [ ] 40. Kafka monitoring, performance tuning, and interview scenarios

## How we'll learn

Each lesson will follow this format:

1. **Intuition first** — why the concept exists and what problem it solves.
2. **Visual explanation** — diagrams and message-flow examples.
3. **Java / Spring Boot** — actual implementation.
4. **Production scenarios** — broker failures, scaling, duplicate messages, and reliability.
5. **Interview preparation** — common Senior Engineer questions and tricky edge cases.

Start Lesson 1

Test my existing Kafka knowledge

---

## You said:

Teach me Apache Kafka lesson 1: What is Apache Kafka? Why do we need it?. Explain intuitively, use Java/Spring Boot examples, diagrams where useful, real-world scenarios, and interview questions.

## ChatGPT said:

# Lesson 1: What Is Apache Kafka? Why Do We Need It?

Beginner

Module 1 · Lesson 1 · ~25 min

Today's goal: **Understand Kafka so clearly that you can explain why it exists, when to use it, and how it solves real problems in distributed systems.**

We won't start by memorizing technical definitions. We'll start with a problem that software engineers face.

## 1. Imagine you're building an e-commerce application

You have an `OrderService` built with Java and Spring Boot.

Whenever a customer places an order, several things must happen:

1. Save the order in the database.
2. Process the payment.
3. Update the inventory.
4. Send a confirmation email.
5. Notify the shipping service.
6. Update analytics.

### Approach A: Without Kafka

Your `OrderService` calls every other service directly through REST APIs.

OrderService is directly dependent on five services.

This works for a small system. But imagine handling 10,000 orders per minute.

Problems begin to appear.

Problem 1: Slow response times

Imagine these hypothetical response times:

| Service | Time |
| --- | --- |
| Payment | 300 ms |
| Inventory | 150 ms |
| Email | 800 ms |
| Shipping | 250 ms |
| Analytics | 200 ms |
| Total | 1,700 ms |

If all requests happen sequentially, the customer waits about 1.7 seconds just for these calls, excluding other processing.

Parallel requests could reduce the wait, but they don't eliminate the other problems.

Problem 2: Service failures

What happens if the email service is temporarily unavailable?

If your implementation requires every API call to succeed, the entire order operation could fail even though the payment was successful.

Problem 3: Tight coupling

Tomorrow, the business wants to add a fraud detection service.

Now you must modify `OrderService` to integrate another downstream dependency.

As the system grows, this becomes difficult to maintain.

## 2. Enter Apache Kafka

Instead of calling every service directly, imagine `OrderService` publishes a message:

OrderCreated event

```json
{
  "orderId": "ORD-101",
  "customerId": "C-42",
  "amount": 125.00,
  "status": "CREATED"
}
```

Kafka stores the event, and other services can independently read it.

### Approach B: With Kafka

OrderService publishes an event. Independent consumers process it.

Notice the difference:

`OrderService` no longer needs to know whether an email service or analytics service exists. It only needs to know where to publish the event.

This is the first major reason Kafka exists: decoupling services using asynchronous communication.

One important detail: publishing an `OrderCreated` event doesn't automatically guarantee that payment, inventory, or shipping succeeds. Those workflows still need appropriate business logic, retries, and consistency handling.

## 3. What exactly is Apache Kafka?

Here's the technical definition:

Apache Kafka is a distributed event-streaming platform used to publish, store, and process streams of events reliably and at scale.

Let's decode that.

| Term | Simple meaning |
| --- | --- |
| Event | Something that happened, such as `OrderCreated` |
| Streaming | Events keep arriving continuously |
| Distributed | Kafka can run across multiple servers |
| Publish | An application sends an event |
| Subscribe | An application reads events |
| Durable storage | Kafka stores events on disk according to its configured retention and replication policies |

Think of Kafka as a **shared, persistent event log**.

Applications append events to the log. Other applications read events from it independently.

This leads to a powerful feature: an event is not automatically deleted just because a consumer reads it.

## 4. Kafka vs a traditional message queue

Think about a message queue:

Traditional work queue (simplified)

In many work-queue configurations, a message is removed after successful acknowledgment.

Kafka's retained event log

Multiple independent consumer groups can read the same retained events, including replaying older events.

The fundamental difference is:

Kafka consumers track their reading positions instead of removing events from the log.

Kafka can therefore support both message distribution and event replay.

A traditional broker such as RabbitMQ can also provide pub/sub and persistence, so the distinction is about Kafka's log-based design, not an absolute limitation of other systems.

## 5. The five basic building blocks

Before writing Java code, you need to understand five terms.

| Concept | Meaning |
| --- | --- |
| Producer | Sends events to Kafka |
| Broker | Kafka server that stores and serves events |
| Topic | Named category of events, such as `orders` |
| Consumer | Reads events from Kafka |
| Offset | Position of an event inside a partition |

A Kafka **cluster** consists of one or more brokers, and topics are divided into partitions. Each partition has its own offsets and ordered event sequence.

### Example: Understanding offsets

Imagine one partition in the `orders` topic.

| Offset | Event |
| --- | --- |
| 0 | Order A |
| 1 | Order B |
| 2 | Order C |
| 3 | Order D |

One Kafka partition, with four consecutive events.

Suppose your email consumer processed through offset 1 and then crashed. If it had committed offset 2 as its next reading position, it could resume reading from offset 2, `Order C`, after restarting.

It does not necessarily need to start from the beginning.

And an analytics consumer could independently start at offset 0 and process all four orders.

This is what makes Kafka useful for replayable event processing.

## 6. Let's write Spring Boot code

We'll implement a small application:

- REST controller receives an order request.
- Kafka producer publishes an event.
- Kafka consumer receives and logs the event.

Assume a Kafka broker is already running at `localhost:9092` and a topic called `orders` exists.

### Step 1: Add the Spring Kafka dependency

```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

### Step 2: Configure Kafka

application.yml

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
    consumer:
      group-id: notification-service
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      auto-offset-reset: earliest
```

Here, `notification-service` identifies the consumer group. `earliest` applies when the group has no valid committed offset.

### Step 3: Create the producer

```java
@Service
public class OrderProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public OrderProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<SendResult<String, String>> publishOrder(String orderId) {

        return kafkaTemplate.send(
            "orders",
            orderId,
            "Order created: " + orderId);
    }
}
```

What happens when we call:

```java
publishOrder("ORD-101");
```

Kafka receives a record with:

- Topic: `orders`
- Key: `ORD-101`
- Value: `Order created: ORD-101`

The returned future completes when the producer receives the configured acknowledgment or an error.

### Step 4: Create a REST controller

```java
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderProducer producer;

    public OrderController(OrderProducer producer) {
        this.producer = producer;
    }

    @PostMapping("/{orderId}")
    public CompletableFuture<ResponseEntity<String>> createOrder(
            @PathVariable String orderId) {

        return producer.publishOrder(orderId)
            .thenApply(result -> ResponseEntity.accepted()
                    .body("Order event published: " + orderId)
            );
    }
}
```

Call:

```http
POST http://localhost:8080/orders/ORD-101
```

This demo publishes an event but does not yet persist an order in a database. We'll cover reliable database-to-Kafka publishing in a later lesson.

### Step 5: Create a consumer

```java
@Component
public class OrderConsumer {

    @KafkaListener(topics = "orders", groupId = "notification-service")
    public void consume(String message) {

        System.out.println("Received: " + message);

        // Send an email notification here
    }
}
```

Expected console output:

```text
Received: Order created: ORD-101
```

That's it! You have implemented a basic Kafka producer and consumer. The controller publishes an event to Kafka, and the consumer receives it independently.

## 7. What happens when a consumer goes down?

This is one of Kafka's most important practical benefits.

Interactive Kafka simulator

Consumer online

Publish events, stop the consumer, and see what happens to events stored in Kafka.

Events in Kafka

0

Waiting to be read

0

Kafka event log

0 published

No events published yet

Green: read by consumer · Amber: not yet read. Both remain in the log.

Publish event

Stop consumer

Reset simulation

The consumer has caught up. The published events are still retained in Kafka.

This simplified simulation assumes durable retention, a healthy broker, successful processing, and correct offset management. Real consumers may take time to catch up or reprocess messages after failures.

The important concept is **Kafka separates message production from message consumption**.

The producer can continue publishing while a consumer is temporarily unavailable, as long as Kafka remains available and has sufficient capacity.

Kafka is not infinite storage, however. Events are subject to retention policies and available disk space.

## 8. Real-world example: Banking transactions

Imagine you're building a real-time payment system.

A customer transfers $500.

After the transfer is committed, several systems may need to react to a `PaymentCompleted` event.

| Consumer | Responsibility |
| --- | --- |
| Notification Service | Send payment confirmation |
| Fraud Analytics | Analyze suspicious transaction patterns |
| Reporting Service | Update transaction reporting |
| Audit Service | Record the event for audit processing |

Instead of making the payment service wait for all these operations, it can publish an event and let the independent services process it.

Important: **Kafka should not be treated as a replacement for the database transaction that actually moves money.** Correct balance changes, idempotency, and transaction consistency must be handled explicitly.

This illustrates an important architectural decision:

- Use **REST/gRPC** when an operation requires an immediate response.
- Use **Kafka** when other services should react asynchronously to events, especially when independent consumption, replay, or high throughput is valuable.

Many production architectures use both.

## 9. When should you NOT use Kafka?

Kafka isn't automatically the best solution for every application.

| Situation | Better starting choice |
| --- | --- |
| Simple synchronous request/response | REST or gRPC |
| Small application with few dependencies | Direct service calls |
| Basic background job queue | SQS or RabbitMQ may be simpler |
| Large-scale event stream with independent consumers | Kafka is a strong candidate |
| Replayable business events and streaming analytics | Kafka is a strong candidate |

Kafka introduces operational responsibilities: broker management, monitoring, partitions, retention, consumer lag, security, and failure handling.

Use it when these capabilities justify the complexity.

## 10. Senior Software Engineer interview questions

<details>
<summary>1. What is Apache Kafka?</summary>

Kafka is a distributed event-streaming platform that lets applications publish, durably store, read, and process streams of events. It is commonly used for event-driven integration and data pipelines.

</details>

<details>
<summary>2. Why would you use Kafka instead of REST?</summary>

Kafka is useful when work can happen asynchronously and multiple independent services need the same events. REST is better suited to immediate request-response interactions. They complement each other.

</details>

<details>
<summary>3. What happens if a Kafka consumer crashes?</summary>

Another consumer in the group may take over its assigned partitions, or the original consumer may resume after restarting. Processing resumes based on committed offsets, so some messages may be processed again.

</details>

<details>
<summary>4. Can multiple services consume the same Kafka event?</summary>

Yes. Separate consumer groups can independently read the same topic. Inside one consumer group, a partition is normally assigned to one consumer at a time.

</details>

<details>
<summary>5. Does Kafka guarantee a message will never be lost?</summary>

Not unconditionally. Reliability depends on producer acknowledgments, replication, broker health, retention, and consumer handling. Kafka offers strong durability mechanisms when configured correctly.

</details>

## 11. Test your understanding

Try answering these without looking at the lesson.

1. `OrderService` publishes an event, but `EmailService` is down. What happens?

   - Kafka automatically sends the email
   - Kafka can retain the event for later consumption
   - OrderService must immediately fail

2. Two different consumer groups subscribe to the `orders` topic. Who receives an event?

   - Only the first group
   - Both groups can independently read it
   - Kafka randomly selects a group

3. After a consumer reads an event, does Kafka immediately delete it?

   - Yes, always
   - No, retention determines how long it remains
   - Only if the consumer crashes

## Lesson 1 summary

Remember this mental model:

Producer → Kafka Topic → Consumer

Kafka solves three fundamental problems:

1. **Decoupling:** Producers and consumers don't need direct knowledge of one another.
2. **Asynchronous processing:** Producers need not wait for consumers to complete their work.
3. **Durable, replayable events:** Consumers can read retained events independently, including after temporary outages.

The official [Apache Kafka Quickstart](https://kafka.apache.org/quickstart/?utm_source=chatgpt.com) is also useful when you're ready to run a local broker.

Up next: Lesson 2

Beginner

Kafka vs RabbitMQ vs Amazon SQS

We'll compare how they handle messages, delivery, scaling, durability, and real-world use cases.

Continue to Lesson 2

---

## You said:

Teach me Apache Kafka lesson 2: Kafka vs traditional message queues (RabbitMQ, SQS). Explain intuitively, use Java/Spring Boot examples, diagrams where useful, real-world scenarios, and interview questions.

Stopped thinking

---

## You said:

Teach me Apache Kafka lesson 4: Topics, partitions, and offsets. Explain intuitively, use Java/Spring Boot examples, diagrams where useful, real-world scenarios, and interview questions.

Stopped thinking

---

## You said:

Teach me Apache Kafka lesson 3: Kafka architecture: brokers, clusters, producers, consumers. Explain intuitively, use Java/Spring Boot examples, diagrams where useful, real-world scenarios, and interview questions.

Stopped thinking
