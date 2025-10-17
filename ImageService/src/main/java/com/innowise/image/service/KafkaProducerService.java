package com.innowise.image.service;

import java.util.UUID;

public interface KafkaProducerService {
    void sendLikeEvent(UUID userId, UUID imageId, boolean isAdded);
    void sendCommentEvent(UUID userId, UUID imageId, UUID commentId, String content, boolean isCreated);
}
