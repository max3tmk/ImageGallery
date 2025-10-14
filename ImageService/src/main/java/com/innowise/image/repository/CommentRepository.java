package com.innowise.image.repository;

import com.innowise.image.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<CommentEntity, UUID> {

    Optional<CommentEntity> findByIdAndImageIdAndUserIdOrderByCreatedAtDesc(UUID id, UUID imageId, UUID userId);

    List<CommentEntity> findAllByImageIdOrderByCreatedAtDesc(UUID imageId);
}