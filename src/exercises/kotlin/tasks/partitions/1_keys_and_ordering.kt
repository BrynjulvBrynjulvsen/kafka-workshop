@file:Suppress("UNREACHABLE_CODE")

package tasks.partitions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import tasks.BarebonesKafkaClients
import tasks.BasicContinuousConsumer
import tasks.Constants

// Ordering and keys

// Create a set of three consumer groups with at least two members each, and listen to a topic with multiple partitions.
// With all three consumer groups running, produce two series of messages. Use keys to ensure that each message
// belonging to each series is consumed in order by each consumer group.
// Notice that they're each assigned a sub-set of the partitions for the topic, and that this allows for
// horizontal scalability not only broker/server- but also client-side.

// Note that some time might pass before consumers begin receiving messages. This happens because Kafka needs to finish
// rebalancing for all these new consumers you've created.
// Bonus task: Whilst waiting for messages to arrive, inspect the consumer group using the kafka-consumer-groups tool
// and verify that it is in a rebalancing state.

// Run with:
// ./gradlew runKotlinClass -PmainClass=tasks.partitions._1_keys_and_orderingKt
fun main() {
    runBlocking(Dispatchers.IO) {
        val groupPrefix = "keys-demo"

        // Step 1: create several consumer groups (two members each) on the partitioned topic.
        val consumers: List<BasicContinuousConsumer> = listOf(
            // BasicContinuousConsumer(groupId = "${groupPrefix}-1", topicName = Constants.PARTITIONED_TOPIC) { record, consumer ->
            //     println("${consumer.groupMetadata().groupId()} partition ${record.partition()} key ${record.key()} value ${record.value()}")
            // },
            // BasicContinuousConsumer(groupId = "${groupPrefix}-1", topicName = Constants.PARTITIONED_TOPIC) { record, consumer ->
            //     println("${consumer.groupMetadata().groupId()} partition ${record.partition()} key ${record.key()} value ${record.value()}")
            // },
            // TODO: add two members for group ${groupPrefix}-2 and ${groupPrefix}-3
        )

        // Step 2: allow rebalancing to finish and then produce keyed series.
        delay(5_000)
        println("Producing keyed sequences...")
        BarebonesKafkaClients.getBareBonesProducer().use { producer ->
            val series = listOf(
                "invoice-123" to listOf("Created", "Approved", "Paid"),
                "invoice-456" to listOf("Created", "Held", "Resolved"),
            )
            // TODO: For each (key, events) pair send events in order using ProducerRecord(Constants.PARTITIONED_TOPIC, key, value)
            // series.forEach { (key, events) -> events.forEach { value -> producer.send(ProducerRecord(Constants.PARTITIONED_TOPIC, key, value)) } }
            // producer.flush()
        }

        // Step 3: Observe each consumer printing partitions and keys; optionally inspect assignments via kafka-consumer-groups --describe.

        delay(1_000)
        consumers.forEach { it.close() }
    }
}
