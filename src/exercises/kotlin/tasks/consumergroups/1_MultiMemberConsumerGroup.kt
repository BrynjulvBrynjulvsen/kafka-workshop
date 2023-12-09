package tasks

import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.util.*

//  Create multiple consumers for a topic with the same consumer group id.
//  What partitions are a consumer assigned? What does this mean in terms of message consumption?
//  Notice that they're each assigned a sub-set of the partitions for the topic, and that this allows for
//  horizontal scalability not only broker/server- but also client-side.
//  When consuming messages, make sure you commit your offsets. Consider what happens if this is not done.

fun main() {
    val uniqueConsumerGroup = "quick-readers-association-${UUID.randomUUID()}"
    val consumers: List<KafkaConsumer<String, String>> = listOf(  )

    consumers.forEach { it.subscribe(listOf(Constants.TOPIC_NAME)) }  // Join the same group, enabling partition balancing, offset handling and other Kafka consumer group features

    consumers.forEachIndexed { cIdx, consumer ->
        // TODO: Implement me
        println("\nPolling records for consumer #$cIdx..")
    }

    // Optional: Re-use an already-existing consumer-group, such as "quick-readers-association", and read all messages
    //  Hint: Even though we started reading from offset 0, the current value will be that of the last consumed message
    //      for each partition..
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
