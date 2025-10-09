@file:Suppress("UNREACHABLE_CODE", "UNUSED_VARIABLE")

package tasks.flink

import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import tasks.Constants
import java.time.Duration

// Transform Kafka events with Flink and calculate rolling counts per key.
//
// Goal: derive aggregates from the stream produced in exercise 1 by keying the events and
// applying a tumbling window. Print the windowed counts to stdout or plug in a custom sink.
//
// Suggested steps:
// 1. Start from the same Kafka source configuration as before (copy the builder or refactor into a helper).
// 2. Map the incoming string payload into something structured (e.g. split on a delimiter and pick a field to be your key).
// 3. Use `keyBy { ... }` and `window(TumblingProcessingTimeWindows.of(Time.seconds(30)))` to group events.
// 4. Aggregate by counting the elements inside each window (`reduce` or `aggregate`).
// 5. Emit the window metadata (start/end) together with the key and count.
// 6. Route the result to `print()` for now so you can iterate quickly.
//
// Execute with:
// ./gradlew runKotlinClass -PmainClass=tasks.flink._2_ProcessAndAggregateKt
fun main() {
    val env = StreamExecutionEnvironment.getExecutionEnvironment()

    val sourceBuilder = KafkaSource.builder<String>()
        // TODO: share the same settings from exercise 1 or extract a helper function

    val source = sourceBuilder.build()
    val stream = env.fromSource(
        source,
        WatermarkStrategy
            .forBoundedOutOfOrderness<String>(Duration.ofSeconds(5))
            .withTimestampAssigner { element, _ ->
                // TODO: extract an event timestamp if available, otherwise use System.currentTimeMillis()
                System.currentTimeMillis()
            },
        "partitioned-topic-source",
    )

    // val keyed = stream
    //     .map { raw -> parseEvent(raw) }
    //     .keyBy { event -> event.type }
    //     .window(TumblingProcessingTimeWindows.of(Time.seconds(30)))
    //     .reduce(
    //         { left, right -> left.copy(count = left.count + right.count) },
    //         { key, window, aggregate, out ->
    //             out.collect("${'$'}key @ ${'$'}{window.start}-${'$'}{window.end}: ${'$'}aggregate")
    //         },
    //     )
    // keyed.print()

    // env.execute("Windowed aggregation with Flink")
}

// fun parseEvent(raw: String): WorkshopEvent {
//     TODO("Split on comma or JSON decode to derive a key and any other fields you need")
// }
//
// data class WorkshopEvent(val type: String, val count: Long = 1)
