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

// Create a set of three consumer groups with at least two members each, and listen to a topic with multiple partitions.
// With all three consumer groups running, produce two series of messages. Use keys to ensure that each message
// belonging to each series is consumed in order by each consumer group.
// Notice that they're each assigned a sub-set of the partitions for the topic, and that this allows for
// horizontal scalability not only broker/server- but also client-side.

// Note that some time might pass before consumers begin receiving messages. This happens because Kafka needs to finish
// rebalancing for all these new consumers you've created.
// Bonus task: Whilst waiting for messages to arrive, inspect the consumer group using the kafka-consumer-groups tool
// and verify that it is in a rebalancing state.

fun main() {

    runBlocking(Dispatchers.IO) {
        val myGroup = "task-9-group"
        listOf(
            BarebonesKafkaClients.getBareBonesConsumer(groupId = "$myGroup-1", offsetConfig = "latest"),
            BarebonesKafkaClients.getBareBonesConsumer(groupId = "$myGroup-1", offsetConfig = "latest"),
            BarebonesKafkaClients.getBareBonesConsumer(groupId = "$myGroup-2", offsetConfig = "latest"),
            BarebonesKafkaClients.getBareBonesConsumer(groupId = "$myGroup-2", offsetConfig = "latest"),
            BarebonesKafkaClients.getBareBonesConsumer(groupId = "$myGroup-3", offsetConfig = "latest"),
            BarebonesKafkaClients.getBareBonesConsumer(groupId = "$myGroup-3", offsetConfig = "latest")
        ).forEach {  consumer ->
            launch {
                consumer.subscribe(listOf(Constants.PARTITIONED_TOPIC))
                while (true) {
                    consumer.poll(Duration.ofMillis(100L)).forEach { record ->
                        println("Group: ${consumer.groupMetadata().groupId()} Partition: ${record.partition()} Key: ${record.key()} Value: ${record.value()}")
                    }
                    consumer.commitSync()
                    delay(5)
                }
            }
        }

        launch {
            delay(1000)
            println("Producing...")
            BarebonesKafkaClients.getBareBonesProducer().use { producer ->
                val values = listOf("First", "Second", "Third")
                values.forEach { producer.produceMessage(key = "first-set", value = it) }
                values.forEach { producer.produceMessage(key = "second-set", value = it) }
                values.forEach { producer.produceMessage(key = "third-set", value = it) }
                values.forEach { producer.produceMessage(key = "fourth-set", value = it) }
            }
            println("Done producing")
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


