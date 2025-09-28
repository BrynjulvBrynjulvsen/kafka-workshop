package tasks.consumergroups

import tasks.BarebonesKafkaClients
import tasks.Constants
import java.time.Duration

// 2: Committing offsets

// Consume the hello-world topic, starting from the first message. As you consume messages, commit offsets.
// Hint: The consumer object offers the methods commitSync and commitAsync to facilitate this.

// Consider what happens if offsets are not committed. After consuming the messages, restart the consumer by re-running
// the main method, using the same consumer group. Observe that the second run does not consume previously consumed
// messages.
// Run with:
// ./gradlew runKotlinClass -PmainClass=tasks.consumergroups._2_OffsetCommittingKt
fun main() {
    BarebonesKafkaClients.getBareBonesConsumer(groupId = "offset-commit-group", offsetConfig = "earliest")
        .use { consumer ->
        consumer.subscribe(listOf(Constants.TOPIC_NAME))

        while (true) {
            // Step 1: poll for records (e.g. Duration.ofSeconds(1)). Break out when the batch is empty.
            // val records = consumer.poll(Duration.ofSeconds(1))
            // if (records.isEmpty) break

            // Step 2: print topic, partition, offset, and value for each record.

            // Step 3: commit the processed offsets (commitSync() or commitAsync()) before the next poll.

            // TODO: Replace the steps above with working code.
        }
    }
}
