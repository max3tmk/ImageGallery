package com.innowise.activity.consumer

import com.innowise.activity.repository.ActivityRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.support.Acknowledgment
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Instant
import java.util.UUID

@ExtendWith(SpringExtension::class)
@SpringBootTest
class ActivityConsumerTest {

    private val repository: ActivityRepository = mock()
    private val consumer = ActivityConsumer(repository)

    @Test
    fun `should save activity on consume`() {
        val event = ActivityEvent(
            id = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            imageId = UUID.randomUUID(),
            type = "create_comment",
            status = true,
            createdAt = Instant.now()
        )

        val ack: Acknowledgment = mock()

        consumer.consume(event, ack)

        verify(repository).save(event)
        verify(ack).acknowledge()
    }
}