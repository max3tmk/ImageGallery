package com.innowise.image.service.impl;

import com.innowise.image.config.S3Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class S3ServiceImplTest {

    @Mock
    private S3Client s3Client;
    @Mock
    private S3Properties properties;

    @InjectMocks
    private S3ServiceImpl s3Service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        when(properties.bucket()).thenReturn("bucket");
        when(properties.endpoint()).thenReturn("http://localhost");
    }

    @Test
    void upload_returnsUrl() throws IOException {
        byte[] data = "data".getBytes();
        String key = "key";
        String contentType = "text/plain";

        String url = s3Service.upload(data, key, contentType);
        assertNotNull(url);
    }

    @Test
    void exists_returnsFalseOnException() {
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenThrow(S3Exception.builder().build());
        assertFalse(s3Service.exists("key"));
    }
}