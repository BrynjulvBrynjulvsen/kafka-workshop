# Kafka Partitions and Ordering

> Use multiple partitions to scale consumers and preserve ordering for related messages.

## Learning goals
- Understand how partitions enable parallel consumption within a consumer group.
- See how message keys influence partition selection and ordering guarantees.
- Practice coordinating multiple consumer groups and using keys from Kotlin.

## Before you start
- Ensure the `partitioned-topic` exists (`./exercise_setup/create_topics.sh` creates it).
- Have a few terminals ready: you’ll observe both CLI commands and Kotlin output.

## Concept refresh
- Kafka guarantees ordering _within_ a partition; adding partitions increases throughput but requires careful key usage.
- When a consumer joins or leaves a group, Kafka rebalances assignments - expect a short pause while this happens.

## Guided exercise

### 1. Observe partition balancing live
1. Produce keyed records with the console producer:
   ```bash
   kafka-console-producer --bootstrap-server kafka1:9092 \
     --topic partitioned-topic --property "parse.key=true" --property "key.separator=:"
   ```
   Enter messages like `team-alpha:Message 1` and `team-beta:Message 1`.

2. Start multiple console consumers using the same group ID:
   ```bash
   kafka-console-consumer --bootstrap-server kafka1:9092 --topic partitioned-topic --group demo-group
   ```
   Launch this in two or three terminals. Watch how each instance prints a subset of messages once rebalancing completes.

   _Checkpoint_: Run `kafka-consumer-groups --bootstrap-server kafka1:9092 --describe --group demo-group` and confirm each partition is assigned to exactly one consumer.

### 2. Implement the Kotlin ordering exercise
- Kotlin scaffolding: [`src/exercises/kotlin/tasks/partitions/1_keys_and_ordering.kt`](../src/exercises/kotlin/tasks/partitions/1_keys_and_ordering.kt).
- **What to implement**:
  - Spin up three consumer groups with at least two members each using the provided helper classes or plain consumers.
  - Subscribe them to `Constants.PARTITIONED_TOPIC` and print `partition`, `key`, and `value` so you can see ordering per key.
  - Produce two or more keyed series (for example, keys `invoice-123` and `invoice-456`) and ensure each series stays in order.
- **Verify**:
  - Run `./gradlew runKotlinClass -PmainClass=tasks.partitions._1_keys_and_orderingKt`.
  - In parallel, use the CLI describe command to ensure the groups are in `Stable` state and partitions are split.
  - Observe that within each key, offsets increase sequentially even though different consumers may handle different partitions.

### 3. Optional: Inspect rebalancing behaviour
- Start the Kotlin class, immediately run `kafka-consumer-groups --bootstrap-server kafka1:9092 --describe --group <group>` and notice the state flips from `Rebalancing` to `Stable` once each member confirms assignments.
- Reduce the delay in your code’s polling loop to see how quickly rebalances complete.

## Troubleshooting tips
- **Messages appear out of order**: confirm you used consistent keys; without a key, Kafka hashes the serialized value which scatters related records.
- **Only one consumer receives data**: check the topic’s partition count - parallelism requires as many partitions as active consumers.
- **Consumer stops receiving**: rebalancing may still be in progress. Allow a few seconds, or print logs when `pauseRendezvous`/`resumeRendezvous` fire if using helpers.

## What’s next?
With partition behaviour under your belt, move on to [schemas and serdes](4_schemas_and_serdes.md) to serialize richer payloads safely.
