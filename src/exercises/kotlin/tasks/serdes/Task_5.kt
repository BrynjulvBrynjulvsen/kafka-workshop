package tasks.serdes

import io.bekk.publisher.WorkshopStatusMessage
import tasks.BarebonesKafkaClients

// Task_5

// Produce an Avro-serialized message to the topic "workshop-status-message"

fun main() {

    BarebonesKafkaClients.getAvroProducer<WorkshopStatusMessage>().use { producer ->
        // TODO: Implement me
//        val avroSerialisedValue: WorkshopStatusMessage
//
//        producer.send(
//            ProducerRecord(
//                Constants.AVRO_TOPIC_NAME,
//                UUID.randomUUID().toString(),  // what happens if you produce several messages using the same key?
//                avroSerialisedValue
//            )
//        )
    }
}