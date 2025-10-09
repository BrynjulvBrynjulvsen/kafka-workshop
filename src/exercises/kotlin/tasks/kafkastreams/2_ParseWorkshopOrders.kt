@file:Suppress("UNREACHABLE_CODE", "UNUSED_VARIABLE")

package tasks.kafkastreams

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder

// Step 2/4 – Parse the raw strings into structured workshop orders.
//
// Goal: convert the incoming values to `WorkshopOrder`, drop malformed records, and emit readable summaries.
//
// Suggested steps:
// 1. Reuse the source stream from step 1.
// 2. Use `flatMapValues` or `mapValuesNotNull` style logic to turn each string into a `WorkshopOrder` via `parseWorkshopOrder`.
// 3. Convert the orders into a friendly summary string (e.g. "customer-123 -> SHIPPED (eu-west) amount=88.40") and log them.
// 4. Start the topology and watch the parsed output flow by.
//
// Execute with:
// ./gradlew runKotlinClass -PmainClass=tasks.kafkastreams._2_ParseWorkshopOrdersKt
fun main() {
    val builder = StreamsBuilder()

    val ordersStream = builder.stream<String, String>(KafkaStreamsExerciseHelpers.sourceTopic)
        // TODO: parse each value into a WorkshopOrder and drop nulls.
        // Hint: call `parseWorkshopOrder(value)` and only emit when it returns a value.

    val summaries = ordersStream
        // TODO: map each order into a descriptive string (KafkaStreamsExerciseHelpers.formatOrderSummary can help).

    // TODO: log the summaries (peek/foreach) so you can verify the output structure looks right.

    val topology = builder.build()
    val streams = KafkaStreams(topology, KafkaStreamsExerciseHelpers.baseStreamsConfig("kstreams-workshop-parse"))

    // TODO: start the streams application and ensure it closes on JVM shutdown.
}
