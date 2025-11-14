package com.innowise.image.service.impl;

import com.innowise.image.dto.ImageDto;
import com.innowise.image.dto.UploadResponseDto;
import com.innowise.image.entity.ImageEntity;
import com.innowise.image.exception.NotFoundException;
import com.innowise.image.repository.ImageRepository;
import com.innowise.image.service.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ImageServiceImplTest {

    private ImageRepository imageRepository;
    private S3Client s3Client;
    private ImageService imageService;

    @BeforeEach
    void setUp() {
        imageRepository = mock(ImageRepository.class);
        s3Client = mock(S3Client.class);
        ModelMapper modelMapper = new ModelMapper();
        imageService = new ImageServiceImpl(imageRepository, modelMapper, s3Client);
    }

    @Test
    void uploadImage_success() throws Exception {
        UUID userId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "abcd".getBytes());

        when(s3Client.putObject(any(PutObjectRequest.class), any(software.amazon.awssdk.core.sync.RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        UUID generatedId = UUID.randomUUID();
        when(imageRepository.save(any(ImageEntity.class))).thenAnswer(inv -> {
            ImageEntity entity = inv.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(generatedId);
            }
            entity.setUrl("http://localhost:4566/images/d814675e-05f5-4aae-9eb4-c9f25cf1b4cb-photo.jpg");
            return entity;
        });

        UploadResponseDto resp = imageService.uploadImage(file, userId, "desc");

        assertNotNull(resp);
        assertEquals(generatedId, resp.getId());
        assertTrue(resp.getUrl().startsWith("http://localhost:4566/images/"));

        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(software.amazon.awssdk.core.sync.RequestBody.class));
        verify(imageRepository, times(1)).save(any(ImageEntity.class));
    }

    @Test
    void uploadImage_failure_s3Throws() {
        UUID userId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "abcd".getBytes());

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(new RuntimeException("S3 error"));

        assertThrows(RuntimeException.class, () -> imageService.uploadImage(file, userId, "desc"));
        verify(imageRepository, never()).save(any());
    }

    @Test
    void getImage_success() {
        UUID id = UUID.randomUUID();
        ImageEntity entity = new ImageEntity();
        entity.setId(id);
        entity.setUrl("http://example.com/img.png");
        when(imageRepository.findById(id)).thenReturn(Optional.of(entity));

        ImageDto dto = imageService.getImage(id);
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals("http://example.com/img.png", dto.getUrl());
    }

    @Test
    void getImage_notFound() {
        UUID id = UUID.randomUUID();
        when(imageRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> imageService.getImage(id));
    }

    @Test
    void getUserImages_success() {
        UUID userId = UUID.randomUUID();
        ImageEntity entity = new ImageEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(userId);
        entity.setUrl("http://example.com/u.png");

        when(imageRepository.findByUserIdOrderByUploadedAtDesc(eq(userId), any()))
                .thenReturn(new PageImpl<>(List.of(entity)));

        Page<ImageDto> page = imageService.getUserImages(userId, PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals(userId, page.getContent().get(0).getUserId());
    }

    @Test
    void getUserImages_empty() {
        UUID userId = UUID.randomUUID();
        when(imageRepository.findByUserIdOrderByUploadedAtDesc(eq(userId), any()))
                .thenReturn(Page.empty());

        Page<ImageDto> page = imageService.getUserImages(userId, PageRequest.of(0, 10));

        assertTrue(page.isEmpty());
    }

    @Test
    void getAllImages_success() {
        ImageEntity entity = new ImageEntity();
        entity.setId(UUID.randomUUID());
        entity.setUrl("http://example.com/all.png");

        when(imageRepository.findAllByOrderByUploadedAtDesc(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        Page<ImageDto> page = imageService.getAllImages(PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
    }

    @Test
    void getAllImages_empty() {
        when(imageRepository.findAllByOrderByUploadedAtDesc(any(PageRequest.class)))
                .thenReturn(Page.empty());

        Page<ImageDto> page = imageService.getAllImages(PageRequest.of(0, 10));

        assertTrue(page.isEmpty());
    }
}