package tasks.suggested_solutions

import org.apache.kafka.clients.producer.ProducerRecord
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
    BarebonesKafkaClients.getBareBonesConsumer(offsetConfig = "earliest")
        .use { consumer ->
            consumer.subscribe(listOf(Constants.TOPIC_NAME))

            consumer.seekToBeginning(consumer.assignment())

            while (true) {
                val consumerRecords = consumer.poll(Duration.ofMillis(10000L))
                consumerRecords.forEach { consumerRecord ->
                    println("Record: topic: ${consumerRecord.topic()}, offset:${consumerRecord.offset()}")
                    println("Record value: ${consumerRecord.value()}")
                }
            }
        }
}
fun produceMessages() {
    BarebonesKafkaClients.getBareBonesProducer().use { producer ->
        listOf("A message", "And another one.", "Yet another one", "One final message", latest).forEach { msg ->
            producer.send(
                ProducerRecord(
                    "hello-world",
                    null,
                    msg
                )
            )
        }
    }
}
