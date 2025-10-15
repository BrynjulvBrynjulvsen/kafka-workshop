package io.bekk.repository

import io.bekk.publisher.WorkshopStatusMessage
import java.util.concurrent.CopyOnWriteArrayList
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Repository

@Repository
class FeedRepository {

    private val statusFeedStore = CopyOnWriteArrayList<WorkshopStatusMessageConsumerRecord>()
    private val helloWorldFeedStore = CopyOnWriteArrayList<ConsumerRecordWithStringValue>()

    val statusFeed: List<WorkshopStatusMessageConsumerRecord>
        get() = statusFeedStore.toList()

    val helloWorldFeed: List<ConsumerRecordWithStringValue>
        get() = helloWorldFeedStore.toList()

    /**
     * Each listener instance uses a random group id so they do not interfere with the Kotlin workshop code.
     * This is convenient for demos but real services should pick a stable group id.
     */
    @KafkaListener(
        topics = [feedTopic],
        containerFactory = "avroListenerFactory",
        groupId = "#{T(java.util.UUID).randomUUID().toString()}"
    )
    fun receiveStatusFeedRecord(
        @Header(KafkaHeaders.RECEIVED_KEY) key: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) timestamp: Long,
        @Header(KafkaHeaders.GROUP_ID) groupId: String,
        @Payload record: WorkshopStatusMessage
    ) {
        statusFeedStore.add(
            WorkshopStatusMessageConsumerRecord(
                topicName = feedTopic,
                partition = partition,
                offset = offset,
                timestamp = timestamp,
                key = key,
                value = WorkshopStatusMessageData(record.message),
            )
        )
        trim(statusFeedStore)
    }

    @KafkaListener(topics = ["hello-world"], containerFactory = "stringListenerFactory", groupId = "#{T(java.util.UUID).randomUUID().toString()}")
    fun receiveHelloWorldRecord(
        @Header(KafkaHeaders.RECEIVED_KEY) key: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) timestamp: Long,
        @Header(KafkaHeaders.GROUP_ID) groupId: String,
        @Payload record: String
    ) {
        helloWorldFeedStore.add(
            ConsumerRecordWithStringValue(
                topicName = "hello-world",
                partition = partition,
                offset = offset,
                timestamp = timestamp,
                key = key,
                value = record,
            )
        )
        trim(helloWorldFeedStore)
    }

    companion object {
        const val feedTopic = "workshop-status-message"
        private const val MAX_ENTRIES = 50

        private fun <T> trim(store: CopyOnWriteArrayList<T>) {
            while (store.size > MAX_ENTRIES) {
                store.removeAt(0)
            }
        }
    }
}

data class WorkshopStatusMessageData(
    val message: String
)

data class WorkshopStatusMessageConsumerRecord(
    val topicName: String,
    val partition: Int,
    val offset: Long,
    val timestamp: Long,
    val key: String,
    val value: WorkshopStatusMessageData
)

// Compare with above - we return a custom data type - this could be replaced with an arbitrary DTO, or we could use the deserialised Avro-generated object
data class ConsumerRecordWithStringValue(
    val topicName: String,
    val partition: Int,
    val offset: Long,
    val timestamp: Long,
    val key: String,
    val value: String
)
