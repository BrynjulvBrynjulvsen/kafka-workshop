package tasks.suggested_solutions

import io.bekk.publisher.WorkshopStatusMessage
import tasks.BarebonesKafkaClients
import tasks.Constants
import java.time.Duration

// Task_6

// Create a listener that consumes messages from the topic "workshop-status-message"
// Use/inspect deserialised Avro-schema object-model
// Optional: Read more about Avro-schema DeSer
fun main() {
    BarebonesKafkaClients.getAvroConsumer<WorkshopStatusMessage>()
        .use { consumer ->
            consumer.subscribe(listOf(Constants.AVRO_TOPIC_NAME))
            while (true) {
                val records = consumer.poll(Duration.ofMillis(3000L))
                println("Records:")
                records.forEach {
                    println("Record value: ${it.value()}")
                }
                consumer.commitAsync()
            }
        }
}
