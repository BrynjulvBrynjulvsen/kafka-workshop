package io.bekk.producer

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.Header
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class WorkshopKafkaProducer(private val kafkaTemplate: KafkaTemplate<String, String>) {
    fun send(
        topic: String,
        key: String,
        payload: String,
        customHeaders: List<Header>? = null
    ) {
        kafkaTemplate.send(
            ProducerRecord(topic, key, payload).apply {
                customHeaders?.forEach {
                    headers().add(it)
                }
            }
        )
    }
}