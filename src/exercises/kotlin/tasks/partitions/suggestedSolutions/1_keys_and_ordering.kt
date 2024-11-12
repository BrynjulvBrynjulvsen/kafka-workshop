package tasks.partitions.suggestedSolutions

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import tasks.BarebonesKafkaClients
import tasks.BasicContinuousConsumer
import tasks.Constants

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
    val myGroup = "task-9-group"

    val consumers = listOf("1", "2", "3").map {
        BasicContinuousConsumer(
            groupId = "$myGroup-$it",
            topicName = Constants.PARTITIONED_TOPIC,
            offsetResetConfig = "latest"
        ) { record, consumer ->
            println(
                "Group: ${
                    consumer.groupMetadata().groupId()
                } Partition: ${record.partition()} Offset: ${record.offset()} Key: ${record.key()} Value: ${record.value()}"
            )
        }
    }

    Thread.sleep(5000L)
    println("Producing...")
    BarebonesKafkaClients.getBareBonesProducer().use { producer ->
        val values = listOf("First", "Second", "Third")
        values.forEach { producer.produceMessage(key = "first-set", value = it) }
        values.forEach { producer.produceMessage(key = "second-set", value = it) }
        values.forEach { producer.produceMessage(key = "third-set", value = it) }
        values.forEach { producer.produceMessage(key = "fourth-set", value = it) }
        producer.flush()
    }
    println("Done producing")
    println("Waiting")
    Thread.sleep(1000L)
    // Not closing means dead consumers Kafka will have to wait for upon next rebalance
    consumers.forEach { it.close() }
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


