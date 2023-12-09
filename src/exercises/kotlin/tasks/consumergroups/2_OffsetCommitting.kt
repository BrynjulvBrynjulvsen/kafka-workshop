package tasks

// 2: Committing offsets

// Consume the hello-world topic, starting from the first message. As you consume messages, commit offsets.
// Hint: The consumer object offers the methods commitSync and commitAsync to facilitate this.
// To test, start the consumers again using the same group. Observe that the second run does not consume previously
// consumed messages.
fun main() {
    BarebonesKafkaClients.getBareBonesConsumer(groupId = "name-task-9", offsetConfig = "earliest").use { consumer ->
        consumer.subscribe(listOf(Constants.TOPIC_NAME))
        // TODO: Implement me
    }
}