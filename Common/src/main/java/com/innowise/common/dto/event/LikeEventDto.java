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
public class LikeEventDto {
    private UUID userId;
    private UUID imageId;
    private boolean added;
    private LocalDateTime timestamp;

    public LikeEventDto(UUID userId, UUID imageId, boolean added) {
        this.userId = userId;
        this.imageId = imageId;
        this.added = added;
        this.timestamp = LocalDateTime.now();
    }
}
