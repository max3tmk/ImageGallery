package com.innowise.image.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.common.security.JwtUtil;
import com.innowise.image.dto.CommentDto;
import com.innowise.image.dto.ImageDto;
import com.innowise.image.dto.UploadResponseDto;
import com.innowise.image.exception.ConflictException;
import com.innowise.image.exception.ForbiddenException;
import com.innowise.image.exception.GlobalExceptionHandler;
import com.innowise.image.exception.NotFoundException;
import com.innowise.image.service.CommentService;
import com.innowise.image.service.ImageService;
import com.innowise.image.service.LikeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ImageControllerTest {

    private MockMvc mockMvc;
    private JwtUtil jwtUtil;
    private LikeService likeService;
    private ImageService imageService;
    private CommentService commentService;
    private ObjectMapper objectMapper;
    private UUID userId;
    private String authHeader;

    @BeforeEach
    void setup() {
        jwtUtil = Mockito.mock(JwtUtil.class);
        likeService = Mockito.mock(LikeService.class);
        imageService = Mockito.mock(ImageService.class);
        commentService = Mockito.mock(CommentService.class);
        objectMapper = new ObjectMapper();

        ImageController imageController = new ImageController(jwtUtil, likeService, imageService, commentService);

        mockMvc = MockMvcBuilders.standaloneSetup(imageController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        userId = UUID.randomUUID();
        authHeader = "Bearer valid-token";

        lenient().when(jwtUtil.extractUserId(anyString())).thenReturn(userId);
    }

    @Test
    void uploadImage_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg",
                MediaType.IMAGE_JPEG_VALUE, "fake-image".getBytes());

        UploadResponseDto response = new UploadResponseDto();
        response.setId(UUID.randomUUID());
        response.setUrl("http://localhost/images/" + response.getId());

        when(imageService.uploadImage(any(), eq(userId), any())).thenReturn(response);

        mockMvc.perform(multipart("/api/images")
                        .file(file)
                        .param("description", "test image")
                        .header(HttpHeaders.AUTHORIZATION, authHeader))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.getId().toString()))
                .andExpect(jsonPath("$.url").value(response.getUrl()));
    }

    @Test
    void uploadImage_missingAuth_shouldFail() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg",
                MediaType.IMAGE_JPEG_VALUE, "fake-image".getBytes());

        mockMvc.perform(multipart("/api/images")
                        .file(file))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Missing or invalid Authorization header"));
    }

    @Test
    void uploadImage_serviceFails_shouldFail_serverError() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg",
                MediaType.IMAGE_JPEG_VALUE, "dummy-content".getBytes());
        when(imageService.uploadImage(any(), any(), any()))
                .thenThrow(new RuntimeException("Internal server error"));

        mockMvc.perform(multipart("/api/images")
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, authHeader))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void uploadImage_conflict_shouldReturn409() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "dup.jpg",
                MediaType.IMAGE_JPEG_VALUE, "duplicate".getBytes());

        when(imageService.uploadImage(any(), any(), any()))
                .thenThrow(new ConflictException("Image already exists"));

        mockMvc.perform(multipart("/api/images")
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, authHeader))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Image already exists"));
    }

    @Test
    void getImage_success() throws Exception {
        UUID imageId = UUID.randomUUID();
        ImageDto dto = new ImageDto();
        dto.setId(imageId);
        dto.setUrl("http://example.com/img.jpg");
        dto.setDescription("desc");
        dto.setUploadedAt(Instant.now());
        dto.setUserId(userId);

        when(imageService.getImage(imageId)).thenReturn(dto);

        mockMvc.perform(get("/api/images/{id}", imageId)
                        .header(HttpHeaders.AUTHORIZATION, authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(imageId.toString()))
                .andExpect(jsonPath("$.url").value("http://example.com/img.jpg"))
                .andExpect(jsonPath("$.description").value("desc"));
    }

    @Test
    void getImage_notFound_shouldReturn404() throws Exception {
        UUID imageId = UUID.randomUUID();
        when(imageService.getImage(imageId)).thenThrow(new NotFoundException("Image not found"));

        mockMvc.perform(get("/api/images/{id}", imageId)
                        .header(HttpHeaders.AUTHORIZATION, authHeader))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Image not found"));
    }

    @Test
    void getImage_forbiddenAccess_shouldReturn403() throws Exception {
        UUID imageId = UUID.randomUUID();
        when(imageService.getImage(imageId)).thenThrow(new ForbiddenException("Access denied"));

        mockMvc.perform(get("/api/images/{id}", imageId)
                        .header(HttpHeaders.AUTHORIZATION, authHeader))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void getUserImages_success() throws Exception {
        ImageDto dto = new ImageDto();
        dto.setId(UUID.randomUUID());
        dto.setUserId(userId);
        dto.setUrl("http://img.local/test");
        dto.setUploadedAt(Instant.now());

        Page<ImageDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);

        when(imageService.getUserImages(eq(userId), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/user/{id}/images", userId)
                .param("page", "0")
                .param("size", "20")
                        .header(HttpHeaders.AUTHORIZATION, authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(dto.getId().toString()))
                .andExpect(jsonPath("$.content[0].url").value(dto.getUrl()));
    }

    @Test
    void getUserImages_invalidUser_shouldReturn404() throws Exception {
        when(imageService.getUserImages(any(UUID.class), any(Pageable.class)))
                .thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(get("/api/user/{id}/images", UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, authHeader))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void getUserImages_missingAuth_shouldReturn500() throws Exception {
        mockMvc.perform(get("/api/user/{id}/images", userId))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void getAllImages_success() throws Exception {
        ImageDto dto = new ImageDto();
        dto.setId(UUID.randomUUID());
        dto.setUrl("http://img.local/test2");
        dto.setUploadedAt(Instant.now());

        Page<ImageDto> page = new PageImpl<>(List.of(dto));

        when(imageService.getAllImages(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/images")
                        .param("page", "0")
                        .param("size", "10")
                        .header(HttpHeaders.AUTHORIZATION, authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(dto.getId().toString()))
                .andExpect(jsonPath("$.content[0].url").value("http://img.local/test2"));
    }

    @Test
    void getAllImages_internalError() throws Exception {
        when(imageService.getAllImages(any(Pageable.class)))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/images")
                        .header(HttpHeaders.AUTHORIZATION, authHeader))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void getAllImages_missingAuth_shouldReturn500() throws Exception {
        mockMvc.perform(get("/api/images"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void toggleLike_success() throws Exception {
        mockMvc.perform(post("/api/images/{id}/likes", UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, authHeader))
                .andExpect(status().isNoContent());
    }

    @Test
    void toggleLike_missingAuth_shouldFail() throws Exception {
        mockMvc.perform(post("/api/images/{id}/likes", UUID.randomUUID()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Missing or invalid Authorization header"));
    }

    @Test
    void countLikes_success() throws Exception {
        UUID imageId = UUID.randomUUID();
        when(likeService.countLikes(imageId)).thenReturn(5);

        mockMvc.perform(get("/api/images/{id}/likes/count", imageId)
                        .header(HttpHeaders.AUTHORIZATION, authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likesCount").value(5));
    }

    @Test
    void addComment_success() throws Exception {
        UUID imageId = UUID.randomUUID();
        CommentDto request = new CommentDto();
        request.setContent("Hello");

        CommentDto response = new CommentDto();
        response.setContent("Hello");
        when(commentService.addComment(eq(imageId), eq(userId), any(CommentDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/images/{id}/comments", imageId)
                        .header(HttpHeaders.AUTHORIZATION, authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Hello"));
    }

    @Test
    void addComment_missingAuth_shouldFail() throws Exception {
        UUID imageId = UUID.randomUUID();
        CommentDto request = new CommentDto();
        request.setContent("Hello");

        mockMvc.perform(post("/api/images/{id}/comments", imageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Missing or invalid Authorization header"));
    }

    @Test
    void addComment_notFound_shouldReturn404() throws Exception {
        UUID imageId = UUID.randomUUID();
        CommentDto request = new CommentDto();
        request.setContent("Missing");

        when(commentService.addComment(eq(imageId), eq(userId), any(CommentDto.class)))
                .thenThrow(new NotFoundException("Image not found"));

        mockMvc.perform(post("/api/images/{id}/comments", imageId)
                        .header(HttpHeaders.AUTHORIZATION, authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Image not found"));
    }

    @Test
    void updateComment_success() throws Exception {
        UUID imageId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        CommentDto request = new CommentDto();
        request.setContent("Updated");

        CommentDto response = new CommentDto();
        response.setContent("Updated");

        when(commentService.updateComment(eq(imageId), eq(commentId), eq(userId), any(CommentDto.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/images/{id}/comments/{commentId}", imageId, commentId)
                        .header(HttpHeaders.AUTHORIZATION, authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated"));
    }

    @Test
    void updateComment_missingAuth_shouldFail() throws Exception {
        UUID imageId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        CommentDto request = new CommentDto();
        request.setContent("Updated");

        mockMvc.perform(put("/api/images/{id}/comments/{commentId}", imageId, commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Missing or invalid Authorization header"));
    }

    @Test
    void updateComment_forbidden_shouldReturn403() throws Exception {
        UUID imageId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        CommentDto request = new CommentDto();
        request.setContent("Updated");

        when(commentService.updateComment(eq(imageId), eq(commentId), eq(userId), any(CommentDto.class)))
                .thenThrow(new ForbiddenException("Access denied"));
        mockMvc.perform(put("/api/images/{id}/comments/{commentId}", imageId, commentId)
                        .header(HttpHeaders.AUTHORIZATION, authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void deleteComment_success() throws Exception {
        UUID imageId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        mockMvc.perform(delete("/api/images/{id}/comments/{commentId}", imageId, commentId)
                        .header(HttpHeaders.AUTHORIZATION, authHeader))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteComment_missingAuth_shouldFail() throws Exception {
        UUID imageId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        mockMvc.perform(delete("/api/images/{id}/comments/{commentId}", imageId, commentId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Missing or invalid Authorization header"));
    }
}