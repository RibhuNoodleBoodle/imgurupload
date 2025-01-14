package com.synch.imgur.upload.controller;

import com.synch.imgur.upload.exceptions.ImgurApiException;
import com.synch.imgur.upload.models.dto.ImageResponseDTO;
import com.synch.imgur.upload.service.ImgurService;
import com.synch.imgur.upload.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ImageControllerTest {

    @Mock
    private ImgurService imgurService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ImageController imageController;

    private MockMvc mockMvc;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // Set up MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(imageController)
                .build();

        // Create test user
        userDetails = User.withUsername("testUser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    void uploadImage_Success() throws Exception {
        // Create a test image file
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        // Mock Imgur service response
        Map<String, String> imgurResponse = new HashMap<>();
        imgurResponse.put("imageHash", "abc123");
        imgurResponse.put("deleteHash", "def456");
        imgurResponse.put("imageUrl", "https://imgur.com/abc123.jpg");
        when(imgurService.uploadImage(any())).thenReturn(imgurResponse);

        // Mock user service response
        ImageResponseDTO imageResponseDTO = ImageResponseDTO.builder()
                .imageHash("abc123")
                .imageUrl("https://imgur.com/abc123.jpg")
                .uploadDate(LocalDateTime.now())
                .build();
        when(userService.associateImageWithUser(
                eq("testUser"),
                eq("abc123"),
                eq("def456"),
                eq("https://imgur.com/abc123.jpg")
        )).thenReturn(imageResponseDTO);

        // Perform the test
        mockMvc.perform(multipart("/api/images")
                        .file(file)
                        .with(request -> {
                            request.setUserPrincipal(userDetails);
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.imageHash").value("abc123"))
                .andExpect(jsonPath("$.data.imageUrl").value("https://imgur.com/abc123.jpg"));
    }

    @Test
    void uploadImage_InvalidFileType() throws Exception {
        // Create a test file with invalid type
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "test content".getBytes()
        );

        // Perform the test
        mockMvc.perform(multipart("/api/images")
                        .file(file)
                        .with(request -> {
                            request.setUserPrincipal(userDetails);
                            return request;
                        }))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(
                        "Invalid file type. Allowed types: JPEG, PNG, GIF"));
    }

    @Test
    void uploadImage_ImgurApiError() throws Exception {
        // Create a test image file
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        // Mock Imgur service to throw exception
        when(imgurService.uploadImage(any()))
                .thenThrow(new ImgurApiException("Failed to upload to Imgur"));

        // Perform the test
        mockMvc.perform(multipart("/api/images")
                        .file(file)
                        .with(request -> {
                            request.setUserPrincipal(userDetails);
                            return request;
                        }))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").contains("Failed to upload to Imgur"));
    }

    @Test
    void getImage_Success() throws Exception {
        // Mock user service response
        ImageResponseDTO imageDTO = ImageResponseDTO.builder()
                .imageHash("abc123")
                .imageUrl("https://imgur.com/abc123.jpg")
                .uploadDate(LocalDateTime.now())
                .build();
        when(userService.getUserImage("testUser", "abc123")).thenReturn(imageDTO);

        // Mock Imgur service response
        Map<String, String> imgurData = new HashMap<>();
        imgurData.put("type", "image/jpeg");
        imgurData.put("width", "800");
        imgurData.put("height", "600");
        when(imgurService.getImage("abc123")).thenReturn(imgurData);

        // Perform the test
        mockMvc.perform(get("/api/images/abc123")
                        .with(request -> {
                            request.setUserPrincipal(userDetails);
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.imageHash").value("abc123"))
                .andExpect(jsonPath("$.data.imageUrl").value("https://imgur.com/abc123.jpg"));
    }

    @Test
    void deleteImage_Success() throws Exception {
        // Mock user service responses
        when(userService.getImageDeleteHash("testUser", "abc123"))
                .thenReturn("def456");

        // Perform the test
        mockMvc.perform(delete("/api/images/abc123")
                        .with(request -> {
                            request.setUserPrincipal(userDetails);
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Image deleted successfully"));
    }
}
