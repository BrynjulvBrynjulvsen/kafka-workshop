package tasks.serdes

import io.bekk.publisher.WorkshopStatusMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import tasks.BarebonesKafkaClients

// Serialization and deserialization

// Produce an Avro-serialized message to the topic "schema-using-topic".
// Then, create a consumer that consumes and deserializes messages from this topic,
// printing some of the deserialized object's values

fun main() {
    runBlocking(Dispatchers.IO) {
        launch {
            BarebonesKafkaClients.getAvroProducer<WorkshopStatusMessage>().use { producer ->
                // TODO: Implement me
            }
        }
        launch {
            BarebonesKafkaClients.getAvroConsumer<WorkshopStatusMessage>()
                .use { consumer ->
                    // TODO: Implement me
                }
        }
    }
}