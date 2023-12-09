package tasks

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SaslConfigs
import java.util.*

object BarebonesKafkaClients {

    private const val BOOTSTRAP_SERVER_URL = "localhost:9094"
    private const val SCHEMA_REGISTRY_URL = "http://localhost:8085"

    fun sharedProps(): Map<String, String> {
        return mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to BOOTSTRAP_SERVER_URL,
            CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to "PLAINTEXT",
            AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to SCHEMA_REGISTRY_URL,
        )
    }

    fun getBareBonesProducer(): KafkaProducer<String, String> {
        val configMap = sharedProps() + mapOf(
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringSerializer",
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringSerializer"
        )
        return KafkaProducer<String, String>(configMap)
    }

    fun getBareBonesConsumer(
        offsetConfig: String = "latest",
        groupId: String = "my-consumer-${UUID.randomUUID()}",
        config: Map<String, String> = emptyMap()
    ) =
        KafkaConsumer<String, String>(
            sharedProps() + config +
                    mapOf(
                        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringDeserializer",
                        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringDeserializer",
                        ConsumerConfig.GROUP_ID_CONFIG to groupId,
                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to offsetConfig,
                    )
        )

    fun <V> getAvroProducer(): KafkaProducer<String, V> =
        KafkaProducer<String, V>(
            sharedProps() + mapOf(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringSerializer",
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to "io.confluent.kafka.serializers.KafkaAvroSerializer"
            )
        )

    fun <V> getAvroConsumer(offsetConfig: String = "earliest",groupId: String = "random-group-${UUID.randomUUID()}"):
            KafkaConsumer<String, V> =
        KafkaConsumer<String, V>(
            sharedProps() + mapOf(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to offsetConfig,
                ConsumerConfig.GROUP_ID_CONFIG to groupId,
                KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG to "true",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringDeserializer",
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to "io.confluent.kafka.serializers.KafkaAvroDeserializer"
            )
        )

}

