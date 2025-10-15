package io.bekk.config

import io.bekk.properties.KafkaProps
import io.bekk.publisher.WorkshopStatusMessage
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.FixedBackOff
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig

/**
 * Deliberately slim Kafka wiring so the workshop can focus on messaging fundamentals.
 * The shortcuts here (single cluster, plain PLAINTEXT, optional DLT, etc.) keep the sample approachable.
 * Call them out if you reuse this code—production services should apply stricter hardening.
 */
@EnableKafka
@Configuration
class KafkaConfig(private val props: KafkaProps) {

    @Bean
    fun stringKafkaTemplate(): KafkaTemplate<String, String> =
        KafkaTemplate(DefaultKafkaProducerFactory<String, String>(stringProducerProps()))

    @Bean
    fun avroKafkaTemplate(): KafkaTemplate<String, WorkshopStatusMessage> =
        KafkaTemplate(DefaultKafkaProducerFactory<String, WorkshopStatusMessage>(avroProducerProps()))

    @Bean
    fun stringListenerFactory(
        stringKafkaTemplate: KafkaTemplate<String, String>,
    ): ConcurrentKafkaListenerContainerFactory<String, String> =
        ConcurrentKafkaListenerContainerFactory<String, String>().apply {
            consumerFactory = DefaultKafkaConsumerFactory<String, String>(stringConsumerProps())
            applyErrorHandler(stringKafkaTemplate)
        }

    @Bean
    fun avroListenerFactory(
        avroKafkaTemplate: KafkaTemplate<String, WorkshopStatusMessage>,
    ): ConcurrentKafkaListenerContainerFactory<String, WorkshopStatusMessage> =
        ConcurrentKafkaListenerContainerFactory<String, WorkshopStatusMessage>().apply {
            consumerFactory = DefaultKafkaConsumerFactory<String, WorkshopStatusMessage>(avroConsumerProps())
            applyErrorHandler(avroKafkaTemplate)
        }

    private fun stringProducerProps(): Map<String, Any> =
        baseClientProps() + mapOf(
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
        )

    private fun avroProducerProps(): Map<String, Any> =
        baseClientProps() + mapOf(
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to KafkaAvroSerializer::class.java,
            AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to props.schemaRegistryUrl,
        )

    private fun stringConsumerProps(): Map<String, Any> =
        baseClientProps() + mapOf(
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
        )

    private fun avroConsumerProps(): Map<String, Any> =
        baseClientProps() + mapOf(
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to KafkaAvroDeserializer::class.java,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to props.schemaRegistryUrl,
            KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG to true,
        )

    private fun baseClientProps(): Map<String, Any> =
        mapOf(
            CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to props.bootstrapServer,
            CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to props.securityProtocol,
        )

    private fun <K, V> ConcurrentKafkaListenerContainerFactory<K, V>.applyErrorHandler(
        template: KafkaTemplate<*, *>,
    ) {
        if (!props.dltEnabled) return

        val recoverer = DeadLetterPublishingRecoverer(template) { record, _ ->
            TopicPartition("${record.topic()}-dlt", record.partition())
        }

        setCommonErrorHandler(
            DefaultErrorHandler(
                recoverer,
                FixedBackOff(1_000L, 2L),
            )
        )
    }
}
