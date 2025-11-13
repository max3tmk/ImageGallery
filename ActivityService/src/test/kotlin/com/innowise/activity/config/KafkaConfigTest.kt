package com.innowise.activity.config

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.ConsumerFactory
import kotlin.test.assertNotNull

@SpringBootTest(properties = ["spring.kafka.bootstrap-servers=localhost:9092"])
class KafkaConfigTest {

    @Autowired
    private lateinit var consumerFactory: ConsumerFactory<String, Any>

    @Test
    fun `consumerFactory should be created`() {
        assertNotNull(consumerFactory)
    }
}
