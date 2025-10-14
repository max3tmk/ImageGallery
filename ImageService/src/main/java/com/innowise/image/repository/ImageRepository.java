package com.innowise.image.repository;

import com.innowise.image.entity.ImageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ImageRepository extends JpaRepository<ImageEntity, UUID> {
    Page<ImageEntity> findByUserIdOrderByUploadedAtDesc(UUID userId, Pageable pageable);
    Page<ImageEntity> findAllByOrderByUploadedAtDesc(Pageable pageable);
}
