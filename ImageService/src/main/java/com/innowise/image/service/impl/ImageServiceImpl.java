package com.innowise.image.service.impl;

import com.innowise.image.dto.ImageDto;
import com.innowise.image.dto.UploadResponseDto;
import com.innowise.image.entity.ImageEntity;
import com.innowise.image.exception.NotFoundException;
import com.innowise.image.repository.ImageRepository;
import com.innowise.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final ModelMapper modelMapper;
    private final S3Client s3Client;

    @Value("${s3.bucket}")
    private String s3Bucket;

    @Override
    public UploadResponseDto uploadImage(MultipartFile file, UUID userId, String description) throws IOException {
        String key = UUID.randomUUID() + "-" + file.getOriginalFilename();

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(s3Bucket)
                        .key(key)
                        .contentType(file.getContentType())
                        .checksumAlgorithm((String) null)
                        .build(),
                software.amazon.awssdk.core.sync.RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        String fileUrl = s3Client.utilities()
                .getUrl(b -> b.bucket(s3Bucket).key(key))
                .toString();

        ImageEntity image = new ImageEntity();
        image.setUrl(fileUrl);
        image.setDescription(description);
        image.setUploadedAt(Instant.now());
        image.setUserId(userId);
        imageRepository.save(image);

        UploadResponseDto response = new UploadResponseDto();
        response.setId(image.getId());
        response.setUrl(image.getUrl());
        return response;
    }

    @Override
    public ImageDto getImage(UUID id) {
        ImageEntity image = imageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Image not found with id: " + id));
        return modelMapper.map(image, ImageDto.class);
    }

    @Override
    public Page<ImageDto> getUserImages(UUID userId, Pageable pageable) {
        return imageRepository.findByUserIdOrderByUploadedAtDesc(userId, pageable)
                .map(img -> modelMapper.map(img, ImageDto.class));
    }

    @Override
    public Page<ImageDto> getAllImages(Pageable pageable) {
        return imageRepository.findAllByOrderByUploadedAtDesc(pageable)
                .map(img -> modelMapper.map(img, ImageDto.class));
    }

    @Override
    public byte[] getImageContent(UUID imageId) {
        ResponseInputStream<GetObjectResponse> response = s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(s3Bucket)
                        .key(imageId.toString())
                        .build()
        );
        try {
            return response.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read image from S3", e);
        }
    }

    @Override
    public MediaType getMediaType(UUID imageId) {
        ImageEntity image = imageRepository.findById(imageId).orElseThrow(() -> new NotFoundException("Image not found"));
        try {
            String lower = image.getUrl().toLowerCase();
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
            if (lower.endsWith(".png")) return MediaType.IMAGE_PNG;
            if (lower.endsWith(".gif")) return MediaType.IMAGE_GIF;
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        catch (Exception e) {
            throw new NotFoundException("Image not found");
        }
    }
}