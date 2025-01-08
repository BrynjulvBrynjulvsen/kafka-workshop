package io.bekk.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.kafka")
class KafkaProps {
    lateinit var bootstrapServer: String
    lateinit var securityProtocol: String
    lateinit var schemaRegistryUrl: String
    var avroSerializableClasses: List<String> = emptyList()
    var keySerializer: String = "org.apache.kafka.common.serialization.StringSerializer"
    var keyDeserializer: String = "org.apache.kafka.common.serialization.StringDeserializer"
    var valueSerializer: String = "io.confluent.kafka.serializers.KafkaAvroSerializer"
    var valueDeserializer: String = "io.confluent.kafka.serializers.KafkaAvroDeserializer"
    var useSpecificAvro: Boolean = true
    var dltEnabled: Boolean = false
}