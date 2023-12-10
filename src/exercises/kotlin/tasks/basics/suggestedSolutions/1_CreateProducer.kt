package tasks.suggested_solutions

import org.apache.kafka.clients.producer.ProducerRecord
import tasks.BarebonesKafkaClients.getBareBonesProducer
import tasks.Constants

// Task_1

// Produce a message to the topic "hello-world"
fun main() {
    getBareBonesProducer().use { producer ->
        producer.send(
            ProducerRecord(
                Constants.TOPIC_NAME,
                "my-key",
                "My first message!"
            )
        )

    }
}
