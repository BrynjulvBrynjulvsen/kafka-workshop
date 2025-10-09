@file:Suppress("UNREACHABLE_CODE", "UNUSED_VARIABLE")

package tasks.flink

import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema
import org.apache.flink.connector.kafka.sink.KafkaSink
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import tasks.Constants
import java.util.Properties

// Push Flink results back into Kafka using the KafkaSink connector.
//
// Goal: reuse the aggregation pipeline from exercise 2 and emit the windowed counts to a
// dedicated output topic (e.g. `flink-aggregates`) so other tools or services can consume them.
//
// Suggested steps:
// 1. Make sure the destination topic exists (use `kafka-topics --create` if needed).
// 2. Configure a `KafkaSink<String>` with producer properties and a `SimpleStringSchema` serializer.
// 3. After computing your aggregates, call `sinkTo(kafkaSink)` instead of `print()`.
// 4. Consider formatting each record as JSON or CSV so downstream consumers can parse it easily.
// 5. Once running, tail the topic with `kafka-console-consumer` or `kcat` to validate the output.
//
// Execute with:
// ./gradlew runKotlinClass -PmainClass=tasks.flink._3_WriteAggregatesToKafkaKt
fun main() {
    val env = StreamExecutionEnvironment.getExecutionEnvironment()

    val sourceBuilder = KafkaSource.builder<String>()
        // TODO: reuse the setup from earlier exercises
        // .setBootstrapServers("localhost:9094")
        // .setTopics(Constants.PARTITIONED_TOPIC)
        // .setGroupId("flink-aggregates")
        // .setStartingOffsets(OffsetsInitializer.latest())
        // .setValueOnlyDeserializer(SimpleStringSchema())

    val source = sourceBuilder.build()
    val stream = env.fromSource(
        source,
        WatermarkStrategy.noWatermarks<String>(),
        "partitioned-topic-source",
    )

    // val aggregates = stream
    //     .map { raw -> parseEvent(raw) }
    //     .keyBy { event -> event.type }
    //     .window(TumblingProcessingTimeWindows.of(Time.minutes(1)))
    //     .reduce { left, right -> left.copy(count = left.count + right.count) }
    //     .map { event -> "${'$'}{event.type},${'$'}{event.count}" }

    val kafkaProps = Properties().apply {
        // TODO: configure bootstrap servers and optional producer tuning
        // put("bootstrap.servers", "localhost:9094")
        // put("acks", "all")
    }

    val sink = KafkaSink.builder<String>()
        // TODO: point the sink at your output topic using KafkaRecordSerializationSchema.builder()
        // .setKafkaProducerConfig(kafkaProps)
        // .setRecordSerializer(
        //     KafkaRecordSerializationSchema.builder<String>()
        //         .setTopic("flink-aggregates")
        //         .setValueSerializationSchema(SimpleStringSchema())
        //         .build(),
        // )
        .build()

    // aggregates.sinkTo(sink)

    // env.execute("Windowed aggregation to Kafka")
}
