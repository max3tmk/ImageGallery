package com.innowise.image.service.impl;

import com.innowise.common.dto.event.CommentEventDto;
import com.innowise.common.dto.event.LikeEventDto;
import com.innowise.image.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private static final String LIKE_TOPIC = "image-like-events";
    private static final String COMMENT_TOPIC = "image-comment-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendLikeEvent(UUID userId, UUID imageId, boolean isAdded) {
        LikeEventDto event = new LikeEventDto(userId, imageId, isAdded);

        kafkaTemplate.send(LIKE_TOPIC, userId.toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Like event sent: {} for user {} on image {}",
                                isAdded ? "ADDED" : "REMOVED", userId, imageId);
                    } else {
                        log.error("Failed to send like event: {}", ex.getMessage(), ex);
                    }
                });
    }

    public void sendCommentEvent(UUID userId, UUID imageId, UUID commentId, String content, boolean isCreated) {
        CommentEventDto event = new CommentEventDto(userId, imageId, commentId, isCreated, content);

        kafkaTemplate.send(COMMENT_TOPIC, userId.toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Comment event sent: {} for user {} on image {}",
                                isCreated ? "CREATED" : "DELETED", userId, imageId);
                    } else {
                        log.error("Failed to send comment event: {}", ex.getMessage(), ex);
                    }
                });
    }
}
