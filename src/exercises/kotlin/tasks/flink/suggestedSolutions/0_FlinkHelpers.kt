package tasks.flink.suggestedSolutions

import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.api.common.functions.FlatMapFunction
import org.apache.flink.api.common.functions.MapFunction
import java.io.Serializable
import java.util.Locale
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema
import org.apache.flink.connector.kafka.sink.KafkaSink
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector
import tasks.Constants
import java.time.Instant
import java.util.Properties

object FlinkHelpers {

    fun kafkaSource(
        groupId: String,
        //You can set this to earliest to get immediate results - the first window will then include historical data on the topic
        offsets: OffsetsInitializer = OffsetsInitializer.latest(),
        valueDeserializer: DeserializationSchema<String> = org.apache.flink.api.common.serialization.SimpleStringSchema(),
    ): KafkaSource<String> =
        KafkaSource.builder<String>()
            .setBootstrapServers("localhost:9094")
            .setTopics(Constants.PARTITIONED_TOPIC)
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
            .setKafkaProducerConfig(
                // Not something to do in production, but makes the workshop less complicated
                Properties().apply { setProperty("allow.auto.create.topics", "true") }
            )
            .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
            .build()

    val orderExtractor: FlatMapFunction<String, WorkshopOrder> = WorkshopOrderExtractor
    val statusCountAggregate: AggregateFunction<WorkshopOrder, Long, Long> = WorkshopStatusCountAggregate
    val statusWindowFormatter: ProcessWindowFunction<Long, String, String, TimeWindow> = WorkshopStatusWindowFormatter
    val orderSummaryMapper: MapFunction<WorkshopOrder, String> = WorkshopOrderSummaryMapper
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

private val json = Json { ignoreUnknownKeys = true }

fun parseWorkshopOrder(raw: String): WorkshopOrder? {
    val jsonObject = runCatching { json.parseToJsonElement(raw).jsonObject }.getOrElse { return null }

    val status = jsonObject["status"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() } ?: return null
    val customer = jsonObject["customer"]?.jsonPrimitive?.contentOrNull ?: "unknown"
    val region = jsonObject["region"]?.jsonPrimitive?.contentOrNull ?: "unknown"
    val amount = jsonObject["amount"]?.jsonPrimitive?.doubleOrNull ?: 0.0
    val timestampMillis = jsonObject["tsMillis"]?.jsonPrimitive?.longOrNull
        ?: jsonObject["ts"]?.jsonPrimitive?.contentOrNull
            ?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() }
        ?: System.currentTimeMillis()

    return WorkshopOrder(customer, status, region, amount, timestampMillis)
}

object WorkshopOrderExtractor : FlatMapFunction<String, WorkshopOrder> {
    override fun flatMap(value: String, out: Collector<WorkshopOrder>) {
        parseWorkshopOrder(value)?.let(out::collect)
    }
}

object WorkshopStatusCountAggregate : AggregateFunction<WorkshopOrder, Long, Long> {
    override fun createAccumulator(): Long = 0L
    override fun add(value: WorkshopOrder, accumulator: Long): Long = accumulator + 1
    override fun getResult(accumulator: Long): Long = accumulator
    override fun merge(a: Long, b: Long): Long = a + b
}

object WorkshopOrderSummaryMapper : MapFunction<WorkshopOrder, String> {
    override fun map(value: WorkshopOrder): String =
        "${value.customerId} -> ${value.status} (${value.region}) amount=${String.format(Locale.US, "%.2f", value.amount)}"
}

object WorkshopStatusWindowFormatter : ProcessWindowFunction<Long, String, String, TimeWindow>() {
    override fun process(
        key: String,
        context: Context,
        elements: Iterable<Long>,
        out: Collector<String>,
    ) {
        val count = elements.firstOrNull() ?: 0L
        val windowStart = Instant.ofEpochMilli(context.window().start)
        val windowEnd = Instant.ofEpochMilli(context.window().end)
        out.collect("status=$key,count=$count,windowStart=$windowStart,windowEnd=$windowEnd")
    }
}
