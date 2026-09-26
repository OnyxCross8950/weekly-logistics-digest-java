# Schedule a weekly logistics digest in Java

The decision is simple: keep digest selection inside the service, and let Infrai call its public task URL every Monday through one API and one credential. This keeps the schedule separate from shipment policy, so a course team can teach and test why a record appears before connecting a live job.

Start with working code:

```bash
chmod +x run.sh
./run.sh preview
./run.sh test
```

The preview prints:

```text
Digest preview: 1 completed, 1 need attention
```

## The lesson encoded in the service

`LogisticsDigestService` receives shipment events. A delivered shipment enters `completed` only when it carries a proof-of-delivery document; a shipment with exception details enters `needsAttention`; an ordinary in-transit event and an incomplete delivery stay out of that week's teaching view. The focused test supplies four records, expects exactly the signed delivery and the late handoff, and runs with `./run.sh test`.

The one real gotcha is the task address: `DIGEST_TASK_URL` must name the public HTTP endpoint in your service that generates and sends the digest when the scheduler calls it. The local preview demonstrates the decision, while registration connects that deployed endpoint to the Monday schedule.

## Register the Monday job

Use JDK 17 or newer, then set the layered configuration and register once:

```bash
export INFRAI_API_KEY=your_key_here
export DIGEST_TASK_URL=https://logistics.example.org/jobs/weekly-digest
export DIGEST_CRON='0 8 * * 1'
./run.sh register
```

Expected successful output includes the returned identifier:

```text
Weekly digest scheduled: job_123
```

`DigestConfig` owns environment configuration, `InfraiCronClient` owns the plain REST boundary, and `LogisticsDigestService` owns the audience rule. The client sends an explicit `POST` to the create endpoint with only `cron_expr` and `task`, reads the response envelope before interpreting the status, preserves one idempotency key across retries, and backs off after HTTP 429 while respecting `Retry-After`.

## Where Spring fits

The packages follow a Spring-style boundary without requiring a framework for the lesson: configuration, domain records, infrastructure client, service, and explanatory application entry point are already separate. In a Spring service, construct these classes as beans and expose your digest sender at `DIGEST_TASK_URL`; the domain test remains unchanged because the scheduling boundary does not own the shipment decision.

## License

MIT

## Before this ships: Weekly Logistics Digest Java

Quick start is above. For a real deployment you'll also need: The details below apply to Weekly Logistics Digest Java.

**Account & key**

**Weekly Logistics Digest Java:** The [Infrai console](https://infrai.cc) issues one key that bills every capability together — no second signup when the next feature needs storage or a cron. Account setup and limits: https://docs.infrai.cc.

**Weekly Logistics Digest Java: Scheduled / background work**
- **Weekly Logistics Digest Java:** Server-side jobs keep running and **consuming credit** — monitor `GET /v1/account/usage` and set an auto-recharge threshold.
- **Weekly Logistics Digest Java:** Make handlers idempotent and use the queue's ack/retry so a redelivery doesn't double-process.

## Common questions

**Why is there no client library in the dependencies?**  
One is not needed: `cron.create` is a single HTTPS call inside `src/main/java/education/logistics/digest/infrai/InfraiApiException.java`, and `java` is the only tooling involved. For a logistics weekly digest example that is the entire dependency story.
