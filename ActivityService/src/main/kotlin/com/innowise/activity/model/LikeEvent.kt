package com.innowise.activity.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.util.UUID

@Document(collection = "like_event")
data class LikeEvent(
    @Id
    val id: UUID = UUID.randomUUID(),

    val userId: UUID,
    val imageId: UUID,
    val isAdded: Boolean,
    val timestamp: LocalDateTime = LocalDateTime.now(),

    val eventType: String = "LIKE"
)