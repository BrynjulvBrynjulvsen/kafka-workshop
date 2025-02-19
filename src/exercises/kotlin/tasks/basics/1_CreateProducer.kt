package tasks

import org.apache.kafka.clients.producer.ProducerRecord
import tasks.BarebonesKafkaClients.getBareBonesProducer

// 1: Produce a message to the topic "hello-world"
fun main() {
    getBareBonesProducer().use { producer ->
        // Send a message here
    }
}
