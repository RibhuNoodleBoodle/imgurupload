package com.synch.imgur.upload.controller;
import com.synch.imgur.upload.exceptions.ImgurApiException;
import com.synch.imgur.upload.models.dto.ApiResponse;
import com.synch.imgur.upload.models.dto.ImageResponseDTO;
import com.synch.imgur.upload.service.ImgurService;
import com.synch.imgur.upload.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Validated

public class ImageController {

    private final ImgurService imgurService;
    private final UserService userService;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_CONTENT_TYPES = {
            "image/jpeg", "image/png", "image/gif"
    };

    /**
     * Uploads an image to Imgur and associates it with the authenticated user.
     * Includes validation for file size and type.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImageResponseDTO>> uploadImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") @NotNull MultipartFile file) {

        log.info("Received image upload request from user: {}", userDetails.getUsername());

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("File size {} exceeds maximum allowed size", file.getSize());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "File size exceeds 10MB limit", null));
        }

        // Validate content type
        String contentType = file.getContentType();
        if (!isValidContentType(contentType)) {
            log.warn("Invalid content type: {}", contentType);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Invalid file type. Allowed types: JPEG, PNG, GIF", null));
        }

        try {
            // Upload to Imgur
            Map<String, String> imgurResponse = imgurService.uploadImage(file);

            // Associate image with user
            ImageResponseDTO imageData = userService.associateImageWithUser(
                    userDetails.getUsername(),
                    imgurResponse.get("imageHash"),
                    imgurResponse.get("deleteHash"),
                    imgurResponse.get("imageUrl")
            );

            log.info("Successfully uploaded and associated image for user: {}", userDetails.getUsername());
            return ResponseEntity.ok(new ApiResponse<>(true, "Image uploaded successfully", imageData));

        } catch (Exception e) {
            log.error("Error during image upload: ", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Failed to upload image: " + e.getMessage(), null));
        }
    }

    /**
     * Retrieves all images associated with the authenticated user.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ImageResponseDTO>>> getUserImages(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Retrieving images for user: {}", userDetails.getUsername());

        try {
            List<ImageResponseDTO> userImages = userService.getUserImages(userDetails.getUsername());
            return ResponseEntity.ok(new ApiResponse<>(true, "Images retrieved successfully", userImages));

        } catch (Exception e) {
            log.error("Error retrieving user images: ", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Failed to retrieve images: " + e.getMessage(), null));
        }
    }

    /**
     * Retrieves a specific image's details if it belongs to the authenticated user.
     */
    @GetMapping("/{imageHash}")
    public ResponseEntity<ApiResponse<ImageResponseDTO>> getImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @NotBlank String imageHash) {

        log.info("Retrieving image {} for user: {}", imageHash, userDetails.getUsername());

        try {
            ImageResponseDTO imageData = userService.getUserImage(userDetails.getUsername(), imageHash);
            if (imageData == null) {
                return ResponseEntity.notFound()
                        .build();
            }

            // Enrich with Imgur metadata
            Map<String, String> imgurData = imgurService.getImage(imageHash);
            imageData.setMetadata(imgurData);

            return ResponseEntity.ok(new ApiResponse<>(true, "Image retrieved successfully", imageData));

        } catch (ImgurApiException e) {
            log.error("Imgur API error: ", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Failed to retrieve image from Imgur", null));
        }
    }

    /**
     * Deletes an image from both Imgur and the user's association.
     */
    @DeleteMapping("/{imageHash}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @NotBlank String imageHash) {

        log.info("Deleting image {} for user: {}", imageHash, userDetails.getUsername());

        try {
            // Verify user owns the image and get deleteHash
            String deleteHash = userService.getImageDeleteHash(userDetails.getUsername(), imageHash);
            if (deleteHash == null) {
                return ResponseEntity.notFound()
                        .build();
            }

            // Delete from Imgur
            imgurService.deleteImage(deleteHash);

            // Remove user association
            userService.removeImageAssociation(userDetails.getUsername(), imageHash);

            log.info("Successfully deleted image {} for user {}", imageHash, userDetails.getUsername());
            return ResponseEntity.ok(new ApiResponse<>(true, "Image deleted successfully", null));

        } catch (Exception e) {
            log.error("Error deleting image: ", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Failed to delete image: " + e.getMessage(), null));
        }
    }

    private boolean isValidContentType(String contentType) {
        if (contentType == null) return false;
        for (String allowed : ALLOWED_CONTENT_TYPES) {
            if (allowed.equalsIgnoreCase(contentType)) return true;
        }
        return false;
    }
}

