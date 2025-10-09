@file:Suppress("UNREACHABLE_CODE", "UNUSED_VARIABLE")

package tasks.flink

import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment

// Step 1/4 – Connect to Kafka and peek at the raw events.
//
// Goal: stand up the smallest possible Flink job that tails `partitioned-topic` and dumps the
// payloads to stdout. Seeing the live stream helps anchor the rest of the module.
//
// Suggested steps:
// 1. Grab a `StreamExecutionEnvironment` (done for you).
// 2. Build a Kafka source either manually or via `FlinkExerciseHelpers.kafkaSource(...)`.
// 3. Feed the source into `env.fromSource(...)` using `WatermarkStrategy.noWatermarks()`.
// 4. Call `print()` so you can inspect the incoming strings.
// 5. Finish with `env.execute("...description...")`.
//
// TODO diagram: "Flink job with a single source operator printing to the console" – a simple box
// for Kafka flowing into a Flink source box, then into a console icon helps newcomers visualise the runtime.
//
// Execute with:
// ./gradlew runKotlinClass -PmainClass=tasks.flink._1_SetupFlinkKafkaSourceKt
fun main() {
    val env = StreamExecutionEnvironment.getExecutionEnvironment()

    // TODO: create a Kafka source that reads from partitioned-topic (use a distinct group id).
    val source = FlinkExerciseHelpers.kafkaSource(groupId = "flink-workshop")

    val stream = env.fromSource(
        source,
        WatermarkStrategy.noWatermarks(),
        "partitioned-topic-source",
    )

    // TODO: print the raw payloads so you can observe the stream.
    // Tip: `stream.print()` routes each message to stdout so you can sanity-check connectivity.

    // TODO: start the job with env.execute("Inspect partitioned-topic via Flink")
    // Tip: give the execution a descriptive name so you recognise it if you open the Flink Web UI.
}
