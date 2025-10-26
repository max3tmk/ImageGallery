package com.innowise.image.service;

import java.io.IOException;

public interface S3Service {
    String upload(byte[] data, String key, String contentType) throws IOException;
    void delete(String key);
}
