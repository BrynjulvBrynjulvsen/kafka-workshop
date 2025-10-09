@file:Suppress("UNREACHABLE_CODE", "UNUSED_VARIABLE")

package tasks.kafkastreams

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder

// Step 1/4 – Wire up a minimal Kafka Streams topology and observe the raw events.
//
// Goal: build a topology that tails `partitioned-topic` and prints each record to stdout.
//
// Suggested steps:
// 1. Create `StreamsBuilder` and call `builder.stream<String, String>(KafkaStreamsExerciseHelpers.sourceTopic)`.
// 2. Use `peek` or `foreach` to log the key/value pairs so you can confirm records flow through the topology.
// 3. Build the topology, create a `KafkaStreams` instance with `KafkaStreamsExerciseHelpers.baseStreamsConfig(...)`, and start it.
// 4. Add a shutdown hook so the app closes cleanly when you stop the process.
//
// Execute with:
// ./gradlew runKotlinClass -PmainClass=tasks.kafkastreams._1_BootstrapKafkaStreamsKt
fun main() {
    val builder = StreamsBuilder()

    val source = builder.stream<String, String>(KafkaStreamsExerciseHelpers.sourceTopic)

    // TODO: print the key/value pairs to stdout so you can confirm the topology is alive.
    // Hint: prefer `peek` during development so you do not mutate the stream.

    val topology = builder.build()
    val streams = KafkaStreams(topology, KafkaStreamsExerciseHelpers.baseStreamsConfig("kstreams-workshop-peek"))

    // TODO: start the streams instance and register a shutdown hook that calls close().
}
