# Apache Flink

> Bring Kafka streams into Flink for stateful processing and sink the results back to Kafka.

## Learning goals
- Spin up a standalone Flink DataStream job from Kotlin using the new Kafka source and sink connectors.
- Enrich and aggregate Kafka events with keyed streams and tumbling windows.
- Push transformed results back to Kafka so other services can consume them.

## Before you start
- Make sure the Kafka stack is running: `docker compose up`.
- When you are ready for Flink, build and launch the optional services:
  ```bash
  docker compose -f docker-compose.yml -f docker-compose.flink.yml build
  docker compose -f docker-compose.yml -f docker-compose.flink.yml up -d flink-jobmanager flink-taskmanager
  ```
- The Flink connector image already includes `flink-connector-kafka:3.4.0-1.20`, so the Kafka source/sink works out of the box.
- Create workshop topics if you have not run earlier scripts yet: `./exercise_setup/create_topics.sh`.
- If you skipped the Kafka producer exercises, keep a stream of sample data flowing by running:
  ```bash
  ./gradlew runKotlinClass -PmainClass=tasks.flink._0_SeedFlinkWorkshopDataKt
  ```
  This helper keeps `partitioned-topic` topped up with synthetic order events shaped like `customer=customer-042,status=SHIPPED,region=eu-west,amount=88.40,ts=2024-05-23T12:34:56Z`.

## Why Flink?
Flink is a distributed stream-processing engine that excels at low-latency, stateful computations with strong event-time
support. It integrates cleanly with Kafka through dedicated connectors. The exercises below walk through a minimal event pipeline:
1. Tail Kafka with a Flink `StreamExecutionEnvironment`.
2. Enrich and aggregate the stream using keyed windows.
3. Emit the derived metrics back to Kafka.

## Hands-on path

### 0. Seed sample events (optional but recommended)
- Kotlin helper: [`src/exercises/kotlin/tasks/flink/0_SeedFlinkWorkshopData.kt`](../src/exercises/kotlin/tasks/flink/0_SeedFlinkWorkshopData.kt).
- **What it does**: continuously produces order snapshots (customer, status, region, amount, timestamp) to `partitioned-topic` using the same clients as the early Kafka labs.
- **Run it**: `./gradlew runKotlinClass -PmainClass=tasks.flink._0_SeedFlinkWorkshopDataKt`. Leave it running in a separate terminal while you complete the Flink steps.
- **Verify**: `kafka-console-consumer` or `kcat` should show the synthetic events arriving. Stop the helper with `Ctrl+C` when you are done.

### 1. Wire Flink to Kafka
- Kotlin scaffold: [`src/exercises/kotlin/tasks/flink/1_SetupFlinkKafkaSource.kt`](../src/exercises/kotlin/tasks/flink/1_SetupFlinkKafkaSource.kt).
- **What to implement**: configure a `KafkaSource<String>` that points to the host-exposed broker (`localhost:9094`), subscribes to `Constants.PARTITIONED_TOPIC`, and starts from `OffsetsInitializer.earliest()`. Feed the source into `env.fromSource(..., WatermarkStrategy.noWatermarks(), "partitioned-topic-source")` and call `print()` so you can confirm the job ingests data.
- **Run it**: `./gradlew runKotlinClass -PmainClass=tasks.flink._1_SetupFlinkKafkaSourceKt`.
- **Verify**: observe the seeded records flowing through your console. If nothing appears, double-check that the seeder (or another producer) is running and that the job connects to `localhost:9094` (not `kafka1:9092`).

### 2. Add transformations and aggregations
- Kotlin scaffold: [`src/exercises/kotlin/tasks/flink/2_ProcessAndAggregate.kt`](../src/exercises/kotlin/tasks/flink/2_ProcessAndAggregate.kt).
- **What to implement**: parse the raw strings into a lightweight event type—splitting on commas and `substringAfter("=")` matches the helper payloads nicely. Use `keyBy` on an attribute (for example, `status` or `region`) and window the stream with `TumblingProcessingTimeWindows.of(Time.seconds(30))`. Use `reduce` or `aggregate` to count events per key per window and emit a descriptive string that includes the window bounds.
- **Execution time vs event time**: the scaffold starts with processing time windows for simplicity, but you can switch to event time by extracting the `ts` field and assigning watermarks via `WatermarkStrategy.forBoundedOutOfOrderness`.
- **Run it**: `./gradlew runKotlinClass -PmainClass=tasks.flink._2_ProcessAndAggregateKt`.
- **Verify**: watch the aggregated output appear twice per minute (for a 30-second window) while your producer keeps feeding the topic.

### 3. Sink the results back to Kafka
- Kotlin scaffold: [`src/exercises/kotlin/tasks/flink/3_WriteAggregatesToKafka.kt`](../src/exercises/kotlin/tasks/flink/3_WriteAggregatesToKafka.kt).
- **What to implement**: configure a `KafkaSink<String>` with a `SimpleStringSchema` serializer and point it at a dedicated output topic such as `flink-aggregates`. Make sure the topic exists (`kafka-topics --create` if needed). Swap `print()` for `sinkTo(kafkaSink)` so the aggregated strings land in Kafka.
- **Run it**: `./gradlew runKotlinClass -PmainClass=tasks.flink._3_WriteAggregatesToKafkaKt`.
- **Verify**: consume the output topic with `docker compose exec kafka1 kafka-console-consumer --bootstrap-server kafka1:9092 --topic flink-aggregates --from-beginning` or `kcat`. You should see one record per window/key combination, e.g. `status=SHIPPED,count=42,windowStart=...`.

## Optional explorations
- Swap `SimpleStringSchema` for JSON (via your favourite serialization library) so downstream systems can parse structured output.
- Move to event-time processing by parsing the `ts` field, then experiment with allowing late events via `WatermarkStrategy.withTimestampAssigner` and `allowedLateness`.
- Try using Flink's Table API or SQL to express the same pipeline as a streaming query.

## Troubleshooting tips
- **`UnknownHostException: kafka1`**: host-side jobs must connect to `localhost:9094`; the `kafka1` hostname only resolves inside the Docker network.
- **Deserialization errors**: ensure you pick the correct schema/format—the exercises assume the seeder's comma-delimited payload. If your topic uses Avro, add the appropriate serializer/deserializer from Flink's Confluent adapter or convert the payload before sinking.
- **No results from the sink**: confirm the window actually fires (enough events arrive within each window) and that your sink builder sets topic, bootstrap servers, and serialization schema.
- **Empty input stream**: restart the seeder or point your source to a topic that has traffic, then rerun the job with `OffsetsInitializer.earliest()` so Flink replays the backlog.

Ready to go deeper? Consider deploying the job to a standalone Flink cluster or integrating Savepoints and checkpoints for production-grade reliability.
