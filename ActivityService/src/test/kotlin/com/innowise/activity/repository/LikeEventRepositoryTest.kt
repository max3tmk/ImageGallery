package com.innowise.activity.repository

import com.innowise.activity.model.LikeEvent
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
class LikeEventRepositoryTest {

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
    private lateinit var likeEventRepository: LikeEventRepository

    @BeforeEach
    fun cleanUp() {
        likeEventRepository.deleteAll()
    }

    @Test
    fun `should save and find like event`() {
        val likeEvent = LikeEvent(
            userId = UUID.randomUUID(),
            imageId = UUID.randomUUID(),
            isAdded = true,
            timestamp = LocalDateTime.now()
        )

        val savedEvent = likeEventRepository.save(likeEvent)
        val foundEvent = likeEventRepository.findById(savedEvent.id)

        assertNotNull(savedEvent)
        assertTrue(foundEvent.isPresent)
        assertEquals(likeEvent.userId, foundEvent.get().userId)
        assertEquals(likeEvent.imageId, foundEvent.get().imageId)
        assertEquals(likeEvent.isAdded, foundEvent.get().isAdded)
    }

    @Test
    fun `should find all like events`() {
        val likeEvent1 = LikeEvent(
            userId = UUID.randomUUID(),
            imageId = UUID.randomUUID(),
            isAdded = true,
            timestamp = LocalDateTime.now()
        )
        val likeEvent2 = LikeEvent(
            userId = UUID.randomUUID(),
            imageId = UUID.randomUUID(),
            isAdded = false,
            timestamp = LocalDateTime.now()
        )

        likeEventRepository.save(likeEvent1)
        likeEventRepository.save(likeEvent2)

        val allEvents = likeEventRepository.findAll()

        assertEquals(2, allEvents.size)
    }
}
