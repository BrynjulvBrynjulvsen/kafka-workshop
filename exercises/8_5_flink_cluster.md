# Exercise 8.5 – Flink Cluster & SQL Gateway

> Deploy the workshop pipeline onto the standalone Flink cluster and operate it through the SQL Gateway.

## Objectives
- Package and submit the Kotlin/DataStream job to the JobManager container.
- Drive the same aggregation purely through SQL using the gateway service.
- Practice operational commands: launching services, inspecting jobs, and stopping statements cleanly.

## Before you start
- Complete the core walkthrough in [`exercises/8_apache_flink.md`](./8_apache_flink.md) so the topics, sample data, and aggregations are familiar.
- Build the Flink Docker image if you have not already:  
  `docker compose -f docker-compose.yml -f docker-compose.flink.yml build`
- Bring up the cluster services (JobManager, TaskManager, and optionally the SQL Gateway):  
  `docker compose -f docker-compose.yml -f docker-compose.flink.yml up -d flink-jobmanager flink-taskmanager`
- Seed Kafka topics if this is a fresh environment: `./exercise_setup/create_topics.sh`.

---

## 1. Submit the Kotlin job to the cluster
- Produce a shaded jar that bundles the exercises:  
  `./gradlew flinkShadow` &rarr; `build/libs/kafka-workshop-flink-exercises.jar`.
- Ship the windowed aggregation job into the cluster via the helper script:

```bash
./exercise_setup/submit_flink_job.sh \
  -c tasks.flink._4_SinkStatusCountsToKafkaKt
```

- The wrapper forwards any extra arguments after the class name to `flink run` and, by default, uses the in-network brokers (`kafka1:9092`). Override `KAFKA_BOOTSTRAP_SERVERS` if needed.
- Inspect the running job through the dashboard (`http://localhost:8081`) or CLI:

```bash
docker compose -f docker-compose.yml -f docker-compose.flink.yml \
  exec flink-jobmanager ./bin/flink list
```

- When you are done, cancel the job:

```bash
docker compose -f docker-compose.yml -f docker-compose.flink.yml \
  exec flink-jobmanager ./bin/flink cancel <jobId>
```

---

## 2. Run the pipeline with Flink SQL Gateway
- Start the gateway alongside the cluster if it is not already running:  
  `docker compose -f docker-compose.yml -f docker-compose.flink.yml up -d flink-sql-gateway`
- Attach the bundled SQL client to the gateway endpoint:

```bash
docker compose -f docker-compose.yml -f docker-compose.flink.yml \
  exec flink-sql-gateway ./bin/sql-client.sh --gateway localhost:8083
```

- Inside the shell, create the Kafka source table. Set both the startup mode and offset reset policy so new sessions begin at the latest records without failing on empty commits:

```sql
CREATE TEMPORARY TABLE orders (
  customer STRING,
  status STRING,
  region STRING,
  amount DOUBLE,
  ts STRING,
  tsMillis BIGINT,
  proc_time AS PROCTIME()
) WITH (
  'connector' = 'kafka',
  'topic' = 'partitioned-topic',
  'properties.bootstrap.servers' = 'kafka1:9092',
  'properties.group.id' = 'sql-gateway-example',
  'properties.auto.offset.reset' = 'latest',
  'scan.startup.mode' = 'latest-offset',
  'value.format' = 'json',
  'value.json.ignore-parse-errors' = 'true'
);
```

- Define the sink table for the aggregates:

```sql
CREATE TABLE flink_aggregates_sql (
  window_start TIMESTAMP_LTZ(3),
  window_end TIMESTAMP_LTZ(3),
  status STRING,
  order_count BIGINT
) WITH (
  'connector' = 'kafka',
  'topic' = 'flink-aggregates-sql',
  'properties.bootstrap.servers' = 'kafka1:9092',
  'format' = 'json',
  'json.timestamp-format.standard' = 'ISO-8601'
);
```

- Launch the tumbling-window aggregation:

```sql
INSERT INTO flink_aggregates_sql
SELECT
  window_start,
  window_end,
  status,
  COUNT(*) AS order_count
FROM TABLE(
  TUMBLE(TABLE orders, DESCRIPTOR(proc_time), INTERVAL '15' SECOND)
)
GROUP BY window_start, window_end, status;
```

- The SQL client prints a statement handle (for example, `Statement_f2b1...`). Use `SHOW STATEMENTS;` to recover it later.
- Stop the statement gracefully with `STOP STATEMENT 'Statement_f2b1...';`. For jobs already finished or failed, no additional cleanup is necessary—rerun the insert to start a fresh statement.

---

## Troubleshooting
- **Missing gateway address**: ensure `docker-compose.flink.yml` passes `-Dsql-gateway.endpoint.rest.address=0.0.0.0` (already configured in the repo).
- **`NoOffsetForPartitionException`**: add both `'scan.startup.mode' = 'latest-offset'` and `'properties.auto.offset.reset' = 'latest'` (or the earliest equivalents) to the source table.
- **Temporary tables shadowing permanent ones**: remove them with `DROP TEMPORARY TABLE orders;` before recreating the catalog table. They disappear automatically when the SQL client session ends.
- **Stopping by job ID**: the SQL client’s `STOP STATEMENT` expects the statement handle, not the Flink job ID. Use `SHOW JOBS WITH HISTORY;` plus `CANCEL JOB '<jobId>';` if you only have the job identifier.

---

## Stretch ideas
- Extend the SQL job with extra dimensions (`region`, `status`) or additional metrics (average amount).
- Swap to event-time processing by exposing `tsMillis`, configuring a watermark strategy, and updating the window descriptor.
- Explore savepoints or `STOP JOB WITH SAVEPOINT` from the JobManager CLI to practice controlled shutdowns.
