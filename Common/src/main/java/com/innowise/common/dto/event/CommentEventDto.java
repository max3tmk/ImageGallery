package com.innowise.common.dto.event;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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
    private boolean created;
    private String content;
    private LocalDateTime timestamp;

    public CommentEventDto(UUID userId, UUID imageId, UUID commentId, boolean created, String content) {
        this.userId = userId;
        this.imageId = imageId;
        this.commentId = commentId;
        this.created = created;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }
}
