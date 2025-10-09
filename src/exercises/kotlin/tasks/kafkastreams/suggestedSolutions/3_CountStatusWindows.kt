package tasks.kafkastreams.suggestedSolutions

import java.time.Duration
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.TimeWindows
import tasks.kafkastreams.KafkaStreamsExerciseHelpers
import tasks.kafkastreams.parseWorkshopOrder

fun main() {
    val builder = StreamsBuilder()

    val windowedCounts = builder.stream<String, String>(KafkaStreamsExerciseHelpers.sourceTopic)
        .flatMap { _, value ->
            parseWorkshopOrder(value)?.let { listOf(KeyValue(it.status, 1L)) } ?: emptyList()
        }
        .groupByKey(Grouped.with(Serdes.String(), Serdes.Long()))
        .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(30)))
        .count(org.apache.kafka.streams.kstream.Materialized.with(Serdes.String(), Serdes.Long()))
        // If you want to see a single event for each window, you can use suppress
        //.suppress (Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded()))

    windowedCounts.toStream().peek { windowedKey, count ->
        val window = windowedKey.window()
        val status = windowedKey.key()
        val formatted = "status=$status,count=$count,windowStart=${window.startTime()},windowEnd=${window.endTime()}"
        println("windowed[${windowedKey}]: $formatted")
    }

    val streams = KafkaStreams(builder.build(), KafkaStreamsExerciseHelpers.baseStreamsConfig("kstreams-workshop-counts"))
    Runtime.getRuntime().addShutdownHook(Thread { streams.close() })
    streams.start()
}
