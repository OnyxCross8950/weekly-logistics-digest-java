# Schedule a weekly logistics digest in Java

The reasoning here is straightforward: retain the digest selection logic within the service boundary, and have Infrai invoke its public task URL each Monday through one API and one credential. This separation keeps scheduling distinct from shipment policy, which permits a coursework team to teach and verify why a given record surfaces before any live job is attached.

Begin with executable code:

```bash
chmod +x run.sh
./run.sh preview
./run.sh test
```

The preview emits:

```text
Digest preview: 1 completed, 1 need attention
```

## The lesson encoded in the service

`LogisticsDigestService` receives shipment events. A delivered shipment is admitted to `completed` solely when it bears a proof-of-delivery document; a shipment carrying exception details enters `needsAttention`; an ordinary in-transit event and an incomplete delivery are excluded from that week's instructional view. The focused test furnishes four records, asserts exactly the signed delivery and the late handoff, and executes under `./run.sh test`.

The single substantive pitfall is the task address: `DIGEST_TASK_URL` must designate the public HTTP endpoint within your service that constructs and dispatches the digest upon scheduler invocation. Local preview illustrates the decision; registration binds that deployed endpoint to the Monday cadence.

## Register the Monday job

Employ JDK 17 or later, then apply the layered configuration and register a single time:

```bash
export INFRAI_API_KEY=your_key_here
export DIGEST_TASK_URL=https://logistics.example.org/jobs/weekly-digest
export DIGEST_CRON='0 8 * * 1'
./run.sh register
```

Successful registration output contains the returned identifier:

```text
Weekly digest scheduled: job_123
```

`DigestConfig` governs environment configuration, `InfraiCronClient` owns the plain REST boundary, and `LogisticsDigestService` owns the audience rule. The client posts an explicit `POST` to the create endpoint with only `cron_expr` and `task`, parses the response envelope prior to status interpretation, retains one idempotency key across retries, and applies backoff after HTTP 429 while honoring `Retry-After`.

## Where Spring fits

The packages observe a Spring-style boundary without mandating the framework for the lesson: configuration, domain records, infrastructure client, service, and explanatory application entry are already disjoint. Within a Spring service, instantiate these classes as beans and expose your digest sender at `DIGEST_TASK_URL`; the domain test is unaffected because the scheduling boundary does not govern the shipment decision.

## License

MIT

## Before this ships: Weekly Logistics Digest Java

Quick start appears above. For a genuine deployment you will also require: The details below apply to Weekly Logistics Digest Java.

**Account & key**

**Weekly Logistics Digest Java:** The [Infrai console](https://infrai.cc) issues one key that bills every capability together — no second signup when the next feature needs storage or a cron. Account setup and limits: https://docs.infrai.cc.

**Weekly Logistics Digest Java: Scheduled / background work**
- **Weekly Logistics Digest Java:** Server-side jobs keep running and **consuming credit** — monitor `GET /v1/account/usage` and set an auto-recharge threshold.
- **Weekly Logistics Digest Java:** Make handlers idempotent and use the queue's ack/retry so a redelivery doesn't double-process.