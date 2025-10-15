package com.innowise.image.service.impl;

import com.innowise.image.client.AuthServiceClient;
import com.innowise.image.dto.CommentDto;
import com.innowise.image.entity.CommentEntity;
import com.innowise.image.exception.NotFoundException;
import com.innowise.image.repository.CommentRepository;
import com.innowise.image.service.CommentService;
import com.innowise.image.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final KafkaProducerService kafkaProducerService;
    private final AuthServiceClient authServiceClient;
    private final ModelMapper modelMapper;

    @Override
    public CommentDto addComment(UUID imageId, UUID userId, CommentDto request) {
        CommentEntity comment = new CommentEntity();
        comment.setImageId(imageId);
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setCreatedAt(Instant.now());
        CommentEntity savedComment = commentRepository.save(comment);
        kafkaProducerService.sendCommentEvent(userId, imageId, savedComment.getId(), request.getContent(), true);

        CommentDto commentDto = modelMapper.map(comment, CommentDto.class);
        commentDto.setAuthorName(authServiceClient.getUsernameById(userId));
        return commentDto;
    }

    @Override
    public CommentDto updateComment(UUID imageId, UUID commentId, UUID userId, CommentDto request) {
        CommentEntity comment = commentRepository.findByIdAndImageIdAndUserIdOrderByCreatedAtDesc(commentId, imageId, userId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));
        comment.setContent(request.getContent());
        commentRepository.save(comment);
        CommentDto commentDto = modelMapper.map(comment, CommentDto.class);
        commentDto.setAuthorName(authServiceClient.getUsernameById(userId));
        return commentDto;
    }

    @Override
    public void deleteComment(UUID imageId, UUID commentId, UUID userId) {
        CommentEntity comment = commentRepository.findByIdAndImageIdAndUserIdOrderByCreatedAtDesc(commentId, imageId, userId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));
        String content = comment.getContent();
        commentRepository.delete(comment);
        kafkaProducerService.sendCommentEvent(userId, imageId, commentId, content, false);
    }

    @Override
    public List<CommentDto> listComments(UUID imageId) {
        List<CommentEntity> comments = commentRepository.findAllByImageIdOrderByCreatedAtDesc(imageId);
        return comments.stream()
                .map(this::toCommentDto)
                .toList();
    }

    private CommentDto toCommentDto(CommentEntity comment) {
        CommentDto dto = modelMapper.map(comment, CommentDto.class);
        dto.setAuthorName(authServiceClient.getUsernameById(comment.getUserId()));
        return dto;
    }

    private CommentDto mapCommentToDto(CommentEntity comment) {
        CommentDto dto = modelMapper.map(comment, CommentDto.class);
        enrichWithAuthorName(dto, comment.getUserId());
        return dto;
    }

    private void enrichWithAuthorName(CommentDto dto, UUID userId) {
        String authorName = authServiceClient.getUsernameById(userId);
        dto.setAuthorName(authorName);
    }}