# Kafka Producers and Consumers

> Build intuition for how Kafka sends and receives messages before touching more advanced features.

## Learning goals
- Produce and consume messages with Kafka CLI tools.
- Understand why consumers must subscribe, poll, and commit offsets.
- Run the starter Kotlin producer/consumer exercises.

## Prerequisites
- Local stack running (`docker compose up`) and topics created via `./exercise_setup/create_topics.sh`.
- Console tools available by attaching to the broker container:
  ```bash
  docker compose exec kafka1 /bin/bash
  ```
  Stay inside that shell for the commands below so hostnames such as `kafka1:9092` resolve correctly.

## Console tools walkthrough

### What you'll do
1. Launch the Kafka console producer and publish a few greetings.
2. Launch the Kafka console consumer and read those greetings.
3. Observe live consumption by producing from a second terminal.

### Commands and checkpoints
1. **Produce some messages**
   ```bash
   kafka-console-producer --bootstrap-server kafka1:9092 --topic hello-world
   ```
   Type a few lines (press `Enter` after each). Remember to reference the broker as `kafka1`, not `localhost`, because you are running inside the container.

   _Checkpoint_: Exit the producer (`Ctrl+C`) and run `docker compose exec kafka1 kafka-topics --bootstrap-server kafka1:9092 --describe --topic hello-world` to confirm the topic exists.

2. **Consume everything from the start**
   ```bash
   kafka-console-consumer --bootstrap-server kafka1:9092 --from-beginning --topic hello-world
   ```
   If you omit `--from-beginning`, the consumer starts at the latest offset and shows nothing for historical data—this is the first gotcha many new users hit.

   _Checkpoint_: You should see the messages you produced. Use `Ctrl+C` to stop.

3. **Watch live traffic**
   - Keep the consumer running. After it catches up, it will continue tailing new data even if you started it with `--from-beginning`.
   - In another terminal (or the host shell), run the console producer again and send new messages.

   _Troubleshooting_: If the consumer stops receiving data, verify the topic name and that the original consumer did not exit due to a typo (look for errors in its terminal).

### How the consumer loop works (concept recap)
1. Client polls Kafka for the next batch of messages (`poll`).
2. Broker returns records.
3. Client processes them.
4. Client commits the offsets (writes them to the `__consumer_offsets` topic).
5. Client polls again.

You will mirror the same subscribe → poll → commit flow in the Kotlin exercises.

## Kotlin code exercises

Starter clients live in [`src/exercises/kotlin/tasks/BarebonesKafkaClients.kt`](../src/exercises/kotlin/tasks/BarebonesKafkaClients.kt). They already configure bootstrap servers, schema registry, and common serializers.

| Exercise | File | Hint | Verify |
| - | - | - | - |
| Basic producer | [`basics/1_CreateProducer.kt`](../src/exercises/kotlin/tasks/basics/1_CreateProducer.kt) | Call `producer.send(ProducerRecord(Constants.TOPIC_NAME, "your-message"))` inside the provided `use` block. | Run `./gradlew runKotlinClass -PmainClass=tasks._1_CreateProducerKt` (adjust the class path if needed) and consume the topic with the console consumer to confirm the write. |
| Basic consumer | [`basics/2_CreateConsumer.kt`](../src/exercises/kotlin/tasks/basics/2_CreateConsumer.kt) | Follow the pattern `subscribe(listOf(Constants.TOPIC_NAME))` → `poll(Duration.ofSeconds(1))` (waits up to one second) → process records → `commitSync()`. Auto-commit is disabled in the helper to make this explicit. | Produce a message, run the class, and ensure it prints the record. Rerun without new messages to confirm the committed offsets prevent duplicates. |
| Long-running consumer | [`basics/3_LongRunningConsumer.kt`](../src/exercises/kotlin/tasks/basics/3_LongRunningConsumer.kt) | Keep the subscribe → poll → commit loop inside `while (true)` and log keys/values so you see live updates. | Let it run while you publish messages via the console producer; new records should appear immediately. |

Need inspiration or to check your work? Suggested solutions sit under `src/exercises/kotlin/tasks/basics/suggestedSolutions/`—peek only after trying.

## Next steps
When you can reliably produce and consume data—both via CLI and code—you’re ready to explore how consumer groups coordinate work in [exercise 2](2_kafka-consumer-groups.md).
