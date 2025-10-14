package com.innowise.image.service;

import java.util.UUID;

public interface LikeService {
    void toggleLike(UUID imageId, UUID userId);
    int countLikes(UUID imageId);
}