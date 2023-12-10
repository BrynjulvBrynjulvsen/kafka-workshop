package tasks.suggested_solutions

import io.bekk.publisher.WorkshopStatusMessage
import org.apache.kafka.clients.producer.ProducerRecord
import tasks.BarebonesKafkaClients
import tasks.Constants
import java.util.*

// Task_5

// Produce an Avro-serialized message to the topic "workshop-status-message"

fun main() {

    BarebonesKafkaClients.getAvroProducer<WorkshopStatusMessage>().use { producer ->
        producer.send(
            ProducerRecord(
                Constants.AVRO_TOPIC_NAME,
                UUID.randomUUID().toString(),
                 WorkshopStatusMessage("Currently at a #KafkaWorkshop 🎉 held by @Bekk 🕴")
            )
        )
    }
}