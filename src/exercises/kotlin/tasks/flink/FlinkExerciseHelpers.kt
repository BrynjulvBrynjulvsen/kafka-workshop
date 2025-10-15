package tasks.flink

import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema
import org.apache.flink.connector.kafka.sink.KafkaSink
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import java.time.Instant
import java.io.Serializable
import java.util.Locale
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

private val json = Json { ignoreUnknownKeys = true }

/**
 * Convenience helpers that keep the exercises focused on Flink concepts rather than connector boilerplate.
 */
object FlinkExerciseHelpers {

    fun kafkaSource(
        groupId: String,
        offsets: OffsetsInitializer = OffsetsInitializer.earliest(),
        valueDeserializer: DeserializationSchema<String> = org.apache.flink.api.common.serialization.SimpleStringSchema(),
    ): KafkaSource<String> =
        KafkaSource.builder<String>()
            .setBootstrapServers(tasks.KafkaConfig.bootstrapServers)
            .setTopics(tasks.Constants.PARTITIONED_TOPIC)
            .setGroupId(groupId)
            .setStartingOffsets(offsets)
            .setValueOnlyDeserializer(valueDeserializer)
            .build()

    fun kafkaSink(
        topic: String,
        valueSerializer: SerializationSchema<String> = org.apache.flink.api.common.serialization.SimpleStringSchema(),
    ): KafkaSink<String> =
        KafkaSink.builder<String>()
            .setBootstrapServers(tasks.KafkaConfig.bootstrapServers)
            .setRecordSerializer(
                KafkaRecordSerializationSchema.builder<String>()
                    .setTopic(topic)
                    .setValueSerializationSchema(valueSerializer)
                    .build(),
            )
            .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
            .build()

    fun formatOrderSummary(order: WorkshopOrder): String =
        "${order.customerId} -> ${order.status} (${order.region}) amount=${String.format(Locale.US, "%.2f", order.amount)}"
}

class WorkshopOrder() : Serializable {
    var customerId: String = ""
    var status: String = ""
    var region: String = ""
    var amount: Double = 0.0
    var timestampMillis: Long = 0L

    constructor(
        customerId: String,
        status: String,
        region: String,
        amount: Double,
        timestampMillis: Long,
    ) : this() {
        this.customerId = customerId
        this.status = status
        this.region = region
        this.amount = amount
        this.timestampMillis = timestampMillis
    }
}

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
