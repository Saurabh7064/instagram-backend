# 12-Week Execution Plan (Build + System Design)

Use this plan with your command protocol:
- `next feature`
- `next concept`

Each week has one primary feature and one primary concept.

## Execution rules per week

1. Run `next feature` until weekly feature target is complete.
2. Run `next concept` for weekly concept target.
3. Evidence required:
   - integration test(s)
   - `curl` or `.http` API verification
   - browser demo for UI-impacting work
4. Update and commit:
   - [Feature Learning Backlog](../features/learning-backlog.md)
   - [System Design Learning Backlog](./learning-backlog.md)
   - [Commit Map](../commit-map.md)

---

## Week-by-week roadmap

| Week | Feature target (project) | Concept target (system design) | Output evidence |
|---|---|---|---|
| 1 | F-006 JWT access token issuance | SD-003 Token lifecycle (access + refresh) | login returns token, integration test, curl/http |
| 2 | F-007 Protected endpoint (`/api/me`) | Strong vs eventual consistency tradeoffs for auth/session reads | protected API test + browser demo |
| 3 | F-008 Refresh token flow | Idempotency + retry/backoff basics | refresh flow tests + curl/http |
| 4 | Add API idempotency key support for write paths | SD-004 API versioning and compatibility | integration test + failure/retry scenario |
| 5 | Feed read API with cursor pagination | Caching strategy fundamentals | pagination test + browser feed demo |
| 6 | Rate limiting for auth and post APIs | SD-006 Rate limiting and abuse protection | limit tests + curl proof |
| 7 | Basic observability setup (request IDs, metrics endpoint) | SD-009 Observability internals | trace/log sample + health dashboard notes |
| 8 | Queue-backed event for "new post" activity | Messaging: at-least-once vs exactly-once | integration test with async flow |
| 9 | Notification worker prototype | Backpressure and consumer tuning | queue lag handling test |
| 10 | Search endpoint prototype (basic inverted index approach) | Search consistency tradeoffs | search tests + demo |
| 11 | Recommendation baseline (simple ranking signals) | Graph/reco fundamentals and cold start | ranking test + feature note |
| 12 | Multi-region design draft + failover runbook (doc + small simulation) | HA/resilience + RTO/RPO | architecture note + failover checklist |

---

## Milestone checkpoints

### End of Week 4
- Secure auth baseline complete (JWT + refresh + `/api/me` + idempotency basics)

### End of Week 8
- Scalable read/write flow with caching, rate limiting, and async events

### End of Week 12
- End-to-end architecture maturity: reliability, search, recommendations, resilience plan

---

## How to operate this with Codex

- Say `next feature` to progress feature target in order.
- Say `next concept` to progress concept target in order.
- If you want to follow this plan strictly, say: `next feature (week X)` or `next concept (week X)`.
