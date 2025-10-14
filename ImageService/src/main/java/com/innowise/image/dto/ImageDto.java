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
public class ImageDto {
    private UUID id;
    private String url;
    private String description;
    private Instant uploadedAt;
    private UUID userId;
    private String authorName;
}
