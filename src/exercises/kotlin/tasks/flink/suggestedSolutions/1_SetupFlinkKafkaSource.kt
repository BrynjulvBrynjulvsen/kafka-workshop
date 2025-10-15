package tasks.flink.suggestedSolutions

import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import tasks.Constants

fun main() {
    val env = StreamExecutionEnvironment.getExecutionEnvironment()

    val source = KafkaSource.builder<String>()
        .setBootstrapServers(tasks.KafkaConfig.bootstrapServers)
        .setTopics(Constants.PARTITIONED_TOPIC)
        .setGroupId("flink-workshop")
        .setStartingOffsets(OffsetsInitializer.earliest())
        .setValueOnlyDeserializer(SimpleStringSchema())
        .build()

    env.fromSource(
        source,
        WatermarkStrategy.noWatermarks(),
        "partitioned-topic-source",
    ).print()

    env.execute("Inspect partitioned-topic via Flink")
}
