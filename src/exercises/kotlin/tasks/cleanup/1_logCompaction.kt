package tasks.cleanup

import tasks.BarebonesKafkaClients.getBareBonesProducer

// IMPORTANT: First perform the setup steps described in 5_deletion_policy.md

// Produce multiple messages to the hello-world-topic using the same key to a log compacted topic. Try to force log
// compaction, then consume the messages.
fun main() {
    produceMessages()
    Thread.sleep(5_000)
    readQueueFromStart()
}


fun produceMessages() {
    getBareBonesProducer().use { producer ->
        // TODO: write a series of messages with the same key
    }
}

fun readQueueFromStart() {
    // TODO: write some messages
    Thread.sleep(5_000)
    // TODO: Write another message to trigger compaction
}
