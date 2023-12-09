package tasks

// Task_9

// Create a set of three consumers listening to a topic. With all three consumers
// running, produce two series of messages. Ensure that each messages belonging to each
// series are consumed in order.

fun main() {
    class ConsumerThread : Thread() {
        val myGroup = "task-9-group"
//        val consumers  // TODO: Implement me

        override fun run() {
            while (true) {
//                consumers.forEachIndexed { consumerNumber, consumer ->
                    // TODO: Implement me
//                }
            }
        }
    }

    ConsumerThread().start()
    Thread.sleep(5000)
    print("Producing...")
    BarebonesKafkaClients.getBareBonesProducer().use { producer ->
        // TODO: Implement me
    }
}
