package tasks.consumergroups

import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import tasks.Constants
import java.util.*

// Create multiple consumers for a topic with the same consumer group id.
// How may consumers are receiving messages? What does this mean in terms of message consumption?

fun main() {
    val uniqueConsumerGroup = "multi-member-group-${UUID.randomUUID()}"
    val consumers: List<KafkaConsumer<String, String>> = listOf(  )

    consumers.forEach { it.subscribe(listOf(Constants.TOPIC_NAME)) }  // Join the same group, enabling partition balancing, offset handling and other Kafka consumer group features

    consumers.forEachIndexed { cIdx, consumer ->
        // TODO: Implement me
        println("\nPolling records for consumer #$cIdx..")
    }

    // Optional: Re-use an already-existing consumer-group, and read all messages
    //  Hint: Even though we started reading from offset 0, the current value will be that of the last consumed message
    //  for each partition. Therefore, we need to make the consumers read from the beginning again.
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
}
