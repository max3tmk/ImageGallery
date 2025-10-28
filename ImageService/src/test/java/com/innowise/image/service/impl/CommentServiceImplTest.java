package com.innowise.image.service.impl;

import com.innowise.image.client.AuthServiceClient;
import com.innowise.image.dto.CommentDto;
import com.innowise.image.entity.CommentEntity;
import com.innowise.image.exception.NotFoundException;
import com.innowise.image.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private CommentServiceImpl commentService;

    private UUID imageId;
    private UUID userId;
    private UUID commentId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        imageId = UUID.randomUUID();
        userId = UUID.randomUUID();
        commentId = UUID.randomUUID();
    }

    @Test
    void addComment_success() {
        CommentDto request = new CommentDto();
        request.setContent("Hello");

        CommentEntity savedEntity = new CommentEntity();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setImageId(imageId);
        savedEntity.setUserId(userId);
        savedEntity.setContent("Hello");

        when(commentRepository.save(any(CommentEntity.class))).thenReturn(savedEntity);
        when(modelMapper.map(any(CommentEntity.class), eq(CommentDto.class)))
                .thenAnswer(invocation -> {
                    CommentEntity entity = invocation.getArgument(0);
                    CommentDto dto = new CommentDto();
                    dto.setId(entity.getId());
                    dto.setContent(entity.getContent());
                    return dto;
                });

        CommentDto result = commentService.addComment(imageId, userId, request);

        assertNotNull(result);
        assertEquals("Hello", result.getContent());
        verify(commentRepository, times(1)).save(any(CommentEntity.class));
        verify(modelMapper, times(1)).map(any(CommentEntity.class), eq(CommentDto.class));
    }

    @Test
    void updateComment_success() {
        CommentDto request = new CommentDto();
        request.setContent("Updated");

        CommentEntity existing = new CommentEntity();
        existing.setId(commentId);
        existing.setImageId(imageId);
        existing.setUserId(userId);
        existing.setContent("Old");

        when(commentRepository.findByIdAndImageIdAndUserIdOrderByCreatedAtDesc(commentId, imageId, userId))
                .thenReturn(Optional.of(existing));
        when(modelMapper.map(any(CommentEntity.class), eq(CommentDto.class)))
                .thenAnswer(invocation -> {
                    CommentEntity entity = invocation.getArgument(0);
                    CommentDto dto = new CommentDto();
                    dto.setId(entity.getId());
                    dto.setContent(entity.getContent());
                    return dto;
                });

        CommentDto result = commentService.updateComment(imageId, commentId, userId, request);

        assertNotNull(result);
        assertEquals("Updated", result.getContent());
        verify(commentRepository, times(1)).save(existing);
    }

    @Test
    void updateComment_notFound() {
        when(commentRepository.findByIdAndImageIdAndUserIdOrderByCreatedAtDesc(commentId, imageId, userId))
                .thenReturn(Optional.empty());

        CommentDto commentDto = new CommentDto();
        assertThrows(NotFoundException.class,
                () -> commentService.updateComment(imageId, commentId, userId, commentDto));
    }

    @Test
    void deleteComment_success() {
        CommentEntity existing = new CommentEntity();
        existing.setId(commentId);
        existing.setImageId(imageId);
        existing.setUserId(userId);

        when(commentRepository.findByIdAndImageIdAndUserIdOrderByCreatedAtDesc(commentId, imageId, userId))
                .thenReturn(Optional.of(existing));
        commentService.deleteComment(imageId, commentId, userId);

        verify(commentRepository, times(1)).delete(existing);
    }

    @Test
    void deleteComment_notFound() {
        when(commentRepository.findByIdAndImageIdAndUserIdOrderByCreatedAtDesc(commentId, imageId, userId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> commentService.deleteComment(imageId, commentId, userId));
    }

    @Test
    void listComments_success() {
        CommentEntity c1 = new CommentEntity();
        c1.setId(UUID.randomUUID());
        c1.setContent("c1");
        CommentEntity c2 = new CommentEntity();
        c2.setId(UUID.randomUUID());
        c2.setContent("c2");

        List<CommentEntity> list = List.of(c1, c2);

        when(commentRepository.findAllByImageIdOrderByCreatedAtDesc(imageId)).thenReturn(list);
        when(modelMapper.map(any(CommentEntity.class), eq(CommentDto.class)))
                .thenAnswer(invocation -> {
                    CommentEntity entity = invocation.getArgument(0);
                    CommentDto dto = new CommentDto();
                    dto.setId(entity.getId());
                    dto.setContent(entity.getContent());
                    return dto;
                });

        List<CommentDto> result = commentService.listComments(imageId);

        assertEquals(2, result.size());
        assertEquals("c1", result.get(0).getContent());
        assertEquals("c2", result.get(1).getContent());
    }
}