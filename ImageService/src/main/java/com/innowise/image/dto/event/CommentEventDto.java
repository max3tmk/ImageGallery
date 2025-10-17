package com.innowise.image.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentEventDto {
    private UUID userId;
    private UUID imageId;
    private UUID commentId;
    private boolean isCreated;
    private String content;
    private LocalDateTime timestamp = LocalDateTime.now();

    public CommentEventDto(UUID userId, UUID imageId, UUID commentId, boolean isAdded, String content) {
        this(userId, imageId, commentId, isAdded, content, LocalDateTime.now());
    }

}