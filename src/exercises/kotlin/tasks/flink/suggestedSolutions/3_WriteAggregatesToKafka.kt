package tasks.flink.suggestedSolutions

import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.datastream.DataStream
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

fun main() {
    val env = StreamExecutionEnvironment.getExecutionEnvironment()
    env.config.disableClosureCleaner()

    val source = FlinkHelpers.kafkaSource(
        groupId = "flink-aggregates-sink",
        valueDeserializer = SimpleStringSchema(),
    )

    val aggregates: DataStream<String> = env.fromSource(
        source,
        WatermarkStrategy.noWatermarks(),
        "partitioned-topic-source",
    )
        .flatMap(FlinkHelpers.statusExtractor)
        .keyBy { status -> status }
        .window(TumblingProcessingTimeWindows.of(1.minutes.toJavaDuration()))
        .aggregate(FlinkHelpers.countAggregate, FlinkHelpers.windowStringifier)

    val sink = FlinkHelpers.kafkaSink(
        topic = "flink-aggregates",
        valueSerializer = SimpleStringSchema(),
    )

    aggregates.sinkTo(sink)

    env.execute("Windowed aggregation to Kafka")
}
