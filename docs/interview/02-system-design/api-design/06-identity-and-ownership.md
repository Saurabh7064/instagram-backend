# 06. Identity and ownership checks

## Question

How do you stop a user from modifying somebody else’s post?

[Module sequence](README.md) · Artifact: READY · Learning: TODO

## What you will learn

Explain exactly why user 9 cannot edit user 7's post, then apply that reasoning to lists, editable fields, and business actions. Browser security mechanisms are a later supporting distinction.

Builds on [error contracts](05-error-contracts.md).

## Signing in answers only the first question

Post 42 belongs to user 7. User 9 signs in successfully, changes the URL to `/api/posts/42`, and submits an edit. Their token is valid. Should the edit work?

The token answers “who is calling?” Checking that identity is **authentication**. We still need to answer “may this person change this post?” Checking permission is **authorization**. For an owner-only edit, compare the stored author's ID, 7, with the authenticated caller's ID, 9. They differ, so reject the edit and leave the caption unchanged.

Notice where the owner comes from: the saved post. Accepting `authorId:7` in the request would let the attacker supply the very fact we are trying to verify. The token identifies the caller, and stored data identifies the owner; neither fact should be replaced by a claim in the edit body.

A building badge provides the same first distinction: it identifies someone, but does not grant access to every office. The comparison has a limit because API permissions can depend on the specific object and can change between requests.

The [error-contract lesson](05-error-contracts.md) covers how to communicate a denial. First we need to make the decision at the point that protects the write.

## The existing owner check

The project uses [Spring Boot 4.0.2 and Java 24](../../../../build.gradle). The excerpt below is a design proposal unless identified as current code; omitted imports and collaborators still need implementation.

```java
private Post requireOwnedPost(UserAccount viewer, Long postId) {
    var post = requirePost(postId);
    if (!post.getAuthor().getId().equals(viewer.getId())) {
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You can only modify your own posts");
    }
    return post;
}
```

### Read the boundary in the code

This is the current ownership helper with the fully qualified user type shortened. `requirePost` loads the stored target; the comparison then uses its author's ID and the viewer already resolved from the token. A mismatch throws before the helper returns an editable post. The caller therefore cannot establish ownership by adding an author ID to the body.

The current update transaction includes lookup and mutation. If a future policy permits ownership or visibility to change concurrently, that policy will also need an explicit locking or revision rule.

## Follow user 9's attempted edit

Start with post 42 owned by user 7 and caption unchanged from its saved value. The token service resolves the submitted valid token to user 9. The post service loads post 42, reads its stored author ID 7, and compares it with 9. They are different, so it throws 403 before applying the edit. A fresh read should still show the original caption.

Removing the token changes which question fails: the server cannot establish the caller's identity and rejects authentication before it compares owners. Removing the post instead makes the current lookup return 404. These are distinct facts, even if an API deliberately chooses a concealment policy for some private resources.

For an implementation test, check both the denial response and unchanged stored state. A method that writes first and throws 403 afterwards has failed its permission promise.

## Permissions apply to rows, fields, and transitions

Object authorization is the owner check already shown. Collection authorization means every returned row belongs to the viewer’s allowed scope. Field authorization means a caller may edit caption but cannot promote themselves by writing role=ADMIN. Transition authorization means permission to read a draft does not imply permission to publish it.

| Request | Identity | Required rule | Expected failure |
|---|---|---|---|
| Edit my post | Valid owner | Stored author matches viewer | Missing post -> 404 |
| Edit another user’s post | Valid non-owner | Same rule | Current service -> 403 |
| Read a private collection | Valid follower/non-follower | Scope query by visibility | Never load all then leak before filtering |
| Create like for body userId=7 | Token user 9 | Ignore/reject caller-selected actor | Actor remains user 9 |
| Poll media job | Valid non-owner | Job owner matches viewer | Consistent denial/concealment |

### Authenticate once, authorize at the business boundary

A proposed security integration could supply an authenticated principal to controllers rather than passing raw headers everywhere. Do not copy `@AuthenticationPrincipal` into this project and assume it works: build.gradle includes password crypto, not a configured full security filter chain. The current implementation calls TokenService inside services. Introducing filter-chain authentication is a separate behavior change with tests.

Even with central authentication, keep domain checks close to the mutation. A controller check followed by a reusable unprotected service method invites another caller to bypass it. Perform the ownership decision and write in an appropriate transaction; if ownership or visibility can change concurrently, define the locking/version policy.

### CORS, CSRF, and bearer tokens solve different problems

Cross-Origin Resource Sharing (CORS) is a browser rule controlling which origins may read certain responses or issue preflighted requests. It does not stop a scripted caller using curl. Cross-Site Request Forgery (CSRF) is an attack that induces a browser to send ambient credentials, commonly cookies, on a request a malicious site induced. A token supplied explicitly in an Authorization header has a different threat model from a session cookie; switching to cookies requires a CSRF design. Transport Layer Security (TLS) protects tokens in transit, but cannot stop a compromised client or token leakage in logs.

Sequential IDs reveal that another object may exist but guessing-resistant IDs do not establish permission. For private objects, a consistent 404 policy can reduce existence disclosure, at the cost of less explicit diagnostics. Keep internal audit evidence separate from the public denial response.

**Adversarial trace:** user 9 sends valid token plus postId 42 and authorId 7 in JSON. DTO excludes authorId -> service resolves token to 9 -> loads stored author 7 -> denies -> no write. Repeat via batch and nested routes: they require the same rule for every item. A permission check only on the collection’s first item is insufficient.

## Apply this to the backend

[PostService.update and delete](../../../../src/main/java/com/instagram/backend/service/PostService.java) resolve the viewer and call `requireOwnedPost` before mutation. `requireViewer` uses the current custom [TokenService](../../../../src/main/java/com/instagram/backend/service/TokenService.java); a new filter-chain security model is not installed by this lesson.

For F-017, decide whether a post's author may delete someone else's comment and whether commenters may delete their own. Test each identity against stored ownership, including a body that claims a different author.

## Useful later: choices and costs

403 communicates denial; 404 can intentionally hide existence but should be applied consistently. URL secrecy is not authorization. Central security filters can reduce duplicated identity parsing; service-level ownership rules still express domain policy. CORS controls browser access behavior and does not authenticate curl callers.

## Change only the target ID, keep a valid token

For a future test, sign in as user 9 and target user 7's post 42. Predict 403 under the current contract and an unchanged caption. A successful edit, or even a denied response with a changed row, demonstrates a broken ownership boundary.

Detect it using both identities and fresh persisted state. Contain the affected mutation route if it is leaking write access; investigate and restore affected data from reviewed audit or backup evidence rather than guessing. Recovery is complete only when the owner still can edit and the non-owner cannot. Prevent recurrence by reusing the service-level rule across single-item, nested, and batch entry points. This is a planned check, not evidence of an incident or a completed lab.

## Interview-ready answer

I derive identity from validated credentials and decide permission for each object, field, collection, and transition. Knowing a post ID or carrying a valid token does not grant edit rights. I keep ownership near the business mutation and scope collection queries to visible rows. In this backend tokens are parsed in services; adding a full security filter chain would be a real separately tested change. CORS is a browser access mechanism, not authentication, and a move to cookie credentials requires a CSRF design. I test owner/non-owner requests, attacker-supplied actor IDs, nested membership, and mixed-permission batch items, then assert denied writes leave data unchanged.

## Before moving on

Walk user 9's attempt against post 42 without notes. Continue when you can identify the trusted identity and owner sources, explain the 403, and specify a check that proves no write occurred.

Attempt the questions before opening their answers and record the explanation in [learner progress](learner-progress.md). This revision leaves learning status unchanged; a finished document is not evidence that you have demonstrated the idea.

## Questions and explained answers

<details>
<summary>1. Why is a valid token insufficient for updating a post?</summary>

The token identifies the caller, not the owner of the requested post. Load the stored author ID and compare it with the authenticated identity before mutation; never accept a body authorId as proof.

</details>

<details>
<summary>2. Can curl bypass permissions by ignoring CORS?</summary>

CORS does not impose domain permissions on curl in the first place. The API must authenticate and authorize every request independently of browser origin enforcement.

</details>

<details>
<summary>3. Can this project use @AuthenticationPrincipal without extra setup?</summary>

It is not currently configured as a full security-chain authentication model. Password crypto and custom token parsing do not automatically supply a resolved Spring Security principal.

</details>

<details>
<summary>4. What changes when credentials move from an explicit bearer header to cookies?</summary>

Browsers may attach cookies automatically, creating a CSRF threat model. Cookie policy, CSRF defenses, origins, and session behavior require explicit design.

</details>

<details>
<summary>5. Why authorize fields and transitions as well as rows?</summary>

An owner may edit caption but not authorId, counters, or an arbitrary publication status. Ownership of the object does not grant every possible mutation.

</details>
