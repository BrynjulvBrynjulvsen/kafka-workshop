@file:Suppress("UNREACHABLE_CODE")

package tasks.partitions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.KafkaConsumer
import tasks.BarebonesKafkaClients

// Task_9

// Create a set of three consumers groups with at least two members each, and listen to a topic with multiple partitions.
// With all three consumer groups running, produce two series of messages. Ensure that each message belonging to each
// series is consumed in order by each consumer group.

// Note that some time might pass before consumers begin receiving messages. This happens because Kafka needs to finish
// rebalancing for all these new consumers you've created.
// Bonus task: Whilst waiting for messages to arrive, inspect the consumer group using the kafka-consumer-groups tool and
// verify that it is in a rebalancing state.

fun main() {
   runBlocking(Dispatchers.IO) {
        val consumers: List<KafkaConsumer<String, String>>  = TODO("implement me")
       consumers.forEach { consumer ->
           launch {
               while(true) {
                   //TODO: Consume and print records. To make it easier to see which is which, consider including keys and partition numbers.
                   delay(100)
               }
           }
       }

       launch {
           print("Producing...")
           BarebonesKafkaClients.getBareBonesProducer().use { producer ->
               // TODO: Produce some messages to be consumed in order
           }
       }

    }
}

