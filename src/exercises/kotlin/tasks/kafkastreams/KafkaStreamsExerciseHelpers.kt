package tasks.kafkastreams

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsConfig
import tasks.Constants
import java.time.Instant
import java.util.Properties

/**
 * Shared helpers so the exercises can focus on Kafka Streams concepts instead of boilerplate.
 */
object KafkaStreamsExerciseHelpers {

    fun baseStreamsConfig(applicationId: String): Properties = Properties().apply {
        put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId)
        put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9094")
        put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().javaClass)
        put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().javaClass)
        put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.AT_LEAST_ONCE)
        put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, org.apache.kafka.streams.processor.WallclockTimestampExtractor::class.java)
        put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1_000)
        put(StreamsConfig.TOPOLOGY_OPTIMIZATION_CONFIG, StreamsConfig.OPTIMIZE)
        put(StreamsConfig.consumerPrefix("auto.offset.reset"), "latest")
    }

    val sourceTopic: String = Constants.PARTITIONED_TOPIC
    const val aggregatesTopic: String = "kstreams-aggregates"

    fun formatOrderSummary(order: WorkshopOrder): String =
        "${order.customerId} -> ${order.status} (${order.region}) amount=${String.format("%.2f", order.amount)}"
}

data class WorkshopOrder(
    val customerId: String,
    val status: String,
    val region: String,
    val amount: Double,
    val timestampMillis: Long,
)

fun parseWorkshopOrder(raw: String): WorkshopOrder? {
    val fields = raw.split(",").mapNotNull { assignment ->
        val parts = assignment.split("=", limit = 2)
        if (parts.size < 2) return@mapNotNull null
        val key = parts[0].trim()
        val value = parts[1].trim()
        key to value
    }.toMap()

    val status = fields["status"]?.takeIf { it.isNotEmpty() } ?: return null
    val customer = fields["customer"] ?: "unknown"
    val region = fields["region"] ?: "unknown"
    val amount = fields["amount"]?.toDoubleOrNull() ?: 0.0
    val timestampMillis = fields["ts"]?.let { Instant.parse(it).toEpochMilli() } ?: System.currentTimeMillis()

    return WorkshopOrder(customer, status, region, amount, timestampMillis)
}
