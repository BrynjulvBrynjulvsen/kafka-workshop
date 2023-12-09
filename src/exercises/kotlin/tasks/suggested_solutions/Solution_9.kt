package tasks.suggested_solutions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import tasks.BarebonesKafkaClients
import tasks.Constants
import java.time.Duration

// Task_9

// Create a set of three consumer groups listening to a topic. With all three consumer groups
// running, produce two series of messages that should be consumed in order. Ensure that each
// series of messages are consumed in the proper order by each consumer group.

fun main() {

    runBlocking(Dispatchers.Default) {
        val myGroup = "task-9-group"
        val consumers = listOf(
            BarebonesKafkaClients.getBareBonesConsumer(groupId = "$myGroup-1", offsetConfig = "latest"),
            BarebonesKafkaClients.getBareBonesConsumer(groupId = "$myGroup-2", offsetConfig = "latest"),
            BarebonesKafkaClients.getBareBonesConsumer(groupId = "$myGroup-3", offsetConfig = "latest")
        ).onEach { it.subscribe(listOf(Constants.PARTITIONED_TOPIC)) }

        launch {
            println("Producing...")
            BarebonesKafkaClients.getBareBonesProducer().use { producer ->
                val values = listOf("First", "Second", "Third")
                values.forEach { producer.produceMessage(key = "first-set", value = it) }
                values.forEach { producer.produceMessage(key = "second-set", value = it) }
            }
        }
        consumers.forEachIndexed { consumerNumber, consumer ->
            launch {
                while (true) {
                    println("Polling for $consumerNumber")
                    consumer.poll(Duration.ofMillis(1000L)).forEach { record ->
                        println("$consumerNumber received ${record.key()}:${record.value()}")
                    }
                    consumer.commitSync()
                    delay(100)
                }
            }
        }
    }

}

fun KafkaProducer<String, String>.produceMessage(key: String, value: String) {
    send(
        ProducerRecord(
            Constants.PARTITIONED_TOPIC,
            key,
            value
        )
    )
}


