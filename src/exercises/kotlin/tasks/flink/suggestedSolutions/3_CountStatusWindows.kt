package tasks.flink.suggestedSolutions

import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

fun main() {
    val env = StreamExecutionEnvironment.getExecutionEnvironment()
    env.config.disableClosureCleaner()

    val source = FlinkHelpers.kafkaSource(groupId = "flink-workshop-counts-solution")

    env.fromSource(
        source,
        WatermarkStrategy.noWatermarks(),
        "partitioned-topic-source",
    )
        .flatMap(FlinkHelpers.orderExtractor)
        .keyBy { order -> order.status }
        .window(TumblingProcessingTimeWindows.of(30.seconds.toJavaDuration()))
        .aggregate(FlinkHelpers.statusCountAggregate, FlinkHelpers.statusWindowFormatter)
        .print()

    env.execute("Count statuses with tumbling windows")
}
