@file:Suppress("UNREACHABLE_CODE", "UNUSED_VARIABLE")

package tasks.flink

import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import tasks.Constants

// Bootstrap the Flink streaming environment and tail a Kafka topic using the new KafkaSource API.
//
// Goal: configure a source that reads plain string payloads from `Constants.PARTITIONED_TOPIC` and
// forward the records to a simple print sink so you can validate that the job wiring works.
//
// Suggested steps:
// 1. Build a KafkaSource configured with the workshop broker (`localhost:9094` on the host)
//    and a group id dedicated to this exercise (e.g. "flink-workshop"). Start from the provided builder.
// 2. Use `OffsetsInitializer.earliest()` so the job replays existing backlog.
// 3. Create a DataStream by calling `env.fromSource(...)` with a `WatermarkStrategy.noWatermarks()`
//    since these events don't rely on event time yet.
// 4. Route the stream to `print()` to confirm the job consumes data.
// 5. Call `env.execute(...)` when you're ready to run the pipeline.
//
// Execute with:
// ./gradlew runKotlinClass -PmainClass=tasks.flink._1_SetupFlinkKafkaSourceKt
fun main() {
    val env = StreamExecutionEnvironment.getExecutionEnvironment()

    val sourceBuilder = KafkaSource.builder<String>()
        // TODO: set bootstrap servers, topics, group id and choose an offsets initializer
        // .setBootstrapServers("localhost:9094")
        // .setTopics(Constants.PARTITIONED_TOPIC)
        // .setGroupId("flink-workshop")
        // .setStartingOffsets(OffsetsInitializer.earliest())
        // .setValueOnlyDeserializer(SimpleStringSchema())

    // val source = sourceBuilder.build()
    // val stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "partitioned-topic-source")
    // stream.print()

    // env.execute("Inspect partitioned-topic via Flink")
}
