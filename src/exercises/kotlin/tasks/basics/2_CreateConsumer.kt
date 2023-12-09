package tasks

import tasks.BarebonesKafkaClients.getBareBonesConsumer

// 2: Consume a message from the topic "hello-world"

fun main() {
    getBareBonesConsumer(offsetConfig = "earliest", groupId = "anotherveryrandomgroup").use { consumer ->
        // TODO: Implement me
    }
}
