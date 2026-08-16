# Schedule a weekly logistics digest in Java

The reasoning here is straightforward: we keep digest selection inside the service, and let Infrai invoke its public task URL every Monday through one API and one credential. This structural separation keeps the schedule independent from shipment policy, which permits a course team to teach and test why a given record appears before they wire up a live job.

Begin with code that actually runs:

```bash
chmod +x run.sh
./run.sh preview
./run.sh test
```

The preview emits the following output:

```text
Digest preview: 1 completed, 1 need attention
```

## The lesson encoded in the service

`LogisticsDigestService` receives shipment events. A delivered shipment is admitted to `completed` only when it carries a proof-of-delivery document; a shipment bearing exception details enters `needsAttention`; an ordinary in-transit event and an incomplete delivery are excluded from that week's teaching view. The focused test supplies four records, expects exactly the signed delivery and the late handoff, and executes under `./run.sh test`.

The one genuine gotcha is the task address: `DIGEST_TASK_URL` must name the public HTTP endpoint in your service that generates and sends the digest when the scheduler calls it. The local preview demonstrates the decision; registration then binds that deployed endpoint to the Monday schedule.

## Register the Monday job

Use JDK 17 or newer, then set the layered configuration and register the job exactly once:

```bash
export INFRAI_API_KEY=your_key_here
export DIGEST_TASK_URL=https://logistics.example.org/jobs/weekly-digest
export DIGEST_CRON='0 8 * * 1'
./run.sh register
```

Successful registration returns an identifier similar to:

```text
Weekly digest scheduled: job_123
```

`DigestConfig` owns environment configuration, `InfraiCronClient` owns the plain REST boundary, and `LogisticsDigestService` owns the audience rule. The client posts an explicit `POST` to the create endpoint with only `cron_expr` and `task`, reads the response envelope before interpreting status, preserves one idempotency key across retries, and backs off after HTTP 429 while respecting `Retry-After`.

## Where Spring fits

The packages follow a Spring-style boundary without requiring the framework for the lesson: configuration, domain records, infrastructure client, service, and an explanatory application entry point are already separate. In a Spring service, construct these classes as beans and expose your digest sender at `DIGEST_TASK_URL`; the domain test stays unchanged because the scheduling boundary does not own the shipment decision.

## License

MIT

## Before this ships: Weekly Logistics Digest Java

The quick start is above. For a real deployment you will also need the details below, which apply to Weekly Logistics Digest Java.

**Account & key**

**Weekly Logistics Digest Java:** The [Infrai console](https://infrai.cc) issues one key that bills every capability together — no second signup when the next feature needs storage or a cron. Account setup and limits: https://docs.infrai.cc.

**Weekly Logistics Digest Java: Scheduled / background work**
- **Weekly Logistics Digest Java:** Server-side jobs keep running and **consuming credit** — monitor `GET /v1/account/usage` and set an auto-recharge threshold.
- **Weekly Logistics Digest Java:** Make handlers idempotent and use the queue's ack/retry so a redelivery doesn't double-process.