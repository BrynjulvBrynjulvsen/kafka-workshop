package io.bekk.producer

import io.bekk.publisher.WorkshopStatusMessage
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.Header
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

/**
 * Minimal façade for workshop demos. Real applications often prefer domain-specific services
 * (and structured payloads everywhere), but we keep a lightweight API here to focus on the Kafka mechanics.
 */
@Service
class WorkshopKafkaProducer(
    private val stringTemplate: KafkaTemplate<String, String>,
    private val avroTemplate: KafkaTemplate<String, WorkshopStatusMessage>,
) {

    fun sendPlainText(
        topic: String,
        key: String,
        payload: String,
        customHeaders: List<Header>? = null,
    ) {
        stringTemplate.send(
            ProducerRecord(topic, key, payload).apply {
                customHeaders?.forEach { headers().add(it) }
            }
        )
    }

    fun sendStatusUpdate(
        topic: String,
        key: String,
        payload: WorkshopStatusMessage,
    ) {
        avroTemplate.send(topic, key, payload)
    }
}
