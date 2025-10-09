package tasks.kafkastreams.suggestedSolutions

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import tasks.kafkastreams.KafkaStreamsExerciseHelpers
import tasks.kafkastreams.parseWorkshopOrder

fun main() {
    val builder = StreamsBuilder()

    val summaries = builder.stream<String, String>(KafkaStreamsExerciseHelpers.sourceTopic)
        .flatMapValues { value ->
            parseWorkshopOrder(value)?.let { listOf(it) } ?: emptyList()
        }
        .mapValues { order -> KafkaStreamsExerciseHelpers.formatOrderSummary(order) }

    summaries.peek { key, value -> println("summary[$key]: $value") }

    val streams = KafkaStreams(builder.build(), KafkaStreamsExerciseHelpers.baseStreamsConfig("kstreams-workshop-parse"))
    Runtime.getRuntime().addShutdownHook(Thread { streams.close() })
    streams.start()
}
