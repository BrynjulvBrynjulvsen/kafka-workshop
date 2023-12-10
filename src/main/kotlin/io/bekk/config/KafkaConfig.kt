package io.bekk.config

import io.bekk.producer.WorkshopKafkaProducer
import io.bekk.properties.KafkaProps
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.avro.specific.SpecificRecordBase
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.IntegerSerializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.serializer.DelegatingByTypeSerializer
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.util.backoff.FixedBackOff

@EnableKafka
@Configuration
class KafkaConfig(val context: ApplicationContext, val props: KafkaProps) {
    @Bean
    fun <K : Any, V : SpecificRecordBase> producerFactory(): ProducerFactory<K, V> =
        DefaultKafkaProducerFactory(
            producerProps(props)
        )

    @Bean
    fun stringProducerFactory(): ProducerFactory<String, String> =
        DefaultKafkaProducerFactory(
            producerProps(props)
        )

    @Bean
    fun <K : Any, V : SpecificRecordBase> kafkaTemplate(): KafkaTemplate<K, V> =
        KafkaTemplate(producerFactory())

    @Bean
    fun stringKafkaTemplate(): KafkaTemplate<String, String> =
        KafkaTemplate(stringProducerFactory())

    @Bean
    fun stringConsumerFactory(): ConsumerFactory<String, String> =
        DefaultKafkaConsumerFactory(
            serverProps(props) + commonProps() +
                    mapOf(
                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
                        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer::class.qualifiedName,
                        ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS to "org.apache.kafka.common.serialization.StringDeserializer",
                        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer::class.qualifiedName,
                        ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS to "org.apache.kafka.common.serialization.StringDeserializer",
                    )
        )

    @Bean
    fun stringListenerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> =
        ConcurrentKafkaListenerContainerFactory<String, String>().apply {
            consumerFactory = stringConsumerFactory()

            mySimpleErrorHandler(stringKafkaTemplate())
        }

    @Bean
    fun <K : Any, V : SpecificRecordBase> consumerFactory(): ConsumerFactory<K, V> =
        DefaultKafkaConsumerFactory(
            serverProps(props) + commonProps() +
                    mapOf(
                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
                        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer::class.qualifiedName,
                        ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS to props.keyDeserializer,
                        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer::class.qualifiedName,
                        ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS to props.valueDeserializer,
                        KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG to props.useSpecificAvro
                    )
        )


    @Bean
    fun <K : Any, V : SpecificRecordBase> listenerFactory(): ConcurrentKafkaListenerContainerFactory<K, V> =
        ConcurrentKafkaListenerContainerFactory<K, V>().apply {
            consumerFactory = consumerFactory()

             mySimpleErrorHandler(kafkaTemplate())
        }

    private fun byteArrayOrAvroSerializer(): DelegatingByTypeSerializer =
        DelegatingByTypeSerializer(props.avroSerializableClasses.map { Class.forName(it) }
            .associateWith { KafkaAvroSerializer() }.plus(
                mapOf(
                    ByteArray::class.java to ByteArraySerializer(),
                    String::class.java to StringSerializer(),
                    Int::class.java to IntegerSerializer()
                )
            )
        )

    private fun deadLetterTemplate(props: Map<String, String>): KafkaTemplate<Any, Any> = KafkaTemplate(
        DefaultKafkaProducerFactory(
            props, byteArrayOrAvroSerializer(), byteArrayOrAvroSerializer()
        )
    )

    fun commonProps() = mapOf(
        CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to props.securityProtocol
    )

    fun producerProps(properties: KafkaProps): Map<String, String> =
        serverProps(properties) + commonProps() + mapOf(
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to properties.keySerializer,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to properties.valueSerializer
        )

    private fun<K: Any, V: Any> ConcurrentKafkaListenerContainerFactory<K,V>.mySimpleErrorHandler(kafkaTemplate: KafkaTemplate<K,V>) {
        if (props.dltEnabled) { //This is useful if you want to enable a DLT handler
            setCommonErrorHandler(
                DefaultErrorHandler(
                    DeadLetterPublishingRecoverer(
                        if (props.avroSerializableClasses.isEmpty()) {
                            kafkaTemplate
                        } else {
                            deadLetterTemplate(producerProps(props)) // useful if you need to handle deserialization failures
                        }
                    ) { record: ConsumerRecord<*, *>, _: Exception ->
                        TopicPartition(

                            "${record.topic()}-dlt", -1
                        )
                    }, FixedBackOff(1L, 2L)
                )
            )
        }
    }

    companion object {

        private fun serverConfig(
            bootstrapServerUrl: String,
            schemaRegistryUrl: String,
            schemaRegistryBasicAuth: String,
            saslUsername: String,
            saslPassword: String
        ): Map<String, String> =
            mapOf(
                CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to bootstrapServerUrl,
                CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to "SASL_SSL",
                AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to schemaRegistryUrl,
                SaslConfigs.SASL_MECHANISM to "SCRAM-SHA-256",
                SaslConfigs.SASL_JAAS_CONFIG to "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"$saslUsername\" password=\"$saslPassword\";",
                SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE to "USER_INFO",
                SchemaRegistryClientConfig.USER_INFO_CONFIG to schemaRegistryBasicAuth
            )

        private fun serverProps(properties: KafkaProps): Map<String, String> =
            serverConfig(
                properties.bootstrapServer,
                properties.schemaRegistryUrl,
                properties.schemaRegistryBasicAuth,
                properties.saslUsername,
                properties.saslPassword
            )

    }

}