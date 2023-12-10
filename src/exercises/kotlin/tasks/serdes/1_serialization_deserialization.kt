package tasks.serdes

import io.bekk.publisher.WorkshopStatusMessage
import tasks.BarebonesKafkaClients

// Serialization and deserialization

// Produce an Avro-serialized message to the topic "schema-using-topice".
// Then, create a consumer that consumes and deserializes messages from this topic,
// printing some of the deserialized object's values

fun main() {

    BarebonesKafkaClients.getAvroProducer<WorkshopStatusMessage>().use { producer ->
        // TODO: Implement me
    }

    BarebonesKafkaClients.getAvroConsumer<WorkshopStatusMessage>()
        .use { consumer ->
            // TODO: Implement me
        }
}