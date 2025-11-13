package com.innowise.activity.repository

import com.innowise.activity.model.CommentEvent
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@DataMongoTest
@Testcontainers
class CommentEventRepositoryTest {

    companion object {
        @Container
        val mongoDBContainer = MongoDBContainer(DockerImageName.parse("mongo:6.0"))
            .withExposedPorts(27017)

        @JvmStatic
        @DynamicPropertySource
        fun setProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl)
        }
    }

    @Autowired
    private lateinit var commentEventRepository: CommentEventRepository

    @BeforeEach
    fun cleanUp() {
        commentEventRepository.deleteAll()
    }

    @Test
    fun `should save and find comment event`() {
        val commentEvent = CommentEvent(
            userId = UUID.randomUUID(),
            imageId = UUID.randomUUID(),
            commentId = UUID.randomUUID(),
            isCreated = true,
            content = "Test comment",
            timestamp = LocalDateTime.now()
        )

        val savedEvent = commentEventRepository.save(commentEvent)
        val foundEvent = commentEventRepository.findById(savedEvent.id)

        assertNotNull(savedEvent)
        assertTrue(foundEvent.isPresent)
        assertEquals(commentEvent.userId, foundEvent.get().userId)
        assertEquals(commentEvent.imageId, foundEvent.get().imageId)
        assertEquals(commentEvent.commentId, foundEvent.get().commentId)
        assertEquals(commentEvent.content, foundEvent.get().content)
    }

    @Test
    fun `should find all comment events`() {
        val commentEvent1 = CommentEvent(
            userId = UUID.randomUUID(),
            imageId = UUID.randomUUID(),
            commentId = UUID.randomUUID(),
            isCreated = true,
            content = "Comment 1",
            timestamp = LocalDateTime.now()
        )
        val commentEvent2 = CommentEvent(
            userId = UUID.randomUUID(),
            imageId = UUID.randomUUID(),
            commentId = UUID.randomUUID(),
            isCreated = false,
            content = "Comment 2",
            timestamp = LocalDateTime.now()
        )

        commentEventRepository.save(commentEvent1)
        commentEventRepository.save(commentEvent2)

        val allEvents = commentEventRepository.findAll()

        assertEquals(2, allEvents.size)
    }
}
