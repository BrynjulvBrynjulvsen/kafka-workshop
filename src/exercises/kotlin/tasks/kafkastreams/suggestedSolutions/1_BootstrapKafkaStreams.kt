package tasks.kafkastreams.suggestedSolutions

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import tasks.kafkastreams.KafkaStreamsExerciseHelpers

fun main() {
    val builder = StreamsBuilder()

    builder.stream<String, String>(KafkaStreamsExerciseHelpers.sourceTopic)
        .peek { key, value -> println("peek[$key]: $value") }

    val streams = KafkaStreams(builder.build(), KafkaStreamsExerciseHelpers.baseStreamsConfig("kstreams-workshop-peek"))

    Runtime.getRuntime().addShutdownHook(Thread { streams.close() })

    streams.start()
}
