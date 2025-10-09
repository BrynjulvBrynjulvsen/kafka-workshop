# Kafka Consumer Groups

> Coordinate multiple consumers, understand offsets, and reset them when needed.

## Learning goals
- See how a consumer group divides partitions among members so work can be parallelized.
- Practice committing offsets in code and from the CLI.
- Inspect, describe, and reset consumer group state using `kafka-consumer-groups`.

## Before you start
- Complete the producers/consumers exercise and ensure `hello-world` and `partitioned-topic` exist.
- Keep the Kafka stack running (`docker compose up`) and have some messages available to consume.

## Why consumer groups matter
- **Parallel consumption**: Kafka assigns partitions in a topic to consumers within the same group so multiple processes can work on the same data set simultaneously. For single-partition topics this means only one member will receive data until you add more partitions (covered in [exercise 3](3_partitions_and_ordering.md)).
- **Progress tracking**: Kafka stores offsets per topic-partition _and_ per consumer group so a stopped consumer can resume later.

Keep those two responsibilities in mind as you work through the exercises.

## Hands-on path

### 1. Practice committing offsets intentionally
- Kotlin scaffolding: [`src/exercises/kotlin/tasks/consumergroups/2_OffsetCommitting.kt`](../src/exercises/kotlin/tasks/consumergroups/2_OffsetCommitting.kt).
- **What to implement**: subscribe to `Constants.TOPIC_NAME`, poll in a loop, print each record, and call `commitSync()` (or `commitAsync()`) once you finish a batch. Break out when the poll returns no records so the program exits cleanly.
- **Why commit?**: committing writes the latest processed offset to Kafka’s internal `__consumer_offsets` topic so reruns (or other group members) resume where you stopped.
- **Commit choices**: `commitSync()` blocks until the broker confirms the write (safer, slower); `commitAsync()` returns immediately (faster, but you may want error handling if the commit fails).
- **Verify**: Run `./gradlew runKotlinClass -PmainClass=tasks.consumergroups._2_OffsetCommittingKt`. After it processes the backlog, rerun the class without producing new records - no messages should print the second time. Optionally confirm the stored offset via `kafka-consumer-groups --describe --group offset-commit-group`.

### 2. Explore group assignments with code
- Kotlin scaffolding: [`src/exercises/kotlin/tasks/consumergroups/1_MultiMemberConsumerGroup.kt`](../src/exercises/kotlin/tasks/consumergroups/1_MultiMemberConsumerGroup.kt).
- **What to implement**: create at least three consumers sharing the same `group.id`, subscribe them to `Constants.PARTITIONED_TOPIC`, poll, and print partition assignments. Use `ContinuousProducer` (from `tasks.ContinuousProducer`) to keep data flowing, for example:
  ```kotlin
  val producer = ContinuousProducer(Constants.PARTITIONED_TOPIC) { "key" to "value" }
  producer.resume()
  ```
- **Execution model**: the provided scaffold spins up every consumer within the same `main` function - run the Gradle task once and let it log in the foreground while you observe.
- **What to watch**: Only one consumer receives data per partition. If your topic has a single partition, the other consumers remain idle - try `partitioned-topic` which has multiple partitions.
- **Verify**: Run `./gradlew runKotlinClass -PmainClass=tasks.consumergroups._1_MultiMemberConsumerGroupKt` and, in parallel, execute `docker compose exec kafka1 kafka-consumer-groups --bootstrap-server kafka1:9092 --describe --group <your-group>` to see partition assignments and lag.

## Console drills with `kafka-consumer-groups`

### Populate data and commit an offset
If you need fresh messages, reuse the solution from the producer exercise or run:

```bash
docker compose exec kafka1 kafka-console-producer --bootstrap-server kafka1:9092 --topic hello-world
```

Then commit offsets for a group using the CLI so the `kafka-consumer-groups` tool has something to report:

```bash
kafka-console-consumer --bootstrap-server kafka1:9092 \
  --from-beginning --topic hello-world --group my-group
```

Let it process a few messages, then stop with `Ctrl+C`.

### List and describe groups
1. **List groups**
   ```bash
   kafka-consumer-groups --bootstrap-server kafka1:9092 --list
   ```
   _Checkpoint_: Your code-created group IDs (for example, `multi-member-group-*`) should appear alongside `console-consumer-...` groups.

2. **Describe a specific group**
   ```bash
   kafka-consumer-groups --bootstrap-server kafka1:9092 --describe --group my-group
   ```
   _What you learn_: for each topic-partition assignment you will see the current committed offset, the latest offset on the broker (log end offset), and the lag between them. The table also shows which client instance holds the assignment (`consumer-id`, `host`, `client-id`).

> _Note_: this command lists partitions _per group_. Kafka does not provide a quick inverse lookup (topics to groups) without additional scripting.

### Reset offsets hands-on
> Make sure no consumers for that group are running; otherwise the reset command will fail.

1. **Reset to a specific offset** (reuse the group ID you populated above, e.g. `my-group`)
   ```bash
   kafka-consumer-groups --bootstrap-server kafka1:9092 \
     --topic hello-world:0 --group my-group \
     --reset-offsets --to-offset 1 --execute
   ```
   Run your consumer again and notice previously read messages reappear.

2. **Reset by timestamp**
   ```bash
   kafka-consumer-groups --bootstrap-server kafka1:9092 \
     --topic hello-world --group my-group \
     --reset-offsets --to-datetime 2023-12-10T11:50:00.000 --execute
   ```
   Adjust the timestamp to one between two message batches and confirm only the later batch is replayed.

### Troubleshooting tips
- **Lag never decreases**: confirm your consumers are committing offsets; otherwise the group will appear perpetually behind. Remember that the barebones consumer disables auto-commit.
- **Reset command complains about active members**: stop your code/CLI consumers or wait for them to time out (`session.timeout.ms`).
- **Describe output shows `-` for consumer-id**: the group has no active members - start a consumer and watch the table refresh.

## Ready for more?
Once you understand how partitions get balanced across a group, jump to [partitions and ordering](3_partitions_and_ordering.md) to control how keys influence that balancing.
