package com.synch.imgur.upload.service;

import com.synch.imgur.upload.config.ImgurProperties;
import com.synch.imgur.upload.exceptions.ImageProcessingException;
import com.synch.imgur.upload.exceptions.ImgurApiException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ImgurService {

    private final RestTemplate imgurRestTemplate;
    private final ImgurProperties imgurProperties;

    // Constructor injection ensures required dependencies are provided
    public ImgurService(@Qualifier("imgurRestTemplate") RestTemplate imgurRestTemplate,
                        ImgurProperties imgurProperties) {
        this.imgurRestTemplate = imgurRestTemplate;
        this.imgurProperties = imgurProperties;
    }

    /**
     * Uploads an image to Imgur and returns the image data including the deleteHash
     * for future deletion capability.
     *
     * @param file The image file to upload
     * @return Map containing image URL, deleteHash, and other metadata
     * @throws ImageProcessingException if there's an error processing the image
     * @throws ImgurApiException if there's an error communicating with Imgur
     */
    public Map<String, String> uploadImage(MultipartFile file) {
        log.info("Starting image upload process for file: {}", file.getOriginalFilename());

        try {
            // Prepare the image data for upload
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            body.add("image", fileResource);

            // Set up the HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Create the HTTP request entity
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Make the API call to Imgur
            log.debug("Sending upload request to Imgur API");
            ResponseEntity<JsonNode> response = imgurRestTemplate.exchange(
                    imgurProperties.getUploadEndpoint(),
                    HttpMethod.POST,
                    requestEntity,
                    JsonNode.class
            );

            // Process the response
            if (response.getBody() != null && response.getBody().get("success").asBoolean()) {
                JsonNode data = response.getBody().get("data");
                Map<String, String> imageData = new HashMap<>();
                imageData.put("imageUrl", data.get("link").asText());
                imageData.put("deleteHash", data.get("deletehash").asText());
                imageData.put("imageHash", data.get("id").asText());

                log.info("Successfully uploaded image. URL: {}", imageData.get("imageUrl"));
                return imageData;
            } else {
                throw new ImgurApiException("Failed to upload image to Imgur");
            }

        } catch (IOException e) {
            log.error("Error processing image file: {}", e.getMessage());
            throw new ImageProcessingException("Failed to process image file", e);
        } catch (RestClientException e) {
            log.error("Error communicating with Imgur API: {}", e.getMessage());
            throw new ImgurApiException("Failed to communicate with Imgur API", e);
        }
    }

    /**
     * Retrieves image information from Imgur.
     *
     * @param imageHash The hash/ID of the image to retrieve
     * @return Map containing image metadata
     * @throws ImgurApiException if there's an error communicating with Imgur
     */
    public Map<String, String> getImage(String imageHash) {
        log.info("Retrieving image information for hash: {}", imageHash);

        try {
            String url = imgurProperties.getImageEndpoint().replace("{imageHash}", imageHash);

            ResponseEntity<JsonNode> response = imgurRestTemplate.getForEntity(url, JsonNode.class);

            if (response.getBody() != null && response.getBody().get("success").asBoolean()) {
                JsonNode data = response.getBody().get("data");
                Map<String, String> imageData = new HashMap<>();
                imageData.put("imageUrl", data.get("link").asText());
                imageData.put("type", data.get("type").asText());
                imageData.put("width", data.get("width").asText());
                imageData.put("height", data.get("height").asText());
                imageData.put("size", data.get("size").asText());

                log.info("Successfully retrieved image data for hash: {}", imageHash);
                return imageData;
            } else {
                throw new ImgurApiException("Failed to retrieve image from Imgur");
            }

        } catch (RestClientException e) {
            log.error("Error retrieving image from Imgur: {}", e.getMessage());
            throw new ImgurApiException("Failed to retrieve image from Imgur", e);
        }
    }

    /**
     * Deletes an image from Imgur using its deleteHash.
     *
     * @param deleteHash The deletion hash of the image
     * @throws ImgurApiException if there's an error communicating with Imgur
     */
    public void deleteImage(String deleteHash) {
        log.info("Initiating deletion of image with deleteHash: {}", deleteHash);

        try {
            String url = imgurProperties.getDeleteEndpoint().replace("{imageHash}", deleteHash);

            ResponseEntity<JsonNode> response = imgurRestTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    null,
                    JsonNode.class
            );

            if (response.getBody() == null || !response.getBody().get("success").asBoolean()) {
                throw new ImgurApiException("Failed to delete image from Imgur");
            }

            log.info("Successfully deleted image with deleteHash: {}", deleteHash);

        } catch (RestClientException e) {
            log.error("Error deleting image from Imgur: {}", e.getMessage());
            throw new ImgurApiException("Failed to delete image from Imgur", e);
        }
    }
}
