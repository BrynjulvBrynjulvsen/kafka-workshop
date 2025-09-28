package tasks

// 3: Create a long-running consumer

// Create a long-running consumer printing messages from the hello-world topic, starting at the
// latest message. While listening, produce messages to the topic and observe that
// the consumer keeps printing new messages.
// Run with:
// ./gradlew runKotlinClass -PmainClass=tasks._3_LongRunningConsumerKt
// Tip: keep a second terminal open and either run the console producer
// (`kafka-console-producer --bootstrap-server kafka1:9092 --topic hello-world`)
// or rerun your solution from 1_CreateProducer so that fresh messages arrive
// while this consumer is polling. Alternatively, produce in a separate thread/coroutine
// from the consumer.
fun main() {
    BarebonesKafkaClients.getBareBonesConsumer(offsetConfig = "latest").use { consumer ->
        consumer.subscribe(listOf(Constants.TOPIC_NAME))
        while (true) {
            // Step 1: Poll for records (short timeout) and iterate through them
            // val records = consumer.poll(java.time.Duration.ofMillis(500))
            // records.forEach { record -> println("${'$'}{record.timestamp()}: ${'$'}{record.value()}") }

            // Step 2: Commit offsets so rebalances do not replay processed records continuously
            // consumer.commitSync()

            // Step 3: Consider sleeping briefly to avoid a tight loop when no data arrives
            // Thread.sleep(200)

            // TODO: Replace the comments above with a working poll/print/commit loop
        }
    }
}
