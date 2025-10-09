package tasks.flink

import org.apache.kafka.clients.producer.ProducerRecord
import tasks.BarebonesKafkaClients
import tasks.Constants
import java.time.Instant
import kotlin.random.Random

// Produce a steady stream of workshop-friendly events into the partitioned topic so the Flink
// exercises have data even if earlier Kafka labs were skipped.
fun main() {
    val topic = Constants.PARTITIONED_TOPIC
    val producer = BarebonesKafkaClients.getBareBonesProducer()
    val random = Random(System.currentTimeMillis())
    val statuses = listOf("PLACED", "PICKED", "PACKED", "SHIPPED", "DELIVERED", "CANCELLED")
    val regions = listOf("eu-north", "eu-west", "us-east")

    Runtime.getRuntime().addShutdownHook(Thread { producer.close() })

    println("Seeding ${topic} with synthetic orders for the Flink workshop. Press Ctrl+C to stop.")

    var counter = 0
    while (true) {
        val customerId = "customer-%03d".format(random.nextInt(1, 500))
        val status = statuses.random(random)
        val region = regions.random(random)
        val amount = String.format("%.2f", random.nextDouble(15.0, 325.0))
        val timestamp = Instant.now().toString()

        val value = "customer=${customerId},status=${status},region=${region},amount=${amount},ts=${timestamp}"
        val record = ProducerRecord(topic, customerId, value)

        producer.send(record) { _, exception ->
            if (exception != null) {
                System.err.println("Failed to send sample record: ${exception.message}")
            }
        }

        counter++
        if (counter % 50 == 0) {
            println("Produced ${counter} sample records to ${topic}...")
        }

        Thread.sleep(250)
    }
}
