# Media Storage, Processing, and CDN Delivery

## Question

Photo and video uploads are growing. Uploads tie up application servers, local disks fill, and distant users see slow downloads.

**“How would you design reliable media upload, processing, storage, and delivery for an Instagram-like service?”**

## Why this problem exists

Media bytes are much larger than post metadata. Proxying every transfer through stateless API instances consumes their network, connections, and disk, while replicas may not share files. Media also needs validation, variants, durable storage, and global delivery. Control decisions should be separate from byte transfer.

## Analogy

A shipping desk issues a destination label; a warehouse receives and inspects the package; regional depots deliver it near customers.

The analogy stops because digital objects can be copied, cached after deletion, uploaded partially, and processed repeatedly.

## Terms to establish

- **Object storage:** durable file storage addressed by object key.
- **Pre-signed URL:** short-lived permission for one upload or download.
- **Transcoding:** creates delivery variants such as thumbnails or lower-bitrate video.
- **CDN:** caches content at edge locations near users.

## What I would do

The client first asks the API to initialize an upload. The API authenticates the user, validates declared size and type, creates an upload record and unguessable object key, then returns a short-lived pre-signed URL. The client uploads directly to private object storage.

An object-created event starts asynchronous processing. Idempotent workers inspect the real file signature, scan content, strip risky metadata when needed, and create immutable variants. The record moves through `UPLOADING`, `PROCESSING`, `FAILED`, and `READY`; only ready media can be published. A CDN serves versioned objects. I would track upload completion, processing age, failures, orphan objects, origin traffic, and CDN hit rate.

## Concrete walkthrough

1. User `17` initializes a 4 MB JPEG upload `U9` with key `raw/17/U9`.
2. The client uploads directly using a URL valid for ten minutes.
3. Storage emits an event; a worker validates the JPEG and creates small, medium, and large variants.
4. The worker records checksums and marks `U9` ready.
5. The post stores the media identifier, and readers receive a CDN URL for the suitable variant.

API load stays bounded while storage and delivery scale independently.

## Instagram-backend mapping

[MediaController.java](../../../../../src/main/java/com/instagram/backend/controller/MediaController.java) currently accepts multipart bytes through the application and serves `/uploads`. [MediaStorageService.java](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) authenticates, creates a UUID filename, writes local storage, records metrics, and prevents path traversal. [Post.java](../../../../../src/main/java/com/instagram/backend/domain/Post.java) stores an image URL.

**Implemented:** authenticated local upload and retrieval for development. **Proposed:** object storage, upload state, pre-signed transfer, content inspection, asynchronous variants, CDN delivery, and orphan cleanup. Local temporary storage is neither shared nor durable across replicas.

## Choices and trade-offs

- Choose **direct object-storage upload** to reduce API load. Never expose unrestricted credentials; URL lifecycle and completion verification are the cost.
- Choose **asynchronous processing** for expensive variants. Do not present unfinished media as ready; state and retries are the cost.
- Choose **immutable CDN keys** for long caching. Do not overwrite bytes at one URL; extra stored versions are the cost.

## Failure drill

**Prediction:** a worker crashes after creating one variant but before marking the upload ready. **Detection:** processing age exceeds its deadline. **Containment:** keep the object unpublished. **Recovery:** retry idempotently, verify checksums, create missing variants, and mark ready atomically. **Prevention:** durable events, bounded retries, dead-letter handling, reconciliation scans, and age alerts.

## Two-to-three-minute interview answer

I would separate metadata control from byte transfer. The API authenticates the user, creates an upload record, and returns a short-lived pre-signed URL; the client uploads directly to private object storage. A durable event triggers idempotent workers that inspect the true file type, scan it, and create immutable image or video variants. Only a `READY` media record can be attached to a published post.

Readers use CDN URLs backed by object storage, with different authorization rules for public and private media. I would monitor incomplete uploads, processing age, failures, orphan bytes, origin traffic, and CDN hit rate, and use reconciliation to recover lost events.

## Practice status

The artifact is ready; comprehension requires tracing ownership, retry, and delivery without notes.

## Questions and explained answers

<details>
<summary>Why not keep uploads on each Spring Boot instance’s local disk?</summary>

Instances restart and scale independently. A file written to one disk may be absent from another or disappear with that instance.
</details>

<details>
<summary>Why inspect content instead of trusting the filename or content type?</summary>

Both are client-controlled. Signature inspection catches disguised or malformed content before publication or unsafe processing.
</details>

<details>
<summary>How can retries avoid duplicating variants?</summary>

Use deterministic variant keys and an idempotency key. Check existing checksums, write only missing outputs, and make the ready transition atomic.
</details>
