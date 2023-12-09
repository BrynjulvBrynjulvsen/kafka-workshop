package tasks

// 3: Create a long running producer

// Create a long-running consumer printing messages from the hello-world topic, starting at the
// latest message. While listening, produce messages to the topic and observe that
// the consumer keeps printing new messages.

fun main() {
    BarebonesKafkaClients.getBareBonesConsumer(offsetConfig = "latest").use { consumer ->
        consumer.subscribe(listOf(Constants.TOPIC_NAME))
        while (true) {
            // TODO: Implement me
        }
    }
}
