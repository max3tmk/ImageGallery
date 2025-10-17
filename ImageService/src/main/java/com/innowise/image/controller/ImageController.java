package com.innowise.image.controller;

import com.innowise.common.security.JwtUtil;
import com.innowise.image.dto.*;
import com.innowise.image.exception.UnauthorizedException;
import com.innowise.image.service.CommentService;
import com.innowise.image.service.ImageService;
import com.innowise.image.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ImageController {

    private final JwtUtil jwtUtil;
    private final LikeService likeService;
    private final ImageService imageService;
    private final CommentService commentService;

    // ---------------------- IMAGES ----------------------

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponseDto> uploadImage(
            @RequestHeader(name = "Authorization", required = false) String authHeader,
            @RequestPart("file") MultipartFile file,
            @RequestPart(name = "description", required = false) String description
    ) throws IOException {

        UUID userId = extractUserIdFromAuthHeader(authHeader);
        UploadResponseDto dto = imageService.uploadImage(file, userId, description);
        return ResponseEntity
                .created(URI.create("/api/images/" + dto.getId()))
                .body(dto);
    }

    @GetMapping("/images/{id}")
    public ResponseEntity<ImageDto> getImage(@PathVariable("id") UUID id) {
        ImageDto dto = imageService.getImage(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/user/{id}/images")
    public ResponseEntity<PageResponseDto<ImageDto>> getUserImages(@PathVariable("id") UUID id, Pageable pageable) {

        Page<ImageDto> page = imageService.getUserImages(id, pageable);
        PageResponseDto<ImageDto> resp = PageResponseDto.<ImageDto>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/images")
    public ResponseEntity<PageResponseDto<ImageDto>> getAllImages(Pageable pageable) {
        Page<ImageDto> page = imageService.getAllImages(pageable);

        PageResponseDto<ImageDto> resp = PageResponseDto.<ImageDto>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();

        return ResponseEntity.ok(resp);
    }

    // ---------------------- LIKES ----------------------

    @PostMapping("/images/{id}/likes")
    public ResponseEntity<Void> toggleLike(
            @RequestHeader(name = "Authorization", required = false) String authHeader,
            @PathVariable("id") UUID imageId
    ) {
        UUID userId = extractUserIdFromAuthHeader(authHeader);
        likeService.toggleLike(imageId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/images/{id}/likes/count")
    public ResponseEntity<LikesCountDto> countLikes(@PathVariable("id") UUID imageId) {
        return ResponseEntity.ok(new LikesCountDto(likeService.countLikes(imageId)));
    }

    // ---------------------- COMMENTS ----------------------

    @PostMapping("/images/{id}/comments")
    public ResponseEntity<CommentDto> addComment(
            @RequestHeader(name = "Authorization", required = false) String authHeader,
            @PathVariable("id") UUID imageId,
            @RequestBody CommentDto request
    ) {
        UUID userId = extractUserIdFromAuthHeader(authHeader);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.addComment(imageId, userId, request));
    }

    @GetMapping("/images/{id}/comments")
    public ResponseEntity<List<CommentDto>> listComments(@PathVariable("id") UUID imageId) {
        return ResponseEntity.ok(commentService.listComments(imageId));
    }

    @PutMapping("/images/{id}/comments/{commentId}")
    public ResponseEntity<CommentDto> updateComment(
            @RequestHeader(name = "Authorization", required = false) String authHeader,
            @PathVariable("id") UUID imageId,
            @PathVariable("commentId") UUID commentId,
            @RequestBody CommentDto request
    ) {
        UUID userId = extractUserIdFromAuthHeader(authHeader);
        return ResponseEntity.ok(commentService.updateComment(imageId, commentId, userId, request));
    }

    @DeleteMapping("/images/{id}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @RequestHeader(name = "Authorization", required = false) String authHeader,
            @PathVariable("id") UUID imageId,
            @PathVariable("commentId") UUID commentId
    ) {
        UUID userId = extractUserIdFromAuthHeader(authHeader);
        commentService.deleteComment(imageId, commentId, userId);
        return ResponseEntity.noContent().build();
    }

    // ---------------------- UTILS ----------------------

    private UUID extractUserIdFromAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        UUID userId = jwtUtil.extractUserId(token);
        if (userId == null) throw new UnauthorizedException("Cannot extract user id from token");
        return userId;
    }
}