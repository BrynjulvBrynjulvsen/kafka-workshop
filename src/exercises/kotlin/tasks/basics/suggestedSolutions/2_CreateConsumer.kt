package tasks.suggested_solutions

import tasks.BarebonesKafkaClients.getBareBonesConsumer
import tasks.Constants
import java.time.Duration

// Task_2

// Consume a message from the topic "hello-world"

fun main() {
    getBareBonesConsumer(offsetConfig = "earliest").use { consumer ->
        consumer.subscribe(listOf(Constants.TOPIC_NAME))
        consumer.poll(Duration.ofMillis(10000L))
        consumer.poll(Duration.ofMillis(10000L))
            .forEach { consumerRecord ->
                println("Record: topic: ${consumerRecord.topic()}, offset:${consumerRecord.offset()}")
                println("Record key: ${consumerRecord.key()}, value: ${consumerRecord.value()}")
            }
    }
}
