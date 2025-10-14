package com.innowise.image.service.impl;

import com.innowise.image.service.S3Service;
import com.innowise.image.config.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.net.URI;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;
    private final S3Properties properties;

    @Override
    public String upload(byte[] data, String key, String contentType) throws IOException {
        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(key)
                .contentType(contentType)
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();

        s3Client.putObject(req, RequestBody.fromBytes(data));

        if (properties.endpoint() != null && !properties.endpoint().isBlank()) {
            String endpoint = properties.endpoint();
            return URI.create(endpoint).resolve("/" + properties.bucket() + "/" + key).toString();
        }

        return String.format("https://%s.s3.amazonaws.com/%s", properties.bucket(), key);
    }

    @Override
    public void delete(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(properties.bucket())
                .key(key)
                .build());
    }

    public boolean exists(String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(properties.bucket())
                    .key(key)
                    .build());
            return true;
        } catch (S3Exception e) {
            return false;
        }
    }

    public byte[] download(String key) {
        GetObjectRequest req = GetObjectRequest.builder()
                .bucket(properties.bucket())
                .key(key)
                .build();
        return s3Client.getObjectAsBytes(req).asByteArray();
    }
}