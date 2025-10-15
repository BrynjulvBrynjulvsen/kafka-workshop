package tasks.auth

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import java.time.Duration
import java.util.Properties
import kotlin.system.exitProcess

/**
 * Goal: connect to the SASL_PLAINTEXT listener using SCRAM-SHA-512 credentials,
 * produce a message, and read it back with a dedicated consumer group.
 *
 * Run with:
 * ./gradlew runKotlinClass -PmainClass=tasks.auth._1_scram_clientKt
 */
fun main() {
    val topicName = "scram-demo"
    val groupId = "scram-demo-group"

    val securityProps = Properties().apply {
        // TODO: set bootstrap.servers to the host:port exposed by the SASL listener (see docker-compose.scram.yml)
        // TODO: set security.protocol to SASL_PLAINTEXT
        // TODO: set sasl.mechanism to SCRAM-SHA-512
        // TODO: set sasl.jaas.config with the workshop user's username/password created during the CLI exercises
    }

    val producerProps = Properties().apply {
        putAll(securityProps)
        put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
        put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
        put(ProducerConfig.ACKS_CONFIG, "all")
    }

    val consumerProps = Properties().apply {
        putAll(securityProps)
        put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
        put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
        put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
        put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    }

    KafkaProducer<String, String>(producerProps).use { producer ->
        println("Sending a probe message to $topicName")
        val participantName = System.getenv("USER") ?: "scram-trainee"
        val record = ProducerRecord(topicName, "auth-check", "Hello from $participantName")
        // TODO: send the record and await the resulting Future to ensure it was acknowledged
    }

    KafkaConsumer<String, String>(consumerProps).use { consumer ->
        consumer.subscribe(listOf(topicName))
        println("Polling for messages as $groupId")
        val records = consumer.poll(Duration.ofSeconds(5))
        if (records.isEmpty) {
            println("No records received within timeout. Double-check ACLs and listener config.")
            exitProcess(1)
        }

        records.forEach {
            println("Received (${it.key()}, ${it.value()}) from partition ${it.partition()} at offset ${it.offset()}")
        }
    }
}
