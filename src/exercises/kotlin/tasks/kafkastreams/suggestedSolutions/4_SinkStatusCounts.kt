package tasks.kafkastreams.suggestedSolutions

import java.time.Duration
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.TimeWindows
import org.apache.kafka.streams.kstream.WindowedSerdes
import tasks.kafkastreams.KafkaStreamsExerciseHelpers
import tasks.kafkastreams.parseWorkshopOrder

fun main() {
    val builder = StreamsBuilder()

    val formatted = builder.stream<String, String>(KafkaStreamsExerciseHelpers.sourceTopic)
        .flatMap { _, value ->
            parseWorkshopOrder(value)?.let { listOf(KeyValue(it.status, 1L)) } ?: emptyList()
        }
        .groupByKey(Grouped.with(Serdes.String(), Serdes.Long()))
        .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(60)))
        .count(org.apache.kafka.streams.kstream.Materialized.with(Serdes.String(), Serdes.Long()))
        .toStream()
        .map { windowedKey, count ->
            val window = windowedKey.window()
            val status = windowedKey.key()
            val payload = "status=$status,count=$count,windowStart=${window.startTime()},windowEnd=${window.endTime()}"
            KeyValue(windowedKey, payload)
        }

    formatted.to(
        KafkaStreamsExerciseHelpers.aggregatesTopic,
        Produced.with(
            WindowedSerdes.timeWindowedSerdeFrom(String::class.java, Duration.ofSeconds(60).toMillis()),
            Serdes.String(),
        ),
    )

    val streams = KafkaStreams(builder.build(), KafkaStreamsExerciseHelpers.baseStreamsConfig("kstreams-workshop-sink"))
    Runtime.getRuntime().addShutdownHook(Thread { streams.close() })
    streams.start()
}
