package com.innowise.image.service.impl;

import com.innowise.image.entity.LikeEntity;
import com.innowise.image.repository.LikeRepository;
import com.innowise.image.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;

    @Override
    public void toggleLike(UUID imageId, UUID userId) {
        LikeEntity existing = likeRepository.findByImageIdAndUserId(imageId, userId).orElse(null);
        if (existing != null) {
            likeRepository.delete(existing);
        } else {
            LikeEntity like = LikeEntity.builder()
                    .imageId(imageId)
                    .userId(userId)
                    .createdAt(Instant.now())
                    .build();
            likeRepository.save(like);
        }
    }

    @Override
    public int countLikes(UUID imageId) {
        return likeRepository.countByImageId(imageId);
    }
}