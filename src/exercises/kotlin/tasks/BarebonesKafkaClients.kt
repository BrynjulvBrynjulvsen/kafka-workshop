package tasks

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration

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
                        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to "false"
                    )
        )

    fun <V> getAvroProducer(): KafkaProducer<String, V> =
        KafkaProducer<String, V>(
            sharedProps() + mapOf(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringSerializer",
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to "io.confluent.kafka.serializers.KafkaAvroSerializer"
            )
        )

    fun <V> getAvroConsumer(offsetConfig: String = "earliest", groupId: String = "random-group-${UUID.randomUUID()}"):
            KafkaConsumer<String, V> =
        KafkaConsumer<String, V>(
            sharedProps() + mapOf(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to offsetConfig,
                ConsumerConfig.GROUP_ID_CONFIG to groupId,
                KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG to "true",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringDeserializer",
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to "io.confluent.kafka.serializers.KafkaAvroDeserializer",
                ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG to "6000"
            )
        )

}

class ContinuousProducer(private val topicName: String, private val messageProducer: () -> Pair<String, String>)  {

    private val pauseRendezvous: Channel<Unit> = Channel(0)
    private val startRendezvous: Channel<Unit> = Channel(0)

    val producer = BarebonesKafkaClients.getBareBonesProducer()

    init {
        CoroutineScope(Job()).launch(Dispatchers.IO) {
            startProducer()
        }
    }

    private suspend fun startProducer() {
        while (true) {
            startRendezvous.receive()
            while (pauseRendezvous.tryReceive().isFailure) {
                producer.send(
                    messageProducer().let {
                    ProducerRecord(
                        topicName,
                        it.first,
                        it.second
                    )
                    }
                )
                delay(100)
            }
        }
    }

    fun resume() {
        runBlocking {
            startRendezvous.send(Unit)
        }
    }

    fun stop() {
        runBlocking { pauseRendezvous.send(Unit) }
    }
}

class BasicConsumer(
    groupId: String,
    private val topicName: String,
    offsetResetConfig: String = "latest",
    private val consumeFunction: (ConsumerRecord<String, String>, KafkaConsumer<String, String>) -> Unit
) {

    val consumer = BarebonesKafkaClients.getBareBonesConsumer(groupId = groupId, offsetConfig = offsetResetConfig)
    private val pauseRendezvous: Channel<Unit> = Channel(0)
    private val resumeRendezvous: Channel<Unit> = Channel(0)

    init {
        CoroutineScope(Job()).launch(Dispatchers.IO) {
            startConsumer()
        }
    }

    private suspend fun startConsumer() {
        while (true) {
            consumer.subscribe(listOf(topicName))
            while (pauseRendezvous.tryReceive().isFailure) {
                consumer.poll(1000.milliseconds.toJavaDuration()).forEach {
                    consumeFunction(it, consumer)
                }
            }
            resumeRendezvous.receive()
        }
    }

    fun resume() {
        runBlocking {
            resumeRendezvous.send(Unit)
        }
    }

    fun stop() {
        runBlocking { pauseRendezvous.send(Unit) }
    }

    fun close() {
        stop()
        consumer.close()
    }

}
