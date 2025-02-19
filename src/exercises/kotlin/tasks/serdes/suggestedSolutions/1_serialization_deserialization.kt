package tasks.serdes.suggestedSolutions

import io.bekk.publisher.WorkshopStatusMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.producer.ProducerRecord
import tasks.BarebonesKafkaClients
import tasks.Constants
import java.time.Duration
import java.util.*

// Serialization and deserialization

// Produce an Avro-serialized message to the topic "schema-using-topice".
// Then, create a consumer that consumes and deserializes messages from this topic,
// printing some of the deserialized object's values
fun main() {

    runBlocking(Dispatchers.IO) {
        launch {
            BarebonesKafkaClients.getAvroProducer<WorkshopStatusMessage>().use { producer ->
                while (true) {
                    producer.send(
                        ProducerRecord(
                            Constants.AVRO_TOPIC_NAME,
                            UUID.randomUUID().toString(),
                            WorkshopStatusMessage("A wonderful Avro message!")
                        )
                    )
                    delay(500)
                }
            }
        }

        launch {
            BarebonesKafkaClients.getAvroConsumer<WorkshopStatusMessage>()
                .use { consumer ->
                    consumer.subscribe(listOf(Constants.AVRO_TOPIC_NAME))
                    while (true) {
                        val records = consumer.poll(Duration.ofMillis(1000L))
                        records.forEach {
                            println("Record value: ${it.value()} at ${it.offset()}:${it.partition()}}")
                        }
                        consumer.commitAsync()
                    }
                }
        }
    }

}