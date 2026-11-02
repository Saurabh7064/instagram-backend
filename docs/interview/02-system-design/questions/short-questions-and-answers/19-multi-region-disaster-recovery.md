# Multi-Region Disaster Recovery

## Question

The primary region fails minutes after accepting new posts. Leadership asks how quickly service can return and whether any accepted data will be lost.

**“How would you survive a region failure, and how would you choose RTO and RPO?”**


## Ask two separate recovery questions

The primary region stops working after accepting new posts. Leadership needs two answers: “When can people use the service again?” and “Which accepted data might be missing?” Restoring a server answers neither question by itself.

**Recovery time objective (RTO)** is the target maximum time to restore the required service. **Recovery point objective (RPO)** describes the tolerated gap in recoverable data, expressed as time. These are business objectives; measured recovery can miss them, so the design needs evidence that it can meet them.

A second office and copied records provide a useful analogy. The second office needs staff and supplies, not just an address. Software adds a stricter problem: two writable regions must not independently claim authority over the same records without a conflict protocol.

## Follow the three-second gap

At 10:00:00, region A commits a post. Copying to B is asynchronous, and this record has not arrived when A fails at 10:00:03. Promoting B immediately does not recreate that post.

Inspect the last confirmed replication position and available logs. If the known gap fits the agreed loss tolerance and the recovery policy permits promotion, responders can make that explicit decision. If zero loss is required, pause writes while finding a recoverable authoritative history; if the only copy is permanently lost, the objective cannot be met by promotion alone.

The three seconds are this example's possible gap, not a promise that every failure loses only three seconds. Actual loss depends on copying progress and recoverable evidence.

## Establish one writer before opening traffic

**Fencing** prevents the old primary from continuing authoritative writes. Merely deciding it looks unhealthy is insufficient: an isolated A might still serve clients and later reconnect. Verify fencing and the replacement's state before enabling writes on B.

Shift only the traffic B can safely handle, with sufficient application, database and media capacity. Include queues, secrets, routing and operator access in the dependency inventory. A warm application with missing photos is not a recovered Instagram service.

A single write region with a prepared standby is often simpler than independent writes in both regions. The cost is standby capacity and a recovery interval. Replication also needs independent backups, because it can faithfully copy accidental deletion or corruption. Rehearse both restoration and full regional evacuation; a configuration diagram cannot measure recovery time.

## Where this appears in our backend

The application currently points to one database through [application.properties](../../../../../src/main/resources/application.properties), and [MediaStorageService](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) stores files on one instance’s local filesystem. [Post](../../../../../src/main/java/com/instagram/backend/domain/Post.java) persists post metadata in that single database.

**Proposed, not implemented:** multi-zone production deployment, cross-region database and object replication, global traffic routing, fencing, backup/restore automation, failover capacity, and reconciliation tooling. Local files must move to durable shared object storage before regional recovery is credible.

## Choosing the approach and paying its costs

- Choose active-passive writes when correctness and operational simplicity dominate. Do not promise instant failover without warm capacity and automation; the cost is recovery delay and standby spend.
- Choose active-active reads with one write region for global read latency. Writes still cross regions, and replicas may be stale.
- Choose active-active writes only when the product has explicit conflict rules and teams can operate them. It improves local write availability but introduces split-brain, reconciliation, and invariant risks.


## Paper drill: the standby cannot take full traffic

Predict the result of redirecting every request to a smaller, stale standby. It can overload immediately and still lack recent data.

Detect replication gaps and destination saturation before opening writes. Contain the incident by enforcing one writer, shifting only safe reads and shedding optional work. Recover data and sufficient capacity, then resume writes under the reviewed loss policy and verify user journeys end to end.

Afterwards, record actual restoration time, the confirmed data gap and unresolved uncertainty. Prevent recurrence with full-load evacuation exercises and a runbook that includes fencing, capacity, every state store and independent backup restoration.

## Two-to-three-minute interview answer

I would first ask the business to set RTO and RPO per critical journey. Those targets decide whether asynchronous replication and minutes of failover are acceptable or whether we need much more expensive synchronous or active-active designs. I would inventory all regional dependencies, not just application pods: database, object storage, queues, secrets, routing, identity, and operator access.

For many systems I would start with active-passive writes, replicated durable data, health-based global routing, warm standby capacity, and explicit fencing so the failed primary cannot resume writes independently. During failover I would verify lag, fence the old writer, shift safe reads, promote one authority, resume writes deliberately, and reconcile the known gap. Backups are also required because replication can copy corruption. The design is only credible after regular restore and full regional evacuation tests at production-like load.


## Before moving on

Artifact: `READY`; comprehension: `TODO`. Explain this scenario in your own words, predict the paper drill, and attempt the questions before opening their answers. Record learning only from your own explanation and predictions.

## Questions and explained answers

<details>
<summary>Why must RTO and RPO be chosen before the topology?</summary>

They express the required recovery speed and acceptable data loss. Without them, the team cannot justify whether a simple standby is sufficient or whether the cost and complexity of stronger replication are necessary.
</details>

<details>
<summary>Why is fencing required during failover?</summary>

Without fencing, the old primary may recover or remain isolated and accept writes while the new primary also writes. That creates two authorities and conflicting histories.
</details>

<details>
<summary>Why are backups still necessary when data is replicated?</summary>

Replication improves availability but can copy accidental deletion, corruption, or malicious changes. A tested backup provides an independent recovery point.
</details>
