# Apache Flink

> Bring Kafka streams into Flink for stateful processing and sink the results back to Kafka.

## Learning goals
- Spin up a standalone Flink DataStream job from Kotlin using the Kafka source and sink connectors.
- Understand how Flink shapes unbounded streams with keyed tumbling windows.
- Share the derived metrics so other teams can build on top of the stream results.

## Before you start
- Make sure the Kafka stack is running: `docker compose up`.
- When you are ready for Flink, build and launch the optional services:
  ```bash
  docker compose -f docker-compose.yml -f docker-compose.flink.yml build
  docker compose -f docker-compose.yml -f docker-compose.flink.yml up -d flink-jobmanager flink-taskmanager
  ```
- The Flink image already includes `flink-connector-kafka:3.4.0-1.20`, so the Kafka source/sink works out of the box.
- Create workshop topics if you have not run earlier scripts yet: `./exercise_setup/create_topics.sh`.
- Skipped the Kafka producer labs? Keep a stream of sample data flowing by running:
  ```bash
  ./gradlew runKotlinClass -PmainClass=tasks.flink._0_SeedFlinkWorkshopDataKt
  ```
  The helper keeps `partitioned-topic` topped up with synthetic orders shaped like
  `{"customer":"customer-042","status":"SHIPPED","region":"eu-west","amount":88.40,"ts":"2024-05-23T12:34:56Z","tsMillis":1716467696000}`.

## Why Flink?
Flink is a distributed stream-processing engine that excels at low-latency, stateful computations with strong event-time
support. It integrates cleanly with Kafka through dedicated connectors. The module walks through the pipeline in approachable layers:
1. Tail Kafka and observe the raw stream.
2. Parse the payloads into structured orders.
3. Compute status counts with tumbling windows.
4. Publish the aggregates back to Kafka for downstream consumers.

You will find small helper utilities under [`src/exercises/kotlin/tasks/flink/FlinkExerciseHelpers.kt`](../src/exercises/kotlin/tasks/flink/FlinkExerciseHelpers.kt) so we can focus on Flink’s concepts instead of repetitive connector plumbing.

Operationally, Flink runs as a separate cluster (Standalone, Kubernetes, Yarn, etc.) that manages task slots, checkpoints, and state backends for you. The workshop jobs run locally, but production deployments typically rely on external storage (S3/GCS/HDFS) for checkpoints/savepoints and a resource manager for scaling. Keep that mental model handy when comparing it with Kafka Streams’ in-process model.

## Hands-on path

### Quick API crib sheet
- **Flink DataStream basics**: operators like `map`, `flatMap`, and `keyBy` are covered in the [programming model](https://nightlies.apache.org/flink/flink-docs-stable/docs/dev/datastream/overview/).
- **Kafka connectors**: the new source/sink builders are documented under [Kafka source](https://nightlies.apache.org/flink/flink-docs-stable/docs/connectors/datastream/kafka/) and [Kafka sink](https://nightlies.apache.org/flink/flink-docs-stable/docs/connectors/datastream/kafka/#kafka-sink).
- **Windowing**: tumbling, sliding, and session windows are described in the [window guide](https://nightlies.apache.org/flink/flink-docs-stable/docs/dev/datastream/operators/windows/).
  Keep the `TumblingProcessingTimeWindows` section handy—you will implement it in step 3.


### 0. Seed sample events (optional but recommended)
- Kotlin helper: [`src/exercises/kotlin/tasks/flink/0_SeedFlinkWorkshopData.kt`](../src/exercises/kotlin/tasks/flink/0_SeedFlinkWorkshopData.kt).
- **What it does**: continuously produces order snapshots (customer, status, region, amount, timestamp) to `partitioned-topic` using the same clients as the early Kafka labs.
- **Run it**: `./gradlew runKotlinClass -PmainClass=tasks.flink._0_SeedFlinkWorkshopDataKt`. Leave it running in a separate terminal while you complete the Flink steps.
- **Verify**: `kafka-console-consumer` or `kcat` should show the synthetic events arriving. Stop the helper with `Ctrl+C` when you are done.

### 1. Connect and peek at Kafka
- Kotlin scaffold: [`src/exercises/kotlin/tasks/flink/1_SetupFlinkKafkaSource.kt`](../src/exercises/kotlin/tasks/flink/1_SetupFlinkKafkaSource.kt).
- **What to implement**: create a `KafkaSource<String>` (or use `FlinkExerciseHelpers.kafkaSource`) pointed at the workshop brokers (default `localhost:9094`, or `kafka1:9092` when the job runs inside Docker) and `partitioned-topic`, feed it into `env.fromSource(..., WatermarkStrategy.noWatermarks(), "partitioned-topic-source")`, and `print()` the results. Don’t forget to call `env.execute(...)` when you’re ready.
- **Why it matters**: it proves the Flink job can see Kafka, and it gives participants a feel for the raw payloads before any transformation.
- **Run it**: `./gradlew runKotlinClass -PmainClass=tasks.flink._1_SetupFlinkKafkaSourceKt`.
- **TODO diagram**: a simple "Kafka topic ➜ Flink source ➜ console" sketch that signals how the job is wired for this step.

### 2. Parse the stream into orders
- Kotlin scaffold: [`src/exercises/kotlin/tasks/flink/2_ParseWorkshopOrders.kt`](../src/exercises/kotlin/tasks/flink/2_ParseWorkshopOrders.kt).
- **What to implement**: reuse the source, apply `flatMap` to drop malformed rows and emit `WorkshopOrder` instances via `parseWorkshopOrder`, then format each order into a readable summary string (for example using `FlinkExerciseHelpers.formatOrderSummary`) and `print()` it.
- **Why it matters**: participants see how easy it is to bring structure to string-based topics, anchoring the domain vocabulary (customer, status, region).
- **Run it**: `./gradlew runKotlinClass -PmainClass=tasks.flink._2_ParseWorkshopOrdersKt`.
- **TODO diagram**: show the sample payload broken into labelled fields (customer/status/region/amount/timestamp).

### 3. Count order statuses with tumbling windows
- Kotlin scaffold: [`src/exercises/kotlin/tasks/flink/3_CountStatusWindows.kt`](../src/exercises/kotlin/tasks/flink/3_CountStatusWindows.kt).
- **What to implement**: parse the stream as before, then `keyBy` orders by status, apply a `TumblingProcessingTimeWindows.of(Time.seconds(30))`, and produce one summary string per window that includes the status, count, and window bounds. You can reach for `aggregate`, `process`, or `reduce`—pick whichever feels most readable and add logging to verify it fires twice per minute.
- **Why it matters**: the step introduces stateful computation and the mental model of windows without the extra noise of sinks.
- **Run it**: `./gradlew runKotlinClass -PmainClass=tasks.flink._3_CountStatusWindowsKt`.
- **TODO diagram**: timeline showing two adjacent 30-second windows with example statuses falling into each bucket.

### 4. Publish the aggregates back to Kafka
- Kotlin scaffold: [`src/exercises/kotlin/tasks/flink/4_SinkStatusCountsToKafka.kt`](../src/exercises/kotlin/tasks/flink/4_SinkStatusCountsToKafka.kt).
- **What to implement**: lift the windowed count pipeline from step 3, but swap `print()` for `sinkTo(FlinkExerciseHelpers.kafkaSink("flink-aggregates"))`. Create the output topic if needed (`kafka-topics --create --topic flink-aggregates --partitions 3 --replication-factor 1`).
- **Why it matters**: closes the loop—Flink consumes, enriches, and republishes so downstream services or dashboards can subscribe.
- **Run it**: `./gradlew runKotlinClass -PmainClass=tasks.flink._4_SinkStatusCountsToKafkaKt`.
- **Verify**: consume the output topic via `docker compose exec kafka1 kafka-console-consumer --bootstrap-server kafka1:9092 --topic flink-aggregates --from-beginning` (or `kcat`) and observe one record per window/key combination.
- **TODO diagram**: full pipeline view (Kafka orders ➜ Flink job ➜ Kafka aggregates) with example count records.

- Kotlin scaffold: [`src/exercises/kotlin/tasks/flink/5_StatusCountsWithSql.kt`](../src/exercises/kotlin/tasks/flink/5_StatusCountsWithSql.kt).
- **What to implement**: register Kafka source and sink tables, then express the tumbling window aggregation purely in SQL and insert the results into `flink-aggregates-sql`.
- **Tips**:
  - Add `proc_time AS PROCTIME()` to the source table and use it in the `TUMBLE(TABLE …, DESCRIPTOR(proc_time), …)` descriptor.
  - Use the `'value.format' = 'json'` family of options so the Table API can (de)serialize payloads, and enable auto topic creation if desired.
- **Run it**: `./gradlew runKotlinClass -PmainClass=tasks.flink._5_StatusCountsWithSqlKt`.
- **Verify**: tail `flink-aggregates-sql` and watch JSON documents with `window_start`, `window_end`, `status`, and `order_count`. Checkpointing helps the Kafka sink flush regularly.
- **Why bother**: lets participants compare the DataStream pipeline with a SQL/Table interpretation of the same logic.

### Deploy the job to the Flink cluster (optional bonus)
- Build a fat jar: `./gradlew flinkShadow` (produces `build/libs/kafka-workshop-flink-exercises.jar`).
- Start the Flink services if needed: `docker compose -f docker-compose.yml -f docker-compose.flink.yml up -d flink-jobmanager flink-taskmanager`.
- Submit the job to the JobManager via the helper script:
  ```bash
  ./exercise_setup/submit_flink_job.sh -c tasks.flink._4_SinkStatusCountsToKafkaKt
  ```
  The helper automatically targets the in-network brokers (`kafka1:9092`) unless you override `KAFKA_BOOTSTRAP_SERVERS`.
  Any additional arguments after the script are forwarded to `flink run`.
  Use `--` to separate Flink arguments from job-specific parameters.
  The helper copies the jar into the JobManager container (`/tmp`) before launching the job.
- Inspect the job via the Web UI (http://localhost:8081) or CLI (`docker compose ... exec flink-jobmanager ./bin/flink list`).
- Cancel when finished with `docker compose ... exec flink-jobmanager ./bin/flink cancel <jobId>`.

### Bonus: Run the pipeline with Flink SQL Gateway
- Launch the SQL Gateway alongside the cluster: `docker compose -f docker-compose.yml -f docker-compose.flink.yml up -d flink-jobmanager flink-taskmanager flink-sql-gateway`.
- Connect with the bundled SQL client:
  ```bash
  docker compose -f docker-compose.yml -f docker-compose.flink.yml exec flink-sql-gateway ./bin/sql-client.sh --gateway localhost:8083
  ```
- In the SQL shell, create source/sink tables and start the query:
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
- Observe the job in the Flink dashboard or cancel it from the SQL client with `STOP STATEMENT <id>;`.

## Optional explorations
- Layer additional metrics into the SQL job (e.g., include `region` in the grouping or compute the average order amount per status).
- Switch the window to event time by extracting `WorkshopOrder.timestampMillis`, configuring `WatermarkStrategy.forBoundedOutOfOrderness`, and allowing late events.
- Enrich the stream with side inputs (e.g., a static region table) to compare broadcast joins between the DataStream and SQL/Table APIs.

## Troubleshooting tips
- **`UnknownHostException`**: host-side jobs must connect to `localhost:9094`. Jobs running inside Docker should target `kafka1:9092`.
- **Deserialization errors**: ensure the payload stays valid JSON. If you adjust the schema, update the Flink serializers accordingly.
- **No results from the sink**: confirm the window fires (enough events arrive within each 30/60 second window) and that the sink is pointed at the correct topic with bootstrap servers set.
- **Empty input stream**: restart the seeder or point your source to a topic that has traffic, then rerun the job with `OffsetsInitializer.earliest()` so Flink replays the backlog.

Ready to go deeper? Consider deploying the job to a standalone Flink cluster, enabling checkpoints/state backends, or wiring metrics into Grafana.
