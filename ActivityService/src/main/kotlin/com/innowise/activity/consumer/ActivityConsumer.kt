package com.innowise.activity.consumer

import com.innowise.activity.model.CommentEvent
import com.innowise.activity.model.LikeEvent
import com.innowise.activity.repository.CommentEventRepository
import com.innowise.activity.repository.LikeEventRepository
import com.innowise.common.dto.event.CommentEventDto
import com.innowise.common.dto.event.LikeEventDto
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
open class ActivityConsumer(
    private val likeEventRepository: LikeEventRepository,
    private val commentEventRepository: CommentEventRepository
) {

    private val logger = LoggerFactory.getLogger(ActivityConsumer::class.java)

    @KafkaListener(topics = ["image-like-events"], groupId = "activity-group")
    open fun consumeLikeEvent(event: LikeEventDto) {
        try {
            val likeEvent = LikeEvent(
                userId = event.getUserId(),
                imageId = event.getImageId(),
                isAdded = event.isAdded(),
                timestamp = event.getTimestamp() ?: java.time.LocalDateTime.now()
            )

            likeEventRepository.save(likeEvent)
            logger.info("Saved LikeEvent: userId=${event.getUserId()}, imageId=${event.getImageId()}, added=${event.isAdded()}")
        } catch (e: Exception) {
            logger.error("Failed to process LikeEvent: ${e.message}", e)
        }
    }

    @KafkaListener(topics = ["image-comment-events"], groupId = "activity-group")
    open fun consumeCommentEvent(event: CommentEventDto) {
        try {
            val commentEvent = CommentEvent(
                userId = event.getUserId(),
                imageId = event.getImageId(),
                commentId = event.getCommentId(),
                isCreated = event.isCreated(),
                content = event.getContent() ?: "",
                timestamp = event.getTimestamp() ?: java.time.LocalDateTime.now()
            )

            commentEventRepository.save(commentEvent)
            logger.info("Saved CommentEvent: userId=${event.getUserId()}, imageId=${event.getImageId()}, commentId=${event.getCommentId()}")
        } catch (e: Exception) {
            logger.error("Failed to process CommentEvent: ${e.message}", e)
        }
    }
}
