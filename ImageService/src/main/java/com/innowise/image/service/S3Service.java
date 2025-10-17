package com.innowise.image.service;

import java.io.IOException;

public interface S3Service {
    /**
     * Upload bytes to S3 under provided key and return public URL (or s3://... key)
     */
    String upload(byte[] data, String key, String contentType) throws IOException;

    /**
     * Optional: delete object by key
     */
    void delete(String key);
}
