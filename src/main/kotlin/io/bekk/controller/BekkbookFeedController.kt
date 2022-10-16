package io.bekk.controller

import io.bekk.publisher.BekkbookStatusMessage
import io.bekk.repository.FeedRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class BekkbookFeedController(
    val feedRepository: FeedRepository
) {

    @GetMapping("/status-feed/")
    fun getStatusFeed(): ResponseEntity<List<BekkbookStatusMessage>> {
        return ResponseEntity.ok(feedRepository.getFeed())
    }

//    ---- OUT OF SCOPE -------
//    fun getAllNewMessagesForTopic()
//    fun streamMessageFeed()

    // Optional:
    @GetMapping("/messages/{topic}")
    fun getAllMessagesForTopic(
        @PathVariable(value = "topic")
        topic: String
    ): ResponseEntity<List<String>> {
        // TODO: implement me
        return ResponseEntity.ok(emptyList())
    }
}