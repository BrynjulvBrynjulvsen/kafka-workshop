package tasks.consumergroups

import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import tasks.BarebonesKafkaClients.getBareBonesConsumer
import tasks.Constants
import tasks.Constants.PARTITIONED_TOPIC
import tasks.ContinuousProducer
import tasks.repeatFor
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

// Create multiple consumers for a topic with the same consumer group id.
// How may consumers are receiving messages? What does this mean in terms of message consumption?
// Run with:
// ./gradlew runKotlinClass -PmainClass=tasks.consumergroups._1_MultiMemberConsumerGroupKt

fun main() {
    val groupId = "multi-member-group-${UUID.randomUUID()}"

    // Step 1: create several consumers that share the same group id.
    val consumers: List<KafkaConsumer<String, String>> = listOf(
        getBareBonesConsumer(groupId = groupId, offsetConfig = "earliest"),
        getBareBonesConsumer(groupId = groupId, offsetConfig = "earliest"),
        getBareBonesConsumer(groupId = groupId, offsetConfig = "earliest"),
    )
    // All members share the same group id; increase or decrease the list to experiment.

    // Step 2: subscribe them all to a multi-partition topic (created by the setup script).
    consumers.forEach { it.subscribe(listOf(PARTITIONED_TOPIC)) }

    // Helper: keeps messages flowing so every consumer has work to do.
    val producer = ContinuousProducer(PARTITIONED_TOPIC) {
        UUID.randomUUID().toString() to "Message sent at ${System.currentTimeMillis()}"
    }
    producer.resume()

    // Step 3: poll in a loop and print which consumer handled which partition.
    repeatFor(10.seconds) {
        consumers.forEachIndexed { index, consumer ->
            println("\nPolling for consumer #$index")
            // TODO: call pollAndPrintRecords(consumer)
            println("Assignments: ${consumer.assignment()}")
        }
    }

    producer.stop()

    // Step 4: seek back to the beginning to replay all partitions with the same group id.
    consumers.forEachIndexed { index, consumer ->
        println("\nReplaying from beginning for consumer #$index")
        // TODO: consumer.seekToBeginning(consumer.assignment())
        // TODO: pollAndPrintRecords(consumer)
    }

    consumers.forEach { it.close() }
}

fun pollAndPrintRecords(consumer: KafkaConsumer<String, String>) {
    // TODO: poll with a short timeout (e.g. Duration.ofMillis(500)) and print topic/partition/offset/value
    val records: ConsumerRecords<String, String>
}
