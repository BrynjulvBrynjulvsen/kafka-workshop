package io.bekk.tasks

import io.bekk.repository.getBareBonesConsumer
import java.time.Duration
import java.util.*

class Task_4

// Create multiple consumers for a topic with the same consumer group id.
//  What partitions are a consumer assigned? What does this mean in terms of message consumption? (Parallelisability 🎉)
//  Notice that they're each assigned a sub-set of the partitions for the topic, and that this allows for
//  horizontal scalability not only broker/server- but also client-side.
fun main() {
    val uniqueConsumerGroup = "quick-readers-association-${UUID.randomUUID()}"
    val consumer1 = getBareBonesConsumer(groupId = uniqueConsumerGroup, offsetConfig = "earliest")
    val consumer2 = getBareBonesConsumer(groupId = uniqueConsumerGroup, offsetConfig = "earliest")
    val consumer3 = getBareBonesConsumer(groupId = uniqueConsumerGroup, offsetConfig = "earliest")
    val consumers = listOf(consumer1, consumer2, consumer3)

    consumers.forEach { it.subscribe(listOf(topicName)) }  // partition-balancing

    consumers.forEachIndexed{ cIdx, consumer ->
        println("\nPolling records for consumer #$cIdx..")
        val consumerRecords = consumer.poll(Duration.ofMillis(500))
        println("==================== Consumer records ====================")
        consumerRecords.forEach { consumerRecord ->
            println("Record: topic: ${consumerRecord.topic()}, offset:${consumerRecord.offset()}")
            println("Record value: ${consumerRecord.value()}")
        }
    }

    // Optional: Re-use an already-existing consumer-group, such as "quick-readers-association", and read all messages
    //  Hint: Even though we started reading from offset 0, the current value will be that of the last consumed message
    //      for each partition..
    consumers.forEachIndexed{ cIdx, consumer ->
        println("\nPolling records for consumer #$cIdx..")
        consumer.seekToBeginning(consumer.assignment())
        val consumerRecords = consumer.poll(Duration.ofMillis(500))
        println("==================== Consumer records ====================")
        consumerRecords.forEach { consumerRecord ->
            println("Record: topic: ${consumerRecord.topic()}, offset:${consumerRecord.offset()}")
            println("Record value: ${consumerRecord.value()}")
        }
    }
}
