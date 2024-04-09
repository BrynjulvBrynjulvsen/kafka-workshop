package tasks.suggested_solutions

import tasks.BarebonesKafkaClients
import tasks.Constants
import java.time.Duration

// Consume the hello-world topic, starting from the first message. As you consume messages, commit offsets.
// Hint: The consumer object offers the methods commitSync and commitAsync to facilitate this.

// Consider what happens if offsets are not committed. After consuming the messages, restart the consumer by re-running
// the main method, using the same consumer group. Observe that the second run does not consume previously consumed
// messages.
fun main() {
    BarebonesKafkaClients.getBareBonesConsumer(groupId = "offset-commit-group", offsetConfig = "earliest").use { consumer ->
        consumer.subscribe(listOf(Constants.TOPIC_NAME))
        while (true) {
            consumer.poll(Duration.ofMillis(5000L))
                .forEach { consumerRecord ->
                    println("Record: topic: ${consumerRecord.topic()}, offset:${consumerRecord.offset()}")
                    println("Record value: ${consumerRecord.value()}")
                }
            //consumer.commitAsync()
        }
    }
}