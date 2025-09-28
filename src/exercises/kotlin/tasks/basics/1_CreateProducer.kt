package tasks

import org.apache.kafka.clients.producer.ProducerRecord
import tasks.BarebonesKafkaClients.getBareBonesProducer

// 1: Produce a message to the topic "hello-world"
// Try running the file with:
// ./gradlew runKotlinClass -PmainClass=tasks._1_CreateProducerKt
fun main() {
    getBareBonesProducer().use { producer ->
        // Step 1: Create a ProducerRecord with a key and value for Constants.TOPIC_NAME
        // val record = ProducerRecord(Constants.TOPIC_NAME, "my-key", "Hello Kafka")

        // Step 2: Send the record using producer.send(record)

        // Step 3 (optional but helpful while learning): call producer.flush() to force the send

        // TODO: Replace the comments above with working code that sends at least one message
    }
}
