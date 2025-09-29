package com.innowise.image.service;

import com.innowise.image.config.S3Properties;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final S3Properties properties;

    public S3Service(S3Client s3Client, S3Properties properties) {
        this.s3Client = s3Client;
        this.properties = properties;
    }

    public void upload(String key, byte[] content) {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(properties.bucket())
                        .key(key)
                        .build(),
                RequestBody.fromBytes(content)
        );
    }

    public ResponseInputStream<GetObjectResponse> download(String key) {
        return s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(properties.bucket())
                        .key(key)
                        .build()
        );
    }

    public void delete(String key) {
        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(properties.bucket())
                        .key(key)
                        .build()
        );
    }
}
