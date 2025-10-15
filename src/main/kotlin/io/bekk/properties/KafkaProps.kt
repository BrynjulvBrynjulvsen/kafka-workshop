package io.bekk.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.kafka")
class KafkaProps {
    lateinit var bootstrapServer: String
    lateinit var schemaRegistryUrl: String
    var securityProtocol: String = "PLAINTEXT"
    var dltEnabled: Boolean = false
}
