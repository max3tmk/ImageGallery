package com.innowise.image.service.impl;

import com.innowise.image.entity.LikeEntity;
import com.innowise.image.repository.LikeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LikeServiceImplTest {

    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private LikeServiceImpl likeService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void toggleLike_addAndRemove() {
        UUID userId = UUID.randomUUID();
        UUID imageId = UUID.randomUUID();

        when(likeRepository.findByImageIdAndUserId(imageId, userId)).thenReturn(Optional.empty());
        likeService.toggleLike(imageId, userId);
        verify(likeRepository, times(1)).save(any());

        LikeEntity like = new LikeEntity();
        when(likeRepository.findByImageIdAndUserId(imageId, userId)).thenReturn(Optional.of(like));
        likeService.toggleLike(imageId, userId);
        verify(likeRepository, times(1)).delete(like);
    }

    @Test
    void countLikes_callsRepository() {
        UUID imageId = UUID.randomUUID();
        likeService.countLikes(imageId);
        verify(likeRepository).countByImageId(imageId);
    }
}