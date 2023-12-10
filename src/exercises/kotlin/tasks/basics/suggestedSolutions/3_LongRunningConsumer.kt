package tasks.suggested_solutions

import tasks.BarebonesKafkaClients
import tasks.Constants
import java.time.Duration
import java.util.*

// Task_7

// Create a long-running consumer printing messages from the hello-world topic, starting at the
// latest message. While listening, produce messages to the topic and observe that
// the consumer keeps printing new messages.

fun main() {
    BarebonesKafkaClients.getBareBonesConsumer(offsetConfig = "latest").use { consumer ->
        consumer.subscribe(listOf(Constants.TOPIC_NAME))
        while (true) {
            consumer.poll(Duration.ofMillis(10000L))
                .forEach { consumerRecord ->
                    println("${Date(consumerRecord.timestamp())}: Received ${consumerRecord.value()}")
                }
            consumer.commitSync()
        }
    }
}
