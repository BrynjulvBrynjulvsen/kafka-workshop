# Kafka Streams

> Build a lightweight streaming application that enriches Kafka topics using the Kafka Streams DSL.

## Learning goals
- Configure a standalone Kafka Streams topology in Kotlin.
- Transform raw topic payloads into structured domain events.
- Aggregate events with tumbling time windows and publish the results to another topic.

## Before you start
- Ensure the Kafka stack is running: `docker compose up`.
- Seed topics if needed: `./exercise_setup/create_topics.sh`.
- Keep the optional Flink/Kafka tooling running if you already started it; Streams can coexist just fine.
- Need data? Reuse the Flink seeder: `./gradlew runKotlinClass -PmainClass=tasks.flink._0_SeedFlinkWorkshopDataKt` to produce synthetic JSON orders (e.g. `{"customer":"customer-042","status":"SHIPPED","region":"eu-west","amount":88.40,"ts":"2024-05-23T12:34:56Z","tsMillis":1716467696000}`) into `partitioned-topic`.

## Why Kafka Streams?
Kafka Streams is a lightweight library for building streaming microservices directly on top of Kafka. The API lets you describe dataflows with builders, stream processors, and stateful aggregations. Compared with Flink, Streams runs in-process with your application (no external cluster) and trades some of Flink's advanced runtime features for a lighter operational footprint. This module mirrors the Flink track so participants can compare ergonomics and concepts across the two frameworks:
1. Connect a Kafka Streams topology and observe the raw data.
2. Parse values into a structured order model.
3. Window and count statuses.
4. Publish results to a downstream topic.

Shared helpers live under [`src/exercises/kotlin/tasks/kafkastreams/KafkaStreamsExerciseHelpers.kt`](../src/exercises/kotlin/tasks/kafkastreams/KafkaStreamsExerciseHelpers.kt). They keep the focus on the Streams DSL rather than configuration noise.

### Operational reflections
- **Kafka Streams**: runs embedded inside your service. You are responsible for provisioning the containers (often StatefulSets), sizing local state stores (RocksDB + changelog topics), and monitoring rebalance lag/resets. Kafka itself becomes the state backend through changelog and repartition topics.
- **Apache Flink**: executes in a dedicated cluster with Job/TaskManagers, checkpoint storage, and built-in backpressure handling. State is centrally managed and recoverable via checkpoints/savepoints, but the cluster becomes another piece of infrastructure to operate.

Use these differences to discuss when to pick Streams (tight coupling to an existing service, minimal extra infra) versus Flink (shared compute layer, richer runtime tooling, more operational separation).

### Quick API crib sheet
- **DSL overview**: the [Kafka Streams Developer Guide](https://kafka.apache.org/documentation/streams/developer-guide/dsl-api.html) introduces `StreamsBuilder`, `KStream`, and `KTable` operations.
- **Windowing**: tumbling and hopping windows are described in the [Time semantics documentation](https://kafka.apache.org/documentation/streams/developer-guide/dsl-api.html#windowing). We will use `TimeWindows.ofSizeWithNoGrace` in step 3.
- **Topology lifecycle**: check the [KafkaStreams javadoc](https://kafka.apache.org/37/javadoc/org/apache/kafka/streams/KafkaStreams.html) for start/close patterns and shutdown hooks.

## Hands-on path

### 1. Bootstrap a Streams topology
- Kotlin scaffold: [`src/exercises/kotlin/tasks/kafkastreams/1_BootstrapKafkaStreams.kt`](../src/exercises/kotlin/tasks/kafkastreams/1_BootstrapKafkaStreams.kt).
- **What to implement**: build a `StreamsBuilder`, consume `partitioned-topic`, log every record, and start the topology using the provided helper config.
- **Run it**: `./gradlew runKotlinClass -PmainClass=tasks.kafkastreams._1_BootstrapKafkaStreamsKt`.
- **Verify**: watch keys/values print to stdout. Stop with `Ctrl+C` and ensure the shutdown hook closes the app.

### 2. Parse workshop orders
- Kotlin scaffold: [`src/exercises/kotlin/tasks/kafkastreams/2_ParseWorkshopOrders.kt`](../src/exercises/kotlin/tasks/kafkastreams/2_ParseWorkshopOrders.kt).
- **What to implement**: use `parseWorkshopOrder` to convert the JSON payloads into `WorkshopOrder`, filter out nulls, map to readable summary strings, and log them.
- **Run it**: `./gradlew runKotlinClass -PmainClass=tasks.kafkastreams._2_ParseWorkshopOrdersKt`.
- **Verify**: summaries should mirror the structure used in the Flink module (customer -> status, region, amount).

### 3. Count statuses with tumbling windows
- Kotlin scaffold: [`src/exercises/kotlin/tasks/kafkastreams/3_CountStatusWindows.kt`](../src/exercises/kotlin/tasks/kafkastreams/3_CountStatusWindows.kt).
- **What to implement**: reuse the parsing flow, key by order status, apply a 30-second `TimeWindows.ofSizeWithNoGrace`, and call `.count()` to produce counts. Convert the results into strings and print them (keep the windowed key so you can tell windows apart).
- **Run it**: `./gradlew runKotlinClass -PmainClass=tasks.kafkastreams._3_CountStatusWindowsKt`.
- **Verify**: expect a new line roughly every 30 seconds with window start/end and the count for each status.

### 4. Sink the aggregates back to Kafka
- Kotlin scaffold: [`src/exercises/kotlin/tasks/kafkastreams/4_SinkStatusCounts.kt`](../src/exercises/kotlin/tasks/kafkastreams/4_SinkStatusCounts.kt).
- **What to implement**: adapt the step 3 topology so the formatted counts are written to `kstreams-aggregates` using `to()`. Remember to keep a windowed key (e.g., via `WindowedSerdes.timeWindowedSerdeFrom(...)`) so compaction does not collapse different windows.
- **Run it**: `./gradlew runKotlinClass -PmainClass=tasks.kafkastreams._4_SinkStatusCountsKt`.
- **Verify**: consume the topic with `docker compose exec kafka1 kafka-console-consumer --bootstrap-server kafka1:9092 --topic kstreams-aggregates --from-beginning` (or `kcat`).

## Optional explorations
- Compare processing time (default) vs. event time by adding a custom timestamp extractor that uses the `tsMillis` field.
- Write the aggregates as JSON using your serializer of choice for friendlier downstream consumption.
- Add a branching step to split orders by region and produce separate aggregates per geography.

## Troubleshooting tips
- **Repeated counts on restart**: Kafka Streams replays records unless you reset offsets. Use a unique application id per exercise run to avoid reusing state.
- **`UnknownTopicOrPartitionException`**: ensure `kstreams-aggregates` exists or enable auto topic creation (the helper sink enables it by default).
- **No output**: confirm the seeder/producer is running and check the application id in `baseStreamsConfig`—each run should have a distinct id to avoid reusing state.

Ready to compare frameworks? Try implementing the same pipeline in Flink and Kafka Streams side by side and note differences in API surface, deployment model, and operational levers.
