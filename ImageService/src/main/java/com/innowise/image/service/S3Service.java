package com.innowise.image.service;

public interface S3Service {
    String upload(byte[] data, String key, String contentType);
    void delete(String key);
}
