package tasks.suggested_solutions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.producer.ProducerRecord
import tasks.BarebonesKafkaClients
import tasks.Constants
import java.time.Duration
import java.util.*
import kotlin.time.Duration.Companion.seconds

// Task_1_3

// Create a long-running consumer printing messages from the hello-world topic, starting at the
// latest message. While listening, produce messages to the topic and observe that
// the consumer keeps printing new messages.

fun main() {
    runBlocking(Dispatchers.Default) {
        launch {
            BarebonesKafkaClients.getBareBonesConsumer(offsetConfig = "latest").use { consumer ->
                consumer.subscribe(listOf(Constants.TOPIC_NAME))
                while (true) {
                    consumer.poll(Duration.ofMillis(1000))
                        .forEach { consumerRecord ->
                            println("${Date(consumerRecord.timestamp())}: Received ${consumerRecord.value()}")
                        }
                    consumer.commitSync()
                }
            }
        }
        launch {
            BarebonesKafkaClients.getBareBonesProducer().use {
                while(true) {
                    it.send(
                        ProducerRecord(
                            Constants.TOPIC_NAME,
                            "",
                            "A random number: ${Math.random()}"
                        )
                    )
                    delay(1.seconds)
                }
            }
        }
    }
}
