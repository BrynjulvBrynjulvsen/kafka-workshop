package tasks.consumergroups

import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import tasks.Constants
import java.util.*

//  Create multiple consumers for a topic with the same consumer group id.
//  What partitions are a consumer assigned? What does this mean in terms of message consumption?
//  Notice that they're each assigned a sub-set of the partitions for the topic, and that this allows for
//  horizontal scalability not only broker/server- but also client-side.
//  When consuming messages, make sure you commit your offsets. Consider what happens if this is not done.

fun main() {
    val uniqueConsumerGroup = "Fmulti-member-group-${UUID.randomUUID()}"
    val consumers: List<KafkaConsumer<String, String>> = listOf(  )

    consumers.forEach { it.subscribe(listOf(Constants.TOPIC_NAME)) }  // Join the same group, enabling partition balancing, offset handling and other Kafka consumer group features

    consumers.forEachIndexed { cIdx, consumer ->
        // TODO: Implement me
        println("\nPolling records for consumer #$cIdx..")
    }

    // Optional: Re-use an already-existing consumer-group, and read all messages from the beginning again
    // Explore the seek methods provided by the consumer-object. You'll need to explicitly seek to the beginning
    // for the consumer's assigned partitions. See consumer.assignment() to get these.
    consumers.forEachIndexed { cIdx, consumer ->
        // TODO: Implement me
        println("\nSeeking to the beginning of the queue, i.e. the first offsets #$cIdx..")
        println("\nPolling records for consumer #$cIdx..")
    }

    consumers.forEach { it.close() }
}

fun pollAndPrintRecords(consumer: KafkaConsumer<String, String>) {
    // TODO: Implement me
    val consumerRecords: ConsumerRecords<String, String>
    // consumerRecords.forEach { record -> }
    // What happens if we do not commit our latest read offsets?
}
