package com.innowise.image.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {
    private UUID id;
    private UUID imageId;
    private UUID userId;
    private String authorName;
    private String content;
    private Instant createdAt;
}
