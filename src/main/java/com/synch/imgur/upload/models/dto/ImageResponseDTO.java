package com.synch.imgur.upload.models.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class ImageResponseDTO {
    private String imageHash;
    private String imageUrl;
    private LocalDateTime uploadDate;
    private Map<String, String> metadata;
}
