package tasks.flink.suggestedSolutions

import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import tasks.flink.FlinkExerciseHelpers

fun main() {
    val env = StreamExecutionEnvironment.getExecutionEnvironment()
    env.config.disableClosureCleaner()

    val source = FlinkExerciseHelpers.kafkaSource(groupId = "flink-workshop-parse-solution")

    env.fromSource(
        source,
        WatermarkStrategy.noWatermarks(),
        "partitioned-topic-source",
    )
        .flatMap(FlinkHelpers.orderExtractor)
        .map(FlinkHelpers.orderSummaryMapper)
        .print()

    env.execute("Parse workshop orders")
}
