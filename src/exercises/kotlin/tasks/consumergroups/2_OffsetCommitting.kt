package tasks.consumergroups

import tasks.BarebonesKafkaClients
import tasks.Constants

// 2: Committing offsets

// Consume the hello-world topic, starting from the first message. As you consume messages, commit offsets.
// Hint: The consumer object offers the methods commitSync and commitAsync to facilitate this.

// After consuming the messages, restart the consumer using the same group. Observe that the second run does not consume previously
// consumed messages.
fun main() {
    BarebonesKafkaClients.getBareBonesConsumer(groupId = "offset-commit-group", offsetConfig = "earliest")
        .use { consumer ->
        consumer.subscribe(listOf(Constants.TOPIC_NAME))
        // TODO: Implement me
    }
}