# Cleanup Policies

> Learn how Kafka reclaims disk space through retention limits and log compaction, then experiment with a toy compacted topic.

## Learning goals
- Understand the difference between `delete` and `compact` cleanup policies.
- Configure a log-compacted topic and observe how tombstones or newer records remove stale data.
- Exercise caution: recognise why these settings are for demos only, not production.

## Before you start
- Kafka stack running and CLI access (`docker compose exec kafka1 /bin/bash`).
- Familiarity with producing keyed messages from earlier exercises.

## Cleanup policy recap
- **Delete**: removes records older than `retention.ms` (per-partition and per-segment). Deletion happens asynchronously once a log segment becomes eligible.
- **Compact**: keeps only the latest value for each key. Writing a `null` value (tombstone) eventually removes the key entirely.

## Guided compacted-topic exercise

### 1. Create the demo topic with aggressive compaction settings
```bash
kafka-topics --create --topic log-compact-example \
  --partitions 6 --replication-factor 1 --bootstrap-server kafka1:9092 \
  --config cleanup.policy=compact

kafka-configs --alter --entity-type topics --entity-name log-compact-example \
  --add-config 'max.compaction.lag.ms=100,min.cleanable.dirty.ratio=0.0,segment.ms=100,delete.retention.ms=100' \
  --bootstrap-server kafka1:9092
```
> These values force compaction to run quickly for demonstration purposes, at the cost of terrible performance—do not copy them to real clusters.

### 2. Produce and compact in Kotlin
- File: [`src/exercises/kotlin/tasks/cleanup/1_logCompaction.kt`](../src/exercises/kotlin/tasks/cleanup/1_logCompaction.kt).
- **What to implement**:
  - In `produceMessages()`, send several values with the same key and add a short `Thread.sleep` to give compaction time.
  - In `readQueueFromStart()`, create a consumer with `offsetConfig = "earliest"`, call `seekToBeginning`, and print records so you can compare before/after compaction. After the initial read, send one extra message with the same key to trigger a new segment and allow compaction to complete.
- **Verify**:
  - Run `./gradlew runKotlinClass -PmainClass=tasks.cleanup._1_logCompactionKt`.
  - You should see many messages produced, then—after waiting—only the latest value (or two) when reading from the consumer. Re-run to confirm older values no longer appear.

### 3. Observe from the CLI (optional)
- Consume from the start with the console consumer and note that only late messages remain:
  ```bash
  kafka-console-consumer --bootstrap-server kafka1:9092 --from-beginning --topic log-compact-example
  ```
- Post a tombstone by sending a null value to remove the key entirely:
  ```bash
  echo "my-fancy-key:" | kafka-console-producer --bootstrap-server kafka1:9092 \
    --topic log-compact-example --property "parse.key=true" --property "key.separator=:"
  ```
  Wait a few seconds and re-consume—the key should disappear after compaction.

## Troubleshooting tips
- **Compaction seems to do nothing**: ensure the extra message after the waiting period is produced; compaction checks only run when new segments roll.
- **Console consumer shows old data**: allow more time—compaction is asynchronous. You can also reduce the wait times even further in the topic config if necessary.
- **`Topic with this name already exists`**: delete the topic with `kafka-topics --delete --topic log-compact-example ...` or pick a new topic name; recreating lets you rerun the experiment from scratch.

## What’s next?
You now understand why Kafka cleanup policies support performance and storage management. Continue to optional connectors or revisit earlier modules to solidify concepts.
