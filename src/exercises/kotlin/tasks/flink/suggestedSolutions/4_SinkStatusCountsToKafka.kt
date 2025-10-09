package tasks.flink.suggestedSolutions

import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time

fun main() {
    val env = StreamExecutionEnvironment.getExecutionEnvironment()
    env.config.disableClosureCleaner()

    val source = FlinkHelpers.kafkaSource(groupId = "flink-workshop-sink-solution")

    val statusCounts = env.fromSource(
        source,
        WatermarkStrategy.noWatermarks(),
        "partitioned-topic-source",
    )
        .flatMap(FlinkHelpers.orderExtractor)
        .keyBy { order -> order.status }
        .window(TumblingProcessingTimeWindows.of(Time.minutes(1)))
        .aggregate(FlinkHelpers.statusCountAggregate, FlinkHelpers.statusWindowFormatter)

    statusCounts.sinkTo(FlinkHelpers.kafkaSink("flink-aggregates"))

    env.execute("Publish status counts to Kafka")
}
