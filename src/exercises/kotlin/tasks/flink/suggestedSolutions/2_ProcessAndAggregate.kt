package tasks.flink.suggestedSolutions

import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.datastream.DataStream
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import java.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

fun main() {
    val env = StreamExecutionEnvironment.getExecutionEnvironment()
    env.config.disableClosureCleaner()

    val source = FlinkHelpers.kafkaSource(
        groupId = "flink-aggregations",
        valueDeserializer = SimpleStringSchema(),
    )

    val aggregated: DataStream<String> = env.fromSource(
        source,
        WatermarkStrategy.noWatermarks(),
        "partitioned-topic-source",
    )
        .flatMap(FlinkHelpers.statusExtractor)
        .keyBy { status -> status }
        .window(TumblingProcessingTimeWindows.of(30.seconds.toJavaDuration()))
        .aggregate(FlinkHelpers.countAggregate, FlinkHelpers.windowStringifier)

    aggregated.print()

    env.execute("Windowed aggregation with Flink")
}
