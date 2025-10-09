@file:Suppress("UNREACHABLE_CODE", "UNUSED_VARIABLE")

package tasks.flink

import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time

// Step 4/4 – Share the aggregates by pushing them back to Kafka.
//
// Goal: reuse the status-counting pipeline and deliver the results to a new Kafka topic (`flink-aggregates`).
// This mirrors how downstream teams or dashboards would consume the metrics.
//
// Suggested steps:
// 1. Recreate the stream from step 3 (or extract it into a helper if you prefer).
// 2. Build a `KafkaSink<String>` via `FlinkExerciseHelpers.kafkaSink("flink-aggregates")`.
// 3. Replace `print()` with `sinkTo(...)`.
// 4. Tail the new topic with `kcat`/`kafka-console-consumer` to validate the output.
//
// TODO diagram: "End-to-end pipeline" – Kafka orders -> Flink job -> Kafka aggregates, with status counts illustrated.
//
// Execute with:
// ./gradlew runKotlinClass -PmainClass=tasks.flink._4_SinkStatusCountsToKafkaKt
fun main() {
    val env = StreamExecutionEnvironment.getExecutionEnvironment()
    env.config.disableClosureCleaner()

    val source = FlinkExerciseHelpers.kafkaSource(groupId = "flink-workshop-sink")
/*
    val statusCounts = env.fromSource(
        source,
        WatermarkStrategy.noWatermarks(),
        "partitioned-topic-source",
    ).flatMap { raw, out ->
        // TODO: parse each record into WorkshopOrder (borrow your solution from step 2).
        // Hint: feel free to extract a helper so you do not duplicate parsing logic again.
    }
        // TODO: plug in the 1-minute tumbling window pipeline you built in step 3 (keyBy -> window -> aggregate).
        // Return strings so the downstream sink can write plain text to Kafka.

    val sink = FlinkExerciseHelpers.kafkaSink(
        topic = "flink-aggregates",
        valueSerializer = SimpleStringSchema(),
    )

 */

    // TODO: send the aggregated results to Kafka and execute the job.
    // Tip: after calling env.execute(...), tail `flink-aggregates` to confirm messages land there.
}
