package tasks.suggested_solutions

import org.apache.kafka.clients.consumer.KafkaConsumer
import tasks.BarebonesKafkaClients.getBareBonesConsumer
import tasks.Constants
import java.time.Duration
import java.util.*

// Task_4

// Create multiple consumers for a topic with the same consumer group id.
//  What partitions are a consumer assigned? What does this mean in terms of message consumption?
//  Notice that they're each assigned a sub-set of the partitions for the topic, and that this allows for
//  horizontal scalability not only broker/server- but also client-side.
//  When consuming messages, make sure you commit your offsets. Consider what happens if this is not done.
fun main() {
    val uniqueConsumerGroup = "quick-readers-association-${UUID.randomUUID()}"
    val consumers = listOf(
        getBareBonesConsumer(groupId = uniqueConsumerGroup, offsetConfig = "earliest"),
        getBareBonesConsumer(groupId = uniqueConsumerGroup, offsetConfig = "earliest"),
        getBareBonesConsumer(groupId = uniqueConsumerGroup, offsetConfig = "earliest")
    )

    consumers.forEach { it.subscribe(listOf(Constants.TOPIC_NAME)) }  // Join the same group, enabling partition balancing, offset handling and other Kafka consumer group features
    consumers.forEachIndexed { cIdx, consumer ->
        println("\nPolling records for consumer #$cIdx..")

        pollAndPrintRecords(consumer)
        println(consumer.assignment())

    }

    // Optional: Re-use an already-existing consumer-group, such as "quick-readers-association", and read all messages
    //  Hint: Even though we started reading from offset 0, the current value will be that of the last consumed message
    //      for each partition..
    consumers.forEachIndexed { cIdx, consumer ->
        println("\nPolling records for consumer #$cIdx..")
        consumer.seekToBeginning(consumer.assignment())
        pollAndPrintRecords(consumer)
        println(consumer.assignment())

    }

    consumers.forEach { it.close() }
}

fun pollAndPrintRecords(consumer: KafkaConsumer<String, String>) {
    val consumerRecords = consumer.poll(Duration.ofMillis(10000L))
    println("==================== Consumer records ====================")
    consumerRecords.forEach { record ->
        println("Record: topic: ${record.topic()}, partition: ${record.partition()}, offset: ${record.offset()}")
        println("Record value: ${record.value()}")
    }
    consumer.commitSync()
}
