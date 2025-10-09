@file:Suppress("UNREACHABLE_CODE", "UNUSED_VARIABLE")

package tasks.flink

import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment

// Step 2/4 – Parse the strings into something meaningful.
//
// Goal: turn the raw comma-delimited payloads into the `WorkshopOrder` data class so we can inspect
// individual fields (status, region, amount) and build intuition for the downstream aggregations.
//
// Suggested steps:
// 1. Reuse the Kafka source from step 1 (the helper does that for you).
// 2. Apply `flatMap` to drop malformed rows and emit `WorkshopOrder` objects using `parseWorkshopOrder`.
// 3. Map each order to a readable summary string (e.g. "customer-123 -> SHIPPED in eu-west") and print it.
// 4. Execute the job and confirm the output matches the seeded events.
//
// TODO diagram: "Raw event structure" – show an example payload broken into labelled fields to reinforce
// how the parsing works.
//
// Execute with:
// ./gradlew runKotlinClass -PmainClass=tasks.flink._2_ParseWorkshopOrdersKt
fun main() {
    val env = StreamExecutionEnvironment.getExecutionEnvironment()
    env.config.disableClosureCleaner()

    val source = FlinkExerciseHelpers.kafkaSource(groupId = "flink-workshop-parse")

    val orders = env.fromSource(
        source,
        WatermarkStrategy.noWatermarks(),
        "partitioned-topic-source",
    ).flatMap { raw, out ->
        // TODO: parse each record using parseWorkshopOrder and emit it if parsing succeeds.
        // Hint: guard against null and call out.collect(order) when parsing works.
    }
        .name("parse-workshop-orders")

    val summaries = orders.map { order ->
        // TODO: turn the structured order into a short descriptive string (or use FlinkExerciseHelpers.formatOrderSummary).
        // Hint: include key fields like customer, status, region so you can eyeball the result.
        TODO("format the order for display")
    }

    // TODO: print the summaries and execute the job.
    // Tip: reuse `stream.print()` plus `env.execute(...)` from step 1.
}
