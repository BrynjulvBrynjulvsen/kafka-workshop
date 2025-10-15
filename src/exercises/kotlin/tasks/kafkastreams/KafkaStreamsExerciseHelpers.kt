package tasks.kafkastreams

import java.util.Locale
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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
        put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, tasks.KafkaConfig.bootstrapServers)
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
        "${order.customerId} -> ${order.status} (${order.region}) amount=${String.format(Locale.US, "%.2f", order.amount)}"
}

private val json = Json { ignoreUnknownKeys = true }

data class WorkshopOrder(
    val customerId: String,
    val status: String,
    val region: String,
    val amount: Double,
    val timestampMillis: Long,
)

fun parseWorkshopOrder(raw: String): WorkshopOrder? {
    val jsonElement = runCatching { json.parseToJsonElement(raw).jsonObject }.getOrElse { return null }

    val status = jsonElement["status"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() } ?: return null
    val customer = jsonElement["customer"]?.jsonPrimitive?.contentOrNull ?: "unknown"
    val region = jsonElement["region"]?.jsonPrimitive?.contentOrNull ?: "unknown"
    val amount = jsonElement["amount"]?.jsonPrimitive?.doubleOrNull ?: 0.0
    val timestampMillis = jsonElement["tsMillis"]?.jsonPrimitive?.longOrNull
        ?: jsonElement["ts"]?.jsonPrimitive?.contentOrNull
            ?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() }
        ?: System.currentTimeMillis()

    return WorkshopOrder(customer, status, region, amount, timestampMillis)
}
