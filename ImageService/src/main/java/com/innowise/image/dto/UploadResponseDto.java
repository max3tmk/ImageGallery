package com.innowise.image.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadResponseDto {
    private UUID id;
    private String url;
}
