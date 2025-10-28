package com.innowise.image.service;

import com.innowise.image.dto.ImageDto;
import com.innowise.image.dto.UploadResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface ImageService {
    UploadResponseDto uploadImage(MultipartFile file, UUID userId, String description) throws IOException;
    ImageDto getImage(UUID id);
    Page<ImageDto> getUserImages(UUID userId, Pageable pageable);
    Page<ImageDto> getAllImages(Pageable pageable);
}