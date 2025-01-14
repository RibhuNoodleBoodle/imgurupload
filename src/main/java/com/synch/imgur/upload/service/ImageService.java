package com.synch.imgur.upload.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class ImageService {

    @Value("${imgur.client.id}")
    private String clientId;

    private final RestTemplate restTemplate = new RestTemplate();

    public ResponseEntity<String> uploadImage(MultipartFile file) {
        String url = "https://api.imgur.com/3/image";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Client-ID", clientId);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        try {
            body.add("image", file.getResource());  // Use file as Resource
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing file.");
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
        return response;
    }

    public ResponseEntity<String> viewImage(String imageId) {
        String url = "https://api.imgur.com/3/image/" + imageId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Client-ID", clientId);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    public ResponseEntity<String> deleteImage(String imageId) {
        String url = "https://api.imgur.com/3/image/" + imageId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Client-ID", clientId);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
    }
}
