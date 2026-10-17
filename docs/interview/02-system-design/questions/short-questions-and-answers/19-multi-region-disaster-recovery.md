# Multi-Region Disaster Recovery

## Question

The primary region fails minutes after accepting new posts. Leadership asks how quickly service can return and whether any accepted data will be lost.

**“How would you survive a region failure, and how would you choose RTO and RPO?”**

## Why this problem exists

A region can lose compute, networking, or data services together, so adding more instances inside that region is not enough. Recovery speed, acceptable data loss, write correctness, and cost pull the design in different directions. These business requirements must be explicit before choosing active-passive or active-active architecture.

## Analogy

A company may keep a second office and copies of critical records so work can resume after a building closes. The analogy stops because two writable data centers can accept conflicting updates during a network partition, creating a split-brain problem that offices do not automatically face.

## Terms to establish

- **RTO** is the maximum acceptable time to restore a service after disruption.
- **RPO** is the maximum acceptable amount of recent data that may be lost, measured in time.
- **Active-passive** keeps one write region primary and prepares another for failover.
- **Fencing** prevents an old or isolated primary from continuing to accept authoritative writes.

## What I would do

1. Set RTO and RPO per journey; feed reads may degrade differently from post creation or account security.
2. Identify every dependency and state store, including media, secrets, queues, DNS, and operational access.
3. Choose a topology and replication mode that can meet those targets, with enough reserved capacity in the destination.
4. Define health-based routing, write ownership, fencing, promotion, rollback, and reconciliation procedures.
5. Test backups, restores, and full regional evacuation regularly; replication without a rehearsed recovery is not a recovery plan.

## Concrete walkthrough

1. Region A commits post metadata at 10:00:00 and asynchronously copies it to Region B.
2. Region A fails at 10:00:03 before the record reaches B.
3. Automation fences Region A, responders verify B’s lag, and global routing shifts reads first.
4. If the agreed RPO permits three seconds of loss, B is promoted and writes resume; otherwise writes remain paused while the missing record is recovered.
5. The observable result is a measured outage and a known reconciliation set, not silent divergent writes.

## Instagram-backend mapping

The application currently points to one database through [application.properties](../../../../../src/main/resources/application.properties), and [MediaStorageService](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) stores files on one instance’s local filesystem. [Post](../../../../../src/main/java/com/instagram/backend/domain/Post.java) persists post metadata in that single database.

**Proposed, not implemented:** multi-zone production deployment, cross-region database and object replication, global traffic routing, fencing, backup/restore automation, failover capacity, and reconciliation tooling. Local files must move to durable shared object storage before regional recovery is credible.

## Choices and trade-offs

- Choose active-passive writes when correctness and operational simplicity dominate. Do not promise instant failover without warm capacity and automation; the cost is recovery delay and standby spend.
- Choose active-active reads with one write region for global read latency. Writes still cross regions, and replicas may be stale.
- Choose active-active writes only when the product has explicit conflict rules and teams can operate them. It improves local write availability but introduces split-brain, reconciliation, and invariant risks.

## Failure drill

**Prediction:** failover starts, but the standby is stale and too small for full traffic. **Symptom and detection:** replica lag, saturation, and rising errors appear immediately after routing changes. **Containment:** fence writes, shift only safe read traffic, shed optional work, and protect the standby. **Recovery:** restore missing data or increase verified capacity before resuming writes. **Prevention:** reserve capacity, alert on replication lag, and run full-load evacuation exercises rather than control-plane-only tests.

## Two-to-three-minute interview answer

I would first ask the business to set RTO and RPO per critical journey. Those targets decide whether asynchronous replication and minutes of failover are acceptable or whether we need much more expensive synchronous or active-active designs. I would inventory all regional dependencies, not just application pods: database, object storage, queues, secrets, routing, identity, and operator access.

For many systems I would start with active-passive writes, replicated durable data, health-based global routing, warm standby capacity, and explicit fencing so the failed primary cannot resume writes independently. During failover I would verify lag, fence the old writer, shift safe reads, promote one authority, resume writes deliberately, and reconcile the known gap. Backups are also required because replication can copy corruption. The design is only credible after regular restore and full regional evacuation tests at production-like load.

## Practice status

The artifact is ready; comprehension remains `TODO` until the learner can set defensible RTO/RPO values, trace a fenced failover, and explain one topology trade-off.

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
