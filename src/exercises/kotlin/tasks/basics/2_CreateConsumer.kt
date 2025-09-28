package tasks

import tasks.BarebonesKafkaClients.getBareBonesConsumer

// 2: Consume a message from the topic "hello-world"
// Execute with:
// ./gradlew runKotlinClass -PmainClass=tasks._2_CreateConsumerKt
fun main() {
    getBareBonesConsumer(offsetConfig = "earliest", groupId = "anotherveryrandomgroup").use { consumer ->
        // Step 1: Subscribe to the topic
        // consumer.subscribe(listOf(tasks.Constants.TOPIC_NAME))

        // Step 2: Poll for records (e.g. java.time.Duration.ofSeconds(1)).
        // The poll call waits up to the given duration before returning an empty batch.
        // val records = consumer.poll(java.time.Duration.ofSeconds(1))

        // Step 3: Iterate the ConsumerRecords and print topic/partition/offset/value

        // Step 4: Commit the offset (commitSync()) so reruns skip already processed messages

        // TODO: Implement the steps above
    }
}
