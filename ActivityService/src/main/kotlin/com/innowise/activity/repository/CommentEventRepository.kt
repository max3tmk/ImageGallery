package com.innowise.activity.repository

import com.innowise.activity.model.CommentEvent
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CommentEventRepository : MongoRepository<CommentEvent, UUID>
