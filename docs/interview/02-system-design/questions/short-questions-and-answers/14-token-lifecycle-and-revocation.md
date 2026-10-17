# Token Lifecycle and Revocation

## Question

A user logs out after a phone is stolen, but the stolen week-long refresh token can still obtain new access tokens.

**“How would you design access tokens, refresh tokens, logout, and revocation at scale?”**

## Why this problem exists

Self-contained access tokens scale because services can verify them without a database lookup, but that also makes immediate revocation difficult. Long-lived refresh tokens reduce repeated login friction and therefore carry more risk. Logout, rotation, key changes, and theft detection need explicit server-side state.

## Analogy: hotel key cards

A guest uses a short-lived room key while the front desk keeps the authoritative stay record and can issue or cancel replacements. The analogy stops because copied digital tokens can be replayed from anywhere and may remain valid until expiry unless checked against server state.

## Terms to establish

- An **access token** is a short-lived credential presented to APIs.
- A **refresh token** obtains new tokens without asking for the password again.
- **Rotation** replaces a refresh token after use and invalidates the previous one.
- A **token family** links successive refresh tokens from one login session so reuse can revoke the whole chain.

## What I would do

1. Issue short-lived signed access tokens with minimal claims, audience, issuer, expiry, and a key identifier.
2. Store only a hash of each refresh token or opaque session secret with user, device, family, expiry, and revocation state.
3. Rotate on every refresh in one transaction; if an already-used token reappears, revoke the family as probable theft.
4. Logout revokes the current session or all user sessions; access tokens expire quickly or are checked against a revocation/session version for high-risk actions.
5. Rotate signing keys with overlapping verification, secure key storage, audit events, and emergency procedures.

## Concrete walkthrough

1. Login creates session family `f7`, refresh record `r1`, and a 15-minute access token.
2. A legitimate refresh consumes `r1`, stores `r2`, and returns new tokens atomically.
3. A thief later replays `r1`; the server sees that it was already used.
4. The service revokes family `f7`, rejects `r1` and `r2`, terminates that device session, and records a security event.
5. Existing access tokens remain usable only until their short expiry unless a high-risk endpoint performs an additional session check.

## Instagram-backend mapping

[TokenService](../../../../../src/main/java/com/instagram/backend/service/TokenService.java) signs 15-minute access JWTs and seven-day refresh JWTs and enforces their `type` claim. [AuthService](../../../../../src/main/java/com/instagram/backend/service/AuthService.java) returns a new refresh token from the refresh endpoint. [application.properties](../../../../../src/main/resources/application.properties) configures both lifetimes and the signing secret.

**Proposed, not implemented:** persisted hashed refresh sessions, one-time rotation state, token families, reuse detection, logout/revocation endpoints, device management, and signing-key identifiers. Current refresh JWTs are not server-revocable before expiry.

## Choices and trade-offs

- Use short access-token lifetimes to bound stolen-token exposure. Extremely short lifetimes increase refresh load and sensitivity to clock or network problems.
- Use stateful refresh sessions for revocation and theft detection. They add a database lookup to refresh, not necessarily to every API request.
- Use an access-token denylist only for exceptional immediate revocation; global denylist checks reduce the main scalability benefit of self-contained tokens.

## Failure drill

**Prediction:** an old rotated refresh token is replayed after theft. **Symptom and detection:** a consumed token ID appears again, possibly from a new location or device. **Containment:** revoke the entire token family and block further refresh. **Recovery:** require reauthentication, terminate affected sessions, and notify the user according to policy. **Prevention:** store hashed tokens, rotate once, detect reuse atomically, protect signing keys, and audit session activity.

## Two-to-three-minute interview answer

I would use short-lived signed access tokens for efficient API verification and stateful, rotating refresh sessions for long-lived access. The server stores only a refresh-token hash with its user, device, token family, expiry, and revocation state. Refresh consumes the old record and creates the replacement atomically.

If an already-used refresh token reappears, I treat that as possible theft and revoke the whole family, because I cannot know whether the legitimate client or attacker holds the newest token. Logout revokes one session or all sessions. Ordinary access tokens expire quickly; especially sensitive actions may also verify session state when immediate revocation matters more than a fully stateless path.

The current project distinguishes access and refresh JWTs and rotates the returned token value, but it has no persisted session or reuse detection, so logout cannot revoke a refresh token before expiry. That is the next correctness and security step.

## Practice status

The artifact is `READY`; comprehension remains `TODO` until the learner can trace rotation, explain family revocation, and state the statelessness trade-off without notes.

## Questions and explained answers

<details>
<summary>Why store a hash of the refresh token instead of its plaintext value?</summary>

A database leak should not immediately provide usable session credentials. The presented token can be hashed and compared, much like a password-derived verifier.
</details>

<details>
<summary>Why revoke the whole token family after reuse?</summary>

Reuse proves that at least two parties may possess tokens from the chain, but the server cannot know which is legitimate. Revoking the family safely ends that compromised session.
</details>

<details>
<summary>Why not check a denylist for every access-token request?</summary>

That provides immediate revocation but adds shared state and latency to every request. Short expiry plus selective checks for high-risk actions often gives a better balance.
</details>
