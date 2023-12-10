package tasks

import tasks.BarebonesKafkaClients.getBareBonesProducer

//class Task_3


// Produce multiple messages to the hello-world-topic using the same key.
//  What happens when you try to consume these messages?
//  Note: If you see all produced messages under the same key, log compaction will not have run yet. This might be due
//  to the scheduled job not having been run, or due to the topic configs.
//  Google and read about "Kafka log compaction" if you'd like to know more.
fun main() {
    produceMessages()
    readQueueFromStart()
}


fun produceMessages() {
    val latest = ".. and greatest!"
    getBareBonesProducer().use { producer ->
        // TODO: Implement me
    }
}

fun readQueueFromStart() {
    val earliest = "bird."
    val latest = ".. and greatest!"
    // TODO: Implement me
}
