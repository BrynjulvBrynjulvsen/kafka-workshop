@file:Suppress("UNREACHABLE_CODE", "UNUSED_VARIABLE")

package tasks.kafkastreams

import java.time.Duration
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Printed
import org.apache.kafka.streams.kstream.TimeWindows
import org.apache.kafka.streams.kstream.Windowed

// Step 3/4 – Aggregate order statuses with tumbling windows.
//
// Goal: count how many orders reach each status in 30-second processing-time windows and print the results.
//
// Suggested steps:
// 1. Parse the stream just like in step 2 (consider extracting a helper so you do not duplicate code).
// 2. Select the status as the new key and window the stream with `TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(30))`.
// 3. Use `.count()` (with an in-memory Materialized store) to produce a `KTable<Windowed<String>, Long>`.
// 4. Convert the windowed result into readable strings that include the status, window bounds, and count. Print them.
//
// Execute with:
// ./gradlew runKotlinClass -PmainClass=tasks.kafkastreams._3_CountStatusWindowsKt
fun main() {
    val builder = StreamsBuilder()

    val orders = builder.stream<String, String>(KafkaStreamsExerciseHelpers.sourceTopic)
        // TODO: parse to WorkshopOrder and drop malformed rows (borrow your step 2 code or extract a helper).

    val windowedCounts = orders
        // TODO: turn each order into (status -> 1) pairs, group by status, then window + count.
        // Hint: map to KeyValue(status, 1L) -> groupByKey(Grouped.with(StringSerde, LongSerde)) -> windowedBy(TimeWindows.ofSizeWithNoGrace(...)) -> count().

    val printableCounts = windowedCounts
        // TODO: convert the `Windowed<String>` key + count into a human-readable string for logging.
        // Remember you can access window start/end via `windowedKey.window().start()` etc.

    //printableCounts.toStream().print(Printed.toSysOut<String>().withLabel("status-counts"))

    val topology = builder.build()
    val streams = KafkaStreams(topology, KafkaStreamsExerciseHelpers.baseStreamsConfig("kstreams-workshop-counts"))

    // TODO: start and stop the streams instance properly.
}
