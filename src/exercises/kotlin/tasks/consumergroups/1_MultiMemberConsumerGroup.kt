package tasks.consumergroups

import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import tasks.Constants
import tasks.doForDuration
import java.util.*
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

// Create multiple consumers for a topic with the same consumer group id.
// How may consumers are receiving messages? What does this mean in terms of message consumption?

fun main() {
    val uniqueConsumerGroup = "multi-member-group-${UUID.randomUUID()}"
    val consumers: List<KafkaConsumer<String, String>> = listOf(  )

    // Join the same group, enabling partition balancing, offset handling and other Kafka consumer group features
    consumers.forEach { it.subscribe(listOf(Constants.TOPIC_NAME)) }

    // If you do not have sufficient messages on your topic yet, try producing some first.
    // Alternatively, try using the ContinuousProducer utility found in BareBonesKafkaClients to produce in the background

    // Joining a consumer group triggers rebalancing, which may take some time. Therefore, try running this in a loop.
    // If you're interested in how rebalancing works, examine what happens if you reduce this timer
    doForDuration(10.seconds) {
        consumers.forEachIndexed { cIdx, consumer ->
            // TODO: Implement me
            println("\nPolling records for consumer #$cIdx..")
        }
    }
    // HINT: If you seem to only get messages for one consumer, check how many partitions your topic has

    // Try re-using an already-existing consumer-group, and read all messages
    // Recall that the poll done above may have committed some or all of the offsets.
    // You can use consumer.seekToBeginning(consumer.assignment()) to read from the
    // beginning of a given consumer's assigned partitions.
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
