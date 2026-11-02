# Media Storage, Processing, and CDN Delivery

## Question

Photo and video uploads are growing. Uploads tie up application servers, local disks fill, and distant users see slow downloads.

**“How would you design reliable media upload, processing, storage, and delivery for an Instagram-like service?”**


## Follow one photo, rather than one large HTTP request

User 17 wants to upload a 4 MB JPEG. The API needs to decide whether the user may upload it and which post can use it. Moving all four megabytes through the same application is a separate job, and doing that for every photo consumes connections, bandwidth and local disk.

**Object storage** keeps file-like objects under keys. In our proposed design, the API creates upload record `U9` and reserves key `raw/17/U9`. It then issues a **pre-signed URL**: a short-lived authorization for a specific storage operation. The client uses it to transfer the bytes directly to storage, without receiving general storage credentials.

Creating the upload record does not prove that the bytes arrived. Set an expiry and verify the completed object, including ownership and applicable size/type rules, before allowing the upload to advance.

## Uploaded does not yet mean ready to publish

After the object arrives, a background worker inspects the actual content and creates the required variants. A filename ending in `.jpg` is client-controlled; it cannot establish that the file is a valid JPEG. Keep the object private while validation or processing is unfinished.

For `U9`, the worker validates the original and produces small, medium and large image versions. The upload record progresses from `UPLOADING` to `PROCESSING`, then `READY` or `FAILED`. Mark it ready only when the required outputs have been verified. Processing a video into delivery formats is commonly called transcoding; an image resize is a simpler instance of producing a variant.

A shipping desk, warehouse and local depot make a useful analogy: permission, receipt/inspection and delivery are different responsibilities. Digital storage adds failures the analogy misses, including partial uploads, duplicate events and cached copies surviving a change at the source.

## Deliver the finished version near the reader

A **content delivery network (CDN)** can cache suitable media near readers, reducing repeated transfers from the original storage. Give each finished version an immutable key: changing the bytes means a new key. That makes long-lived caching easier to reason about.

Public and private objects still need different access policies. Guess-resistant filenames do not establish permission. Deletion also needs a cache and access-revocation policy; deleting the original alone does not prove every cached copy has disappeared.

Separate these stages because they scale and fail differently. API authorization, byte transfer and background processing each need their own completion evidence.

## Where this appears in our backend

[MediaController.java](../../../../../src/main/java/com/instagram/backend/controller/MediaController.java) currently accepts multipart bytes through the application and serves `/uploads`. [MediaStorageService.java](../../../../../src/main/java/com/instagram/backend/service/MediaStorageService.java) authenticates, creates a UUID filename, writes local storage, records metrics, and prevents path traversal. [Post.java](../../../../../src/main/java/com/instagram/backend/domain/Post.java) stores an image URL.

**Implemented:** authenticated local upload and retrieval for development. **Proposed:** object storage, upload state, pre-signed transfer, content inspection, asynchronous variants, CDN delivery, and orphan cleanup. Local temporary storage is neither shared nor durable across replicas.

## Choosing the approach and paying its costs

- Choose **direct object-storage upload** to reduce API load. Never expose unrestricted credentials; URL lifecycle and completion verification are the cost.
- Choose **asynchronous processing** for expensive variants. Do not present unfinished media as ready; state and retries are the cost.
- Choose **immutable CDN keys** for long caching. Do not overwrite bytes at one URL; extra stored versions are the cost.


## Paper drill: the worker stops halfway

Before continuing, predict the state of `U9` if its small variant exists but the worker stops before creating medium and large. It must remain unfinished; one output is not evidence that publication is safe.

Detect it through excessive time in `PROCESSING` and a comparison of required and existing outputs. Keep the media unpublished. Retry with stable upload and variant identities, verify existing output content, and create only missing or invalid variants before completing the ready transition.

A periodic comparison of upload records and stored objects can recover missed events and find abandoned uploads. Clean up only under a documented retention policy, with checks that an object is not referenced. Retried work and orphan cleanup are part of the design, not evidence that one event will always run exactly once.

## Two-to-three-minute interview answer

I would separate metadata control from byte transfer. The API authenticates the user, creates an upload record, and returns a short-lived pre-signed URL; the client uploads directly to private object storage. A durable event triggers idempotent workers that inspect the true file type, scan it, and create immutable image or video variants. Only a `READY` media record can be attached to a published post.

Readers use CDN URLs backed by object storage, with different authorization rules for public and private media. I would monitor incomplete uploads, processing age, failures, orphan bytes, origin traffic, and CDN hit rate, and use reconciliation to recover lost events.


## Before moving on

Artifact: `READY`; comprehension: `TODO`. Explain this scenario in your own words, predict the paper drill, and attempt the questions before opening their answers. Record learning only from your own explanation and predictions.

## Questions and explained answers

<details>
<summary>Why not keep uploads on each Spring Boot instance’s local disk?</summary>

Instances restart and scale independently. A file written to one disk may be absent from another or disappear with that instance.
</details>

<details>
<summary>Why inspect content instead of trusting the filename or content type?</summary>

Both are client-controlled. Inspect and decode the actual bytes using supported formats, strict size/resource limits and appropriate isolation. A matching signature helps identify a format but does not prove that the entire file is valid or safe.
</details>

<details>
<summary>How can retries avoid duplicating variants?</summary>

Use deterministic variant keys and an idempotency key. Check existing checksums, write only missing outputs, and make the ready transition atomic.
</details>
