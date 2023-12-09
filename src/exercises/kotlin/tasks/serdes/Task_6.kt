package tasks

import io.bekk.publisher.WorkshopStatusMessage

// Task_6

// Create a listener that consumes messages from the topic "workshop-status-message"
// Use/inspect deserialised Avro-schema object-model
// Optional: Read more about Avro-schema DeSer
fun main() {
    BarebonesKafkaClients.getAvroConsumer<WorkshopStatusMessage>("my-group-id")
        .use { consumer ->
            // TODO: Implement me
        }
}
