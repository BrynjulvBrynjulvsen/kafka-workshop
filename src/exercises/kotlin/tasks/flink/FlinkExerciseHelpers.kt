package tasks.flink

import java.io.Serializable
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema
import org.apache.flink.connector.kafka.sink.KafkaSink
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import java.time.Instant

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
            .setBootstrapServers("localhost:9094")
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
            .setBootstrapServers("localhost:9094")
            .setRecordSerializer(
                KafkaRecordSerializationSchema.builder<String>()
                    .setTopic(topic)
                    .setValueSerializationSchema(valueSerializer)
                    .build(),
            )
            .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
            .build()

    fun formatOrderSummary(order: WorkshopOrder): String =
        "${order.customerId} -> ${order.status} (${order.region}) amount=${String.format("%.2f", order.amount)}"
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
