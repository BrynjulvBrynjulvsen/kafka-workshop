package tasks

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.config.SaslConfigs
import tasks.cleanup.suggested_solutions.topic_name
import java.time.Duration
import kotlin.time.Duration.Companion.seconds

val bootstrapUrl = "<your-bootstrap-server-url>"
val schemaRegistryUrl = "https://<your-schema-registry-url>"

fun main() {
    // Kotlin convention uses the main()-function as a main entry point


}

fun `produce a basic message`() {

    val producer = KafkaProducer<String, String>(
        mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapUrl,
            CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to "PLAINTEXT",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringSerializer",
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringSerializer"
        )
    )

    producer.send(
        ProducerRecord(
            "my-topic-name",
            "my-key",
            "my-value"
        )
    )
}

fun `consume a basic topic from start`() {
    val consumer = KafkaConsumer<String, String>(
        mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapUrl,
            CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to "PLAINTEXT",
            AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to schemaRegistryUrl,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringDeserializer",
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringDeserializer",
            ConsumerConfig.GROUP_ID_CONFIG to "my-group-id", // Which consumer group to join. If this already exist,
            // you will join the group and trigger a rebalance upon calling poll()
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
        )
    )

    consumer.subscribe(listOf("my-topic-name")) // Configure which topics your consumer should subscribe to.
    // Doesn't actually join the consumer group at this step

    repeatFor (90.seconds) {
        consumer.poll(Duration.ofMillis(10000L)) // Poll Kafka. This step triggers connecting to Kafka and joining
            // the consumer group when first called.
            .forEach {
                //do something with your ConsumerRecord<String, String>
            }
        consumer.commitAsync() // Commits the offset received from last .poll() to Kafka for this consumer group
    }
    // Remember to close the consumer when done - otherwise, next rebalance for this group will have to wait until the
    // old consumers time out
    consumer.close()
}

fun `produce a continuous stream of messages`() {

    // a helper class which continuously produce messages based on the input function. Useful for eliminating boilerplate in later exercises.
    // note the lambda after paranthesis - this is Kotlin convention for a call whose final parameter is a function.
    val continuousProducer = ContinuousProducer(Constants.TOPIC_NAME) { "key" to "value" }

    // Counterpart to the above - a helper class which continuously consumes messages
    val continuousConsumer = BasicContinuousConsumer(groupId = "my-group", topicName = Constants.TOPIC_NAME) {
        record, _ -> println("${record.key()} ${record.value()}")
    }

    Thread.sleep(10000L)
    continuousConsumer.close()
}

