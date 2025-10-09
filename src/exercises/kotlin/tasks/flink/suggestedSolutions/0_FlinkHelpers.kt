package tasks.flink.suggestedSolutions

import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.api.common.functions.FlatMapFunction
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

object FlinkHelpers {

    fun kafkaSource(
        groupId: String,
        offsetsInitializer: OffsetsInitializer = OffsetsInitializer.earliest(),
        valueDeserializer: DeserializationSchema<String> = org.apache.flink.api.common.serialization.SimpleStringSchema(),
    ): KafkaSource<String> =
        KafkaSource.builder<String>()
            .setBootstrapServers("localhost:9094")
            .setTopics(Constants.PARTITIONED_TOPIC)
            .setGroupId(groupId)
            .setStartingOffsets(offsetsInitializer)
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

    val statusExtractor: FlatMapFunction<String, String> = StatusExtractor
    val countAggregate: AggregateFunction<String, Long, Long> = StatusCountAggregate
    val windowStringifier: ProcessWindowFunction<Long, String, String, TimeWindow> = StatusWindowStringifier

}

// This line may show a warning about missing readResolve. That function is a convention relied upon by native Java serialization, but Flink typically does not make use of it.
object StatusExtractor : FlatMapFunction<String, String> {
    override fun flatMap(value: String, out: Collector<String>) {
        extractStatus(value)?.let(out::collect)
    }
}

object StatusCountAggregate : AggregateFunction<String, Long, Long> {
    override fun createAccumulator(): Long = 0L
    override fun add(value: String, accumulator: Long): Long = accumulator + 1
    override fun getResult(accumulator: Long): Long = accumulator
    override fun merge(a: Long, b: Long): Long = a + b
}

object StatusWindowStringifier : ProcessWindowFunction<Long, String, String, TimeWindow>() {
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

fun extractStatus(raw: String): String? {
    val prefix = "status="
    val start = raw.indexOf(prefix)
    if (start == -1) return null
    val afterPrefix = start + prefix.length
    val end = raw.indexOf(',', afterPrefix).let { if (it == -1) raw.length else it }
    if (afterPrefix >= end) return null
    return raw.substring(afterPrefix, end).trim().takeIf { it.isNotEmpty() }
}
