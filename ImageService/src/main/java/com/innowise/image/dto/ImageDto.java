package com.innowise.image.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageDto {
    private UUID id;
    private String url;
    private String description;
    private Instant uploadedAt;
    private UUID userId;
    private String authorName;
}
