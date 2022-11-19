package tasks.suggested_solutions

import tasks.BarebonesKafkaClients
import tasks.Constants
import java.time.Duration

//class Task_3
val latest = ".. and greatest!"

// Produce multiple messages to the hello-world-topic using the same key.
//  What happens when you try to consume these messages?
//  Note: If you see all produced messages under the same key, log compaction will not have run yet. This might be due
//  to the scheduled job not having been run, or due to the topic configs.
//  Google and read about "Kafka log compaction" if you'd like to know more.
fun main() {
    produceMessages()
    readQueueFromStart()
}

fun readQueueFromStart() {
    BarebonesKafkaClients.getBareBonesConsumer(offsetConfig = "earliest").use { consumer ->
        consumer.subscribe(listOf(Constants.TOPIC_NAME))
        val consumerRecords = consumer.poll(Duration.ofMillis(500L))
        consumerRecords.forEach { consumerRecord ->
            println("Record: topic: ${consumerRecord.topic()}, offset:${consumerRecord.offset()}")
            println("Record value: ${consumerRecord.value()}")
        }
        consumer.commitSync()

        // iff log compaction has been run:
        assert(consumerRecords.toList().size == 1)
        assert(consumerRecords.last().value() == latest)
    }
}