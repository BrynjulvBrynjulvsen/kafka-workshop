package tasks.consumergroups.suggestedSolutions

import org.apache.kafka.clients.consumer.KafkaConsumer
import tasks.BarebonesKafkaClients.getBareBonesConsumer
import tasks.Constants
import tasks.ContinuousProducer
import tasks.doForDuration
import java.time.Duration
import java.util.*
import kotlin.concurrent.timer
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource


// Create multiple consumers for a topic with the same consumer group id.
//
fun main() {
    val uniqueConsumerGroup = "multi-member-group-${UUID.randomUUID()}"
    val consumers = listOf(
        getBareBonesConsumer(groupId = uniqueConsumerGroup, offsetConfig = "earliest"),
        getBareBonesConsumer(groupId = uniqueConsumerGroup, offsetConfig = "earliest"),
        getBareBonesConsumer(groupId = uniqueConsumerGroup, offsetConfig = "earliest")
    )

    consumers.forEach { it.subscribe(listOf(Constants.PARTITIONED_TOPIC)) }  // Join the same group, enabling partition balancing, offset handling and other Kafka consumer group features



    val continuousProducer = ContinuousProducer(Constants.PARTITIONED_TOPIC)
    continuousProducer.resume()

    doForDuration(10.seconds) {
        consumers.forEachIndexed { cIdx, consumer ->
            println("\nPolling records for consumer #$cIdx..")
            pollAndPrintRecords(consumer)
            println(consumer.assignment())
        }
    }

    continuousProducer.stop()


    // Try re-using an already-existing consumer-group, and read all messages
    // Recall that the poll done above may have committed some or all of the offsets.
    // You can use consumer.seekToBeginning(consumer.assignment()) to read from the
    // beginning of a given consumer's assigned partitions.

    consumers.forEachIndexed { cIdx, consumer ->
        println("\nPolling records for consumer #$cIdx..")
        consumer.seekToBeginning(consumer.assignment())
        pollAndPrintRecords(consumer)
        println(consumer.assignment())

    }

    consumers.forEach { it.close() }
}

fun pollAndPrintRecords(consumer: KafkaConsumer<String, String>) {
    val consumerRecords = consumer.poll(Duration.ofMillis(500L))
    println("==================== Consumer records ====================")
    consumerRecords.forEach { record ->
        println("Record: topic: ${record.topic()}, partition: ${record.partition()}, offset: ${record.offset()}")
        println("Record value: ${record.value()}")
    }
}
