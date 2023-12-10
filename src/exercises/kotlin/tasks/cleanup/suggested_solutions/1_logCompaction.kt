package tasks.cleanup.suggested_solutions

import org.apache.kafka.clients.producer.ProducerRecord
import tasks.BarebonesKafkaClients
import java.time.Duration

//Log compaction


// IMPORTANT: First perform the setup steps described in 5_deletion_policy.md

// Produce multiple messages to the hello-world-topic using the same key to a log compacted topic. Try to force log
// compaction, then consume the messages.

fun main() {
    produceMessages()
    Thread.sleep(5000)
    readQueueFromStart()
}

const val topic_name = "log-compact-example"

fun readQueueFromStart() {
    BarebonesKafkaClients.getBareBonesConsumer(offsetConfig = "earliest")
        .use { consumer ->
            consumer.subscribe(listOf(topic_name))

            consumer.seekToBeginning(consumer.assignment())

            while (true) {
                val consumerRecords = consumer.poll(Duration.ofMillis(10000L))
                consumerRecords.forEach { consumerRecord ->
                    println("Partition: ${consumerRecord.partition()}, key: ${consumerRecord.key()},  value: ${consumerRecord.value()}, offset: ${consumerRecord.offset()}")
                }
            }
        }
}
fun produceMessages() {
    BarebonesKafkaClients.getBareBonesProducer().use { producer ->
        listOf("A message", "And another one.", "Yet another one", "One final message", "The very final message, promise").forEach { msg ->
            producer.send(
                ProducerRecord(
                    topic_name,
                    "my-fancy-key",
                    msg
                )
            )
        }
        Thread.sleep(5_000)
        producer.send(
            ProducerRecord(
                topic_name,
                "my-fancy-key",
                "A message to trigger cleanup by the creation of a new log segment"
            )
        )
    }
}
