package com.innowise.activity.consumer

import com.innowise.activity.model.CommentEvent
import com.innowise.activity.model.LikeEvent
import com.innowise.activity.repository.CommentEventRepository
import com.innowise.activity.repository.LikeEventRepository
import com.innowise.common.dto.event.CommentEventDto
import com.innowise.common.dto.event.LikeEventDto
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.UUID

class ActivityConsumerTest {

    private lateinit var likeEventRepository: LikeEventRepository
    private lateinit var commentEventRepository: CommentEventRepository
    private lateinit var activityConsumer: ActivityConsumer

    @BeforeEach
    fun setUp() {
        likeEventRepository = mock(LikeEventRepository::class.java)
        commentEventRepository = mock(CommentEventRepository::class.java)
        activityConsumer = ActivityConsumer(likeEventRepository, commentEventRepository)
    }

    @Test
    fun `consumeLikeEvent should save like event`() {
        val userId = UUID.randomUUID()
        val imageId = UUID.randomUUID()
        val likeEventDto = createLikeEventDto(userId, imageId, true)

        activityConsumer.consumeLikeEvent(likeEventDto)

        verify(likeEventRepository, times(1)).save(any(LikeEvent::class.java))
    }

    @Test
    fun `consumeCommentEvent should save comment event`() {
        val userId = UUID.randomUUID()
        val imageId = UUID.randomUUID()
        val commentId = UUID.randomUUID()
        val commentEventDto = createCommentEventDto(userId, imageId, commentId, true, "Test comment")

        activityConsumer.consumeCommentEvent(commentEventDto)

        verify(commentEventRepository, times(1)).save(any(CommentEvent::class.java))
    }

    @Test
    fun `consumeLikeEvent should handle exception gracefully`() {
        val likeEventDto = LikeEventDto()
        likeEventDto.setUserId(UUID.randomUUID())
        likeEventDto.setImageId(UUID.randomUUID())
        likeEventDto.setAdded(true)
        likeEventDto.setTimestamp(LocalDateTime.now())

        `when`(likeEventRepository.save(any(LikeEvent::class.java))).thenThrow(RuntimeException("Test exception"))

        activityConsumer.consumeLikeEvent(likeEventDto)
    }

    @Test
    fun `consumeCommentEvent should handle exception gracefully`() {
        val commentEventDto = CommentEventDto()
        commentEventDto.setUserId(UUID.randomUUID())
        commentEventDto.setImageId(UUID.randomUUID())
        commentEventDto.setCommentId(UUID.randomUUID())
        commentEventDto.setCreated(true)
        commentEventDto.setContent("Test comment")
        commentEventDto.setTimestamp(LocalDateTime.now())

        `when`(commentEventRepository.save(any(CommentEvent::class.java))).thenThrow(RuntimeException("Test exception"))

        activityConsumer.consumeCommentEvent(commentEventDto)
    }

    private fun createLikeEventDto(userId: UUID, imageId: UUID, added: Boolean): LikeEventDto {
        val dto = LikeEventDto()
        dto.setUserId(userId)
        dto.setImageId(imageId)
        dto.setAdded(added)
        dto.setTimestamp(LocalDateTime.now())
        return dto
    }

    private fun createCommentEventDto(userId: UUID, imageId: UUID, commentId: UUID, created: Boolean, content: String): CommentEventDto {
        val dto = CommentEventDto()
        dto.setUserId(userId)
        dto.setImageId(imageId)
        dto.setCommentId(commentId)
        dto.setCreated(created)
        dto.setContent(content)
        dto.setTimestamp(LocalDateTime.now())
        return dto
    }
}
