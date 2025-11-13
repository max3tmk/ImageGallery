package com.innowise.activity.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.util.UUID

@Document(collection = "comment_event")
data class CommentEvent(
    @Id
    val id: UUID = UUID.randomUUID(),

    val userId: UUID,
    val imageId: UUID,
    val commentId: UUID,
    val isCreated: Boolean,
    val content: String?,
    val timestamp: LocalDateTime = LocalDateTime.now(),

    val eventType: String = "COMMENT"
)