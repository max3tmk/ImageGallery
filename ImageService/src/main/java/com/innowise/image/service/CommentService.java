package com.innowise.image.service;

import com.innowise.image.dto.CommentDto;
import java.util.List;
import java.util.UUID;

public interface CommentService {
    CommentDto addComment(UUID imageId, UUID userId, CommentDto request);
    List<CommentDto> listComments(UUID imageId);
    CommentDto updateComment(UUID imageId, UUID commentId, UUID userId, CommentDto request);
    void deleteComment(UUID imageId, UUID commentId, UUID userId);
}