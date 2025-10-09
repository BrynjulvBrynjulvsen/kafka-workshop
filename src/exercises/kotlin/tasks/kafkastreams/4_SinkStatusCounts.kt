@file:Suppress("UNREACHABLE_CODE", "UNUSED_VARIABLE")

package tasks.kafkastreams

import java.time.Duration
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.kstream.TimeWindows
import org.apache.kafka.streams.kstream.WindowedSerdes

// Step 4/4 – Publish the status aggregates back to Kafka.
//
// Goal: reuse the windowed counts from step 3 and push the formatted results to a dedicated topic (`kstreams-aggregates`).
//
// Suggested steps:
// 1. Build the same status-counting topology as before (parse -> key by status -> window -> count).
// 2. Map the windowed results into strings suitable for downstream consumers.
// 3. `to()` the stream using a windowed key serde (e.g. `WindowedSerdes.timeWindowedSerdeFrom`) so each window remains distinct.
// 4. Start the application and tail `kstreams-aggregates` to confirm the records land there.
//
// Execute with:
// ./gradlew runKotlinClass -PmainClass=tasks.kafkastreams._4_SinkStatusCountsKt
fun main() {
    val builder = StreamsBuilder()

    val windowedCounts = builder.stream<String, String>(KafkaStreamsExerciseHelpers.sourceTopic)
        // TODO: parse WorkshopOrder values and keep only well-formed records.
        // Hint: reuse the helper from step 2 or extract the logic into a small function.
        // TODO: map to KeyValue(status, 1L), group by key (Grouped.with), window with TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(60)), and count.

    val formatted = windowedCounts
        // TODO: convert the windowed key + count into a string value that downstream services can understand.
/*
    formatted.to(
        KafkaStreamsExerciseHelpers.aggregatesTopic,
        Produced.with(
            WindowedSerdes.timeWindowedSerdeFrom(String::class.java, Duration.ofSeconds(60).toMillis()),
            Serdes.String(),
        ),
    )

 */

    val topology = builder.build()
    val streams = KafkaStreams(topology, KafkaStreamsExerciseHelpers.baseStreamsConfig("kstreams-workshop-sink"))

    // TODO: start the streams application and add a shutdown hook that calls close().
}
