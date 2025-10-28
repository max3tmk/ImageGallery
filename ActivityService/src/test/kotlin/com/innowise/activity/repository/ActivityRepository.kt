package com.innowise.activity.repository

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Instant
import java.util.UUID

@ExtendWith(SpringExtension::class)
@DataMongoTest
class ActivityRepositoryTest @Autowired constructor(
    val repository: ActivityRepository
) {

    @Test
    fun `should save and retrieve activity`() {
        val activity = ActivityEvent(
            id = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            imageId = UUID.randomUUID(),
            type = "add_like",
            status = true,
            createdAt = Instant.now()
        )

        repository.save(activity)

        val found = repository.findById(activity.id).orElseThrow()
        assertEquals(activity.userId, found.userId)
        assertEquals(activity.type, found.type)
    }
}