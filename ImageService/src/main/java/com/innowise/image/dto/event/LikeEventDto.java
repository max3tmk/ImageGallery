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
public class LikeEventDto {
    private UUID userId;
    private UUID imageId;
    private boolean isAdded;
    private LocalDateTime timestamp;

    public LikeEventDto(UUID userId, UUID imageId, boolean isAdded) {
        this(userId, imageId, isAdded, LocalDateTime.now());
    }
}