# Token Lifecycle and Revocation

## Question

A user logs out after a phone is stolen, but the stolen week-long refresh token can still obtain new access tokens.

**“How would you design access tokens, refresh tokens, logout, and revocation at scale?”**


## Why does logout fail to end the stolen session?

The stolen phone holds a refresh token that remains valid for a week. If the server checks only its signature and expiry, it sees a valid credential each time. A logout button that merely erases the legitimate client's local copy cannot erase the thief's copy.

An **access token** is the credential sent to ordinary API endpoints. A **refresh token** obtains replacement credentials without asking for the password again. Short access-token lifetimes limit exposure, but a usable refresh token can keep producing new access until its own rules stop it.

The server therefore needs a session record it can revoke. Store a verifier for the refresh secret, rather than a reusable plaintext secret, together with the session owner, expiry and state. A hotel front desk that can cancel a guest's replacement keys is a helpful analogy. Digital copies add a difficult detail: the server cannot know which person is holding a copied valid credential.

## Follow one replacement chain

Login creates session family `f7` with refresh token `r1`. The family is the chain of replacements belonging to this login. On refresh, a transaction consumes `r1` and creates `r2`. This one-time replacement is **rotation**; returning a different token value without remembering consumption is not enough.

If `r1` appears again, the server has evidence of replay or a client retry/race. Under a strict reuse policy it revokes `f7`, including `r2`, and requires login again. That contains a possible theft but can also interrupt a legitimate session after a lost refresh response. Coordinate client refresh attempts and document this trade-off; replay is a security signal, not proof of a particular attacker.

## What happens to access tokens already issued?

Revoking `f7` stops future refreshes. A self-contained access token may still pass signature and expiry checks until its short lifetime ends. High-risk endpoints can additionally check session state when immediate revocation is required. That check introduces shared state and an availability dependency into those requests.

Keep access-token claims minimal and verify the intended issuer, audience, expiry and token purpose. Plan signing-key rotation and secure key storage separately from refresh-token rotation: replacing a session token and replacing a signing key solve different problems.

The design choice is the amount of revocation delay the product accepts, and where it will pay for a server-side check to shorten it.

## Where this appears in our backend

[TokenService](../../../../../src/main/java/com/instagram/backend/service/TokenService.java) signs 15-minute access JWTs and seven-day refresh JWTs and enforces their `type` claim. [AuthService](../../../../../src/main/java/com/instagram/backend/service/AuthService.java) issues a refresh JWT again from the refresh endpoint, without recording one-time consumption. [application.properties](../../../../../src/main/resources/application.properties) configures both lifetimes and the signing secret.

**Proposed, not implemented:** persisted hashed refresh sessions, one-time rotation state, token families, reuse detection, logout/revocation endpoints, device management, and signing-key identifiers. Current refresh JWTs are not server-revocable before expiry. Reissuing them does not guarantee a different token value when the encoded claims and timestamps are identical.

## Choosing the approach and paying its costs

- Use short access-token lifetimes to bound stolen-token exposure. Extremely short lifetimes increase refresh load and sensitivity to clock or network problems.
- Use stateful refresh sessions for revocation and theft detection. They add a database lookup to refresh, not necessarily to every API request.
- Use an access-token denylist only for exceptional immediate revocation; global denylist checks reduce the main scalability benefit of self-contained tokens.


## Paper drill: `r1` returns after `r2` was issued

Predict the strict-policy response to a repeated `r1`. Its consumed state should be detected atomically, the affected family should be revoked, and further refreshes from that family should fail.

Inspect session history to distinguish likely theft from accidental parallel refresh or a lost response, without logging token secrets. Contain possible compromise through family revocation; recover through reauthentication and the product's session-review process.

Prevent avoidable repeats by coordinating refresh on the client and testing concurrent refresh and lost-response behavior. Protect refresh verifiers and signing keys, and state explicitly how long already-issued access tokens can continue working after revocation.

## Two-to-three-minute interview answer

I would use short-lived signed access tokens for efficient API verification and stateful, rotating refresh sessions for long-lived access. The server stores only a refresh-token hash with its user, device, token family, expiry, and revocation state. Refresh consumes the old record and creates the replacement atomically.

If an already-used refresh token reappears, I treat that as possible theft under a strict reuse policy and revoke the family. I would also document the legitimate retry/concurrency cases and coordinate client refreshes, because the server cannot reliably identify the rightful holder from replay alone. Logout revokes one session or all sessions. Ordinary access tokens expire quickly; especially sensitive actions may also verify session state when immediate revocation matters more than a fully stateless path.

The current project distinguishes access and refresh JWTs and issues them again on refresh, but it has no persisted consumption state or reuse detection, so logout cannot revoke a refresh token before expiry. That is the next correctness and security step.


## Before moving on

Artifact: `READY`; comprehension: `TODO`. Explain this scenario in your own words, predict the paper drill, and attempt the questions before opening their answers. Record learning only from your own explanation and predictions.

## Questions and explained answers

<details>
<summary>Why store a hash of the refresh token instead of its plaintext value?</summary>

A database leak should not immediately provide usable session credentials. The presented token can be hashed and compared, much like a password-derived verifier.
</details>

<details>
<summary>Why revoke the whole token family after reuse?</summary>

Reuse can indicate theft, but a lost response or concurrent legitimate refresh can also repeat a consumed token. A strict policy revokes the family because the server cannot reliably identify the legitimate holder. That contains possible compromise at the cost of possible forced reauthentication.
</details>

<details>
<summary>Why not check a denylist for every access-token request?</summary>

That provides immediate revocation but adds shared state and latency to every request. Short expiry plus selective checks for high-risk actions often gives a better balance.
</details>
