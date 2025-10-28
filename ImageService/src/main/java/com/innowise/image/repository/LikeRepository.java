package com.innowise.image.repository;

import com.innowise.image.entity.LikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LikeRepository extends JpaRepository<LikeEntity, UUID> {
    Optional<LikeEntity> findByImageIdAndUserId(UUID imageId, UUID userId);
    int countByImageId(UUID imageId);
}
