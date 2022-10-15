package io.bekk.tasks

import io.bekk.repository.getBareBonesConsumer

class Task_4

// Create multiple consumers for a topic with the same consumer group id.
//  What partitions are a consumer assigned? What does this mean in terms of message consumption? (Parallelisability 🎉)
//  Notice that they're each assigned a sub-set of the partitions for the topic, and that this allows for
//  horizontal scalability not only broker/server- but also client-side.
fun main() {
    val consumer1 = getBareBonesConsumer(groupId = "quick-readers-association")
    val consumer2 = getBareBonesConsumer(groupId = "quick-readers-association")
    val consumer3 = getBareBonesConsumer(groupId = "quick-readers-association")
    val consumers = listOf(consumer1, consumer2, consumer3)
    consumers.forEach { it.subscribe(listOf(topicName)) }

}
