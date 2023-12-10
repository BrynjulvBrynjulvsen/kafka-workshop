package io.bekk.controller

import io.bekk.producer.WorkshopKafkaProducer
import io.bekk.repository.WorkshopStatusMessageConsumerRecord
import io.bekk.repository.ConsumerRecordWithStringValue
import io.bekk.repository.FeedRepository
import org.apache.avro.generic.GenericRecord
import org.apache.avro.generic.GenericRecordBuilder
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class WorkshopFeedController(
    val feedRepository: FeedRepository,
    val producer: WorkshopKafkaProducer
) {

    @GetMapping("/status-feed/")
    fun getStatusFeed(): ResponseEntity<WorkshopStatusMessageConsumerRecordList> {
        return ResponseEntity.ok(
            WorkshopStatusMessageConsumerRecordList(recordList = feedRepository.statusFeed)
        )
    }

    @GetMapping("/hello-world-feed/")
    fun getHelloWorldFeed(): ResponseEntity<ConsumerRecordWithStringValueList> {
        return ResponseEntity.ok(
            ConsumerRecordWithStringValueList(recordList = feedRepository.helloWorldFeed)
        )
    }

    @PutMapping("/hello-world/{message}")
    fun putHelloWorldFeed(@PathVariable message: String): ResponseEntity<String> {
        producer.send("hello-world", UUID.randomUUID().toString(), message)
        return ResponseEntity.ok("Posted")
    }

}



data class WorkshopStatusMessageConsumerRecordList(
    val recordList: List<WorkshopStatusMessageConsumerRecord>
)
data class ConsumerRecordWithStringValueList(
    val recordList: List<ConsumerRecordWithStringValue>
)
