@file:Suppress("UNREACHABLE_CODE", "UNUSED_VARIABLE")

package tasks.flink

import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time

// Step 3/4 – Count order statuses with tumbling windows.
//
// Goal: group the parsed orders by status (PLACED, SHIPPED, …) and calculate a count per 30-second window.
// Printing the aggregates first keeps the feedback loop tight before we wire up any sinks.
//
// Suggested steps:
// 1. Parse the incoming strings into `WorkshopOrder` objects (reuse your step 2 logic).
// 2. `keyBy { it.status }`, apply a 30-second tumbling processing-time window, and count the records per window.
// 3. Format the result into human-readable strings that include the status, count, and window boundaries.
// 4. Print the aggregates and execute the job.
//
// TODO diagram: "Window timeline" – illustrate a 30-second tumbling window with example order statuses landing in each bucket.
//
// Execute with:
// ./gradlew runKotlinClass -PmainClass=tasks.flink._3_CountStatusWindowsKt
fun main() {
    val env = StreamExecutionEnvironment.getExecutionEnvironment()
    env.config.disableClosureCleaner()

    val source = FlinkExerciseHelpers.kafkaSource(groupId = "flink-workshop-counts")
/*
    val orders = env.fromSource(
        source,
        WatermarkStrategy.noWatermarks(),
        "partitioned-topic-source",
    ).flatMap { raw, out ->
        // TODO: reuse parseWorkshopOrder to turn raw strings into WorkshopOrder objects.
        // Hint: this should look identical to the parsing logic from step 2.
    }


 */
/*
    val statusCounts = orders
        // TODO: key by status, window for 30 seconds, and emit a descriptive string per window.
        // Suggested flow: keyBy -> window(TumblingProcessingTimeWindows.of(Time.seconds(30))) -> aggregate/process.
        // Remember to include window start/end in your output so the timing is obvious.
*/
    // TODO: print the aggregated strings and execute the job.
    // Tip: expect the first window to fire ~30 seconds after starting if you read only new data.
}
